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

package gda.device.temperature.corba.impl;

import gda.device.DeviceException;
import gda.device.Temperature;
import gda.device.TemperatureRamp;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.device.temperature.corba.CorbaTemperaturePOA;
import gda.device.temperature.corba.CorbaTemperatureRamp;
import gda.factory.corba.CorbaFactoryException;

import java.io.Serializable;
import java.util.ArrayList;

import org.omg.CORBA.Any;

/**
 * A server side implementation for a distributed Temperature class
 */
/**
 * 
 */
public class TemperatureImpl extends CorbaTemperaturePOA {
	//
	// Private reference to implementation object
	//
	private Temperature temperature;

	private DeviceImpl deviceImpl;
	private ScannableImpl scannableImpl;
	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param temperature
	 *            the Temperature implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public TemperatureImpl(Temperature temperature, org.omg.PortableServer.POA poa) {
		this.temperature = temperature;
		this.poa = poa;
		deviceImpl = new DeviceImpl(temperature, poa);
		scannableImpl = new ScannableImpl(temperature, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Temperature implementation object
	 */
	public Temperature _delegate() {
		return temperature;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param temperature
	 *            set the Temperature implementation object
	 */
	public void _delegate(Temperature temperature) {
		this.temperature = temperature;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public double getCurrentTemperature() throws CorbaDeviceException {
		try {
			return temperature.getCurrentTemperature();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setTargetTemperature(double target) throws CorbaDeviceException {
		try {
			temperature.setTargetTemperature(target);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getTargetTemperature() throws CorbaDeviceException {
		try {
			return temperature.getTargetTemperature();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public String[] getProbeNames() throws CorbaDeviceException {
		try {
			ArrayList<String> names = temperature.getProbeNames();
			String[] result = new String[names.size()];
			for (int i = 0; i < names.size(); i++)
				result[i] = names.get(i);
			return result;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setProbe(String probeName) throws CorbaDeviceException {
		try {
			temperature.setProbe(probeName);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public boolean isAtTargetTemperature() throws CorbaDeviceException {
		try {
			return temperature.isAtTargetTemperature();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setLowerTemp(double lowLimit) throws CorbaDeviceException {
		try {
			temperature.setLowerTemp(lowLimit);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setUpperTemp(double upperLimit) throws CorbaDeviceException {
		try {
			temperature.setUpperTemp(upperLimit);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getUpperTemp() throws CorbaDeviceException {
		try {
			return temperature.getUpperTemp();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getLowerTemp() throws CorbaDeviceException {
		try {
			return temperature.getLowerTemp();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void waitForTemp() throws CorbaDeviceException {
		try {
			temperature.waitForTemp();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void clearRamps() throws CorbaDeviceException {
		try {
			temperature.clearRamps();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void addRamp(CorbaTemperatureRamp ramp) throws CorbaDeviceException {
		try {
			temperature.addRamp(new TemperatureRamp(ramp.ramp, ramp.startTemp, ramp.endTemp, ramp.rate, ramp.dwellTime,
					ramp.coolingSpeed));
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setRamps(CorbaTemperatureRamp[] ramps) throws CorbaDeviceException {
		try {
			ArrayList<TemperatureRamp> rampList = new ArrayList<TemperatureRamp>();
			for (int i = 0; i < ramps.length; i++) {
				rampList.add(new TemperatureRamp(ramps[i].ramp, ramps[i].startTemp, ramps[i].endTemp, ramps[i].rate,
						ramps[i].dwellTime, ramps[i].coolingSpeed));
			}
			temperature.setRamps(rampList);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void start() throws CorbaDeviceException {
		try {
			temperature.start();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void stop() throws CorbaDeviceException {
		try {
			temperature.stop();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void begin() throws CorbaDeviceException {
		try {
			temperature.begin();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void end() throws CorbaDeviceException {
		try {
			temperature.end();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getRampRate() throws CorbaDeviceException {
		try {
			return temperature.getRampRate();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setRampRate(double arg0) throws CorbaDeviceException {
		try {
			temperature.setRampRate(arg0);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void hold() throws CorbaDeviceException {
		try {
			temperature.hold();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public boolean isRunning() throws CorbaDeviceException {
		try {
			return temperature.isRunning();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setAttribute(String attributeName, org.omg.CORBA.Any value) throws CorbaDeviceException {
		deviceImpl.setAttribute(attributeName, value);
	}

	@Override
	public org.omg.CORBA.Any getAttribute(String attributeName) throws CorbaDeviceException {
		return deviceImpl.getAttribute(attributeName);
	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		deviceImpl.reconfigure();
	}

	@Override
	public void close() throws CorbaDeviceException {
		deviceImpl.close();
	}

	@Override
	public void asynchronousMoveTo(Any arg0) throws CorbaDeviceException {
		try {
			temperature.asynchronousMoveTo(arg0);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}

	}

	@Override
	public Any getPosition() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = temperature.getPosition();
			any.insert_Value((Serializable) obj);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
		return any;
	}

	@Override
	public boolean isBusy() throws CorbaDeviceException {
		try {
			return temperature.isBusy();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void atScanStart() throws CorbaDeviceException {
		try {
			temperature.atScanEnd();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void atScanEnd() throws CorbaDeviceException {
		try {
			temperature.atScanStart();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void waitWhileBusy() throws CorbaDeviceException {
		try {
			temperature.waitForTemp();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public boolean isAt(Any arg0) throws CorbaDeviceException {
		try {
			return this.temperature.isAt(arg0.extract_Value());
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String _toString() {
		return scannableImpl._toString();
	}

	@Override
	public void atPointEnd() throws CorbaDeviceException {
		scannableImpl.atPointEnd();
	}

	@Override
	public void atPointStart() throws CorbaDeviceException {
		scannableImpl.atPointStart();
	}

	@Override
	public void atScanLineEnd() throws CorbaDeviceException {
		scannableImpl.atScanLineEnd();
	}

	@Override
	public void atScanLineStart() throws CorbaDeviceException {
		scannableImpl.atScanLineStart();
	}

	@Override
	public String[] getExtraNames() {
		return scannableImpl.getExtraNames();
	}

	@Override
	public String[] getInputNames() {
		return scannableImpl.getInputNames();
	}

	@Override
	public int getLevel() {
		return scannableImpl.getLevel();
	}

	@Override
	public String[] getOutputFormat() {
		return scannableImpl.getOutputFormat();
	}

	@Override
	public Any checkPositionValid(Any arg0) throws CorbaDeviceException {
		return scannableImpl.checkPositionValid(arg0);
	}

	@Override
	public void moveTo(Any arg0) throws CorbaDeviceException {
		scannableImpl.moveTo(arg0);
	}

	@Override
	public void setExtraNames(String[] arg0) {
		scannableImpl.setExtraNames(arg0);
	}

	@Override
	public void setInputNames(String[] arg0) {
		scannableImpl.setInputNames(arg0);
	}

	@Override
	public void setLevel(int arg0) {
		scannableImpl.setLevel(arg0);
	}

	@Override
	public void setOutputFormat(String[] arg0) {
		scannableImpl.setOutputFormat(arg0);
	}

	@Override
	public void atEnd() throws CorbaDeviceException {
		scannableImpl.atEnd();
	}

	@Override
	public void atStart() throws CorbaDeviceException {
		scannableImpl.atStart();
	}

	@Override
	public int getProtectionLevel() throws CorbaDeviceException {
		return deviceImpl.getProtectionLevel();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws CorbaDeviceException {
		deviceImpl.setProtectionLevel(newLevel);
	}

	@Override
	public void atLevelMoveStart() throws CorbaDeviceException {
		scannableImpl.atLevelMoveStart();
	}
	
	@Override
	public void atCommandFailure() throws CorbaDeviceException {
		scannableImpl.atCommandFailure();
	}
	
	@Override
	public String toFormattedString() throws CorbaDeviceException {
		return scannableImpl.toFormattedString();
	}
	
	@Override
	public void atLevelStart() throws CorbaDeviceException {
		scannableImpl.atLevelStart();
	}

}
