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

package gda.device.spin.corba.impl;

import java.io.Serializable;

import org.omg.CORBA.Any;

import gda.device.DeviceException;
import gda.device.ISpin;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.device.spin.corba.CorbaSpinPOA;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed Spin class
 */

public class SpinImpl extends CorbaSpinPOA {
	// reference to implementation objects.
	private ISpin spin;

	private DeviceImpl deviceImpl;

	private ScannableImpl scannableImpl;

	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param spin
	 *            the Spin implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public SpinImpl(ISpin spin, org.omg.PortableServer.POA poa) {
		this.spin = spin;
		this.poa = poa;
		deviceImpl = new DeviceImpl(spin, poa);
		scannableImpl = new ScannableImpl(spin, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Robot implementation object
	 */
	public ISpin _delegate() {
		return spin;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param spin
	 *            set the Robot implementation object
	 */
	public void _delegate(ISpin spin) {
		this.spin = spin;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void asynchronousMoveTo(Any arg0) throws CorbaDeviceException {
		try {
			spin.asynchronousMoveTo(arg0);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public Any getPosition() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = spin.getPosition();
			any.insert_Value((Serializable) obj);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
		return any;
	}

	@Override
	public boolean isBusy() throws CorbaDeviceException {
		try {
			return spin.isBusy();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void atScanEnd() throws CorbaDeviceException {
		off();
	}

	@Override
	public void atScanStart() throws CorbaDeviceException {
		on();
	}

	@Override
	public void stop() throws CorbaDeviceException {
		off();
	}

	@Override
	public double getSpeed() throws CorbaDeviceException {
		try {
			return spin.getSpeed();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String getState() throws CorbaDeviceException {
		try {
			return spin.getState();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void off() throws CorbaDeviceException {
		try {
			spin.off();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void on() throws CorbaDeviceException {
		try {
			spin.on();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void setSpeed(double arg0) throws CorbaDeviceException {
		try {
			spin.setSpeed(arg0);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
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
	public void setAttribute(String attributeName, org.omg.CORBA.Any value) throws CorbaDeviceException {
		deviceImpl.setAttribute(attributeName, value);
	}

	@Override
	public org.omg.CORBA.Any getAttribute(String attributeName) throws CorbaDeviceException {
		return deviceImpl.getAttribute(attributeName);
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
	public void waitWhileBusy() throws CorbaDeviceException {
		scannableImpl.waitWhileBusy();
	}

	@Override
	public boolean isAt(Any arg0) throws CorbaDeviceException {
		try {
			return this.spin.isAt(arg0.extract_Value());
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
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