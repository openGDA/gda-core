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

import java.net.URI;
import java.util.List;
import java.util.Vector;

import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;

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
		NexusFile file = NexusFileHDF5.openNexusFile(filename);

		try {
			for (SubEntryLink link : links) {
				String value = link.target;
				String key = link.key;
				if (value.startsWith("nxfile")) { // create soft link to location in external file
					file.linkExternal(new URI(value), key, false);
				} else if (value.startsWith("#")) { // create soft link to current file
					file.linkExternal(new URI("nxfile://" + filename + value), key, false);
				} else if (value.startsWith("!")) { // create external link
					String path = value.substring(1);
					if (!path.startsWith(Tree.ROOT)) {
						path = Tree.ROOT + path;
					}
					try {
						file.link(path, key);
					} catch (Throwable t) {
						path = NexusUtils.stripAugmentedPath(path);
						int i = path.lastIndexOf(Node.SEPARATOR);
						String pathToGroup = path.substring(0, i);
						String groupName = path.substring(i + 1);

						logger.debug("DBG: pathToGroup = {}", pathToGroup);
						logger.debug("DBG: groupName = {}", groupName);

						long fid = -1;
						fid = H5.H5Fopen(filename, HDF5Constants.H5F_ACC_RDWR, HDF5Constants.H5P_DEFAULT);

						long gid = -1;
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
				} else { // create hard link
					if (!value.startsWith(Tree.ROOT)) {
						value = Tree.ROOT + value;
					}
					try {
						file.link(value, key);
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

	/**
	 * Define the key to be generated and its target
	 * @param key
	 * @param target
	 * Formats for target:
	 * "/entry1:NXentry/default:NXdata/simpleScannable1:SDS" - creates a hard link to a location in current file
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
