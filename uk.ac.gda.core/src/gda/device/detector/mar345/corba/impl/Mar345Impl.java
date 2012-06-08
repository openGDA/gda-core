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

package gda.device.detector.mar345.corba.impl;

import gda.device.DeviceException;
import gda.device.Mar345;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.device.detector.mar345.corba.CorbaMar345POA;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.CorbaFactoryException;

import java.io.Serializable;

import org.omg.CORBA.Any;

/**
 * A server side implementation for a distributed Mar345Detector class
 */
public class Mar345Impl extends CorbaMar345POA {

	private Mar345 mar345;
	private DetectorImpl detectorImpl;
	private ScannableImpl scannableImpl;
	private DeviceImpl deviceImpl;
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param mar345
	 *            the Mar345Detector implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public Mar345Impl(Mar345 mar345, org.omg.PortableServer.POA poa) {
		this.mar345 = mar345;
		this.poa = poa;
		detectorImpl = new DetectorImpl(mar345, poa);
		scannableImpl = new ScannableImpl(mar345, poa);
		deviceImpl = new DeviceImpl(mar345, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Mar345 implementation object
	 */
	public Mar345 _delegate() {
		return mar345;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param mar345
	 *            set the Mar345 implementation object
	 */
	public void _delegate(Mar345 mar345) {
		this.mar345 = mar345;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void collectData() throws CorbaDeviceException {
		try {
			mar345.collectData();
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setCollectionTime(double time) throws CorbaDeviceException {
		try {
			mar345.setCollectionTime(time);
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}
	
	@Override
	public double getCollectionTime() throws CorbaDeviceException {
		try {
			return mar345.getCollectionTime();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public int getStatus() throws CorbaDeviceException {
		try {
			return mar345.getStatus();
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public Any readout() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = mar345.readout();
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
			return detectorImpl.getDataDimensions();
		} catch (Exception e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void appendToKeywordList(String keywords) throws CorbaDeviceException {
		try {
			mar345.appendToKeywordList(keywords);
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void clearKeywordList() throws CorbaDeviceException {
		try {
			mar345.clearKeywordList();
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void sendKeywordList() throws CorbaDeviceException {
		try {
			mar345.sendKeywordList();
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void sendKeywords(String keywords) throws CorbaDeviceException {
		try {
			mar345.sendKeywords(keywords);
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void erase() throws CorbaDeviceException {
		try {
			mar345.erase();
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void scan() throws CorbaDeviceException {
		try {
			mar345.scan();
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setDirectory(String directory) throws CorbaDeviceException {
		try {
			mar345.setDirectory(directory);
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setMode(int mode) throws CorbaDeviceException {
		try {
			mar345.setMode(mode);
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setFormat(String format) throws CorbaDeviceException {
		try {
			mar345.setFormat(format);
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setRootName(String rootName) throws CorbaDeviceException {
		try {
			mar345.setRootName(rootName);
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public String getDirectory() throws CorbaDeviceException {
		try {
			return mar345.getDirectory();
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getMode() throws CorbaDeviceException {
		try {
			return mar345.getMode();
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public String getFormat() throws CorbaDeviceException {
		try {
			return mar345.getFormat();
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public String getRootName() throws CorbaDeviceException {
		try {
			return mar345.getRootName();
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public boolean createsOwnFiles() throws CorbaDeviceException {
		try {
			return mar345.createsOwnFiles();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void endCollection() throws CorbaDeviceException {
		try {
			mar345.endCollection();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void prepareForCollection() throws CorbaDeviceException {
		try {
			mar345.prepareForCollection();
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

}
