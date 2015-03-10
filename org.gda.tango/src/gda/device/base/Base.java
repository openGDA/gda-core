/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.base;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import gda.device.Scannable;
import gda.factory.FactoryException;

public interface Base {
	/*
	 * Commands
	 */
	void init() throws DevFailed;

	DevState getState() throws DevFailed;

	String getStatus() throws DevFailed;

	/**
	 * getAttrStringValueList is used to get the list of allowed strings for an attribute e.g. acq_mode can have values
	 * Single, Accumulation, Concatenation
	 */
	String[] getAttrStringValueList(String attributeName) throws DevFailed;
	
	Scannable getControlScannable(String name, String attributeName, String format) throws FactoryException;	
}
