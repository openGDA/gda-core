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

package gda.device.zebra.controller.impl;

import gda.device.zebra.controller.Zebra;
import gda.epics.ReadOnlyPV;

import java.io.IOException;

import org.springframework.beans.factory.InitializingBean;

public class ZebraImpl implements Zebra, InitializingBean {

	String zebraPrefix;

	DevicePVCreator dev;

	@Override
	public void setPCPulseSource(int val) throws IOException {
		dev.getPVInteger(Zebra.PCPulseSource).putCallback(val);
	}

	@Override
	public int getPCPulseSource() throws IOException {
		return dev.getPVInteger(Zebra.PCPulseSource).get();
	}

	@Override
	public void setPCPulseDelay(double val) throws IOException {
		dev.getPVDouble(Zebra.PCPulseDelay).putCallback(val);
	}

	@Override
	public double getPCPulseDelay() throws IOException {
		return dev.getPVDouble(Zebra.PCPulseDelay).get();
	}
	@Override
	public double getPCPulseDelayRBV() throws IOException {
		return dev.getPVDouble(Zebra.PCPulseDelayRBV).get();
	}

	@Override
	public void setPCPulseWidth(double val) throws IOException {
		dev.getPVDouble(Zebra.PCPulseWidth).putCallback(val);
	}

	@Override
	public double getPCPulseWidth() throws IOException {
		return dev.getPVDouble(Zebra.PCPulseWidth).get();
	}
	@Override
	public double getPCPulseWidthRBV() throws IOException {
		return dev.getPVDouble(Zebra.PCPulseWidthRBV).get();
	}

	@Override
	public void setPCPulseStep(double val) throws IOException {
		dev.getPVDouble(Zebra.PCPulseStep).putCallback(val);
	}

	@Override
	public double getPCPulseStep() throws IOException {
		return dev.getPVDouble(Zebra.PCPulseStep).get();
	}
	@Override
	public double getPCPulseStepRBV() throws IOException {
		return dev.getPVDouble(Zebra.PCPulseStepRBV).get();
	}

	@Override
	public void setPCGateSource(int val) throws IOException {
		dev.getPVInteger(Zebra.PCGateSource).putCallback(val);
	}

	@Override
	public int getPCGateSource() throws IOException {
		return dev.getPVInteger(Zebra.PCGateSource).get();
	}

	@Override
	public void setPCGateStart(double val) throws IOException {
		dev.getPVDouble(Zebra.PCGateStart).putCallback(val);
	}

	@Override
	public double getPCGateStart() throws IOException {
		return dev.getPVDouble(Zebra.PCGateStart).get();
	}
	@Override
	public double getPCGateStartRBV() throws IOException {
		return dev.getPVDouble(Zebra.PCGateStartRBV).get();
	}

	@Override
	public void setPCGateWidth(double val) throws IOException {
		dev.getPVDouble(Zebra.PCGateWidth).putCallback(val);
	}

	@Override
	public double getPCGateWidth() throws IOException {
		return dev.getPVDouble(Zebra.PCGateWidth).get();
	}
	@Override
	public double getPCGateWidthRBV() throws IOException {
		return dev.getPVDouble(Zebra.PCGateWidthRBV).get();
	}

	@Override
	public void setPCGateStep(double val) throws IOException {
		dev.getPVDouble(Zebra.PCGateStep).putCallback(val);
	}

	@Override
	public double getPCGateStep() throws IOException {
		return dev.getPVDouble(Zebra.PCGateStep).get();
	}

	@Override
	public void setPCArmSource(int val) throws IOException {
		dev.getPVInteger(Zebra.PCArmSource).putCallback(val);
	}

	@Override
	public int getPCArmSource() throws IOException {
		return dev.getPVInteger(Zebra.PCArmSource).get();
	}

	@Override
	public void pcArm() throws IOException {
		dev.getPVInteger(Zebra.PCArm).putCallback(1,5); 
	}

	@Override
	public void pcDisarm() throws IOException {
		dev.getPVInteger(Zebra.PCDisArm).putCallback(1,5);
	}

	@Override
	public boolean isPCArmed() throws IOException {
		return dev.getPVInteger(Zebra.PCArmOut).get() == 1;
	}

	@Override
	public void setPCCaptureBitField(int val) throws IOException {
		dev.getPVInteger(Zebra.PCCaptureBitField).putCallback(val);
	}

	@Override
	public int getPCCaptureBitField() throws IOException {
		return dev.getPVInteger(Zebra.PCCaptureBitField).get();
	}

	@Override
	public void setPCEnc(int val) throws IOException {
		dev.getPVInteger(Zebra.PCEnc).putCallback(val);
	}

	@Override
	public int getPCEnc() throws IOException {
		return dev.getPVInteger(Zebra.PCEnc).get();
	}

	@Override
	public void setPCGateNumberOfGates(int val) throws IOException {
		dev.getPVInteger(Zebra.PCGateNumberOfGates).putCallback(val);
	}

	@Override
	public int getPCGateNumberOfGates() throws IOException {
		return dev.getPVInteger(Zebra.PCGateNumberOfGates).get();
	}

	@Override
	public void setPCNumberOfPointsCaptured(final int val) throws IOException {
		dev.getPVInteger(Zebra.PCNumberOfPointsCaptured).putCallback(val);
	}

	@Override
	public int getPCNumberOfPointsCaptured() throws IOException {
		return dev.getPVInteger(Zebra.PCNumberOfPointsCaptured).get();
	}

	
	public String getZebraPrefix() {
		return zebraPrefix;
	}

	public void setZebraPrefix(String zebraPrefix) {
		this.zebraPrefix = zebraPrefix;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (zebraPrefix == null || zebraPrefix.isEmpty())
			throw new Exception("zebraPrefix is not set");
		dev = new DevicePVCreator(zebraPrefix);

	}

	@Override
	public ReadOnlyPV<Double[]> getEnc1AvalPV() {
		return dev.getReadOnlyPVDoubleArray(Zebra.PCEnc1Aval);
	}

	@Override
	public ReadOnlyPV<Integer> getNumberOfPointsCapturedPV() {
		return dev.getPVInteger(Zebra.PCNumberOfPointsCaptured);
	}

	
}
