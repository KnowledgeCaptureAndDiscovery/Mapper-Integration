/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.isi.wings.catalog.resource.api.impl.kb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import edu.isi.wings.catalog.component.api.ComponentCreationAPI;
import edu.isi.wings.catalog.component.classes.Component;
import edu.isi.wings.catalog.component.classes.ComponentTree;
import edu.isi.wings.catalog.component.classes.ComponentTreeNode;
import edu.isi.wings.catalog.component.classes.requirements.ComponentRequirement;
import edu.isi.wings.catalog.resource.api.ResourceAPI;
import edu.isi.wings.catalog.resource.classes.EnvironmentValue;
import edu.isi.wings.catalog.resource.classes.GridkitCloud;
import edu.isi.wings.catalog.resource.classes.Machine;
import edu.isi.wings.catalog.resource.classes.Software;
import edu.isi.wings.catalog.resource.classes.SoftwareEnvironment;
import edu.isi.wings.catalog.resource.classes.SoftwareVersion;
import edu.isi.wings.common.kb.KBUtils;
import edu.isi.wings.ontapi.KBAPI;
import edu.isi.wings.ontapi.KBObject;
import edu.isi.wings.ontapi.KBTriple;
import edu.isi.wings.ontapi.OntFactory;
import edu.isi.wings.ontapi.OntSpec;

public class ResourceKB implements ResourceAPI {

  public String onturl, liburl;
  private String tdbRepository;

  private OntFactory ontologyFactory;

  private KBAPI ontkb, libkb;
  
  String localhost;

  HashMap<String, KBObject> pmap;
  HashMap<String, KBObject> cmap;
  
  HashMap<String, SoftwareVersion> swcache;
  HashMap<String, Machine> mcache;
  ArrayList<String> machineWhiteList;

  public ResourceKB(Properties props) {
    this.onturl = props.getProperty("ont.resource.url");
    this.liburl = props.getProperty("lib.resource.url");
    this.localhost = this.onturl+"#Localhost";
    
    this.tdbRepository = props.getProperty("tdb.repository.dir");
    if (tdbRepository == null) {
      this.ontologyFactory = new OntFactory(OntFactory.JENA);
    } else {
      this.ontologyFactory = new OntFactory(OntFactory.JENA, this.tdbRepository);
    }
    KBUtils.createLocationMappings(props, this.ontologyFactory);

    this.initializeAPI();
  }

