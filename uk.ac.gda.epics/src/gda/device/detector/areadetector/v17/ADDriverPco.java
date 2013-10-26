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

import gda.epics.PV;

public interface ADDriverPco {

	public enum PcoTriggerMode {
		AUTO, SOFTWARE,
		/**
		 * In External and Software the exposure length is governed by the exposure time set in the camera.
		 */
		EXTERNAL_AND_SOFTWARE,

		/**
		 * In External Pulse the exposure length is controlled by the hardware trigger
		 */
		EXTERNAL_PULSE
	}

	PV<Boolean> getArmModePV();

	PV<Double> getCameraUsagePV();

	PV<Integer> getAdcModePV();

	PV<Integer> getTimeStampModePV();

	PV<Integer> getBinXPV();

	PV<Integer> getBinYPV();

	PV<Integer> getMinXPV();

	PV<Integer> getSizeXPV();

	PV<Integer> getMinYPV();

	PV<Integer> getSizeYPV();
}
