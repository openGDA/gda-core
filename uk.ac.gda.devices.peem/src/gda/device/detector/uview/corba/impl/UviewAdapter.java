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

package gda.device.detector.uview.corba.impl;

import gda.device.Scannable;
import gda.device.UView;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.uview.corba.CorbaUView;
import gda.device.detector.uview.corba.CorbaUViewHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import java.io.IOException;

import org.omg.CORBA.COMM_FAILURE;

/**
 * A client side implementation of the adapter pattern for the UView class
 */
public class UviewAdapter extends DetectorAdapter implements UView, Findable, Scannable {

	private CorbaUView corbaUView;

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
	public UviewAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaUView = CorbaUViewHelper.narrow(obj);
	}

	@Override
	public String shotSingleImage() throws IOException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaUView.shotSingleImage();

			} catch (COMM_FAILURE cf) {
				corbaUView = CorbaUViewHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
		// throw new IOException("Communication failure: retry failed");
		return "Can not finish the shotting";
	}

	@Override
	public void prepare() throws IOException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaUView.prepare();

			} catch (COMM_FAILURE cf) {
				corbaUView = CorbaUViewHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
		// throw new IOException("Communication failure: retry failed");
	}

	@Override
	public void connect(String host) throws IOException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaUView.connect(host);
				return;
			} catch (COMM_FAILURE cf) {
				corbaUView = CorbaUViewHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IOException(ex.message);
			}
		}
		throw new IOException("Communication failure: retry failed");

	}

	@Override
	public void disconnect() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaUView.disconnect();
				return;
			} catch (COMM_FAILURE cf) {
				corbaUView = CorbaUViewHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
		// throw new IOException("Communication failure: retry failed");

	}

	@Override
	public boolean isConnected() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaUView.isConnected();
			} catch (COMM_FAILURE cf) {
				corbaUView = CorbaUViewHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
		// throw new IOException("Communication failure: retry failed");
		return false;
	}

	@Override
	public int createROI(String nameROI) throws IOException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaUView.createROI(nameROI);

			} catch (COMM_FAILURE cf) {
				corbaUView = CorbaUViewHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
		// throw new IOException("Communication failure: retry failed");

		return 0;
	}

	@Override
	public Object readoutROI(String nameROI) throws IOException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = corbaUView.readoutROI(nameROI);
				return any.extract_Value();

			} catch (COMM_FAILURE cf) {
				corbaUView = CorbaUViewHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IOException(ex.message);
			}
		}
		// throw new IOException("Communication failure: retry failed");

		return null;
	}

	@Override
	public void setBoundsROI(String nameROI, int x, int y, int width, int height) throws IOException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaUView.setBoundsROI(nameROI, x, y, width, height);
				return;
			} catch (COMM_FAILURE cf) {
				corbaUView = CorbaUViewHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
		// throw new IOException("Communication failure: retry failed");
	}

	@Override
	public Object getBoundsROI(String nameROI) throws IOException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = corbaUView.getBoundsROI(nameROI);
				return any.extract_Value();

			} catch (COMM_FAILURE cf) {
				corbaUView = CorbaUViewHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IOException(ex.message);
			}
		}
		return null;
	}

	@Override
	public Object getHashROIs() throws IOException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = corbaUView.getHashROIs();
				return any.extract_Value();

			} catch (COMM_FAILURE cf) {
				corbaUView = CorbaUViewHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IOException(ex.message);
			}
		}

		return null;
	}

}
