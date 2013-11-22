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

public interface Zebra {

	final public static int PC_MODE_POSITION = 0;
	final public static int PC_MODE_TIME = 1;

	final public static int PC_TIMEUNIT_SEC = 1;
	final public static int PC_TIMEUNIT_MS = 0;

	double getPCPulseDelay() throws Exception;

	double getPCPulseDelayRBV() throws Exception;

	void setPCPulseDelay(double delay) throws Exception;

	double getPCPulseWidth() throws Exception;

	double getPCPulseWidthRBV() throws Exception;

	void setPCPulseWidth(double width) throws Exception;

	double getPCPulseStep() throws Exception;

	double getPCPulseStepRBV() throws Exception;

	void setPCPulseStep(double step) throws Exception;

	void setPCPulseSource(int ordinal) throws Exception;

	int getPCPulseSource() throws Exception, Exception;

	int getPCGateSource() throws Exception, Exception;

	void setPCGateSource(int ordinal) throws Exception;

	double getPCGateStart() throws Exception;

	double getPCGateStartRBV() throws Exception;

	double getPCGateWidth() throws Exception;

	int getPCGateNumberOfGates() throws Exception;

	double getPCGateStep() throws Exception;

	void setPCGateStart(double start) throws Exception;

	void setPCGateWidth(double width) throws Exception;

	double getPCGateWidthRBV() throws Exception;

	void setPCGateNumberOfGates(int numberOfGates) throws Exception;

	void setPCGateStep(double step) throws Exception;

	int getPCArmSource() throws Exception;

	void setPCArmSource(int ordinal) throws Exception;

	boolean isPCArmed() throws Exception;

	void pcArm() throws Exception;

	void pcDisarm() throws Exception;

	void setPCCaptureBitField(int val) throws Exception;

	int getPCCaptureBitField() throws Exception;

	void setPCEnc(int val) throws Exception;

	int getPCEnc() throws Exception;

	ReadOnlyPV<Double[]> getEnc1AvalPV();

	ReadOnlyPV<Integer> getNumberOfPointsCapturedPV();

	void setPCNumberOfPointsCaptured(int val) throws Exception;

	int getPCNumberOfPointsCaptured() throws Exception;

	/*
	 * 0 - ms, 1-s
	 */
	void setPCTimeUnit(int i) throws Exception;

	int getPCTimeUnit() throws Exception;

	void setPCPulseMax(int numberTriggers) throws Exception;

	int getPCPulseMax() throws Exception, Exception;

	ReadOnlyPV<Integer> getNumberOfPointsDownloadedPV();

	ReadOnlyPV<Double[]> getPCTimePV();

	void setPCDir(int i)  throws Exception;
	int getPCDir()  throws Exception;
	
	void setPulseInput(int pulseId, int input) throws Exception;
	void setPulseDelay(int pulseId, double delay) throws Exception;
	void setPulseWidth(int pulseId, double width) throws Exception;
	void setPulseTimeUnit(int pulseId, int timeunit) throws Exception;
	
	void setOutTTL(int outId, int val) throws Exception;
	

}
