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
import gda.epics.CachedLazyPVFactory;
import gda.epics.ReadOnlyPV;

import java.io.IOException;

import org.springframework.beans.factory.InitializingBean;

public class ZebraImpl implements Zebra, InitializingBean {

	final public static String connected = "CONNECTED";
	final public static String store = "STORE";
	final public static String sysReset = "SYS_RESET";
	final public static String sysVer = "SYS_VER";
	final public static String sysStat1Lo = "SYS_STAT1LO";
	final public static String sysStat1Hi = "SYS_STAT1HI";
	final public static String sysStat1 = "SYS_STAT1";
	final public static String sysStat2Lo = "SYS_STAT2LO";
	final public static String sysStat2Hi = "SYS_STAT2HI";
	final public static String sysStat2 = "SYS_STAT2";
	final public static String sysErrState = "SYS_STATERR";

	final public static String PCSource = "PC_ENC";
	final public static String PCTimeUnits = "PC_TSPRE";
	final public static String PCTimeUnits_ms = "ms";
	final public static String PCTimeUnits_s = "s";
	
	
	final public static String PCArmSource = "PC_ARM_SEL";
	final public static String PCArm = "PC_ARM";
	final public static String PCDisArm = "PC_DISARM";
	final public static String PCArmOut = "PC_ARM_OUT";
	final public static String PCArmSourceSoft = "Soft";
	final public static String PCArmSourceExternal = "External";

	final public static String PCGateSource = "PC_GATE_SEL";
	final public static String PCGateStart = "PC_GATE_START";
	final public static String PCGateWidth = "PC_GATE_WID";
	final public static String PCGateNumberOfGates = "PC_GATE_NGATE";
	final public static String PCGateStep = "PC_GATE_STEP";
	final public static String PCGateStatus = "PC_GATE_OUT";
	

	final public static String PCPulseSource = "PC_PULSE_SEL";
	final public static String PCPulseDelay = "PC_PULSE_DLY";
	final public static String PCPulseWidth = "PC_PULSE_WID";
	final public static String PCPulseStep = "PC_PULSE_STEP";
	final public static String PCPulseStatus = "PC_PULSE_OUT";
	public static final String PCCaptureBitField = "PC_NUM_CAP";
	public static final String PCEnc = "PC_ENC";
	public static final String PCEnc1Aval = "PC_ENC1.AVAL";
	public static final String PCNumberOfPointsCaptured = "PC_NUM_CAP";
	public static final String PCPulseStepRBV = "PC_PULSE_STEP:RBV";
	public static final String PCPulseWidthRBV = "PC_PULSE_WID:RBV";
	public static final String PCPulseDelayRBV = "PC_PULSE_DLY:RBV";
	final public static String PCGateStartRBV = "PC_GATE_START:RBV";
	final public static String PCGateWidthRBV = "PC_GATE_WID:RBV";
	final public static String PCTimeUnit = "PC_TSPRE";
	private static final String PCPulseMax = "PC_PULSE_MAX";	
	
	String zebraPrefix;

	CachedLazyPVFactory dev;

	@Override
	public void setPCPulseSource(int val) throws IOException {
		dev.getPVInteger(PCPulseSource).putWait(val);
	}

	@Override
	public int getPCPulseSource() throws IOException {
		return dev.getPVInteger(PCPulseSource).get();
	}

	@Override
	public void setPCPulseDelay(double val) throws IOException {
		dev.getPVDouble(PCPulseDelay).putWait(val);
	}

	@Override
	public double getPCPulseDelay() throws IOException {
		return dev.getPVDouble(PCPulseDelay).get();
	}
	@Override
	public double getPCPulseDelayRBV() throws IOException {
		return dev.getPVDouble(PCPulseDelayRBV).get();
	}

	@Override
	public void setPCPulseWidth(double val) throws IOException {
		dev.getPVDouble(PCPulseWidth).putWait(val);
	}

	@Override
	public double getPCPulseWidth() throws IOException {
		return dev.getPVDouble(PCPulseWidth).get();
	}
	@Override
	public double getPCPulseWidthRBV() throws IOException {
		return dev.getPVDouble(PCPulseWidthRBV).get();
	}

	@Override
	public void setPCPulseStep(double val) throws IOException {
		dev.getPVDouble(PCPulseStep).putWait(val);
	}

