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

package gda.device.detector.mar345.corba.impl;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Mar345;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.mar345.corba.CorbaMar345;
import gda.device.detector.mar345.corba.CorbaMar345Helper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Mar345 class
 */
public class Mar345Adapter extends DetectorAdapter implements Mar345, Detector, Findable, Scannable {

	private CorbaMar345 corbaMar345;

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
	public Mar345Adapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaMar345 = CorbaMar345Helper.narrow(obj);
	}

	@Override
	public void appendToKeywordList(String keywords) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMar345.appendToKeywordList(keywords);

			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}

	}

	@Override
	public void clearKeywordList() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMar345.clearKeywordList();

			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}

	}

	@Override
	public void sendKeywordList() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMar345.sendKeywordList();

			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}

	}

	@Override
	public void sendKeywords(String keywords) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMar345.sendKeywords(keywords);

			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}

	}

	@Override
	public void erase() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMar345.erase();

			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
	}

	@Override
	public String getDirectory() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMar345.getDirectory();

			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
		return null;
	}

	@Override
	public String getFormat() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMar345.getFormat();

			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
		return null;
	}

	@Override
	public int getMode() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMar345.getMode();

			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
		return -1;
	}

	@Override
	public String getRootName() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMar345.getRootName();

			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
		return null;
	}

	@Override
	public void scan() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMar345.scan();
			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
	}

	@Override
	public void setDirectory(String directory) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMar345.setDirectory(directory);
			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
	}

	@Override
	public void setMode(int mode) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMar345.setMode(mode);

			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
	}

	@Override
	public void setRootName(String rootName) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMar345.setRootName(rootName);

			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
	}

	@Override
	public void setFormat(String format) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMar345.setFormat(format);

			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMar345.createsOwnFiles();
			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
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
				corbaMar345.endCollection();
				return;
			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
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
				corbaMar345.prepareForCollection();
				return;
			} catch (COMM_FAILURE cf) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMar345 = CorbaMar345Helper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
