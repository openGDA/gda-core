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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;

/**
 * This class extends the PlotXY class allowing the user to add the new data specified to the curent plot.
 */
public class PlotOverXY extends PlotXY {
	
	protected static Logger logger = LoggerFactory.getLogger(PlotOverXY.class);

	/**
	 * Constructor, this specifies which axes should be plotted
	 * 
	 * @param x
	 *            The string containing the X axis name
	 * @param y
	 *            The String containing the Y axis name
	 */
	public PlotOverXY(String x, String[] y) {
		super(x, y);
	}

	/**
	 * Function that is called at the start of the scan. In this object this is used to set up the data vector plot
	 * window to be ready to recieve the data being sent to it.
	 * 
	 * @throws DeviceException
	 */
	@Override
	public void atScanStart() throws DeviceException {
		try {
			js.runCommand("finder.find(\"Current_Scan_Holder\").overplot = 1");
			String Composite = "\"" + yval[0] + "\"";
			for (int i = 1; i < yval.length; i++) {
				Composite += ",\"" + yval[i] + "\"";
			}
			js.runCommand("finder.find(\"Current_Scan_Holder\").prepare(\"" + xval + "\",[" + Composite + "])");
		} catch (Exception e) {
			logger.error("Failure in PlotOverXY");
			logger.error(e.toString());
			throw new DeviceException("Software device PlotOverXY failed");
		}

	}
}
