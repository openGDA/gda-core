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

import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.XmapDetector;
import gda.device.detector.DAServer;
import gda.device.detector.hardwaretriggerable.HardwareTriggerableDetectorBase;
import gda.device.detector.xmap.edxd.EDXDController.COLLECTION_MODES;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionStreamIndexer;
import gda.factory.Configurable;
import gda.factory.FactoryException;

public class HardwareTriggeredNexusXmap extends HardwareTriggerableDetectorBase implements XmapDetector,
		PositionCallableProvider, Configurable {
	static final Logger logger = LoggerFactory.getLogger(HardwareTriggeredNexusXmap.class);
	
	private DAServer daServer;
	private NexusXmap xmap;
	private EDXDMappingController controller;
	
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
	private PositionStreamIndexer<NexusTreeProvider> indexer;
	@Override
	public void configure() throws FactoryException {
	
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
	public void start() throws DeviceException {
		xmap.start();
	}

	@Override
	public void collectData() throws DeviceException {
		//do nothing
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return " Hardware Triggered Xmap detector";
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
	public Object readout() throws DeviceException {
		throw new DeviceException("Can only be used in continuous scans");
	}
	
	@Override
	public void arm() throws DeviceException {
		//prepare the detector for continuous collection
		// TODO Auto-generated method stub
		/*try {
			setupFilename();
			controller.resetCounters();
			
			//controller.setNexusCapture(0);
			controller.setAutoPixelsPerBuffer(true);
			int numberOfPointsPerScan = getHardwareTriggerProvider().getNumberTriggers(); 
			//??TODO should get the number of points per scan 
			controller.setPixelsPerBuffer(numberOfPointsPerScan);
			int buffPerRow = (numberOfPointsPerScan + 1)/124 + 1;
			//controller.setNexusNumCapture(buffPerRow);
			//TODO prepare tfg for triggering
			//controller.clearAndStart();
			//controller.setNexusCapture(1);
			controller.startRecording();
		} catch (Exception e) {
			
			logger.error("TODO put description of error here", e);
			throw new DeviceException("Error occurred arming the xmap detector", e);
		}*/

	}
	private void setupFilename() throws Exception {
		String beamline = null;
		try {
			beamline = GDAMetadataProvider.getInstance().getMetadataValue("instrument", "gda.instrument", null);
		} catch (DeviceException e1) {
		}

		// If the beamline name isn't set then default to 'base'.
		if (beamline == null) {
			// If the beamline name is not set then use 'base'
			beamline = "base";
		}

		controller.setFilenamePrefix(beamline);
		controller.setFilenamePostfix(getName());
		
		// Check to see if the data directory has been defined.
		String dataDir = PathConstructor.createFromDefaultProperty();

		controller.setDirectory(dataDir);
		
		// Now lets try and setup the NumTracker...
		NumTracker runNumber = new NumTracker("tmp");
		// Get the current number
		Number scanNumber = runNumber.getCurrentFileNumber();
		
		controller.setFileNumber(scanNumber);
	}

	@Override
	public void update(Object source, Object arg) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("rawtypes")
	@Override
	public Callable getPositionCallable() throws DeviceException {
		return indexer.getPositionCallable();
	}
	
	@Override
	public void atScanLineStart() throws DeviceException
	{
		try {
			//setup tfg time frames
			setTimeFrames();
			setupFilename();
			controller.resetCounters();
			
			//controller.setNexusCapture(0);
			controller.setAutoPixelsPerBuffer(true);
			int numberOfPointsPerScan = getHardwareTriggerProvider().getNumberTriggers(); 
			//??TODO should get the number of points per scan 
			controller.setPixelsPerRun(numberOfPointsPerScan);
			int buffPerRow = (numberOfPointsPerScan + 1)/124 + 1;
			controller.setHdfNumCapture(buffPerRow);
			controller.startRecording();
		} catch (Exception e) {
			
			logger.error("TODO put description of error here", e);
			throw new DeviceException("Error occurred arming the xmap detector", e);
		}
		this.clearAndStart();
		this.indexer  = new PositionStreamIndexer<NexusTreeProvider>(new XmapPositionInputStream(this));
	}

	private void setTimeFrames() {
		switchOnExtTrigger();
		getDaServer().sendCommand("tfg setup-groups ext-start cycles 1");
		getDaServer().sendCommand(getHardwareTriggerProvider().getNumberTriggers() + " 0.000001 0.00000001 0 0 0 8");
		getDaServer().sendCommand("-1 0 0 0 0 0 0");
		getDaServer().sendCommand("tfg arm");
	}
	private void switchOnExtTrigger() {
		getDaServer().sendCommand("tfg setup-trig start ttl0");
	}

	private void switchOffExtTrigger() {
		getDaServer().sendCommand("tfg setup-trig start"); // disables external triggering
	}
	public void setDaServer(DAServer daServer) {
		this.daServer = daServer;
	}

	public DAServer getDaServer() {
		return daServer;
	}
	@Override
	public void atScanStart() throws DeviceException
	{
		controller.setCollectionMode(COLLECTION_MODES.MCA_MAPPING);
	}
	@Override
	public void atScanEnd() throws DeviceException
	{
		try {
			switchOffExtTrigger();
			controller.endRecording();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
			logger.error("TODO put description of error here", e);
			throw new DeviceException("Unalble to end hdf5 capture" , e);
		}
		controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
	}
	@Override
	public void atCommandFailure() throws DeviceException
	{
		try {
			switchOffExtTrigger();
			controller.endRecording();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
			logger.error("TODO put description of error here", e);
			throw new DeviceException("Unalble to end hdf5 capture" , e);
		}
		controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
	}
	
	@Override
	public void atScanLineEnd() throws DeviceException
	{
		
	}

	public void waitForFile(String fileName) throws DeviceException, InterruptedException {
		double timeoutMilliSeconds = getCollectionTime() * getHardwareTriggerProvider().getNumberTriggers()*1000;
		double waitedSoFarMilliSeconds = 0;
		int waitTime = 1000;
		while(isStillWriting(fileName) || waitedSoFarMilliSeconds <=  timeoutMilliSeconds)
		{
			Thread.sleep(waitTime);
			waitedSoFarMilliSeconds += waitTime;
		}
		//wait for another second to file to be closed
		//Thread.sleep(waitTime);
		
	}

	public boolean isStillWriting(String fileName) throws DeviceException {
		try {
			if(controller.getHDFFileName().equals(fileName))
				return controller.getCaptureStatus();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
			throw new DeviceException("CAnnot read the file capture status", e);
		}
		return false;
	}
	@Override
	public boolean integratesBetweenPoints() {
		// TODO Auto-generated method stub
		return true;
	}
	
	public boolean isInBufferMode() throws Exception{
		return controller.isBufferedArrayPort();
	}

}
