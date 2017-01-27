/**-
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

package gda.device.detector.pixium;

import static gda.jython.InterfaceProvider.getCurrentScanInformationHolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NXDetector;
import gda.device.detector.addetector.filewriter.MultipleImagesPerHDF5FileWriter;
import gda.device.detector.addetector.filewriter.SingleImagePerFileWriter;
import gda.device.detector.addetector.triggering.AbstractADTriggeringStrategy;
import gda.device.detector.addetector.triggering.PixiumSimpleAcquire;
import gda.device.detector.areadetector.v17.impl.NDFileImpl;
import gda.device.detector.nxdetector.NXPluginBase;
import gda.device.detector.pixium.events.ScanEndEvent;
import gda.device.detector.pixium.events.ScanPointStartEvent;
import gda.device.detector.pixium.events.ScanStartEvent;
import gda.epics.CachedLazyPVFactory;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.Predicate;
import gda.scan.ConcurrentScan;
import gda.scan.RepeatScan;
import gda.scan.ScanInformation;
import gov.aps.jca.TimeoutException;
import uk.ac.gda.util.UnixToWindowsFilePathConverter;

public class PixiumNXDetector extends NXDetector implements IPixiumNXDetector {

	private static final Logger logger = LoggerFactory.getLogger(PixiumNXDetector.class);

	private static final String DETECTOR_NOT_IDLE_ERROR_MESSAGE = "Failed to set value because detector was found in a state different from Idle!";

	private CachedLazyPVFactory dev;
	private String prefix;
	private List<String> earlyFramesTranslatn = new ArrayList<>();
	private List<Integer> allowedPUModes = new ArrayList<>();

	private String shutterPVName;
	private int shutterIntForClose;
	private int shutterIntForOpen;
	private boolean useShutter = true;
	private PV<Integer> shutterPV;
	private Scannable fastshutter=null;
	private Scriptcontroller eventAdmin;

	private long currentPointNumber;

	private PV<Integer> getShutterPV() {
		if (shutterPV == null) {
			shutterPV = LazyPVFactory.newIntegerPV(shutterPVName);
		}
		return shutterPV;
	}

	public String getShutterPVName() {
		return shutterPVName;
	}

	public void setShutterPVName(String shutterPVName) {
		this.shutterPVName = shutterPVName;
	}

	public int getShutterIntForClose() {
		return shutterIntForClose;
	}

	public void setShutterIntForClose(int shutterIntForClose) {
		this.shutterIntForClose = shutterIntForClose;
	}

	public int getShutterIntForOpen() {
		return shutterIntForOpen;
	}

	public void setShutterIntForOpen(int shutterIntForOpen) {
		this.shutterIntForOpen = shutterIntForOpen;
	}

	public boolean isUseShutter() {
		return useShutter;
	}

	public void setUseShutter(boolean useShutter) {
		this.useShutter = useShutter;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		if (prefix == null) {
			throw new IllegalArgumentException("The prefix must not be null!");
		}
		dev = new CachedLazyPVFactory(prefix);

		if (shutterPVName == null) {
			throw new IllegalArgumentException("The shutterPV must not be null for calibration to work!");
		}

		if( shutterPV == null ){
			shutterPV = LazyPVFactory.newIntegerPV(shutterPVName);
		}
		earlyFramesTranslatn.add("Off");
		earlyFramesTranslatn.add("On");
		allowedPUModes.addAll(Arrays.asList(1,3,4,7,13,14,15));
	}

	@Override
	public void atScanStart() throws DeviceException {
		//send event to client with scan info at start so users are informed
		final int scannumber =getCurrentScanInformationHolder().getCurrentScanInformation().getScanNumber();
		final int numberOfPoints = getCurrentScanInformationHolder().getCurrentScanInformation().getNumberOfPoints();
		String dataDir=PathConstructor.createFromDefaultProperty();
		if (!dataDir.endsWith(File.separator)) {
			dataDir += File.separator;
		}
		final String beamline=LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME);
		final String scanFilename=dataDir+String.format("%s-%d", beamline,scannumber) + ".nxs";
		if (getEventAdmin()!=null && getEventAdmin() instanceof ScriptControllerBase) {
			((ScriptControllerBase)getEventAdmin()).update(getEventAdmin(), new ScanStartEvent(scannumber,numberOfPoints,scanFilename));
		}
		print("Scan data will write to : "+ scanFilename);
		currentPointNumber=0;
		super.atScanStart();
	}

	@Override
	public void atPointStart() throws DeviceException {
		currentPointNumber++;
		if (getEventAdmin()!=null && getEventAdmin() instanceof ScriptControllerBase) {
			((ScriptControllerBase)getEventAdmin()).update(getEventAdmin(), new ScanPointStartEvent(currentPointNumber));
		}
		super.atPointStart();
		if (fastshutter!=null) {
			fastshutter.moveTo("OPEN");
		}
	}

	@Override
	public void atPointEnd() throws DeviceException {
		super.atPointEnd();
		if (fastshutter!=null) {
			fastshutter.moveTo("CLOSE");
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		if (getEventAdmin()!=null && getEventAdmin() instanceof ScriptControllerBase) {
			((ScriptControllerBase)getEventAdmin()).update(getEventAdmin(), new ScanEndEvent());
		}
		super.atScanEnd();
	}

	@Override
	public void includeEarlyFrames() throws IOException, DeviceException {
		if (isIdle()) {
			dev.getPVInteger(EARLY_FRAMES).putWait(0);
		} else {
			throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
		}
	}

	public void includeEarlyFramesExt() throws IOException, DeviceException {
		if (isIdle()) {
			final int newVal = 0;
			final int oldVal = dev.getPVInteger(EARLY_FRAMES_RBV).get();
			dev.getPVInteger(EARLY_FRAMES).putWait(newVal);
			String msg =	"Old : " + earlyFramesTranslatn.get(oldVal) + " ("+ oldVal +")\n";
			msg +=			"New : " + earlyFramesTranslatn.get(newVal) + " ("+ newVal +")";
			print(msg);
		} else {
			throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
		}
	}

	@Override
	public void excludeEarlyFrames() throws Exception {
		if (waitForIdle()) {
			dev.getPVInteger(EARLY_FRAMES).putWait(1);
		} else {
			print("excludeEarlyFrames = " + dev.getPVInteger(DETECTOR_STATE_RBV).get());
			throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
		}
	}

	public void excludeEarlyFramesExt() throws Exception {
		if (isIdle()) {
			final int newVal = 1;
			final int oldVal = dev.getPVInteger(EARLY_FRAMES_RBV).get();
			dev.getPVInteger(EARLY_FRAMES).putWait(newVal);
			String msg =	"Old : " + earlyFramesTranslatn.get(oldVal) + " ("+ oldVal +")\n";
			msg +=			"New : " + earlyFramesTranslatn.get(newVal) + " ("+ newVal +")";
			print(msg);
		} else {
			throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
		}
	}

	public int reportEarlyFrames() throws IOException {
		return dev.getPVInteger(EARLY_FRAMES_RBV).get();
	}

	public int reportEarlyFramesExt() throws IOException  {
		final int outVal = dev.getPVInteger(EARLY_FRAMES_RBV).get();
		final String msg = earlyFramesTranslatn.get(outVal) + " ("+ outVal+")";
		print(msg);
		return outVal;
	}

	@Override
	public void setBaseExposure(double expTime) throws Exception {
		if (isIdle()) {
			dev.getPVDouble(BASE_EXPOSURE).putWait(expTime);
		} else {
			throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
		}
	}

	public void setBaseExposureExt(double expTime) throws Exception {
		if (isIdle()) {
			final double oldVal = dev.getPVDouble(BASE_EXPOSURE_RBV).get();
			dev.getPVDouble(BASE_EXPOSURE).putWait(expTime);
			String msg =	"Old : " + oldVal +"\n";
			msg +=			"New : " + expTime;
			print(msg);
		} else {
			throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
		}
	}

	@Override
	public double getBaseExposure() throws Exception {
		return dev.getPVDouble(BASE_EXPOSURE_RBV).get();
	}

	@Override
	public void setBaseAcquirePeriod(double acqTime) throws Exception {
		if (isIdle()) {
			dev.getPVDouble(BASE_ACQUIRE_PERIOD).putWait(acqTime);
		} else {
			throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
		}
	}

	public void setBaseAcquirePeriodExt(double acqTime) throws Exception {
		if (isIdle()) {
			final double oldVal = dev.getPVDouble(BASE_ACQUIRE_PERIOD_RBV).get();
			dev.getPVDouble(BASE_ACQUIRE_PERIOD).putWait(acqTime);
			String msg =	"Old : " + oldVal +"\n";
			msg +=			"New : " + acqTime;
			print(msg);
		} else {
			throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
		}
	}

	@Override
	public double getBaseAcquirePeriod() throws Exception {
		return dev.getPVDouble(BASE_ACQUIRE_PERIOD_RBV).get();
	}

	@Override
	public void setExposuresPerImage(int numExp) throws Exception {
		if (isIdle()) {
			dev.getPVInteger(EXPOSURES_PER_IMAGE).putWait(numExp);
		} else {
			throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
		}
	}

	public void setExposuresPerImageExt(int numExp) throws Exception {
		if (isIdle()) {
			final int oldVal = dev.getPVInteger(EXPOSURES_PER_IMAGE_RBV).get();
			dev.getPVInteger(EXPOSURES_PER_IMAGE).putWait(numExp);
			String msg =	"Old : " + oldVal +"\n";
			msg +=			"New : " + numExp;
			print(msg);
		} else {
			throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
		}
	}

	@Override
	public int getExposuresPerImage() throws Exception {
		return dev.getPVInteger(EXPOSURES_PER_IMAGE_RBV).get();
	}

	@Override
	public void setNumImages(int numImg) throws Exception {
		if (isIdle()) {
			dev.getPVInteger(NUM_IMAGES).putWait(numImg);
		} else {
			throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
		}
	}

	public void setNumImagesExt(int numImg) throws Exception {
		if (isIdle()) {
			final int oldVal = dev.getPVInteger(NUM_IMAGES_RBV).get();
			dev.getPVInteger(NUM_IMAGES).putWait(numImg);
			String msg =	"Old : " + oldVal +"\n";
			msg +=			"New : " + numImg;
			print(msg);
		} else {
			throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
		}
	}

	@Override
	public int getNumImages() throws Exception {
		return dev.getPVInteger(NUM_IMAGES_RBV).get();
	}

	@Override
	public void setPUMode(int mode) throws Exception {
		if (allowedPUModes.contains(mode)) {
			if (isIdle()) {
				dev.getPVInteger(PU_MODE).putWait(mode);
			} else {
				throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
			}
		} else {
			String msg = "The input PU mode of " + mode + " is not allowed!\n";
			logger.error(msg);
			msg += "The allowable values are: " + allowedPUModes.toString();
			throw new IllegalArgumentException(msg);
		}
	}

	public void setPUModeExt(int mode) throws Exception {
		if (allowedPUModes.contains(mode)) {
			if (isIdle()) {
				final int oldVal = dev.getPVInteger(PU_MODE_RBV).get();
				dev.getPVInteger(PU_MODE).putWait(mode);
				String msg =	"Old : " + oldVal +"\n";
				msg +=			"New : " + mode;
				print(msg);
			} else {
				throw new DeviceException(DETECTOR_NOT_IDLE_ERROR_MESSAGE);
			}
		} else {
			String msg = "The input PU mode of " + mode + " is not allowed.\n";
			logger.error(msg);
			msg += "The allowable values are: " + allowedPUModes.toString();
			throw new IllegalArgumentException(msg);
		}
	}

	@Override
	public int getPUMode() throws Exception {
		return dev.getPVInteger(PU_MODE_RBV).get();
	}

	public void reportAllowablePUModes() {
		print(allowedPUModes.toString());
	}

	@Override
	public void calibrate() throws Exception {
		// TO-DO
		if (useShutter) {
			// close shutter
			print("About to close shutter (" + getShutterPVName() + ")...");
			final int shutterClose = getShutterIntForClose();
			getShutterPV().putWait(shutterClose);

			// wait for shutter to close
			waitForIntPVValEqualTo(getShutterPV(), shutterClose, 10);
			print("Shutter closed at Shutter State: " + getShutterPV().get() + " ("+ getShutterPVName() + ")");

		} else {
			print("The use of shutter was not requested for this calibration!");
		}

		print("\t at Shutter State: " + getShutterPV().get() + " ("+ getShutterPVName() + ")");
		print("\t at Detector State: " + dev.getPVInteger(DETECTOR_STATE_RBV).get());

		// stop detector acquire
		print("About to stop acquire 2...");
//		dev.getPVInteger(ACQUIRE).putWait(0);
		dev.getPVInteger(ACQUIRE).putNoWait(0);

		// wait for detector to stop acquire
		waitForIntPVValEqualTo(dev.getPVInteger(ACQUIRE), 0, 10);
		print("Acquire stopped at Acquire State: " + dev.getPVInteger(ACQUIRE).get());
		waitForIntPVValEqualTo(dev.getPVInteger(DETECTOR_STATE_RBV), 0, 60*2.0);

		print("\t at Acquire State: " + dev.getPVInteger(ACQUIRE).get());
		print("\t at Detector State: " + dev.getPVInteger(DETECTOR_STATE_RBV).get());

		// calibrate
		print("About to calibrate...");
		dev.getPVInteger(CALIBRATE).putWait(1,60.0*3.5);

		// wait for calibration to be running
		//waitForIntPVValNotEqualTo(dev.getPVInteger(CALIBRATE_RBV), valBeforeCalibrate, 30);
		print("Calibrating started at Calibration-Running State: " + dev.getPVInteger(CALIBRATE_RBV).get());

		// wait for calibration to end
		waitForIntPVValEqualTo(dev.getPVInteger(CALIBRATE_RBV), 0, 30);
		print("Calibrating ended at Calibration-Running State: " + dev.getPVInteger(CALIBRATE_RBV).get());

		print("\t at Calibration-Running State: " + dev.getPVInteger(CALIBRATE_RBV).get());
		print("\t at Detector State: " + dev.getPVInteger(DETECTOR_STATE_RBV).get());

		final double baseExposure = getBaseExposure();
		final double baseAcquirePeriod = getBaseAcquirePeriod();

		String msg = "\n";
		msg += "Detector calibrated to:";
		msg += "\n Base Exposure: " + baseExposure;
		msg += "\n Base Acquire Period: " + baseAcquirePeriod;
		msg += "\n Shutter State: " + getShutterPV().get() + " ("+ getShutterPVName() + ")";
		print(msg);
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

	public void waitForShutterPos(int shutterPosWaitedFor, double timeoutSec) throws DeviceException {
		if( shutterPV == null ){
			shutterPV = LazyPVFactory.newIntegerPV(shutterPVName);
		}
		try {
			shutterPV.setValueMonitoring(true);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
		logger.info("Waiting for shutter {} to close...", shutterPVName);
		try {
			shutterPV.waitForValue(new EqualTo(shutterPosWaitedFor) {
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

	private class EqualTo implements Predicate<Integer> {

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

	private class NotEqualTo implements Predicate<Integer> {

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

	@Override
	public void acquire(double collectionTime, int numImages) throws Exception {
		// TO-DO
		if (numImages <= 0) {
			throw new IllegalArgumentException("The input value of numImages must be a positive integer.");
		}

		final ScanInformation scanInfo_IGNORED = null;
		final PixiumSimpleAcquire psa = (PixiumSimpleAcquire)this.getCollectionStrategy();
		psa.prepareForCollection(collectionTime, numImages, scanInfo_IGNORED);
		psa.collectData();
		psa.waitWhileBusy();
	}

	@Override
	public void acquire(double collectionTime) throws Exception {
		// TO-DO
		if (collectionTime <= 0) {
			throw new IllegalArgumentException("The input value of collectionTime must be a positive number.");
		}

		int numImages = Integer.MAX_VALUE;
		numImages = 12; //for testing
		final ScanInformation scanInfo_IGNORED = null;
		final int numberImagesPerCollection_IGNORED = -1;
		final PixiumSimpleAcquire psa = (PixiumSimpleAcquire)this.getCollectionStrategy();
		psa.prepareForCollection(collectionTime, numberImagesPerCollection_IGNORED, scanInfo_IGNORED);
		final ConcurrentScan scan = RepeatScan.create_repscan(numImages, this);
		scan.runScan();
	}

	@Override
	public void stop() throws DeviceException {
		super.stop();
		try {
			stopAcquiring();
		} catch (Exception e) {
			throw new DeviceException("Failed to stop Pixium detector", e);
		}
	}

	public void stopAcquiring() throws Exception {
		final AbstractADTriggeringStrategy ats = (AbstractADTriggeringStrategy)this.getCollectionStrategy();
		ats.getAdBase().stopAcquiring();
	}

	public void setWindowsSubString(String windowsSubString) {
		UnixToWindowsFilePathConverter filePathConverter = null;
		for (NXPluginBase plugin : getAdditionalPluginList()) {
			filePathConverter = null;
			if (plugin instanceof SingleImagePerFileWriter){
				final SingleImagePerFileWriter fileWriter = (SingleImagePerFileWriter)plugin;
				final NDFileImpl ndFile = (NDFileImpl)fileWriter.getNdFile();
				filePathConverter = (UnixToWindowsFilePathConverter)(ndFile.getFilePathConverter());
			} else if (plugin instanceof MultipleImagesPerHDF5FileWriter) {
				final MultipleImagesPerHDF5FileWriter fileWriter = (MultipleImagesPerHDF5FileWriter)plugin;
				final NDFileImpl ndFile = (NDFileImpl)fileWriter.getNdFile();
				filePathConverter = (UnixToWindowsFilePathConverter)(ndFile.getFilePathConverter());
			}
			if (filePathConverter != null) {
				final String oldVal = filePathConverter.getWindowsSubString();
				filePathConverter.setWindowsSubString(windowsSubString);
				String msg =	"Old : " + oldVal +"\n";
				msg +=			"New : " + windowsSubString;
				print(msg);
			}
		}

	}

	public String[] getWindowsSubString() {
		final String[] windowsSubString = new String[getAdditionalPluginList().size()];
		UnixToWindowsFilePathConverter filePathConverter = null;
		int i = 0;
		for (NXPluginBase plugin : getAdditionalPluginList()) {
			filePathConverter = null;
			if (plugin instanceof SingleImagePerFileWriter){
				final SingleImagePerFileWriter fileWriter = (SingleImagePerFileWriter)plugin;
				final NDFileImpl ndFile = (NDFileImpl)fileWriter.getNdFile();
				filePathConverter = (UnixToWindowsFilePathConverter)(ndFile.getFilePathConverter());
			} else if (plugin instanceof MultipleImagesPerHDF5FileWriter) {
				final MultipleImagesPerHDF5FileWriter fileWriter = (MultipleImagesPerHDF5FileWriter)plugin;
				final NDFileImpl ndFile = (NDFileImpl)fileWriter.getNdFile();
				filePathConverter = (UnixToWindowsFilePathConverter)(ndFile.getFilePathConverter());
			}

			if (filePathConverter != null) {
				windowsSubString[i] = filePathConverter.getWindowsSubString();
			} else {
				windowsSubString[i] = "undefined";
			}
			i += 1;
		}
		return windowsSubString;
	}

	public Scannable getFastshutter() {
		return fastshutter;
	}

	public void setFastshutter(Scannable fastshutter) {
		this.fastshutter = fastshutter;
	}

	private boolean isIdle() throws IOException {
		return dev.getPVInteger(DETECTOR_STATE_RBV).get()==0;
	}

	private boolean waitForIdle() throws Exception  {
		final int totMillis = 60 * 1000 * 10;
		final int grain = 25;
		for (int i = 0; i < totMillis/grain; i++) {
			if (isIdle()) return true;
			Thread.sleep(grain);
		}
		throw new TimeoutException("Timeout waiting for detector to be in the Idle state.");
	}

	public Scriptcontroller getEventAdmin() {
		return eventAdmin;
	}

	public void setEventAdmin(Scriptcontroller eventAdmin) {
		this.eventAdmin = eventAdmin;
	}
}