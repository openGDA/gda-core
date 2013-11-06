/*-
 * Copyright © 2012 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.data.scan.datawriter;

import gda.data.nexus.extractor.NexusExtractor;

import java.util.List;
import java.util.Vector;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;

import org.nexusformat.NXlink;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to create addition links in a nexus file.
 * Links can be to datasets within the same file or in external files
 */
public class NXLinkCreator {

	private static final Logger logger = LoggerFactory.getLogger(NXLinkCreator.class);
	
	List<SubEntryLink> links = new Vector<SubEntryLink>();
	public NXLinkCreator() {
	}

	public void makelinks(String filename) throws Exception {
		NexusFile file = new NexusFile(filename,NexusFile.NXACC_RDWR);
		try {
			for( SubEntryLink link : links){
				String value = link.value;
				String key = link.key;
				if( value.startsWith("nxfile")){
					makelink(file, key, null, value);
					
				} else if( value.startsWith("#")){
					makelink(file, key, null, "nxfile://"+filename+value);
				} else if( value.startsWith("!")){
					String unavailable = "";
					String path = value.split("!")[1];
					String linkInfo = getExternalLinkInfo(file, path, NexusExtractor.SDSClassName, unavailable);
					logger.debug("DBG: linkInfo = "+ linkInfo);
					if( linkInfo == null || linkInfo==unavailable){
						String [] parts = path.split("/");
						String [] pathParts = new String [parts.length];
						String [] classParts = new String [parts.length];
						for( int i=0; i<parts.length; i++){
							String [] subParts = parts[i].split(":");
							pathParts[i] = subParts[0];
							classParts[i] = subParts[1];
						}
						
						String pathToGroup = "";
						for( int i=0; i<pathParts.length-1; i++){
							pathToGroup += "/" + pathParts[i];
						}
						
						String groupName = pathParts[pathParts.length-1];
						
						logger.debug("DBG: pathToGroup = "+ pathToGroup);
						logger.debug("DBG: groupName = "+ groupName);
						
						int fid = -1;
						fid = H5.H5Fopen(filename, HDF5Constants.H5F_ACC_RDWR, HDF5Constants.H5P_DEFAULT);
						
						int gid = -1;
						gid = H5.H5Gopen(fid, pathToGroup, HDF5Constants.H5P_DEFAULT);
						
						String[] linkName = new String[2]; // file name and file path
						
						linkInfo = "nxfile://";
						linkInfo += linkName[1] + "#" + linkName[0];
						logger.debug("DBG: linkInfo 1 = "+ linkInfo);
						
						if (gid >= 0) 
							H5.H5Gclose(gid);
						
						if (fid >= 0)
							H5.H5Fclose(fid);
					}
					makelink(file, key, null, linkInfo);
				} else {
					try{
						NXlink nxlink = getLink(file,value);
						makelink(file, key, nxlink, null);
					} catch(Exception e){
						throw new Exception("Error making link for " + value + " in file " + file, e);
					}
				}
				
			}
			
		} finally {
			file.flush();
			try {
				file.finalize();
			} catch (Throwable e) {
				throw new Exception("Error finalising " + filename,e);
			} finally {
				file.close();
				
			}
		}
	}
	private void makelink(NexusFile file, String path, NXlink link, String url) throws Exception {
		String [] parts = path.split("/",2);
		if( parts[0].isEmpty()){
			if( parts.length>1){
				makelink(file,parts[1], link, url);
			}
			return;
		}
		if( parts.length > 1){
			String []subParts = parts[0].split(":");
			String name = subParts[0];
			String nxClass = subParts[1];
			if( !(file.groupdir().containsKey(name) && file.groupdir().get(name).equals(nxClass))){
				try {
					// workaround because opengroup(x) sometimes fails after a call to makegroup(x)
					file.opengroup(name, nxClass);
					file.closegroup();
				} catch (Throwable e) {
					file.makegroup(name, nxClass);
				}
			}
			file.opengroup(name, nxClass);
			try{
				makelink(file, parts[1], link, url);
			} finally {
				file.closegroup();
			}
			return;
		}
		if( link != null)
			file.makenamedlink(parts[0], link);
		if( url != null)
			file.linkexternaldataset(parts[0],url);
	}


	private NXlink getLink(NexusFile file, String path) throws NexusException, Exception {
		String [] parts = path.split("/",2);
		if( parts[0].isEmpty()){
			if( parts.length>1){
				return getLink(file,parts[1]);
			}
			return null;
		}
		String []subParts = parts[0].split(":");
		String name = subParts[0];
		String nxClass = subParts[1];
		if( parts.length > 1){
			if( !(file.groupdir().containsKey(name) && file.groupdir().get(name).equals(nxClass))){
				throw new Exception("Item not found " + name + ":" + nxClass);
			}
			file.opengroup(name, nxClass);
			try{
				return getLink(file, parts[1]);
			} finally {
				file.closegroup();
			}
		}
		NXlink link=null;
		if( nxClass.equals(NexusExtractor.NXDataClassName))
		{
			try{
				file.opendata(name);
			} catch( NexusException ne){
				throw new Exception("Error calling opendata for "+name, ne);
			}
			link = file.getdataID();
			file.closedata();
		} 
		return link;
	}

	private String getExternalLinkInfo(NexusFile file, String path, String dataClassName, String unavailable) throws NexusException, Exception {
		String [] parts = path.split("/",2);
		if( parts[0].isEmpty()){
			if( parts.length>1){
				return getExternalLinkInfo(file,parts[1],dataClassName, unavailable);
			}
			return null;
		}
		String []subParts = parts[0].split(":");
		String name = subParts[0];
		String nxClass = subParts[1];
		if( parts.length > 1){
			if( !(file.groupdir().containsKey(name) && file.groupdir().get(name).equals(nxClass))){
				throw new Exception("Item not found " + name + ":" + nxClass);
			}
			file.opengroup(name, nxClass);
			try{
				return getExternalLinkInfo(file, parts[1],dataClassName, unavailable);
			} finally {
				file.closegroup();
			}
		}
		String linkInfo = unavailable;
		if( nxClass.equals(dataClassName))
		{
			linkInfo = file.isexternaldataset("data");
		} 
		return linkInfo;
	}

	/**
	 * Define the key to be generated  and its target
	 * Formats for value:
	 * "/entry1:NXentry/default:NXdata/simpleScannable1:NXdata" - creates a hard link to a location in current file
	 * "nxfile://<filePath>#entry1/SimpleDetector1/simpleScannable1" - create soft link to a location in other file
	 * "#entry2/test2" - create soft link to location in current file - NB h5py module in Python cannot read this
	 */
	public void addLink(String key, String value) {
		links.add( new SubEntryLink(key, value));
	}

	class SubEntryLink{
		public String key;
		public String value;
		public SubEntryLink(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}
		
	}

}

