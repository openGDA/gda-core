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


public class ZebraPosnCompPulse {
	enum SOURCE { POSITION, TIME, EXTERNAL}
	
	Zebra zebra;

	
	
	public Zebra getZebra() {
		return zebra;
	}
	public void setZebra(Zebra zebra) {
		this.zebra = zebra;
	}
	
	public SOURCE getSource() throws DeviceException {
		int pulseSource = zebra.getPCPulseSource();
		switch(pulseSource){
		case 0:
			return SOURCE.POSITION;
		case 1:
			return SOURCE.TIME;
		case 2:
			return SOURCE.EXTERNAL;
		default:
			throw new DeviceException("Inalid PCPulseSource value " + pulseSource);
		}
	}
	public void setSource(SOURCE source) {
		zebra.setPCPulseSource(source.ordinal());
	}

	public boolean isStatus() {
		return zebra.getPCPulseOut() == 0;
	}
	public double getDelay() {
		return zebra.getPCPulseDelay();
	}
	public void setDelay(double delay) {
		zebra.setPCPulseDelay(delay);
	}
	public double getWidth() {
		return zebra.getPCPulseWidth();
	}
	public void setWidth(double width) {
		zebra.setPCPulseWidth(width);
	}
	public double getStep() {
		return zebra.getPCPulseStep();
	}
	public void setStep(double step) {
		zebra.setPCPulseStep(step);
	}
}
