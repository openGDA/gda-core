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

package gda.device.detector.xspress.corba.impl;

import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.device.detector.xspress.XspressDetector;
import gda.device.detector.xspress.corba.CorbaDetectorElement;
import gda.device.detector.xspress.corba.CorbaXspressPOA;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.CorbaFactoryException;

import org.omg.CORBA.Any;

import uk.ac.gda.beans.xspress.DetectorElement;

/**
 * A server side implementation for a distributed Xspress class
 */
public class XspressImpl extends CorbaXspressPOA {
	//
	// Private reference to implementation object
	//
	private XspressDetector xspress;

	private DetectorImpl detectorImpl;
	private ScannableImpl scannableImpl;
	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param xspress
	 *            the Xspress implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public XspressImpl(XspressDetector xspress, org.omg.PortableServer.POA poa) {
		this.xspress = xspress;
		this.poa = poa;
		detectorImpl = new DetectorImpl(xspress, poa);
		scannableImpl = new ScannableImpl(xspress, poa);
		deviceImpl = new DeviceImpl(xspress, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Xspress implementation object
	 */
	public XspressDetector _delegate() {
		return xspress;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param xspress
	 *            set the Xspress implementation object
	 */
	public void _delegate(XspressDetector xspress) {
		this.xspress = xspress;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public int getNumberOfDetectors() throws CorbaDeviceException {
		try {
			return xspress.getNumberOfDetectors();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int[][][] getMCData(int time) throws CorbaDeviceException {
		try {
			return xspress.getMCData(time);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setDetectorWindow(int detector, int start, int end) throws CorbaDeviceException {
		try {
			xspress.setDetectorWindow(detector, start, end);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public CorbaDetectorElement getDetector(int which) throws CorbaDeviceException {
		CorbaDetectorElement cd = null;
		try {
			DetectorElement d = xspress.getDetector(which);
			cd = DetectorElementConverter.toCorbaDetectorElement(d);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
		return cd;
	}

	@Override
	public void saveDetectors(String filename) throws CorbaDeviceException {
		try {
			xspress.saveDetectors(filename);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void loadAndInitializeDetectors(String filename) throws CorbaDeviceException {
		try {
			xspress.loadAndInitializeDetectors(filename);
		} catch (Exception e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void start() throws CorbaDeviceException {
		try {
			xspress.start();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	// implement inherited Detector interface methods delegation
	@Override
	public int getStatus() throws CorbaDeviceException {
		return detectorImpl.getStatus();
	}

	@Override
	public Any readout() throws CorbaDeviceException {
		return detectorImpl.readout();
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
		return detectorImpl.createsOwnFiles();
	}

	@Override
	public void endCollection() throws CorbaDeviceException {
		detectorImpl.endCollection();
	}

	@Override
	public void prepareForCollection() throws CorbaDeviceException {
		detectorImpl.prepareForCollection();
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

	// implement inherited Scannable interface methods delegation
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
	public Any getPosition() throws CorbaDeviceException {
		return scannableImpl.getPosition();
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
	public void stop() throws CorbaDeviceException {
		scannableImpl.stop();

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


	@Override
	public boolean isAt(Any arg0) throws CorbaDeviceException {
		try {
			return this.xspress.isAt(arg0.extract_Value());
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	// implement inherit Device methods delegation
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
		detectorImpl.reconfigure();
	}

	@Override
	public void close() throws CorbaDeviceException {
		detectorImpl.close();
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
	public void clear() throws CorbaDeviceException {
		try {
			xspress.clear();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String getResGrade() throws CorbaDeviceException {
		try {
			return xspress.getResGrade();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void setResGrade(String grade) throws CorbaDeviceException {
		try {
			xspress.setResGrade(grade);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String getReadoutMode() throws CorbaDeviceException {
		try {
			return xspress.getReadoutMode();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void setReadoutMode(String mode) throws CorbaDeviceException {
		try {
			xspress.setReadoutMode(mode);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public int[] getRawScalerData() throws CorbaDeviceException {
		try {
			return xspress.getRawScalerData();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public double getDeadtimeCalculationEnergy() throws CorbaDeviceException {
		try {
			return xspress.getDeadtimeCalculationEnergy();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void setDeadtimeCalculationEnergy(double arg0) throws CorbaDeviceException {
		try {
			xspress.setDeadtimeCalculationEnergy(arg0);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

}
