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

import gda.data.nexus.NexusUtils;

import java.net.URI;
import java.util.List;
import java.util.Vector;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;

import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to create additional links in a nexus file.
 * Links can be to datasets within the same file or in external files
 */
public class NXLinkCreator {

	private static final Logger logger = LoggerFactory.getLogger(NXLinkCreator.class);
	
	List<SubEntryLink> links = new Vector<SubEntryLink>();
	public NXLinkCreator() {
	}

	public void makelinks(String filename) throws Exception {
		NexusFile file = NexusUtils.createNexusFile(filename);
		file.openToWrite(false);

		try {
			for (SubEntryLink link : links) {
				String value = link.target;
				String key = link.key;
				if (value.startsWith("nxfile")) { // create soft link to location in external file
//					makelink(file, key, null, value);
					file.linkExternal(new URI(value), key, false);
				} else if (value.startsWith("#")) { // create soft link to current file
//					makelink(file, key, null, "nxfile://" + filename + value);
					file.linkExternal(new URI("nxfile://" + filename + value), key, false);
				} else if (value.startsWith("!")) { // create external link
					String unavailable = "";
					String path = value.substring(1);
					try {
						file.link(value, key);
					} catch (Throwable t) {
						path = NexusUtils.stripAugmentedPath(path);
						int i = path.lastIndexOf(Node.SEPARATOR);
						String pathToGroup = path.substring(0, i);
						String groupName = path.substring(i + 1);

						logger.debug("DBG: pathToGroup = {}", pathToGroup);
						logger.debug("DBG: groupName = {}", groupName);

						int fid = -1;
						fid = H5.H5Fopen(filename, HDF5Constants.H5F_ACC_RDWR, HDF5Constants.H5P_DEFAULT);

						int gid = -1;
						gid = H5.H5Gopen(fid, pathToGroup, HDF5Constants.H5P_DEFAULT);

						String oname = groupName;
						String[] linkName = new String[2]; // file name and file path
						H5.H5Lget_val(gid, oname, linkName, HDF5Constants.H5P_DEFAULT);

						String linkInfo = "nxfile://";
						linkInfo += linkName[1] + "#" + linkName[0];
						logger.debug("DBG: linkInfo 1 = {}", linkInfo);

						if (gid >= 0)
							H5.H5Gclose(gid);

						if (fid >= 0)
							H5.H5Fclose(fid);
						file.linkExternal(new URI(linkInfo), key, false);
					}
//					makelink(file, key, null, linkInfo);
				} else { // create hard link
					try {
						file.link(key, value);
					} catch (Exception e) {
						throw new Exception("Error making link for " + value + " in file " + file, e);
					}
				}
			}
		} finally {
			try {
				file.flush();
			} catch (Throwable e) {
				logger.error("Error flushing file", e);
				// do not rethrow as we need to finalize
			}
			try {
				file.close();
			} catch (Throwable e) {
				throw new Exception("Error closing " + filename, e);
			} finally {
			}
		}
	}

//	/**
//	 * Use visitor to go down NeXus file
//	 */
//	interface Visitor {
//		/**
//		 * Do things on arriving at any intermediate destination
//		 * @param augmentedPath
//		 * @throws Exception
//		 */
//		void arrive(String augmentedPath) throws Exception;
//
//		/**
//		 * Do things when leaving intermediate destination on return leg 
//		 * @throws Exception
//		 */
//		void depart() throws Exception;
//
//		/**
//		 * Do things when leaving final destination
//		 * @param part use null cannot find final destination 
//		 * @param name
//		 * @param nxClass
//		 * @throws Exception
//		 */
//		void depart(String part, String name, String nxClass) throws Exception;
//
//		/**
//		 * @return any payload from final destination 
//		 */
//		Object take();
//	}
//
//	private static void visit(String path, Visitor visitor) throws Exception {
//		
//		String[] parts = path.split("/", 2);
//		if (parts[0].isEmpty()) { // at top or bottom
//			if (parts.length > 1) {
//				visit(parts[1], visitor); // recurse down tree in file
//				return;
//			}
//			visitor.depart(null, null, null); // final return
//			return;
//		}
//		String[] subParts = parts[0].split(":");
//		if (parts.length > 1) {
//			visitor.arrive(subParts[0], subParts[1]);
//			try {
//				visit(parts[1], visitor); // recurse down tree in file
//			} finally {
//				visitor.depart();
//			}
//			return;
//		}
//		visitor.depart(parts[0], subParts[0], subParts.length > 1 ? subParts[1] : null);
//	}
//
//	private static void makelink(final NexusFile file, String path, final NXlink link, final String url) throws Exception {
//		visit(path, new Visitor() {
//			@Override
//			public void arrive(String augmentedPath) throws Exception {
//				GroupNode group = file.getGroup(augmentedPath, false);
//				
//			}
//			@Override
//			public void arrive(String name, String nxClass) throws NexusException {
//				if (!(file.groupdir().containsKey(name) && file.groupdir().get(name).equals(nxClass))) {
//					try {
//						// workaround because opengroup(x) sometimes fails after a call to makegroup(x)
//						file.opengroup(name, nxClass);
//						file.closegroup();
//					} catch (Throwable e) {
//						file.makegroup(name, nxClass);
//					}
//				}
//				file.opengroup(name, nxClass);
//			}
//
//			@Override
//			public void depart() throws NexusException {
//				file.closegroup();
//			}
//			
//			@Override
//			public void depart(String part, String name, String nxClass) throws NexusException {
//				if (part == null)
//					return;
//
//				if (link != null)
//					file.makenamedlink(part, link);
//				if (url != null)
//					file.linkexternaldataset(part, url);
//			}
//
//			@Override
//			public Object take() {
//				return null;
//			}
//		});
//	}
//
//	private static NXlink getLink(final NexusFile file, String path) throws NexusException, Exception {
//		Visitor v = new Visitor() {
//			NXlink link = null;
//			@Override
//			public void arrive(String augmentedPath) throws Exception {
//				GroupNode group = file.getGroup(augmentedPath, false);
//				
//			}
//			public void arrive(String name, String nxClass) throws Exception {
//				if( !(file.groupdir().containsKey(name) && file.groupdir().get(name).equals(nxClass))){
//					throw new Exception("Item not found " + name + ":" + nxClass);
//				}
//				file.opengroup(name, nxClass);
//			}
//
//			@Override
//			public void depart(String part, String name, String nxClass) throws Exception {
//				if (part == null)
//					return;
//
//				if (nxClass.equals(NexusExtractor.NXDataClassName)) {
//					try {
//						file.opendata(name);
//					} catch (NexusException ne) {
//						throw new Exception("Error calling opendata for " + name, ne);
//					}
//					link = file.getdataID();
//					file.closedata();
//				}
//			}
//
//			@Override
//			public void depart() throws Exception {
//				file.closegroup();
//			}
//
//			@Override
//			public Object take() {
//				return link;
//			}
//		};
//		visit(path, v);
//		return (NXlink) v.take();
//	}
//
//	private static String getExternalLinkInfo(final NexusFile file, String path, final String dataClassName, final String unavailable) throws NexusException, Exception {
//		Visitor v = new Visitor() {
//			String linkInfo = null;
//			@Override
//			public void arrive(String augmentedPath) throws Exception {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void arrive(String name, String nxClass) throws Exception {
//				if( !(file.groupdir().containsKey(name) && file.groupdir().get(name).equals(nxClass))){
//					throw new Exception("Item not found " + name + ":" + nxClass);
//				}
//				file.opengroup(name, nxClass);
//			}
//			
//			@Override
//			public void depart(String part, String name, String nxClass) throws Exception {
//				if (part == null)	{
//					linkInfo = null;
//					return;
//				}
//				linkInfo = nxClass.equals(dataClassName) ? file.isexternaldataset("data") : unavailable;
//			}
//			
//			@Override
//			public void depart() throws Exception {
//				file.closegroup();
//			}
//			
//			@Override
//			public Object take() {
//				return linkInfo;
//			}
//		};
//
//		visit(path, v);
//		return (String) v.take();
//	}

	/**
	 * Define the key to be generated and its target
	 * @param key
	 * @param target
	 * Formats for target:
	 * "/entry1:NXentry/default:NXdata/simpleScannable1:NXdata" - creates a hard link to a location in current file
	 * "nxfile://<filePath>#entry1/SimpleDetector1/simpleScannable1" - create soft link to a location in other file
	 * "#entry2/test2" - create soft link to location in current file - NB h5py module in Python cannot read this
	 */
	public void addLink(String key, String target) {
		links.add(new SubEntryLink(key, target));
	}

	class SubEntryLink {
		public String key;
		public String target;

		public SubEntryLink(String key, String value) {
			super();
			this.key = key;
			this.target = value;
		}
	}
}