	@Override
	public double getPCPulseStep() throws IOException {
		return dev.getPVDouble(PCPulseStep).get();
	}
	@Override
	public double getPCPulseStepRBV() throws IOException {
		return dev.getPVDouble(PCPulseStepRBV).get();
	}

	@Override
	public int getPCPulseMax() throws IOException {
		return dev.getPVInteger(PCPulseMax).get();
	}

	@Override
	public void setPCPulseMax(int val) throws IOException {
		dev.getPVInteger(PCPulseMax).putWait(val);
	}
	
	@Override
	public void setPCGateSource(int val) throws IOException {
		dev.getPVInteger(PCGateSource).putWait(val);
	}

	@Override
	public int getPCGateSource() throws IOException {
		return dev.getPVInteger(PCGateSource).get();
	}

	@Override
	public void setPCGateStart(double val) throws IOException {
		dev.getPVDouble(PCGateStart).putWait(val);
	}

	@Override
	public double getPCGateStart() throws IOException {
		return dev.getPVDouble(PCGateStart).get();
	}
	@Override
	public double getPCGateStartRBV() throws IOException {
		return dev.getPVDouble(PCGateStartRBV).get();
	}

	@Override
	public void setPCGateWidth(double val) throws IOException {
		dev.getPVDouble(PCGateWidth).putWait(val);
	}

	@Override
	public double getPCGateWidth() throws IOException {
		return dev.getPVDouble(PCGateWidth).get();
	}
	@Override
	public double getPCGateWidthRBV() throws IOException {
		return dev.getPVDouble(PCGateWidthRBV).get();
	}

	@Override
	public void setPCGateStep(double val) throws IOException {
		dev.getPVDouble(PCGateStep).putWait(val);
	}

	@Override
	public double getPCGateStep() throws IOException {
		return dev.getPVDouble(PCGateStep).get();
	}

	@Override
	public void setPCArmSource(int val) throws IOException {
		dev.getPVInteger(PCArmSource).putWait(val);
	}

	@Override
	public int getPCArmSource() throws IOException {
		return dev.getPVInteger(PCArmSource).get();
	}

	@Override
	public void pcArm() throws IOException {
		dev.getPVInteger(PCArm).putWait(1,5); 
	}

	@Override
	public void pcDisarm() throws IOException {
		dev.getPVInteger(PCDisArm).putWait(1,5);
	}

	@Override
	public boolean isPCArmed() throws IOException {
		return dev.getPVInteger(PCArmOut).get() == 1;
	}

	@Override
	public void setPCCaptureBitField(int val) throws IOException {
		dev.getPVInteger(PCCaptureBitField).putWait(val);
	}

	@Override
	public int getPCCaptureBitField() throws IOException {
		return dev.getPVInteger(PCCaptureBitField).get();
	}

	@Override
	public void setPCEnc(int val) throws IOException {
		dev.getPVInteger(PCEnc).putWait(val);
	}

	@Override
	public int getPCEnc() throws IOException {
		return dev.getPVInteger(PCEnc).get();
	}

	@Override
	public void setPCTimeUnit(int val) throws IOException {
		dev.getPVInteger(PCTimeUnit).putWait(val);
	}

	@Override
	public int getPCTimeUnit() throws IOException {
		return dev.getPVInteger(PCTimeUnit).get();
	}
	
	@Override
	public void setPCGateNumberOfGates(int val) throws IOException {
		dev.getPVInteger(PCGateNumberOfGates).putWait(val);
	}

	@Override
	public int getPCGateNumberOfGates() throws IOException {
		return dev.getPVInteger(PCGateNumberOfGates).get();
	}

	@Override
	public void setPCNumberOfPointsCaptured(final int val) throws IOException {
		dev.getPVInteger(PCNumberOfPointsCaptured).putWait(val);
	}

	@Override
	public int getPCNumberOfPointsCaptured() throws IOException {
		return dev.getPVInteger(PCNumberOfPointsCaptured).get();
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
		dev = new CachedLazyPVFactory(zebraPrefix);

	}

	@Override
	public ReadOnlyPV<Double[]> getEnc1AvalPV() {
		return dev.getReadOnlyPVDoubleArray(PCEnc1Aval);
	}

	@Override
	public ReadOnlyPV<Integer> getNumberOfPointsCapturedPV() {
		return dev.getPVInteger(PCNumberOfPointsCaptured);
	}

	
}
