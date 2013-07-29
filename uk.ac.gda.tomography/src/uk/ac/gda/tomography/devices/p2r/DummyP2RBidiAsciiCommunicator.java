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

package uk.ac.gda.tomography.devices.p2r;

import gda.device.DeviceException;
import gda.io.BidiAsciiCommunicator;

public class DummyP2RBidiAsciiCommunicator implements BidiAsciiCommunicator {

	private double m1=0.;
	private double m2=0.;

	@Override
	public String send(String string) {
		if(string.startsWith("M")){
			String substring = string.substring(1, string.length());
			String[] split = substring.split(",");
			if( split.length==2){
				m1 = Double.valueOf(split[0]);
				m2 = Double.valueOf(split[1]);	
				return string;
			}
		}
		if(string.startsWith("S")){
			return String.format("F%f,%f,", m1,m2);
		}
		return "ERROR";

	}

	@Override
	public void sendCmdNoReply(String cmd) throws DeviceException {
	}

}
