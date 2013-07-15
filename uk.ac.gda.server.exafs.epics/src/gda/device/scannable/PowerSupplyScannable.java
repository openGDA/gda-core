/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.epics.CAClient;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

public class PowerSupplyScannable extends ScannableBase implements Scannable{

	private CAClient ca_client = new CAClient();

	private Object readback;
	private Object voltage;
	private Object disable;
	private Object send;
	private Object start_ramp;
	
	@Override
	public void configure() throws FactoryException {
		super.configure();
		// to make sure the column is correct in data files
		if (getInputNames().length == 1 && getInputNames()[0].equals(ScannableBase.DEFAULT_INPUT_NAME)) {
			setInputNames(new String[] { getName() });
		}
	}
	
	@Override
	public boolean isBusy() throws DeviceException {
		try {
			double voltage_a = Double.parseDouble(ca_client.caget((String) readback));
			Thread.sleep(100);
			double voltage_b = Double.parseDouble(ca_client.caget((String) readback));
			if(voltage_a!=voltage_b)
				return true;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (CAException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
	
		int intVal = Integer.parseInt(position.toString());

		try {
			ca_client.caput((String) disable, 1);
			Thread.sleep(500);
			ca_client.caput((String) voltage, intVal);
			Thread.sleep(500);
			ca_client.caput((String) start_ramp, 1);
			Thread.sleep(500);
			ca_client.caput((String) disable, 0);
			
		} catch (Exception e) {
			if( e instanceof DeviceException)
				throw (DeviceException)e;
			throw new DeviceException(getName() +" exception in rawAsynchronousMoveTo", e);
		}
	}
	
	@Override
	public Object rawGetPosition() throws DeviceException {
		try {
			return ca_client.caget((String) readback);
		} catch (Exception e) {
			if( e instanceof DeviceException)
				throw (DeviceException)e;
			throw new DeviceException(getName() +" exception in rawGetPosition", e);
		}
	}

	public Object getReadback() {
		return readback;
	}

	public void setReadback(Object readback) {
		this.readback = readback;
	}

	public Object getVoltage() {
		return voltage;
	}

	public void setVoltage(Object voltage) {
		this.voltage = voltage;
	}

	public Object getDisable() {
		return disable;
	}

	public void setDisable(Object disable) {
		this.disable = disable;
	}

	public Object getSend() {
		return send;
	}

	public void setSend(Object send) {
		this.send = send;
	}

	public Object getStart_ramp() {
		return start_ramp;
	}

	public void setStart_ramp(Object startRamp) {
		start_ramp = startRamp;
	}
	
}
