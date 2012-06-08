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

package gda.device.detector.corba.impl;

import gda.device.Detector;
import gda.device.Device;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.CorbaDetector;
import gda.device.detector.corba.CorbaDetectorHelper;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Detector class
 */
public class DetectorAdapter extends ScannableAdapter implements Detector, Findable, Device, Scannable {
	private CorbaDetector corbaDetector;

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
	public DetectorAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaDetector = CorbaDetectorHelper.narrow(obj);
	}

	@Override
	public void collectData() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDetector.collectData();
				return;
			} catch (COMM_FAILURE cf) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setCollectionTime(double time) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDetector.setCollectionTime(time);
				return;
			} catch (COMM_FAILURE cf) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getStatus() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaDetector.getStatus();
			} catch (COMM_FAILURE cf) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public Object readout() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = corbaDetector.readout();
				return any.extract_Value();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaDetector.getDataDimensions();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaDetector.createsOwnFiles();
			} catch (COMM_FAILURE cf) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void endCollection() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDetector.endCollection();
				return;
			} catch (COMM_FAILURE cf) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public void prepareForCollection() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDetector.prepareForCollection();
				return;
			} catch (COMM_FAILURE cf) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getDescription() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaDetector.getDescription();
			} catch (COMM_FAILURE cf) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getDetectorID() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaDetector.getDetectorID();
			} catch (COMM_FAILURE cf) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getDetectorType() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaDetector.getDetectorType();
			} catch (COMM_FAILURE cf) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaDetector.getCollectionTime();
			} catch (COMM_FAILURE cf) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDetector = CorbaDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
