/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress4;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import uk.ac.gda.devices.detector.xspress3.Xspress3Detector.XspressHelperMethods;

public class Xspress4OdinDetector extends Xspress4Detector {

	private static final Logger logger = LoggerFactory.getLogger(Xspress4OdinDetector.class);

	@Override
	public void configure() throws FactoryException {
		super.configure();
	}

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
	}

	@Override
	public void startDetector() throws DeviceException {
		if (isWriteHDF5Files()) {
			getController().startHdfWriter();
		}
		getController().startAcquire();
		waitForCounterToReset();
	}

	@Override
	public void setupNumFramesToCollect(int numberOfFramesToCollect) throws DeviceException {
		getController().setNumImages(numberOfFramesToCollect);
		//set the number of frames in the Odin writer (if using)
		if (isWriteHDF5Files()) {
			setupHdfWriter(numberOfFramesToCollect);
		}
	}

	@Override
	public String generateDefaultHdfFileName() {
		int scanNumber = XspressHelperMethods.getScanNumber();
		return getFilePrefix()+"_"+scanNumber;
	}

	private void waitForCounterToReset() throws DeviceException {
		int currentNumFrames = getController().getTotalFramesAvailable();
		if (currentNumFrames != 0) {
			logger.debug("Waiting for array counter to reset to zero");
			try {
				getController().waitForCounterToIncrement(currentNumFrames, 1000L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new DeviceException(e);
			}
		}
	}
	@Override
	public void atPointStart() throws DeviceException {
		super.atPointStart();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		if (!isWriteHDF5Files()) {
			return;
		}
		// get the name of the meta file *before* stopping the detector
		// (the PV is cleared when the detector is stopped!)
		String hdfFullName = getController().getHdfFullFileName();

		waitForFileWriter(); // hdf writer

		// Try to stop the detector (don't wait)
		getController().stopAcquire();

		// Add link to the metafile
		addLinkToNexusFile(hdfFullName, "#", FilenameUtils.getName(hdfFullName));

		// Add links to MCA data
		List<String> mcaFileNames = getFileListForMcaLinks(hdfFullName);
		addMcaLinks(mcaFileNames);
	}

	/**
	 * Add link to MCA data for each channel of the detector, with 4 channels worth of data per file h5 file.
	 * i.e. :
	 * <br>
	 * raw_mca0, raw_mca1, raw_mca2, raw_mca3 -> mca_0, mca_1, mca_2, mca_3 data in mcaFileNames[0], <br>
	 * raw_mca4, raw_mca5, raw_mca6, raw_mca7 -> mca_4, mca_5, mca_6, mca_7 data in mcaFileNames[1] <br>
	 * etc
	 *
	 * @param mcaFileNames - list of the MCA h5 files (each is assumed to contain data for 4 channels)
	 */
	private void addMcaLinks(List<String> mcaFileNames) {
		if (4*mcaFileNames.size() < getNumberOfElements()) {
			logger.warn("Not adding links to MCA data - only {} files given, but {} are needed", mcaFileNames.size(), 1+getNumberOfElements()/4);
			return;
		}
		for(int i=0; i<getNumberOfElements(); i++) {
			String hdfFileName = mcaFileNames.get(i/4);
			String linkNameInNexus = String.format("raw_mca%d", i);
			String targetNameInHdf = String.format("mca_%d", i);
			addLinkToNexusFile(hdfFileName, "#"+targetNameInHdf, linkNameInNexus);
		}
	}

	/**
	 * Create list of all the h5 files containing MCA data
	 *
	 * @param metaFileName
	 * @return list of h5 files (sorted into alphabetical order)
	 * @throws DeviceException
	 */
	private List<String> getFileListForMcaLinks(String metaFileName) throws DeviceException {
		String folderName = FilenameUtils.getFullPath(metaFileName);
		File folder = new File(folderName);
		if (!folder.exists()) {
			throw new DeviceException("Unable to find directory "+folderName);
		}

		// Create list of files written during the scan, not including the metawriter output
		String baseName = FilenameUtils.getName(metaFileName).replace("meta.h5", "");
		File[] listOfFiles = folder.listFiles( (dir, name) -> name.startsWith(baseName) && !name.contains("meta"));

		// return sorted list of files (order of list returned by Folder#listFiles' is not guaranteed).
		return Stream.of(listOfFiles).sorted().map(File::getPath).toList();
	}

	@Override
	public void acquireFrameAndWait(double collectionTimeMillis, double timeoutMillis) throws DeviceException {
		int numFramesBeforeAcquire = getController().getTotalFramesAvailable();
		logger.info("acquireFrameAndWait called. Current number of frames = {}", numFramesBeforeAcquire);
		// Don't call getControll().startAcquire() here - detector is already acquiring,
		// just need to send a 'software trigger' and wait for the counter to change
		getController().sendSoftwareTrigger();
		try {
			getController().waitForCounterToIncrement(numFramesBeforeAcquire, (long)timeoutMillis);
		} catch (InterruptedException e) {
			// Reset interrupt status
			Thread.currentThread().interrupt();
			logger.warn("Interrupted while waiting for acquire");
		}
		logger.info("Wait for acquire finished. {} frames available", getController().getTotalFramesAvailable());
		if (getController().getTotalFramesAvailable()==numFramesBeforeAcquire) {
			logger.warn("Acquire not finished after waiting for {} secs", timeoutMillis*0.001);
		}
	}

	@Override
	public double[][] getMCAData(double timeMillis) throws DeviceException {
		double mcaArrayData[][] = null;
		try {
			getController().prepareForMcaCollection(timeMillis);

			// Start detector
			getController().startAcquire();

			// Ensure frame counter has reset to zero
			waitForCounterToReset();

			acquireMcaData(timeMillis);

			mcaArrayData = getController().getMcaData();
		} catch (DeviceException e) {
			throw new DeviceException("Problem collecting MCA data : "+e.getMessage(), e);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Interrupted while reading MCA data from {}", getName(), e);
		}
		return mcaArrayData;
	}
}
