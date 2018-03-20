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

package gda.device.detector.analyser.corba.impl;

import java.io.Serializable;

import org.omg.CORBA.Any;

import gda.device.Analyser;
import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.analyser.corba.CorbaAnalyserPOA;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed Analyser class
 */
public class AnalyserImpl extends CorbaAnalyserPOA {
	//
	// Private reference to implementation object
	//
	private Analyser analyser;
	private DetectorImpl detectorImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 *
	 * @param analyser
	 *            the Analyser implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public AnalyserImpl(Analyser analyser, org.omg.PortableServer.POA poa) {
		this.analyser = analyser;
		this.poa = poa;
		detectorImpl = new DetectorImpl(analyser, poa);
	}

	/**
	 * Get the implementation object
	 *
	 * @return the Analyser implementation object
	 */
	public Analyser _delegate() {
		return analyser;
	}

	/**
	 * Set the implementation object.
	 *
	 * @param analyser
	 *            set the Analyser implementation object
	 */
	public void _delegate(Analyser analyser) {
		this.analyser = analyser;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void startAcquisition() throws CorbaDeviceException {
		try {
			analyser.startAcquisition();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void stopAcquisition() throws CorbaDeviceException {
		try {
			analyser.stopAcquisition();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void addRegionsOfInterest(int regionIndex, double regionLow, double regionHigh, int regionBackground,
			double regionPreset, String regionName) throws CorbaDeviceException {
		try {
			analyser
					.addRegionOfInterest(regionIndex, regionLow, regionHigh, regionBackground, regionPreset, regionName);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void deleteRegionOfInterest(int regionIndex) throws CorbaDeviceException {
		try {
			analyser.deleteRegionOfInterest(regionIndex);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void clear() throws CorbaDeviceException {
		try {
			analyser.clear();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public org.omg.CORBA.Any getCalibrationParameters() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = analyser.getCalibrationParameters();
			any.insert_Value((Serializable) obj);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
		return any;

	}

	@Override
	public Any getData() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = analyser.getData();
			any.insert_Value((Serializable) obj);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
		return any;

	}

	@Override
	public Any getElapsedParameters() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = analyser.getElapsedParameters();
			any.insert_Value((Serializable) obj);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
		return any;

	}

	@Override
	public Any getPresets() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = analyser.getPresets();
			any.insert_Value((Serializable) obj);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
		return any;

	}

	@Override
	public double[][] getRegionsOfInterestCount() throws CorbaDeviceException {

		try {
			return analyser.getRegionsOfInterestCount();

		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public Any getRegionsOfInterest() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = analyser.getRegionsOfInterest();
			any.insert_Value((Serializable) obj);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
		return any;

	}

	@Override
	public int getSequence() throws CorbaDeviceException {

		try {
			return (int) analyser.getSequence();

		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setCalibration(Any calibrate) throws CorbaDeviceException {
		try {
			analyser.setCalibration(calibrate.extract_Value());
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setData(Any data) throws CorbaDeviceException {
		try {
			analyser.setData(data.extract_Value());
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setPresets(Any data) throws CorbaDeviceException {
		try {
			analyser.setPresets(data.extract_Value());
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setRegionsOfInterest(Any lowHigh) throws CorbaDeviceException {
		try {
			analyser.setRegionsOfInterest(lowHigh.extract_Value());
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setSequence(int seq) throws CorbaDeviceException {
		try {
			analyser.setSequence(seq);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void collectData() throws CorbaDeviceException {
		try {
			analyser.collectData();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

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
	public int getStatus() throws CorbaDeviceException {
		try {
			return analyser.getStatus();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public Any readout() throws CorbaDeviceException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		try {
			java.lang.Object obj = analyser.readout();
			any.insert_Value((Serializable) obj);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
		return any;
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
	public int getNumberOfRegions() throws CorbaDeviceException {
		try {
			return analyser.getNumberOfRegions();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setNumberOfRegions(int seq) throws CorbaDeviceException {
		try {
			analyser.setNumberOfRegions(seq);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public int[] getDataDimensions() throws CorbaDeviceException {
		try {
			int[] dimension = detectorImpl.getDataDimensions();
			return dimension;
		} catch (Exception ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getNumberOfChannels() throws CorbaDeviceException {
		try {
			return (int) analyser.getNumberOfChannels();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setNumberOfChannels(int channels) throws CorbaDeviceException {
		try {
			analyser.setNumberOfChannels(channels);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public boolean createsOwnFiles() throws CorbaDeviceException {
		try {
			return analyser.createsOwnFiles();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void endCollection() throws CorbaDeviceException {
		try {
			analyser.endCollection();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void prepareForCollection() throws CorbaDeviceException {
		try {
			analyser.prepareForCollection();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

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
	public void atScanStart() throws CorbaDeviceException {
		detectorImpl.atScanLineStart();

	}

	@Override
	public void atScanLineStart() throws CorbaDeviceException {
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
	public Any getPosition() throws CorbaDeviceException {
		return detectorImpl.getPosition();
	}

	@Override
	public boolean isBusy() throws CorbaDeviceException {
		return detectorImpl.isBusy();
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
	public void stop() throws CorbaDeviceException {
		detectorImpl.stop();

	}

	@Override
	public void waitWhileBusy() throws CorbaDeviceException {
		detectorImpl.waitWhileBusy();

	}

	@Override
	public void close() throws CorbaDeviceException {
		detectorImpl.close();

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
	public String[] getOutputFormat() {
		return detectorImpl.getOutputFormat();
	}

	@Override
	public Any checkPositionValid(Any arg0) throws CorbaDeviceException {
		return detectorImpl.checkPositionValid(arg0);
	}

	@Override
	public void setOutputFormat(String[] arg0) {
		detectorImpl.setOutputFormat(arg0);
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
	public boolean isAt(Any arg0) throws CorbaDeviceException {
		return detectorImpl.isAt(arg0);
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
