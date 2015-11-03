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

import gda.epics.NoCallbackPV;

import java.io.IOException;


public interface ADDriverMerlinThresholdSweep {

	// TODO: This is no longer needed.
	public enum MerlinThresholdSweepTriggerMode {
		INTERNAL, TRIGGER_ENABLE, TRIGGER_START_RISING, TRIGGER_START_FALLING, TRIGGER_BOTH_RISING, SOFTWARE
	}

	public enum MerlinThresholdSweepImageMode {
		SINGLE, MULTIPLE, CONTINUOPUS, THRESHOLD, BACKGROUND
	}

	public void setStep(Double step) throws IOException;

	public Double getStep() throws IOException;

	public void setStop(Double stop) throws IOException;

	public Double getStop() throws IOException;

	public void setStart(Double start) throws IOException;

	public Double getStart() throws IOException;

	public int getNumberPointsPerSweep() throws IOException;

	public void setNumber(Double number) throws IOException;

	public Double getNumber() throws IOException;

	public NoCallbackPV<Boolean> getStartThresholdScanningPV();

	// TODO: This should be isUseImageModeNotStartThresholdScanning
	public boolean isUseTriggerModeNotStartThresholdScanning();
}
