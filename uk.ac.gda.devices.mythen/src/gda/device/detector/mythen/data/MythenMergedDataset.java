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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;

/**
 * Holds data from a Mythen merged {@code .dat} file.
 */
public class MythenMergedDataset {

	private List<MythenMergedData> lines;

	/**
	 * Creates a new processed dataset containing the supplied data.
	 *
	 * @param lines the processed data
	 */
	public MythenMergedDataset(List<MythenMergedData> lines) {
		this.lines = lines;
	}

	/**
	 * Loads the specified Mythen {@code .dat} file. It is assumed that the
	 * file contains three columns - angle, count and error.
	 *
	 * @param file the file to load
	 */
	public MythenMergedDataset(File file) {
		lines = new Vector<MythenMergedData>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(" ");
				final double angle = Double.parseDouble(tokens[0]);
				final double count = Double.parseDouble(tokens[1]);
				final double error = Double.parseDouble(tokens[2]);
				if(tokens.length>3){
					final double channel = Double.parseDouble(tokens[3]);
					MythenMergedData data = new MythenMergedData(angle, count, error, channel);
					lines.add(data);
				} else {
					MythenMergedData data = new MythenMergedData(angle, count, error);
					lines.add(data);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read Mythen data file " + file, e);
		}
	}

	/**
	 * Returns the lines in this dataset.
	 *
	 * @return the lines
	 */
	public List<MythenMergedData> getLines() {
		return Collections.unmodifiableList(lines);
	}

	/**
	 * Saves the data to the specified file.
	 *
	 * @param file the file to save the data to
	 */
	public void save(File file) {
		try {
			PrintWriter pw = new PrintWriter(file);
			for (MythenMergedData line : lines) {
				pw.printf("%f %f %f\n", line.getAngle(), line.getCount(), line.getError());
			}
			pw.close();
		} catch (IOException ioe) {
			throw new RuntimeException("Could not save data to " + file, ioe);
		}
	}

	/**
	 * Converts this dataset to a 2D {@code double} array, where each row of the
	 * array contains the angle and count.
	 *
	 * @return a {@code double} array
	 */
	public double[][] toDoubleArray() {
		double[][] data = new double[lines.size()][];
		int i = 0;
		for (MythenMergedData line : lines) {
			data[i++] = new double[] {line.getAngle(), line.getCount()};
		}
		return data;
	}

	/**
	 * Returns a {@code double} array of the angles in this dataset.
	 *
	 * @return array of angles
	 */
	public double[] getAngleArray() {
		double[] data = new double[lines.size()];
		int i = 0;
		for (MythenMergedData line : lines) {
			data[i++] = line.getAngle();
		}
		return data;
	}

	/**
	 * Returns a {@code double} array of the counts in this dataset.
	 *
	 * @return array of counts
	 */
	public double[] getCountArray() {
		double[] data = new double[lines.size()];
		int i = 0;
		for (MythenMergedData line : lines) {
			data[i++] = line.getCount();
		}
		return data;
	}

	/**
	 * Returns a {@code double} array of the counts in this dataset.
	 *
	 * @return array of errors
	 */
	public double[] getErrorArray() {
		double[] data = new double[lines.size()];
		int i = 0;
		for (MythenMergedData line : lines) {
			data[i++] = line.getError();
		}
		return data;
	}
	/**
	 * Returns a {@link IDataset} containing the angles in this dataset.
	 *
	 * @return a {@link IDataset} of angles
	 */
	public IDataset getAngleDataSet() {
		Dataset dataset = DatasetFactory.createFromObject(getAngleArray());
		dataset.setName("angle");
		return dataset;
	}

	/**
	 * Returns a {@link IDataset} containing the counts in this dataset.
	 *
	 * @return a {@link IDataset} of counts
	 */
	public IDataset getCountDataSet() {
		Dataset dataset = DatasetFactory.createFromObject(getCountArray());
		dataset.setName("count");
		return dataset;
	}

	/**
	 * Returns a {@link IDataset} containing the counts in this dataset.
	 *
	 * @return a {@link IDataset} of errors
	 */
	public IDataset getErrorDataSet() {
		Dataset dataset = DatasetFactory.createFromObject(getErrorArray());
		dataset.setName("error");
		return dataset;
	}
	@Override
	public String toString() {
		final int numLines = lines.size();
		return getClass().getSimpleName() + "[" + numLines + " line" + (numLines == 1 ? "" : "s") + "]";
	}

}
