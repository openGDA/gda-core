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

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.nexusformat.NexusFile;


/**
 * Class used to create soft links to datasets in a nexus file. Links can be to datasets within the same file or in
 * external files nxc= new NXLinkCreator() nxc.addLink(...) nxc.addLink(...) ... nxc.makelinks(filename)
 */
public class NXLinkCreator {

	List<SubEntryLink> links = new Vector<SubEntryLink>();

	public NXLinkCreator() {
	}

	/**
	 * @param filename
	 *            - path to file in which to create the links
	 * @throws Exception
	 */
	public void makelinks(String filename) throws Exception {
		NexusFile file = new NexusFile(filename,NexusFile.NXACC_RDWR);
		try {
			for( SubEntryLink link : links){
				String filepath = link.filepath;
				if( filepath != null && !filepath.isEmpty()){
					makelink(file, link.key,  filepath, link.target);
				} else { 
					makelink(file, link.key, filename, link.target);
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

	private void makelink(NexusFile file, String key, String filepath, String target) throws Exception {
		String[] keyParts = key.split("/", 2);
		if (keyParts[0].isEmpty()) {
			if (keyParts.length > 1) {
				makelink(file, keyParts[1], filepath, target);
			}
			return;
		}
		if (keyParts.length > 1) {
			String[] subParts = keyParts[0].split(":");
			String name = subParts[0];
			String nxClass = subParts[1];
			Hashtable groupdir = file.groupdir();
			boolean folderKey = groupdir.containsKey(name);
			Object folderName = groupdir.get(name);
			if (!(folderKey && folderName.equals(nxClass))) {
				try {
					// workaround because opengroup(x) sometimes fails after a call to makegroup(x)
					file.opengroup(name, nxClass);
					file.closegroup();
				} catch (Throwable e) {
					file.makegroup(name, nxClass);
				}
			}
			file.opengroup(name, nxClass);
			try {
				makelink(file, keyParts[1], filepath, target);
			} finally {
				file.closegroup();
			}
			return;
		}
		if (target.startsWith("/")) {
			target = target.substring(1, target.length());
		}
		file.linkexternaldataset(keyParts[0], "nxfile://" + filepath + "#" + target);
		file.flush();
	}

	/**
	 * Adds a dataset soft link specification to create an item in the current file
	 * 
	 * @param key
	 *            - path to item to be created e.g. /entry2/item1
	 * @param filepath
	 *            - absolute filepath to the file holding the target. Leave null or empty for current file
	 * @param target
	 *            - path to actual dataset to be linked e.g. entry1/default/detector1. Notice the lack of initial slash
	 */
	public void addLink(String key, String filepath, String target) {
		links.add(new SubEntryLink(key, filepath, target));
	}

	class SubEntryLink {
		public String key;
		public String filepath;
		public String target;

		public SubEntryLink(String key, String filepath, String target) {
			super();
			this.key = key;
			this.filepath = filepath;
			this.target = target;
		}

	}

}
