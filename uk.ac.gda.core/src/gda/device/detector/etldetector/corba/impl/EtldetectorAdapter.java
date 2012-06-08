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

import gda.device.Detector;
import gda.device.Device;
import gda.device.DeviceException;
import gda.device.EtlDetector;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.etldetector.corba.CorbaEtlDetector;
import gda.device.detector.etldetector.corba.CorbaEtlDetectorHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.Object;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Detector class
 */
public class EtldetectorAdapter extends DetectorAdapter implements EtlDetector, Detector, Findable, Device , Scannable{
	private CorbaEtlDetector corbaEtlDetector;

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
	public EtldetectorAdapter(Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaEtlDetector = CorbaEtlDetectorHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public int getActualHV() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaEtlDetector.getActualHV();

			} catch (COMM_FAILURE cf) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getHV() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaEtlDetector.getHV();
			} catch (COMM_FAILURE cf) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setHV(int hv) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaEtlDetector.setHV(hv);
				return;
			} catch (COMM_FAILURE cf) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setLowerThreshold(int llim) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaEtlDetector.setLowerThreshold(llim);
				return;
			} catch (COMM_FAILURE cf) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getLowerThreshold() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaEtlDetector.getLowerThreshold();
			} catch (COMM_FAILURE cf) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setUpperThreshold(int ulim) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaEtlDetector.setUpperThreshold(ulim);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getUpperThreshold() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaEtlDetector.getUpperThreshold();

			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaEtlDetector = CorbaEtlDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

}
