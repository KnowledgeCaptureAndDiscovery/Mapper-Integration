package edu.isi.wings.portal.resources;

import javax.annotation.PostConstruct;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;

import edu.isi.wings.portal.controllers.DataController;

@Path("{user}/{domain}/data{external:(/external)?}")
public class DataResource extends WingsResource {
  DataController dc;

  @PathParam("external") String external;
  boolean loadExternal;
  
  @PostConstruct
  public void init() {
    super.init();
    this.loadExternal = ("/external".equals(external));
    if(this.hasPermissions() && !this.isPage("intro"))
      this.dc = new DataController(config, this.loadExternal);
  }
  
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    if(this.dc != null) {
      request.setAttribute("controller", this.dc);
      return this.callViewer("DataViewer");
    }
    return null;
  }
  
  @GET
  @Path("intro")
  public void getIntroduction() {
    String introPage = "ManageData" + (loadExternal ? "External" : "");
    this.loadIntroduction(introPage);
  }
  
  @GET
  @Path("getDataJSON")
  @Produces(MediaType.APPLICATION_JSON)
  public String getDataJSON(  
      @QueryParam("data_id") String data_id) {
    if(this.dc != null)
      return this.dc.getDataJSON(data_id);
    return null;
  }
  
  @GET
  @Path("getDataTypeJSON")
  @Produces(MediaType.APPLICATION_JSON)
  public String getDataTypeJSON(
      @QueryParam("data_type") String data_type) {
    if(this.dc != null)
      return this.dc.getDatatypeJSON(data_type);
    return null;
  }
  
  @GET
  @Path("getDataHierarchyJSON")
  @Produces(MediaType.APPLICATION_JSON)
  public String getDataHierarchyJSON() {
    if(this.dc != null)
      return this.dc.getDataHierarchyJSON();
    return null;
  }
  
  @GET
  @Path("getDataListJSON")
  @Produces(MediaType.APPLICATION_JSON)
  public String getDataListJSON() {
    if(this.dc != null)
      return this.dc.getDataListJSON();
    return null;
  }
  
  @GET
  @Path("fetch")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response fetchData(
      @QueryParam("data_id") String data_id) {
    if(this.dc != null)
      return this.dc.streamData(data_id, this.context);
    return Response.status(Status.FORBIDDEN).build();    
  }
  
  @POST
  @Path("saveDataJSON")
  @Produces(MediaType.TEXT_PLAIN)
  public String saveDataJSON(
      @FormParam("data_id") String data_id,
      @FormParam("propvals_json") String json) {
    if(this.dc != null && this.isOwner() &&
        this.dc.saveDataJSON(data_id, json))
      return "OK";
    return null;
  }
  
  @POST
  @Path("saveDataTypeJSON")
  @Produces(MediaType.APPLICATION_JSON)
  public String saveDataTypeJSON(
      @FormParam("data_type") String data_type,
      @FormParam("props_json") String json) {
    if(this.dc != null && this.isOwner() && !config.isSandboxed())
      return this.dc.saveDatatypeJSON(data_type, json);
    return null;
  }
  
  @POST
  @Path("newDataType")
  @Produces(MediaType.TEXT_PLAIN)
  public String newDataType(
      @FormParam("data_type") String data_type,
      @FormParam("parent_type") String parent_type) {
    if(this.dc != null && this.isOwner() && !config.isSandboxed() &&
        this.dc.addDatatype(parent_type, data_type))
      return "OK";
    return null;
  }
  
  @POST
  @Path("delDataTypes")
  @Produces(MediaType.TEXT_PLAIN)
  public String delDataTypes(
      @FormParam("data_type") String dtypes) {
    Gson gson = new Gson();
    String[] data_types = gson.fromJson(dtypes, String[].class);
    if(this.dc != null && this.isOwner() && !config.isSandboxed() &&
        this.dc.delDatatypes(data_types))
      return "OK";
    return null;
  }
  
  @POST
  @Path("moveDatatypeTo")
  @Produces(MediaType.TEXT_PLAIN)
  public String moveDatatypeTo(
      @FormParam("data_type") String data_type,
      @FormParam("from_parent_type") String from_parent_type,
      @FormParam("to_parent_type") String to_parent_type) {
    if(this.dc != null && this.isOwner() && !config.isSandboxed() &&
        this.dc.moveDatatypeTo(data_type, from_parent_type, to_parent_type))
      return "OK";
    return null;
  }
  
  @POST
  @Path("moveDataTo")
  @Produces(MediaType.TEXT_PLAIN)
  public String moveDataTo(
      @FormParam("data_id") String data_id,
      @FormParam("from_parent_type") String from_parent_type,
      @FormParam("to_parent_type") String to_parent_type) {
    if(this.dc != null && this.isOwner() && !config.isSandboxed() &&
        this.dc.moveDataTo(data_id, from_parent_type, to_parent_type))
      return "OK";
    return null;
  }
  
  @POST
  @Path("addDataForType")
  @Produces(MediaType.TEXT_PLAIN)
  public String addDataForType(
      @FormParam("data_id") String data_id,
      @FormParam("data_type") String data_type) {
    if(this.dc != null && this.isOwner() && !config.isSandboxed() &&
        this.dc.addDataForDatatype(data_id, data_type))
      return "OK";
    return null;
  }
  
  @POST
  @Path("delData")
  @Produces(MediaType.TEXT_PLAIN)
  public String delData(
      @FormParam("data_id") String data_id) {
    if(this.dc != null && this.isOwner() && !config.isSandboxed() &&
        this.dc.delData(data_id))
      return "OK";
    return null;
  }
  
  @POST
  @Path("renameData")
  @Produces(MediaType.TEXT_PLAIN)
  public String renameData(
      @FormParam("data_id") String data_id,
      @FormParam("newid") String newid) {
    if(this.dc != null && this.isOwner() && !config.isSandboxed() &&
        this.dc.renameData(data_id, newid))
      return "OK";
    return null;
  }
  
  @POST
  @Path("renameDataType")
  @Produces(MediaType.TEXT_PLAIN)
  public String renameDataType(
      @FormParam("data_type") String data_type,
      @FormParam("newid") String newid) {
    if(this.dc != null && this.isOwner() && !config.isSandboxed() &&
        this.dc.renameDataType(data_type, newid))
      return "OK";
    return null;
  }
  
  @POST
  @Path("setDataLocation")
  @Produces(MediaType.TEXT_PLAIN)
  public String setDataLocation(
      @FormParam("data_id") String data_id,
      @FormParam("location") String location) {
    if(this.dc != null && this.isOwner() && !config.isSandboxed() &&
        this.dc.setDataLocation(data_id, location))
      return "OK";
    return null;
  }
  
  @POST
  @Path("addBatchData")
  @Produces(MediaType.TEXT_PLAIN)
  public String addBatchData(
      @FormParam("data_type") String data_type,
      @FormParam("data_ids") String dids,
      @FormParam("data_locations") String locs) {
    Gson gson = new Gson();
    String[] data_ids = gson.fromJson(dids, String[].class);
    String[] data_locations = gson.fromJson(locs, String[].class);
    if(this.dc != null && this.isOwner() && !config.isSandboxed() &&
        this.dc.addBatchData(data_type, data_ids, data_locations))
      return "OK";
    return null;
  }
  
  @POST
  @Path("registerData")
  @Produces(MediaType.TEXT_PLAIN)
  public String registerData(
      @FormParam("data_id") String data_id,
      @FormParam("newname") String newname,
      @FormParam("metadata_json") String json) {
    if(this.dc != null && this.isOwner() && !config.isSandboxed())        
      return this.dc.registerData(data_id, newname, json);
    return null;
  }
  
  @POST
  @Path("importFromExternalCatalog")
  @Produces(MediaType.TEXT_PLAIN)
  public String importFromExternalCatalog(
      @FormParam("data_id") String data_id,
      @FormParam("data_type") String data_type,
      @FormParam("propvals_json") String json,
      @FormParam("location") String location) {
    if(this.dc != null && this.isOwner() && !config.isSandboxed() &&        
        this.dc.importFromExternalCatalog(data_id, data_type, json, location))
      return "OK";
    return null;
  }

}