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

import gda.device.DeviceException;
import gda.epics.CachedLazyPVFactory;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.jython.InterfaceProvider;
import gda.observable.Predicate;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class MythenEpicsController implements InitializingBean {
	static final Logger logger = LoggerFactory.getLogger(MythenEpicsController.class);

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
	private static final String FILE_FORMAT = "FileFormat";
	private static final String FULL_FILE_NAME_RBV = "FullFileName_RBV";
	
	private enum Setting {
		standard,
		fast,
		highgain;
	}
	private CachedLazyPVFactory dev;
	private String prefix;
	
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
	
	public void setSetting(int setting) throws Exception {
		dev.getPVInteger(SETTING).putWait(setting);
	}
	public int getSetting() throws Exception {
		return dev.getPVInteger(SETTING).get();
	}
	
	public Setting getSettingEnum() throws Exception {
		return LazyPVFactory.newEnumPV(SETTING, Setting.class).get();
	}
	public void setBitDepth(int value) throws Exception {
		dev.getPVInteger(BIT_DEPTH).putWait(value);
	}
	
	public int getBitDepth() throws Exception {
		return dev.getPVInteger(BIT_DEPTH_RBV).get();
	}
	
	public void online() throws Exception {
		dev.getPVInteger(ONLINE).putWait(1);
	}
	
	public void offline() throws Exception {
		dev.getPVInteger(ONLINE).putWait(0);
	}
	
	public boolean isOnline() throws IOException{
		return dev.getPVInteger(ONLINE).get().intValue()==1;
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
	public void setDelayTime(int value) throws IOException {
		dev.getPVInteger(DELAY_TIME).putWait(value);
	}
	public int getDelayTime() throws Exception {
		return dev.getPVInteger(DELAY_TIME_RBV).get();
	}
	public void setNumGates(int value) throws IOException {
		dev.getPVInteger(NUM_GATES).putWait(value);
	}
	public int getNumGates() throws Exception {
		return dev.getPVInteger(NUM_GATES_RBV).get();
	}
	
	public void setFlatFieldPath(String value) throws IOException {
		dev.getPVString(FLAT_FIELD_PATH).putWait(value);
	}
	public String getFlatFieldPath() throws Exception {
		return dev.getPVString(FLAT_FIELD_PATH_RBV).get();
	}
	public void enableFlatFieldCorrection() throws IOException {
		dev.getPVInteger(USE_FLAT_FIELD).putWait(1);
	}
	public void disableFlatFieldCorrection() throws IOException {
		dev.getPVInteger(USE_FLAT_FIELD).putWait(0);
	}
	public boolean isFlatFieldCorrectionEnabled() throws IOException {
		return dev.getPVInteger(USE_FLAT_FIELD).get().intValue()==1;
	}
	public void setFlatFieldFile(String value) throws IOException {
		dev.getPVString(FLAT_FIELD_FILE).putWait(value);
	}
	public String getFlatFieldFile() throws Exception {
		return dev.getPVString(FLAT_FIELD_FILE_RBV).get();
	}
	
	public void enableCountRateCorrection() throws IOException {
		dev.getPVInteger(USE_COUNT_RATE).putWait(1);
	}
	public void disableCountRateCorrection() throws IOException {
		dev.getPVInteger(USE_COUNT_RATE).putWait(0);
	}
	public boolean isCountRateCorrectionEnabled() throws IOException {
		return dev.getPVInteger(USE_COUNT_RATE).get().intValue()==1;
	}
	public void enableBadChannelCorrection() throws IOException {
		dev.getPVInteger(USE_PIXEL_MASK).putWait(1);
	}
	public void disableBadChannelCorrection() throws IOException {
		dev.getPVInteger(USE_PIXEL_MASK).putWait(0);
	}
	public boolean isBadChannelCorrectionEnabled() throws IOException {
		return dev.getPVInteger(USE_PIXEL_MASK).get().intValue()==1;
	}
	public void enableAngularConversion() throws IOException {
		dev.getPVInteger(USE_ANGULAR_CONV).putWait(1);
	}
	public void disableAngularConversion() throws IOException {
		dev.getPVInteger(USE_ANGULAR_CONV).putWait(0);
	}
	public boolean isAngularConversionEnabled() throws IOException {
		return dev.getPVInteger(USE_ANGULAR_CONV).get().intValue()==1;
	}
	public void setConfigFile(String value) throws IOException {
		dev.getPVString(SETUP_FILE).putWait(value);
	}
	public String getConfigFile() throws Exception {
		return dev.getPVString(SETUP_FILE).get();
	}
	public void loadConfigFile() throws IOException {
		dev.getPVInteger(LOAD_SETUP).putWait(1);
	}
	public void saveConfigFile() throws IOException {
		dev.getPVInteger(SAVE_SETUP).putWait(1);
	}
	
	public void setFilePath(String value) throws IOException {
		dev.getPVString(FILE_PATH).putWait(value);
	}
	public String getFilePath() throws Exception {
		return dev.getPVString(FILE_PATH_RBV).get();
	}
	public boolean isFilePathExists() throws IOException {
		return dev.getPVInteger(FILE_PATH_EXISTS_RBV).get().intValue()==1;
	}
	public void setFileName(String value) throws IOException {
		dev.getPVString(FILE_NAME).putWait(value);
	}
	public String getFileName() throws Exception {
		return dev.getPVString(FILE_NAME_RBV).get();
	}
	public void setNextFileNumber(int value) throws IOException {
		dev.getPVInteger(FILE_NUMBER).putWait(value);
	}
	public int getNextFileNumber() throws Exception {
		return dev.getPVInteger(FILE_NUMBER_RBV).get();
	}
	public void enableAutoIncrement() throws IOException {
		dev.getPVInteger(AUTO_INCREMENT).putWait(1);
	}
	public void disableAutoIncrement() throws IOException {
		dev.getPVInteger(AUTO_INCREMENT).putWait(0);
	}
	public void enableAutoSave() throws IOException {
		dev.getPVInteger(AUTO_SAVE).putWait(1);
	}
	public void disableAutoSave() throws IOException {
		dev.getPVInteger(AUTO_SAVE).putWait(0);
	}
	public void setFileTemplate(String value) throws IOException {
		dev.getPVString(FILE_TEMPLATE).putWait(value);
	}

	public String getFileTemplate() throws Exception {
		return dev.getPVString(FILE_TEMPLATE_RBV).get();
	}
	public String getFullFilename() throws Exception {
		return dev.getPVString(FULL_FILE_NAME_RBV).get();
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
	

}
