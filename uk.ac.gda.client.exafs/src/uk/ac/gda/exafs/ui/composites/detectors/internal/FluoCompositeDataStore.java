/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites.detectors.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.ElementCountsData;

public class FluoCompositeDataStore {

	private static final Logger logger = LoggerFactory.getLogger(FluoCompositeDataStore.class);

	private String fileName;

	public FluoCompositeDataStore(String fileName) {
		this.fileName = fileName;
	}

	public int[][] readDataFromFile() {
		int[][] result = new int[][] {/* empty */};
		try {
			File dataFile = new File(fileName);
			if (!dataFile.exists()) {
				return result;
			}

			BufferedReader in = new BufferedReader(new FileReader(dataFile));
			String strLine = in.readLine();
			in.close();

			ElementCountsData newData = new ElementCountsData();
			newData.setDataString(strLine);
			result = newData.getData();

		} catch (IOException e) {
			logger.error("IOException whilst reading stored detector editor data from file " + fileName);
		}
		return result;
	}

	public void writeDataToFile(int[][] newData) {
		ElementCountsData elementCountsData = new ElementCountsData();
		elementCountsData.setData(newData);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write(elementCountsData.getDataString());
			out.write("\n");
			out.close();
		} catch (IOException e) {
			logger.error("IOException whilst writing stored detector editor data from file " + fileName);
		}
	}

}
