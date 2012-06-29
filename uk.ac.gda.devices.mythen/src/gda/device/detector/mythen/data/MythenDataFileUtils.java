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

package gda.device.detector.mythen.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

/**
 * Utility class for working with Mythen data files.
 */
public class MythenDataFileUtils {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(MythenDataFileUtils.class);

	/**
	 * Reads the specified Mythen processed data files.
	 * 
	 * @param filenames
	 *            the names of the files to read
	 * @return 3D double array of data
	 */
	public static double[][][] readMythenProcessedDataFiles(String filenames[]) {
		// 3D array of data; will be filled in by tasks (one task per file to be loaded)
		final double[][][] data = new double[filenames.length][][];

		// thread pool for loading files
		// 4 threads seems to give good results
		ExecutorService executor = Executors.newFixedThreadPool(4);

		// create and execute a task for each file to be loaded
		for (int i = 0; i < filenames.length; i++) {
			final int index = i;
			final String filename = filenames[i];
			Runnable r = new Runnable() {
				@Override
				public void run() {
					data[index] = readMythenProcessedDataFile(filename, false);
				}

			};
			executor.execute(r);
		}

		// wait until executor has shut down
		executor.shutdown();
		try {
			boolean terminated = executor.awaitTermination(1, TimeUnit.MINUTES);
			if (!terminated) {
				throw new Exception("Timed out waiting for files to load");
			}
		} catch (Exception e) {
			throw new RuntimeException("Unable to load data", e);
		}

		return data;
	}

	/**
	 * Reads a Mythen raw data file, consisting of two columns (channel number and count).
	 * 
	 * @param filename
	 *            the file to read
	 * @return a 2D double array of the data
	 */
	public static double[][] readMythenRawDataFile(String filename) {
		try {
			return loadByUsingStreamTokenizer(filename, FileType.RAW);
		} catch (IOException e) {
			throw new RuntimeException("Unable to load Mythen raw data file", e);
		}
	}

	/**
	 * Reads a Mythen processed data file, consisting of three columns (angle, count and error).
	 * 
	 * @param filename
	 *            the file to read
	 * @return a 2D double array of the data
	 */
	public static double[][] readMythenProcessedDataFile(String filename, boolean hasChannelInfo) {
		try {
			if(!hasChannelInfo)
				return loadByUsingStreamTokenizer(filename, FileType.PROCESSED);
			else
				return loadByUsingStreamTokenizer(filename, FileType.PROCESSED_WITH_CHANNELS);
		} catch (IOException e) {
			throw new RuntimeException("Unable to load Mythen processed data file", e);
		}
	}

	public enum FileType {
		RAW, PROCESSED, PROCESSED_WITH_CHANNELS
	}

	protected static double[][] loadByUsingSplit(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		return getDataFromReaderUsingSplit(br);
	}

