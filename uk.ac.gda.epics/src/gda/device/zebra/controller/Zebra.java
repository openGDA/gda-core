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

package gda.device.zebra.controller;

import gda.device.DeviceException;

import java.io.IOException;

public interface Zebra {
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
	public static final String PCCaptureBitField = "PC_CAP";
	public static final String PCEnc = "PC_ENC";
	
	double getPCPulseDelay() throws IOException;

	void setPCPulseDelay(double delay) throws IOException;

	double getPCPulseWidth() throws IOException;

	void setPCPulseWidth(double width) throws IOException;

	double getPCPulseStep() throws IOException;

	void setPCPulseStep(double step) throws IOException;

	void setPCPulseSource(int ordinal) throws IOException;

	int getPCPulseSource() throws IOException;

	int getPCGateSource() throws IOException;

	void setPCGateSource(int ordinal) throws IOException;

	double getPCGateStart() throws IOException;

	double getPCGateWidth() throws IOException;

	int getPCGateNumberOfGates() throws IOException;

	double getPCGateStep() throws IOException;

	void setPCGateStart(double start) throws IOException;

	void setPCGateWidth(double width) throws IOException;

	void setPCGateNumberOfGates(int numberOfGates) throws IOException;

	void setPCGateStep(double step) throws IOException;

	int getPCArmSource() throws IOException;

	void setPCArmSource(int ordinal) throws IOException;

	boolean isPCArmed() throws IOException;

	void pcArm() throws IOException;

	void pcDisarm() throws IOException;

	void setPCCaptureBitField(int val) throws IOException;

	int getPCCaptureBitField() throws IOException;

	void setPCEnc(int val) throws IOException;

	int getPCEnc() throws IOException;
	
}
