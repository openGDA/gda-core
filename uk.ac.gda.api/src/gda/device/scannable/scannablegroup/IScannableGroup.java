/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.scannable.scannablegroup;

import gda.device.Scannable;

/**
 * Distributed interface for the ScannableGroup which provides a logical group of scannables
 */
public interface IScannableGroup extends Scannable {

	/**
	 * @param groupMemberName
	 *            the name of the scannable object to add to this group
	 */
	public void addGroupMemberName(String groupMemberName);

	/**
	 * @return the names of the members of this group
	 */
	public String[] getGroupMemberNames();
}