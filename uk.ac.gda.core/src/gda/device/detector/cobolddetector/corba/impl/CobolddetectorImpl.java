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

package gda.device.detector.cobolddetector.corba.impl;

import gda.device.CoboldDetector;
import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.detector.cobolddetector.corba.CorbaCoboldDetectorPOA;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.CorbaFactoryException;

import java.util.ArrayList;

import org.omg.CORBA.Any;

/**
 * A server side implementation for a distributed CoboldDetector class
 */
public class CobolddetectorImpl extends CorbaCoboldDetectorPOA {
	//
	// Private reference to implementation object
	//
	private CoboldDetector cobolddetector;

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
	 * @param cobolddetector
	 *            the CoboldDetector implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public CobolddetectorImpl(CoboldDetector cobolddetector, org.omg.PortableServer.POA poa) {
		this.cobolddetector = cobolddetector;
		this.poa = poa;
		detectorImpl = new DetectorImpl(cobolddetector, poa);
		scannableImpl = new ScannableImpl(cobolddetector, poa);
		deviceImpl = new DeviceImpl(cobolddetector, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the CoboldDetector implementation object
	 */
	public CoboldDetector _delegate() {
		return cobolddetector;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param cobolddetector
	 *            set the CoboldDetector implementation object
	 */
	public void _delegate(CoboldDetector cobolddetector) {
		this.cobolddetector = cobolddetector;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void countAsync(double time) throws CorbaDeviceException {
		try {
			cobolddetector.countAsync(time);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void collectData() throws CorbaDeviceException {
		detectorImpl.collectData();
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
	public void setAttribute(String attributeName, Any value) throws CorbaDeviceException {
		deviceImpl.setAttribute(attributeName, value);
	}

	@Override
	public Any getAttribute(String attributeName) throws CorbaDeviceException {
		return deviceImpl.getAttribute(attributeName);
	}

	@Override
	public String getChannelLabel(int channel) throws CorbaDeviceException {
		try {
			return cobolddetector.getChannelLabel(channel);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void setChannelLabel(int channel, String label) throws CorbaDeviceException {
		try {
			cobolddetector.setChannelLabel(channel, label);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public Any readout() throws CorbaDeviceException {
		return detectorImpl.readout();
	}

	@Override
	public int getStatus() throws CorbaDeviceException {
		return detectorImpl.getStatus();
	}

	@Override
	public String[] getChannelLabelList() throws CorbaDeviceException {
		ArrayList<String> channelLabelList;
		String[] names = null;
		try {
			channelLabelList = cobolddetector.getChannelLabelList();
			names = new String[channelLabelList.size()];
			for (int i = 0; i < channelLabelList.size(); i++)
				names[i] = channelLabelList.get(i);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}

		return names;
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
	public void reconfigure() throws CorbaFactoryException {
		deviceImpl.reconfigure();
	}

	@Override
	public void close() throws CorbaDeviceException {
		deviceImpl.close();
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
			return this.cobolddetector.isAt(arg0.extract_Value());
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
	public void atCommandFailure() throws CorbaDeviceException {
		scannableImpl.atCommandFailure();
	}
	
	@Override
	public String toFormattedString() throws CorbaDeviceException {
		return scannableImpl.toFormattedString();
	}

}
