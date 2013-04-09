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

import gda.epics.ReadOnlyPV;

import java.io.IOException;

public interface Zebra {

	final public static int PC_MODE_POSITION = 0;
	final public static int PC_MODE_TIME = 1;

	final public static int PC_TIMEUNIT_SEC = 1;
	final public static int PC_TIMEUNIT_MS = 0;

	double getPCPulseDelay() throws IOException;

	double getPCPulseDelayRBV() throws IOException;

	void setPCPulseDelay(double delay) throws IOException;

	double getPCPulseWidth() throws IOException;

	double getPCPulseWidthRBV() throws IOException;

	void setPCPulseWidth(double width) throws IOException;

	double getPCPulseStep() throws IOException;

	double getPCPulseStepRBV() throws IOException;

	void setPCPulseStep(double step) throws IOException;

	void setPCPulseSource(int ordinal) throws IOException;

	int getPCPulseSource() throws IOException;

	int getPCGateSource() throws IOException;

	void setPCGateSource(int ordinal) throws IOException;

	double getPCGateStart() throws IOException;

	double getPCGateStartRBV() throws IOException;

	double getPCGateWidth() throws IOException;

	int getPCGateNumberOfGates() throws IOException;

	double getPCGateStep() throws IOException;

	void setPCGateStart(double start) throws IOException;

	void setPCGateWidth(double width) throws IOException;

	double getPCGateWidthRBV() throws IOException;

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

	ReadOnlyPV<Double[]> getEnc1AvalPV();

	ReadOnlyPV<Integer> getNumberOfPointsCapturedPV();

	void setPCNumberOfPointsCaptured(int val) throws IOException;

	int getPCNumberOfPointsCaptured() throws IOException;

	/*
	 * 0 - ms, 1-s
	 */
	void setPCTimeUnit(int i) throws IOException;

	int getPCTimeUnit() throws IOException;

	void setPCPulseMax(int numberTriggers) throws IOException;

	int getPCPulseMax() throws IOException;

}
