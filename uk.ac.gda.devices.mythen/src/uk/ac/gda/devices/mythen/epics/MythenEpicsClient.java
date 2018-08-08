/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.mythen.epics;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.mythen.client.AcquisitionParameters;
import gda.device.detector.mythen.client.MythenClient;
import gda.epics.CachedLazyPVFactory;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.jython.InterfaceProvider;
import gda.observable.Predicate;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class MythenEpicsClient implements MythenClient, InitializingBean {
	static final Logger logger = LoggerFactory.getLogger(MythenEpicsClient.class);

	private static final String THRESHOLD_ENERGY = "ThresholdEnergy";
	private static final String THRESHOLD_ENERGY_RBV = "ThresholdEnergy_RBV";
	private static final String BEAM_ENERGY = "BeamEnergy";
	private static final String BEAM_ENERGY_RBV = "BeamEnergy_RBV";
	private static final String SETTING = "Setting";
	private static final String BIT_DEPTH = "BitDepth";
	private static final String BIT_DEPTH_RBV = "BitDepth_RBV";
	private static final String ONLINE = "Online";
	private static final String NUM_CYCLES = "NumCycles";
	private static final String NUM_CYCLES_RBV = "NumCycles_RBV";
	private static final String NUM_FRAMES = "NumFrames";
	private static final String NUM_FRAMES_RBV = "NumFrames_RBV";
	private static final String DELAY_TIME = "DelayTime";
	private static final String DELAY_TIME_RBV = "DelayTime_RBV";
	private static final String NUM_GATES = "NumExposures";
	private static final String NUM_GATES_RBV = "NumExposures_RBV";
	//##data correction fields
	private static final String FLAT_FIELD_PATH = "FlatFieldPath";
	private static final String FLAT_FIELD_PATH_RBV = "FlatFieldPath_RBV";
	private static final String USE_FLAT_FIELD = "UseFlatField";
	private static final String FLAT_FIELD_FILE = "FlatFieldFile";
	private static final String FLAT_FIELD_FILE_RBV = "FlatFieldFile_RBV";
	private static final String USE_COUNT_RATE = "UseCountRate";
	private static final String USE_PIXEL_MASK = "UsePixelMask";
	private static final String USE_ANGULAR_CONV = "UseAngularConv";
	//## detector configuration fields
	private static final String SETUP_FILE = "SetupFile";
	private static final String LOAD_SETUP = "LoadSetup";
	private static final String SAVE_SETUP = "SaveSetup";
	//##data file fields
	private static final String FILE_PATH = "FilePath";
	private static final String FILE_PATH_RBV = "FilePath_RBV";
	private static final String FILE_NAME = "FileName";
	private static final String FILE_NAME_RBV = "FileName_RBV";
	private static final String FILE_PATH_EXISTS_RBV = "FilePathExists_RBV";
	private static final String FILE_NUMBER = "FileNumber";
	private static final String FILE_NUMBER_RBV = "FileNumber";
	private static final String AUTO_INCREMENT = "AutoIncrement";
	private static final String AUTO_SAVE = "AutoSave";
	private static final String FILE_TEMPLATE = "FileTemplate";
	private static final String FILE_TEMPLATE_RBV = "FileTemplate_RBV";
//	 private static final String FILE_FORMAT = "FileFormat"; 
	private static final String FULL_FILE_NAME_RBV = "FullFileName_RBV";
	
	private ADBase adbase;
	public enum Setting {
		standard,
		fast,
		highgain;
	}
	private CachedLazyPVFactory dev;
	private String prefix;

	private enum YesNo {
		No,
		Yes;
	}
	public enum TriggerMode{
		auto,
		trigger,
		ro_trigger,
		gating,
		triggered_gating;
	}
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void afterPropertiesSet() {
		
		if (prefix == null) {
			throw new IllegalArgumentException("The prefix must not be null!");
		}
		if (adbase== null) {
			throw new IllegalArgumentException("The adbase must not be null!");
		}
		dev = new CachedLazyPVFactory(prefix);
	}

	public void setThresholdEnergy(double energy) throws Exception {
		dev.getPVDouble(THRESHOLD_ENERGY).putWait(energy);
	}
	
	public double getThresholdEnergy() throws Exception {
		return dev.getPVDouble(THRESHOLD_ENERGY_RBV).get();
	}
	
	public void setBeamEnergy(double energy) throws Exception {
		dev.getPVDouble(BEAM_ENERGY).putWait(energy);
	}

	public double getsetBeamEnergy() throws Exception {
		return dev.getPVDouble(BEAM_ENERGY_RBV).get();
	}
	
	public void setSetting(Setting setting) throws Exception {
		LazyPVFactory.newEnumPV(prefix+SETTING, Setting.class).putWait(setting);
	}
	
	public Setting getSetting() throws Exception {
		return LazyPVFactory.newEnumPV(prefix+SETTING, Setting.class).get();
	}
	public void setBitDepth(int value) throws Exception {
		dev.getPVInteger(BIT_DEPTH).putWait(value);
	}
	
	public int getBitDepth() throws Exception {
		return dev.getPVInteger(BIT_DEPTH_RBV).get();
	}
	
	public void online() throws Exception {
		LazyPVFactory.newEnumPV(prefix+ONLINE,YesNo.class).putWait(YesNo.Yes);
	}
	
	public void offline() throws Exception {
		LazyPVFactory.newEnumPV(prefix+ONLINE,YesNo.class).putWait(YesNo.No);
	}
	
	public boolean isOnline() throws IOException{
		return LazyPVFactory.newEnumPV(prefix+ONLINE,YesNo.class).get()==YesNo.Yes;
	}
	
	public void setNumCycles(int value) throws IOException {
		dev.getPVInteger(NUM_CYCLES).putWait(value);
	}
	public int getNumCycles() throws Exception {
		return dev.getPVInteger(NUM_CYCLES_RBV).get();
	}
	
	public void setNumFrames(int value) throws IOException {
		dev.getPVInteger(NUM_FRAMES).putWait(value);
	}
	public int getNumFrames() throws Exception {
		return dev.getPVInteger(NUM_FRAMES_RBV).get();
	}
	public void setDelayTime(double value) throws IOException {
		dev.getPVDouble(DELAY_TIME).putWait(value);
	}
	public double getDelayTime() throws Exception {
		return dev.getPVDouble(DELAY_TIME_RBV).get();
	}
	public void setNumGates(int value) throws IOException {
		dev.getPVInteger(NUM_GATES).putWait(value);
	}
	public int getNumGates() throws Exception {
		return dev.getPVInteger(NUM_GATES_RBV).get();
	}
	
	public void setFlatFieldPath(String value) throws IOException {
		dev.getPVStringAsBytes(FLAT_FIELD_PATH).putWait(value);
	}
	public String getFlatFieldPath() throws Exception {
		return dev.getPVStringAsBytes(FLAT_FIELD_PATH_RBV).get();
	}
	public void enableFlatFieldCorrection() throws IOException {
		LazyPVFactory.newEnumPV(prefix+USE_FLAT_FIELD, YesNo.class).putWait(YesNo.Yes);
	}
	public void disableFlatFieldCorrection() throws IOException {
		LazyPVFactory.newEnumPV(prefix+USE_FLAT_FIELD, YesNo.class).putWait(YesNo.No);
	}
	public boolean isFlatFieldCorrected() throws IOException {
		return LazyPVFactory.newEnumPV(prefix+USE_FLAT_FIELD, YesNo.class).get()==YesNo.Yes;
	}
	public void setFlatFieldFile(String value) throws IOException {
		dev.getPVStringAsBytes(FLAT_FIELD_FILE).putWait(value);
	}
	public String getFlatFieldFile() throws Exception {
		return dev.getPVStringAsBytes(FLAT_FIELD_FILE_RBV).get();
	}
	
	public void enableCountRateCorrection() throws IOException {
		LazyPVFactory.newEnumPV(prefix+USE_COUNT_RATE, YesNo.class).putWait(YesNo.Yes);
	}
	public void disableCountRateCorrection() throws IOException {
		LazyPVFactory.newEnumPV(prefix+USE_COUNT_RATE, YesNo.class).putWait(YesNo.No);
	}
	public boolean isCountRateCorrected() throws IOException {
		return LazyPVFactory.newEnumPV(prefix+USE_COUNT_RATE, YesNo.class).get()==YesNo.Yes;
	}
	public void enableBadChannelCorrection() throws IOException {
		LazyPVFactory.newEnumPV(prefix+USE_PIXEL_MASK, YesNo.class).putWait(YesNo.Yes);
	}
	public void disableBadChannelCorrection() throws IOException {
		LazyPVFactory.newEnumPV(prefix+USE_PIXEL_MASK, YesNo.class).putWait(YesNo.No);
	}
	public boolean isBadChannelCorrected() throws IOException {
		return LazyPVFactory.newEnumPV(prefix+USE_PIXEL_MASK, YesNo.class).get()==YesNo.Yes;
	}
	public void enableAngularConversion() throws IOException {
		LazyPVFactory.newEnumPV(prefix+USE_ANGULAR_CONV,YesNo.class).putWait(YesNo.Yes);
	}
	public void disableAngularConversion() throws IOException {
		LazyPVFactory.newEnumPV(prefix+USE_ANGULAR_CONV,YesNo.class).putWait(YesNo.No);
	}
	public boolean isAngularConversionEnabled() throws IOException {
		return LazyPVFactory.newEnumPV(prefix+USE_ANGULAR_CONV,YesNo.class).get()==YesNo.Yes;
	}
	public void setConfigFile(String value) throws IOException {
		dev.getPVStringAsBytes(SETUP_FILE).putWait(value);
	}
	public String getConfigFile() throws Exception {
		return dev.getPVStringAsBytes(SETUP_FILE).get();
	}
	public void loadConfigFile() throws IOException {
		LazyPVFactory.newEnumPV(prefix+LOAD_SETUP, YesNo.class).putWait(YesNo.Yes);
	}
	public void saveConfigFile() throws IOException {
		LazyPVFactory.newEnumPV(prefix+SAVE_SETUP,YesNo.class).putWait(YesNo.Yes);
	}
	
	public void setFilePath(String value) throws IOException {
		dev.getPVStringAsBytes(FILE_PATH).putWait(value);
	}
	public String getFilePath() throws Exception {
		return dev.getPVStringAsBytes(FILE_PATH_RBV).get();
	}
	public boolean isFilePathExists() throws IOException {
		return LazyPVFactory.newEnumPV(prefix+FILE_PATH_EXISTS_RBV, YesNo.class).get()==YesNo.Yes;
	}
	public void setFileName(String value) throws IOException {
		dev.getPVStringAsBytes(FILE_NAME).putWait(value);
	}
	public String getFileName() throws Exception {
		return dev.getPVStringAsBytes(FILE_NAME_RBV).get();
	}
	public void setNextFileNumber(int value) throws IOException {
		dev.getPVInteger(FILE_NUMBER).putWait(value);
	}
	public int getNextFileNumber() throws Exception {
		return dev.getPVInteger(FILE_NUMBER_RBV).get();
	}
	public void enableAutoIncrement() throws IOException {
		//dev.getPVInteger(AUTO_INCREMENT).putWait(1);
		LazyPVFactory.newEnumPV(getPrefix()+AUTO_INCREMENT, YesNo.class).putWait(YesNo.Yes);
	}
	public void disableAutoIncrement() throws IOException {
		LazyPVFactory.newEnumPV(getPrefix()+AUTO_INCREMENT, YesNo.class).putWait(YesNo.No);
	}
	public boolean isAutoIncrement() throws IOException {
		return LazyPVFactory.newEnumPV(getPrefix()+AUTO_INCREMENT, YesNo.class).get()==YesNo.Yes;
	}
	public void enableAutoSave() throws IOException {
		LazyPVFactory.newEnumPV(getPrefix()+AUTO_SAVE, YesNo.class).putWait(YesNo.Yes);
	}
	public void disableAutoSave() throws IOException {
		LazyPVFactory.newEnumPV(getPrefix()+AUTO_SAVE, YesNo.class).putWait(YesNo.No);
	}
	public boolean isAutoSave() throws IOException {
		return LazyPVFactory.newEnumPV(getPrefix()+AUTO_SAVE, YesNo.class).get()==YesNo.Yes;
	}
	public void setFileTemplate(String value) throws IOException {
		//dev.getPVString(FILE_TEMPLATE).putWait(value);
		dev.getPVStringAsBytes(FILE_TEMPLATE).putWait(value);
	}

	public String getFileTemplate() throws Exception {
		return dev.getPVStringAsBytes(FILE_TEMPLATE_RBV).get();
	}
	public String getFullFilename() throws Exception {
		return dev.getPVStringAsBytes(FULL_FILE_NAME_RBV).get();
	}

	public void waitForIntPVValEqualTo(PV<Integer> pv, int valWaitedFor, double timeoutSec) throws DeviceException {
		try {
			pv.setValueMonitoring(true);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
		logger.info("Waiting for the value of PV {} to be equal to {}...", pv.getPvName(), valWaitedFor);
		try {
			pv.waitForValue(new EqualTo(valWaitedFor) {
			}, timeoutSec);
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}
	
	public void waitForIntPVValNotEqualTo(PV<Integer> pv, int valWaitedFor, double timeoutSec) throws DeviceException {
		try {
			pv.setValueMonitoring(true);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
		logger.info("Waiting for the value of PV {} to be not equal to {}...", pv.getPvName(), valWaitedFor);
		try {
			pv.waitForValue(new NotEqualTo(valWaitedFor) {
			}, timeoutSec);
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}
	
	/**
	 * method to print message to the Jython Terminal console.
	 * 
	 * @param msg
	 */
	private void print(String msg) {
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print(msg);
		}
	}
	
	public class EqualTo implements Predicate<Integer> {

		private final int value;

		public EqualTo(int value) {
			this.value = value;
		}

		@Override
		public boolean apply(Integer object) {
			return (object == value);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EqualTo other = (EqualTo) obj;
			if (value != other.value)
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "EqualTo(" + value + ")";
		}
	}
	
	public class NotEqualTo implements Predicate<Integer> {

		private final int value;

		public NotEqualTo(int value) {
			this.value = value;
		}

		@Override
		public boolean apply(Integer object) {
			print("apply : " + object.toString());
			return (object != value);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + value;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EqualTo other = (EqualTo) obj;
			if (value != other.value)
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "NotEqualTo(" + value + ")";
		}
	}
	/**
	 * start acquire data from detector asynchronously.
	 * Note the MythenClient interface parameter - {@link AcquisitionParameters} is no longer suitable for Mythen 3. 
	 * This method is implemented here only for matching interface requirement. No detector configuration is set. 
	 */
	@Override
	public void acquire(AcquisitionParameters params) throws DeviceException {
		//set parameters
		
		// acquire data
		try {
			adbase.startAcquiring();
		} catch (Exception e) {
			logger.error("Start acquisition failed", e);
			throw new DeviceException("Start acquisition failed", e);
		}
		
		
	}

	public ADBase getAdbase() {
		return adbase;
	}

	public void setAdbase(ADBase adbase) {
		this.adbase = adbase;
	}
	/**
	 * synchronise acquire data from detector
	 * @throws DeviceException
	 */
	public void startWait() throws DeviceException {
		try {
			adbase.startAcquiringWait();
		} catch (Exception e) {
			logger.error("Start acquisition failed", e);
			throw new DeviceException("Start acquisition failed", e);
		}
	}
	
	public void autoMode() throws Exception {
		setTriggerMode(0);
	}
	public void triggerMode() throws Exception {
		setTriggerMode(1);
	}
	public void ro_TriggerMode() throws Exception {
		setTriggerMode(2);
	}
	public void gatingMode() throws Exception {
		setTriggerMode(3);
	}
	public void triggerredGatingMode() throws Exception {
		setTriggerMode(4);
	}

	public void setTriggerMode(int value) throws Exception {
		adbase.setTriggerMode(value);
	}
	public void setImageMode(int value) throws Exception {
		adbase.setImageMode(value);
	}
	public void setExposure(double exposureTime) throws Exception {
		adbase.setAcquireTime(exposureTime);
	}
	public double getExposure() throws Exception {
		return adbase.getAcquireTime_RBV();
	}
	public void setAcquirePeriod(double acquireperiod) throws Exception {
		adbase.setAcquirePeriod(acquireperiod);
	}
	public double getAcquirePeriod() throws Exception {
		return adbase.getAcquirePeriod_RBV();
	}
	public void stop() throws Exception {
		adbase.stopAcquiring();
	}
	public void resetArrayCounter() throws Exception {
		adbase.setArrayCounter(0);
	}

	public boolean isBusy() {
		return adbase.getStatus()!=Detector.IDLE;
	}

}
