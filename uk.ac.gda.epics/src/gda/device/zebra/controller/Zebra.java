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

import gda.epics.CachedLazyPVFactory;
import gda.epics.ReadOnlyPV;
import gda.observable.Observable;

import java.io.IOException;

public interface Zebra {

	// PC Setup
	final public static int PC_ENC_ENC1 = 0;
	final public static int PC_ENC_ENC2 = 1;
	final public static int PC_ENC_ENC3 = 2;
	final public static int PC_ENC_ENC4 = 3;
	final public static int PC_ENC_ENCSUM = 4;

	final public static int PC_DIR_POSITIVE = 0;
	final public static int PC_DIR_NEGATIVE = 1;

	final public static int PC_TIMEUNIT_10SEC = 2;
	final public static int PC_TIMEUNIT_SEC = 1;
	final public static int PC_TIMEUNIT_MS = 0;

	// Arm
	final public static int PC_ARM_SOURCE_SOFT = 0;
	final public static int PC_ARM_SOURCE_EXTERNAL = 1;

	// Gate
	final public static int PC_GATE_SOURCE_POSITION = 0;
	final public static int PC_GATE_SOURCE_TIME = 1;
	final public static int PC_GATE_SOURCE_EXTERNAL = 2;

	// Pulse
	final public static int PC_PULSE_SOURCE_POSITION = 0;
	final public static int PC_PULSE_SOURCE_TIME = 1;
	final public static int PC_PULSE_SOURCE_EXTERNAL = 2;

	@Deprecated
	final public static int PC_MODE_POSITION = PC_PULSE_SOURCE_POSITION;
	@Deprecated
	final public static int PC_MODE_TIME = PC_PULSE_SOURCE_TIME;

	final public static int PULSE_TIMEUNIT_10SEC = 2;
	final public static int PULSE_TIMEUNIT_SEC = 1;
	final public static int PULSE_TIMEUNIT_MS = 0;

	void setUseAvalField(boolean useAvalField);

	boolean isUseAvalField();

	void setPvFactory(CachedLazyPVFactory pvFactory);

	double getPCPulseDelay() throws Exception;

	double getPCPulseDelayRBV() throws Exception;

	void setPCPulseDelay(double delay) throws Exception;

	double getPCPulseWidth() throws Exception;

	double getPCPulseWidthRBV() throws Exception;

	void setPCPulseWidth(double width) throws Exception;

	double getPCPulseStep() throws Exception;

	double getPCPulseStepRBV() throws Exception;

	void setPCPulseStep(double step) throws Exception;

	/**
	 * @param ordinal should be PC_PULSE_SOURCE_POSITION, PC_PULSE_SOURCE_TIME or PC_PULSE_SOURCE_EXTERNAL
	 * @throws Exception
	 */
	void setPCPulseSource(int ordinal) throws Exception;

	int getPCPulseSource() throws Exception, Exception;

	int getPCGateSource() throws Exception, Exception;

	/**
	 * @param ordinal should be PC_GATE_SOURCE_POSITION, PC_GATE_SOURCE_TIME or PC_GATE_SOURCE_EXTERNAL
	 * @throws Exception
	 */
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

	/**
	 * @param ordinal should be PC_ARM_SOURCE_SOFT or PC_ARM_SOURCE_EXTERNAL
	 * @throws Exception
	 */
	void setPCArmSource(int ordinal) throws Exception;

	boolean isPCArmed() throws Exception;

	void pcArm() throws Exception;

	void pcDisarm() throws Exception;

	void setPCCaptureBitField(int val) throws Exception;

	int getPCCaptureBitField() throws Exception;

	/**
	 * @param val should be PC_ENC_ENC1, PC_ENC_ENC2, PC_ENC_ENC3, PC_ENC_ENC4 or PC_ENC_ENCSUM
	 * @throws Exception
	 */
	void setPCEnc(int val) throws Exception;

	int getPCEnc() throws Exception;

	@Deprecated
	ReadOnlyPV<Double[]> getEnc1AvalPV();

	ReadOnlyPV<Double[]> getEncPV(int encoder);

	ReadOnlyPV<Integer> getNumberOfPointsCapturedPV();

	void setPCNumberOfPointsCaptured(int val) throws Exception;

	int getPCNumberOfPointsCaptured() throws Exception;

	String getZebraPrefix();

	void setZebraPrefix(String zebraPrefix);

	/**
	 * @param i should be PC_TIMEUNIT_SEC or PC_TIMEUNIT_MS
	 * @throws Exception
	 */
	void setPCTimeUnit(int i) throws Exception;

	int getPCTimeUnit() throws Exception;

	void setPCPulseMax(int numberTriggers) throws Exception;

	int getPCPulseMax() throws Exception, Exception;

	ReadOnlyPV<Integer> getNumberOfPointsDownloadedPV();

	ReadOnlyPV<Double[]> getPCTimePV();

	/**
	 * @param i should be PC_DIR_POSITIVE or PC_DIR_NEGATIVE
	 * @throws Exception
	 */
	void setPCDir(int i)  throws Exception;
	int getPCDir()  throws Exception;

	void setPulseInput(int pulseId, int input) throws Exception;
	void setPulseDelay(int pulseId, double delay) throws Exception;
	void setPulseWidth(int pulseId, double width) throws Exception;
	void setPulseTimeUnit(int pulseId, int timeunit) throws Exception;

	void setOutTTL(int outId, int val) throws Exception;

	/**
	 * Returns the state of the specified TTL output.
	 */
	boolean getTtlOutputState(int output) throws IOException;

	void setValue(String beforeUnderscore, int beforeUnderscoreId, String afterUnderscore, int afterUnderscoreId, int val) throws Exception;

	void setPCArmInput(int input) throws Exception;

	/**
	 * Returns whether a soft input is set.
	 */
	public boolean isSoftInputSet(int inputNumber) throws IOException;

	/**
	 * Sets a soft input.
	 */
	public void setSoftInput(int inputNumber, boolean set) throws IOException;

	/**
	 * Returns an {@link Observable} that can be used to receive events about changes to soft inputs.
	 */
	public Observable<SoftInputChangedEvent> getSoftInputObservable();

	void encCopyMotorPosToZebra(int posNum) throws Exception;

	/**
	 * Reset the zebra box and all internal counters.
	 * @throws IOException
	 */
	public void reset() throws IOException;

	void setPCPulseStart(double val) throws Exception;

	double getPCPulseStart() throws Exception;

	double getPCPulseStartRBV() throws Exception;
}
