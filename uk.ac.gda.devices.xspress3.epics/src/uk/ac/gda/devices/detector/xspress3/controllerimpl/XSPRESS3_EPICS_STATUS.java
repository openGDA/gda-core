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

package uk.ac.gda.devices.detector.xspress3.controllerimpl;

import gda.device.Detector;

public enum XSPRESS3_EPICS_STATUS {
	IDLE, ACQUIRE, READOUT, CORRECT, SAVING, ABORTING, ERROR, WAITING, INITIALIZING, DISCONNECTED, ABORTED;

	/**
	 * @return Convert the current Xspress detector state enum value to a Detector state
	 * e.g. Detector.IDLE, FAULT, BUSY, PAUSED, STANDBY or FAULT;
	 *
	 */
	public int toGdaDetectorState() {
		XSPRESS3_EPICS_STATUS currentStatus = this;
		if (currentStatus == XSPRESS3_EPICS_STATUS.IDLE || currentStatus == XSPRESS3_EPICS_STATUS.ABORTED) {
			return Detector.IDLE;
		}
		if (currentStatus == XSPRESS3_EPICS_STATUS.ERROR) {
			return Detector.FAULT;
		}
		if (currentStatus == XSPRESS3_EPICS_STATUS.ACQUIRE || currentStatus == XSPRESS3_EPICS_STATUS.READOUT
				|| currentStatus == XSPRESS3_EPICS_STATUS.CORRECT || currentStatus == XSPRESS3_EPICS_STATUS.SAVING
				|| currentStatus == XSPRESS3_EPICS_STATUS.ABORTING) {
			return Detector.BUSY;
		}
		if (currentStatus == XSPRESS3_EPICS_STATUS.WAITING) {
			return Detector.PAUSED;
		}
		if (currentStatus == XSPRESS3_EPICS_STATUS.INITIALIZING
				|| currentStatus == XSPRESS3_EPICS_STATUS.DISCONNECTED) {
			return Detector.STANDBY;
		}
		// unknown
		return Detector.FAULT;
	}
}
