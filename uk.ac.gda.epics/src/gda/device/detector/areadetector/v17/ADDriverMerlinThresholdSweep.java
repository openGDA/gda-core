/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import gda.epics.NoCallbackPV;


public interface ADDriverMerlinThresholdSweep {

	// TODO: This is no longer needed.
	public enum MerlinThresholdSweepTriggerMode {
		INTERNAL, TRIGGER_ENABLE, TRIGGER_START_RISING, TRIGGER_START_FALLING, TRIGGER_BOTH_RISING, SOFTWARE
	}

	public enum MerlinThresholdSweepImageMode {
		SINGLE, MULTIPLE, CONTINUOPUS, THRESHOLD, BACKGROUND
	}

	void setStep(Double step) throws IOException;

	Double getStep() throws IOException;

	void setStop(Double stop) throws IOException;

	Double getStop() throws IOException;

	void setStart(Double start) throws IOException;

	Double getStart() throws IOException;

	int getNumberPointsPerSweep() throws IOException;

	void setNumber(Double number) throws IOException;

	Double getNumber() throws IOException;

	NoCallbackPV<Boolean> getStartThresholdScanningPV();

	// TODO: This should be isUseImageModeNotStartThresholdScanning
	boolean isUseTriggerModeNotStartThresholdScanning();
}