  protected void initializeAPI() {
    try {
      this.ontkb = this.ontologyFactory.getKB(onturl, OntSpec.PLAIN, false, true);
      this.libkb = this.ontologyFactory.getKB(liburl, OntSpec.PLAIN);
      this.initializeMaps();
      this.initializeLibrary();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void initializeMaps() {
    this.pmap = new HashMap<String, KBObject>();
    this.cmap = new HashMap<String, KBObject>();
    for (KBObject prop : this.ontkb.getAllObjectProperties()) {
      this.pmap.put(prop.getName(), prop);
    }
    for (KBObject prop : this.ontkb.getAllDatatypeProperties()) {
      this.pmap.put(prop.getName(), prop);
    }
    this.pmap.put("type", this.ontkb.getProperty(KBUtils.RDF+"type"));
    for (KBObject cls : this.ontkb.getAllClasses()) {
      if(!cls.isAnonymous())
        this.cmap.put(cls.getName(), cls);
    }
  }
  
  private void initializeLibrary() {
    Machine m = this.getMachine(this.localhost);
    if(m == null) {
      m = new Machine(this.localhost);
      m.setHealthy(true);
      m.setHostName("Localhost");
      if(this.addMachine(this.localhost) && this.saveMachine(m))
        this.save();
    }
    else if(!m.isHealthy()) {
      m.setHealthy(true);
      if(this.saveMachine(m))
        this.save();
    }
  }
  
  private ArrayList<KBObject> getInstancesOfClass(KBObject cls, KBAPI kb) {
    ArrayList<KBObject> insts = new ArrayList<KBObject>();
    for (KBTriple triple : kb.genericTripleQuery(null, this.pmap.get("type"), cls))
      insts.add(triple.getSubject());
    return insts;
  }
  
  @Override
  public ArrayList<String> getMachineIds() {
    ArrayList<String> machineIds = new ArrayList<String>();
    for (KBObject mobj : this.getInstancesOfClass(this.cmap.get("Machine"), this.libkb))
      machineIds.add(mobj.getID());
    return machineIds;
  }

  @Override
  public ArrayList<String> getSoftwareIds() {
    ArrayList<String> softwareIds = new ArrayList<String>();
    for (KBObject swobj : this.getInstancesOfClass(this.cmap.get("SoftwareGroup"), this.libkb))
      softwareIds.add(swobj.getID());
    return softwareIds;
  }

  @Override
  public ArrayList<String> getSoftwareVersionIds(String softwareid) {
    ArrayList<String> versionIds = new ArrayList<String>();
    KBObject swobj = this.libkb.getIndividual(softwareid);
    for (KBTriple vtriple : this.libkb.genericTripleQuery(null,
        this.pmap.get("hasSoftwareGroup"), swobj))
      versionIds.add(vtriple.getSubject().getID());
    return versionIds;
  }

  @Override
  public boolean addMachine(String machineid) {
    KBObject obj = this.libkb.createObjectOfClass(machineid,
        this.cmap.get("Machine"));
    return (obj != null);
  }

  @Override
  public boolean addSoftware(String softwareid) {
    KBObject obj = this.libkb.createObjectOfClass(softwareid,
        this.cmap.get("SoftwareGroup"));
    return (obj != null);
  }

  @Override
  public boolean addSoftwareVersion(String versionid, String softwareid) {
    KBObject obj = this.libkb.createObjectOfClass(versionid,
        this.cmap.get("SoftwareVersion"));
    if (obj == null)
      return false;
    KBObject swobj = this.libkb.getIndividual(softwareid);
    if (swobj == null)
      return false;
    this.libkb.setPropertyValue(obj, this.pmap.get("hasSoftwareGroup"), swobj);
    return true;
  }

  @Override
  public boolean removeMachine(String machineid) {
    Machine m = this.getMachine(machineid);
    KBObject mobj = this.libkb.getIndividual(machineid);
    if(mobj == null)
      return false;
    ArrayList<KBObject> evalues = 
        this.libkb.getPropertyValues(mobj, pmap.get("hasEnvironment"));
    for(KBObject evalue : evalues) {
      for(KBTriple t : this.libkb.genericTripleQuery(evalue, null, null))
        this.libkb.removeTriple(t);
    }
    KBUtils.removeAllTriplesWith(this.libkb, machineid, false);
    GridkitCloud.resetNode(m);
    return true;
  }

  @Override
  public boolean removeSoftware(String softwareid) {
    Software sw = this.getSoftware(softwareid);
    
    // Remove environment variables
    for(String var : sw.getEnvironmentVariables()) {
      for (KBTriple t : this.libkb.genericTripleQuery(null, 
          pmap.get("hasEnvironmentVariable"), this.libkb.createLiteral(var))) {
        for (KBTriple st : this.libkb.genericTripleQuery(null, 
            pmap.get("hasEnvironment"), t.getSubject())) 
          this.libkb.removeTriple(st);
      }
    }
    
    // Remove software versions
    for(SoftwareVersion ver : sw.getVersions())
      this.removeSoftwareVersion(ver.getID());
    
    // Remove software
    KBUtils.removeAllTriplesWith(this.libkb, softwareid, false);
    return true;
  }

  @Override
  public boolean removeSoftwareVersion(String versionid) {
    KBUtils.removeAllTriplesWith(this.libkb, versionid, false);
    return true;
  }

  @Override
  public Machine getMachine(String machineid) {
    Machine machine = new Machine(machineid);
    KBObject mobj = this.libkb.getIndividual(machineid);
    if(mobj == null)
      return null;
    
    KBObject hostName = this.libkb.getPropertyValue(mobj, 
        pmap.get("hasHostName"));
    KBObject hostIP = this.libkb.getPropertyValue(mobj, 
        pmap.get("hasHostIP"));
    KBObject userId = this.libkb.getPropertyValue(mobj, 
        pmap.get("hasUserID"));
    KBObject userKey = this.libkb.getPropertyValue(mobj, 
        pmap.get("hasUserKey"));
    KBObject storageGB = this.libkb.getPropertyValue(mobj,
        pmap.get("hasStorageGB"));
    KBObject memoryGB = this.libkb.getPropertyValue(mobj, 
        pmap.get("hasMemoryGB"));
    KBObject is64Bit = this.libkb.getPropertyValue(mobj, 
        pmap.get("is64Bit"));
    KBObject isHealthy = this.libkb.getPropertyValue(mobj, 
        pmap.get("isHealthy"));
    KBObject storageFolder = this.libkb.getPropertyValue(mobj,
        pmap.get("hasWingsStorageFolder"));
    KBObject executionFolder = this.libkb.getPropertyValue(mobj,
        pmap.get("hasExecutionFolder"));
    KBObject os = this.libkb.getPropertyValue(mobj, 
        pmap.get("hasOperatingSystem"));
    ArrayList<KBObject> softwares = this.libkb.getPropertyValues(mobj,
        pmap.get("hasSoftware"));
    ArrayList<KBObject> environment = this.libkb.getPropertyValues(mobj,
        pmap.get("hasEnvironment"));
    if(hostName != null)
      machine.setHostName((String)hostName.getValue());
    if(hostIP != null)
      machine.setHostIP((String)hostIP.getValue());
    if(userId != null)
      machine.setUserId((String)userId.getValue());
    if(userKey != null)
      machine.setUserKey((String)userKey.getValue());
    if(memoryGB != null)
      machine.setMemoryGB((Float)memoryGB.getValue());
    if(storageGB != null)
      machine.setStorageGB((Float)storageGB.getValue());
    if(is64Bit != null)
      machine.setIs64Bit((Boolean)is64Bit.getValue());
    if(isHealthy != null)
      machine.setHealthy((Boolean)isHealthy.getValue());
    if(storageFolder != null)
      machine.setStorageFolder((String)storageFolder.getValue());
    if(executionFolder != null)
      machine.setExecutionFolder((String)executionFolder.getValue());
    if(os != null)
      machine.setOSid(os.getID());
    
    for(KBObject software : softwares) 
      machine.addSoftware(software.getID());
    
    for(KBObject env : environment) {
      KBObject evar = this.libkb.getPropertyValue(env, 
          pmap.get("hasEnvironmentVariable"));
      KBObject evalue = this.libkb.getPropertyValue(env, 
          pmap.get("hasEnvironmentValue"));
      if(evar != null && evalue != null) {
        EnvironmentValue eval = new EnvironmentValue();
        eval.setValue((String)evalue.getValue());
        eval.setVariable((String)evar.getValue());
        machine.addEnvironmentValues(eval);
      }
    }
    return machine;
  }

  @Override
  public Software getSoftware(String softwareid) {
    Software software = new Software(softwareid);
    KBObject mobj = this.libkb.getIndividual(softwareid);
    if(mobj == null)
      return null;
    software.setName(mobj.getName());
    
    ArrayList<KBObject> evars = 
        this.libkb.getPropertyValues(mobj, pmap.get("hasEnvironmentVariable"));
    for(KBObject evar : evars) {
      if(evar.getValue() != null)
        software.addEnvironmentVariable((String)evar.getValue());
    }
    
    for(String versionid : this.getSoftwareVersionIds(softwareid)) {
      software.addVersion(this.getSoftwareVersion(versionid));
    }
    return software;
  }

  @Override 
  public ArrayList<SoftwareVersion> getAllSoftwareVersions() {
    ArrayList<SoftwareVersion> versions = new ArrayList<SoftwareVersion>();
    for (KBObject verobj : this.getInstancesOfClass(this.cmap.get("SoftwareVersion"), this.libkb))
      versions.add(this.getSoftwareVersion(verobj.getID()));
    return versions;
  }
  
  @Override 
  public ArrayList<SoftwareEnvironment> getAllSoftwareEnvironment() {
    ArrayList<SoftwareEnvironment> environment = 
        new ArrayList<SoftwareEnvironment>();
    for (KBObject swobj : this.getInstancesOfClass(this.cmap.get("SoftwareGroup"), this.libkb)) {
        ArrayList<KBObject> evars = 
          this.libkb.getPropertyValues(swobj, pmap.get("hasEnvironmentVariable"));
        for(KBObject evar : evars) {
          SoftwareEnvironment env = new SoftwareEnvironment();
          env.setSoftwareGroupId(swobj.getID());
          if(evar.getValue() != null) {
            env.setVariable((String)evar.getValue());
            environment.add(env);
          }
        };
    }
    return environment;
  }
  
  @Override
  public SoftwareVersion getSoftwareVersion(String versionid) {
    SoftwareVersion version = new SoftwareVersion(versionid);
    KBObject mobj = this.libkb.getIndividual(versionid);
    if(mobj == null)
      return null;
    
    KBObject vernum = 
        this.libkb.getPropertyValue(mobj, pmap.get("hasVersionNumber"));
    KBObject vertext = 
        this.libkb.getPropertyValue(mobj, pmap.get("hasVersionText"));
    KBObject group = 
        this.libkb.getPropertyValue(mobj, pmap.get("hasSoftwareGroup"));
    if(vernum != null)
        version.setVersionNumber((Integer)vernum.getValue());
    if(vertext != null)
      version.setVersionText((String)vertext.getValue());
    if(group != null)
      version.setSoftwareGroupId(group.getID());
    return version;
  }

  private void createCaches() {
    if(swcache == null) {
      swcache = new HashMap<String, SoftwareVersion>();
      for(SoftwareVersion swver : this.getAllSoftwareVersions()) {
        swcache.put(swver.getID(), swver);
      }
    }
    if(mcache == null) {
      mcache = new HashMap<String, Machine>();
      for(String mid : this.getMachineIds()) {
        mcache.put(mid, this.getMachine(mid));
      }
    }
  }
  
  @Override
  public ArrayList<String> getMatchingMachineIds(ComponentRequirement req) {
    ArrayList<String> machineIds = new ArrayList<String>();
    this.createCaches();
    
    for(String machineId : mcache.keySet()) {
      Machine machine = mcache.get(machineId);

      if(this.machineWhiteList != null && !this.machineWhiteList.contains(machineId))
        continue;
      
      // Check that the machine is "healthy" - could be fetched live ?
      if(!machine.isHealthy())
        continue;
      
      // The machine's memory/storage - could be live information ?
      if(req.getMemoryGB() > machine.getMemoryGB())
        continue;
      if(req.getStorageGB() > machine.getStorageGB())
        continue;
      
      boolean ok = true;
      // Check all required softwares
      for(String softwareId : req.getSoftwareIds()) {
        boolean swmatch = false;
        SoftwareVersion swver = swcache.get(softwareId);
        // Check that each machine has the required software version
        //  - OR a higher version of the same software
        for(String mswid : machine.getSoftwareIds()) {
          SoftwareVersion mswver = swcache.get(mswid);
          if(mswver == null || swver == null)
            continue;
          if(mswver.getSoftwareGroupId().equals(swver.getSoftwareGroupId()) &&
              mswver.getVersionNumber() >= swver.getVersionNumber()) {
            swmatch = true;
            break;
          }
        }
        if(!swmatch) {
          ok = false;
          break;
        }
      }
      if(ok)
        machineIds.add(machineId);
    }
    return machineIds;
  }
  
  @Override
  public boolean saveMachine(Machine machine) {
    KBObject mobj = this.libkb.getIndividual(machine.getID());
    if(mobj == null)
      return false;
    
    // Remove existing & re-add
    this.removeMachine(machine.getID());
    this.addMachine(machine.getID());
    
    if(!machine.getID().equals(this.localhost)) {
      this.libkb.setPropertyValue(mobj, pmap.get("hasHostName"), 
          this.libkb.createLiteral(machine.getHostName()));
      this.libkb.setPropertyValue(mobj, pmap.get("hasHostIP"), 
          this.libkb.createLiteral(machine.getHostIP()));
    }
    this.libkb.setPropertyValue(mobj, pmap.get("hasUserID"), 
        this.libkb.createLiteral(machine.getUserId()));
    this.libkb.setPropertyValue(mobj, pmap.get("hasUserKey"), 
        this.libkb.createLiteral(machine.getUserKey()));
    this.libkb.setPropertyValue(mobj, pmap.get("hasStorageGB"), 
        this.libkb.createLiteral(machine.getStorageGB()));
    this.libkb.setPropertyValue(mobj, pmap.get("hasMemoryGB"), 
        this.libkb.createLiteral(machine.getMemoryGB()));
    this.libkb.setPropertyValue(mobj, pmap.get("is64Bit"), 
        this.libkb.createLiteral(machine.is64Bit()));
    this.libkb.setPropertyValue(mobj, pmap.get("isHealthy"), 
        this.libkb.createLiteral(machine.isHealthy()));
    this.libkb.setPropertyValue(mobj, pmap.get("hasExecutionFolder"), 
        this.libkb.createLiteral(machine.getExecutionFolder()));
    this.libkb.setPropertyValue(mobj, pmap.get("hasWingsStorageFolder"), 
        this.libkb.createLiteral(machine.getStorageFolder()));
    
    if(machine.getOSid() != null)
      this.libkb.setPropertyValue(mobj, pmap.get("hasOperatingSystem"), 
          this.libkb.getIndividual(machine.getOSid()));

    if(machine.getSoftwareIds() != null) {
      for(String softwareId : machine.getSoftwareIds()) {
        this.libkb.addPropertyValue(mobj, pmap.get("hasSoftware"), 
            this.libkb.getIndividual(softwareId));
      }
    }
    if(machine.getEnvironmentValues() != null) {
      for(EnvironmentValue evalue : machine.getEnvironmentValues()) {
        KBObject eobj = 
            this.libkb.createObjectOfClass(null, cmap.get("EnvironmentValue"));
        this.libkb.setPropertyValue(eobj, pmap.get("hasEnvironmentVariable"), 
            this.libkb.createLiteral(evalue.getVariable()));
        this.libkb.setPropertyValue(eobj, pmap.get("hasEnvironmentValue"), 
            this.libkb.createLiteral(evalue.getValue()));
        this.libkb.addPropertyValue(mobj, pmap.get("hasEnvironment"), eobj);
      }
    }
    return true;
  }

  @Override
  public boolean saveSoftware(Software software) {
    KBObject mobj = this.libkb.getIndividual(software.getID());
    if(mobj == null)
      return false;
    
    // Create version ids if null & Remove empty versions
    for(SoftwareVersion version : software.getVersions()) {
      if(version.getVersionText() == null || 
          version.getVersionText().equals("")) {
        software.getVersions().remove(version);
      }
      if(version.getID() == null) {
        String versionId = 
          software.getID()+"_"+KBUtils.sanitizeID(version.getVersionText());
        version.setID(versionId);
      }
    }
    // Remove software versions
    Software osoftware = this.getSoftware(software.getID());
    for(SoftwareVersion oversion : osoftware.getVersions()) {
      boolean removed = true;
      for(SoftwareVersion version : software.getVersions()) {
        if(oversion.getID().equals(version.getID())) {
          removed = false;
          break;
        }
      }
      if(removed)
        this.removeSoftwareVersion(oversion.getID());
    }
    // Add/Update software versions
    for(SoftwareVersion version : software.getVersions()) {
      boolean added = true;
      for(SoftwareVersion oversion : osoftware.getVersions()) {
        if(oversion.getID().equals(version.getID())) {
          added = false;
          break;
        }
      }
      if(added) {
        this.addSoftwareVersion(version.getID(), software.getID());
        this.saveSoftwareVersion(version);
      }
      else {
        this.saveSoftwareVersion(version);
      }
    }

    // Update environment variables
    KBObject evarprop = pmap.get("hasEnvironmentVariable");
    for (String evar : osoftware.getEnvironmentVariables()) {
      this.libkb.removeTriple(mobj, evarprop, this.libkb.createLiteral(evar));
    }
    for (String evar : software.getEnvironmentVariables()) {
      this.libkb.addPropertyValue(mobj, evarprop,
          this.libkb.createLiteral(evar));
    }
    
    return true;
  }

  @Override
  public boolean saveSoftwareVersion(SoftwareVersion version) {
    KBObject mobj = this.libkb.getIndividual(version.getID());
    if(mobj == null)
      return false;
    
    this.libkb.setPropertyValue(mobj, pmap.get("hasVersionNumber"), 
        this.libkb.createLiteral(version.getVersionNumber()));
    this.libkb.setPropertyValue(mobj, pmap.get("hasVersionText"), 
        this.libkb.createLiteral(version.getVersionText()));
    return true;
  }

  @Override
  public void setMachineWhitelist(ArrayList<String> whitelist) {
    this.machineWhiteList = whitelist;
  }
  
  public void copyFrom(ResourceAPI rc, ComponentCreationAPI cc) {
    HashMap<String, SoftwareVersion> sws = new HashMap<String, SoftwareVersion>();
    ComponentTree tree = cc.getComponentHierarchy(true);
    ArrayList<ComponentTreeNode> nodes = new ArrayList<ComponentTreeNode>();
    nodes.add(tree.getRoot());
    while(!nodes.isEmpty()) {
      ComponentTreeNode node = nodes.remove(0);
      Component comp = node.getCls().getComponent();
      if(comp != null && comp.getType() == Component.CONCRETE) {
        for(String swid : comp.getComponentRequirement().getSoftwareIds()) {
          sws.put(swid, rc.getSoftwareVersion(swid));
        }
      }
      nodes.addAll(node.getChildren());
    }
    
    for(String verid : sws.keySet()) {
      SoftwareVersion ver = sws.get(verid);
      if(ver == null)
        continue;
      
      Software sw = rc.getSoftware(ver.getSoftwareGroupId());
      
      String myverid = this.liburl + "#" + ver.getName();
      String myswid = this.liburl + "#" + sw.getName();
      
      if(this.getSoftwareVersion(myverid) == null) {
        Software mysw = this.getSoftware(myswid);
        if(mysw == null) {
          this.addSoftware(myswid);
          mysw = this.getSoftware(myswid);
          mysw.setEnvironmentVariables(sw.getEnvironmentVariables());
        }
        ver.setID(myverid);
        mysw.addVersion(ver);
        this.saveSoftware(mysw);
      }
    } 
    this.save();
  }
  
  @Override
  public boolean save() {
    if (this.libkb != null)
      return this.libkb.save();
    return false;
  }

  @Override
  public void end() {
    if (this.ontkb != null)
      this.ontkb.end();
    if (this.libkb != null)
      this.libkb.end();
  }

  @Override
  public void delete() {
    if (this.libkb != null)
      this.libkb.delete();
  }
}
