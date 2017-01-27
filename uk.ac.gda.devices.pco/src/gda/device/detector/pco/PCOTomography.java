/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.pco;

import java.awt.Point;
import java.awt.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.IPCOControllerV17;
import gda.device.detector.IPCODetector;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.FfmpegStream;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.NDROI;
import gda.device.detector.areadetector.v17.NDStats;
import gda.factory.Findable;
import uk.ac.gda.devices.pco.LiveModeUtil;
import uk.ac.gda.tomography.devices.ITomographyDetector;

/**
 * Tomography implementation for the PCO camera.
 */
public class PCOTomography implements ITomographyDetector, Findable {
	private static final double PRC_MIN_TIME = 0.4;

	private String name;

	private static final Logger logger = LoggerFactory.getLogger(PCOTomography.class);

	private IPCODetector pcoDetector;

	public IPCODetector getPcoDetector() {
		return pcoDetector;
	}

	public void setPcoDetector(IPCODetector pcoDetector) {
		this.pcoDetector = pcoDetector;
	}

	@Override
	public void setExposureTime(double collectionTime) throws Exception {
		pcoDetector.getController().getAreaDetector().setAcquireTime(collectionTime);
		pcoDetector.getController().getAreaDetector().setAcquirePeriod(0); //required for new IOC
	}

	private double maxIntensity = 65000;

	@Override
	public void acquireMJpeg(Double expTime, int binX, int binY, Double scale, Double offset) throws Exception {

		// plugins arranged - cam -> proc -> roi ->mjpeg
		final IPCOControllerV17 controller = pcoDetector.getController();

		final ADBase areaDetector = controller.getAreaDetector();
		//
		pcoDetector.getController().disarmCamera();
		//
		final NDStats stat = controller.getStat();
		final NDProcess proc1 = controller.getProc1();
		final NDROI roi1 = controller.getRoi1();
		final FfmpegStream mJpeg1 = controller.getMJpeg1();
		final NDFile tiff = controller.getTiff();

		controller.setImageMode(ImageMode.CONTINUOUS.ordinal());

		areaDetector.setTriggerMode(2);
		setExposureTime(expTime);
		// enabling stat
		stat.getPluginBase().enableCallbacks();
		stat.setComputeStatistics(1);
		final NDPluginBase proc1PluginBase = proc1.getPluginBase();
		stat.getPluginBase().setNDArrayPort(proc1PluginBase.getPortName_RBV());
		// Setting default values so the MJPeg viewer can cope with the image streaming.
		//
		proc1PluginBase.setNDArrayPort(areaDetector.getPortName_RBV());
		proc1PluginBase.enableCallbacks();
		proc1PluginBase.setMinCallbackTime(PRC_MIN_TIME);
		proc1.setEnableFilter(0);
		proc1.setEnableOffsetScale(1);

		proc1.setScale(scale);
		proc1.setOffset(offset);
		//
		roi1.getPluginBase().enableCallbacks();
		roi1.getPluginBase().setNDArrayPort(proc1PluginBase.getPortName_RBV());

		roi1.setSizeX(areaDetector.getArraySizeX_RBV());
		roi1.setSizeY(areaDetector.getArraySizeY_RBV());

		roi1.setBinX(binX);
		roi1.setBinY(binY);

		roi1.enableScaling();
		roi1.setScale(binX * binY);

		roi1.enableX();
		roi1.enableY();

		//
		mJpeg1.getPluginBase().enableCallbacks();
		mJpeg1.getPluginBase().setNDArrayPort(roi1.getPluginBase().getPortName_RBV());

		/* Disable tiff */
		tiff.getPluginBase().disableCallbacks();

		controller.acquire();
	}

	@Override
	public void resumeAcquisition() throws Exception {
		final IPCOControllerV17 controller = pcoDetector.getController();
		controller.acquire();
	}

	@Override
	public void setZoomRoiStart(Point roiStart) throws Exception {
		final NDROI roi2 = pcoDetector.getController().getRoi2();
		roi2.enableX();
		roi2.enableY();
		roi2.setMinX(roiStart.x);
		roi2.setMinY(roiStart.y);
	}

