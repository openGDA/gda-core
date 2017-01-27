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

package gda.device.detector.pixium;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.fileregistrar.FileRegistrarHelper;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NXDetectorDataWithFilepathForSrs;
import gda.device.detector.areadetector.v17.NDFile;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import uk.ac.gda.devices.pixium.IPixiumController;
import uk.ac.gda.devices.pixium.IPixiumDetector;

/**
 * Pixium detector supports EPICS area detector pixium driver and various area detector plugins, including region of interest, sub array sample, data processing
 * and statics. HDF5 and Tiff file savers, and MJPEG live image streaming. Instance of this class is a scannable thus can be used in a scan command for data
 * collection. In addition, it also provide a fast data collection methods when nothing need to be scanned over. There are a few default properties implemented
 * in this class you must be aware of:
 * <p>
 * <li>data acquisition is defaulted to file saver capture control while continuously acquiring and you can change this to camera acquire control by setting
 * <code>pixium.setCaptureControl(False)</code>.</li>
 * <li>data storage is defaulted to the lustre storage, to change it to local storage on Windows run <code>
 * pixium.setLocalDataStore(True)</code></li>
 * <li>data format is defaulted to TIFF image, to change it to HDF5 run <code>pixium.setHdfFormat(True)</code></li>
 * <li>Data plotting is defaulted to NO so it does not automatically plot after each acquisition, to change it run
 * <code>pixium.setAutoPlotting(True)</code></li>
 * </p>
 */
public class PixiumDetector extends DetectorBase implements InitializingBean, IPixiumDetector {

	private static final Logger logger = LoggerFactory.getLogger(PixiumDetector.class);

	public static final double UnixEpochDifferenceFromEPICS = 631152000;

	private boolean localDataStore = false; // default to write to Lustre storage
	private File scanSaveFolder; // scan number is used as folder name
	private String detectorID;
	private String plotName; // image display pane -set in Spring configuration
	private Windows2LinuxFilePath localDataStoreWindows2LinuxFilePath;
	private Windows2LinuxFilePath nonLocalDataStoreWindows2LinuxFilePath;
	private IPixiumController controller;
	private boolean captureControl = true; // default is capture mode
	private boolean hdfFormat = false; // default is tiff format
	private boolean isPreviewing = false;
	private int scanpointnumber = 0; // to support multiple images per scan data point
	private Vector<String> outputformats = new Vector<>();
	private boolean scanRunning;
	private boolean readAcquisitionTime = true;
	private boolean readAcquisitionPeriod = false;
	private boolean readFilepath = true;
	private INexusTree nexusMetaDataForPixium;
	private double[] timesCurrentAcq;
	private boolean getPositionCalledForCurrentAcq;

	private String pixCamPortName = "pix.cam";
	private int scanpointindex;

	public PixiumDetector() {

		setInputNames(new String[] { getName() });
		outputformats.add("%s"); // define input name's output format
	}

	public void setFormat(String format) {
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", format);
	}

	public void setSRSFormat() {
		LocalProperties.set("gda.data.scan.datawriter.dataFormat", "SrsDataFile");
	}

