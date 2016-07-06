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

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.fileregistrar.FileRegistrarHelper;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.IPCOControllerV17;
import gda.device.detector.IPCODetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDFile;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import gda.util.Sleep;
import gov.aps.jca.TimeoutException;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.IFileLoader;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.TIFFImageLoader;
import uk.ac.gda.devices.pco.LiveModeUtil;

/**
 * Separating out the detector from the controller - Part of GDA-4231 area detector stuff to get all detectors aligned
 * to EPICS V1.7
 * 
 * @author rsr31645
 */
public class PCODetector extends DetectorBase implements InitializingBean, IPCODetector {

	private boolean isWindowsIoc = true;
	public final String GDA_FILE_TRANSFER_SCRIPT = "gda.file.transfer.script";
	private String description;
	private String detectorID;
	private String detectorType;
	private IPCOControllerV17 controller;
	private File scanSaveFolder;
	private String localFilePath;
	// parameter required by Tomography reconstruction software
	private String projectionFolderName = "projections";

	// private String numTrackerTag;
	private Windows2LinuxFilePath localDataStoreWindows2LinuxFilePath;
	private Windows2LinuxFilePath nonLocalDataStoreWindows2LinuxFilePath;
	private Windows2LinuxFilePath demandRawDataStoreWindows2LinuxFileName;
	private String darkFileNameRoot;
	private int numberOfDarkImages;
	private String faltFileNameRoot;
	private int numberOfFlatImages;
	private static final Logger logger = LoggerFactory.getLogger(PCODetector.class);
	private boolean saveLocal = false;
	private boolean hdfFormat = true; // default is HDF format
	private String plotName;
	private long aquisitionStartTime;
	private int pauseTime;
	private boolean externalTriggered = false;
	private boolean isPreviewing = false;
	private boolean scanRunning;
	private boolean firstcall;

	public PCODetector() {
		setLocal(true); // do not use CORBA, use RMI
	}

	public void preview(double acquireTime) throws Exception {
		ADBase areaDetector = controller.getAreaDetector();

		// stop the camera first
		controller.stop();
		// then change to preview parameters
		controller.setImageMode(2);
		areaDetector.setAcquireTime(acquireTime);
		// make sure all file savers disabled
		controller.disableTiffSaver();
		controller.disableHdfSaver();
		// TODO start external MJEP GUI on client?
		controller.acquire();
		isPreviewing = true;
	}

	private void stopPreview() throws Exception {
		controller.stop();
		isPreviewing = false;
	}

	/**
	 * @return Returns the hdfFormat.
	 */
	@Override
	public boolean isHdfFormat() {
		return hdfFormat;
	}

	@Override
	public void collectData() throws DeviceException {
		// TODO sleep needed to give time for camera readout as detector has no READOUT status
		if (!isExternalTriggered()) {
			// software data collection
			try {
				controller.acquire();
				// aquisitionStartTime = System.currentTimeMillis();
			} catch (Exception e1) {
				logger.error("{} acquire failed", getName());
				throw new DeviceException(getName() + " acquire request failed.", e1);
			}
		} else {
			// make sure that the new time is at least the acquisition Time plus the pause
			long nextStartTime = (long) (aquisitionStartTime + pauseTime + (collectionTime * 1000.0));
			long currentTime = System.currentTimeMillis();
			// InterfaceProvider.getTerminalPrinter().print(String.format("%d, %d", nextStartTime, currentTime));
			// wait for the correct amount of time, and then start
			if (nextStartTime > currentTime) {
				try {
					Thread.sleep(nextStartTime - currentTime);
				} catch (InterruptedException e) {
					throw new DeviceException("Failed to cause Thread.sleep", e);
				}
			}
			// triggered data collection
			try {
				controller.trigger();
				aquisitionStartTime = System.currentTimeMillis();
			} catch (Exception e) {
				if (e instanceof RuntimeException)
					throw (RuntimeException) e;
				if (e instanceof DeviceException)
					throw (DeviceException) e;
				throw new DeviceException("Failed to make PCO camera trigger", e);
			}
		}
	}

	public boolean isExternalTriggered() {
		return this.externalTriggered;
	}

	@Override
	public void setExternalTriggered(boolean externalTriggered) {
		this.externalTriggered = externalTriggered;
	}

	/**
	 * @param hdfFormat
	 *            The hdfFormat to set.
	 */
	@Override
	public void setHdfFormat(boolean hdfFormat) {
		this.hdfFormat = hdfFormat;
	}

