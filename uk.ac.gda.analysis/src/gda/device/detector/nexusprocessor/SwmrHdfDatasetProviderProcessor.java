/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.device.detector.nexusprocessor;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.dataset.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.swmr.SwmrFileReader;
import gda.device.detector.GDANexusDetectorData;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Dataset provider which reads a dataset from a SWMR hdf5 file. The nexus processor uses a
 * {@link SwmrFileReader} to actually obtain the dataset from the file.
 * <p>
 * The file is opened using the file path which is provided for the first point only by
 * the e.g. ADDetector position callable.
 * <p>
 * The frame number to read is identified by the {@code frameNo} dataset in the
 * {@link GDANexusDetectorData} passed in from the detector.
 */
public class SwmrHdfDatasetProviderProcessor extends NexusProviderDatasetProcessor {

	private static final Logger logger = LoggerFactory.getLogger(SwmrHdfDatasetProviderProcessor.class);
	private static final int[] SINGLE_FRAME_STEP = new int[] {1, 1, 1};
	private static final int[] SINGLE_UID_STEP = new int[] {1};

	/**	This is the name of the dataset in the detector's Nexus tree
	 * that contains the associated frame number with the current point.
	 * <p>
	 * This string should be used in any detector classes that write NexusTreeProvider
	 * and would like to use this processor.
	 */
	public static final String FRAME_NO_DATASET_NAME = "frameNo";
	private int frameNo = 0;

	private String hdfDataEntry;
	private String hdfFilePath;
	private SwmrFileReader swmrReader;
	private int[] detectorDatasetShape;
	private int numberScanPoints;
	private boolean useUidDataset;
	/** Name of the UID Dataset	 */
	private String uidName = "uid";
	private List<DatasetProcessor> enabledBeforeScan = new ArrayList<>();


	public SwmrHdfDatasetProviderProcessor(String detName, String dataName, String className,
			List<DatasetProcessor> processors, DatasetCreator datasetCreator) {
		super(detName, dataName, className, processors, datasetCreator);
	}

	/**
	 * No argument constructor for Spring
	 */
	public SwmrHdfDatasetProviderProcessor() {
		super();
	}

	@Override
	protected Dataset extractDataset(GDANexusDetectorData nexusTreeProvider) throws Exception {
		ensureFileOpen(nexusTreeProvider);
		NexusGroupData frameData = nexusTreeProvider.getData(getDetName(), FRAME_NO_DATASET_NAME, NexusExtractor.SDSClassName);
		if (frameData == null) {
			if (frameNo == 0) {
				logger.info("Using internal frame count as no frame count data ({}) found for detName: {}",
							FRAME_NO_DATASET_NAME, getDetName());
			}
			frameNo++;
		} else {
			frameNo = frameData.toDataset().getInt();
		}
		return readDatasetFromFile(frameNo);
	}

	private synchronized void ensureFileOpen(GDANexusDetectorData nexusTreeProvider) throws URISyntaxException, ScanFileHolderException, NexusException {
		if (hdfFilePath == null) {
			// First point                         Should we use getDataName() here vvvv ?
			NexusGroupData dataFileGroup = nexusTreeProvider.getData(getDetName(), "data", NexusExtractor.ExternalSDSLink);
			setDatafileParameters(dataFileGroup);
			openFile();
		}
	}

	private void openFile() throws ScanFileHolderException {
		swmrReader = new SwmrFileReader();
		swmrReader.openFile(hdfFilePath);
		swmrReader.addDatasetToRead(getDetName(), hdfDataEntry);
		if (useUidDataset) {
			swmrReader.addDatasetToRead(getDetName() + uidName, uidName);
		}
		frameNo = 0;
	}

	private void setDatafileParameters(NexusGroupData dataFileGroup) throws URISyntaxException, NexusException {
		String hdfUri = dataFileGroup.toDataset().getString();
		URI myUri = new URI(hdfUri);
		hdfFilePath = Paths.get(myUri.getPath()).toString();
		hdfDataEntry = "/" + myUri.getFragment();
		// Read the frame size from the file using the NexusFile interface (as is lightweight)
		try (var dFile = ServiceProvider.getService(INexusFileFactory.class).newNexusFile(hdfFilePath, true)) {
			dFile.openToRead();
			var dShape = dFile.getData(hdfDataEntry).getDataset().getShape();
			// Assume 3d shape
			if (dShape.length != 3) {
				throw new NexusException("Detector data shape is not 3d");
			}
			detectorDatasetShape = new int[] {1, dShape[1], dShape[2]};
			logger.debug("Set detector data read shape to: {}", Arrays.toString(detectorDatasetShape));
		}

	}

