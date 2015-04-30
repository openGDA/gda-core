/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.data.metadata;

import gda.data.nexus.NexusUtils;
import gda.factory.Findable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NexusMetadataReader class.
 */
public class NexusMetadataReader implements Findable {
	
	private static final Logger logger = LoggerFactory.getLogger(NexusMetadataReader.class);
	
	NexusFile nf = null;

	String filename = null;

	String name = null;

	static ArrayList<NexusMetadataEntry> nexusMetadataEntries = new ArrayList<NexusMetadataEntry>();

	/**
	 * Constructor.
	 */
	public NexusMetadataReader() {
	}

	/**
	 * Constructor.
	 * 
	 * @param filename
	 */
	public NexusMetadataReader(String filename) {
		this.filename = filename;
	}

//	private String getString(NexusFile nf, GroupNode group, String item) throws NexusException {
////		byte bytes[];
////		String string = null;
//
//		DataNode data = nf.getData(group, item);
//		ILazyDataset lazy = data.getDataset();
//		if (lazy == null)
//			return null;
//
//		if (!lazy.elementClass().equals(String.class))
//			return null;
//
//		IDataset d = lazy.getSlice();
//		return d.getString();
////		nf.opendata(item);
////		bytes = new byte[getDimension(nf)];
////		nf.getdata(bytes);
////		string = new String(bytes);
////
////		return string;
//	}

	private String getString(DataNode data){
		ILazyDataset lazy = data.getDataset();
		if (lazy == null)
			return null;

		if (!lazy.elementClass().equals(String.class))
			return null;

		IDataset d = lazy.getSlice();
		return d.getString();
	}

//	private int getDimension(NexusFile nf) {
//		int dimension = 0;
//		int arg0[] = new int[20];
//		int arg1[] = new int[20];
//
//		try {
//			nf.getinfo(arg0, arg1);
//			dimension = arg0[0];
//		} catch (NexusException e) {
//			// ignore
//		}
//
//		return dimension;
//	}

	/**
	 * Gets the metadata item with a given name.
	 * 
	 * @param name
	 * @return metadata
	 * @throws NexusException 
	 */
	public String getNamedNexusMetadata(String name) throws NexusException {

		for (NexusMetadataEntry nme : nexusMetadataEntries) {
			if (nme.getName().equals(name)) {
				String location = nme.getAccessName();
				return getNexusMetadata(location);
			}
		}

		// FIXME should we not be able to decide whether name exists as empty or is not there at all?
		return "";
	}

	private String transformAccessName(String accessName) {
		if (accessName == null)
			return null;

		String path = accessName.replace(':', '/');
		path = path.replace('%', ':');
		return path;
	}

	/**
	 * Gets the metadata item at a given location.
	 * 
	 * @param location
	 * @return metadata
	 * @throws NexusException 
	 */
	public String getNexusMetadata(String location) throws NexusException {
		nf = NexusUtils.createNXFile(filename);
		nf.openToRead();
		return getString(nf.getData(transformAccessName(location)));
//		
//		String error = null;
//		String metadata = "";
//		String temp;
//		String name = "";
//		String nexusClass = "";
//
//		StringTokenizer st1 = new StringTokenizer(location, ":");
//		StringTokenizer st2;
//
//		// use colon to separate groups
//		// in each group, use percent to separate NXclass
//		try {
//			nf = NexusUtils.createNXFile(filename);
//			nf.openToRead();
//			while (st1.hasMoreTokens()) {
//				temp = st1.nextToken();
//				if (!st1.hasMoreTokens()) {
//					metadata = getString(nf, temp);
//				} else {
//					st2 = new StringTokenizer(temp, "%");
//					if (st2.hasMoreTokens()) {
//						name = st2.nextToken();
//					}
//					if (st2.hasMoreTokens()) {
//						nexusClass = st2.nextToken();
//					}
//					nf.opengroup(name, nexusClass);
//				}
//			}
//
//			nf.finalize();
//		} catch (NexusException ne) {
//			error = "NeXusException occurred when accessing NeXus file\n" + ne.getMessage();
//		} catch (Throwable et) {
//			error = "Error occurred when accessing NeXus file\n" + et.getMessage();
//		}
//
//		if (error != null) {
//			logger.debug(error);
//		}
//
//		return metadata;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Adds a metadata entry.
	 * 
	 * @param entry
	 */
	public void addNexusMetadataEntry(NexusMetadataEntry entry) {
		NexusMetadataEntry nme;
		boolean found = false;

		for (Iterator<NexusMetadataEntry> it = nexusMetadataEntries.iterator(); it.hasNext();) {
			nme = it.next();
			if (nme.getName().equals(entry.getName())) {
				nme.setAccessName(entry.getAccessName());
				found = true;
				break;
			}
		}

		if (!found) {
			nexusMetadataEntries.add(entry);
		}
	}
	
	/**
	 * Sets the entries within this metadata.
	 * 
	 * @param entries the metadata entries
	 */
	public void setNexusMetadataEntries(List<NexusMetadataEntry> entries) {
		nexusMetadataEntries = new ArrayList<NexusMetadataEntry>();
		for (NexusMetadataEntry entry : entries) {
			addNexusMetadataEntry(entry);
		}
	}

	/**
	 * Gets a list of NexusMetadataEntry.
	 * 
	 * @return ArrayList of nexus metadata entries.
	 */
	public ArrayList<NexusMetadataEntry> getNexusMetadataEntries() {
		return nexusMetadataEntries;
	}

	/**
	 * Set the filename.
	 * 
	 * @param filename
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
}
