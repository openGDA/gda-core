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
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.cobolddetector.corba.CorbaCoboldDetector;
import gda.device.detector.cobolddetector.corba.CorbaCoboldDetectorHelper;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import java.util.ArrayList;

import org.omg.CORBA.Any;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Cobolddetector class
 */
public class CobolddetectorAdapter extends DetectorAdapter implements CoboldDetector, Detector, Findable, Scannable {
	private CorbaCoboldDetector corbaCoboldDetector;

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
	public CobolddetectorAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(obj);
	}

	@Override
	public void collectData() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCoboldDetector.collectData();
				// the lines below must be performed client-side as
				// observers are client-side GUI objects
				// fill an array with the new data
				// double[] data = corbaCoboldDetector.readout();
				// print out data and notify observers
				// notifyIObservers(this, data);
				return;
			} catch (COMM_FAILURE cf) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void countAsync(double time) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCoboldDetector.countAsync(time);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getChannelLabel(int channel) throws DeviceException {

		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCoboldDetector.getChannelLabel(channel);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setChannelLabel(int channel, String label) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCoboldDetector.setChannelLabel(channel, label);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public ArrayList<String> getChannelLabelList() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String[] names = corbaCoboldDetector.getChannelLabelList();
				ArrayList<String> channelLabelList = new ArrayList<String>();
				for (i = 0; i < names.length; i++) {
					channelLabelList.add(names[i]);
				}

				return channelLabelList;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaCoboldDetector.setAttribute(attributeName, (Any) value);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaCoboldDetector.getAttribute(attributeName);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaCoboldDetector = CorbaCoboldDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