	@Override
	public void setupZoomMJpeg(Rectangle roi, Point bin) throws Exception {
		logger.info("zoom - roisize-" + roi + " - bin - " + bin);
		final IPCOControllerV17 controller = pcoDetector.getController();
		final NDROI roi2 = controller.getRoi2();
		final ADBase areaDetector = controller.getAreaDetector();
		final NDProcess proc2 = controller.getProc2();
		final FfmpegStream mJpeg2 = controller.getMJpeg2();

		roi2.getPluginBase().enableCallbacks();
		roi2.getPluginBase().setNDArrayPort(areaDetector.getPortName_RBV());

		roi2.enableX();
		roi2.enableY();

		roi2.setBinX(bin.x);
		roi2.setBinY(bin.y);

		roi2.setMinX(roi.x);
		roi2.setMinY(roi.y);
		roi2.setSizeX(roi.width);
		roi2.setSizeY(roi.height);

		final NDPluginBase proc2PluginBase = proc2.getPluginBase();
		proc2PluginBase.setNDArrayPort(roi2.getPluginBase().getPortName_RBV());
		proc2PluginBase.enableCallbacks();
		proc2PluginBase.setMinCallbackTime(PRC_MIN_TIME);

		final NDPluginBase mjpeg2PluginBase = mJpeg2.getPluginBase();
		mjpeg2PluginBase.setNDArrayPort(proc2PluginBase.getPortName_RBV());
		mjpeg2PluginBase.enableCallbacks();
	}

	@Override
	public Integer getRoi1BinX() throws Exception {
		return pcoDetector.getController().getRoi1().getBinX();
	}

	@Override
	public Integer getRoi2BinX() throws Exception {
		return pcoDetector.getController().getRoi2().getBinX();
	}

	@Override
	public void enableFlatField() throws Exception {
		final IPCOControllerV17 controller = pcoDetector.getController();
		final NDProcess proc1 = controller.getProc1();
		final NDProcess proc2 = controller.getProc2();
		proc1.setEnableFlatField(1);
		proc2.setEnableFlatField(1);
	}

	@Override
	public void disableFlatField() throws Exception {
		final IPCOControllerV17 controller = pcoDetector.getController();
		final NDProcess proc1 = controller.getProc1();
		final NDProcess proc2 = controller.getProc2();
		proc1.setEnableFlatField(0);
		proc2.setEnableFlatField(0);

	}

	@Override
	public String getTiffFilePath() throws Exception {
		return pcoDetector.getController().getTiff().getFilePath_RBV();
	}

	@Override
	public String getTiffFileName() throws Exception {
		return pcoDetector.getController().getTiff().getFileName_RBV();
	}

	@Override
	public String getTiffFileTemplate() throws Exception {
		return pcoDetector.getController().getTiff().getFileTemplate_RBV();
	}

	@Override
	public void setTiffFileNumber(int fileNumber) throws Exception {
		pcoDetector.getController().getTiff().setFileNumber(fileNumber);
	}

