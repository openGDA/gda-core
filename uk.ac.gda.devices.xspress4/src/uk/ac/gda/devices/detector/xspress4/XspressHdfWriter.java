/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * Create an hdf file with various 1-dimensional datasets in it; datasets grow slowly.
 * Created with SWMR mode enabled so that SwmrFileReader *should* be able to open it
 * and retrieve latest datasets as they grow... for testing Xspress4BufferedDetector,
 * since although simulated area detector creates Hdf file, it is not similar enough to
 * Xspress hdf file to be useful for testing...
 */
public class XspressHdfWriter extends ScannableBase {

	private static final Logger logger = LoggerFactory.getLogger(XspressHdfWriter.class);

	private int compressionLevel = NexusFile.COMPRESSION_NONE;

	/** Path to group in hdf file where datasets will be written to */
	private String pathToGroup = "/entry/instrument/NDAttributes/";
	private String defaultDataNameFormat = "Chan%02dSca%d";
	private String default2dDataNameFormat = "scalar_chan%d";

	/** List of names of datasets to be written to hdf file */
	private List<String> datasetNames = new ArrayList<>();

	private List<ILazyWriteableDataset> lazyDatasets;

	private int numElements = 4;
	private int numScalers = 8;

	private String fileName = "/scratch/testfile.h5";
	private int numFrames = 50;
	private double timePerFrame = 0.5;
	private boolean write2dData = false;

	/** How many frames of data should be added to lazy datasets in each append and write operation */
	private int numFramesToAppendEachTime = 20;

	/** Number of frames written by {@link #writeHdfFile()} */
	private volatile int currentFrame = 0;

	private volatile boolean stopWriting = false;
	private volatile boolean writeInProgress = false;

	public XspressHdfWriter() {
		setOutputFormat(new String[]{});
		setInputNames(new String[]{});
		setDefaultNames();
	}

	@Override
	public void asynchronousMoveTo(Object position) {
		return; // do nothing
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return null;
	}

	/**
	 * Set the default list of dataset names to match Xspress3,4 NDAttribute datasets : <br>
	 *  Chan1Sca0, Chan1Sca1, Chan0Sca2...  Chan2Sca0, Chan2Sca1 etc... <p>
	 *  Ranges of the loops over channel and scaler number are set by {@link numChannels} and {@link numScalers}
	 *  (channel starts from 1, scalers start from 0).
	 */
	public void setDefaultNames() {
		datasetNames = new ArrayList<>();
		for(int channel=0; channel<numElements; channel++) {
			if (write2dData) {
				datasetNames.add(String.format(default2dDataNameFormat, channel));
			} else {
				for(int scaler=0; scaler<numScalers; scaler++) {
					datasetNames.add(String.format(defaultDataNameFormat, channel+1, scaler));
				}
			}
		}
	}

	/**
	 * Start writing data to hdf file asynchronously and return immediately.
	 * (calls {@link #writeHdfFile()} in background thread).
	 */
	public void writeData() {
		writeInProgress = true;
		Async.execute(() -> {
			try {
				writeHdfFile();
			} catch (NexusException | InterruptedException | DatasetException e) {
				logger.error("Problem writing data {}", e.getMessage(), e);
			}
		} );
	}

	/**
	 * Write data into hdf file. {@link #numFrames} of data will be written for each
	 * dataset name given in {@link #datasetNames}. Up to {@link #numFrames} frames of
	 * data will be written, with pause of {@link #timePerFrame} between each frame.
	 * Use {@link #getCurrentFrameNumber()} to monitor progress and call {@link #stop()}
	 * to writing to stop writing frames early.
	 * @see Also {@link #writeData()}
	 * @throws NexusException
	 * @throws InterruptedException
	 * @throws DatasetException
	 */
	public void writeHdfFile() throws NexusException, InterruptedException, DatasetException {
		logger.info("Writing hdf file to {}", fileName);
		File f = new File(fileName).getParentFile();
		if (!f.exists()) {
			f.mkdirs();
		}
		try (NexusFile hdfFile = NexusFileHDF5.createNexusFile(fileName, true)) {
			setupLazyDatasets(hdfFile);
			hdfFile.activateSwmrMode(); //activate SWMR mode *after* adding lazy datasets to the file
			writeInProgress = true;
			currentFrame = 0;
			while (currentFrame < numFrames && !stopWriting) {
				appendData(numFramesToAppendEachTime);
				hdfFile.flush();
				Thread.sleep((long) (1000 * timePerFrame));
			}
		} finally {
			writeInProgress = false;
			logger.info("Write finished - {}/{} frames written", currentFrame, numFrames);
		}
	}

