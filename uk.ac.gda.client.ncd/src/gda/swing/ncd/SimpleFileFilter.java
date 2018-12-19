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

package gda.swing.ncd;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A class to implement File filtering for a File chooser
 */
public class SimpleFileFilter extends FileFilter {
	private String[] fileExtensions;

	private String description;

	/**
	 * @param fileExtensions
	 * @param description
	 */
	public SimpleFileFilter(String[] fileExtensions, String description) {
		this.fileExtensions = new String[fileExtensions.length];
		for (int i = 0; i < fileExtensions.length; i++)
			this.fileExtensions[i] = fileExtensions[i].toLowerCase();

		this.description = (description == null) ? "Files" : description;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		}
		String name = file.getName().toLowerCase();
		for (int i = fileExtensions.length - 1; i >= 0; i--) {
			if (name.endsWith(fileExtensions[i]))
				return true;
		}
		return false;
	}
}