	/**
	 * start live streaming of images from detector, no viewer will open
	 *
	 * @throws Exception
	 */
	public void preview() throws Exception {
		// stop the camera first
		controller.stop();
		// then change to preview parameters
		controller.setImageMode(2);
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
	 * supports both file capture controlled and camera exposure controlled acquisition. Using
	 * pixium.setCaptureControl(True) to enable file capturing controlled acquisition; using
	 * pixium.setCaptureControl(False) to enable camera exposing controlled acquisition. Using pixium.isCaptureControl()
	 * to test which mode of acquisition is currently set.
	 */
	@Override
	public void collectData() throws DeviceException {
		try {
			if (!isCaptureControl()) {
				startAcquire();
			} else {
				startCapture();
			}
		} catch (Exception e) {
			throw new DeviceException("Failed to start Pixium detector to Acquire or capture", e);
		}
	}

	INexusTree getNexusMetaDataForPixium() throws DeviceException {
		try {
			NexusTreeNode top = new NexusTreeNode("top", NexusExtractor.NXDetectorClassName, null);
			top.addChildNode(makeNexusTreeNode("exposure_time", controller.getAcquireTime()));
			top.addChildNode(makeNexusTreeNode("acquire_peirod", controller.getAcquireTime()));
			return top;
		} catch (Exception e) {
			throw new DeviceException("Error getting metadata from Pixium", e);
		}
	}

	INexusTree makeNexusTreeNode(String label, double data) {
		NexusGroupData groupData = new NexusGroupData(data);
		return new NexusTreeNode(label, NexusExtractor.SDSClassName, null, groupData);
	}

	/**
	 * start image capture to file in capturing controlled acquisition mode. Precondition is that camera is already
	 * collecting continuously. It support both HDF format and TIFF format collection. Using pixium.setHdfFormat(True)
	 * to enable HDF format collection; using pixium.setHdfFormat(False) to enable TIFF format collection. Using
	 * pixium.isHdfFormat() to test which format is set currently.
	 */
	@Override
	public void startCapture() throws Exception {
		// to use image capture, detector must be acquiring continuously
		int state = controller.getAreaDetector().getStatus();
		if (state != Detector.BUSY) {
			logger.warn("Detector () is not acquiring, so is not ready for image capture.", getName());
			print("Detector " + getName() + " is not acquiring, so is not ready for image capture.");
			return;
		}
		if (hdfFormat) {
			controller.startRecording();
		} else {
			controller.startTiffCapture();
		}
	}

	/**
	 * start image acquire of the camera if camera is not already busy.
	 */
	@Override
	public void startAcquire() throws Exception {
		// first make sure the detector is idle
		int state = getStatus();
		if (state != Detector.IDLE) {
			logger.warn("Detector () is not ready for data collection. Please check the detector in EPICS screen.",
					getName());
			print("Detector " + getName() + " is not ready for data collection. Please check EPICS setting.");
			return;
		}
		controller.acquire();
	}

	/**
	 * get detector status from GDA sub components representing EPICS Plugin objects. It checks detector calibration
	 * state, detector acquiring state, and file save states.
	 */
	@Override
	public int getStatus() {
		int status = controller.getStatus(); // get detector calibration state
		if (status != Detector.BUSY) {
			if (!isCaptureControl()) {
				status = controller.getAreaDetector().getStatus(); // get acquire state from cam
			} else {
				if (hdfFormat) {
					status = controller.getHdf().getStatus(); // get capture state from HDF
				} else {
					status = controller.getTiff().getStatus(); // get capture state from tif
				}
			}
		}
		return status;
	}

	@Override
	public Object readout() throws DeviceException {
		final Vector<String> output = new Vector<>();
		final String absolutePath = scanSaveFolder.getAbsolutePath() + File.separator;
		final NXDetectorData dataTree = new NXDetectorData();
		try {
			if (hdfFormat) {
				print("Frames collected: " + controller.getHdf().getFile().getNumCaptured_RBV());
				if (controller.getHdf().getFile().getNumCapture_RBV() == controller.getHdf().getFile()
						.getNumCaptured_RBV()) {
					// when capturing completed, must wait for buffer to write out and full filename RBV update
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
						output.add(getFilePath(controller.getHDFFileName()));
						dataTree.addScanFileLink(getName(), "nxfile://" + output.get(0)
								+ "#entry/instrument/detector/data");
					}
				}
			} else {
				// cannot wait EPICS AD full file name update before collect next image - to support NDArray buffering
				String fileName = controller.getTiff().getFileName();
				fileName = fileName.trim();
				int counter = 0;
				if ((int) getCollectionTime() > 1) {
					int i = 0;
					// handle multiple images per data scan point.
					for (i = 0; i < getCollectionTime(); i++) {
						output.add(String.format(controller.getTiff().getFileTemplate(), absolutePath, fileName, i
								+ scanpointindex));
					}
					if (scanRunning) {
						dataTree.addFileNames(getName(), "scan point " + counter,
								output.toArray(new String[output.size()]), false, true);
					}
					scanpointindex = i + scanpointindex;
					counter += 1;
				} else {
					// single image per scan data point
					output.add(String.format(controller.getTiff().getFileTemplate(), absolutePath, fileName,
							scanpointindex));
					if (scanRunning) {
						dataTree.addFileName(getName(), output.get(0));
					}
					scanpointindex += 1;
				}
			}
		} catch (Exception e) {
			throw new DeviceException("readout failed to add scan file link to NeXus data file.", e);
		}
		// FileRegistrarHelper.registerFiles(output);
		if (LocalProperties.get("gda.data.scan.datawriter.dataFormat").matches("NexusDataWriter")) {
			// NeXus file format
			return dataTree;
		}
		// SRS file format
		return output.toArray(new String[output.size()]);
	}

