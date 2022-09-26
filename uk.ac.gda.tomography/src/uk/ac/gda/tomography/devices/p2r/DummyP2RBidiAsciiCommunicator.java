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


	private double md=0.;
	private double mr=0.;
	private double mf=0.;
	private double sd=1.;
	private double sr=1;

	@Override
	public String send(String string) {
		if(string.startsWith("MD")){
			String substring = string.substring(2, string.length());
			md = Double.valueOf(substring);
			return string;
		}
		if(string.startsWith("ST")){
			return String.format("F%f,%f,%f,%f,%f", md,mr,mf,sd,sr);
		}
		if(string.equals("MS")){
			return string;
		}
		if(string.equals("AB")){
			return string;
		}
		if(string.equals("SS")){
			return string;
		}
		if(string.startsWith("SD")){
			return string;
		}
		return "ERROR";

	}

	@Override
	public void sendCmdNoReply(String cmd) throws DeviceException {
	}

}
