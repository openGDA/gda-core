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

package gda.device.enumpositioner.corba.impl;

import java.io.Serializable;

import org.omg.CORBA.Any;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.corba.CorbaDeviceException;
import gda.device.enumpositioner.corba.CorbaEnumPositionerPOA;
import gda.device.enumpositioner.corba.CorbaEnumPositionerStatus;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed EnumPositioner class
 */

public class EnumpositionerImpl extends CorbaEnumPositionerPOA {
	// reference to implementation objects.
	private EnumPositioner enumpositioner;
	private ScannableImpl scannableImpl;

	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param enumpositioner
	 *            the EnumPositioner implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public EnumpositionerImpl(EnumPositioner enumpositioner, org.omg.PortableServer.POA poa) {
		this.enumpositioner = enumpositioner;
		this.poa = poa;
		scannableImpl = new ScannableImpl(enumpositioner, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the EnumPositioner implementation object
	 */
	public EnumPositioner _delegate() {
		return enumpositioner;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param enumpositioner
	 *            set the EnumPositioner implementation object
	 */
	public void _delegate(EnumPositioner enumpositioner) {
		this.enumpositioner = enumpositioner;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	// implement EnumPositioner interface methods delegation
	@Override
	public String[] getPositions() throws CorbaDeviceException {
		try {
			return enumpositioner.getPositions();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public CorbaEnumPositionerStatus getStatus() throws CorbaDeviceException {
		try {
			return CorbaEnumPositionerStatus.from_int(enumpositioner.getStatus().value());
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	/**
	 * 
	 * @param position
	 * @throws CorbaDeviceException
	 */
	public void moveTo(String position) throws CorbaDeviceException {
		try {
			enumpositioner.moveTo(position);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}
	// implemet inherited Scannable interface methods delegation
	@Override
	public Any getPosition() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = enumpositioner.getPosition();
			any.insert_Value((Serializable) obj);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
		return any;
	}

	@Override
	public void stop() throws CorbaDeviceException {
		try {
			enumpositioner.stop();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}


	public void moveTo_string(String arg0) throws CorbaDeviceException {
		try {
			enumpositioner.moveTo(arg0);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String _toString() {
		return scannableImpl._toString();
	}

	@Override
	public void asynchronousMoveTo(Any arg0) throws CorbaDeviceException {
		scannableImpl.asynchronousMoveTo(arg0);
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
	public void atScanEnd() throws CorbaDeviceException {
		scannableImpl.atScanEnd();
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
	public void atScanStart() throws CorbaDeviceException {
		scannableImpl.atScanStart();
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
	public boolean isBusy() throws CorbaDeviceException {
		return scannableImpl.isBusy();
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
	public void atEnd() throws CorbaDeviceException {
		scannableImpl.atEnd();
	}

	@Override
	public void atStart() throws CorbaDeviceException {
		scannableImpl.atStart();
	}

	@Override
	public boolean isAt(Any arg0) throws CorbaDeviceException {
		try {
			return this.enumpositioner.isAt(arg0.extract_Value());
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}
	// implement inherited Device interface methods delegation
	@Override
	public void reconfigure() throws CorbaFactoryException {
		scannableImpl.reconfigure();
	}

	@Override
	public void close() throws CorbaDeviceException {
		scannableImpl.close();
	}

	@Override
	public void setAttribute(String attributeName, org.omg.CORBA.Any value) throws CorbaDeviceException {
		scannableImpl.setAttribute(attributeName, value);
	}

	@Override
	public org.omg.CORBA.Any getAttribute(String attributeName) throws CorbaDeviceException {
		return scannableImpl.getAttribute(attributeName);
	}

	@Override
	public int getProtectionLevel() throws CorbaDeviceException {
		return scannableImpl.getProtectionLevel();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws CorbaDeviceException {
		scannableImpl.setProtectionLevel(newLevel);
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