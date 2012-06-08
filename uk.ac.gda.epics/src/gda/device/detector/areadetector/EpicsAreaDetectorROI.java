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

package gda.device.detector.areadetector;

import gda.factory.Configurable;
import gda.factory.Localizable;
import gov.aps.jca.CAException;

/**
 *
 */
public interface EpicsAreaDetectorROI extends Configurable, Localizable {
	// Getters and setters for Spring
	public String getBasePVName();

	public void setBasePVName(String basePVName);

	public void reset() throws CAException, InterruptedException;

	// Methods for manipulating the underlying channels
	public void setEnable(boolean enable) throws CAException, InterruptedException;

}
