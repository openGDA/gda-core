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

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.base.Preconditions;

import gda.device.zebra.LogicGateConfiguration;
import gda.device.zebra.controller.SoftInputChangedEvent;
import gda.device.zebra.controller.Zebra;
import gda.epics.CachedLazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.Findable;
import gda.observable.Observable;
import gda.observable.ObservableUtil;
import gda.observable.Observer;

public class ZebraImpl implements Zebra, Findable, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(ZebraImpl.class);
	private String name="zebra";

	private static final int SysSignalMin = 0;
	private static final int SysSignalMax = 63;

	private static final String connected = "CONNECTED";
	private static final String store = "STORE";
	private static final String sysReset = "SYS_RESET";
	private static final String sysResetProc = "SYS_RESET.PROC";
	private static final String sysVer = "SYS_VER";
	private static final String sysStat1Lo = "SYS_STAT1LO";
	private static final String sysStat1Hi = "SYS_STAT1HI";
	private static final String sysStat1 = "SYS_STAT1";
	private static final String sysStat2Lo = "SYS_STAT2LO";
	private static final String sysStat2Hi = "SYS_STAT2HI";
	private static final String sysStat2 = "SYS_STAT2";
	private static final String sysErrState = "SYS_STATERR";

	private static final String PCSource = "PC_ENC";
	private static final String PCTimeUnits = "PC_TSPRE";
	private static final String PCTimeUnits_ms = "ms";
	private static final String PCTimeUnits_s = "s";

	private static final String PCArmSource = "PC_ARM_SEL";
	private static final String PCArm = "PC_ARM";
	private static final String PCDisArm = "PC_DISARM";
	private static final String PCArmOut = "PC_ARM_OUT";
	private static final String PCArmSourceSoft = "Soft";
	private static final String PCArmSourceExternal = "External";
	private static final String PCDir = "PC_DIR";

	private static final double PCGateStartMin = -214881.9984;
	private static final double PCGateStartMax =  214881.9984;
	private static final double PCGateWidthMin =       0.0000;
	private static final double PCGateWidthMax =  214881.9984;
	private static final double PCGateStepMin =       0.0000;
	private static final double PCGateStepMax =   214881.9984;
	private static final int PCGateNumberOfGatesMin = 0;
	private static final int PCGateNumberOfGatesMax = 2147483647;

	private static final String PCGateSource = "PC_GATE_SEL";
	private static final String PCGateStart = "PC_GATE_START";
	private static final String PCGateWidth = "PC_GATE_WID";
	private static final String PCGateNumberOfGates = "PC_GATE_NGATE";
	private static final String PCGateStep = "PC_GATE_STEP";
	private static final String PCGateStatus = "PC_GATE_OUT";

	private static final double PCPulseDelayMin =       0.0000;
	private static final double PCPulseDelayMax =  214881.9984;
	private static final double PCPulseWidthMin =       0.0000;
	private static final double PCPulseWidthMax =  214881.9984;
	private static final double PCPulseStepMin  =       0.0016;
	private static final double PCPulseStepMax  =  214881.9984;

	private static final String PCPulseSource = "PC_PULSE_SEL";
	private static final String PCPulseDelay = "PC_PULSE_DLY";
	private static final String PCPulseStart = "PC_PULSE_START";
	private static final String PCPulseWidth = "PC_PULSE_WID";
	private static final String PCPulseStep = "PC_PULSE_STEP";
	private static final String PCPulseStatus = "PC_PULSE_OUT";

	private static final int PCPulsePulseMaxMin = 0;
	private static final int PCPulsePulseMaxMax = 2147483647;

	private static final String PCCaptureBitField = "PC_BIT_CAP";
	private static final String PCEnc = "PC_ENC";
	private static final String PCEnc1Aval = "PC_ENC1";
	private static final String PCTime = "PC_TIME";
	public static final String[] PCCapture = {"PC_ENC1", "PC_ENC2", "PC_ENC3", "PC_ENC4", "PC_SYS1", "PC_SYS2", "PC_DIV1", "PC_DIV2", "PC_DIV3", "PC_DIV4", "PC_TIME"};
	private static final String PCNumberOfPointsCaptured = "PC_NUM_CAP";
	private static final String PCNumberOfPointsDownloaded = "PC_NUM_DOWN";
	private static final String PCPulseStepRBV = "PC_PULSE_STEP:RBV";
	private static final String PCPulseWidthRBV = "PC_PULSE_WID:RBV";
	private static final String PCPulseDelayRBV = "PC_PULSE_DLY:RBV";
	private static final String PCGateStartRBV = "PC_GATE_START:RBV";
	private static final String PCGateWidthRBV = "PC_GATE_WID:RBV";
	private static final String PCTimeUnit = "PC_TSPRE";
	private static final String PCPulseMax = "PC_PULSE_MAX";
	private static final String PCArmInput="PC_ARM_INP";

	private static final String SYS_SOFT_IN_PV = "SOFT_IN";

	private static final double PulseDelayMin = 0.0001;
	private static final double PulseDelayMax = 6.5535;

	private static final double PulseWidthMin = 0.0001;
	private static final double PulseWidthMax = 6.5535;

	private String zebraPrefix;

	private CachedLazyPVFactory pvFactory;

	@Deprecated
	private boolean useAvalField = false;

	private boolean armPutNoWait = false;

	public boolean isArmPutNoWait() {
		return armPutNoWait;
	}

	/**
	 * This is for EPICS support module versions 1-14 and above.<br>
	 * This switches the behaviour to be compatible with the way Area Detector behaves i.e. a put with wait to arm the detector only returns once the
	 * acquisition is complete.
	 */
	public void setArmPutNoWait(boolean armPutNoWait) {
		this.armPutNoWait = armPutNoWait;
	}

	/**
	 * This is for EPICS support module versions below 1-7
	 *
	 * @param useAvalField
	 *            if true the captured ENC1 values are stored in .AVAL field ( original IOC interface). Default is false
	 */
	@Deprecated
	@Override
	public void setUseAvalField(boolean useAvalField) {
		this.useAvalField = useAvalField;
	}

	@Deprecated
	@Override
	public boolean isUseAvalField() {
		return useAvalField;
	}

	@Override
	public void setPvFactory(CachedLazyPVFactory pvFactory) {
		this.pvFactory = pvFactory;
	}

	@Override
	public void setPCPulseSource(int val) throws Exception {
		assert (val == PC_PULSE_SOURCE_POSITION ||
				val == PC_PULSE_SOURCE_TIME ||
				val == PC_PULSE_SOURCE_EXTERNAL);

		pvFactory.getIntegerPVValueCache(PCPulseSource).putWait(val);
	}

	@Override
	public int getPCPulseSource() throws Exception {
		return pvFactory.getIntegerPVValueCache(PCPulseSource).get();
	}

	@Override
	public void setPCPulseDelay(double val) throws Exception {
		assert (PCPulseDelayMin <= val && val <= PCPulseDelayMax);
		pvFactory.getDoublePVValueCache(PCPulseDelayRBV).resetCache();
		pvFactory.getDoublePVValueCache(PCPulseDelay).putWait(val);
	}

	@Override
	public double getPCPulseDelay() throws Exception {
		return pvFactory.getDoublePVValueCache(PCPulseDelay).get();
	}

	@Override
	public double getPCPulseDelayRBV() throws Exception {
		return pvFactory.getDoublePVValueCache(PCPulseDelayRBV).get();
	}

	@Override
	public void setPCPulseStart(double val) throws Exception {
		pvFactory.getDoublePVValueCache(PCPulseStart).putWait(val);
	}

	@Override
	public double getPCPulseStart() throws Exception {
		return pvFactory.getDoublePVValueCache(PCPulseStart).get();
	}
	@Override
	public double getPCPulseStartRBV() throws Exception {
		return pvFactory.getDoublePVValueCache(PCPulseStart).get();
	}

	@Override
	public void setPCPulseWidth(double val) throws Exception {
		assert (PCPulseWidthMin <= val && val <= PCPulseWidthMax);
		// TODO: PULSE_STEP *must* be bigger than PULSE_WID so warn here if it isn't!
		pvFactory.getDoublePVValueCache(PCPulseWidthRBV).resetCache();
		pvFactory.getDoublePVValueCache(PCPulseWidth).putWait(val);
	}

	@Override
	public double getPCPulseWidth() throws Exception {
		return pvFactory.getDoublePVValueCache(PCPulseWidth).get();
	}
	@Override
	public double getPCPulseWidthRBV() throws Exception {
		return pvFactory.getDoublePVValueCache(PCPulseWidthRBV).get();
	}

	@Override
	public void setPCPulseStep(double val) throws Exception {
		assert (PCPulseStepMin <= val && val <= PCPulseStepMax);
		pvFactory.getDoublePVValueCache(PCPulseStepRBV).resetCache();
		pvFactory.getDoublePVValueCache(PCPulseStep).putWait(val);
	}

	@Override
	public double getPCPulseStep() throws Exception {
		return pvFactory.getDoublePVValueCache(PCPulseStep).get();
	}
	@Override
	public double getPCPulseStepRBV() throws Exception {
		return pvFactory.getDoublePVValueCache(PCPulseStepRBV).get();
	}

	@Override
	public int getPCPulseMax() throws Exception {
		return pvFactory.getIntegerPVValueCache(PCPulseMax).get();
	}

	@Override
	public void setPCPulseMax(int val) throws Exception {
		assert (PCPulsePulseMaxMin <= val && val <= PCPulsePulseMaxMax);
		pvFactory.getIntegerPVValueCache(PCPulseMax).putWait(val);
	}

	@Override
	public void setPCGateSource(int val) throws Exception {
		assert (val == PC_GATE_SOURCE_POSITION ||
				val == PC_GATE_SOURCE_TIME ||
				val == PC_GATE_SOURCE_EXTERNAL);

		pvFactory.getIntegerPVValueCache(PCGateSource).putWait(val);
	}

	@Override
	public int getPCGateSource() throws Exception {
		return pvFactory.getPVInteger(PCGateSource).get();
	}

	@Override
	public void setPCGateStart(double val) throws Exception {
		assert (PCGateStartMin <= val && val <= PCGateStartMax);
		pvFactory.getDoublePVValueCache(PCGateStartRBV).resetCache();
		pvFactory.getDoublePVValueCache(PCGateStart).putWait(val);
	}

	@Override
	public double getPCGateStart() throws Exception {
		return pvFactory.getDoublePVValueCache(PCGateStart).get();
	}
	@Override
	public double getPCGateStartRBV() throws Exception {
		return pvFactory.getDoublePVValueCache(PCGateStartRBV).get();
	}

	@Override
	public void setPCGateWidth(double val) throws Exception {
		assert (PCGateWidthMin <= val && val <= PCGateWidthMax);
		pvFactory.getDoublePVValueCache(PCGateWidthRBV).resetCache();
		pvFactory.getDoublePVValueCache(PCGateWidth).putWait(val);
	}

	@Override
	public double getPCGateWidth() throws Exception {
		return pvFactory.getDoublePVValueCache(PCGateWidth).get();
	}
	@Override
	public double getPCGateWidthRBV() throws Exception {
		return pvFactory.getDoublePVValueCache(PCGateWidthRBV).get();
	}

	@Override
	public void setPCGateStep(double val) throws Exception {
		assert (PCGateStepMin <= val && val <= PCGateStepMax);
		pvFactory.getDoublePVValueCache(PCGateStep).putWait(val);
	}

	@Override
	public double getPCGateStep() throws Exception {
		return pvFactory.getDoublePVValueCache(PCGateStep).get();
	}

	@Override
	public void setPCArmSource(int val) throws Exception {
		assert (val == PC_ARM_SOURCE_SOFT ||
				val == PC_ARM_SOURCE_EXTERNAL);
		pvFactory.getIntegerPVValueCache(PCArmSource).putWait(val);
	}

	@Override
	public int getPCArmSource() throws Exception {
		return pvFactory.getIntegerPVValueCache(PCArmSource).get();
	}

	@Override
	public void pcArm() throws Exception {
		logger.trace("pcArm()...");
		//reset(); // Before re-enabling this, please leave a note here explaining why it was re-enabled.

		if (armPutNoWait) {
			pvFactory.getPVInteger(PCArm).putNoWait(1);
		} else {
			pvFactory.getPVInteger(PCArm).putWait(1);
		}

		while (!isPCArmed()) {
			logger.info("Zebra not yet armed, waiting...");
			Thread.sleep(100);
		}
		logger.trace("...pcArm()");
	}

	@Override
	public void pcDisarm() throws Exception {
		pvFactory.getPVInteger(PCDisArm).putWait(1,5);
	}

	@Override
	public boolean isPCArmed() throws Exception {
		return pvFactory.getPVInteger(PCArmOut).get() == 1;
	}

	@Override
	public void setPCCaptureBitField(int val) throws Exception {
		pvFactory.getIntegerPVValueCache(PCCaptureBitField).putWait(val);
	}

	@Override
	public int getPCCaptureBitField() throws Exception {
		return pvFactory.getIntegerPVValueCache(PCCaptureBitField).get();
	}

	@Override
	public void setPCEnc(int val) throws Exception {
		assert (val == PC_ENC_ENC1 ||
				val == PC_ENC_ENC2 ||
				val == PC_ENC_ENC3 ||
				val == PC_ENC_ENC4 ||
				val == PC_ENC_ENCSUM);
		pvFactory.getIntegerPVValueCache(PCEnc).putWait(val);
	}

	@Override
	public int getPCEnc() throws Exception {
		return pvFactory.getIntegerPVValueCache(PCEnc).get();
	}

	@Override
	public void setPCTimeUnit(int val) throws Exception {
		assert (val == PC_TIMEUNIT_10SEC ||
				val == PC_TIMEUNIT_SEC ||
				val == PC_TIMEUNIT_MS);
		pvFactory.getIntegerPVValueCache(PCTimeUnit).putWait(val);
	}

	@Override
	public int getPCTimeUnit() throws Exception {
		return pvFactory.getIntegerPVValueCache(PCTimeUnit).get();
	}

	@Override
	public void setPCGateNumberOfGates(int val) throws Exception {
		assert (PCGateNumberOfGatesMin <= val && val <= PCGateNumberOfGatesMax);
		pvFactory.getIntegerPVValueCache(PCGateNumberOfGates).putWait(val);
	}

	@Override
	public int getPCGateNumberOfGates() throws Exception {
		return pvFactory.getIntegerPVValueCache(PCGateNumberOfGates).get();
	}

	@Override
	public void setPCNumberOfPointsCaptured(int val) throws Exception {
		// TODO: Is this really supposed to be settable? If so, what range is acceptable?
		//assert (PCNumberOfPointsCapturedMin <= val && val <= PCNumberOfPointsCapturedMax);
		pvFactory.getIntegerPVValueCache(PCNumberOfPointsCaptured).putWait(val);
	}

	@Override
	public int getPCNumberOfPointsCaptured() throws Exception {
		return pvFactory.getIntegerPVValueCache(PCNumberOfPointsCaptured).get();
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
	public void afterPropertiesSet() throws Exception {
		if( name == null || name.isEmpty())
			throw new Exception("name is not set");
		if (zebraPrefix == null || zebraPrefix.isEmpty())
			throw new Exception("zebraPrefix is not set");
		if (pvFactory == null) {
			pvFactory = new CachedLazyPVFactory(zebraPrefix);
		}
	}

	@Override
	public ReadOnlyPV<Double[]> getPcCapturePV(int capture) {
		Preconditions.checkArgument(0 <= capture && capture <= 10, "capture=%i, must be between 0 and 10 inclusive", capture);
		return pvFactory.getReadOnlyPVDoubleArray(PCCapture[capture]);
	}

	@Deprecated
	@Override
	public ReadOnlyPV<Double[]> getEnc1AvalPV() {
		return pvFactory.getReadOnlyPVDoubleArray(useAvalField? PCEnc1Aval + ".AVAL": PCEnc1Aval);
	}

	@Override
	public ReadOnlyPV<Double[]> getEncPV(int encoder) {
		String pcEnc;
		switch (encoder) {
		case Zebra.PC_ENC_ENC1:
			pcEnc = "PC_ENC1";
			break;
		case Zebra.PC_ENC_ENC2:
			pcEnc = "PC_ENC2";
			break;
		case Zebra.PC_ENC_ENC3:
			pcEnc = "PC_ENC3";
			break;
		case Zebra.PC_ENC_ENC4:
			pcEnc = "PC_ENC4";
			break;
		default:
			pcEnc = "PC_ENC1";
			break;
		}
		return pvFactory.getReadOnlyPVDoubleArray(pcEnc);
	}

	@Override
	public ReadOnlyPV<Integer> getNumberOfPointsCapturedPV() {
		return pvFactory.getReadOnlyPVInteger(PCNumberOfPointsCaptured);
	}

	@Override
	public ReadOnlyPV<Integer> getNumberOfPointsDownloadedPV() {
		return pvFactory.getReadOnlyPVInteger(PCNumberOfPointsDownloaded);
	}

	@Override
	public ReadOnlyPV<Double[]> getPCTimePV() {
		return pvFactory.getReadOnlyPVDoubleArray(PCTime);
	}

	@Override
	public void setPCDir(int val) throws Exception {
		assert (val == PC_DIR_POSITIVE ||
				val == PC_DIR_NEGATIVE);
		pvFactory.getIntegerPVValueCache(PCDir).putWait(val);
	}

	@Override
	public int getPCDir() throws Exception {
		return pvFactory.getIntegerPVValueCache(PCDir).get();
	}

	@Override
	public void setPulseInput(int pulseId, int inputSignal) throws Exception {
		assert(SysSignalMin <= inputSignal && inputSignal <= SysSignalMax);
		pvFactory.getIntegerPVValueCache("PULSE"+pulseId+"_INP").putWait(inputSignal);
	}

	@Override
	public void setPulseDelay(int pulseId, double delay) throws Exception {
		assert (PulseDelayMin <= delay && delay <= PulseDelayMax);
		pvFactory.getDoublePVValueCache("PULSE"+pulseId +"_DLY").putWait(delay);
	}

	@Override
	public void setPulseWidth(int pulseId, double width) throws Exception {
		assert (PulseWidthMin <= width && width <= PulseWidthMax);
		pvFactory.getDoublePVValueCache("PULSE"+pulseId +"_WID").putWait(width);
	}

	@Override
	public void setPulseTimeUnit(int pulseId, int timeunit) throws Exception {
		assert (timeunit == PULSE_TIMEUNIT_10SEC ||
				timeunit == PULSE_TIMEUNIT_SEC ||
				timeunit == PULSE_TIMEUNIT_MS);
		pvFactory.getIntegerPVValueCache("PULSE"+pulseId+"_PRE").putWait(timeunit);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void setOutTTL(int outId, int outputSignal) throws Exception {
		assert(SysSignalMin <= outputSignal && outputSignal <= SysSignalMax);
		pvFactory.getIntegerPVValueCache("OUT"+outId+"_TTL").putWait(outputSignal);
	}

	@Override
	public boolean getTtlOutputState(int output) throws IOException {
		Preconditions.checkArgument(1 <= output && output <= 4);
		final String pvSuffix = String.format("OUT%d_TTL:STA", output);
		final PV<Integer> pv = pvFactory.getPVInteger(pvSuffix);
		final int value = pv.get();
		return (value == 1);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void setValue(String beforeUnderscore, int beforeUnderscoreId, String afterUnderscore, int afterUnderscoreId,int val) throws Exception {
		String pvSuffix = beforeUnderscore;
		if (beforeUnderscoreId > 0) {
			pvSuffix += beforeUnderscoreId;
		}
		pvSuffix += "_";
		pvSuffix += afterUnderscore;
		if (afterUnderscoreId > 0) {
			pvSuffix += afterUnderscoreId;
		}
		pvFactory.getIntegerPVValueCache(pvSuffix).putWait(val);
	}


	@Override
	public void setName(String name) {
		this.name=name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setPCArmInput(int input) throws Exception {
		pvFactory.getIntegerPVValueCache(PCArmInput).putWait(input);

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void encCopyMotorPosToZebra(int posNum) throws Exception {
		Preconditions.checkArgument(1 <= posNum && posNum <= 4, "posNum must be between 1 and 4 inclusive");
		final String pvSuffix = String.format("M%d:SETPOS.PROC", posNum);
		final PV<Integer> pv = pvFactory.getPVInteger(pvSuffix);
		pv.putWait(1);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private ObservableUtil<SoftInputChangedEvent> softInputObservable;

	@Override
	public Observable<SoftInputChangedEvent> getSoftInputObservable() {
		if( softInputObservable == null){
			softInputObservable = new ObservableUtil<SoftInputChangedEvent>();
			startMonitoringSoftInputs();
		}
		return softInputObservable;
	}

	private int lastSoftInputsValue;

	private final Lock softInputsLock = new ReentrantLock();

	private void startMonitoringSoftInputs() {

		final PV<Integer> pv = pvFactory.getPVInteger(SYS_SOFT_IN_PV);

		try {
			lastSoftInputsValue = pv.get();

			pv.addObserver(new Observer<Integer>() {
				@Override
				public void update(Observable<Integer> source, Integer arg) {

					softInputsLock.lock();
					try {

						final int newValue = arg;

						for (int input=1; input<=4; input++) {

							final boolean wasSetBefore = isSoftInputSet(lastSoftInputsValue, input);
							final boolean isSetNow = isSoftInputSet(newValue, input);

							if (wasSetBefore != isSetNow) {
								final SoftInputChangedEvent ev = new SoftInputChangedEvent(input, isSetNow);
								softInputObservable.notifyIObservers(softInputObservable, ev);
							}
						}

						lastSoftInputsValue = newValue;
					}

					finally {
						softInputsLock.unlock();
					}
				}
			});
			logger.info("Now monitoring soft inputs");
		}
		catch (Exception e) {
			logger.error("Could not start monitoring soft inputs", e);
		}
	}

	@Override
	public boolean isSoftInputSet(int inputNumber) throws IOException {
		final PV<Integer> pv = pvFactory.getPVInteger(SYS_SOFT_IN_PV);
		final int softInputPvValue = pv.get();
		return isSoftInputSet(softInputPvValue, inputNumber);
	}

	@Override
	public void setSoftInput(int inputNumber, boolean set) throws IOException {
		final PV<Integer> pv = pvFactory.getPVInteger(SYS_SOFT_IN_PV);
		final int oldValue = pv.get();
		final int bit = 1<<(inputNumber-1);
		final int newValue = set ? (oldValue | bit) : (oldValue & ~bit);

		pv.putWait(newValue);
	}

	private static boolean isSoftInputSet(int softInputPvValue, int inputNumber) {
		return ((softInputPvValue & (1<<(inputNumber-1))) > 0);
	}

	// TODO: Add this to the Interface & document what it does
	//@Override
	public boolean isSysStatSet(int sysStat) throws IOException {
		String inPV;
		if ( 0 <= sysStat && sysStat <= 15) { inPV = sysStat1Lo; } else
		if (16 <= sysStat && sysStat <= 31) { inPV = sysStat1Hi; } else
		if (32 <= sysStat && sysStat <= 47) { inPV = sysStat2Lo; } else
		if (48 <= sysStat && sysStat <= 63) { inPV = sysStat2Hi; } else {
			final String err = "Zebra '" + this.name + "': isSysStatSet(" + sysStat + ") invalid, should be between 0 and 63!";
			logger.error(err);
			throw new IllegalArgumentException(err);
		}
		final PV<Integer> pv = pvFactory.getPVInteger(inPV);
		final int sysStatPvValue = pv.get();
		logger.info("Zebra '" + this.name + "': isSysStatSet(" + sysStat + ") " + inPV + " = " + sysStatPvValue);
		return isSysStatSet(sysStatPvValue, sysStat);
	}

	private static boolean isSysStatSet(int sysStatPvValue, int inputNumber) {
		return ((sysStatPvValue & (1<<inputNumber)) > 0);
	}

	@Override
	public void reset() throws IOException {
		logger.trace("reset()...");
		pvFactory.getPVInteger(sysResetProc).putWait(1);
		logger.trace("...reset()");
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void applyAndGateConfig(int gateNumber, LogicGateConfiguration config) throws IOException {
		applyLogicGateConfig("AND", gateNumber, config);
	}

	@Override
	public void applyOrGateConfig(int gateNumber, LogicGateConfiguration config) throws IOException {
		applyLogicGateConfig("OR", gateNumber, config);
	}

	private void applyLogicGateConfig(String type, int gateNumber, LogicGateConfiguration config) throws IOException {

		// Use?
		final String usePvName = String.format("%s%d_ENA", type, gateNumber);
		final PV<Integer> usePv = pvFactory.getPVInteger(usePvName);
		usePv.putWait(booleanArrayToInteger(config.getUse()));

		// Input Source
		for (int input = 1; input <= 4; input++) {
			final String sourcePvName = String.format("%s%d_INP%d", type, gateNumber, input);
			final PV<Integer> sourcePv = pvFactory.getPVInteger(sourcePvName);
			sourcePv.putWait(config.getSources()[input - 1]);
		}

		// Invert
		final String invertPvName = String.format("%s%d_INV", type, gateNumber);
		final PV<Integer> invertPv = pvFactory.getPVInteger(invertPvName);
		invertPv.putWait(booleanArrayToInteger(config.getInvert()));
	}

	private static int booleanArrayToInteger(boolean[] values) {
		int val = 0;
		for (int i = 0; i < values.length; i++) {
			if (values[i]) {
				val += (1 << i);
			}
		}
		return val;
	}

}
