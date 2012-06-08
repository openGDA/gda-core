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

package gda.analysis;

import gda.device.DeviceException;
import gda.device.scannable.PseudoDevice;
import gda.jython.JythonServerFacade;

/**
 * Class that allows the user to plot to the DataVector window as part of a scan
 */
public class PlotXY extends PseudoDevice {

	String[] allList = null;
	String[] yval = null;
	String xval = "";
	JythonServerFacade js = JythonServerFacade.getInstance();

	/**
	 * Constructor which takes in the axis to plot as strings
	 * 
	 * @param x
	 *            The X axis plot string
	 * @param y
	 *            The Y axis plot string
	 */
	public PlotXY(String x, String[] y) {
		this.setLevel(1000);
		String[] val = y;
		this.setInputNames(val);
		String[] formats = new String[val.length];
		for (int i = 0; i < val.length; i++) {
			formats[i] = "%5.5g";
		}
		this.setOutputFormat(formats);
		setName("PlotXY");
		xval = x;
		yval = y;

		String all = js.evaluateCommand("dir()");

		// process the string
		all = all.replace("[", "");
		all = all.replace("]", "");
		all = all.replace("'", "");
		all = all.replace(" ", "");
		allList = all.split(",");

		/*
		 * check Current_Scan_Holder exists"
		 */
		String res = js.evaluateCommand("finder.find(\"Current_Scan_Holder\") != None");
		String expected = js.evaluateCommand("True");
		if (!res.equals(expected)) {
			throw new IllegalArgumentException(
					"PlotXY. Current_Scan_Holder does not seem to exist  - please add to server xml");
		}
	}

	/**
	 * Function that move the device, in this case it dose nothing, as the device is a detector in essence
	 * 
	 * @param position
	 *            the position to move to (not used)
	 * @throws DeviceException
	 */
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {

		// do nothing
	}

	/**
	 * @return An object containing the position, in this case it is a PyList
	 * @throws DeviceException
	 */
	@Override
	public Object getPosition() throws DeviceException {

		double result[] = new double[yval.length];

		for (int i = 0; i < yval.length; i++) {

			String ist = yval[i].replace("+", " + ");
			ist = ist.replace("-", " - ");
			ist = ist.replace("/", " / ");
			ist = ist.replace("*", " * ");
			ist = ist.replace(")", " ) ");
			ist = ist.replace("(", " ( ");

			String[] istList = ist.split(" ");
			for (int s = 0; s < allList.length; s++) {
				for (int a = 0; a < istList.length; a++) {
					if (istList[a].compareTo(allList[s]) == 0) {
						String compositeString = allList[s] + ".getPosition()";
						String resultString = js.evaluateCommand(compositeString);
						istList[a] = resultString;
					}
				}
			}
			String nstr = "";
			for (int j = 0; j < istList.length; j++) {
				nstr += istList[j];
			}
			String temp = js.evaluateCommand(nstr);
			System.out.println(nstr + " == " + temp);
			result[i] = (Double.parseDouble(temp));
		}
		return result;
	}

	/**
	 * @return the state of the device, as this is a software device it is never buys, hence it returns false.
	 * @throws DeviceException
	 */
	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	/**
	 * Function that is called at the start of the scan. In this object this is used to set up the data vector plot
	 * window to be ready to recieve the data being sent to it.
	 * 
	 * @throws DeviceException
	 */
	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
		js.runCommand("finder.find(\"Current_Scan_Holder\").overplot = 0");
		String Composite = "\"" + yval[0] + "\"";
		for (int i = 1; i < yval.length; i++) {
			Composite += ",\"" + yval[i] + "\"";
		}
		js.runCommand("finder.find(\"Current_Scan_Holder\").prepare(\"" + xval + "\",[" + Composite + "])");
	}

}