	@Override
	public int getStatus() {
		if (isExternalTriggered()) {
			if (firstcall) {
				firstcall = false;
				try {
					if (controller.getTiff().getNumCaptured_RBV() == 0) {
						return Detector.BUSY;
					}
				} catch (Exception e) {
					logger.error("getNumCaptured_RBV() failed at getStatus()", e);
				}
				return Detector.IDLE;
			}
			// this to make sure motors can be moved at detector readout time
			if (System.currentTimeMillis() < (aquisitionStartTime + (collectionTime * 1000))) {
				return Detector.BUSY;
			}
			return Detector.IDLE;
		}
		int status = controller.getAreaDetector().getStatus(); // get camera acquire state
		return status;
	}

	@Override
	public Object readout() throws DeviceException {
		String output = "";
		NXDetectorData dataTree = new NXDetectorData();
		try {
			if (hdfFormat) {
				print("Frames collected: " + controller.getHdf().getFile().getNumCaptured_RBV());
				if (controller.getHdf().getFile().getNumCapture_RBV() == controller.getHdf().getFile()
						.getNumCaptured_RBV()) {
					// when capturing completed
					int totalmillis = 120 * 1000; // 2 minutes timeout
					int grain = 25;
					long timer = 0, timer0 = System.currentTimeMillis();
					while (isWriterBusy() && timer - timer0 < totalmillis) {
						Thread.sleep(grain);
						timer = System.currentTimeMillis();
					}
					if (timer - timer0 >= totalmillis) {
						throw new TimeoutException(
								"It takes too long to write out data from EPICS Area Detector HDF buffer.");
					}
					if (scanRunning) {
						output = getFilePath(controller.getHDFFileName());
						dataTree.addScanFileLink(getName(), "nxfile://" + output + "#entry/instrument/detector/data");
					}
				}
			} else {
				// do not want to wait EPICS AD full file name update before collect next image - to support NDArray
				// buffering
				String fileTemplate = "File template not set";
				String fileName = "File name not set";
				int fileNumber = -1;
				if (controller.getTiff() != null) {
					if (controller.getTiff().getFileTemplate() != null) {
						fileTemplate = controller.getTiff().getFileTemplate().trim();
					}
					fileName = controller.getTiff().getFileName();
					fileNumber = controller.getTiff().getFileNumber();
				}

				output = String.format(fileTemplate, scanSaveFolder.getAbsolutePath(), fileName, fileNumber);
				if (scanRunning) {
					dataTree.addScanFileLink(getName(), "nxfile://" + output + "#entry/instrument/detector/data");
				}
			}
		} catch (Exception e) {
			throw new DeviceException("readout failed to add scan file link to NeXus data file.", e);
		}
		return output; // dataTree;
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

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		try { // TODO beamline reported sometime exposure need to be set twice to succeed
			if (LiveModeUtil.isLiveMode()) {
				if (controller.isArmed()) {
					controller.disarmCamera();
				}
			}
			else {
				LoggerFactory.getLogger("PCODetector:"+this.getName()).info("setCollectionTime: Not live!");
			}
			ADBase areaDetector = controller.getAreaDetector();
			areaDetector.setAcquireTime(collectionTime);
			this.collectionTime = collectionTime;
		} catch (Exception e) {
			logger.error("{} failed to set exposure time to {}.", getName(), collectionTime);
			throw new DeviceException(getName() + " failed to set exposure time to " + collectionTime, e);
		}
	}

	public void setCollectionTime(Object... collectspec) throws DeviceException {
		this.collectionTime = Double.parseDouble(collectspec[0].toString());
		int numImagesPerPoint = Integer.parseInt(collectspec[1].toString());
		int totalNumImages = Integer.parseInt(collectspec[2].toString());
		try {
			if (LiveModeUtil.isLiveMode()) {
				if (controller.isArmed()) {
					controller.disarmCamera();
				}
			}
			else {
				LoggerFactory.getLogger("PCODetector:"+this.getName()).info("setCollectionTime: Not live!");
			}
			ADBase areaDetector = controller.getAreaDetector();
			areaDetector.setAcquireTime(collectionTime);
			controller.setNumImages(numImagesPerPoint);
			if (numImagesPerPoint > 1) {
				controller.setImageMode(1); // multiple image per data point
			}
			controller.getHdf().getFile().setNumCapture(totalNumImages);
		} catch (Exception e) {
			logger.error("{} failed to set exposure time {}.", getName(), collectionTime);
			throw new DeviceException(getName() + " failed to set exposure time", e);
		}
	}

