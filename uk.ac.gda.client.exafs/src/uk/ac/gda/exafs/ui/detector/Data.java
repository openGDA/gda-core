/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.ElementCountsData;
import uk.ac.gda.richbeans.components.data.DataWrapper;

public class Data {
	
	private static final Logger logger = LoggerFactory.getLogger(Data.class);
	
	public Data() {
	}
	
	public DataWrapper readStoredData(String dataXMLName) {
		DataWrapper newwrapper = new DataWrapper();
		try {
			File dataFile = new File(dataXMLName);
			if (!dataFile.exists())
				return newwrapper;
			BufferedReader in = new BufferedReader(new FileReader(dataFile));
			ElementCountsData[] elements = new ElementCountsData[0];
			String strLine;
			while ((strLine = in.readLine()) != null) {
				ElementCountsData newData = new ElementCountsData();
				newData.setDataString(strLine);
				elements = (ElementCountsData[]) ArrayUtils.add(elements, newData);
			}
			in.close();
			if (elements.length == 0)
				return newwrapper;
			newwrapper.setValue(elements);
		} catch (IOException e) {
			logger.error("IOException whilst reading stored detector editor data from file " + dataXMLName);
			return newwrapper;
		}
		return newwrapper;
	}

	public void writeStoredData(String dataXMLName, ElementCountsData[] elementCountsData) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(dataXMLName));
			for (int i = 0; i < elementCountsData.length; i++) {
				out.write(elementCountsData[i].getDataString());
				out.write("\n");
			}
			out.close();
		} catch (IOException e) {
			logger.error("IOException whilst writing stored detector editor data from file " + dataXMLName);
		}
	}
	
	public void save(int[][][] data, String filePath) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			StringBuffer toWrite = new StringBuffer();
			for (int i = 0; i < data.length; i++) 
				for (int j = 0; j < data[0].length; j++) {
					for (int k = 0; k < data[0][0].length; k++)
						toWrite.append(data[i][j][k] + "\t");
					writer.write(toWrite.toString() + "\n");
					toWrite = new StringBuffer();
				}
			writer.close();
		} catch (IOException e) {
			logger.warn("Exception writing acquire data to xml file", e);
		}
	}

}