	@Override
	public String demandRaw(Double acqTime, String demandRawFilePath, String demandRawFileName, Boolean isHdf,
			Boolean isFlatFieldCorrectionRequired, Boolean demandWhileStreaming) throws Exception {
		final IPCOControllerV17 controller = pcoDetector.getController();

		// Ensure disarm the camera before any change
		if (!demandWhileStreaming) {
			if (LiveModeUtil.isLiveMode()) {
				if (controller.isArmed()) {
					controller.disarmCamera();
				}
			}
			else {
				LoggerFactory.getLogger("PCOTomography:"+this.getName()).info("demandRaw: Not live!");
			}
		}
		// Expectation is that this called only after the camera has issued a resetAll command.
		// set file template to
		if (isHdf) {
			setHdfFormat(true);
			// FIXME - program so that HDF can be used.

		} else {
			final boolean isHdfFormat = isHdfFormat();
			setHdfFormat(false);
			// if it isn't hdf the assumption is it should be tiff.
			// Demand raw
			// Stop tiff capture
			// stop tiff capture
			try {
				final NDFile tiff = controller.getTiff();
				final ADBase areaDetector = controller.getAreaDetector();
				final String areadDetectorPortName = areaDetector.getPortName_RBV();

				final NDStats ndStat = controller.getStat();
				ndStat.getPluginBase().enableCallbacks();
				ndStat.setComputeStatistics(1);
				tiff.getPluginBase().enableCallbacks();
				tiff.getPluginBase().setBlockingCallbacks(1);

				tiff.stopCapture();
				// set num capture to 1
				tiff.setNumCapture(1);
				// set tiff capture mode to 'Single'
				tiff.setFileWriteMode(2);
				// set file path to demandRawFilePath
				tiff.resetFileTemplate();
				tiff.getPluginBase().setBlockingCallbacks(0);
				// set file name to demandRawFileName
				tiff.setFileName(demandRawFileName);
				// reset file number to 0
				// tiff.setFileNumber(0);
				// set auto-increment to No
				tiff.setAutoIncrement(1);
				// set file format to 'tif'
				tiff.setFileFormat(0);

				pcoDetector.setTiffFilePathBasedOnIocOS(demandRawFilePath);

				if (!demandWhileStreaming) {
					// set file template
					areaDetector.stopAcquiring(); //stop so that subsequent start works
					areaDetector.setImageMode(0);
					areaDetector.setTriggerMode(2);
					// set num image to 1
					areaDetector.setNumImages(1);

					areaDetector.setAcquireTime(acqTime);
					areaDetector.setAcquirePeriod(0.);
				}
				if (isFlatFieldCorrectionRequired) {

					/* Demand raw goes through proc so that flat field can be applied */
					final NDProcess proc1 = controller.getProc1();
					final NDProcess proc2 = controller.getProc2();
					proc1.setEnableLowClip(0);
					proc1.setEnableHighClip(0);

					//
					proc1.setEnableFilter(0);
					proc2.setEnableFilter(0);
					/**/
					proc2.setEnableLowClip(0);
					proc2.setEnableHighClip(0);

					// Set up Proc1

					proc1.setEnableOffsetScale(0);
					proc2.setEnableOffsetScale(0);

					proc1.getPluginBase().enableCallbacks();
					proc1.getPluginBase().setNDArrayPort(areadDetectorPortName);
					// to synchronise acquisition chain along all the plugins
					proc1.getPluginBase().setBlockingCallbacks(1);

					proc1.getPluginBase().setBlockingCallbacks(0);
					ndStat.getPluginBase().setNDArrayPort(proc1.getPluginBase().getPortName_RBV());
					tiff.getPluginBase().setNDArrayPort(proc1.getPluginBase().getPortName_RBV());
				} else {
					ndStat.getPluginBase().setNDArrayPort(areadDetectorPortName);
					tiff.getPluginBase().setNDArrayPort(areadDetectorPortName);
				}
				if (!demandWhileStreaming) {
					tiff.startCapture();
					// must wait for acquire and write into file finish
					pcoDetector.acquireSynchronously();
				} else {
					tiff.startCaptureSynchronously();
				}

				// to remove synchronised acquisition chain along all the plugins
			} finally {
				setHdfFormat(isHdfFormat);
			}

		}
		return getTiffImageFileName();
	}

	private void prepareProcForFlat(NDProcess proc, int numberOfImages) throws Exception {
		final IPCOControllerV17 controller = pcoDetector.getController();
		proc.getPluginBase().setNDArrayPort(controller.getAreaDetector().getPortName_RBV());
		proc.getPluginBase().enableCallbacks();

		// Enable recursive filter
		proc.setEnableFilter(1);
		// set 'Filter type:' value to "RecursiveAverage"
		proc.setFilterType(0);
		// set number filter - to number of images
		proc.setNumFilter(numberOfImages);
		// click the reset button
		proc.setResetFilter(1);
		// Disable Scale and Offset
		proc.setEnableOffsetScale(0);
		// disable flat field
		proc.setEnableFlatField(0);
		proc.setScaleFlatField(getMaxIntensity());
		// Necessary, if this is not set it does not provide right image.
		proc.setOScale(1.0);
		proc.setOOffset(0);
		proc.setFScale(1.0);
		proc.setFOffset(0);
		proc.setFilterCallbacks(1);

		proc.setEnableOffsetScale(0);
		proc.setEnableHighClip(0);
		proc.setEnableLowClip(0);
		proc.setEnableBackground(1);
	}