	// @Override
	public Callable<NexusTreeProvider> getPositionCallable() {
		NexusTreeProviderCallable nexusTreeProviderCallable = new NexusTreeProviderCallable(getName(),
				nexusMetaDataForPixium, timesCurrentAcq, !getPositionCalledForCurrentAcq);
		getPositionCalledForCurrentAcq = true;
		return nexusTreeProviderCallable;
	}

	private class NexusTreeProviderCallable implements Callable<NexusTreeProvider> {

		final private String name;
		final private INexusTree nexusMetaDataForPixium1;
		private final double[] timesCurrentAcq2;
		private final boolean firstCall;

		public NexusTreeProviderCallable(String name, INexusTree nexusMetaDataForLima, double[] timesCurrentAcq,
				boolean firstCall) {
			this.name = name;
			this.nexusMetaDataForPixium1 = nexusMetaDataForLima;
			timesCurrentAcq2 = timesCurrentAcq;
			this.firstCall = firstCall;
		}

		@Override
		public NexusTreeProvider call() throws Exception {

			// use NXDetectorDataWithFilepathForSrs so that filename is printed in terminal
			NXDetectorDataWithFilepathForSrs data = new NXDetectorDataWithFilepathForSrs(PixiumDetector.this);
			if (hdfFormat) {
				String filename;
				try {
					filename = controller.getHdfFullFileName();
				} catch (Exception e) {
					logger.error("Cannot get the image data file name from EPICS HDF plugin.", e);
					throw new DeviceException(getName() + ": Cannot get the image data file name from EPICS.", e);
				}
				// add reference to external file
				NexusTreeNode fileNameNode = data.addFileNames(getName(), "image_data", new String[] { filename },
						true, true);

				// add filename as an NXNote
				// data.addFileName(getName(), filename);
				data.addScanFileLink(getName(), filename);
				if (firstCall) {
					fileNameNode.addChildNode(new NexusTreeNode("signal", NexusExtractor.AttrClassName, fileNameNode,
							new NexusGroupData(1)));
					fileNameNode.addChildNode(new NexusTreeNode("axes", NexusExtractor.AttrClassName, fileNameNode,
							new NexusGroupData("time")));
					data.addAxis(name, "time", new NexusGroupData(timesCurrentAcq2), 1, 1, "s", false);
				}
				// must match the list of input and extra names
				data.setDoubleVals(new Double[] { new Double(1.0) });
			} else {
				String[] filenames;
				try {
					filenames = createFileName();
				} catch (Exception e) {
					logger.error("Cannot get the image data file name from EPICS Tiff plugin", e);
					throw new DeviceException(getName() + ": Cannot get the image data file name from EPICS.", e);
				}

				// add reference to external file
				NexusTreeNode fileNameNode = data.addFileNames(getName(), "scan_point_" + scanpointnumber, filenames,
						true, true);
				if (firstCall) {
					fileNameNode.addChildNode(new NexusTreeNode("signal", NexusExtractor.AttrClassName, fileNameNode,
							new NexusGroupData(1)));
					fileNameNode.addChildNode(new NexusTreeNode("axes", NexusExtractor.AttrClassName, fileNameNode,
							new NexusGroupData("time")));
					data.addAxis(name, "time", new NexusGroupData(timesCurrentAcq2), 1, 1, "s", false);
				}
				// add filename as an NXNote
				data.addFileNames(getName(), filenames);
				// data.addScanFileLink(getName(), filenames[filenames.length-1]);
				// must match the list of input and extra names
				data.setDoubleVals(new Double[] { new Double(filenames.length) });

				// FileRegistrarHelper.registerFiles(output);
			}

			if (firstCall) {
				INexusTree detTree = data.getDetTree(name);
				for (INexusTree item : nexusMetaDataForPixium1) {
					detTree.addChildNode(item);
				}
			}
			return data;
		}
	}

	@Override
	public void prepareForCollection() throws DeviceException {

		imagecounter = 0;
		scanpointnumber = 0;
	}

	private int imagecounter = 0;

