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

package gda.device.detector.odccd.corba.impl;

import gda.device.Detector;
import gda.device.ODCCD;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.odccd.ODCCDImage;
import gda.device.detector.odccd.corba.CorbaODCCD;
import gda.device.detector.odccd.corba.CorbaODCCDHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import java.io.IOException;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the ODCCD class
 */
public class OdccdAdapter extends DetectorAdapter implements ODCCD, Detector, Findable, Scannable {

	private CorbaODCCD corbaODCCD;

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
	public OdccdAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaODCCD = CorbaODCCDHelper.narrow(obj);
	}

	@Override
	public void connect(String host) throws IOException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaODCCD.connect(host);
				return;
			} catch (COMM_FAILURE cf) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IOException("ODCCD.connect - CorbaDeviceException seen", ex);
			}
		}
		throw new IOException("Communication failure: retry failed");

	}

	@Override
	public void disconnect() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaODCCD.disconnect();
				return;
			} catch (COMM_FAILURE cf) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IllegalArgumentException("ODCCD.disconnect - CorbaDeviceException ", ex);
			}
		}
		throw new IllegalArgumentException("ODCCD.disconnect - Communication failure: retry failed");
	}

	@Override
	public boolean isConnected() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaODCCD.isConnected();

			} catch (COMM_FAILURE cf) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IllegalArgumentException("ODCCD.isConnected - CorbaDeviceException ", ex);
			}
		}
		throw new IllegalArgumentException("ODCCD.isConnected - Communication failure: retry failed");
	}

	@Override
	public String getDataName() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaODCCD.getDataName();

			} catch (COMM_FAILURE cf) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IllegalArgumentException("ODCCD.getDataName - CorbaDeviceException ", ex);
			}
		}
		throw new IllegalArgumentException("ODCCD.getDataName - Communication failure: retry failed");
	}

	@Override
	public double temperature() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaODCCD.temperature();

			} catch (COMM_FAILURE cf) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IllegalArgumentException("ODCCD.temperature - CorbaDeviceException ", ex);
			}
		}
		throw new IllegalArgumentException("ODCCD.temperature - Communication failure: retry failed");
	}

	@Override
	public double waterTemperature() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaODCCD.waterTemperature();

			} catch (COMM_FAILURE cf) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IllegalArgumentException("ODCCD.waterTemperature - CorbaDeviceException ", ex);
			}
		}
		throw new IllegalArgumentException("ODCCD.waterTemperature - Communication failure: retry failed");
	}

	@Override
	public void runScript(String command) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaODCCD.runScript(command);
				return;

			} catch (COMM_FAILURE cf) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IllegalArgumentException("ODCCD.runScript - CorbaDeviceException ", ex);
			}
		}
		throw new IllegalArgumentException("ODCCD.runScript - Communication failure: retry failed");
	}

	@Override
	public String shutter() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaODCCD.shutter();
			} catch (COMM_FAILURE cf) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IllegalArgumentException("ODCCD.shutter - CorbaDeviceException ", ex);
			}
		}
		throw new IllegalArgumentException("ODCCD.shutter - Communication failure: retry failed");
	}

	@Override
	public String openShutter() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaODCCD.openShutter();

			} catch (COMM_FAILURE cf) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IllegalArgumentException("ODCCD.openShutter - CorbaDeviceException ", ex);
			}
		}
		throw new IllegalArgumentException("ODCCD.openShutter - Communication failure: retry failed");
	}

	@Override
	public String closeShutter() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaODCCD.closeShutter();

			} catch (COMM_FAILURE cf) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IllegalArgumentException("ODCCD.closeShutter - CorbaDeviceException ", ex);
			}
		}
		throw new IllegalArgumentException("ODCCD.closeShutter - Communication failure: retry failed");
	}

	@Override
	public ODCCDImage readDataFromISDataBase(String pathname) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return (ODCCDImage) corbaODCCD.readDataFromISDataBase(pathname).extract_Value();

			} catch (COMM_FAILURE cf) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaODCCD = CorbaODCCDHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IllegalArgumentException("ODCCD.readDataFromISDataBase - CorbaDeviceException ", ex);
			}
		}
		throw new IllegalArgumentException("ODCCD.readDataFromISDataBase - Communication failure: retry failed");
	}
}
