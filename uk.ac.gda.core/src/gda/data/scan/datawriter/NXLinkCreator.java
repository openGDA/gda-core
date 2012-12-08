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

import org.nexusformat.NXlink;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

/**
 * Class used to create addition links in a nexus file.
 * Links can be to datasets within the same file or in external files
 */
public class NXLinkCreator {

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
				} else {
					NXlink nxlink = getLink(file,value);
					makelink(file, key, nxlink, null);
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
				file.makegroup(name, nxClass);
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

