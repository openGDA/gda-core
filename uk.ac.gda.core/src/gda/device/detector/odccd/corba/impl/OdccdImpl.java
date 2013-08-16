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

package gda.device.detector.odccd.corba.impl;

import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.device.ODCCD;
import gda.device.detector.odccd.ODCCDImage;
import gda.device.detector.odccd.corba.CorbaODCCDPOA;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.CorbaFactoryException;
import gda.util.exceptionUtils;

import java.io.IOException;
import java.io.Serializable;

import org.omg.CORBA.Any;

/**
 * A server side implementation for a distributed ODCCDController class
 */
public class OdccdImpl extends CorbaODCCDPOA {

	private ODCCD odccd;
	private DetectorImpl detectorImpl;
	private ScannableImpl scannableImpl;
	private DeviceImpl deviceImpl;
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param odccd
	 *            the ODCCDController implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public OdccdImpl(ODCCD odccd, org.omg.PortableServer.POA poa) {
		this.odccd = odccd;
		this.poa = poa;
		detectorImpl = new DetectorImpl(odccd, poa);
		scannableImpl = new ScannableImpl(odccd, poa);
		deviceImpl = new DeviceImpl(odccd, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the ODCCDController implementation object
	 */
	public ODCCD _delegate() {
		return odccd;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param odccd
	 *            set the ODCCDController implementation object
	 */
	public void _delegate(ODCCD odccd) {
		this.odccd = odccd;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void connect(String host) throws CorbaDeviceException {
		try {
			odccd.connect(host);
		} catch (IOException ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));

		}

	}

	@Override
	public void disconnect() throws CorbaDeviceException {
		try {
			odccd.disconnect();
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public boolean isConnected() throws CorbaDeviceException {
		try {
			return odccd.isConnected();
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public String getDataName() throws CorbaDeviceException {
		try {
			return odccd.getDataName();
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public double temperature() throws CorbaDeviceException {
		try {
			return odccd.temperature();
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public double waterTemperature() throws CorbaDeviceException {
		try {
			return odccd.waterTemperature();
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public void runScript(String command) throws CorbaDeviceException {
		try {
			odccd.runScript(command);
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
		}

	}

	@Override
	public String shutter() throws CorbaDeviceException {
		try {
			return odccd.shutter();
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public String openShutter() throws CorbaDeviceException {
		try {
			return odccd.openShutter();
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public String closeShutter() throws CorbaDeviceException {
		try {
			return odccd.closeShutter();
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public org.omg.CORBA.Any readDataFromISDataBase(String pathname) throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			ODCCDImage image = odccd.readDataFromISDataBase(pathname);
			any.insert_Value(image);
			return any;
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public void collectData() throws CorbaDeviceException {
		try {
			odccd.collectData();
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public void setCollectionTime(double time) throws CorbaDeviceException {
		try {
			odccd.setCollectionTime(time);
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
		}

	}
	
	@Override
	public double getCollectionTime() throws CorbaDeviceException {
		try {
			return odccd.getCollectionTime();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public int getStatus() throws CorbaDeviceException {
		try {
			return odccd.getStatus();
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public Any readout() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = odccd.readout();
			any.insert_Value((Serializable) obj);
			return any;
		} catch (Exception ex) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(ex));
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
			return detectorImpl.getDataDimensions();
		} catch (Exception e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public boolean createsOwnFiles() throws CorbaDeviceException {
		try {
			return odccd.createsOwnFiles();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(de));
		}
	}

	@Override
	public void endCollection() throws CorbaDeviceException {
		try {
			odccd.endCollection();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(de));
		}
	}

	@Override
	public void prepareForCollection() throws CorbaDeviceException {
		try {
			odccd.prepareForCollection();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(exceptionUtils.getFullStackMsg(de));
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
		return scannableImpl.isAt(arg0);
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
