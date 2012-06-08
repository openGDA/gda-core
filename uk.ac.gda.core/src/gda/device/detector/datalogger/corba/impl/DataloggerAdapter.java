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

package gda.device.detector.datalogger.corba.impl;

import gda.device.DataLogger;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.datalogger.corba.CorbaDataLogger;
import gda.device.detector.datalogger.corba.CorbaDataLoggerHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the DataLogger class
 */
public class DataloggerAdapter extends DetectorAdapter implements DataLogger, Detector, Findable, Scannable {
	private CorbaDataLogger corbaDataLogger;

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
	public DataloggerAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaDataLogger = CorbaDataLoggerHelper.narrow(obj);
	}

	@Override
	public int getNoOfChannels() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaDataLogger.getNoOfChannels();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaDataLogger = CorbaDataLoggerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDataLogger = CorbaDataLoggerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void connect() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDataLogger.connect();
				return;
			} catch (COMM_FAILURE cf) {
				corbaDataLogger = CorbaDataLoggerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDataLogger = CorbaDataLoggerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void disconnect() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDataLogger.disconnect();
				return;
			} catch (COMM_FAILURE cf) {
				corbaDataLogger = CorbaDataLoggerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDataLogger = CorbaDataLoggerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
