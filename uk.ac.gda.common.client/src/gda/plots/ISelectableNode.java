/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.plots;

import java.awt.Color;


/**
 *
 */
public interface ISelectableNode {
	/**
	 * @param selectedFlag
	 */
	public void setSelectedFlag(boolean selectedFlag);

	/**
	 * @return true if this item(not necessarily its children ) is selected
	 */
	public boolean getSelectedFlag();

	/**
	 * @return indication of whether the item and its children at selected
	 */
	public Selected getSelected();
	
	/**
	 * @param maxlength
	 * @return string to be used as a label
	 */
	public String toLabelString(int maxlength);
	
	/**
	 * @return color to be used for the label
	 */
	public Color getColor();
}

