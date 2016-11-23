/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.client.livecontrol;

import org.eclipse.swt.widgets.Composite;

import gda.factory.Findable;

/**
 * A Interface which controls wanting to be configured in Spring and used in the Live controls view must implement.
 *
 * @author James Mudd
 *
 */
public interface LiveControl extends Findable {

	/**
	 * This method will create the GUI for this control.
	 *
	 * @param composite The composite onto which the control should draw
	 */
	public void createControl(Composite composite);

	/**
	 * Gets the group this control is in.
	 *
	 * @return The group this control is in
	 */
	public String getGroup();

}
