/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17;

import java.io.IOException;

public interface NDPva extends GetPluginBaseAvailable {

	public static final String PVNAME_RBV = "PvName_RBV";

	public Object getImage() throws IOException;

	public Object getImageObject() throws IOException;

	public int getHeight();

	public int getWidth();

	/**
	 * The Epics V4 PV name for the data
	 */
	public String getPvName() throws IOException;

}
