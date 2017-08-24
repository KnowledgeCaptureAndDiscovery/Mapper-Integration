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

package edu.isi.wings.catalog.data.api;

import java.util.ArrayList;
import java.util.HashMap;

import edu.isi.wings.catalog.data.classes.DataItem;
import edu.isi.wings.catalog.data.classes.DataTree;
import edu.isi.wings.catalog.data.classes.MetadataProperty;
import edu.isi.wings.catalog.data.classes.MetadataValue;

/**
 * The interface used by data catalog viewers to query the data. Read/Only
 * access
 */
public interface DataCreationAPI {
	// Query
	DataTree getDataHierarchy(); // Tree List of Data and Datatypes
	
	DataTree getDatatypeHierarchy(); // Tree List of Datatypes

	DataTree getMetricsHierarchy(); // Tree List of Metrics and Metric types

	ArrayList<String> getAllDatatypeIds();
	
	HashMap<String, ArrayList<String>> getAllDatatypeDatasets();

	MetadataProperty getMetadataProperty(String propid);

	ArrayList<MetadataProperty> getMetadataProperties(String dtypeid, boolean direct);

	ArrayList<MetadataProperty> getAllMetadataProperties();

	DataItem getDatatypeForData(String dataid);

	ArrayList<DataItem> getDataForDatatype(String dtypeid, boolean direct);

	String getTypeNameFormat(String dtypeid);

	String getDataLocation(String dataid);

	ArrayList<MetadataValue> getMetadataValues(String dataid, ArrayList<String> propids);

	// Write
	boolean addDatatype(String dtypeid, String parentid);

	boolean removeDatatype(String dtypeid);

	boolean renameDatatype(String newtypeid, String oldtypeid);

	boolean moveDatatypeParent(String dtypeid, String fromtypeid, String totypeid);

	boolean moveDataParent(String dataid, String fromtypeid, String totypeid);
	
	boolean addData(String dataid, String dtypeid);

	boolean renameData(String newdataid, String olddataid);

	boolean removeData(String dataid);

	boolean setDataLocation(String dataid, String locuri);

	boolean setTypeNameFormat(String dtypeid, String format);

	boolean addObjectPropertyValue(String dataid, String propid, String valid);

	boolean addDatatypePropertyValue(String dataid, String propid, Object val);

	boolean addDatatypePropertyValue(String dataid, String propid, String val, String xsdtype);

	boolean removePropertyValue(String dataid, String propid, Object val);

	boolean removeAllPropertyValues(String dataid, ArrayList<String> propids);

	boolean addMetadataProperty(String propid, String domain, String range);
	
	boolean addMetadataPropertyDomain(String propid, String domain);

	boolean removeMetadataProperty(String propid);
	
	boolean removeMetadataPropertyDomain(String propid, String domain);

	boolean renameMetadataProperty(String oldid, String newid);

	// Sync/Save
	boolean save();
	
	void end();
	
	void delete();
	
	// Copy from another API (Advisable to give the same implementation of the API here)
	void copyFrom(DataCreationAPI dc);

	// Get/Set external data catalog to copy from
	DataCreationAPI getExternalCatalog();
	
	void setExternalCatalog(DataCreationAPI dc);
}
