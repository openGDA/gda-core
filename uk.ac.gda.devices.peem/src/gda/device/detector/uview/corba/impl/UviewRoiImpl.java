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

package gda.device.detector.uview.corba.impl;

import gda.device.DeviceException;
import gda.device.UViewROI;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.device.detector.uview.corba.CorbaUViewROIPOA;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.CorbaFactoryException;

import java.io.Serializable;

import org.omg.CORBA.Any;

/**
 * A server side implementation for a distributed UView class
 */
public class UviewRoiImpl extends CorbaUViewROIPOA {

	private UViewROI ur;

	private DetectorImpl detectorImpl;

	private ScannableImpl scannableImpl;

	private DeviceImpl deviceImpl;

	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param ur
	 *            the ur implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public UviewRoiImpl(UViewROI ur, org.omg.PortableServer.POA poa) {
		this.ur = ur;
		this.poa = poa;
		detectorImpl = new DetectorImpl(ur, poa);
		scannableImpl = new ScannableImpl(ur, poa);
		deviceImpl = new DeviceImpl(ur, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the UView implementation object
	 */
	public UViewROI _delegate() {
		return ur;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param ur
	 */
	public void _delegate(UViewROI ur) {
		this.ur = ur;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void collectData() throws CorbaDeviceException {
		try {
			ur.collectData();
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setCollectionTime(double time) throws CorbaDeviceException {
		try {
			ur.setCollectionTime(time);
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}
	
	@Override
	public double getCollectionTime() throws CorbaDeviceException {
		try {
			return ur.getCollectionTime();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public int getStatus() throws CorbaDeviceException {
		try {
			return ur.getStatus();
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public Any readout() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = ur.readout();
			any.insert_Value((Serializable) obj);
			return any;
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
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
	public int[] getDataDimensions() throws CorbaDeviceException {
		try {
			int[] dimension = detectorImpl.getDataDimensions();
			return dimension;
		} catch (Exception e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public boolean createsOwnFiles() throws CorbaDeviceException {
		try {
			return ur.createsOwnFiles();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void endCollection() throws CorbaDeviceException {
		try {
			ur.endCollection();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void prepareForCollection() throws CorbaDeviceException {
		try {
			ur.prepareForCollection();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
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
	public void setBounds(int x, int y, int width, int height) throws CorbaDeviceException {
		try {
			ur.setBounds(x, y, width, height);
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setLocation(int x, int y) throws CorbaDeviceException {
		try {
			ur.setLocation(x, y);
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setSize(int width, int height) throws CorbaDeviceException {
		try {
			ur.setSize(width, height);
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
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
	public Any getPosition() throws CorbaDeviceException {
		return scannableImpl.getPosition();
	}

	@Override
	public boolean isBusy() throws CorbaDeviceException {
		return scannableImpl.isBusy();
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
	public void stop() throws CorbaDeviceException {
		scannableImpl.stop();

	}

	@Override
	public void waitWhileBusy() throws CorbaDeviceException {
		scannableImpl.waitWhileBusy();

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
	public boolean isAt(Any arg0) throws CorbaDeviceException {
		try {
			return this.ur.isAt(arg0.extract_Value());
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String getDescription() throws CorbaDeviceException {
		return detectorImpl.getDescription();
	}

	@Override
	public String getDetectorID() throws CorbaDeviceException {
		return detectorImpl.getDetectorID();
	}

	@Override
	public String getDetectorType() throws CorbaDeviceException {
		return detectorImpl.getDetectorType();
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
	public void atLevelStart() throws CorbaDeviceException {
		scannableImpl.atLevelStart();
	}
	
	@Override
	public void atCommandFailure() throws CorbaDeviceException {
		scannableImpl.atCommandFailure();
	}
	
	@Override
	public String toFormattedString() throws CorbaDeviceException {
		return scannableImpl.toFormattedString();
	}

}
