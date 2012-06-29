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
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Holds data from a Mythen {@code .raw} file.
 */
public class MythenRawDataset {
	private List<MythenRawData> lines;

	public MythenRawDataset() {
	}

	/**
	 * Loads the specified Mythen {@code .raw} file.
	 * 
	 * @param file
	 *            the file to load
	 */
	public MythenRawDataset(File file) {
		lines = readRawMythenFile(file);
	}

	private List<MythenRawData> readRawMythenFile(File file) {
		Vector<MythenRawData> lines = new Vector<MythenRawData>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(" ");
				final int channel = Integer.parseInt(tokens[0]);
				final int count = Integer.parseInt(tokens[1]);
				MythenRawData rawLine = new MythenRawData(channel, count);
				lines.add(rawLine);
			}
		} catch (IOException e) {
			throw new RuntimeException("Couldn't read Mythen data file " + file, e);
		}

		return lines;
	}

	public void setLines(List<MythenRawData> lines) {
		this.lines = lines;
	}

	/**
	 * Returns the lines in this file.
	 * 
	 * @return the lines
	 */
	public List<MythenRawData> getLines() {
		return Collections.unmodifiableList(lines);
	}

	/**
	 * Returns a {@code double} array of the channels in this dataset.
	 * 
	 * @return array of counts
	 */
	public double[] getChannelArray() {
		double[] data = new double[lines.size()];
		int i = 0;
		for (MythenRawData line : lines) {
			data[i++] = line.getChannel();
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
		for (MythenRawData line : lines) {
			data[i++] = line.getCount();
		}
		return data;
	}

	@Override
	public String toString() {
		final int numLines = lines.size();
		return getClass().getSimpleName() + "[" + numLines + " line" + (numLines == 1 ? "" : "s") + "]";
	}
}
