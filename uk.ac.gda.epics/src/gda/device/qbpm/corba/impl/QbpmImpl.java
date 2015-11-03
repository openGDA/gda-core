/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.qbpm.corba.impl;

import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.enumpositioner.corba.CorbaEnumPositionerStatus;
import gda.device.enumpositioner.corba.impl.EnumpositionerImpl;
import gda.device.qbpm.EpicsQbpm;
import gda.device.qbpm.corba.CorbaQbpmPOA;
import gda.factory.corba.CorbaFactoryException;

import org.omg.CORBA.Any;

/**
 * A server side implementation for a distributed qbpm class
 */
/**
 *
 */
public class QbpmImpl extends CorbaQbpmPOA {
	//
	// Private reference to implementation objects
	//
	private EpicsQbpm qbpm;
	private EnumpositionerImpl enumpositionerImpl;
	private DeviceImpl deviceImpl;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param qbpm
	 *            the qbpm implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public QbpmImpl(EpicsQbpm qbpm, org.omg.PortableServer.POA poa) {
		this.qbpm = qbpm;
		deviceImpl = new DeviceImpl(qbpm, poa);
		enumpositionerImpl = new EnumpositionerImpl(qbpm, poa);
	}

	@Override
	public void setAttribute(String attributeName, Any value) throws CorbaDeviceException {
		deviceImpl.setAttribute(attributeName, value);
	}

	@Override
	public Any getAttribute(String attributeName) throws CorbaDeviceException {
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
	public String _toString() {
		return enumpositionerImpl._toString();
	}

	@Override
	public void asynchronousMoveTo(Any arg0) throws CorbaDeviceException {
		enumpositionerImpl.asynchronousMoveTo(arg0);
	}

	@Override
	public void atPointEnd() throws CorbaDeviceException {
		enumpositionerImpl.atPointEnd();

	}

	@Override
	public void atPointStart() throws CorbaDeviceException {
		enumpositionerImpl.atPointStart();

	}

	@Override
	public void atScanEnd() throws CorbaDeviceException {
		enumpositionerImpl.atScanEnd();

	}

	@Override
	public void atScanLineEnd() throws CorbaDeviceException {
		enumpositionerImpl.atScanLineEnd();

	}

	@Override
	public void atScanLineStart() throws CorbaDeviceException {
		enumpositionerImpl.atScanLineStart();

	}

	@Override
	public void atScanStart() throws CorbaDeviceException {
		enumpositionerImpl.atScanStart();

	}

	@Override
	public String[] getExtraNames() {
		return enumpositionerImpl.getExtraNames();
	}

	@Override
	public String[] getInputNames() {
		return enumpositionerImpl.getInputNames();
	}

	@Override
	public int getLevel() {
		return enumpositionerImpl.getLevel();
	}

	@Override
	public Any getPosition() throws CorbaDeviceException {
		return enumpositionerImpl.getPosition();
	}

	@Override
	public boolean isBusy() throws CorbaDeviceException {
		return enumpositionerImpl.isBusy();
	}

	@Override
	public void moveTo(Any arg0) throws CorbaDeviceException {
		enumpositionerImpl.moveTo(arg0);

	}

	@Override
	public void setExtraNames(String[] arg0) {
		enumpositionerImpl.setExtraNames(arg0);

	}

	@Override
	public void setInputNames(String[] arg0) {
		enumpositionerImpl.setInputNames(arg0);

	}

	@Override
	public void setLevel(int arg0) {
		enumpositionerImpl.setLevel(arg0);

	}

	@Override
	public void stop() throws CorbaDeviceException {
		enumpositionerImpl.stop();

	}

	@Override
	public void waitWhileBusy() throws CorbaDeviceException {
		enumpositionerImpl.waitWhileBusy();

	}

	@Override
	public String[] getOutputFormat() {
		return enumpositionerImpl.getOutputFormat();
	}

	@Override
	public Any checkPositionValid(Any arg0) throws CorbaDeviceException {
		return enumpositionerImpl.checkPositionValid(arg0);
	}

	@Override
	public void setOutputFormat(String[] arg0) {
		enumpositionerImpl.setOutputFormat(arg0);
	}

	@Override
	public void atEnd() throws CorbaDeviceException {
		enumpositionerImpl.atEnd();
	}

	@Override
	public void atStart() throws CorbaDeviceException {
		enumpositionerImpl.atStart();
	}

	@Override
	public int getElementCount() throws CorbaDeviceException {
		try {
			return qbpm.getElementCount();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public String getUnit() throws CorbaDeviceException {
		try {
			return qbpm.getUnit();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public String getBpmName() {
		return qbpm.getBpmName();
	}

	@Override
	public void setBpmName(String name) {
		qbpm.setBpmName(name);
	}

	@Override
	public String getCurrAmpQuadName() {
		return qbpm.getCurrAmpQuadName();
	}

	@Override
	public void setCurrAmpQuadName(String name) {
		qbpm.setCurrAmpQuadName(name);
	}

	@Override
	public CorbaEnumPositionerStatus getStatus() throws CorbaDeviceException {
		return enumpositionerImpl.getStatus();
	}

	@Override
	public double getCurrent1() throws CorbaDeviceException {
		try {
			return qbpm.getCurrent1();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public double getCurrent2() throws CorbaDeviceException {
		try {
			return qbpm.getCurrent2();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public double getCurrent3() throws CorbaDeviceException {
		try {
			return qbpm.getCurrent3();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public double getCurrent4() throws CorbaDeviceException {
		try {
			return qbpm.getCurrent4();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String getRangeValue() throws CorbaDeviceException {
		try {
			return qbpm.getRangeValue();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getIntensityTotal() throws CorbaDeviceException {
		try {
			return qbpm.getIntensityTotal();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public double getXPosition() throws CorbaDeviceException {
		try {
			return qbpm.getXPosition();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public double getYPosition() throws CorbaDeviceException {
		try {
			return qbpm.getYPosition();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String[] getPositions() throws CorbaDeviceException {
		return enumpositionerImpl.getPositions();
	}

	public void moveTo_string(String arg0) throws CorbaDeviceException {
		enumpositionerImpl.moveTo_string(arg0);
	}

	@Override
	public boolean isAt(Any arg0) throws CorbaDeviceException {
		try {
			return this.qbpm.isAt(arg0.extract_Value());
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
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
		enumpositionerImpl.atLevelMoveStart();
	}
	
	@Override
	public void atCommandFailure() throws CorbaDeviceException {
		enumpositionerImpl.atCommandFailure();
	}
	
	@Override
	public String toFormattedString() throws CorbaDeviceException {
		return enumpositionerImpl.toFormattedString();
	}

	@Override
	public void atLevelStart() throws CorbaDeviceException {
		enumpositionerImpl.atLevelStart();
	}

	@Override
	public void atLevelEnd() throws CorbaDeviceException {
		enumpositionerImpl.atLevelEnd();
	}
}
