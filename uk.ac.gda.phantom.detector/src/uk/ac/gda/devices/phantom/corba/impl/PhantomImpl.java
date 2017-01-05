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

package uk.ac.gda.devices.phantom.corba.impl;

import java.io.Serializable;

import org.omg.CORBA.Any;

import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.phantom.corba.CorbaPhantomPOA;
import gda.device.scannable.corba.impl.NullString;
import gda.factory.FactoryException;
import gda.factory.corba.CorbaFactoryException;
import uk.ac.gda.devices.phantom.Phantom;

/**
 * A server side implementation for a distributed Motor class
 */
public class PhantomImpl extends CorbaPhantomPOA {
	//
	// Private reference to implementation object
	//
	private Phantom phantom;

	// this is here for legacy reasons i think, but the warning is suppressed for the time being
	// in case something requires it.
	@SuppressWarnings("unused")
	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 *
	 * @param phantom
	 *            The phantom object which has been passed by corba
	 * @param poa
	 *            the portable object adapter
	 */
	public PhantomImpl(Phantom phantom, org.omg.PortableServer.POA poa) {
		this.phantom = phantom;
		this.poa = poa;
		deviceImpl = new DeviceImpl(phantom, poa);
	}

	/**
	 * Get the implementation object
	 *
	 * @return the Motor implementation object
	 */
	public Phantom _delegate() {
		return phantom;
	}

	/**
	 * Set the implementation object.
	 *
	 * @param phantom
	 *            set the Phantom implementation object
	 */
	public void _delegate(Phantom phantom) {
		this.phantom = phantom;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void setUpForCollection(int arg0, int arg1, int arg2, int arg3) throws CorbaDeviceException {
		try {
			phantom.setUpForCollection(arg0, arg1, arg2, arg3);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException("Problem in the phantom corba : " + ex.toString());
		}
	}

	@Override
	public Any retrieveData(int arg0, int arg1, int arg2) throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = phantom.retrieveData(arg0, arg1, arg2);
			any.insert_Value((Serializable) obj);
		} catch (Exception ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
		return any;
	}

	@Override
	public String command(String arg0) throws CorbaDeviceException {
		try {
			return phantom.command(arg0);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException("Problem in the phantom corba : " + ex.toString());
		}
	}

