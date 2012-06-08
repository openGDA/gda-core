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

import gda.device.Analyser;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.analyser.corba.CorbaAnalyser;
import gda.device.detector.analyser.corba.CorbaAnalyserHelper;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import java.io.Serializable;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Analyser class
 */
public class AnalyserAdapter extends DetectorAdapter implements Analyser, Detector, Findable, Scannable {
	private CorbaAnalyser corbaAnalyser;

	/**
	 * Create client side interface to the CORBA package.
	 * 
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public AnalyserAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaAnalyser = CorbaAnalyserHelper.narrow(obj);
	}

	@Override
	public void startAcquisition() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAnalyser.startAcquisition();
				return;
			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void stopAcquisition() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAnalyser.stopAcquisition();
				return;

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public void addRegionOfInterest(int regionIndex, double regionLow, double regionHigh, int regionBackground,
			double regionPreset, String regionName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAnalyser.addRegionsOfInterest(regionIndex, regionLow, regionHigh, regionBackground, regionPreset,
						regionName);
				return;

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void deleteRegionOfInterest(int regionIndex) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAnalyser.deleteRegionOfInterest(regionIndex);
				return;

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void clear() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaAnalyser.clear();
				return;

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public java.lang.Object getCalibrationParameters() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAnalyser.getCalibrationParameters().extract_Value();

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public java.lang.Object getData() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAnalyser.getData().extract_Value();

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public java.lang.Object getElapsedParameters() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAnalyser.getElapsedParameters().extract_Value();
			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public java.lang.Object getPresets() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAnalyser.getPresets().extract_Value();
			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public java.lang.Object getRegionsOfInterest() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAnalyser.getRegionsOfInterest().extract_Value();
			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double[][] getRegionsOfInterestCount() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAnalyser.getRegionsOfInterestCount();

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public long getSequence() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAnalyser.getSequence();

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setCalibration(java.lang.Object calibrate) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			try {
				any.insert_Value((Serializable) calibrate);
				corbaAnalyser.setCalibration(any);
				return;

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public void setData(java.lang.Object data) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			try {
				any.insert_Value((Serializable) data);
				corbaAnalyser.setData(any);
				return;

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public void setPresets(java.lang.Object data) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			try {
				any.insert_Value((Serializable) data);
				corbaAnalyser.setPresets(any);
				return;

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public void setRegionsOfInterest(java.lang.Object lowHigh) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			try {
				any.insert_Value((Serializable) lowHigh);
				corbaAnalyser.setRegionsOfInterest(any);
				return;

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public void setSequence(long sequence) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {

			try {
				corbaAnalyser.setSequence((int) sequence);
				return;

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public int getNumberOfRegions() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAnalyser.getNumberOfRegions();

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setNumberOfRegions(int regions) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {

			try {
				corbaAnalyser.setNumberOfRegions(regions);
				return;

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public long getNumberOfChannels() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaAnalyser.getNumberOfChannels();

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setNumberOfChannels(long channels) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {

			try {
				corbaAnalyser.setNumberOfChannels((int) channels);
				return;

			} catch (COMM_FAILURE cf) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaAnalyser = CorbaAnalyserHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
