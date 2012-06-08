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

package gda.device.detector.areadetector.v17;

import gda.device.Detector;

public interface EpicsAreaDetector extends Detector {
	/**
	 * specify the area detector acquisition control point.
	 * If set to true, file saver capture control is used; 
	 * if set to false, detector acquire control is used.
	 * @param captureMode
	 */
	public abstract void setCaptureControl(boolean captureMode);
	/**
	 * check which area detector acquisition control point is used.
	 * @return true if file saver capture control is used; false if detector acquire control is used.
	 */
	public abstract boolean isCaptureControl();
	/**
	 * reset all area detector and plugins to its initial states
	 * @throws Exception
	 */
	public abstract void resetAll() throws Exception;

}
