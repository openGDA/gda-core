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

	final public static String pcSource = "PC_ENC";
	final public static String pcTimeUnits = "PC_TSPRE";
	final public static String pcTimeUnits_ms = "ms";
	final public static String pcTimeUnits_s = "s";
	
	
	final public static String pcArmSource = "PC_ARM_SEL";
	final public static String pcArm = "PC_ARM";
	final public static String pcDisArm = "PC_DISARM";
	final public static String pcArmOut = "PC_ARM_OUT";
	final public static String pcArmSourceSoft = "Soft";
	final public static String pcArmSourceExternal = "External";

	final public static String pcGateSource = "PC_GATE_SEL";
	final public static String pcGateStart = "PC_GATE_START";
	final public static String pcGateWidth = "PC_GATE_WID";
	final public static String pcGateNumGates = "PC_GATE_NGATE";
	final public static String pcGateStep = "PC_GATE_STEP";
	final public static String pcGateStatus = "PC_GATE_OUT";
	

	final public static String pcPulseSource = "PC_PULSE_SEL";
	final public static String pcPulseDelay = "PC_PULSE_DLY";
	final public static String pcPulseWidth = "PC_PULSE_WID";
	final public static String pcPulseStep = "PC_PULSE_STEP";
	final public static String pcPulseStatus = "PC_PULSE_OUT";
	
	int getPCPulseOut();

	double getPCPulseDelay();

	void setPCPulseDelay(double delay);

	double getPCPulseWidth();

	void setPCPulseWidth(double width);

	double getPCPulseStep();

	void setPCPulseStep(double step);

	void setPCPulseSource(int ordinal);

	int getPCPulseSource();

	int getPCGateSource();

	void setPCGateSource(int ordinal);

	int getPCGateOut();

	double getPCGateStart();

	double getPCGateWidth();

	double getPCGateNumberOfGates();

	double getPCGateStep();

	void setPCGateStart(double start);

	void setPCGateWidth(double width);

	void setPCGateNumberOfGates(double numberOfGates);

	void setPCGateStep(double step);

	int getPCArmSource();

	void setPCArmSource(int ordinal);

	int getPCArmOut();

	void pcArm();

	void pcDisarm();

	
}