	/**
	 * Read the specified frame number from the dataset. Note that this starts at 1 for the first frame.
	 * <p>
	 * This method is synchronized as the thread safety of the underlying hdf Java code and native
	 * libraries is not known.
	 */
	private synchronized Dataset readDatasetFromFile(int frameNo) throws InterruptedException, NexusException {
		if (useUidDataset) {
			waitOnUidDataset(frameNo);
		} else {
			waitOnExpandingFrameDataset(frameNo);
		}
		Dataset dataset = swmrReader.readDataset(hdfDataEntry, new int[] { frameNo - 1, 0, 0 }, detectorDatasetShape,
				SINGLE_FRAME_STEP);
		return dataset.squeeze();
	}

	/**
	 * Block until the desired frame number is available for reading
	 * in the file.
	 * <p>
	 * Poll the frame dataset until
	 * it contains the expected number of frames
	 * @throws InterruptedException
	 * @throws NexusException
	 */
	private void waitOnExpandingFrameDataset(int frameNo)  throws InterruptedException, NexusException{
		try {
			while (swmrReader.getNumAvailableFrames() < frameNo) {
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			logger.error("Interrupted whilst waiting for frames - wanted: {}, available: {}",
					swmrReader.getNumAvailableFrames(), frameNo);
			throw e;
		}
	}

	/**
	 * Block until the desired frame number is available for reading
	 * in the file.
	 * <p>
	 * Read the uid dataset and wait until the expected uid for the frame has been written
	 * @throws InterruptedException
	 * @throws NexusException
	 */
	private void waitOnUidDataset(int frameNo)  throws InterruptedException, NexusException{
		final int[] uidShape = new int[] { numberScanPoints };
		Dataset uidDataset;
		try {
			do {
				swmrReader.getNumAvailableFrames(); // This is just to refresh the file
				uidDataset = swmrReader.readDataset(uidName, new int[] { 0 }, uidShape, SINGLE_UID_STEP);
				Thread.sleep(100);
			} while (uidDataset.getInt(frameNo - 1) != frameNo);
		} catch (InterruptedException e) {
			logger.error("Interrupted whilst waiting for uid - waiting for: {}", frameNo);
			throw e;
		}
	}

	public void closeFile() {
		try {
			if (swmrReader != null) {
				swmrReader.releaseFile();
			}
		} catch (ScanFileHolderException e) {
			logger.error("Error closing file: {}", hdfFilePath, e);
		} finally {
			hdfDataEntry = null;
			hdfFilePath = null;
		}
	}

	@Override
	public void atScanStart() {
		closeFile();
		int[] scanDim = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getDimensions();
		if(scanDim.length > 1) {
			InterfaceProvider.getTerminalPrinter().print("Processing disabled for multi dimensional scans.");
			getEnabledProcessors().forEach(proc -> enabledBeforeScan.add(proc));
			getEnabledProcessors().forEach(proc -> proc.setEnable(false));
		} else {
			numberScanPoints = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getNumberOfPoints();
			getEnabledProcessors().forEach(DatasetProcessor::atScanStart);
		}
	}

	@Override
	public void stop() {
		getEnabledProcessors().forEach(DatasetProcessor::stop);
		resetProcessors();
		closeFile();
	}

	@Override
	public void atScanEnd() {
		getEnabledProcessors().forEach(DatasetProcessor::atScanEnd);
		resetProcessors();
		closeFile();
	}

	private void resetProcessors() {
		enabledBeforeScan.forEach(entry -> entry.setEnable(true));
		enabledBeforeScan.clear();
	}

	public boolean isUseUidDataset() {
		return useUidDataset;
	}

	public void setUseUidDataset(boolean useUidDataset) {
		this.useUidDataset = useUidDataset;
	}

	public String getUidName() {
		return uidName;
	}

	public void setUidName(String uidName) {
		this.uidName = uidName;
	}

}
