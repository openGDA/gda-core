/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.calibration;

import java.util.Collection;

import gda.device.DeviceException;
import gda.factory.Findable;
import gda.observable.IObservable;

public interface BraggCalibrationService extends Findable, IObservable {
	Collection<CalibrationEdge> getEdges();
	void runEdgeScans(Collection<CalibrationEdge> edges);
	String exafsDataPath();
	String braggDataPath();
	/** The maximum absolute value of the new intercept */
	double expectedInterceptLimit();
	/**
	 * Set the new intercept value
	 * @throws DeviceException if communication with device fails
	 */
	void setIntercept(double intercept) throws DeviceException;
	/** Get the authorisation level required to control the bragg motor*/
	int braggProtectionLevel();
}
