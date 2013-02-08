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

public class ZebraPosnCompGate {
	enum SOURCE { POSITION_POSITIVE, TIME, EXTERNAL, POSITION_NEGATIVE}
	SOURCE source;
	
	Zebra zebra;
	
	public Zebra getZebra() {
		return zebra;
	}
	public void setZebra(Zebra zebra) {
		this.zebra = zebra;
	}
	
	public SOURCE getSource() throws DeviceException {
		int pulseSource = zebra.getPCGateSource();
		switch(pulseSource){
		case 0:
			return SOURCE.POSITION_POSITIVE;
		case 1:
			return SOURCE.TIME;
		case 2:
			return SOURCE.EXTERNAL;
		case 3:
			return SOURCE.POSITION_NEGATIVE;
		default:
			throw new DeviceException("Inalid PCGateSource value " + pulseSource);
		}
	}
	public void setSource(SOURCE source) {
		zebra.setPCGateSource(source.ordinal());
	}
	public boolean isStatus() {
		return zebra.getPCGateOut() == 0;
	}
	public double getStart() {
		return zebra.getPCGateStart();
	}
	public void setStart(double start) {
		zebra.setPCGateStart(start);
	}
	public double getWidth() {
		return zebra.getPCGateWidth();
	}
	public void setWidth(double width) {
		zebra.setPCGateWidth(width);
	}
	public double getNumberOfGates() {
		return zebra.getPCGateNumberOfGates();
	}
	public void setNumberOfGates(double numberOfGates) {
		zebra.setPCGateNumberOfGates(numberOfGates);
	}
	public double getStep() {
		return zebra.getPCGateStep();
	}
	public void setStep(double step) {
		zebra.setPCGateStep(step);
	}
	
}
