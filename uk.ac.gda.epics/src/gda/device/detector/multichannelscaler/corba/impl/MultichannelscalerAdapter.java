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

package gda.device.detector.multichannelscaler.corba.impl;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.multichannelscaler.EpicsMcsSis3820;
import gda.device.detector.multichannelscaler.corba.CorbaMultiChannelScaler;
import gda.device.detector.multichannelscaler.corba.CorbaMultiChannelScalerHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the DataLogger class
 */
public class MultichannelscalerAdapter extends DetectorAdapter implements EpicsMcsSis3820, Detector, Findable,
		Scannable {
	private CorbaMultiChannelScaler corbaMultiChannelScaler;

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
	public MultichannelscalerAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaMultiChannelScaler = CorbaMultiChannelScalerHelper.narrow(obj);
	}

	@Override
	public int[] getData(int channel) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMultiChannelScaler.getChannelData(channel);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaMultiChannelScaler = CorbaMultiChannelScalerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMultiChannelScaler = CorbaMultiChannelScalerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int[][] getData() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMultiChannelScaler.getData();
			} catch (COMM_FAILURE cf) {
				corbaMultiChannelScaler = CorbaMultiChannelScalerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMultiChannelScaler = CorbaMultiChannelScalerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getElapsedTime() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMultiChannelScaler.getElapsedTime();
			} catch (COMM_FAILURE cf) {
				corbaMultiChannelScaler = CorbaMultiChannelScalerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMultiChannelScaler = CorbaMultiChannelScalerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getElapsedTimeFromEpics() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMultiChannelScaler.getElapsedTimeFromEpics();
			} catch (COMM_FAILURE cf) {
				corbaMultiChannelScaler = CorbaMultiChannelScalerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMultiChannelScaler = CorbaMultiChannelScalerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
