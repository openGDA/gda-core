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

package gda.device.detector.xmap;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DAServer;
import gda.device.detector.DetectorBase;
import gda.device.detector.NexusDetector;
import gda.device.detector.xmap.edxd.EDXDController.COLLECTION_MODES;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.device.detector.xmap.util.XmapBufferedHdf5FileLoader;
import gda.device.detector.xmap.util.XmapFileLoader;
import gda.device.detector.xmap.util.XmapNexusFileLoader;
import gda.epics.CAClient;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gov.aps.jca.CAException;

/**
 * For using Xia XMap within the ContinuousScan-style trajectory scans.
 *
 * @author rjw82
 *
 */
public class XmapBufferedDetector extends DetectorBase implements BufferedDetector, NexusDetector {
	private static final long serialVersionUID = -361735061750343662L;
	private NexusXmap xmap;
	private XmapFileLoader fileLoader;
	private static final Logger logger = LoggerFactory.getLogger(XmapBufferedDetector.class);
	protected ContinuousParameters continuousParameters = null;
	protected boolean isContinuousMode = false;
	private DAServer daServer;
	private EDXDMappingController controller;
	private boolean isSlave = true;
	private String daServerName;
	private int lastScanNumber = 0;
	private int lastRowNumber = -1;
	private String lastFileName = null;
	private boolean lastFileReadStatus = false;
	private String capturepv;
	private boolean deadTimeEnabled = true;
	private String defaultSubDirectory = "xmapData";
	private String qexafsEnergyName = "qexafs_energy";

	public NexusXmap getXmap() {
		return xmap;
	}

	public void setXmap(NexusXmap xmap) {
		this.xmap = xmap;
	}

	public String getDaServerName() {
		return daServerName;
	}

	public void setDaServerName(String daServerName) {
		this.daServerName = daServerName;
	}

	public EDXDMappingController getController() {
		return controller;
	}

	public void setController(EDXDMappingController controller) {
		this.controller = controller;
	}

	@Override
	public void clearMemory() throws DeviceException {
		xmap.clear();
	}

