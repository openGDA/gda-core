/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.zebra;

import gda.device.IScannableMotor;
import gda.device.zebra.controller.Zebra;

public class DummyZebraMotorInfoProvider implements ZebraMotorInfoProvider {

	private static final double SCURVE_TIME_TO_VELOCITY = .03;

	private IScannableMotor scannableMotor;
	private int pcEnc = Zebra.PC_ENC_ENC1;
	private boolean exposureStepDefined = false;
	private double exposureStep = 0;

	@Override
	public double distanceToAccToVelocity(double requiredSpeed) {
		return SCURVE_TIME_TO_VELOCITY * requiredSpeed / 2;
	}

	public void setPcEnc(int pcEnc) {
		this.pcEnc = pcEnc;
	}

	@Override
	public int getPcEnc() {
		return pcEnc;
	}

	public void setScannableMotor(IScannableMotor scannableMotor) {
		this.scannableMotor = scannableMotor;
	}

	@Override
	public IScannableMotor getActualScannableMotor() {
		return scannableMotor;
	}

	public void setExposureStepDefined(boolean exposureStepDefined) {
		this.exposureStepDefined = exposureStepDefined;
	}

	@Override
	public boolean isExposureStepDefined() {
		return exposureStepDefined;
	}

	public void setExposureStep(double exposureStep) {
		this.exposureStep = exposureStep;
	}

	@Override
	public double getExposureStep() {
		return exposureStep;
	}
}
