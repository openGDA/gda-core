/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.hdf5.HDF5Utils;
import org.eclipse.january.dataset.Dataset;

/* This class is used for testing Xspress3 v2 and will replace in the long run Xspress3FileReader. A new class was needed in order not to interfere with
 * other beamlines that are using Xspress3. With SWMR, we probably do not need this class anymore.
 */

public class Xspress3FileReaderv2 {

	private static final String DATA_PATH = "entry/instrument/detector/data";
	private String url;
	private int numberOfDetectorElements;
	private int mcaSize;
	private double[][][] theData = null; // [frame][element][mcaChannel]

	public Xspress3FileReaderv2(String filename, int numberOfDetectorElements, int mcaSize) {
		this.url = filename;
		this.numberOfDetectorElements = numberOfDetectorElements;
		this.mcaSize = mcaSize;
	}

	public void readFile() throws ScanFileHolderException {
		if (theData == null) {
			fillDataBuffer();
		}
	}

	/*
	 * Reads the whole row (whole file) into memory <p>
	 */
	private void fillDataBuffer() throws ScanFileHolderException {
		// data is frame x numberOfDetectorElements x mcaSize

		// P. Chang write a new method in HDF5Utils to load the entire dataset, probably just need one line now with URL and DATA_PATH
		int[][] shape;
		shape = HDF5Utils.getDatasetShape(url, DATA_PATH);
		Dataset dataset;
		// here probably just use numFrames instead, check dimension of shape[0] with and without chunking
		int[] start = new int[shape[0].length];
		int[] step = new int[shape[0].length];
		Arrays.fill(step, 1);
		Arrays.fill(start, 0);

		dataset = HDF5Utils.loadDataset(url, DATA_PATH, start, shape[0], step, Dataset.FLOAT64, 1, false);
		double[] buffer = (double[]) dataset.getBuffer();
		int numFrames = shape[0][0];
		theData = new double[numFrames][numberOfDetectorElements][mcaSize];

		int index = 0;
		for (int frame = 0; frame < numFrames; frame++) {
			for (int element = 0; element < numberOfDetectorElements; element++) {
				for (int mcaChannel = 0; mcaChannel < mcaSize; mcaChannel++) {
					theData[frame][element][mcaChannel] = buffer[index];
					index++;
				}
			}
		}
	}

	/**
	 * Assumes {@link #readFile()} has been called and returned normally.
	 *
	 * @param frameNumber
	 * @return
	 */
	public double[][] getFrame(int frameNumber) {
		return theData[frameNumber];
	}

}