	@Override
	public ContinuousParameters getContinuousParameters() throws DeviceException {
		return continuousParameters;
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		if (daServer == null) {
			logger.debug("XmapBuffereddetector configure(): finding: " + daServerName);
			if ((daServer = (DAServer) Finder.getInstance().find(daServerName)) == null) {
				logger.error("XmapBufferedDetector.configure(): Server " + daServerName + " not found");
			}
		}
		setConfigured(true);
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		String fileName = null;
		try {
			fileName = controller.getHDFFileName();
		} catch (Exception e) {
			logger.error("Error getting HDF filename", e);
		}
		if (fileName != null && isStillWriting(fileName))
			return 0; // nothing available yet until file written and closed
		// wait for another second to file to be closed
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			logger.error("Error performing sleep", e);
		}
		// for Xmap, as data written to file and not accessible during data collection,
		// return the total number of frames only at the end of the scan i.e. file has been written
		try {
			int numFrames = controller.getHdf5().getNumCaptured_RBV();
			logger.debug("Number of captured frames = {}", numFrames);
			return numFrames;
		}catch(Exception e) {
			throw new DeviceException("Problem getting number of captured frames", e);
		}
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return isContinuousMode;
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		if (continuousParameters != null)
			return continuousParameters.getNumberDataPoints();
		return 9999;
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		setCollectionTime(continuousParameters.getTotalTime());
		Integer lastFrame = getNumberFrames() - 1;
		return readFrames(0, lastFrame);
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		String fileNameOnDls = "";
		try {
			if (lastFileName == null && !lastFileReadStatus) {
				lastFileName = this.controller.getHDFFileName();

				waitForFile();
				//added wed 31st jul 2013 as now need to stop before reading h5 file. don't know why this is the case
				xmap.stop();
				//sleep required for gda to recognise number of arrays has finalized.
//				Thread.sleep(1000);
				// change to linux format
				fileNameOnDls = XmapFileUtils.getDataDirectoryDirName(lastFileName);

				XmapFileUtils.waitForFileToBeReadable(fileNameOnDls);

				createFileLoader(fileNameOnDls);
				try {
					logger.info("Loading {}", fileNameOnDls);
					fileLoader.loadFile();
				} catch (Exception e) {
					// this could be an NFS cache problem, so wait and try once more after a few seconds
					logger.warn("Exception trying to read Xmap HDF5 file, so wait and try again...");
					Thread.sleep(5000);
					fileLoader = null;
					createFileLoader(fileNameOnDls);
					fileLoader.loadFile();  // let this throw its exception to surrounding catch
				}
				lastFileReadStatus = true;
			}

			int numOfPointsInFile = fileLoader.getNumberOfDataPoints();
			int numPointsToRead = finalFrame - startFrame + 1;

			if (numOfPointsInFile < numPointsToRead) {
				String msg = "Xmap data file "+ fileNameOnDls +  " only has " + numOfPointsInFile + " data point but expected at least " + numPointsToRead;
				logger.error(msg);
				throw new DeviceException( msg);
			}

			XmapNXDetectorDataCreator dataCreator = new XmapNXDetectorDataCreator(
					fileLoader, xmap.getVortexParameters().getDetectorList(),
					getExtraNames(), getOutputFormat(), getName(),
					isDeadTimeEnabled(), xmap.getEventProcessingTimes(),
					xmap.isSumAllElementData());
			NexusTreeProvider[] container = new NexusTreeProvider[numPointsToRead];
			for (int i = 0; i < numPointsToRead; i++) {
				container[i] = dataCreator.writeToNexusFile(i, fileLoader.getData(i));
			}
			return container;
		} catch (Exception e) {
			try {
				stop();
				controller.endRecording();
			} catch (Exception e1) {
				controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
				logger.error("Unable to end hdf5 capture", e1);
				throw new DeviceException("Unable to end hdf5 capture", e1);
			}
			controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
			throw new DeviceException("Unable to load file called " + fileNameOnDls, e);
		}
	}

	private void createFileLoader(String fileName) throws  Exception {
		if (controller.isBufferedArrayPort())
			fileLoader = new XmapBufferedHdf5FileLoader(fileName);
		else
			fileLoader = new XmapNexusFileLoader(fileName, getXmap().getNumberOfMca());
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		isContinuousMode = on;
		if (on)
			setupContinuousOperation();
		if (!isSlave) {
			if (on) {
				setTimeFrames();
			} else {
				switchOffExtTrigger();
			}
		}
	}

	private void switchOnExtTrigger() throws DeviceException {
		getDaServer().sendCommand("tfg setup-trig start ttl0");
	}

	private void switchOffExtTrigger() throws DeviceException {
		getDaServer().sendCommand("tfg setup-trig start"); // disables external triggering
	}

	private void setTimeFrames() throws DeviceException {
		switchOnExtTrigger();
		getDaServer().sendCommand("tfg setup-groups ext-start cycles 1");
		getDaServer().sendCommand(continuousParameters.getNumberDataPoints() + " 0.000001 0.00000001 0 0 0 8");
		getDaServer().sendCommand("-1 0 0 0 0 0 0");
		getDaServer().sendCommand("tfg arm");
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException {
		this.continuousParameters = parameters;
	}

	@Override
	public void collectData() throws DeviceException {
		// do nothing as collection triggered by hardware
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false; // no, as this returns data in the readFrames method
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		return collectionTime;
	}

	@Override
	public String getDescription() throws DeviceException {
		return null;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return null;
	}

	@Override
	public int getStatus() throws DeviceException {
		return xmap.getStatus();
	}

	@Override
	public void setCollectionTime(double time) throws DeviceException {
		this.collectionTime = time;
	}

	@Override
	public void atScanStart() throws DeviceException {
		stopAcq();
		controller.setCollectionMode(COLLECTION_MODES.MCA_MAPPING);
	}

	@Override
	public void atScanEnd() throws DeviceException {
		try {
			stop();
			controller.endRecording();
		} catch (Exception e) {
			controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
			logger.error("Unalble to end hdf5 capture", e);
			throw new DeviceException("Unalble to end hdf5 capture", e);
		}
		controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
		stopAcq();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		try {
			stop();
			controller.endRecording();
		} catch (Exception e) {
			controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
			logger.error("cannot set collection mode to mca spectra", e);
			throw new DeviceException("Unalble to end hdf5 capture", e);
		}
		controller.setCollectionMode(COLLECTION_MODES.MCA_SPECTRA);
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		lastFileName = null;
		lastFileReadStatus = false;
	}

	@Override
	public String[] getInputNames() {
		return xmap.getInputNames();
	}

	@Override
	public String[] getExtraNames() {
		return xmap.getExtraNames();
	}

	@Override
	public String[] getOutputFormat() {
		return xmap.getOutputFormat();
	}

	private void setupFilename() throws Exception {
		// filename prefix
		String beamline = "base";
		try {
			beamline = GDAMetadataProvider.getInstance().getMetadataValue("instrument", "gda.instrument", null);
		} catch (DeviceException e1) {
			// don't let an exception stop us here
			logger.warn("Cannot get instrument or gda.instrument property value");
		}
		controller.setFilenamePrefix(beamline);

		// scan number
		NumTracker runNumber = new NumTracker("scanbase_numtracker");
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
		dataDir = Paths.get(dataDir, defaultSubDirectory).toString();

		File scanSubDir = new File(dataDir);
		if (!scanSubDir.exists()) {
			boolean directoryExists = scanSubDir.mkdirs();
			if (!directoryExists) {
				throw new DeviceException("Failed to create temporary directory to place Xmap HDF5 files: " + dataDir);
			}

			// set 777 perms to ensure detector account
			Set<PosixFilePermission> perms = new HashSet<>();
			perms.add(PosixFilePermission.OWNER_READ);
			perms.add(PosixFilePermission.OWNER_WRITE);
			perms.add(PosixFilePermission.OWNER_EXECUTE);
			perms.add(PosixFilePermission.GROUP_READ);
			perms.add(PosixFilePermission.GROUP_WRITE);
			perms.add(PosixFilePermission.GROUP_EXECUTE);
			perms.add(PosixFilePermission.OTHERS_READ);
			perms.add(PosixFilePermission.OTHERS_WRITE);
			perms.add(PosixFilePermission.OTHERS_EXECUTE);
			Files.setPosixFilePermissions(scanSubDir.toPath(), perms);
		}
		dataDir = dataDir.replace("/dls/" + beamline.toLowerCase()+"/data/", "X:/");
		controller.setDirectory(dataDir);
	}

	private void setupContinuousOperation() throws DeviceException {
		try {
			setupFilename();
			controller.resetCounters();

			int numberOfPointsPerScan = continuousParameters.getNumberDataPoints();

			// This has a -1 for b18. This is because the B18 Position Compare does not send the first
			// pulse and so the first data point is always missed.
			if (LocalProperties.get("gda.factory.factoryName").equalsIgnoreCase("b18")){
				// Only subtract 1 when doing qexafs scans, not for all scans
				List<String> scnNames = Arrays.asList(InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getDetectorNames());
				if (scnNames != null && scnNames.contains(qexafsEnergyName)) {
					numberOfPointsPerScan -= 1;
				}
			}

			controller.setPixelsPerRun(numberOfPointsPerScan);
			controller.setAutoPixelsPerBuffer(true);

			controller.setHdfNumCapture(numberOfPointsPerScan);
			controller.startRecording();
		} catch (Exception e) {
			logger.error("Error occurred arming the xmap detector", e);
			throw new DeviceException("Error occurred arming the xmap detector", e);
		}
		xmap.clearAndStart();
	}

	@Override
	public void stop() throws DeviceException {
		xmap.stop();
		stopAcq();
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		// FIXME should this really be extending DetectorBase???
		return null; // cannot act as a regular detector, buffered detector only.
	}

	public boolean isStillWriting(String fileName) throws DeviceException {
		try {
			if (controller.getHDFFileName().equals(fileName)) {
				return controller.getCaptureStatus();
			}
		} catch (Exception e) {
			logger.error("CAnnot read the file capture status", e);
			throw new DeviceException("CAnnot read the file capture status", e);
		}
		return false;
	}

	public void waitForFile() throws InterruptedException {
		double timeoutMilliSeconds = 100000;
		double waitedSoFarMilliSeconds = 0;
		int waitTime = 1000;

		while (controller.getCaptureStatus() && waitedSoFarMilliSeconds <= timeoutMilliSeconds) {
			Thread.sleep(waitTime);
			waitedSoFarMilliSeconds += waitTime;
		}
	}


	public boolean isSlave() {
		return isSlave;
	}

	public void setSlave(boolean isSlave) {
		this.isSlave = isSlave;
	}

	public void setDaServer(DAServer daServer) {
		this.daServer = daServer;
	}

	public DAServer getDaServer() {
		return daServer;
	}

	public String getCapturepv() {
		return capturepv;
	}

	public void setCapturepv(String stoppv) {
		this.capturepv = stoppv;
	}

	public void stopAcq() {
		CAClient ca_client = new CAClient();
		try {
			if (capturepv != null)
				ca_client.caput(capturepv, 0);
		} catch (CAException e) {
			logger.error("Could not stop xmap capture", e);
		} catch (InterruptedException e) {
			logger.error("Could not stop xmap capture", e);
		}
	}

	public boolean isDeadTimeEnabled() {
		return deadTimeEnabled;
	}

	public void setDeadTimeEnabled(boolean deadTimeEnabled) {
		this.deadTimeEnabled = deadTimeEnabled;
	}

	public String getDefaultSubDirectory() {
		return defaultSubDirectory;
	}

	/**
	 * Set name of subdirectory in data directory where detector Hdf5 file should be written.
	 * @param defaultSubDirectory
	 */
	public void setDefaultSubDirectory(String defaultSubDirectory) {
		this.defaultSubDirectory = defaultSubDirectory;
	}
}
