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

import gda.analysis.datastructure.ManagedDataObject;

/**
 * Code originally written by SLAC TEAM (AIDA) Modified at Diamond A Link is a managed object representing a symbolic
 * link. It is used only within the Project implementation, and is never exposed to the user.
 */
public class Link extends ManagedDataObject {

	private Path path;

	private boolean isBroken = false;

	/**
	 * Create the link;
	 * 
	 * @param name
	 * @param path
	 */
	Link(String name, Path path) {
		this.setName(name);
		this.path = path;
	}

	/**
	 * @return path
	 */
	Path path() {
		return path;
	}

	/**
	 * @return Is the link broken ?
	 */
	boolean isBroken() {
		return isBroken;
	}

	/**
	 * @return Return the type of this managed object i.e. "lnk"
	 */
	@Override
	public String getType() {
		return "lnk";
	}

}