	// Methods for the Scannable interface
	@Override
	public void atScanStart() throws DeviceException {
		logger.debug("scan start");
		if (isPreviewing) { // gda in preview
			try {
				stopPreview();
			} catch (Exception e) {
				throw new DeviceException("Failed to stop preview", e);
			}
		}

		try {
			pauseTime = controller.getReadoutTime();
			boolean firstTime = true;
			// ensure detector buffer writer completed before proceed
			while (isWriterBusy()) {
				if (firstTime) {
					logger.warn(
							"{}: buffer is still writing out data to disk from previous collection. please wait...",
							getName());
					print("the file writer in EPICS Area detector is busy. Please release it to proceed or abort this scan instead");
					firstTime = false;
				}
				Sleep.sleep(100);
			}
			controller.makeDetectorReadyForCollection(); // this will leave camera disarmed
			if (hdfFormat) {
				controller.getHdf().getFile().getPluginBase().enableCallbacks();
				controller.getTiff().getPluginBase().disableCallbacks();
				controller.getHdf().stopCapture();
				controller.getHdf().getFile().getPluginBase().setDroppedArrays(0);
				controller.getHdf().getFile().getPluginBase().setArrayCounter(0);
			} else {
				controller.getHdf().getFile().getPluginBase().disableCallbacks();
				controller.getTiff().getPluginBase().enableCallbacks();
				controller.getTiff().stopCapture();
				controller.getTiff().getPluginBase().setDroppedArrays(0);
				controller.getTiff().getPluginBase().setArrayCounter(0);
				controller.getTiff().setFileNumber(0);
			}
			setScanNumberAlreadyIncremented(true); // scan number is increamented by scan class before atScanStart
													// called.
			// must set file path and file name before starting capturing -EPICS required this
			initialiseFilePath();
			setFileName();
			ScanInformation scaninfo = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
			if (hdfFormat) {
				controller.setScanDimensions(scaninfo.getDimensions());
				controller.startRecording();
			} else {
				controller.getTiff().setNumCapture(totalNumberImages2Collect(scaninfo.getDimensions()));
				controller.getTiff().startCapture();
			}
			scanRunning = true;
			controller.armCamera();
			Sleep.sleep(3000);
			firstcall = true;
			aquisitionStartTime = System.currentTimeMillis();
		} catch (Exception e) {
			logger.error("atScanStart failed with error:", e);
			throw new DeviceException("atScanStart failed with error:", e);
		}
	}

	private int totalNumberImages2Collect(int[] dimensions) {
		int total = 1;
		for (int i = 0; i < dimensions.length; i++) {
			total *= dimensions[i];
		}
		return total;
	}

	@Override
	public void atScanEnd() throws DeviceException {
		setScanNumberAlreadyIncremented(true);

		scanRunning = false;
		if (hdfFormat) {
			int num;
			try {
				if ((num = controller.getHdf().getFile().getPluginBase().getDroppedArrays_RBV()) != 0) {
					controller.getHdf().stopCapture();
					throw new DeviceException("Buffer reports dropped array number: " + num);
				}
			} catch (Exception e) {
				logger.error("Failed to get Dropped Array parameter from HDF plugin.", e);
				throw new DeviceException("Failed to get Dropped Array parameter from HDF plugin.", e);
			}
			long starttimer = System.currentTimeMillis();
			long timer = 0;
			try {
				while (controller.getHdf().getCapture_RBV() == 1
						&& timer < starttimer + 10 * getCollectionTime() * 1000) {
					Sleep.sleep(50);
					timer = System.currentTimeMillis();
					// if (controller.getHdf().getNumCaptured_RBV() +
					// controller.getHdf().getPluginBase().getDroppedArrays_RBV()==
					// controller.getHdf().getPluginBase().getArrayCounter_RBV()) {
					// controller.getHdf().stopCapture();
					// }
				}
			} catch (Exception e) {
				logger.error("Failed to getCapture_RBV() from HDF plugin.", e);
				throw new DeviceException("Failed to getCapture_RBV() from HDF plugin.", e);
			}
			if (timer >= starttimer + 10 * getCollectionTime() * 1000) {
				try {
					controller.getHdf().stopCapture();
				} catch (Exception e) {
					logger.error("failed to stop capture on HDF plugin", e);
					throw new DeviceException("failed to stop capture on HDF plugin", e);
				}
				throw new DeviceException("TimeoutException: It takes to long to collect the last frame");
			}
			try {
				FileRegistrarHelper.registerFile(getFilePath(controller.getHDFFileName()));
			} catch (Exception e) {
				logger.error("failed to getHDFFileName() in HDF plugin", e);
				throw new DeviceException("failed to getHDFFileName() in HDF plugin", e);
			}
			try {
				controller.endRecording();
			} catch (Exception e) {
				logger.error("failed to endRecording() in HDF plugin", e);
				throw new DeviceException("failed to endRecording() in HDF plugin", e);
			}
		} else {
			int num;
			try {
				num = controller.getTiff().getPluginBase().getDroppedArrays_RBV();
			} catch (Exception e1) {
				logger.error("Failed to get Dropped Array parameter from Tiff plugin.", e1);
				throw new DeviceException("Failed to get Dropped Array parameter from Tiff plugin.", e1);
			}
			if (num != 0) {
				try {
					controller.getTiff().stopCapture();
				} catch (Exception e) {
					logger.error("Failed to stop capture in Tiff plugin.", e);
					throw new DeviceException("Failed to stop capture in Tiff plugin.", e);
				}
				throw new DeviceException(getName() + ": reports dropped array number: " + num);
			}
			long starttimer = System.currentTimeMillis();
			long timer = 0;
			try {
				while (controller.getTiff().getCapture_RBV() == 1
						&& timer < starttimer + 10 * getCollectionTime() * 1000) {
					Sleep.sleep((int) getCollectionTime() / 10);
					timer = System.currentTimeMillis();
					// if (controller.getTiff().getNumCaptured_RBV() +
					// controller.getTiff().getPluginBase().getDroppedArrays_RBV()==
					// controller.getTiff().getPluginBase().getArrayCounter_RBV()) {
					// controller.getTiff().stopCapture();
					// }
				}
			} catch (Exception e) {
				logger.error("Failed to getCapture_RBV() in Tiff plugin.", e);
				throw new DeviceException("Failed to getCapture_RBV() in Tiff plugin.", e);
			}
			// if (timer>=starttimer+10*getCollectionTime()*1000){
			// try {
			// controller.getTiff().stopCapture();
			// } catch (Exception e) {
			// logger.error("Failed to stop capture in Tiff plugin.", e);
			// throw new DeviceException("Failed to stop capture in Tiff plugin.", e);
			// }
			// throw new DeviceException("TimeoutException: It takes to long to collect the last frame");
			// }
		}
		try {
			controller.disarmCamera();
		} catch (Exception e) {
			logger.error("Failed to disarmCamera() in Tiff plugin.", e);
			throw new DeviceException("Failed to disarmCamera() in Tiff plugin.", e);
		}

		if (saveLocal) {
			try {
				while (isWriterBusy()) {
					Sleep.sleep(100);
				}
			} catch (Exception e) {
				logger.error("check buffer writting state failed", e);
				throw new DeviceException("check buffer writting state failed", e);
			}
			transferFiles();
		}
	}

