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

package uk.ac.gda.server.ncd.detectorsystem.corba.impl;

import org.omg.CORBA.Any;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.CorbaFactoryException;
import uk.ac.gda.server.ncd.detectorsystem.NcdDetector;
import uk.ac.gda.server.ncd.detectorsystem.corba.CorbaNcdDetectorsystemPOA;

/**
 * A server side implementation for a distributed Detector class
 */
public class DetectorsystemImpl extends CorbaNcdDetectorsystemPOA {
	//
	// Private reference to implementation object
	//
	private NcdDetector ncdDetector;
	private DetectorImpl detectorImpl;
	private ScannableImpl scannableImpl;

	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Constructor
	 * 
	 * @param ncdDetector
	 *            the Detector
	 * @param poa
	 *            the POA
	 */
	public DetectorsystemImpl(NcdDetector ncdDetector, org.omg.PortableServer.POA poa) {
		this.ncdDetector = ncdDetector;
		this.poa = poa;
		detectorImpl = new DetectorImpl(ncdDetector, poa);
		scannableImpl = new ScannableImpl(ncdDetector, poa);
		deviceImpl = new DeviceImpl(ncdDetector, poa);
	}

	/**
	 * Get the delegate
	 * 
	 * @return the Detector delegate
	 */
	public Detector _delegate() {
		return ncdDetector;
	}

	/**
	 * Set the delegate
	 * 
	 * @param ncdDetector
	 *            the Detector delegate
	 */
	public void _delegate(NcdDetector ncdDetector) {
		this.ncdDetector = ncdDetector;
	}

	/**
	 * _default_POA method
	 * 
	 * @return the POA
	 */
	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void setAttribute(String attributeName, Any value) throws CorbaDeviceException {
		detectorImpl.setAttribute(attributeName, value);
	}

	@Override
	public Any getAttribute(String attributeName) throws CorbaDeviceException {
		return detectorImpl.getAttribute(attributeName);
	}

	@Override
	public int getStatus() throws CorbaDeviceException {
		return detectorImpl.getStatus();
	}

	@Override
	public Any readout() throws CorbaDeviceException {
		return detectorImpl.readout();
	}

	@Override
	public void clear() throws CorbaDeviceException {
		try {
			ncdDetector.clear();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void start() throws CorbaDeviceException {
		try {
			ncdDetector.start();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void collectData() throws CorbaDeviceException {
		detectorImpl.collectData();
	}

	@Override
	public int[] getDataDimensions() throws CorbaDeviceException {
		return detectorImpl.getDataDimensions();
	}

	@Override
	public void setCollectionTime(double time) throws CorbaDeviceException {
		detectorImpl.setCollectionTime(time);
	}
	
	@Override
	public double getCollectionTime() throws CorbaDeviceException {
		return detectorImpl.getCollectionTime();
	}	

	@Override
	public boolean createsOwnFiles() throws CorbaDeviceException {
		try {
			return ncdDetector.createsOwnFiles();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void endCollection() throws CorbaDeviceException {
		try {
			ncdDetector.endCollection();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void prepareForCollection() throws CorbaDeviceException {
		try {
			ncdDetector.prepareForCollection();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void configure() throws CorbaFactoryException {
		detectorImpl.configure();
	}

	@Override
	public boolean isConfigured() throws CorbaDeviceException {
		return detectorImpl.isConfigured();
	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		detectorImpl.reconfigure();
	}

	@Override
	public void close() throws CorbaDeviceException {
		detectorImpl.close();
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

	/**
	 * @param o
	 * @param arg
	 */
	public void update(java.lang.Object o, java.lang.Object arg) {
		deviceImpl.update(o, arg);
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
	public void atLevelEnd() throws CorbaDeviceException {
		scannableImpl.atLevelEnd();
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
	public String getTfgName() throws CorbaDeviceException {
		try {
			return ncdDetector.getTfgName();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}
	
	@Override
	public int getNumberOfFrames() throws CorbaDeviceException {
		try {
			return ncdDetector.getNumberOfFrames();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}
}