	@Override
	public String takeFlat(double acqTime, int numberOfImages, String fileLocation, String fileName,
			String filePathTemplate) throws Exception {
		final IPCOControllerV17 controller = pcoDetector.getController();
		String fullFileName = null;
		final boolean isHdfFormat = isHdfFormat();
		setHdfFormat(false);
		logger.info("{} starts to collect {} averaged flat field, please wait...", pcoDetector.getName(),
				numberOfImages);
		final ADBase areaDetector = controller.getAreaDetector();
		areaDetector.setAcquireTime(acqTime);
		final NDProcess proc1 = controller.getProc1();
		prepareProcForFlat(proc1, numberOfImages);

		// Proc2
		final NDProcess proc2 = controller.getProc2();
		prepareProcForFlat(proc2, numberOfImages);
		// capture the appropriate number of images
		controller.setImageMode(ImageMode.CONTINUOUS.ordinal());
		//
		final NDFile tiff = controller.getTiff();
		tiff.getPluginBase().enableCallbacks();
		tiff.stopCapture();

		// save the flat field - so that 'disabled' is considered
		proc1.setSaveFlatField(1);
		proc2.setSaveFlatField(1);
		//
		// on tiff - set the file path, file name, file template and set the 'tiff' to listen to 'proc1'
		pcoDetector.setTiffFilePathBasedOnIocOS(fileLocation);

		tiff.setFileName(fileName);
		tiff.setFileTemplate(filePathTemplate);
		tiff.getPluginBase().setNDArrayPort(proc1.getPluginBase().getPortName_RBV());
		tiff.setNumCapture(1);
		tiff.setFileWriteMode(2);
		tiff.startCapture();

		//
		pcoDetector.getController().acquire();

		while (tiff.getStatus() != Detector.IDLE) {
			Thread.sleep(200);
		}

		//
		proc1.setEnableFilter(0);
		proc2.setEnableFilter(0);
		proc1.setEnableFlatField(0);
		proc2.setEnableFlatField(0);
		//
		proc1.setEnableBackground(0);
		proc2.setEnableBackground(0);
		fullFileName = controller.getTiffFullFileName();
		setHdfFormat(isHdfFormat);
		return fullFileName;
	}

	@Override
	public String getTiffImageFileName() throws Exception {
		return pcoDetector.getTiffImageFileName();
	}

	private void prepareProcForDark(NDProcess proc, int numberOfImages) throws Exception {
		final IPCOControllerV17 controller = pcoDetector.getController();
		// set Proc1 array port to Roi1 and enable callback
		final NDPluginBase pluginBase = proc.getPluginBase();

		pluginBase.setNDArrayPort(controller.getAreaDetector().getPortName_RBV());
		pluginBase.enableCallbacks();
		// Setting min time on prc to avoid dropping frames.
		pluginBase.setMinCallbackTime(PRC_MIN_TIME);

		proc.setEnableFlatField(0);
		// Enable recursive filter
		proc.setEnableFilter(1);
		// set 'Filter type:' value to "RecursiveAverage
		proc.setFilterType(0);
		// set number filter - to number of images
		proc.setNumFilter(numberOfImages);
		// click the reset button
		proc.setResetFilter(1);
		// Necessary, if this is not set it does not provide right image.
		proc.setOScale(1.0);
		proc.setOOffset(0);
		proc.setFScale(1.0);
		proc.setFOffset(0);
		// disable background subtraction
		proc.setEnableBackground(0);
		proc.setFilterCallbacks(1);
		proc.setEnableOffsetScale(0);
		proc.setEnableHighClip(0);
		proc.setEnableLowClip(0);
	}

