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

package gda.device.detector.etldetector.corba.impl;

import org.omg.CORBA.Any;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.EtlDetector;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.device.detector.etldetector.corba.CorbaEtlDetectorPOA;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed EtlDetector class
 */
public class EtldetectorImpl extends CorbaEtlDetectorPOA {
	//
	// Private reference to implementation object
	//
	private EtlDetector etldetector;
	private DetectorImpl detectorImpl;
	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 *
	 * @param etldetector
	 *            the Detector implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public EtldetectorImpl(EtlDetector etldetector, org.omg.PortableServer.POA poa) {
		this.etldetector = etldetector;
		this.poa = poa;
		detectorImpl = new DetectorImpl(etldetector, poa);
	}

	/**
	 * Get the implementation object
	 *
	 * @return the Detector implementation object
	 */
	public Detector _delegate() {
		return etldetector;
	}

	/**
	 * Set the implementation object.
	 *
	 * @param etldetector
	 *            set the Detector implementation object
	 */
	public void _delegate(EtlDetector etldetector) {
		this.etldetector = etldetector;
	}
	// implement EtlDetector methods delegation
	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public int getActualHV() throws CorbaDeviceException {
		try {
			return etldetector.getActualHV();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setHV(int mv) throws CorbaDeviceException {
		try {
			etldetector.setHV(mv);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public int getHV() throws CorbaDeviceException {
		try {
			return etldetector.getHV();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setLowerThreshold(int llim) throws CorbaDeviceException {
		try {
			etldetector.setLowerThreshold(llim);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public int getLowerThreshold() throws CorbaDeviceException {
		try {
			return etldetector.getLowerThreshold();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setUpperThreshold(int ulim) throws CorbaDeviceException {
		try {
			etldetector.setUpperThreshold(ulim);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public int getUpperThreshold() throws CorbaDeviceException {
		try {
			return etldetector.getUpperThreshold();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
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
		return detectorImpl._toString();
	}

	@Override
	public void asynchronousMoveTo(Any arg0) throws CorbaDeviceException {
		detectorImpl.asynchronousMoveTo(arg0);
	}

	@Override
	public void atPointEnd() throws CorbaDeviceException {
		detectorImpl.atPointEnd();
	}

	@Override
	public void atPointStart() throws CorbaDeviceException {
		detectorImpl.atPointStart();
	}

	@Override
	public void atScanEnd() throws CorbaDeviceException {
		detectorImpl.atScanEnd();
	}

	@Override
	public void atScanLineEnd() throws CorbaDeviceException {
		detectorImpl.atScanLineEnd();
	}

	@Override
	public void atScanLineStart() throws CorbaDeviceException {
		detectorImpl.atScanLineStart();
	}

	@Override
	public void atScanStart() throws CorbaDeviceException {
		detectorImpl.atScanStart();
	}

	@Override
	public String[] getExtraNames() {
		return detectorImpl.getExtraNames();
	}

	@Override
	public String[] getInputNames() {
		return detectorImpl.getInputNames();
	}

	@Override
	public int getLevel() {
		return detectorImpl.getLevel();
	}

	@Override
	public String[] getOutputFormat() {
		return detectorImpl.getOutputFormat();
	}

	@Override
	public Any getPosition() throws CorbaDeviceException {
		return detectorImpl.getPosition();
	}

	@Override
	public boolean isBusy() throws CorbaDeviceException {
		return detectorImpl.isBusy();
	}

	@Override
	public Any checkPositionValid(Any arg0) throws CorbaDeviceException {
		return detectorImpl.checkPositionValid(arg0);
	}

	@Override
	public void moveTo(Any arg0) throws CorbaDeviceException {
		detectorImpl.moveTo(arg0);
	}

	@Override
	public void setExtraNames(String[] arg0) {
		detectorImpl.setExtraNames(arg0);
	}

	@Override
	public void setInputNames(String[] arg0) {
		detectorImpl.setInputNames(arg0);
	}

	@Override
	public void setLevel(int arg0) {
		detectorImpl.setLevel(arg0);
	}

	@Override
	public void setOutputFormat(String[] arg0) {
		detectorImpl.setOutputFormat(arg0);
	}

	@Override
	public void stop() throws CorbaDeviceException {
		detectorImpl.stop();

	}

	@Override
	public void waitWhileBusy() throws CorbaDeviceException {
		detectorImpl.waitWhileBusy();
	}

	@Override
	public void atEnd() throws CorbaDeviceException {
		detectorImpl.atEnd();
	}

	@Override
	public void atStart() throws CorbaDeviceException {
		detectorImpl.atStart();
	}

	@Override
	public boolean isAt(Any arg0) throws CorbaDeviceException {
		try {
			return this.etldetector.isAt(arg0.extract_Value());
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	// implement inherit Device methods delegation
	@Override
	public void setAttribute(String attributeName, Any value) throws CorbaDeviceException {
		detectorImpl.setAttribute(attributeName, value);
	}

	@Override
	public Any getAttribute(String attributeName) throws CorbaDeviceException {
		return detectorImpl.getAttribute(attributeName);
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
	public int getProtectionLevel() throws CorbaDeviceException {
		return detectorImpl.getProtectionLevel();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws CorbaDeviceException {
		detectorImpl.setProtectionLevel(newLevel);
	}

	@Override
	public void atLevelMoveStart() throws CorbaDeviceException {
		detectorImpl.atLevelMoveStart();
	}

	@Override
	public void atCommandFailure() throws CorbaDeviceException {
		detectorImpl.atCommandFailure();
	}

	@Override
	public String toFormattedString() throws CorbaDeviceException {
		return detectorImpl.toFormattedString();
	}

	@Override
	public void atLevelStart() throws CorbaDeviceException {
		detectorImpl.atLevelStart();
	}

	@Override
	public void atLevelEnd() throws CorbaDeviceException {
		detectorImpl.atLevelEnd();
	}
}