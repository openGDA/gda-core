/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import gda.analysis.RCPPlotter;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.fileregistrar.FileRegistrarHelper;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.areadetector.AreaDetectorBin;
import gda.device.detector.areadetector.AreaDetectorROI;
import gda.device.detector.areadetector.EPICSAreaDetectorImage;
import gda.device.detector.areadetector.EpicsAreaDetector;
import gda.device.detector.areadetector.EpicsAreaDetectorFileSave;
import gda.device.detector.areadetector.EpicsAreaDetectorROI;
import gda.device.detector.areadetector.EpicsAreaDetectorROIElement;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.util.Sleep;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.january.dataset.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PixiumController extends DetectorBase {

	// Setup the logging facilities
	transient private static final Logger logger = LoggerFactory.getLogger(PixiumController.class);

	// Variables to hold the spring settings
	private EpicsAreaDetector           areaDetector;
	private EpicsAreaDetectorROI        areaDetectorROI;
	private EpicsAreaDetectorROIElement fullFrameROI;
	private EpicsAreaDetectorFileSave   fullFrameSaver;
	private EpicsAreaDetectorROIElement previewROI;
	private EPICSAreaDetectorImage      image;
	private Integer                     idlePollTime_ms			= 100;
	private String                      basePVName              = null;

	private boolean liveThreadRunning = false;

	private File fullImageLocation;

	private LiveThread liveThread;

	/** Possible status value, indicates detector is in readout state */
	public final int READOUT = MONITORING + 1;
	/** Possible status value, indicates detector is in data correcting state */
	public final int CORRECT = READOUT + 1;
	/** Possible status value, indicates detector is in data saving state */
	public final int SAVING = CORRECT + 1;
	/** Possible status value, indicates detector is in aborting state */
	public final int ABORTING = SAVING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int MODECHANGING = ABORTING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int DEFINING = MODECHANGING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int DELETING = DEFINING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int LOADING = DELETING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int UNLOADING = LOADING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int ACTIVATING = UNLOADING + 1;
	/** Possible status value, indicates detector is in readout state */
	public final int DEACTIVATING = ACTIVATING + 1;


	// Getters and Setters for Spring
	public EpicsAreaDetector getAreaDetector() {
		return areaDetector;
	}

	public void setAreaDetector(EpicsAreaDetector areaDetector) {
		this.areaDetector = areaDetector;
	}

	public EpicsAreaDetectorROIElement getFullFrameROI() {
		return fullFrameROI;
	}

	public void setFullFrameROI(EpicsAreaDetectorROIElement fullFrameROI) {
		this.fullFrameROI = fullFrameROI;
	}

	public EpicsAreaDetectorFileSave getFullFrameSaver() {
		return fullFrameSaver;
	}

	public void setFullFrameSaver(EpicsAreaDetectorFileSave fullFrameSaver) {
		this.fullFrameSaver = fullFrameSaver;
	}

	public EPICSAreaDetectorImage getImage() {
		return image;
	}

	public void setImage(EPICSAreaDetectorImage image) {
		this.image = image;
	}

	public Integer getIdlePollTime_ms() {
		return idlePollTime_ms;
	}

	public void setIdlePollTime_ms(Integer idlePollTimeMs) {
		idlePollTime_ms = idlePollTimeMs;
	}

	public EpicsAreaDetectorROIElement getPreviewROI() {
		return previewROI;
	}

	public void setPreviewROI(EpicsAreaDetectorROIElement previewROI) {
		this.previewROI = previewROI;
	}	

	public EpicsAreaDetectorROI getAreaDetectorROI() {
		return areaDetectorROI;
	}

	public void setAreaDetectorROI(EpicsAreaDetectorROI areaDetectorROI) {
		this.areaDetectorROI = areaDetectorROI;
	}	

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	// Values internal to the object for Channel Access
	private EpicsChannelManager ecm = new EpicsChannelManager();
	private EpicsController ecl = EpicsController.getInstance();

	// Channels
	private Channel channelLogicalMode;
	private Channel channelLogicalMode_RBV;
	private Channel channelPUMode;
	private Channel channelPUMode_RBV;
	private Channel channelNumberOffsetRefs;
	private Channel channelNumberOffsetRefs_RBV;
	private Channel channelOffsetReferenceNumber;
	private Channel channelOffsetReferenceNumber_RBV;
	private Channel channelOffsetReference;
	private Channel channelOffsetReference_RBV;
	private Channel channelActivateMode;	
	private Channel channelXRayWindow;
	private Channel channelXRayWindow_RBV;
	private Channel channelXRayFrequency;
	private Channel channelXRayFrequency_RBV;
	private Channel channelOffsetReferenceApply;
	private Channel channelDefineMode;
	private Channel channelDeleteMode;
	private Channel channelLoadMode;
	private Channel channelUnloadMode;
	private Channel channelSetActiveMode;
	private Channel channelUnsetActiveMode;
	private Channel channelChangeMode;
	private Channel channelAquisitionMode_RBV;	

	private String plotName;

	private int pointNumber;

	private boolean localDataStore = false;

	public boolean isLocalDataStore() {
		return localDataStore;
	}

	public void setLocalDataStore(boolean localDataStore) {
		this.localDataStore = localDataStore;
	}

	// Methods for configurable interface and the reset method
	@Override
	public void configure() throws FactoryException{
		try {
			channelLogicalMode = ecl.createChannel(basePVName + "LogicalMode");
			channelLogicalMode_RBV = ecl.createChannel(basePVName + "LogicalMode_RBV");
			channelPUMode = ecl.createChannel(basePVName + "PUMode");
			channelPUMode_RBV = ecl.createChannel(basePVName + "PUMode_RBV");
			channelNumberOffsetRefs = ecl.createChannel(basePVName + "NumberOfOffsets");
			channelNumberOffsetRefs_RBV = ecl.createChannel(basePVName + "NumberOfOffsets_RBV");
			channelOffsetReferenceNumber = ecl.createChannel(basePVName + "OffsetReferenceNumber");
			channelOffsetReferenceNumber_RBV = ecl.createChannel(basePVName + "OffsetReferenceNumber_RBV");
			channelOffsetReference = ecl.createChannel(basePVName + "OffsetReference");
			channelOffsetReference_RBV = ecl.createChannel(basePVName + "OffsetReference_RBV");
			channelXRayWindow = ecl.createChannel(basePVName + "XRayWindow");
			channelOffsetReference = ecl.createChannel(basePVName + "OffsetReferenceNumber");
			channelOffsetReference_RBV = ecl.createChannel(basePVName + "OffsetReferenceNumber_RBV");
			channelAquisitionMode_RBV = ecl.createChannel(basePVName + "AcquisitionMode_RBV");
			channelActivateMode = ecl.createChannel(basePVName + "ActivateMode");
			channelXRayWindow_RBV = ecl.createChannel(basePVName + "XRayWindow_RBV");
			channelXRayFrequency = ecl.createChannel(basePVName + "FrameRate");
			channelXRayFrequency_RBV = ecl.createChannel(basePVName + "FrameRate_RBV");
			channelOffsetReferenceApply = ecl.createChannel(basePVName + "DefineOffsetReference");
			channelDefineMode = ecl.createChannel(basePVName + "DefineMode");
			channelDeleteMode = ecl.createChannel(basePVName + "DeleteMode");
			channelLoadMode = ecl.createChannel(basePVName + "LoadMode");
			channelUnloadMode = ecl.createChannel(basePVName + "UnloadMode");
			channelSetActiveMode = ecl.createChannel(basePVName + "ActivateMode");
			channelUnsetActiveMode = ecl.createChannel(basePVName + "DeactivateMode");
			channelChangeMode = ecl.createChannel(basePVName + "ChangeMode");
			channelAquisitionMode_RBV = ecl.createChannel(basePVName + "AcquisitionMode_RBV");
			
			// acknowledge that creation phase is completed
			ecm.creationPhaseCompleted();
			
		} catch (Exception e) {
			throw new FactoryException("Failure to initialise AreaDetector", e);
		}

	}	
	
	public void resetAll() throws CAException, InterruptedException {
		areaDetector.reset();
		areaDetectorROI.reset();
		previewROI.reset();
		fullFrameROI.reset();
		fullFrameSaver.reset();
		previewROI.reset();
		image.reset();
	}
	
	// Pixium specific methods
	public String setMode(int logicalMode, int offset) throws IllegalStateException, CAException, TimeoutException, InterruptedException {
		
		ecl.caput(channelLogicalMode, logicalMode);
		Thread.sleep(2000);
		ecl.caput(channelOffsetReferenceNumber, offset);
		ecl.caput(channelSetActiveMode, 1);
		
		Thread.sleep(2000);
		return this.report();
	}
	
	
	public String report() throws TimeoutException, CAException, InterruptedException {
		String result = this.getAquisitionMode();
		result += "\nBinning      = " + areaDetector.getBinning().toString();
		result += "\nROI          = " + areaDetector.getROI().toString();
		result += "\nX-Ray window = " + this.getXRayWindow() + "ms"; 
		result += "\nFrequency    = " + this.getFrequency() + "mHz";
		return result;
	}

	public void setExposures(int numberOfExposures) throws CAException, InterruptedException {
		areaDetector.setNumExposures(numberOfExposures);
	}
	
	public int getExposures() throws NumberFormatException, TimeoutException, CAException, InterruptedException {
		return areaDetector.getNumExposures();
	}
	
	// Methods which are required for the Detector Interface
	@Override
	public void collectData() throws DeviceException {
		try {
			// first make sure the detector is idle
			String state = areaDetector.getState();
			while(!state.equalsIgnoreCase("0")) {
				logger.debug("State of the camera is {}",state);
				state = areaDetector.getState();
				Sleep.sleep(idlePollTime_ms);				
			}

			// Having some issues with the camera, so adding in a small sleep here, 
			// running the camera more slowly seems to help with EPICS so might be
			// an issue of the commands coming through too quickly with the new 10G
			// Card

			areaDetector.acquire();

			// now sleep for a small period to let the detector set itself to busy
			Thread.sleep(100);

		} catch (Exception e) {
			throw new DeviceException("Failure to cause Pixium detector to Acquire",e);
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	@Override
	public String getDescription() throws DeviceException {
		//TODO Parameterise this
		return "Pixium" ;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		//TODO Parameterise this
		return "I12";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Image Plate";
	}

	@Override
	public int getStatus() throws DeviceException {	
		try {
			
			int state = areaDetector.getAquireState();
			if(state == 1) {
				return Detector.BUSY;
			}
			
			state = Integer.parseInt(areaDetector.getState());
			if(state == 1 || state==2 || state==3 || state==4) {
				return Detector.BUSY;
			}
		} catch (Exception e) {
			throw new DeviceException("Failure to cause Pixium Detector to read Status",e);
		} 

		return Detector.IDLE;
	}

	@Override
	public Object readout() throws DeviceException {

		Vector<String> outputArray = new Vector<String>();
		String output = ""; 		

		try {
			String absolutePath = fullImageLocation.getAbsolutePath();			
			String fileName = fullFrameSaver.getFileName();
			fileName = fileName.trim();
			
			output = String.format(fullFrameSaver.getFileTemplate(),absolutePath,fileName,0);
			FileRegistrarHelper.registerFile(output);
			outputArray.add(output.trim());		
			
			// now all the files collected need to be registered
			for ( int i = 1; i < getCollectionTime(); i++) {
				
				output = String.format(fullFrameSaver.getFileTemplate(),absolutePath,fileName,i);
				// registers the file for archiving.
				FileRegistrarHelper.registerFile(output);
				
				outputArray.add(String.format(fullFrameSaver.getFileTemplate(),"",fileName,i));				
			}
			
		} catch (Exception e) {
			throw new DeviceException("Failure to readout Pixium detector",e);
		}

		// whatever happens though, pipe the output to the PCOPlot screen
//		try {
//			AreaDetectorBin bin = previewROI.getBinning();
//			double scalefactor = bin.getBinX()*bin.getBinY();
//			// the division here is to normalise the data so it always appears to be plain 16bit
//			DataSet data = (DataSet)this.getImage().getImage().__div__(scalefactor);
//
//			RCPPlotter.imagePlot(getPlotName(), data);
//		} catch (Exception e) {
//			logger.error("Failure to send Pixium update to PixiumPlot, with error",e);
//		}
		int numberOfFilesCollected = outputArray.size();
		String output1 = numberOfFilesCollected +" images collected: [" + outputArray.get(0) + ", ......, "+outputArray.get(numberOfFilesCollected-1) +"]";
		return output1;
		//return outputArray.toString();
	}


	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		// The collection time in this case is the number of images which appear per aquisition.
		this.collectionTime = collectionTime;
		try {
			areaDetector.setNumImages((int) Math.round(collectionTime));
		} catch (Exception e) {
			if( e instanceof RuntimeException)
				throw (RuntimeException)e;
			throw new DeviceException(getName() + " exception in setCollectionTime",e);
		}
	}

	public void acquire(int numberOfImage) throws Exception {
		if (liveThreadRunning) {
			// try to deactivate live mode before starting the scan
			try {
				this.stoplive();
			} catch (Exception e) {
				throw new DeviceException("Preview running when scan was started, and could not be deactivated.  Stop the preview and then restart the scan",e);
			} 
		}

		try {
			areaDetector.setNumImages(numberOfImage);
			// as it is not part of any scan we need to increment file number
			// here.
			NumTracker nt = new NumTracker("i12");
			long filenamuber = nt.incrementNumber();
			// set up file path in EPICS
			pointNumber = 0; // initialise a scan
			resetAndEnableFullFrameCapture();
			fullImageLocation = createMainFileStructure();
			setFileSaverParameters(fullImageLocation);
			InterfaceProvider.getTerminalPrinter().print("Saving data in " + fullImageLocation.getAbsolutePath());
			atPointStart();
			// create metadata file to hold time stamp info
			File file = new File(fullImageLocation.getAbsolutePath()+ File.separator + filenamuber + ".dat");
			FileWriter fileWriter = new FileWriter(file);
			Vector<String> imageFiles = new Vector<String>();
			int lastImageCounter = areaDetector.getArrayCounter();
			int imageCounter = 0;
			String filename = file.getAbsolutePath();
			String fullFilename = null;
			double timeStamp;
			imageFiles.add(filename);
			areaDetector.acquire();
			// kick off the preview image thread
			//liveThreadRunning = true;

			// run up the thread to monitor the image
			
//			try {
//				liveThread = new LiveThread();
//				AreaDetectorBin bin = previewROI.getBinning();
//				liveThread.setup(bin.getBinX()*2, bin.getBinY()*2, getImage(), this, false);
//				liveThread.start();
//			} catch (Exception e) {
//				logger.error("Failed to start live view thread", e);
//			} 	

			while (getStatus() == Detector.BUSY) {
				imageCounter = areaDetector.getArrayCounter();
				if (imageCounter > lastImageCounter) {
					fullFilename = fullFrameSaver.getFullFileName();
					timeStamp = fullFrameSaver.getTimeStamp();
					filename = windows2LinuxFilename(fullFilename);
					imageFiles.add(filename);
					InterfaceProvider.getTerminalPrinter().print(imageCounter + "\t" + timeStamp + "\t" + filename);
					lastImageCounter = imageCounter;
					fileWriter.write(imageCounter + "\t" + timeStamp + "\t"	+ filename + "\n");
				}
			}
			fileWriter.close();
			FileRegistrarHelper.registerFiles(imageFiles);

		} catch (CAException e) {
			logger.error("Pixium acquire failed with error", e);
		}
	}

	public void setFileSaverParameters(File filePath) throws CAException, InterruptedException {
		// make the change for the windows filesystem and mount
		// TODO Definatly parameterise this
		String filePathString =null;
		if (localDataStore ) {
			filePathString = filePath.getAbsolutePath().replace("/dls/i12", "C:");
		} else {
			filePathString = filePath.getAbsolutePath().replace("/dls/i12", "Z:");
		}
		fullFrameSaver.setFilePath(filePathString);
		//fullFrameSaver.setFilePath("c:/pixiumdata/");
	}
	private String windows2LinuxFilename(String filename) {
		String filePathString;
		if (localDataStore ) {
			filePathString = filename.replace("C:","/dls/i12");
		} else {
			filePathString = filename.replace("Z:","/dls/i12");
		}
		
		return filePathString;
	}

	public void resetAndEnableFullFrameCapture() throws CAException, TimeoutException, InterruptedException {
		
		// ROI Elements
		areaDetectorROI.setEnable(true);

		// ROI Full Frame enable, to the size of the camera
		fullFrameROI.setUse(true);
		AreaDetectorROI roi = areaDetector.getROI();
		fullFrameROI.setROI(roi);
		fullFrameROI.setDataType(areaDetector.getInitialDataType());		

		// Frame Saver Elements, set all to capture, but dont enable them
		fullFrameSaver.setEnable(true);
		fullFrameSaver.setframeCounter(0);
		fullFrameSaver.startCapture();

		// Make sure the preview channel is also available
		image.setEnable(true);
		previewROI.setUse(true);
		
		// Set the area Detector code
		areaDetector.setArrayCounter(0);
		// set the image mode to Multiple
		areaDetector.setImageMode(1);

	}


	// Methods for the Scannable interface
	@Override
	public void atScanStart() throws DeviceException {
		//TODO needs implementation, especially folder creation
		logger.debug("Starting Pixium.AtScanStart");

		if (liveThreadRunning) {
			// try to deactivate live mode before starting the scan
			try {
				this.stoplive();
			} catch (Exception e) {
				throw new DeviceException("Preview running when scan was started, and could not be deactivated.  Stop the preview and then restart the scan",e);
			} 
		}
	
		// make sure the camera is set to taking single images
		try {
			pointNumber=0; // initialise a scan
			resetAndEnableFullFrameCapture();	
			fullImageLocation = createMainFileStructure();
			setFileSaverParameters(fullImageLocation);
			// force the Acquire period to zero
			areaDetector.setAcquirePeriod(0.0);
			
			// check that the Acquisition mode is ok.
			if (this.getAquisitionMode().contains("NOT SET")) {
				throw new DeviceException("Pixium Mode not set!");
			}
		
		} catch (Exception e) {
			throw new DeviceException("AtScanStart failed with error :",e);
		} finally {
			logger.debug("Ending PCO.AtScanStart");
		}
		
		// kick off the preview image thread
		liveThreadRunning = true;

		// run up the thread to monitor the image
		
		try {
			liveThread = new LiveThread();
			AreaDetectorBin bin = previewROI.getBinning();
			liveThread.setup(bin.getBinX()*2, bin.getBinY()*2, getImage(), this, false);
			liveThread.start();
		} catch (Exception e) {
			logger.error("Failed to start live view thread", e);
		} 	

	}
	
	@Override
	public void atScanEnd() {

		try {
			// stop the thread
			liveThreadRunning = false;
		
			liveThread.join();

		} catch (Exception e) {
			logger.warn("Fail to stop liveThread at scan end",e);
		}		
	}

	@Override
	public void atPointStart() throws DeviceException {
		String filename =fullFrameSaver.getInitialFileName().concat(String.format("_%06d",pointNumber));
		try {
			fullFrameSaver.setFileName(filename);
			fullFrameSaver.setframeCounter(0);
		} catch (Exception e) {
			if( e instanceof RuntimeException)
				throw (RuntimeException)e;
			throw new DeviceException(getName() + " exception in atPointStart",e);
		}
	}

	@Override
	public void atPointEnd(){
		pointNumber++;
	}
	
	private String getAquisitionMode() throws TimeoutException, CAException, InterruptedException {
		return ecl.cagetString(channelAquisitionMode_RBV);
	}
	
	private int getXRayWindow() throws TimeoutException, CAException, InterruptedException {
		ecl.caput(channelOffsetReference,ecl.cagetInt(channelOffsetReferenceNumber));
		return ecl.cagetInt(channelXRayWindow_RBV);
	}
	
	private int getFrequency() throws TimeoutException, CAException, InterruptedException {
		ecl.caput(channelOffsetReference,ecl.cagetInt(channelOffsetReferenceNumber));
		return ecl.cagetInt(channelXRayFrequency_RBV);
	}

	
	// Helper methods for dealing with the file system.
	public File createMainFileStructure() throws IOException {

		// set up the filename which will be the base directory for data to be saved to
		File path = new File(PathConstructor.createFromDefaultProperty()); 
		NumTracker nt = new NumTracker("i12");
		String filenumber = Long.toString(nt.getCurrentFileNumber());
		File scanFolder = new File(path, filenumber);
		// Make the directory if required.
		if(!scanFolder.isDirectory()) {
			// create the directory
			scanFolder.mkdir();
		}

		return scanFolder;
	}


	public void waitForIdle() throws TimeoutException, CAException, InterruptedException {
		String state = areaDetector.getState();
		while(!state.equalsIgnoreCase("0")) {
			logger.debug("State of the camera is {}",state);
			state = areaDetector.getState();
			Sleep.sleep(idlePollTime_ms);				
		}
	}


	public void preview(double exposureTime) throws CAException, TimeoutException, InterruptedException, DeviceException {

		// if the live thread is running then ignore this statement
		if (liveThreadRunning == false) {

			// make sure the detector is stopped
			waitForIdle();

			// remember to turn off the image saving
			// Might be timing issues
			fullFrameSaver.setEnable(false);

			// set the exposure time
			areaDetector.setAcquirePeriod(exposureTime);

			image.setEnable(true);
			
			// set it going
			areaDetector.acquire();

			liveThreadRunning = true;

			// run up the thread to monitor the image
			liveThread = new LiveThread();
			AreaDetectorBin bin = previewROI.getBinning();
			liveThread.setup(bin.getBinX()*2, bin.getBinY()*2, getImage(), this, true);
			liveThread.start();

		} else {
			logger.warn("Preview already running");

			// however if the collection time specified is different,
			// then stop the live preview, and start it again with the new time

			if (Math.abs(this.getCollectionTime() - exposureTime) > 0.001) {

				try {
					InterfaceProvider.getTerminalPrinter().print("Changing the exposure time");
					this.stoplive();
					this.preview(exposureTime);
				} catch (InterruptedException e) {
					InterfaceProvider.getTerminalPrinter().print(
					"Preview already running, tried to change the exposure time but there was a problem");
				}
			}


		}

	}

	@Override
	public void stop() throws DeviceException{
		super.stop();
		try {
			this.stoplive();
		} catch (Exception e) {
			throw new DeviceException("Cannot stop pixium detector",e);
		} 
	}

	public void stoplive() throws CAException, InterruptedException, TimeoutException {

		// stop the camera
		areaDetector.stop();

		// stop the thread
		liveThreadRunning = false;

		liveThread.join();		

		waitForIdle();

		Thread.sleep(100);

		// set the mode back to single
		areaDetector.setImageMode(0);

		// set the ROI size back to full screen
		previewROI.setROI(areaDetector.getInitialMinX(), areaDetector.getInitialMinY(),
				areaDetector.getInitialSizeX(), areaDetector.getInitialSizeY());

		// pause to allow all the settings to be flushed to EPICS
		Thread.sleep(1000);
	}


	private class LiveThread extends Thread {

		int binX;
		int binY;
		EPICSAreaDetectorImage threadimage = null;
		PixiumController parent = null;
		boolean preview = false;

		public void setup(int binx, int biny, EPICSAreaDetectorImage image, PixiumController parent, boolean preview) {
			this.binX = binx;
			this.binY = biny;
			this.threadimage = image;
			this.parent = parent;
			this.preview = preview;
		}

		@SuppressWarnings("static-access")
		@Override
		public void run() {

			while (parent.liveThreadRunning) {

				logger.debug("Monitoring thread running");

				try {
					double scalefactor = binX*binY;
					// the division here is to normalise the data so it always appears to be plain 16bit
					DoubleDataset data = threadimage.getImage().idivide(scalefactor);

					// set it going
					if(preview) {
					areaDetector.acquire();
					}
					
					RCPPlotter.imagePlot(getPlotName(), data);
					
				} catch (Exception e) {
					logger.warn("Failure send Pixium update to PIxiumPlot, with error",e);
				} finally {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						logger.error("Failure send Pixium update to PixiumPlot, with error",e1);
					}
				}

				// should sleep here for a bit to let the thread update.
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

		}
	}
	public String getPlotName() {
		return plotName;
	}
	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}	


}