	/** Add new lazy datasets in the Hdf file.
	 * An empty 1-dimensional unlimited dataset is created for each item in {@link #datasetNames}
	 * in group given by {@link #pathToGroup}.
	 * @param hdfFile
	 * @throws NexusException
	 */
	private void setupLazyDatasets(NexusFile hdfFile) throws NexusException {
		logger.info("Adding lazy datasets to hdf file");
		lazyDatasets = new ArrayList<>();
		int[] initialShape = {0};
		int[] maxShape = {ILazyWriteableDataset.UNLIMITED};
		int[] chunking = NexusUtils.estimateChunking( new int[] {numFrames}, Double.BYTES);

		if (write2dData) {
			logger.info("Using 2-dimensional datasets");
			initialShape = new int[] {0, numScalers};
			maxShape = new int [] {ILazyWriteableDataset.UNLIMITED, numScalers};
			chunking = NexusUtils.estimateChunking( new int[] {numFrames, numScalers}, Double.BYTES);
		}

		for(String datasetName : datasetNames) {
			logger.info("  {}", datasetName);
			// create new lazy dataset
			ILazyWriteableDataset lazyDataset = NexusUtils.createLazyWriteableDataset(datasetName, Double.class, initialShape, maxShape, null);
			lazyDataset.setName(datasetName);
			lazyDataset.setChunking(chunking);

			// add it to hdf file
			hdfFile.createData(pathToGroup, lazyDataset, compressionLevel, true);
			lazyDatasets.add(lazyDataset);
		}
	}


	/**
	 * Append up to 'numFramesToWrite' frames of data to lazy datasets.
	 * @param numFramesToWrite
	 * @throws DatasetException
	 */
	private void appendData(int numFramesToWrite) throws DatasetException {
		if (currentFrame + numFramesToAppendEachTime > numFrames) {
			numFramesToWrite = numFrames - currentFrame;
		}
		int[] start = {currentFrame};
		int[] stop = {currentFrame+numFramesToWrite};
		Dataset data = DatasetFactory.zeros(DoubleDataset.class, numFramesToWrite);

		if (write2dData) {
			logger.info("Using 2-dimensional datasets");
			start = new int[] {currentFrame, 0};
			stop = new int [] {currentFrame+numFramesToWrite, numScalers};
			data = DatasetFactory.zeros(DoubleDataset.class, numFramesToWrite, numScalers);
		}

		logger.debug("Appending values to lazy dataset : start = {}, stop = {}", Arrays.toString(start), Arrays.toString(stop));

		for(int i=0; i<lazyDatasets.size(); i++) {
			for(int j=0; j<numFramesToWrite; j++) {
				if (write2dData) {
					for(int k=0; k<stop[1]; k++) {
						data.set(i*1000+k*10+j, j, k);
					}
				} else {
					data.set(i*1000 + currentFrame, j);
				}
			}
			lazyDatasets.get(i).setSlice(null, data, start, stop, null);
		}
		currentFrame+=numFramesToWrite;
	}

	@Override
	public boolean isBusy() {
		return writeInProgress;
	}

	@Override
	public void stop() {
		stopWriting = true;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public double getTimePerFrame() {
		return timePerFrame;
	}

	public void setTimePerFrame(double timePerFrame) {
		this.timePerFrame = timePerFrame;
	}

	public int getNumFrames() {
		return numFrames;
	}

	public void setNumFrames(int numFrames) {
		this.numFrames = numFrames;
	}

	public int getCurrentFrameNumber() {
		return currentFrame;
	}

	public List<String> getDatasetNames() {
		return datasetNames;
	}

	public void setDatasetNames(List<String> datasetNames) {
		this.datasetNames = datasetNames;
	}

	public String getPathToGroup() {
		return pathToGroup;
	}

	public void setPathToGroup(String pathToGroup) {
		this.pathToGroup = pathToGroup;
	}

	public int getNumScalers() {
		return numScalers;
	}

	public void setNumScalers(int numScalers) {
		this.numScalers = numScalers;
	}

	public int getNumElements() {
		return numElements;
	}

	public void setNumElements(int numElements) {
		this.numElements = numElements;
	}

	public void setNumFramesToAppendEachTime(int numFramesToAppendEachTime) {
		this.numFramesToAppendEachTime = numFramesToAppendEachTime;
	}

	public String getDefaultDataNameFormat() {
		return defaultDataNameFormat;
	}

	public void setDefaultDataNameFormat(String defaultDataNameFormat) {
		this.defaultDataNameFormat = defaultDataNameFormat;
	}

	public boolean isWrite2dData() {
		return write2dData;
	}

	public void setWrite2dData(boolean write2dData) {
		this.write2dData = write2dData;
	}
}
