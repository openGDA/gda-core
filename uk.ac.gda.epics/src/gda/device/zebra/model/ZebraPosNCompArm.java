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

package gda.device.zebra.model;

import gda.device.DeviceException;
import gda.device.zebra.controller.Zebra;

public class ZebraPosNCompArm {
	enum SOURCE { SOFT, EXTERNAL}
	SOURCE source;
	
	boolean armed;

	Zebra zebra;
	
	public Zebra getZebra() {
		return zebra;
	}
	public void setZebra(Zebra zebra) {
		this.zebra = zebra;
	}
	
	public SOURCE getSource() throws DeviceException {
		int armSource = zebra.getPCArmSource();
		switch(armSource){
		case 0:
			return SOURCE.SOFT;
		case 1:
			return SOURCE.EXTERNAL;
		default:
			throw new DeviceException("Inalid PCArmSource value " + armSource);
		}
	}
	public void setSource(SOURCE source) {
		zebra.setPCArmSource(source.ordinal());
	}

	public boolean isArmed() {
		return zebra.getPCArmOut() == 0;
	}

	public void arm(boolean arm) {
		if( arm)
			zebra.pcArm();
		else
			zebra.pcDisarm();
	}
}
