/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import gda.device.zebra.LogicGateConfiguration;
import gda.device.zebra.controller.SoftInputChangedEvent;
import gda.device.zebra.controller.Zebra;
import gda.epics.CachedLazyPVFactory;
import gda.epics.DummyReadOnlyPV;
import gda.epics.ReadOnlyPV;
import gda.factory.Findable;
import gda.observable.Observable;
import gda.observable.Observer;
import gda.observable.Predicate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;


public class ZebraDummy implements Zebra, Findable, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(ZebraDummy.class);

	private static final String ZEBRA_PV = "ZEBRA:DUMMY:PV";

	private String name;
	private double pcPulseDelay;
	private double pcPulseWidth;
	private double pcPulseStep;
	private int pcPulseSource;
	private int pcGateSource;
	private double pcGateStart;
	private double pcGateWidth;
	private double pcGateStep;
	private int pcGateNumberOfGates;
	private int pcArmSource;
	private boolean pcArmed;
	private int pcCaptureBitField;
	private int pcEnc;
	private int pcNumberOfPointsCaptured;
	private int pcTimeUnit;
	private int pcPulseMax;
	private int pcDir;
	private double pcPulseStart;
	private boolean useAvalField;
	private CachedLazyPVFactory pvFactory;
	private String zebraPrefix;

	private Map<Integer, Integer> pulseInputs = new HashMap<>();
	private Map<Integer, Double> pulseDelays = new HashMap<>();
	private Map<Integer, Double> pulseWidths = new HashMap<>();
	private Map<Integer, Integer> pulseTimeUnits = new HashMap<>();
	private Map<Integer, Integer> outTTLs = new HashMap<>();
	private Map<Integer, Boolean> ttlOutputStates = new HashMap<>();
	private Map<Integer, Boolean> softInputSet = new HashMap<>();

	//----------------------------------------------------------------------------------------------------

	private void initialise() {
		name = "ZebraDummy";
		pcPulseDelay = 0;
		pcPulseWidth = 0;
		pcPulseStep = 0;
		pcPulseSource = Zebra.PC_PULSE_SOURCE_POSITION;
		pcGateSource = Zebra.PC_GATE_SOURCE_POSITION;
		pcGateStart = 0;
		pcGateWidth = 0;
		pcGateStep = 0;
		pcGateNumberOfGates = 0;
		pcArmSource = Zebra.PC_ARM_SOURCE_EXTERNAL;
		pcArmed = false;
		pcCaptureBitField = 0;
		pcEnc = Zebra.PC_ENC_ENC1;
		pcNumberOfPointsCaptured = 0;
		pcTimeUnit = Zebra.PC_TIMEUNIT_SEC;
		pcPulseMax = 0;
		pcDir = 0;
		pulseInputs.clear();
		pulseDelays.clear();
		pulseWidths.clear();
		pulseTimeUnits.clear();
		outTTLs.clear();
		ttlOutputStates.clear();
		softInputSet.clear();
	}

	public ZebraDummy() {
		initialise();
	}

	// -----------------------------------------------------------------------------------------------------------------
	// InitialisingBean interface
	// -----------------------------------------------------------------------------------------------------------------
	@Override
	public void afterPropertiesSet() throws Exception {
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Findable interface
	// -----------------------------------------------------------------------------------------------------------------
	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getName() {
		return name;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Zebra interface
	// -----------------------------------------------------------------------------------------------------------------
	@Override
	public double getPCPulseDelay() throws Exception {
		return pcPulseDelay;
	}

	@Override
	public double getPCPulseDelayRBV() throws Exception {
		return pcPulseDelay;
	}

	@Override
	public void setPCPulseDelay(double delay) throws Exception {
		pcPulseDelay = delay;
	}

	@Override
	public double getPCPulseWidth() throws Exception {
		return pcPulseWidth;
	}

	@Override
	public double getPCPulseWidthRBV() throws Exception {
		return pcPulseWidth;
	}

	@Override
	public void setPCPulseWidth(double width) throws Exception {
		pcPulseWidth = width;
	}

	@Override
	public double getPCPulseStep() throws Exception {
		return pcPulseStep;
	}

	@Override
	public double getPCPulseStepRBV() throws Exception {
		return pcPulseStep;
	}

	@Override
	public void setPCPulseStep(double step) throws Exception {
		pcPulseStep = step;
	}

	@Override
	public void setPCPulseSource(int ordinal) throws Exception {
		pcPulseSource = ordinal;
	}

	@Override
	public int getPCPulseSource() throws Exception, Exception {
		return pcPulseSource;
	}

	@Override
	public int getPCGateSource() throws Exception, Exception {
		return pcGateSource;
	}

	@Override
	public void setPCGateSource(int ordinal) throws Exception {
		pcGateSource = ordinal;
	}

	@Override
	public double getPCGateStart() throws Exception {
		return pcGateStart;
	}

	@Override
	public double getPCGateStartRBV() throws Exception {
		return pcGateStart;
	}

	@Override
	public double getPCGateWidth() throws Exception {
		return pcGateWidth;
	}

	@Override
	public int getPCGateNumberOfGates() throws Exception {
		return pcGateNumberOfGates;
	}

	@Override
	public double getPCGateStep() throws Exception {
		return pcGateStep;
	}

	@Override
	public void setPCGateStart(double start) throws Exception {
		pcGateStart = start;
	}

	@Override
	public void setPCGateWidth(double width) throws Exception {
		pcGateWidth = width;
	}

	@Override
	public double getPCGateWidthRBV() throws Exception {
		return pcGateWidth;
	}

	@Override
	public void setPCGateNumberOfGates(int numberOfGates) throws Exception {
		pcGateNumberOfGates = numberOfGates;
	}

	@Override
	public void setPCGateStep(double step) throws Exception {
		pcGateStep = step;
	}

	@Override
	public int getPCArmSource() throws Exception {
		return pcArmSource;
	}

	@Override
	public void setPCArmSource(int ordinal) throws Exception {
		pcArmSource = ordinal;
	}

	@Override
	public boolean isPCArmed() throws Exception {
		return pcArmed;
	}

	@Override
	public void pcArm() throws Exception {
		logger.info("Arm Zebra Box");
		pcArmed = true;
	}

	@Override
	public void pcDisarm() throws Exception {
		logger.info("Dis-Arm Zebra Box");
		pcArmed = false;
	}

	@Override
	public void setPCCaptureBitField(int val) throws Exception {
		pcCaptureBitField = val;
	}

	@Override
	public int getPCCaptureBitField() throws Exception {
		return pcCaptureBitField;
	}

	@Override
	public void setPCEnc(int val) throws Exception {
		pcEnc = val;
	}

	@Override
	public int getPCEnc() throws Exception {
		return pcEnc;
	}

	@Override
	public ReadOnlyPV<Double[]> getPcCapturePV(int capture) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	@Override
	public ReadOnlyPV<Double[]> getEnc1AvalPV() {
		return new DummyReadOnlyPV<Double[]>(ZEBRA_PV, new Double[] { 1.0, 1.3, 1.7 });
	}

	@Override
	public ReadOnlyPV<Double[]> getEncPV(int encoder) {
		return new DummyReadOnlyPV<Double[]>(ZEBRA_PV, new Double[] { 2.0, 2.3, 2.7 });
	}

	@Override
	public ReadOnlyPV<Integer> getNumberOfPointsCapturedPV() {
		return new DummyReadOnlyPV<Integer>(ZEBRA_PV, 42);
	}

	@Override
	public void setPCNumberOfPointsCaptured(int val) throws Exception {
		pcNumberOfPointsCaptured = val;
	}

	@Override
	public int getPCNumberOfPointsCaptured() throws Exception {
		return pcNumberOfPointsCaptured;
	}

	@Override
	public void setPCTimeUnit(int i) throws Exception {
		pcTimeUnit = i;
	}

	@Override
	public int getPCTimeUnit() throws Exception {
		return pcTimeUnit;
	}

	@Override
	public void setPCPulseMax(int numberTriggers) throws Exception {
		pcPulseMax = numberTriggers;
	}

	@Override
	public int getPCPulseMax() throws Exception, Exception {
		return pcPulseMax;
	}

	@Override
	public void setPCPulseInput(int input) throws Exception {
	}

	@Override
	public ReadOnlyPV<Integer> getNumberOfPointsDownloadedPV() {
		return new DummyReadOnlyPV<Integer>(ZEBRA_PV, 33);
	}

	@Override
	public ReadOnlyPV<Double[]> getPCTimePV() {
		return new DummyReadOnlyPV<Double[]>(ZEBRA_PV, new Double[] { 3.5, 4.6, 7.3 });
	}

	@Override
	public void setPCDir(int i) throws Exception {
		pcDir = i;
	}

	@Override
	public int getPCDir() throws Exception {
		return pcDir;
	}

	@Override
	public void setPulseInput(int pulseId, int input) throws Exception {
		pulseInputs.put(pulseId, input);
	}

	@Override
	public void setPulseDelay(int pulseId, double delay) throws Exception {
		pulseDelays.put(pulseId, delay);
	}

	@Override
	public void setPulseWidth(int pulseId, double width) throws Exception {
		pulseWidths.put(pulseId, width);
	}

	@Override
	public void setPulseTimeUnit(int pulseId, int timeunit) throws Exception {
		pulseTimeUnits.put(pulseId, timeunit);
	}

	@Override
	public void setOutTTL(int outId, int val) throws Exception {
		outTTLs.put(outId, val);
	}

	@Override
	public boolean getTtlOutputState(int output) throws IOException {
		final Boolean result = ttlOutputStates.get(output);
		return (result == null) ? false : true;
	}

	@Override
	public void setPCArmInput(int input) throws Exception {
	}

	@Override
	public boolean isSoftInputSet(int inputNumber) throws IOException {
		final Boolean set = softInputSet.get(inputNumber);
		return (set == null) ? false : set;
	}

	@Override
	public void setSoftInput(int inputNumber, boolean set) throws IOException {
		softInputSet.put(inputNumber, set);
	}

	@Override
	public Observable<SoftInputChangedEvent> getSoftInputObservable() {
		return new Observable<SoftInputChangedEvent>() {
			@Override
			public void addObserver(Observer<SoftInputChangedEvent> observer) throws Exception {
			}

			@Override
			public void addObserver(Observer<SoftInputChangedEvent> observer, Predicate<SoftInputChangedEvent> predicate) throws Exception {
			}

			@Override
			public void removeObserver(Observer<SoftInputChangedEvent> observer) {
			}
		};
	}

	@Override
	public void reset() throws IOException {
		initialise();
	}

	@Override
	public void setPCPulseStart(double val) throws Exception {
		pcPulseStart = val;
	}

	@Override
	public double getPCPulseStart() throws Exception {
		return pcPulseStart;
	}

	@Override
	public double getPCPulseStartRBV() throws Exception {
		return pcPulseStart;
	}

	@Override
	public void setUseAvalField(boolean useAvalField) {
		this.useAvalField = useAvalField;
	}

	@Override
	public boolean isUseAvalField() {
		return useAvalField;
	}

	@Override
	public void setPvFactory(CachedLazyPVFactory pvFactory) {
		this.pvFactory = pvFactory;
	}

	@Override
	public String getZebraPrefix() {
		return zebraPrefix;
	}

	@Override
	public void setZebraPrefix(String zebraPrefix) {
		this.zebraPrefix = zebraPrefix;
	}

	@Override
	public void setValue(String beforeUnderscore, int beforeUnderscoreId, String afterUnderscore, int afterUnderscoreId, int val) throws Exception {
	}

	@Override
	public void encCopyMotorPosToZebra(int posNum) throws Exception {
	}

	@Override
	public void applyAndGateConfig(int gateNumber, LogicGateConfiguration config) throws IOException {
		// Do nothing
	}

	@Override
	public void applyOrGateConfig(int gateNumber, LogicGateConfiguration config) throws IOException {
		// Do nothing
	}

}
