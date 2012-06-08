/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.analysis.datastructure;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.motor.MotorBase;
import gda.factory.Finder;

import java.util.Vector;

/**
 * ScanLineContainer Class
 */
public class ScanLineContainer {

	Vector<DataVector> data;

	Vector<String> title;

	/**
	 * @param headerLine
	 * @param dataLine
	 */
	public ScanLineContainer(String headerLine, String dataLine) {

		// first thing to do is to get the objects from the header line, and
		// work
		// out what size they are. also grab the data.

		String[] vals = headerLine.split("\t");
		String[] datavals = dataLine.split("\t");

		title = new Vector<String>();
		data = new Vector<DataVector>();

		Finder finder = Finder.getInstance();

		int counter = 0;

		for (int i = 0; i < vals.length; i++) {
			Object found = finder.find(vals[i]);

			title.add(vals[i]);

			if (found instanceof MotorBase) {

				// create a new Datavector, containing the single piece of info
				double insert[] = { Double.parseDouble(datavals[counter]) };
				data.add(new DataVector(insert));
				counter++;
			}

			if (found instanceof Detector) {
				// get the size of the data

				int width = 0;
				int height = 0;

				try {
					width = ((Detector) found).getDataDimensions()[0];
					height = ((Detector) found).getDataDimensions()[1];
				} catch (DeviceException e) {
					// abort the read, and show the exception
					System.err
							.println("Error occuring in ScanFileContainer, trying to access data in object :" + found);
					System.err.println(e);
				}

				// create a holder
				double insert[] = new double[width * height];

				// Fill the holder with the data from the file
				for (int x = 0; x < width * height; x++) {
					insert[x] = Double.parseDouble(datavals[counter]);
					counter++;
				}

				data.add(new DataVector(width, height, insert));

			}
		}
	}

	@Override
	public String toString() {

		String Out = "";

		for (int i = 0; i < data.size(); i++) {

			Out += "Axis : " + title.get(i) + "\n";
			Out += data.get(i).toString();

		}

		return Out;

	}

	/**
	 * @param axisName
	 * @return DataVector
	 */
	public DataVector getAxis(String axisName) {

		for (int i = 0; i < data.size(); i++) {
			// find the correct data, then plot it.
			if (title.get(i).compareTo(axisName) == 0) {
				DataVector output = new DataVector(data.get(i).getDimensions());
				output.createListFromArray(data.get(i).doubleArray());

				return (output);
			}
		}
		return (null);
	}

}
