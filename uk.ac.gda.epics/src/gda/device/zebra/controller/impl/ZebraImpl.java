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

import gda.device.zebra.controller.SoftInputChangedEvent;
import gda.device.zebra.controller.Zebra;
import gda.epics.CachedLazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.Findable;
import gda.observable.Observable;
import gda.observable.ObservableUtil;
import gda.observable.Observer;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.base.Preconditions;

public class ZebraImpl implements Zebra, Findable, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(ZebraImpl.class);
	String name="zebra";

	final public static int SysSignalMin = 0;
	final public static int SysSignalMax = 63;

	final public static String connected = "CONNECTED";
	final public static String store = "STORE";
	final public static String sysReset = "SYS_RESET";
	final public static String sysResetProc = "SYS_RESET.PROC";
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
	final public static String PCDir = "PC_DIR";

	final public static double PCGateStartMin = -214881.9984;
	final public static double PCGateStartMax =  214881.9984;
	final public static double PCGateWidthMin =       0.0000;
	final public static double PCGateWidthMax =  214881.9984;
	final public static double PCGateStepMin =       0.0000;
	final public static double PCGateStepMax =  214881.9984;
	final public static int PCGateNumberOfGatesMin = 0;
	final public static int PCGateNumberOfGatesMax = 2147483647;

	final public static String PCGateSource = "PC_GATE_SEL";
	final public static String PCGateStart = "PC_GATE_START";
	final public static String PCGateWidth = "PC_GATE_WID";
	final public static String PCGateNumberOfGates = "PC_GATE_NGATE";
	final public static String PCGateStep = "PC_GATE_STEP";
	final public static String PCGateStatus = "PC_GATE_OUT";

	final public static double PCPulseDelayMin =       0.0000;
	final public static double PCPulseDelayMax =  214881.9984;
	final public static double PCPulseWidthMin =       0.0000;
	final public static double PCPulseWidthMax =  214881.9984;
	final public static double PCPulseStepMin  =       0.0016;
	final public static double PCPulseStepMax  =  214881.9984;

	final public static String PCPulseSource = "PC_PULSE_SEL";
	final public static String PCPulseDelay = "PC_PULSE_DLY";
	final public static String PCPulseWidth = "PC_PULSE_WID";
	final public static String PCPulseStep = "PC_PULSE_STEP";
	final public static String PCPulseStatus = "PC_PULSE_OUT";

	final public static int PCPulsePulseMaxMin = 0;
	final public static int PCPulsePulseMaxMax = 2147483647;

	public static final String PCCaptureBitField = "PC_BIT_CAP";
	public static final String PCEnc = "PC_ENC";
	public static final String PCEnc1Aval = "PC_ENC1";
	public static final String PCTime = "PC_TIME";
	public static final String PCNumberOfPointsCaptured = "PC_NUM_CAP";
	public static final String PCNumberOfPointsDownloaded = "PC_NUM_DOWN";
	public static final String PCPulseStepRBV = "PC_PULSE_STEP:RBV";
	public static final String PCPulseWidthRBV = "PC_PULSE_WID:RBV";
	public static final String PCPulseDelayRBV = "PC_PULSE_DLY:RBV";
	final public static String PCGateStartRBV = "PC_GATE_START:RBV";
	final public static String PCGateWidthRBV = "PC_GATE_WID:RBV";
	final public static String PCTimeUnit = "PC_TSPRE";
	private static final String PCPulseMax = "PC_PULSE_MAX";
	public static final String PCArmInput="PC_ARM_INP";

	private static final String SYS_SOFT_IN_PV = "SOFT_IN";

	final public static double PulseDelayMin = 0.0001;
	final public static double PulseDelayMax = 6.5535;

	final public static double PulseWidthMin = 0.0001;
	final public static double PulseWidthMax = 6.5535;

	String zebraPrefix;

	CachedLazyPVFactory dev;



	private boolean useAvalField=false;

	public boolean isUseAvalField() {
		return useAvalField;
	}

	/**
	 *
	 * @param useAvalField if true the captured ENC1 values are stored in .AVAL field ( original IOC interface). Default is false
	 */
	public void setUseAvalField(boolean useAvalField) {
		this.useAvalField = useAvalField;
	}

	@Override
	public void setPCPulseSource(int val) throws Exception {
		assert (val == PC_PULSE_SOURCE_POSITION ||
				val == PC_PULSE_SOURCE_TIME ||
				val == PC_PULSE_SOURCE_EXTERNAL);

		dev.getIntegerPVValueCache(PCPulseSource).putWait(val);
	}

	@Override
	public int getPCPulseSource() throws Exception {
		return dev.getIntegerPVValueCache(PCPulseSource).get();
	}

	@Override
	public void setPCPulseDelay(double val) throws Exception {
		assert (PCPulseDelayMin <= val && val <= PCPulseDelayMax);
		dev.getDoublePVValueCache(PCPulseDelayRBV).resetCache();
		dev.getDoublePVValueCache(PCPulseDelay).putWait(val);
	}

	@Override
	public double getPCPulseDelay() throws Exception {
		return dev.getDoublePVValueCache(PCPulseDelay).get();
	}
	@Override
	public double getPCPulseDelayRBV() throws Exception {
		return dev.getDoublePVValueCache(PCPulseDelayRBV).get();
	}

	@Override
	public void setPCPulseWidth(double val) throws Exception {
		assert (PCPulseWidthMin <= val && val <= PCPulseWidthMax);
		// TODO: PULSE_STEP *must* be bigger than PULSE_WID so warn here if it isn't!
		dev.getDoublePVValueCache(PCPulseWidthRBV).resetCache();
		dev.getDoublePVValueCache(PCPulseWidth).putWait(val);
	}

	@Override
	public double getPCPulseWidth() throws Exception {
		return dev.getDoublePVValueCache(PCPulseWidth).get();
	}
	@Override
	public double getPCPulseWidthRBV() throws Exception {
		return dev.getDoublePVValueCache(PCPulseWidthRBV).get();
	}

	@Override
	public void setPCPulseStep(double val) throws Exception {
		assert (PCPulseStepMin <= val && val <= PCPulseStepMax);
		dev.getDoublePVValueCache(PCPulseStepRBV).resetCache();
		dev.getDoublePVValueCache(PCPulseStep).putWait(val);
	}

	@Override
	public double getPCPulseStep() throws Exception {
		return dev.getDoublePVValueCache(PCPulseStep).get();
	}
	@Override
	public double getPCPulseStepRBV() throws Exception {
		return dev.getDoublePVValueCache(PCPulseStepRBV).get();
	}

	@Override
	public int getPCPulseMax() throws Exception {
		return dev.getIntegerPVValueCache(PCPulseMax).get();
	}

	@Override
	public void setPCPulseMax(int val) throws Exception {
		assert (PCPulsePulseMaxMin <= val && val <= PCPulsePulseMaxMax);
		dev.getIntegerPVValueCache(PCPulseMax).putWait(val);
	}

	@Override
	public void setPCGateSource(int val) throws Exception {
		assert (val == PC_GATE_SOURCE_POSITION ||
				val == PC_GATE_SOURCE_TIME ||
				val == PC_GATE_SOURCE_EXTERNAL);

		dev.getIntegerPVValueCache(PCGateSource).putWait(val);
	}

	@Override
	public int getPCGateSource() throws Exception {
		return dev.getPVInteger(PCGateSource).get();
	}

	@Override
	public void setPCGateStart(double val) throws Exception {
		assert (PCGateStartMin <= val && val <= PCGateStartMax);
		dev.getDoublePVValueCache(PCGateStartRBV).resetCache();
		dev.getDoublePVValueCache(PCGateStart).putWait(val);
	}

	@Override
	public double getPCGateStart() throws Exception {
		return dev.getDoublePVValueCache(PCGateStart).get();
	}
	@Override
	public double getPCGateStartRBV() throws Exception {
		return dev.getDoublePVValueCache(PCGateStartRBV).get();
	}

	@Override
	public void setPCGateWidth(double val) throws Exception {
		assert (PCGateWidthMin <= val && val <= PCGateWidthMax);
		dev.getDoublePVValueCache(PCGateWidthRBV).resetCache();
		dev.getDoublePVValueCache(PCGateWidth).putWait(val);
	}

	@Override
	public double getPCGateWidth() throws Exception {
		return dev.getDoublePVValueCache(PCGateWidth).get();
	}
	@Override
	public double getPCGateWidthRBV() throws Exception {
		return dev.getDoublePVValueCache(PCGateWidthRBV).get();
	}

	@Override
	public void setPCGateStep(double val) throws Exception {
		assert (PCGateStepMin <= val && val <= PCGateStepMax);
		dev.getDoublePVValueCache(PCGateStep).putWait(val);
	}

	@Override
	public double getPCGateStep() throws Exception {
		return dev.getDoublePVValueCache(PCGateStep).get();
	}

	@Override
	public void setPCArmSource(int val) throws Exception {
		assert (val == PC_ARM_SOURCE_SOFT ||
				val == PC_ARM_SOURCE_EXTERNAL);
		dev.getIntegerPVValueCache(PCArmSource).putWait(val);
	}

	@Override
	public int getPCArmSource() throws Exception {
		return dev.getIntegerPVValueCache(PCArmSource).get();
	}

	@Override
	public void pcArm() throws Exception {
		dev.getPVInteger(sysResetProc).putWait(1); // TODO: Do we really need to RESET before each Arm?
		dev.getPVInteger(PCArm).putWait(1);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
		}
	}

	@Override
	public void pcDisarm() throws Exception {
		dev.getPVInteger(PCDisArm).putWait(1,5);
	}

	@Override
	public boolean isPCArmed() throws Exception {
		return dev.getPVInteger(PCArmOut).get() == 1;
	}

	@Override
	public void setPCCaptureBitField(int val) throws Exception {
		dev.getIntegerPVValueCache(PCCaptureBitField).putWait(val);
	}

	@Override
	public int getPCCaptureBitField() throws Exception {
		return dev.getIntegerPVValueCache(PCCaptureBitField).get();
	}

	@Override
	public void setPCEnc(int val) throws Exception {
		assert (val == PC_ENC_ENC1 ||
				val == PC_ENC_ENC2 ||
				val == PC_ENC_ENC3 ||
				val == PC_ENC_ENC4 ||
				val == PC_ENC_ENCSUM);
		dev.getIntegerPVValueCache(PCEnc).putWait(val);
	}

	@Override
	public int getPCEnc() throws Exception {
		return dev.getIntegerPVValueCache(PCEnc).get();
	}

	@Override
	public void setPCTimeUnit(int val) throws Exception {
		assert (val == PC_TIMEUNIT_10SEC ||
				val == PC_TIMEUNIT_SEC ||
				val == PC_TIMEUNIT_MS);
		dev.getIntegerPVValueCache(PCTimeUnit).putWait(val);
	}

	@Override
	public int getPCTimeUnit() throws Exception {
		return dev.getIntegerPVValueCache(PCTimeUnit).get();
	}

	@Override
	public void setPCGateNumberOfGates(int val) throws Exception {
		assert (PCGateNumberOfGatesMin <= val && val <= PCGateNumberOfGatesMax);
		dev.getIntegerPVValueCache(PCGateNumberOfGates).putWait(val);
	}

	@Override
	public int getPCGateNumberOfGates() throws Exception {
		return dev.getIntegerPVValueCache(PCGateNumberOfGates).get();
	}

	@Override
	public void setPCNumberOfPointsCaptured(int val) throws Exception {
		// TODO: Is this really supposed to be settable? If so, what range is acceptable?
		//assert (PCNumberOfPointsCapturedMin <= val && val <= PCNumberOfPointsCapturedMax);
		dev.getIntegerPVValueCache(PCNumberOfPointsCaptured).putWait(val);
	}

	@Override
	public int getPCNumberOfPointsCaptured() throws Exception {
		return dev.getIntegerPVValueCache(PCNumberOfPointsCaptured).get();
	}


	public String getZebraPrefix() {
		return zebraPrefix;
	}

	public void setZebraPrefix(String zebraPrefix) {
		this.zebraPrefix = zebraPrefix;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if( name == null || name.isEmpty())
			throw new Exception("name is not set");
		if (zebraPrefix == null || zebraPrefix.isEmpty())
			throw new Exception("zebraPrefix is not set");
		dev = new CachedLazyPVFactory(zebraPrefix);
	}

	@Override
	public ReadOnlyPV<Double[]> getEnc1AvalPV() {
		return dev.getReadOnlyPVDoubleArray(useAvalField? PCEnc1Aval + ".AVAL": PCEnc1Aval);
	}

	@Override
	public ReadOnlyPV<Integer> getNumberOfPointsCapturedPV() {
		return dev.getReadOnlyPVInteger(PCNumberOfPointsCaptured);
	}

	@Override
	public ReadOnlyPV<Integer> getNumberOfPointsDownloadedPV() {
		return dev.getReadOnlyPVInteger(PCNumberOfPointsDownloaded);
	}

	@Override
	public ReadOnlyPV<Double[]> getPCTimePV() {
		return dev.getReadOnlyPVDoubleArray(PCTime);
	}

	@Override
	public void setPCDir(int val) throws Exception {
		assert (val == PC_DIR_POSITIVE ||
				val == PC_DIR_NEGATIVE);
		dev.getIntegerPVValueCache(PCDir).putWait(val);
	}

	@Override
	public int getPCDir() throws Exception {
		return dev.getIntegerPVValueCache(PCDir).get();
	}

	@Override
	public void setPulseInput(int pulseId, int inputSignal) throws Exception {
		assert(SysSignalMin <= inputSignal && inputSignal <= SysSignalMax);
		dev.getIntegerPVValueCache("PULSE"+pulseId+"_INP").putWait(inputSignal);
	}

	@Override
	public void setPulseDelay(int pulseId, double delay) throws Exception {
		assert (PulseDelayMin <= delay && delay <= PulseDelayMax);
		dev.getDoublePVValueCache("PULSE"+pulseId +"_DLY").putWait(delay);
	}

	@Override
	public void setPulseWidth(int pulseId, double width) throws Exception {
		assert (PulseWidthMin <= width && width <= PulseWidthMax);
		dev.getDoublePVValueCache("PULSE"+pulseId +"_WID").putWait(width);
	}

	@Override
	public void setPulseTimeUnit(int pulseId, int timeunit) throws Exception {
		assert (timeunit == PULSE_TIMEUNIT_10SEC ||
				timeunit == PULSE_TIMEUNIT_SEC ||
				timeunit == PULSE_TIMEUNIT_MS);
		dev.getIntegerPVValueCache("PULSE"+pulseId+"_PRE").putWait(timeunit);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void setOutTTL(int outId, int outputSignal) throws Exception {
		assert(SysSignalMin <= outputSignal && outputSignal <= SysSignalMax);
		dev.getIntegerPVValueCache("OUT"+outId+"_TTL").putWait(outputSignal);
	}

	@Override
	public boolean getTtlOutputState(int output) throws IOException {
		Preconditions.checkArgument(1 <= output && output <= 4);
		final String pvSuffix = String.format("OUT%d_TTL:STA", output);
		final PV<Integer> pv = dev.getPVInteger(pvSuffix);
		final int value = pv.get();
		return (value == 1);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
		dev.getIntegerPVValueCache(pvSuffix).putWait(val);
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
		dev.getIntegerPVValueCache(PCArmInput).putWait(input);

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void encCopyMotorPosToZebra(int posNum) throws Exception {
		Preconditions.checkArgument(1 <= posNum && posNum <= 4, "posNum must be between 1 and 4 inclusive");
		final String pvSuffix = String.format("M%d:SETPOS.PROC", posNum);
		final PV<Integer> pv = dev.getPVInteger(pvSuffix);
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

	int lastSoftInputsValue;

	private final Lock softInputsLock = new ReentrantLock();

	private void startMonitoringSoftInputs() {

		final PV<Integer> pv = dev.getPVInteger(SYS_SOFT_IN_PV);


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
		final PV<Integer> pv = dev.getPVInteger(SYS_SOFT_IN_PV);
		final int softInputPvValue = pv.get();
		return isSoftInputSet(softInputPvValue, inputNumber);
	}

	@Override
	public void setSoftInput(int inputNumber, boolean set) throws IOException {

		final PV<Integer> pv = dev.getPVInteger(SYS_SOFT_IN_PV);

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
		final PV<Integer> pv = dev.getPVInteger(inPV);
		final int sysStatPvValue = pv.get();
		logger.info("Zebra '" + this.name + "': isSysStatSet(" + sysStat + ") " + inPV + " = " + sysStatPvValue);
		return isSysStatSet(sysStatPvValue, sysStat);
	}

	private static boolean isSysStatSet(int sysStatPvValue, int inputNumber) {
		return ((sysStatPvValue & (1<<inputNumber)) > 0);
	}

	@Override
	public void reset() throws IOException {
		dev.getPVInteger(sysResetProc).putWait(1);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