	@Override
	public void collectData() throws CorbaDeviceException {
		try {
			phantom.collectData();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public boolean createsOwnFiles() throws CorbaDeviceException {
		try {
			return phantom.createsOwnFiles();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public void endCollection() throws CorbaDeviceException {
		try {
			phantom.endCollection();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public int[] getDataDimensions() throws CorbaDeviceException {
		try {
			return phantom.getDataDimensions();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public String getDescription() throws CorbaDeviceException {
		try {
			return phantom.getDescription();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public String getDetectorID() throws CorbaDeviceException {
		try {
			return phantom.getDetectorID();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public String getDetectorType() throws CorbaDeviceException {
		try {
			return phantom.getDetectorType();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public int getStatus() throws CorbaDeviceException {
		try {
			return phantom.getStatus();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public void prepareForCollection() throws CorbaDeviceException {
		try {
			phantom.prepareForCollection();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public Any readout() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = phantom.readout();
			any.insert_Value((Serializable) obj);
		} catch (Exception ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
		return any;
	}

	@Override
	public void setCollectionTime(double arg0) throws CorbaDeviceException {
		try {
			phantom.setCollectionTime(arg0);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public double getCollectionTime() throws CorbaDeviceException {
		try {
			return phantom.getCollectionTime();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String _toString() {
		return phantom.toString();
	}

	/**
	 * Not yet implemented {@inheritDoc}
	 */
	@Override
	public void asynchronousMoveTo(Any arg0) throws CorbaDeviceException {
		try {
			phantom.asynchronousMoveTo(arg0);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public void atPointEnd() throws CorbaDeviceException {
		try {
			phantom.atPointEnd();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public void atPointStart() throws CorbaDeviceException {
		try {
			phantom.atPointStart();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public void atScanEnd() throws CorbaDeviceException {
		try {
			phantom.atScanEnd();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public void atScanLineEnd() throws CorbaDeviceException {
		try {
			phantom.atScanLineEnd();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public void atScanLineStart() throws CorbaDeviceException {
		try {
			phantom.atScanLineStart();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public void atScanStart() throws CorbaDeviceException {
		try {
			phantom.atScanStart();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public String[] getExtraNames() {
		return phantom.getExtraNames();
	}

	@Override
	public String[] getInputNames() {
		return phantom.getInputNames();
	}

	@Override
	public int getLevel() {
		return phantom.getLevel();
	}

	@Override
	public String[] getOutputFormat() {
		return phantom.getOutputFormat();
	}

	@Override
	public Any getPosition() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = phantom.getPosition();
			any.insert_Value((Serializable) obj);
		} catch (Exception ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
		return any;
	}

	@Override
	public boolean isAt(Any arg0) throws CorbaDeviceException {
		try {
			return phantom.isAt(arg0);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public boolean isBusy() throws CorbaDeviceException {
		try {
			return phantom.isBusy();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public Any checkPositionValid(Any arg0) throws CorbaDeviceException {
		try {
			String ret = this.phantom.checkPositionValid(arg0.extract_Value());
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			if (ret == null){
				any.insert_Value(new NullString());
			} else {
				any.insert_Value(ret);
			}
			return any;
		} catch (DeviceException e) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(e));
		}
	}

	@Override
	public void moveTo(Any arg0) throws CorbaDeviceException {
		try {
			phantom.moveTo(arg0);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public void setExtraNames(String[] arg0) {
		phantom.setExtraNames(arg0);

	}

	@Override
	public void setInputNames(String[] arg0) {

		phantom.setInputNames(arg0);

	}

	@Override
	public void setLevel(int arg0) {

		phantom.setLevel(arg0);

	}

	@Override
	public void setOutputFormat(String[] arg0) {

		phantom.setOutputFormat(arg0);

	}

	@Override
	public void stop() throws CorbaDeviceException {
		try {
			phantom.stop();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public void waitWhileBusy() throws CorbaDeviceException {
		try {
			phantom.waitWhileBusy();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		} catch (InterruptedException e) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(e));
		}

	}

	@Override
	public void close() throws CorbaDeviceException {
		try {
			phantom.close();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public Any getAttribute(String arg0) throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = phantom.getAttribute(arg0);
			any.insert_Value((Serializable) obj);
		} catch (Exception ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
		return any;
	}

	@Override
	public int getProtectionLevel() throws CorbaDeviceException {
		try {
			return phantom.getProtectionLevel();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public void setAttribute(String arg0, Any arg1) throws CorbaDeviceException {
		try {
			phantom.setAttribute(arg0, arg1);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public void setProtectionLevel(int arg0) throws CorbaDeviceException {
		try {
			phantom.setProtectionLevel(arg0);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		try {
			phantom.reconfigure();
		} catch (FactoryException e) {
			throw new CorbaFactoryException(gda.util.exceptionUtils.getFullStackMsg(e));
		}

	}

	@Override
	public String toFormattedString() throws CorbaDeviceException {
		return phantom.toFormattedString();
	}


	/**
	 * Deprecated method, so not included in the interface {@inheritDoc}
	 */
	@Override
	public void atEnd() throws CorbaDeviceException {

	}

	/**
	 * Deprecated method, so not included in the interface {@inheritDoc}
	 */
	@Override
	public void atStart() throws CorbaDeviceException {

	}

	@Override
	public void atLevelMoveStart() throws CorbaDeviceException {

	}

	@Override
	public void atLevelStart() throws CorbaDeviceException {

	}

	@Override
	public void atLevelEnd() throws CorbaDeviceException {

	}

	@Override
	public void atCommandFailure() throws CorbaDeviceException {

	}

}