	private String[] createFileName() throws Exception {
		Vector<String> output = new Vector<String>();
		String absolutePath = scanSaveFolder.getAbsolutePath() + File.separator;
		// cannot wait EPICS AD full file name update before collect next image - to support NDArray buffering
		String fileName = controller.getTiff().getFileName();
		fileName = fileName.trim();
		if ((int) getCollectionTime() > 1) {
			int i = 0;
			// handle multiple images per data scan point.
			for (i = 0; i < getCollectionTime(); i++) {
				output.add(String.format(controller.getTiff().getFileTemplate(), absolutePath, fileName, i
						+ imagecounter));
			}

			imagecounter = i + imagecounter;
		} else {
			// single image per scan data point
			output.add(String.format(controller.getTiff().getFileTemplate(), absolutePath, fileName, scanpointnumber));
		}
		scanpointnumber += 1;
		return output.toArray(new String[] {});
	}

	public boolean isReadAcquisitionTime() {
		return this.readAcquisitionTime;
	}

	@Override
	public void asynchronousMoveTo(Object collectionTime) throws DeviceException {
		int numberOfImages = Integer.parseInt(collectionTime.toString());
		try {
			acquire(numberOfImages);
		} catch (Exception e) {
			logger.error("Failed to acquire " + numberOfImages + " images fro detector {}", getName(), e);
			throw new DeviceException("Failed to acquire " + numberOfImages + " images fro detector " + getName(), e);
		}
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		// The collection time in this case is the number of images to acquire at each point.
		this.collectionTime = collectionTime;
		try {
			if (!isCaptureControl()) {
				controller.setNumImages((int) Math.round(collectionTime));
			} else {
				if (!hdfFormat) {
					controller.setTiffNumCapture((int) Math.round(collectionTime));
				} else {
					controller.getHdf().setExtraDimSizeN((int) Math.round(collectionTime));
				}
			}
		} catch (Exception e) {
			throw new DeviceException("Pixium setCollectionTime failed with error", e);
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
			boolean firstTime = true;
			// check that the Acquisition mode is ok.
			if (controller.getAcquisitionMode().contains("NOT SET")) {
				throw new DeviceException("Pixium Mode not set!");
			}
			// ensure detector buffer writer completed before proceed
			while (isWriterBusy()) {
				if (firstTime) {
					logger.warn(
							"{}: buffer is still wrim2ting out data to disk from previous collection. please wait...",
							getName());
					print("the file writer in EPICS Area detector is busy. Please release it to proceed or abort this scan instead");
					firstTime = false;
				}
				Thread.sleep(100);
			}
			// reset detector
			resetAll();
			ScanInformation scanInfo = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
			List<String> detectorNames = new ArrayList<String>();
			for (String name : scanInfo.getDetectorNames()) {
				detectorNames.add(name);
			}
			if (detectorNames.contains("pixium")) {
				// to support multiple images per scan data point must reset output format for each scan
				outputformats.clear();
				outputformats.add("%s"); // define input name's output format
				if ((int) Math.round(collectionTime) > 1) {
					String[] extraNames = new String[(int) Math.round(collectionTime)];
					for (int i = 0; i < (int) Math.round(collectionTime); i++) {
						extraNames[i] = "image_" + i;
						outputformats.add("%s");
					}
					setExtraNames(extraNames);
				} else {
					setExtraNames(new String[] {});
				}
				setOutputFormat(outputformats.toArray(new String[outputformats.size()]));
				logger.debug("output format size is {}", outputformats.size());
			}
			initialiseFilePath();
			setFileName();
			ScanInformation scaninfo = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
			if (!isCaptureControl()) {
				controller.resetAndEnableCameraControl();
				if (hdfFormat) {
					controller.setScanDimensions(scaninfo.getDimensions());
					controller.startRecording();
				} else {
					// setTifPluginToAreaDetector();
					controller.getTiff().setFileNumber(0);
					controller.getTiff().setNumCapture(
							totalNumberImages2Collect(scaninfo.getDimensions()) * (int) getCollectionTime());
					controller.getTiff().startCapture();
				}
			} else {
				controller.resetAndStartFilesRecording();
				if (hdfFormat) {
					throw new IllegalStateException(
							"HDF format only available for acquiring mode control, not supported for capturing mode control.");
				}
				controller.getTiff().setFileNumber(0);
				controller.getTiff().setNumCapture((int) (getCollectionTime()));
			}
			// set file path, file name and file number
			scanpointnumber = 0;
			scanpointindex=0;
			// scanRunning = true;
		} catch (Exception e) {
			logger.error("AtScanStart failed with error :", e);
			throw new DeviceException("AtScanStart failed with error :", e);
		} finally {
			logger.debug("Ending Pixium.AtScanStart");
		}
	}

