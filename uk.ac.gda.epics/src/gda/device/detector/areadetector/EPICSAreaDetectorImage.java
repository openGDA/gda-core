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

import org.eclipse.january.dataset.DoubleDataset;

import gda.factory.Configurable;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

public interface EPICSAreaDetectorImage extends Configurable {

	// getters and setters for spring
	String getBasePVName();

	void setBasePVName(String basePVName);

	String getInitialArrayPort();

	void setInitialArrayPort(String initialArrayPort);

	String getInitialArrayAddress();

	void setInitialArrayAddress(String initialArrayAddress);

	void reset() throws CAException, InterruptedException;

	// Methods for manipulating the underlying channels
	void setEnable(boolean enable) throws CAException, InterruptedException;

	void setArrayPort(String arrayPort) throws CAException, InterruptedException;

	String getArrayPort() throws TimeoutException, CAException, InterruptedException;

	void setArrayAddress(String arrayAddress) throws CAException, InterruptedException;

	String getArrayAddress() throws TimeoutException, CAException, InterruptedException;

	double getTimeStamp() throws TimeoutException, CAException, InterruptedException;

	/**
	 * Gets the most recent image which has been put to the image plug-in in the Epics environment
	 *
	 * @return a 2D dataset containing the image data
	 * @throws TimeoutException if the data cannot be retrieved in time
	 * @throws CAException if there are any other CA errors
	 */
	DoubleDataset getImage() throws TimeoutException, CAException, InterruptedException, InterruptedException;
}