	@Override
	public String takeDark(int numberOfImages, double acqTime, String fileLocation, String fileName,
			String filePathTemplate) throws Exception {

		String fullFileName = null;
		final boolean isHdfFormat = isHdfFormat();
		setHdfFormat(false);
		final IPCOControllerV17 controller = pcoDetector.getController();
		controller.getAreaDetector().setImageMode(ImageMode.CONTINUOUS.ordinal());
		logger.info("{} starts to collect {} averaged flat field, please wait...", pcoDetector.getName(),
				numberOfImages);

		final ADBase areaDetector = controller.getAreaDetector();
		areaDetector.setAcquireTime(acqTime);

		// Proc1
		final NDProcess proc1 = controller.getProc1();
		prepareProcForDark(proc1, numberOfImages);

		// Proc2
		final NDProcess proc2 = controller.getProc2();
		prepareProcForDark(proc2, numberOfImages);

		//
		final NDFile tiff = controller.getTiff();
		tiff.getPluginBase().enableCallbacks();
		tiff.stopCapture();

		// save the flat field - so that 'disabled' is considered
		proc1.setSaveFlatField(1);
		proc2.setSaveFlatField(1);
		//
		// on tiff - set the file path, file name, file template and set the 'tiff' to listen to 'proc1'
		pcoDetector.setTiffFilePathBasedOnIocOS(fileLocation);
		tiff.setFileName(fileName);
		tiff.setFileTemplate(filePathTemplate);
		tiff.getPluginBase().setNDArrayPort(proc1.getPluginBase().getPortName_RBV());
		tiff.setNumCapture(1);
		tiff.setFileWriteMode(2);
		tiff.startCapture();

		// save background subtraction for both procs - to memory
		proc1.setSaveBackground(1);
		proc2.setSaveBackground(1);
		//
		pcoDetector.getController().acquire();

		while (tiff.getStatus() != Detector.IDLE) {
			Thread.sleep(200);
		}

		// disable recursive filter for both procs
		proc1.setEnableFilter(0);
		proc2.setEnableFilter(0);

		proc1.setEnableOffsetScale(0);
		proc2.setEnableOffsetScale(0);

		fullFileName = controller.getTiffFullFileName();
		setHdfFormat(isHdfFormat);
		return fullFileName;
	}

	@Override
	public void abort() throws Exception {
		pcoDetector.stop();
		pcoDetector.stopCapture();
		pcoDetector.getController().disarmCamera();
	}

	@Override
	public void setHdfFormat(boolean hdfFormat) {
		pcoDetector.setHdfFormat(hdfFormat);
	}

	@Override
	public void resetFileFormat() throws Exception {
		pcoDetector.getController().getTiff().resetFileTemplate();
	}

	@Override
	public boolean isHdfFormat() {
		return pcoDetector.isHdfFormat();
	}

	@Override
	public void resetAll() throws Exception {
		// Ensure that the camera is stopped before resetAll is called
		pcoDetector.getController().disarmCamera();
		pcoDetector.resetAll();
		// HDF plugin is not used for the tomography alignment.
		final NDFileHDF5 hdf = pcoDetector.getController().getHdf();
		if (hdf != null) {
			hdf.getFile().getPluginBase().disableCallbacks();
		}
	}

	@Override
	public void setupForTilt(int minY, int maxY, int minX, int maxX) throws Exception {
		final IPCOControllerV17 controller = pcoDetector.getController();
		final ADBase areaDetector = controller.getAreaDetector();
		// Adding 1 as the detector starts counting pixels beginning from 1
		final NDROI roi2 = controller.getRoi2();
		roi2.setSizeY(maxY - minY + 1);
		roi2.setMinY(minY);

		roi2.setSizeX(maxX - minX + 1);
		roi2.setMinX(minX);

		roi2.getPluginBase().enableCallbacks();

		roi2.getPluginBase().setNDArrayPort(areaDetector.getPortName_RBV());

		controller.getTiff().getPluginBase().setNDArrayPort(roi2.getPluginBase().getPortName_RBV());

		controller.getTiff().getPluginBase().enableCallbacks();

	}

