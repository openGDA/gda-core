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

package gda.data.structure;

import gda.data.structure.filemounter.SRSMount;

/**
 * Code originally written by SLAC TEAM (AIDA) Modified at Diamond A mount point allows one project to be "mounted" in
 * another. Mount point extends Folder so that it appears as just another folder to the project, but it overrides all of
 * Folders methods and forwards them to the mountPoint within the mounted project.
 */
public class FileMountPoint extends Folder {

	private Folder mountPoint = null;

	/**
	 * Constructor.
	 * 
	 * @param filename
	 * @param format
	 */
	public FileMountPoint(String filename, String format) {
		if (format == "SRS") {
			mountPoint = new SRSMount(filename);
		}

	}

	protected void unmount() {
	}

	@Override
	public Object getChild(int index) {
		return mountPoint.getChild(index);
	}

	@Override
	public Object getChild(String name) {
		return mountPoint.getChild(name);
	}

	@Override
	public int getChildCount() {
		return mountPoint.getChildCount();
	}

	@Override
	public int getIndexOfChild(Object child) {
		return mountPoint.getIndexOfChild(child);
	}

	@Override
	public void add(String name, Object object) {
		mountPoint.add(name, object);
	}

	@Override
	public String getChildName(Object child) {
		return mountPoint.getChildName(child);
	}

	@Override
	public String getChildName(int i) {
		return mountPoint.getChildName(i);
	}

	@Override
	public void remove(Object child) {
		// not implemented for a file mount point
	}

	@Override
	public String getType() {
		return "mnt";
	}

}