	protected static double[][] loadByUsingStreamTokenizer(String filename, FileType type) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		return getDataFromReaderUsingStreamTokenizer(br, type);
	}

	protected static double[][] loadByReadingFileContentAndUsingSplit(String filename) throws IOException {
		String contents = readFileContents(filename);
		return getDataFromReaderUsingSplit(new BufferedReader(new StringReader(contents)));
	}

	protected static double[][] loadByReadingFileContentAndUsingStreamTokenizer(String filename, FileType type)
			throws IOException {
		String contents = readFileContents(filename);
		return getDataFromReaderUsingStreamTokenizer(new StringReader(contents), type);
	}

	protected static double[][] getDataFromReaderUsingSplit(BufferedReader r) throws IOException {
		try {
			List<double[]> data = new Vector<double[]>();
			String line = null;
			while ((line = r.readLine()) != null) {
				String[] tokens = line.split(" ");
				double[] values = new double[tokens.length];
				for (int i = 0; i < tokens.length; i++) {
					values[i] = Double.parseDouble(tokens[i]);
				}
				data.add(values);
			}
			return data.toArray(new double[data.size()][]);
		} finally {
			try {
				r.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	protected static double[][] getDataFromReaderUsingStreamTokenizer(Reader r, FileType type) throws IOException {
		try {
			List<double[]> data = new Vector<double[]>();
			StreamTokenizer st = new StreamTokenizer(r);
			while (st.nextToken() != StreamTokenizer.TT_EOF) {
				double angle = st.nval;
				st.nextToken();
				double count = st.nval;
				//st.nextToken();
				if (type == FileType.PROCESSED) {
					st.nextToken();
					double error = st.nval;
					data.add(new double[] {angle, count, error});
				}
				else if (type == FileType.PROCESSED_WITH_CHANNELS) {
					st.nextToken();
					double error = st.nval;
					double channel = st.nval;
					data.add(new double[] {angle, count, error, channel});
				}
				else {
					data.add(new double[] {angle, count});
				}
			}
			return data.toArray(new double[data.size()][]);
		} finally {
			try {
				r.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	protected static String readFileContents(String filename) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		return FileCopyUtils.copyToString(br);
	}

	public static double[][][] binMythenData(double[][][] input, double binSize) {

		// Find min/max angle
		double minAngle = Double.MAX_VALUE;
		double maxAngle = Double.MIN_VALUE;
		for (double[][] dataset : input) {
			for (double[] channel : dataset) {
				minAngle = Math.min(channel[0], minAngle);
				maxAngle = Math.max(channel[0], maxAngle);
			}
		}

		// Determine bins
		final int minBinNum = (int) Math.floor(minAngle / binSize);
		final int maxBinNum = (int) Math.ceil(maxAngle / binSize);
		final int numBins = maxBinNum - minBinNum + 1;

		final int numDatasets = input.length;

		// Create binned data array
		double[][][] binnedData = new double[numDatasets][][];

		// Create array for each dataset
		for (int dataset = 0; dataset < numDatasets; dataset++) {
			binnedData[dataset] = new double[numBins][];
		}

		// Within each dataset, create array for each bin
		// Iterate through bins first, then datasets, for efficiency (only calculate a bin's start angle once)
		for (int bin = 0; bin < numBins; bin++) {
			final double binStartAngle = (bin + minBinNum) * binSize;
			for (int dataset = 0; dataset < numDatasets; dataset++) {
				binnedData[dataset][bin] = new double[] { binStartAngle, 0 };
			}
		}

		// Bin data
		for (int dataset = 0; dataset < numDatasets; dataset++) {
			// logger.debug(String.format("Dataset %d of %d", dataset+1, numDatasets));
			final double[][] inputDataset = input[dataset];
			for (double[] channel : inputDataset) {
				final double angle = channel[0];
				final int binNumForAngle = (int) Math.floor(angle / binSize);
				final int binIndexForAngle = binNumForAngle - minBinNum;
				final double[] binForAngle = binnedData[dataset][binIndexForAngle];

				final double count = channel[1];
				// logger.debug(String.format("%.16f ÷ %.1f = %.16f => %d/%d\tcount=%.0f", angle, binSize,
				// (angle/binSize), binNumForAngle, binIndexForAngle, count));
				binForAngle[1] = Math.max(binForAngle[1], count);
			}
		}

		return binnedData;
	}

	/**
	 * Takes a collection of datasets and returns a subset of the data that includes channels within the specified angle
	 * range.
	 * 
	 * @param data
	 *            array of datasets; each dataset is a 2D array
	 * @param minAngle
	 *            minimum angle to include in result dataset
	 * @param maxAngle
	 *            maximum angle to include in result dataset
	 * @return data subset
	 */
	public static double[][][] getDataSubset(double[][][] data, double minAngle, double maxAngle) {
		final int numChannels = data[0].length;

		// Get single 1D array of angles
		// Assumes data has been binned, so that each dataset has the same angles
		double[] angles = new double[numChannels];
		for (int c = 0; c < numChannels; c++) {
			angles[c] = data[0][c][0];
		}

		// Work out min/max channel indices for desired min/max angles
		int minPos = getInclusiveIndexForMinIncludedAngle(angles, minAngle);
		int maxPos = getExclusiveIndexForMaxIncludedAngle(angles, maxAngle);
		int numChannelsInSubset = maxPos - minPos;

		// Build subset of data
		final int numDatasets = data.length;
		double[][][] output = new double[numDatasets][][];
		for (int d = 0; d < numDatasets; d++) {
			output[d] = new double[numChannelsInSubset][];
			for (int c = minPos; c < maxPos; c++) {
				output[d][c - minPos] = data[d][c];
			}
		}

		return output;
	}

	protected static int getInclusiveIndexForMinIncludedAngle(double[] angles, double minAngle) {
		int minPos = Arrays.binarySearch(angles, minAngle);
		return (minPos < 0) ? -(minPos + 1) : minPos;
	}

	protected static int getExclusiveIndexForMaxIncludedAngle(double[] angles, double maxAngle) {
		int maxPos = Arrays.binarySearch(angles, maxAngle);
		return (maxPos < 0) ? -(maxPos + 1) : maxPos + 1;
	}

	/**
	 * Saves the given processed data to the specified file.
	 * 
	 * @param data
	 *            the processed data - an array of (angle, count, error) arrays
	 * @param file
	 *            the file in which to save the data
	 */
	public static void saveProcessedDataFile(double[][] data, String file) throws IOException {
		PrintWriter pw = new PrintWriter(file);
		for (double[] point : data) {
			pw.printf("%f %f %f%n", point[0], point[1], point[2]);
		}
		pw.close();
	}

}
