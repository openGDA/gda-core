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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;

/**
 * Holds data from a Mythen {@code .dat} file.
 */
public class MythenProcessedDataset {

	private List<MythenProcessedData> lines;

	private List<String> headerStrings = Collections.emptyList();

	/**
	 * Creates a new processed dataset containing the supplied data.
	 *
	 * @param lines the processed data
	 */
	public MythenProcessedDataset(List<MythenProcessedData> lines) {
		this.lines = lines;
	}

	/**
	 * Loads the specified Mythen {@code .dat} file. It is assumed that the
	 * file contains three columns - angle, count and error.
	 *
	 * @param file the file to load
	 */
	public MythenProcessedDataset(File file) {
		lines = new Vector<MythenProcessedData>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) {
					// ignore comments/headers
					continue;
				}
				String[] tokens = line.split(" ");
				final double angle = Double.parseDouble(tokens[0]);
				final int count = Integer.parseInt(tokens[1]);
				final int error = Integer.parseInt(tokens[2]);
				if(tokens.length>3){
					final int channel = Integer.parseInt(tokens[3]);
					MythenProcessedData data = new MythenProcessedData(angle, count, error, channel);
					lines.add(data);
				}
				else{
					MythenProcessedData data = new MythenProcessedData(angle, count, error);
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
	public List<MythenProcessedData> getLines() {
		return Collections.unmodifiableList(lines);
	}

	/**
	 * Saves the data to the specified file.
	 *
	 * @param file the file to save the data to
	 */
	public void save(File file, boolean hasChannelInfo, boolean includeHeaders) {
		try(PrintWriter pw = new PrintWriter(file)) {
			if (includeHeaders) {
				pw.printf("# Filename\t %s\n", file.getAbsolutePath());
				addHeader(pw);
			}
			if (!hasChannelInfo) {
				for (MythenProcessedData line : lines) {
					pw.printf("%f %d %d\n", line.getAngle(), line.getCount(), line.getError());
				}
			} else {
				for (MythenProcessedData line : lines) {
					pw.printf("%f	%d	%d	%d\n", line.getAngle(), line.getCount(), line.getError(), line.getChannel());
				}
			}
		} catch (IOException ioe) {
			throw new RuntimeException("Could not save data to " + file, ioe);
		}
	}

	/**
	 * Add header lines containing useful information to file.
	 *
	 * @param pw
	 */
	private void addHeader(PrintWriter pw) {
		for (String headerString : headerStrings) {
			pw.printf("# %s\n", headerString);
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyy , HH:mm:ss");
		pw.printf("# Date\t %s\n", formatter.format(LocalDateTime.now()));
		pw.printf("# Angle,	Count,	Error,	Channel\n");
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
		for (MythenProcessedData line : lines) {
			data[i++] = new double[] { line.getAngle(), line.getCount(), line.getError(), line.getChannel()};
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
		for (MythenProcessedData line : lines) {
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
		for (MythenProcessedData line : lines) {
			data[i++] = line.getCount();
		}
		return data;
	}

	/**
	 * Returns a {@link DoubleDataset} containing the angles in this dataset.
	 *
	 * @return a {@link DoubleDataset} of angles
	 */
	public DoubleDataset getAngleDataSet() {
		DoubleDataset ds = DatasetFactory.createFromObject(DoubleDataset.class, getAngleArray());
		ds.setName("angle");
		return ds;
	}
	/**
	 * Returns a {@link IDataset} containing the angles in this dataset.
	 *
	 * @return a {@link IDataset} of angles
	 */
	public Dataset getAngleDataset() {
		Dataset dataset = DatasetFactory.createFromObject(getAngleArray());
		dataset.setName("angle");
		return dataset;
	}
	/**
	 * Returns a {@link DoubleDataset} containing the counts in this dataset.
	 *
	 * @return a {@link DoubleDataset} of counts
	 */
	public DoubleDataset getCountDataSet() {
		DoubleDataset ds = DatasetFactory.createFromObject(DoubleDataset.class, getCountArray());
		ds.setName("count");
		return ds;
	}
	public Dataset getCountDataset() {
		Dataset dataset = DatasetFactory.createFromObject(getCountArray());
		dataset.setName("counts");
		return dataset;
	}

	public List<String> getHeaderStrings() {
		return headerStrings;
	}

	public void setAdditionalHeaderStrings(List<String> headerStrings) {
		this.headerStrings = headerStrings;
	}

	@Override
	public String toString() {
		final int numLines = lines.size();
		return getClass().getSimpleName() + "[" + numLines + " line" + (numLines == 1 ? "" : "s") + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lines == null) ? 0 : lines.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MythenProcessedDataset other = (MythenProcessedDataset) obj;
		if (lines == null) {
			if (other.lines != null)
				return false;
		} else if (!lines.equals(other.lines))
			return false;
		return true;
	}

}