	private int totalNumberImages2Collect(int[] dimensions) {
		int total = 1;
		for (int i = 0; i < dimensions.length; i++) {
			total *= dimensions[i];
		}
		return total;
	}

	private void initialiseFilePath() throws IOException, Exception {
		scanSaveFolder = createMainFileStructure();
		setFilePath(scanSaveFolder);

		// Initialise the file template
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
	public void atPointStart() throws DeviceException {
	}

	@Override
	public void atPointEnd() throws DeviceException {
		// pointNumber++;
	}

	@Override
	public void stop() throws DeviceException {
		super.stop();
		try {
			if (!isCaptureControl()) {
				stopAcquire();
			} else {
				stopCapture();
			}
		} catch (Exception e) {
			throw new DeviceException("Cannot stop pixium detector", e);
		}
	}

	@Override
	public void stopAcquire() throws Exception {
		controller.stopAcquiring();
	}

	@Override
	public void stopCapture() throws Exception {
		controller.stopTiffCapture();
	}

	/**
	 * change detector logical mode and return status message following the change The logical mode you changed to must
	 * be exist.
	 *
	 * @param logicalMode
	 * @param offsetreferenceNumber
	 * @return status message of the detector.
	 * @throws Exception
	 */
	@Override
	public String setMode(int logicalMode, int offsetreferenceNumber) throws Exception {
		controller.setLogicalMode(logicalMode);
		Thread.sleep(100);
		controller.setOffsetReferenceNumber(offsetreferenceNumber);
		Thread.sleep(100);
		controller.changeMode();
		while (getStatus() == Detector.BUSY) {
			Thread.sleep(100);
		}
		return controller.getAreaDetector().getStatusMessage_RBV();
	}

	/**
	 * start Offset Calibration process. This must be done whenever the detector mode has been changed.
	 *
	 * @throws Exception
	 */
	@Override
	public void startOffsetCalibration() throws Exception {
		controller.startOffsetCalibration();
	}

	@Override
	public void startOffsetCalibration(double timeout) throws Exception {
		controller.startOffsetCalibration(timeout);
	}

	/**
	 * connect EPICS IOC to detector hardware.
	 *
	 * @throws Exception
	 */
	@Override
	public void connect() throws Exception {
		controller.connect();
	}

	/**
	 * disconnect EPICS IOC to detector hardware
	 *
	 * @throws Exception
	 */
	@Override
	public void disconnect() throws Exception {
		controller.disconnect();
	}

	@Override
	public void resetAll() throws Exception {
		controller.resetAll();
	}

	File metadataFile;
	Vector<String> imageFiles = new Vector<String>();

	@Override
	public void acquire(final int numberOfImage) throws Exception {
		try {
			beforeAcquire();
			if (!isCaptureControl()) {
				controller.setNumImages(numberOfImage);
			} else {
				controller.setTiffNumCapture(numberOfImage);
			}
			final File metadatafile = this.metadataFile;
			//
			setTifPluginToAreaDetector();

			Thread acquire = new Thread(new Runnable() {

				@Override
				public void run() {
					boolean firstFrame = true;
					try {
						FileWriter fileWriter = new FileWriter(metadatafile);
						int imageCounter = 0;
						int lastImageCounter = -1;
						NDFile fullFrameSaver = controller.getTiff();
						String format = fullFrameSaver.getFileTemplate_RBV();
						String filePath = fullFrameSaver.getFilePath() + File.separator;
						String filename = fullFrameSaver.getFileName();
						String fullFilename = null;
						String timeStamp;
						double period = controller.getAcquirePeriod();
						print("Acquisition period is " + period);

						collectData();
						int initialImageCounter = controller.getArrayCounter();
						while (imageCounter < numberOfImage) {
							if (firstFrame) {
								Thread.sleep((int) (period * 1000));
								firstFrame = false;
							}

							// logger.debug("image counter {}, last Image Counter {}", imageCounter, lastImageCounter);
							if (imageCounter > lastImageCounter) {
								// full filename PV update too slow which give wrong info.
								// fullFilename = fullFrameSaver.getFullFileName_RBV();
								fullFilename = String.format(format, filePath, filename,
										fullFrameSaver.getFileNumber_RBV());
								double epoch = fullFrameSaver.getPluginBase().getTimeStamp_RBV();
								timeStamp = getTimeStamp(epoch);
								String filename1 = windows2LinuxFilename(fullFilename);
								imageFiles.add(filename1);
								print(imageCounter + "\t" + timeStamp + "\t" + filename1);
								lastImageCounter = imageCounter;
								fileWriter.write(imageCounter + "\t" + timeStamp + "\t" + filename1 + "\n");
							}
							if (!isCaptureControl()) {
								imageCounter = controller.getArrayCounter() - initialImageCounter;
							} else {
								imageCounter = fullFrameSaver.getNumCaptured_RBV();
							}
							// print("image counter = "+imageCounter+"; last Image counter = "+lastImageCounter +
							// "; initial image counter = "+initialImageCounter);
						}
						print("Pixium metatdata is saved to " + metadatafile.getAbsolutePath());
						fileWriter.close();
						print("Acquiring completed");
					} catch (Exception e) {
						logger.error("Pixium acquire failed with error", e);
					}
				}
			});
			acquire.start();
			afterAcquire();
		} catch (CAException e) {
			logger.error("Pixium acquire failed with error", e);
		}
	}

	private void setTifPluginToAreaDetector() throws Exception {
		controller.getTiff().getPluginBase().setNDArrayPort(pixCamPortName);
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return controller.getAcquirePeriod();
	}

	private String getTimeStamp(double epoch) {
		Date date = new Date((long) ((epoch + UnixEpochDifferenceFromEPICS) * 1000));
		SimpleDateFormat simpleDatef = new SimpleDateFormat("dd/MM/yy hh:mm:ss.SSS");
		return simpleDatef.format(date);
	}

	/**
	 * processes after collected data: <li>Archival of data collected</li> <li>Display the last image</li>
	 */
	public void afterAcquire() {
		FileRegistrarHelper.registerFiles(imageFiles);
		// TODO call stop?
		// stop();
		// Ensure GDA reset cache detector status as stop is not called after fast collection or callback failed.
		if (getStatus() != Detector.IDLE) {
			if (!isCaptureControl()) {
				controller.getAreaDetector().setStatus(Detector.IDLE);
			} else {
				if (hdfFormat) {
					controller.getHdf().getFile().setStatus(Detector.IDLE);
				} else {
					controller.getTiff().setStatus(Detector.IDLE);
				}
			}
		}
	}

	/**
	 * processing to be done before starting acquire data from detector - create data storage parameters and metadata
	 * file
	 *
	 * @throws Exception
	 * @throws IOException
	 * @throws DeviceException
	 */
	public void beforeAcquire() throws Exception, IOException, DeviceException {
		imageFiles.clear();
		// check that the Acquisition mode is ok.
		if (controller.getAcquisitionMode().contains("NOT SET")) {
			throw new DeviceException("Pixium Mode not set!");
		}
		// if (getStatus() == Detector.BUSY) {
		// logger.warn("Detector {} is busy, scan cannot be started", getName());
		// throw new DeviceException("Detector " + getName() +" is busy, scan can not be started.");
		// }
		// as outside of any scan we need to increment file number here.
		NumTracker nt = new NumTracker(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME));
		long filenamuber = nt.incrementNumber();
		// set up file path in EPICS
		// pointNumber = 0;

		if (!isCaptureControl()) {
			controller.resetAndEnableCameraControl();
		} else {
			controller.resetAndStartFilesRecording();
		}

		// set file path, file name, and initialize file number
		initialiseFilePath();
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print(
					"Saving data in directory: " + scanSaveFolder.getAbsolutePath());
		}
		// atPointStart();
		// create metadata file to hold time stamp info
		metadataFile = new File(scanSaveFolder.getParent() + File.separator + filenamuber + ".dat");
		String filename = metadataFile.getAbsolutePath();
		imageFiles.add(filename);
		scanpointnumber = 0;
	}

	public void syncDetectorStatusWithEPICS() throws Exception {
		if (!isCaptureControl()) {
			controller.getAreaDetector().getEPICSStatus();
		} else {
			controller.getTiff().getEPICSStatus();
		}
	}

	private String windows2LinuxFilename(String filename) {
		String filePathString;
		if (localDataStore) {
			filePathString = localDataStoreWindows2LinuxFilePath.getFilePathString(filename);
		} else {
			filePathString = nonLocalDataStoreWindows2LinuxFilePath.getFilePathString(filename);
		}

		return filePathString;
	}

	/**
	 *
	 */
	private static class Windows2LinuxFilePath {
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

	/**
	 * Helper methods to create data directory using GDA file number tracking system. It uses the current file number to
	 * create a data directory for EPICS to save images to. Important: it is the caller's responsibility to ensure the
	 * current file number is not already exist to avoid data over-written.
	 *
	 * @return directory
	 * @throws IOException
	 */
	public File createMainFileStructure() throws IOException {

		// set up the filename which will be the base directory for data to be saved to
		File path = new File(PathConstructor.createFromDefaultProperty());
		NumTracker nt = new NumTracker(LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME));
		String filenumber = Long.toString(nt.getCurrentFileNumber()); // scan already increments file number
		File scanFolder = new File(path, filenumber);
		if (!scanFolder.isDirectory()) {
			scanFolder.mkdir();
		}
		if (!scanFolder.canWrite()) {
			scanFolder.setWritable(true);
		}

		return scanFolder;
	}

	/**
	 * manage the file path mapping between Windows (where the EPICS IOC is running) and Linux (Where data is to be
	 * stored).
	 *
	 * @param filePath
	 * @throws Exception
	 */
	public void setFilePath(File filePath) throws Exception {

		String filePathString = null;
		if (localDataStore) {
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
		if (localDataStore) {
			filePathString = filePath.replace(getLocalDataStoreWindows2LinuxFilePath().getWindowsPath(),
					getLocalDataStoreWindows2LinuxFilePath().getLinuxPath());
		} else {
			filePathString = filePath.replace(getNonLocalDataStoreWindows2LinuxFilePath().getWindowsPath(),
					getNonLocalDataStoreWindows2LinuxFilePath().getLinuxPath());
		}
		return filePathString;
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	/**
	 * @return Returns the pixiumController.
	 */
	public IPixiumController getController() {
		return controller;
	}

	/**
	 * @param pixiumController
	 *            The pixiumController to set.
	 */
	public void setController(IPixiumController pixiumController) {
		this.controller = pixiumController;
	}

	@Override
	public boolean isCaptureControl() {
		return captureControl;
	}

	@Override
	public void setCaptureControl(boolean captureControl) {
		this.captureControl = captureControl;
	}

	@Override
	public boolean isLocalDataStore() {
		return localDataStore;
	}

	@Override
	public void setLocalDataStore(boolean localDataStore) {
		this.localDataStore = localDataStore;
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

	public boolean isHdfFormat() {
		return hdfFormat;
	}

	public void setHdfFormat(boolean hdfFormat) {
		this.hdfFormat = hdfFormat;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Pixium";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return detectorID;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Flat Panel detector";
	}

	/**
	 * @param detectorID
	 *            The detectorID to set.
	 */
	public void setDetectorID(String detectorID) {
		this.detectorID = detectorID;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		try {
			return new int[] { controller.getAreaDetector().getArraySizeX_RBV(),
					controller.getAreaDetector().getSizeY_RBV() };
		} catch (Exception e) {
			logger.error("Pixium {} failed to get image diamension", getName(), e);
			throw new DeviceException("Pixium failed to get image diamension", e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (detectorID == null) {
			throw new IllegalArgumentException("'detectorID' needs to be set");
		}

		if (controller == null) {
			throw new IllegalArgumentException("'pixiumController' needs to be set");
		}
	}

	/**
	 * check if file writer is still busy
	 *
	 * @return true - busy.
	 * @throws Exception
	 */
	public boolean isWriterBusy() throws Exception {
		if (hdfFormat) {
			return controller.getHdf().getFile().getWriteFile_RBV() == 1; // get buffer writer state from HDF
		}
		return controller.getTiff().getWriteFile_RBV() == 1; // get buffer writer state from tif
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

	public void setReadAcquisitionTime(boolean readAcquisitionTime) {
		this.readAcquisitionTime = readAcquisitionTime;
	}

	public boolean isReadAcquisitionPeriod() {
		return readAcquisitionPeriod;
	}

	public void setReadAcquisitionPeriod(boolean readAcquisitionPeriod) {
		this.readAcquisitionPeriod = readAcquisitionPeriod;
	}

	public boolean isReadFilepath() {
		return readFilepath;
	}

	public void setReadFilepath(boolean readFilepath) {
		this.readFilepath = readFilepath;
	}

	public void setPixCamPortName(String pixCamPortName) {
		this.pixCamPortName = pixCamPortName;
	}
}
