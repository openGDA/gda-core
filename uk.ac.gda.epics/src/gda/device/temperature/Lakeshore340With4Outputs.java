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

package gda.device.temperature;

import gda.device.DeviceException;

public class Lakeshore340With4Outputs extends GdaLakeshore340 {

	public Lakeshore340With4Outputs() {
		setInputNames(new String[] { "Channel0Temp" });
		setExtraNames(new String[] { "Channel1Temp", "Channel2Temp", "Channel3Temp" });
		String[] outputFormats = new String[inputNames.length + extraNames.length];
		for (int i = 0; i < outputFormats.length; i++) {
			outputFormats[i] = "%5.2f";
		}
		setOutputFormat(outputFormats);

	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		double[] temp = new double[4];
		temp[0] = controller.getChannel0Temp();
		temp[1] = controller.getChannel1Temp();
		temp[2] = controller.getChannel2Temp();
		temp[3] = controller.getChannel3Temp();
		return temp;
	}

	public void setReadbackChannel(int ch) throws DeviceException {
		controller.setReadbackChannel(ch);
	}

	public int getReadbackChannel() {
		return controller.getReadbackChannel();
	}
}
