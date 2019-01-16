/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.xspress4;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;

public class HelperClasses {

	/**
	 * Load dataset from nexus file; file should be closed initially (it will be opened to read the data, and closed afterwards).
	 * @param nexusFilename
	 * @param groupName
	 * @param dataName
	 * @return dataset
	 * @throws NexusException
	 * @throws DatasetException
	 */
	public static IDataset getDataset(String nexusFilename, String groupName, String dataName)
			throws NexusException {
		//NexusFileHDF5.openNexusFileReadOnly(nexusFilename);
		try (NexusFile file = new NexusFileHDF5(nexusFilename, true)) {
			file.openToRead();
			GroupNode group = null;
			try {
				group = file.getGroup("/entry1/" + groupName, false);
			}catch(Exception e) {
				System.out.println("Ignoring first getGroup exception...");
			}
			group = file.getGroup("/entry1/" + groupName, false);
			DataNode d = file.getData(group, dataName);
			return d.getDataset().getSlice(null, null, null);
		} catch (NexusException|DatasetException e) {
			String msg = "Problem opening Nexus dataset /entry1/" + groupName + "/" + dataName + " : " + e.getMessage();
			throw new NexusException(msg, e);
		}
	}

	public static void checkDatasetShape(IDataset dataset, int[] expectedShape) {
		assertArrayEquals("Error checking shape of dataset "+dataset.getName(), expectedShape, dataset.getShape());
	}

	public static enum CheckType {GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL_TO, LESS_OR_EQUAL_TO};

	public static boolean numberOk(double number, double limit, CheckType checkType) {
		switch(checkType) {
		case GREATER_THAN : return number > limit;
		case GREATER_OR_EQUAL_TO : return number >= limit;
		case LESS_THAN : return number < limit;
		case LESS_OR_EQUAL_TO : return number <= limit;
		default : return false;
		}
	}

	public static void checkDatasetMinValue(IDataset dataset, CheckType checkType, double limit) {
		double datasetMin = dataset.min().doubleValue();
		boolean ok = numberOk(datasetMin, limit, checkType);
		assertTrue("Min. value in dataset "+dataset.getName()+" not "+checkType.toString()+" "+limit, ok);
	}

	public static void checkDatasetMaxValue(IDataset dataset, CheckType checkType, double limit) {
		double datasetMax = dataset.max().doubleValue();
		boolean ok = numberOk(datasetMax, limit, checkType);
		assertTrue("Max. value in dataset "+dataset.getName()+" not "+checkType.toString()+" "+limit, ok);
	}

	/**
	 * Read Ascii text file, parsing each line into array of strings (strings separated by 1 or more whitespace chars).
	 * @param filepath
	 * @return List< String[] > content of each line in file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List< String[] > readAsciiFile(String filepath) throws IOException {
		try( BufferedReader inputStream = new BufferedReader(new FileReader(filepath)) ) {
			List<String[]> fileContents = new ArrayList<>();
			String line;
			while( (line = inputStream.readLine()) != null) {
				fileContents.add(line.split("\\s+"));
			}
			return fileContents;
		}
	}
}