	@Override
	public void resetAfterTiltToInitialValues() throws Exception {
		final IPCOControllerV17 controller = pcoDetector.getController();
		controller.getTiff().getPluginBase().setNDArrayPort(controller.getAreaDetector().getPortName_RBV());
		controller.getTiff().getPluginBase().disableCallbacks();
		// ADBase adBase = pcoDetector.getController().getAreaDetector();
		// adBase.setMinY(adBase.getInitialMinY());
		// adBase.setSizeY(adBase.getInitialSizeY());
		// adBase.setMinX(adBase.getInitialMinX());
		// adBase.setSizeX(adBase.getInitialSizeX());
	}

	@Override
	public void setProcScale(double factor) throws Exception {
		final IPCOControllerV17 controller = pcoDetector.getController();
		controller.getProc1().setEnableOffsetScale(1);
		controller.getProc1().setScale(factor);
		controller.getProc2().setEnableOffsetScale(1);
		controller.getProc2().setScale(factor);
	}

	private void setProcOffset(double offset) throws Exception {
		final IPCOControllerV17 controller = pcoDetector.getController();
		controller.getProc1().setEnableOffsetScale(1);
		controller.getProc1().setOffset(offset);
		controller.getProc2().setEnableOffsetScale(1);
		controller.getProc2().setOffset(offset);
	}

	@Override
	public void setRoi1ScalingDivisor(double divisor) throws Exception {
		final IPCOControllerV17 controller = pcoDetector.getController();
		controller.getRoi1().enableScaling();
		controller.getRoi1().setScale(divisor);
	}

	public double getMaxIntensity() {
		return maxIntensity;
	}

	public void setMaxIntensity(double maxIntensity) {
		this.maxIntensity = maxIntensity;
	}

	@Override
	public String getDetectorName() {
		return pcoDetector.getName();
	}

	@Override
	public boolean isAcquiring() throws DeviceException {
		try {
			return pcoDetector.getController().getAreaDetector().getAcquireState() == 1;
		} catch (Exception e) {
			throw new DeviceException(e);
		}
		// return pcoDetector.isBusy();
	}

	@Override
	public void setExternalTriggered(Boolean val) {
		pcoDetector.setExternalTriggered(val);
	}

	@Override
	public void initDetector() throws Exception {
		// set the image model to single
		final IPCOControllerV17 controller = pcoDetector.getController();
		controller.setImageMode(0);
		// setting the ADC model to 2-ADC mode
		pcoDetector.setADCMode(1);

		enableLowHighClip();

		// acquire a single image to set the arrays correctly
		pcoDetector.acquireSynchronously();
	}

	private void enableLowHighClip() throws Exception {
		final IPCOControllerV17 controller = pcoDetector.getController();
		final NDProcess proc1 = controller.getProc1();
		proc1.setEnableHighClip(1);
		proc1.setHighClip(65534);
		//
		final NDProcess proc2 = controller.getProc2();
		proc2.setEnableHighClip(1);
		proc2.setHighClip(65534);

		proc1.setEnableLowClip(1);
		proc1.setLowClip(0);
		//
		proc2.setEnableLowClip(1);
		proc2.setLowClip(0);
	}

	@Override
	public void disableDarkSubtraction() throws Exception {
		pcoDetector.getController().getProc1().setEnableBackground(0);
		pcoDetector.getController().getProc2().setEnableBackground(0);
	}

	@Override
	public void enableDarkSubtraction() throws Exception {
		pcoDetector.getController().getProc1().setEnableBackground(1);
		pcoDetector.getController().getProc2().setEnableBackground(1);
	}

	@Override
	public double getProc1Scale() throws Exception {
		return pcoDetector.getController().getProc1().getScale();
	}

	@Override
	public void setProc1Scale(double newScale) throws Exception {
		final NDProcess proc1 = pcoDetector.getController().getProc1();

		proc1.setEnableOffsetScale(1);
		logger.debug("Setting new scale:{}", newScale);

		proc1.setScale(newScale);
	}

	@Override
	public void setOffsetAndScale(double offset, double scale) throws Exception {
		enableLowHighClip();
		setProcScale(scale);
		setProcOffset(offset);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setupHistoStatCollection(int binSize) throws Exception {
		NDStats stat = pcoDetector.getController().getStat();
		stat.getPluginBase().enableCallbacks();
		stat.setComputeHistogram(1);
		stat.setHistSize(binSize);
	}
}