	@Override
	public void atPointStart() throws DeviceException {
		if (!hdfFormat) {
			try { // Tiff file save must make capture ready for capturing in Stream mode at each point
					// controller.getTiff().setNumCapture(1);
					// controller.getTiff().startCapture();
			} catch (Exception e) {
				logger.error("Failed to start Capturing.", e);
				throw new DeviceException(getName() + ": Failed to start Capturing.");
			}
		}
	}

	private void initialiseFilePath() throws IOException, Exception {
		scanSaveFolder = createMainFileStructure(); // this statement rely on GDA file tracker increment properly
													// already
		setFilePath(scanSaveFolder);
		
		//Initialise the file template
		NDFile fullFrameSaver;
		if (hdfFormat) {
			fullFrameSaver = controller.getHdf().getFile();
		} else {
			fullFrameSaver = controller.getTiff();
		}
		fullFrameSaver.setFileTemplate(fullFrameSaver.getInitialFileTemplate());
	}

	private void setFileName() throws IOException, Exception {
		NDFile fullFrameSaver;
		if (hdfFormat) {
			fullFrameSaver = controller.getHdf().getFile();
		} else {
			fullFrameSaver = controller.getTiff();
		}
		fullFrameSaver.setFileName(fullFrameSaver.getInitialFileName());
	}

	@Override
	public void stop() throws DeviceException {
		super.stop();
		try {
			controller.stop();
			setScanNumberAlreadyIncremented(false);
		} catch (Exception e) {
			logger.error("PCO Detector failed to stop", e);
			throw new DeviceException("PCO Detector failed to stop", e);
		}
		if (saveLocal) {
			transferFiles();
		}
	}

	@Override
	public void resetAll() throws Exception {
		controller.resetAll();
	}

