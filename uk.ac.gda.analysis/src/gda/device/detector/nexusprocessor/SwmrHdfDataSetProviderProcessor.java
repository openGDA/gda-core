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
import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.dataset.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.swmr.SwmrFileReader;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;

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
public class SwmrHdfDataSetProviderProcessor extends NexusProviderDatasetProcessor {

	private static final Logger logger = LoggerFactory.getLogger(SwmrHdfDataSetProviderProcessor.class);
	private static final int[] SINGLE_DATASET_STEP = new int[] {1, 1, 1};

	/**	This is the name of the dataset in the detector's Nexus tree
	 * that contains the associated frame number with the current point.
	 * <p>
	 * This string should be used in any detector classes that write NexusTreeProvider
	 * and would like to use this processor.
	 */
	public static final String FRAME_NO_DATASET_NAME = "frameNo";

	private String hdfDataEntry;
	private String hdfFilePath;
	private SwmrFileReader swmrReader;
	private int detectorHeight;
	private int detectorWidth;
	private int[] detectorDatasetShape;


	public SwmrHdfDataSetProviderProcessor(String detName, String dataName, String className,
			List<DataSetProcessor> processors, DatasetCreator datasetCreator) {
		super(detName, dataName, className, processors, datasetCreator);
	}

	@Override
	public GDANexusDetectorData process(GDANexusDetectorData nexusTreeProvider) throws Exception {
		if (!isEnabled()) {
			return null;
		}

		// First point
		if (hdfFilePath == null) {
			NexusGroupData dataFileGroup = nexusTreeProvider.getData(getDetName(), "data", NexusExtractor.ExternalSDSLink);
			setDatafileParameters(dataFileGroup);
			openFile();
		}

		NexusGroupData frameData = nexusTreeProvider.getData(getDetName(), FRAME_NO_DATASET_NAME, NexusExtractor.SDSClassName);
		if (frameData == null) {
			throw new IllegalArgumentException("No frame data found for detName: " + getDetName());
		}
		int frameNo = frameData.toDataset().getInt();
		Dataset dataset = readDatasetFromFile(frameNo);

	    return getProcessors().stream()
	            .filter(DataSetProcessor::isEnabled)
	            .map(processor -> processDataset(processor, dataset))
	            .reduce(new NXDetectorData(), GDANexusDetectorData::mergeIn);
	}

	/**
	 * Wrapper for {@link DataSetProcessor#process(String, String, Dataset)} to convert
	 * Exception into a RuntimeException
	 */
	private GDANexusDetectorData processDataset(DataSetProcessor processor, Dataset dataset) {
		try {
			return processor.process(getDetName(), getDataName(), dataset);
		} catch (Exception e) {
			throw new IllegalStateException("Error from dataset processor: " + processor.getName(), e);
		}
	}

	private void openFile() throws ScanFileHolderException {
		swmrReader = new SwmrFileReader();
		swmrReader.openFile(hdfFilePath);
		swmrReader.addDatasetToRead(getDetName(), hdfDataEntry);
	}

	private void setDatafileParameters(NexusGroupData dataFileGroup) throws URISyntaxException {
		String hdfUri = dataFileGroup.toDataset().getString();
		URI myUri = new URI(hdfUri);
		hdfFilePath = Paths.get(myUri.getPath()).toString();
		hdfDataEntry = myUri.getFragment();
	}

	/**
	 * Read the specified frame number from the dataset. Note that this starts at 1 for the first frame.
	 * <p>
	 * This method is synchronized as the thread safety of the underlying hdf Java code and native
	 * libraries is not known.
	 */
	private synchronized Dataset readDatasetFromFile(int frameNo) throws InterruptedException, NexusException {
		while (swmrReader.getNumAvailableFrames() < frameNo) {
			Thread.sleep(10);
		}

		List<Dataset> datasets = swmrReader.readDatasets(new int[] { frameNo - 1, 0, 0 }, detectorDatasetShape,
				SINGLE_DATASET_STEP);
		return datasets.get(0).squeeze();
	}

	public void closeFile() {
		try {
			if (swmrReader != null) {
				swmrReader.releaseFile();
			}
		} catch (ScanFileHolderException e) {
			logger.error("Error closing file: " + hdfFilePath, e);
		} finally {
			hdfDataEntry = null;
			hdfFilePath = null;
		}
	}

	@Override
	public void atScanStart() {
		getProcessors().forEach(DataSetProcessor::atScanStart);
	}

	@Override
	public void stop() {
		getProcessors().forEach(DataSetProcessor::stop);
		closeFile();
	}

	@Override
	public void atScanEnd() {
		getProcessors().forEach(DataSetProcessor::atScanEnd);
		closeFile();
	}

	public int getDetectorHeight() {
		return detectorHeight;
	}

	public void setDetectorHeight(int detectorHeight) {
		this.detectorHeight = detectorHeight;
		detectorDatasetShape = new int[] {1, this.detectorHeight, this.detectorWidth};
	}

	public int getDetectorWidth() {
		return detectorWidth;
	}

	public void setDetectorWidth(int detectorWidth) {
		this.detectorWidth = detectorWidth;
		detectorDatasetShape = new int[] {1, this.detectorHeight, this.detectorWidth};
	}

}
