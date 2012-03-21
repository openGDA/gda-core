/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector.xmap.corba.impl;

import gda.device.DeviceException;
import gda.device.XmapDetector;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.CorbaFactoryException;

import org.omg.CORBA.Any;
import org.omg.PortableServer.POA;


/**
 *
 */
public class XmapImpl extends gda.device.detector.xmap.corba.CorbaXmapDetectorPOA {

	private XmapDetector xmapDetector;
	private POA poa;
	private DetectorImpl detectorImpl;
	private ScannableImpl scannableImpl;
	private DeviceImpl deviceImpl;

	/**
	 * @param xmapDetector
	 * @param poa
	 */
	public XmapImpl(XmapDetector xmapDetector, org.omg.PortableServer.POA poa) {
		this.xmapDetector = xmapDetector;
		this.poa = poa;
		detectorImpl = new DetectorImpl(xmapDetector, poa);
		scannableImpl = new ScannableImpl(xmapDetector, poa);
		deviceImpl = new DeviceImpl(xmapDetector, poa);
	}
	
	/**
	 * Get the implementation object
	 * 
	 * @return the Detector implementation object
	 */
	public XmapDetector _delegate() {
		return xmapDetector;
	}

	/**
	 * Set the implementation object.
	 * @param xmapDetector 
	 */
	public void _delegate(XmapDetector xmapDetector) {
		this.xmapDetector = xmapDetector;
	}
	// implement EtlDetector methods delegation
	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}
	@Override
	public void clear() throws CorbaDeviceException {
		try {
			xmapDetector.clear();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void clearAndStart() throws CorbaDeviceException {
		try {
			xmapDetector.clearAndStart();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getAcquisitionTime() throws CorbaDeviceException {
		try {
			return xmapDetector.getAcquisitionTime();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int[] getChannelData(int arg0) throws CorbaDeviceException {
		try {
			return xmapDetector.getData(arg0);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int[][] getData() throws CorbaDeviceException {
		try {
			return xmapDetector.getData();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getNumberOfBins() throws CorbaDeviceException {
		try {
			return xmapDetector.getNumberOfBins();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getReadRate() throws CorbaDeviceException {
		try {
			return xmapDetector.getReadRate();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getRealTime() throws CorbaDeviceException {
		try {
			return xmapDetector.getRealTime();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getStatusRate() throws CorbaDeviceException {
		try {
			return xmapDetector.getStatusRate();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setAcquisitionTime(double time) throws CorbaDeviceException {
		try {
			xmapDetector.setAcquisitionTime(time);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setNumberOfBins(int numberOfBins) throws CorbaDeviceException {
		try {
			xmapDetector.setNumberOfBins(numberOfBins);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setReadRate(double readRate) throws CorbaDeviceException {
		try {
			xmapDetector.setReadRate(readRate);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setStatusRate(double statusRate) throws CorbaDeviceException {
		try {
			xmapDetector.setStatusRate(statusRate);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void start() throws CorbaDeviceException {
		try {
			xmapDetector.start();
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

	/*
	public boolean isActive() {
		return detectorImpl.isActive();
	}
	*/
	
	/*
	public void setActive(boolean activate) {
		detectorImpl.setActive(activate);
	}
	*/
	
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
			return this.xmapDetector.isAt(arg0.extract_Value());
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
	public int getNumberOfROIs() throws CorbaDeviceException {
		try {
			return xmapDetector.getNumberOfROIs();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double[] getROIsSum() throws CorbaDeviceException {
		try {
			return xmapDetector.getROIsSum();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setNumberOfROIs(int arg0) throws CorbaDeviceException {
		//Do nothing
	}

	@Override
	public void setROIs(double[][] arg0) throws CorbaDeviceException {
		try {
			xmapDetector.setROIs(arg0);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
		
	}

	@Override
	public int getNumberOfMca() throws CorbaDeviceException {
		try {
			return xmapDetector.getNumberOfMca();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setNthROI(double[][] rois, int roiIndex) throws CorbaDeviceException {
		try {
			xmapDetector.setNthROI(rois, roiIndex);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}
}
