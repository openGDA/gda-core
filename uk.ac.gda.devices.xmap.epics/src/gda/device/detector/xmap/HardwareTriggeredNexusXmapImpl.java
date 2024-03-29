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

package gda.device.detector.xmap;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.NumTracker;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetectorBase;
import gda.device.detector.xmap.edxd.EDXDController.COLLECTION_MODES;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.device.scannable.PositionStreamIndexer;
import gda.jython.InterfaceProvider;

public class HardwareTriggeredNexusXmapImpl extends HardwareTriggerableDetectorBase implements
		HardwareTriggeredNexusXmap {
	static final Logger logger = LoggerFactory.getLogger(HardwareTriggeredNexusXmapImpl.class);
	private boolean slave = true;
	private NexusXmap xmap;
	private EDXDMappingController controller;
	private boolean integrateBetweenPoints = true;
	private int lastScanNumber = 0;
	private String[] cachedExtraNames;
	private String[] cachedOutputFormat;
	private int lastRowNumber = -1;

	private PositionStreamIndexer<NexusTreeProvider> indexer;
	private int scanNumberOfPoints;
	private boolean armedForNewScan;

	@Override
	public NexusXmap getXmap() {
		return xmap;
	}

	public void setXmap(NexusXmap xmap) {
		this.xmap = xmap;
	}

	public EDXDMappingController getController() {
		return controller;
	}

	public void setController(EDXDMappingController controller) {
		this.controller = controller;
	}

	public int getScanNumberOfPoints() {
		return scanNumberOfPoints;
	}

	public void setScanNumberOfPoints(int scanNumberOfPoints) {
		this.scanNumberOfPoints = scanNumberOfPoints;
	}

	@Override
	public void configure(){
		if (isConfigured()) {
			return;
		}
		// put any code required to configure device here
		setConfigured(true);
	}

	@Override
	public void clear() throws DeviceException {
		xmap.clear();
	}

	@Override
	public void clearAndStart() throws DeviceException {
		xmap.clearAndStart();
	}

	@Override
	public double getAcquisitionTime() throws DeviceException {
		return xmap.getAcquisitionTime();
	}

	@Override
	public List<String> getChannelLabels() {
		return xmap.getChannelLabels();
	}

	@Override
	public int[] getData(int mcaNumber) throws DeviceException {
		return xmap.getData(mcaNumber);
	}

	@Override
	public int[][] getData() throws DeviceException {
		return xmap.getData();
	}

	@Override
	public int getNumberOfBins() throws DeviceException {
		return xmap.getNumberOfBins();
	}

	@Override
	public int getNumberOfMca() throws DeviceException {
		return xmap.getNumberOfMca();
	}

	@Override
	public int getNumberOfROIs() throws DeviceException {
		return xmap.getNumberOfROIs();
	}

	@Override
	public double[] getROICounts(int iRoi) throws DeviceException {
		return xmap.getROICounts(iRoi);
	}

	@Override
	public double[] getROIsSum() throws DeviceException {
		// TODO Auto-generated method stub
		return xmap.getROIsSum();
	}

	@Override
	public double getReadRate() throws DeviceException {
		// TODO Auto-generated method stub
		return xmap.getReadRate();
	}

	@Override
	public double getRealTime() throws DeviceException {
		// TODO Auto-generated method stub
		return xmap.getRealTime();
	}

	@Override
	public int getStatus() throws DeviceException {
		return xmap.getStatus();
	}

	@Override
	public double getStatusRate() throws DeviceException {
		// TODO Auto-generated method stub
		return xmap.getStatusRate();
	}

	@Override
	public double readoutScalerData() throws DeviceException {
		// TODO Auto-generated method stub
		return xmap.readoutScalerData();
	}

	@Override
	public void setAcquisitionTime(double time) throws DeviceException {
		xmap.setAcquisitionTime(time);
	}

	@Override
	public void setNthROI(double[][] rois, int roiIndex) throws DeviceException {
		xmap.setNthROI(rois, roiIndex);
	}

	@Override
	public void setNumberOfBins(int numberOfBins) throws DeviceException {
		xmap.setNumberOfBins(numberOfBins);

	}

	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		xmap.setROIs(rois);
	}

	@Override
	public void setReadRate(double readRate) throws DeviceException {
		xmap.setReadRate(readRate);
	}

	@Override
	public void setStatusRate(double statusRate) throws DeviceException {
		xmap.setStatusRate(statusRate);
	}

	@Override
	public String[] getExtraNames() {
		if (armedForNewScan) {
			if (cachedExtraNames == null) {
				cachedExtraNames = xmap.getExtraNames();
			}
			return cachedExtraNames;
		}
		return xmap.getExtraNames();
	}

	@Override
	public String[] getInputNames() {
		return xmap.getInputNames();
	}

	@Override
	public String[] getOutputFormat() {
		if (armedForNewScan) {
			if (cachedOutputFormat == null)
				cachedOutputFormat = getOutputFormatFromSuper();
			return cachedOutputFormat;
		}

		return getOutputFormatFromSuper();
	}

	private String[] getOutputFormatFromSuper() {
		int inputNamesLength = xmap.getInputNames().length;
		int extraNamesLength = xmap.getExtraNames().length;
		String[] outputFormat = new String[inputNamesLength + extraNamesLength];
		String[] currentOF = xmap.getOutputFormat();
		for (int i = 0; i < outputFormat.length; i++) {
			outputFormat[i] = currentOF[0];
		}
		return outputFormat;
	}

	@Override
	public void start() throws DeviceException {
		xmap.start();
	}

	@Override
	public void collectData() throws DeviceException {
		if (!isHardwareTriggering()) {
			xmap.collectData();
		} else {
			arm();
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Hardware Triggered Xmap detector";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		if (!isHardwareTriggering()) {
			xmap.readout();
		}
		throw new DeviceException("Can only be used in continuous scans");
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		if (!isHardwareTriggering()) {
			xmap.prepareForCollection();
		}
	}

	private void arm() throws DeviceException {
		try {
			controller.startRecording();
			this.clearAndStart();
			armedForNewScan = false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			armedForNewScan = false;
			logger.error("Error occurred arming the xmap detector", e);
			throw new DeviceException("Error occurred arming the xmap detector", e);

		}
	}

	@Override
	public void stop() throws DeviceException {
		atScanEnd();
	}

	private void setupFileAttributes() throws Exception {

		// filename prefix
		String beamline = GDAMetadataProvider.getInstance().getMetadataValue("instrument", "gda.instrument", "base");
		controller.setFilenamePrefix(beamline);

		// scan number
		NumTracker runNumber = new NumTracker("tmp");
		// Get the current number
		Number scanNumber = runNumber.getCurrentFileNumber();
		if (!(scanNumber.intValue() == lastScanNumber))
			lastRowNumber = -1;
		lastScanNumber = scanNumber.intValue();
		controller.setFileNumber(scanNumber);

		// row number
		lastRowNumber++;
		controller.setFilenamePostfix(lastRowNumber + "-" + getName());

		// set the sub-directory and create if necessary
		String dataDir = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		dataDir = dataDir + "tmp" + File.separator + lastScanNumber;
		if (!(new File(dataDir)).exists()) {
			boolean directoryExists = (new File(dataDir)).mkdirs();
			if (!directoryExists) {
				throw new DeviceException("Failed to create temporary directory to place Xmap HDF5 files: " + dataDir);
			}
		}
		dataDir = dataDir.replace("/dls/" + beamline.toLowerCase(), "X:/");
		controller.setDirectory(dataDir);
	}

	@Override
	public void update(Object source, Object arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public Callable<NexusTreeProvider> getPositionCallable() throws DeviceException {
		return indexer.getPositionCallable();
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		try {
			setupFileAttributes();
			controller.resetCounters();

			// controller.setNexusCapture(0);
			controller.setAutoPixelsPerBuffer(true);
			controller.setPixelsPerRun(scanNumberOfPoints);
			int buffPerRow = (scanNumberOfPoints + 1) / 124 + 1;
			if (controller.isBufferedArrayPort())
				controller.setHdfNumCapture(scanNumberOfPoints);
			else
				controller.setHdfNumCapture(buffPerRow);

			cachedExtraNames = null;
			cachedOutputFormat = null;
			armedForNewScan = true;

		} catch (Exception e) {
			armedForNewScan = false;
			logger.error("Error occurred arming the xmap detector", e);
			throw new DeviceException("Error occurred arming the xmap detector", e);
		}
		this.indexer = new PositionStreamIndexer<>(new XmapPositionInputStream(this, xmap.isSumAllElementData()));
	}

	@Override
	public void atScanStart() throws DeviceException {
		controller.setCollectionMode(COLLECTION_MODES.MCA_MAPPING);
	}

	@Override
	public void atScanEnd() throws DeviceException {
		try {
			xmap.stop();
			controller.endRecording();
		} catch (Exception e) {
			controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
			logger.error("Unalble to end hdf5 capture", e);
			throw new DeviceException("Unalble to end hdf5 capture", e);
		}
		controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		try {
			xmap.stop();
			controller.endRecording();
		} catch (Exception e) {
			controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
			logger.error("Unalble to end hdf5 capture", e);
			throw new DeviceException("Unalble to end hdf5 capture", e);
		}
		controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
	}

	@Override
	public void waitForCurrentScanFile() throws DeviceException, InterruptedException {
		String fileName = getHDFFileName();
		// Should actually use getHardwareTriggerProvider().getNumberTriggers()
		// but this is always null before the scan is run once
		while (true) {// (waitedSoFar <= timeOut){
			if (fileName.contains(lastScanNumber + "-" + lastRowNumber)) {
				waitForFile(fileName);
				break;
			}
			Thread.sleep(100);
			fileName = getHDFFileName();
		}
	}

	@Override
	public void waitForFile(String fileName) throws DeviceException, InterruptedException {
		// Should actually use getHardwareTriggerProvider().getNumberTriggers()
		// but this is always null before the scan is run once
		double timeoutMilliSeconds = getCollectionTime() * this.scanNumberOfPoints * 1000 + 1000000;
		double waitedSoFarMilliSeconds = 0;
		int waitTime = 1000;
		while (isStillWriting(fileName) && waitedSoFarMilliSeconds <= timeoutMilliSeconds) {
			Thread.sleep(waitTime);
			waitedSoFarMilliSeconds += waitTime;
		}
		// wait for another second to file to be closed
		Thread.sleep(waitTime);

	}

	public boolean isStillWriting(String fileName) throws DeviceException {
		try {
			if (controller.getHDFFileName().equals(fileName))
				return controller.getCaptureStatus();
		} catch (Exception e) {
			logger.error("Cannot read the file capture status", e);
			throw new DeviceException("Cannot read the file capture status", e);
		}
		return false;
	}

	@Override
	public boolean integratesBetweenPoints() {
		return isIntegrateBetweenPoints();
	}

	@Override
	public String getHDFFileName() throws DeviceException {
		try {
			return controller.getHDFFileName();
		} catch (Exception e) {
			throw new DeviceException("CAnnot get the hdf5 file name from the xmap controller", e);
		}
	}

	@Override
	public boolean isInBufferMode() throws Exception {
		return controller.isBufferedArrayPort();
	}

	public boolean isSlave() {
		return slave;
	}

	public void setSlave(boolean slave) {
		this.slave = slave;
	}

	public boolean isIntegrateBetweenPoints() {
		return integrateBetweenPoints;
	}

	public void setIntegrateBetweenPoints(boolean integrateBetweenPoints) {
		this.integrateBetweenPoints = integrateBetweenPoints;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		if (armedForNewScan)
			return false;
		return super.isBusy();

	}
}