	public void setFormat(String format) {
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, format);
	}

	public void setNexusFormat() {
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "NexusDataWriter");
	}

	/**
	 * manage the file path mapping between Windows (where the EPICS IOC is running) and Linux (Where data is to be
	 * stored).
	 * 
	 * @param filePath
	 * @throws Exception
	 */
	@Override
	public void setFilePath(File filePath) throws Exception {

		String filePathString = null;
		if (saveLocal) {
			filePathString = filePath.getAbsolutePath().replace(
					getLocalDataStoreWindows2LinuxFilePath().getLinuxPath(),
					getLocalDataStoreWindows2LinuxFilePath().getWindowsPath());
		} else {
			filePathString = filePath.getAbsolutePath().replace(
					getNonLocalDataStoreWindows2LinuxFilePath().getLinuxPath(),
					getNonLocalDataStoreWindows2LinuxFilePath().getWindowsPath());
		}
		if (hdfFormat) {
			controller.getHdf().getFile().setFilePath(filePathString);
		} else {
			controller.getTiff().setFilePath(filePathString);
		}
	}

	public String getFilePath(String filePath) {

		String filePathString = null;
		if (saveLocal) {
			filePathString = filePath.replace(getLocalDataStoreWindows2LinuxFilePath().getWindowsPath(),
					getLocalDataStoreWindows2LinuxFilePath().getLinuxPath());
		} else {
			filePathString = filePath.replace(getNonLocalDataStoreWindows2LinuxFilePath().getWindowsPath(),
					getNonLocalDataStoreWindows2LinuxFilePath().getLinuxPath());
		}
		return filePathString;
	}

	File metadataFile;
	Vector<String> imageFiles = new Vector<String>();
	private boolean scanNumberAlreadyIncremented = false;

	public boolean isScanNumberAlreadyIncremented() {
		return scanNumberAlreadyIncremented;
	}

	public void setScanNumberAlreadyIncremented(boolean scanNumberAlreadyIncremented) {
		this.scanNumberAlreadyIncremented = scanNumberAlreadyIncremented;
	}

	private long filenumber = -1;
	private boolean isWindowsIocSet = false;

	/**
	 * processes after collected data: <li>Archival of data collected</li> <li>Display the last image</li>
	 */
	public void afterCollection() {
		FileRegistrarHelper.registerFiles(imageFiles);
		// TODO call stop?
		// stop();
		// Ensure GDA reset cache detector status as stop is not called after fast collection or callback failed.
		if (getStatus() != Detector.IDLE) {
			controller.getAreaDetector().setStatus(Detector.IDLE);
		} else {
			if (hdfFormat) {
				controller.getHdf().getFile().setStatus(Detector.IDLE);
			} else {
				controller.getTiff().setStatus(Detector.IDLE);
			}
		}
		print("collection completed.");
	}

	/**
	 * processing to be done before starting acquire data from detector - create data storage parameters and metadata
	 * file
	 * 
	 * @throws Exception
	 * @throws IOException
	 * @throws DeviceException
	 */
	public void beforeCollection() throws Exception, IOException, DeviceException {
		// as outside of any scan we need to increment file number here.
		NumTracker nt = new NumTracker(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME));
		if (!isScanNumberAlreadyIncremented()) {
			filenumber = nt.getCurrentFileNumber() + 1;
		} else {
			filenumber = nt.getCurrentFileNumber();
		}
		// set file path must be done before start capture
		initialiseFilePath();
		print("Saving data in directory: " + scanSaveFolder.getAbsolutePath());
	}

	/**
	 * collect specified number of dark images - users must ensure the shutter is closed before call this method
	 * 
	 * @param numberOfDarks
	 * @throws Exception
	 */
	@Override
	public void collectDarkSet(int numberOfDarks) throws Exception {

		logger.info("{} starts to collect {} dark images, please wait...", getName(), numberOfDarks);
		beforeCollection();

		if (hdfFormat) {
			// set file name must be done before start capture
			controller.getHdf().setFileName(getDarkFileNameRoot());
			controller.getHdf().setNumExtraDims(0);
			controller.getHdf().setNumCapture(numberOfDarks);
			controller.getHdf().getFile().startCapture();

		} else {
			// set file name must be done before start capture
			controller.getTiff().setFileName(getDarkFileNameRoot());
			controller.getTiff().setFileNumber(0);
			controller.getTiff().setNumCapture(numberOfDarks);
			controller.getTiff().startCapture();
		}
		// capture the appropriate number of images
		controller.setNumImages(numberOfDarks);
		controller.setImageMode(1);
		if (isExternalTriggered()) {
			setExternalTriggered(false);
		}
		collectData();
		print("starting collect " + numberOfDarks + " dark images. Please wait ......");
		// wait for the collection to be complete
		while (getStatus() == Detector.BUSY) {
			Sleep.sleep(100);
		}
		if (hdfFormat) {
			imageFiles.add(getFilePath(controller.getHdf().getFile().getFullFileName_RBV()));
		} else {
			String formatter = controller.getTiff().getFileTemplate().trim();
			String filename = controller.getTiff().getFileName();
			for (int i = 0; i < numberOfDarks; i++) {
				imageFiles.add(getFilePath(String.format(formatter, scanSaveFolder.getAbsolutePath(), filename, i)));
			}
		}
		afterCollection();
	}

	/**
	 * collect the dark image set - users must ensure the shutter is closed and number of images is set in EPICS
	 * 
	 * @throws Exception
	 */
	@Override
	public void collectDarkSet() throws Exception {
		int numberOfDarkImages = controller.getNumImages();
		collectDarkSet(numberOfDarkImages);
	}

	/**
	 * collect specified numbers of flat images, users must ensure the shutter is open and sample is outside from the
	 * field of view.
	 * 
	 * @param numberOfFlats
	 *            - number of flat images for each flat set
	 * @param flatSet
	 *            - the set number
	 * @throws Exception
	 */
	@Override
	public void collectFlatSet(int numberOfFlats, int flatSet) throws Exception {
		logger.info("{} starts to collect {} flat images, please wait...", getName(), numberOfFlats);
		beforeCollection();

		if (hdfFormat) {
			controller.getHdf().setFileName(getFlatFileNameRoot());
			controller.getHdf().setNumExtraDims(0);
			controller.getHdf().setNumCapture(numberOfFlats);
			controller.getHdf().getFile().startCapture();
		} else {
			controller.getTiff().setFileName(String.format("%s_%03d", getFlatFileNameRoot(), flatSet));
			controller.getTiff().setFileNumber(0);
			controller.getTiff().setNumCapture(numberOfFlats);
			controller.getTiff().startCapture();
		}

		// capture the appropriate number of images
		controller.setNumImages(numberOfFlats);
		controller.setImageMode(1);
		if (isExternalTriggered()) {
			setExternalTriggered(false);
		}
		collectData();
		print("starting collect " + numberOfFlats + " flat images. Please wait ......");
		// wait for the collection to be complete
		while (getStatus() == Detector.BUSY) {
			Sleep.sleep(100);
		}
		if (hdfFormat) {
			imageFiles.add(getFilePath(controller.getHdf().getFile().getFullFileName_RBV()));
		} else {
			String formatter = controller.getTiff().getFileTemplate().trim();
			String filename = controller.getTiff().getFileName();
			for (int i = 0; i < numberOfFlats; i++) {
				imageFiles.add(getFilePath(String.format(formatter, scanSaveFolder.getAbsolutePath(), filename, i)));
			}
		}
		afterCollection();
	}

	/**
	 * collect specified numbers of flat images, users must ensure the shutter is open and sample is outside from the
	 * field of view and set the number of flat images required in EPICS.
	 * 
	 * @throws Exception
	 */
	public void collectFlatSet(int flatSet) throws Exception {
		int numberOfFlats = controller.getNumImages();
		collectFlatSet(numberOfFlats, flatSet);
	}

	public void resetFileNumber() throws Exception {
		if (hdfFormat) {
			controller.getHdf().setFileNumber(0);
		} else {
			controller.getTiff().setFileNumber(0);
		}
		setScanNumberAlreadyIncremented(false);
	}

	/**
	 * plot image to the PCOPlot view.
	 * 
	 * @param imageFileName
	 */

	@Override
	public void plotImage(final String imageFileName) {
		// Plot the last image collected from file
		Thread plot = new Thread(new Runnable() {
			boolean plotted = false;
			int trycounter = 0;
			double starttime = System.currentTimeMillis();
			double time = 0.0, timeout = 15000.0;
			double fileexisttime;
			private boolean firsttime = true;

			@Override
			public void run() {
				while (!plotted && time < timeout) {

					if ((new File(imageFileName)).exists()) {
						if (firsttime) {
							fileexisttime = System.currentTimeMillis();
							firsttime = false;
						}
						try {

							IFileLoader loader = LoaderFactory.getLoader(TIFFImageLoader.class, imageFileName);
							IDataHolder dataHolder = loader.loadFile();
							IDataset dataset = dataHolder.getDataset(0);
							if (dataset != null) {
								dataset.clearMetadata(null);
								SDAPlotter.imagePlot(getPlotName(), dataset);
								// SDAPlotter.imagePlot(getPlotName(), imageFileName);
							}

							plotted = true;
						} catch (Exception e) {
							trycounter++;
							logger.error("Plot data {} try {} failed", imageFileName, trycounter);
							plotted = false;
						}
						print("Time elasped since plotting request: " + (System.currentTimeMillis() - starttime));
						logger.debug("Time elasped since plotting request: {}",
								(System.currentTimeMillis() - starttime));
						print("Time elasped since file first appears: " + (System.currentTimeMillis() - fileexisttime));
						logger.debug("Time elasped since file first appears: {}",
								(System.currentTimeMillis() - fileexisttime));

					} else {
						// logger.debug("wait for data file {} to plot.", imageFileName);
					}
					time = (System.currentTimeMillis() - starttime);
				}
			}
		}, "plotpcoimage");
		plot.start();
	}

	// Helper methods for dealing with the file system.
	@Override
	public File createMainFileStructure() throws IOException {
		// set up the filename which will be the base directory for data to be saved to
		File path = new File(PathConstructor.createFromDefaultProperty());
		NumTracker nt = new NumTracker(LocalProperties.GDA_BEAMLINE_NAME);
		String filenumber;
		if (!isScanNumberAlreadyIncremented()) {
			// for non-scan use
			filenumber = Long.toString(this.filenumber);
		} else {
			// for scan which already increments the file number
			filenumber = Long.toString(nt.getCurrentFileNumber());
		}
		File scanFolder = new File(path, filenumber);
		// Make the directory if required.
		if (!scanFolder.isDirectory()) {
			// create the directory
			scanFolder.mkdir();
		}
		if (hdfFormat) { // HDF to save both h5 and nxs data in scanNumber folder
			return scanFolder;
		}
		// tiff to save image in projections folder under scanNumber folder
		File projectionsFolder = new File(scanFolder, projectionFolderName);
		if (!projectionsFolder.isDirectory()) {
			// create the directory required for Tomography reconstruction software
			projectionsFolder.mkdir();
		}
		return projectionsFolder;
	}

	@Override
	public boolean isBusy() {
		return getStatus() == Detector.BUSY;
	}

	private void transferFiles() throws DeviceException {
		try {
			// copy the files off
			Runtime rt = Runtime.getRuntime();
			String script = LocalProperties.get(GDA_FILE_TRANSFER_SCRIPT, null);
			if (script == null) {
				logger.warn("property '{}' is not defined.", GDA_FILE_TRANSFER_SCRIPT);
				return;
			}
			Process p = rt.exec(String.format("%s %s", script, scanSaveFolder.getAbsolutePath()));
			InterfaceProvider.getTerminalPrinter().print("Start to copy data to Central Storage");
			// TODO this should be in a monitoring loop
			p.waitFor();

		} catch (Exception e) {
			throw new DeviceException("Failed to run pco.transferFiles.", e);
		}
	}

	@Override
	public boolean isWriterBusy() throws Exception {
		if (hdfFormat) {
			return controller.getHdf().getFile().getWriteFile_RBV() == 1; // get buffer writer state from HDF
		}
		return controller.getTiff().getWriteFile_RBV() == 1; // get buffer writer state from tif
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (description == null) {
			throw new IllegalArgumentException("'description' is required");
		}
		if (detectorID == null) {
			throw new IllegalArgumentException("'detectorID' is required");
		}
		if (detectorType == null) {
			throw new IllegalArgumentException("'detectorType' is required");
		}
		if (controller == null) {
			throw new IllegalArgumentException("'pcoController' needs to be set");
		}
	}

	public void setController(IPCOControllerV17 controller) {
		this.controller = controller;
	}

	@Override
	public IPCOControllerV17 getController() {
		return controller;
	}

	@Override
	public String getDescription() throws DeviceException {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return detectorID;
	}

	public void setDetectorID(String detectorID) {
		this.detectorID = detectorID;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return detectorType;
	}

	public void setDetectorType(String detectorType) {
		this.detectorType = detectorType;
	}

	public void setLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
	}

	@Override
	public String getLocalFilePath() {
		return localFilePath;
	}

	@Override
	public String getProjectionFolderName() {
		return projectionFolderName;
	}

	public void setProjectionFolderName(String projectionFoldername) {
		this.projectionFolderName = projectionFoldername;
	}

	public void setDarkFileNameRoot(String darkFileNameRoot) {
		this.darkFileNameRoot = darkFileNameRoot;
	}

	@Override
	public String getDarkFileNameRoot() {
		return darkFileNameRoot;
	}

	public void setFlatFileNameRoot(String faltFileNameRoot) {
		this.faltFileNameRoot = faltFileNameRoot;
	}

	@Override
	public String getFlatFileNameRoot() {
		return faltFileNameRoot;
	}

	public void setNumberOfDarkImages(int numberOfDarkImages) {
		this.numberOfDarkImages = numberOfDarkImages;
	}

	@Override
	public int getNumberOfDarkImages() {
		return numberOfDarkImages;
	}

	public void setNumberOfFlatImages(int numberOfFlatImages) {
		this.numberOfFlatImages = numberOfFlatImages;
	}

	@Override
	public int getNumberOfFlatImages() {
		return numberOfFlatImages;
	}

	static class Windows2LinuxFilePath {
		private String windowsPath;

		private String linuxPath;

		public String getWindowsPath() {
			return windowsPath;
		}

		public void setWindowsPath(String windowsPath) {
			this.windowsPath = windowsPath;
		}

		public void setLinuxPath(String linuxPath) {
			this.linuxPath = linuxPath;
		}

		public String getLinuxPath() {
			return linuxPath;
		}

		/**
		 * @param fileName
		 * @return the linux file name for a given windows file name respective to the location of the datastore.
		 */
		public String getFilePathString(String fileName) {
			String filePathString = fileName.replace(windowsPath, linuxPath);
			return filePathString;
		}
	}

	public Windows2LinuxFilePath getLocalDataStoreWindows2LinuxFilePath() {
		return localDataStoreWindows2LinuxFilePath;
	}

	public void setLocalDataStoreWindows2LinuxFileName(Windows2LinuxFilePath localDataStoreWindows2LinuxFilePath) {
		this.localDataStoreWindows2LinuxFilePath = localDataStoreWindows2LinuxFilePath;
	}

	public Windows2LinuxFilePath getNonLocalDataStoreWindows2LinuxFilePath() {
		return nonLocalDataStoreWindows2LinuxFilePath;
	}

	public void setNonLocalDataStoreWindows2LinuxFileName(Windows2LinuxFilePath nonLocalDataStoreWindows2LinuxFilePath) {
		this.nonLocalDataStoreWindows2LinuxFilePath = nonLocalDataStoreWindows2LinuxFilePath;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	@Override
	public String getPlotName() {
		return plotName;
	}

	public String getFullFilename() throws Exception {
		if (isHdfFormat()) {
			return controller.getHdf().getFullFileName_RBV();
		}
		return controller.getTiff().getFullFileName_RBV();
	}

	public void setNumImages(int num) throws Exception {
		controller.setNumImages(num);
	}

	public void setNumCapture(int num) throws Exception {
		if (hdfFormat) {
			controller.getHdf().getFile().setNumCapture(num);
		} else {
			controller.getTiff().setNumCapture(num);
		}
	}

	/**
	 * Acquire and wait till the acquisition is complete.
	 * 
	 * @throws Exception
	 */
	@Override
	public void acquireSynchronously() throws Exception {
		controller.getAreaDetector().startAcquiringSynchronously();
		if (hdfFormat) {
			while (controller.getHdf().getStatus() != Detector.IDLE) {
				Sleep.sleep(100);
			}
		} else {
			while (controller.getTiff().getStatus() != Detector.IDLE) {
				Sleep.sleep(100);
			}
		}
	}

	@Override
	public void stopCapture() throws Exception {
		if (hdfFormat) {
			controller.getHdf().stopCapture();
		} else {
			controller.getTiff().stopCapture();
		}
	}

	public void setWindowsIoc(boolean isWindowsIoc) {
		this.isWindowsIoc = isWindowsIoc;
		isWindowsIocSet = true;
	}

	public boolean isWindowsIoc() {
		if (isWindowsIocSet) {
			return isWindowsIoc;
		}
		// Only the live mode is windows
		return LiveModeUtil.isLiveMode();
	}

	/**
	 * @return Returns the demandRawDataStoreWindows2LinuxFileName.
	 */
	public Windows2LinuxFilePath getDemandRawDataStoreWindows2LinuxFileName() {
		return demandRawDataStoreWindows2LinuxFileName;
	}

	/**
	 * s
	 * 
	 * @param demandRawDataStoreWindows2LinuxFileName
	 *            The demandRawDataStoreWindows2LinuxFileName to set.
	 */
	public void setDemandRawDataStoreWindows2LinuxFileName(Windows2LinuxFilePath demandRawDataStoreWindows2LinuxFileName) {
		this.demandRawDataStoreWindows2LinuxFileName = demandRawDataStoreWindows2LinuxFileName;
	}

	@Override
	public String getTiffImageFileName() throws Exception {
		if (isWindowsIoc()) {
			return controller.getTiffFullFileName().replaceAll(
					demandRawDataStoreWindows2LinuxFileName.getWindowsPath(),
					demandRawDataStoreWindows2LinuxFileName.getLinuxPath());
		}
		return controller.getTiffFullFileName();
	}

	@Override
	public void setTiffFilePathBasedOnIocOS(String demandRawFilePath) throws Exception {
		if (isWindowsIoc()) {
			String replacedWindowsPath = demandRawFilePath.replaceAll(
					demandRawDataStoreWindows2LinuxFileName.getLinuxPath(),
					demandRawDataStoreWindows2LinuxFileName.getWindowsPath());
			controller.getTiff().setFilePath(replacedWindowsPath);
		} else {
			controller.getTiff().setFilePath(demandRawFilePath);
		}
	}

	@Override
	public void setADCMode(int mode) throws Exception {
		// The ADC mode is available only the actual PCO IOC and not the simulation
		if (LiveModeUtil.isLiveMode()) {
			controller.setADCMode(mode);
		}
		else {
			LoggerFactory.getLogger("PCODetector:"+this.getName()).info("setADCMode: Not live!");
		}
	}

	public void setSaveLocal(boolean saveLocal) {
		this.saveLocal = saveLocal;
	}

	public boolean isSaveLocal() {
		return saveLocal;
	}

	public void setTriggerPV(String triggerPV) {
		controller.setTriggerPV(triggerPV);
	}

	public String getTriggerPV() {
		return controller.getTriggerPV();
	}

	public void setTimestampMode(int timestampMode) throws Exception {
		controller.setTimestampMode(timestampMode);
	}

}
