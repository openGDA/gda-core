/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package gda.device.panda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.swmr.SwmrFileReader;
import gda.device.DeviceException;

public class PandaHdfReader {
	private static final Logger logger = LoggerFactory.getLogger(PandaHdfReader.class);
	private SwmrFileReader swmrFileReader;
	private List<String> dataNames = Collections.emptyList();
	private List<String> numberFormat = Collections.emptyList();
	private String dataFormatString = "%5.5g";

	private String filename = "";

	private double hdfPollIntervalSec = 0.2;
	private int hdfPollNumRetries = 20;

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public void connect() throws DeviceException {
		if (StringUtils.isEmpty(filename)) {
			logger.warn("Cannot open SWMR file - filename is empty!");
			return;
		}

		logger.debug("Opening file {}", filename);
		try {
			swmrFileReader = new SwmrFileReader();
			swmrFileReader.openFile(filename);
		} catch (ScanFileHolderException e) {
			throw new DeviceException("Problem opening Hdf file "+filename, e);
		}

		for(String name : dataNames) {
			swmrFileReader.addDatasetToRead(name, name);
		}
	}

	public void close() throws ScanFileHolderException {
		if (swmrFileReader != null && swmrFileReader.isFileOpen()) {
			swmrFileReader.releaseFile();
		}
	}

	/**
	 * Wait until specified number of frames is available to read from Hdf file. i.e. until
	 * shape[0] >= frameIndex. Waits in a loop in a loop for a few seconds until data is
	 * readable before continuing. ({@link #setHdfPollIntervalSec(double)} and {@link #setHdfPollNumRetries(int)}
	 * can be used to control the poll time interval and maximum number of attempts to be made).
	 *
	 * @param frameIndex
	 * @throws NexusException
	 */
	public void waitForFrames(int frameIndex) throws NexusException {
		boolean done = false;
		int numRetries = 0;
		while(numRetries < hdfPollNumRetries && !done) {
			logger.debug("Waiting {} seconds for {} frames of data to be available...", hdfPollIntervalSec, frameIndex+1);
			try {
				Thread.sleep((long)(1000*hdfPollIntervalSec));
			} catch (InterruptedException e) {
				logger.warn("Interruped waiting for Hdf data shape monitoring", e);
			}
			int numFrames = swmrFileReader.getNumAvailableFrames();
			done = numFrames > frameIndex;
			logger.debug("Num frames = {}", numFrames);
			numRetries++;
		}
	}

	/**
	 * Read single frame of data from hdf file.
	 * If the frame has not yet been written, number of frames available is determined from disc
	 * in a loop for a few seconds to wait for the data to be readable before continuing
	 * ({@link #setHdfPollIntervalSec(double)} and {@link #setHdfPollNumRetries(int)}
	 * can be used to control the poll time interval and maximum number of attempts to be made).
	 * If data is not available after waiting, exception is thrown.
	 *
	 * @param frameNumber to be read
	 * @return array of single value from each dataset in list set by {@link #setDataNames(List)}
	 * @throws NexusException if data cannot be read (not written to disc yet, or some other problem)
	 */
	public double[] readData(int frameNumber) throws NexusException {
		int frameIndex = frameNumber-1;
		int numFrames = swmrFileReader.getNumAvailableFrames();
		logger.debug("{} frames available to read", numFrames);

		// wait for a bit for data to be flushed
		if (frameIndex >= numFrames) {
			waitForFrames(frameIndex);
			numFrames = swmrFileReader.getNumAvailableFrames();
		}

		if (frameIndex >= numFrames) {
			String msg = String.format("PandaHdfReader cannot read frame %d from %s. Only %d frames are available", frameNumber, filename, numFrames);
			throw new NexusException(msg);
		}
		List<double[]> frameData = readData(frameIndex, frameIndex);
		//extract the first value from each array, make new array with
		return frameData.stream()
				.map(arr -> arr[0])
				.mapToDouble(d->d)
				.toArray();
	}

	/**
	 * Read datasets from Swmr file and return a List containing array of values for each one;
	 * Data from each dataset in datasetNames list is read from file.
	 *
	 * @param startFrame first frame to read
	 * @param endFrame last frame to read (inclusive)
	 * @return List<double[]>
	 * @throws NexusException
	 */
	public List<double[]> readData(int startFrame, int endFrame) throws NexusException {
		// loop over dataset names, get frames for each into array.
		logger.debug("Reading frames {}...{} from hdf file", startFrame, endFrame);

		List<double[]> allData = new ArrayList<>();
		for(String name : dataNames) {
			Dataset data = swmrFileReader.readDataset(name, new int[] {startFrame}, new int[] {endFrame-startFrame+1}, new int[] {1});
			allData.add(data.cast(DoubleDataset.class).getData());
		}
		return allData;
	}

	public List<String> getDataNames() {
		return dataNames;
	}

	public void setDataNames(List<String> dataNames) {
		this.dataNames = dataNames;
	}

	public String[] getOutputNames() {
		return dataNames.toArray(new String[] {});
	}

	public String[] getOutputFormat() {
		if (numberFormat.isEmpty() || numberFormat.size() != dataNames.size()) {
			return Collections.nCopies(dataNames.size(), dataFormatString).toArray(new String[] {});
		} else {
			return numberFormat.toArray(new String[] {});
		}
	}

	public double getHdfPollIntervalSec() {
		return hdfPollIntervalSec;
	}

	public void setHdfPollIntervalSec(double hdfPollIntervalSec) {
		this.hdfPollIntervalSec = hdfPollIntervalSec;
	}

	public int getHdfPollNumRetries() {
		return hdfPollNumRetries;
	}

	public void setHdfPollNumRetries(int hdfPollNumRetries) {
		this.hdfPollNumRetries = hdfPollNumRetries;
	}

	public String getDataFormatString() {
		return dataFormatString;
	}

	public void setDataFormatString(String dataFormatString) {
		this.dataFormatString = dataFormatString;
	}

}
