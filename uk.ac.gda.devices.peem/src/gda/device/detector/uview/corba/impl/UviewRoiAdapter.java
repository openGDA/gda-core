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
import gda.device.UViewROI;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.uview.corba.CorbaUViewROI;
import gda.device.detector.uview.corba.CorbaUViewROIHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import java.io.IOException;

import org.omg.CORBA.COMM_FAILURE;

/**
 * A client side implementation of the adapter pattern for the UView class
 */
public class UviewRoiAdapter extends DetectorAdapter implements UViewROI, Findable, Scannable {

	private CorbaUViewROI corbaUViewROI;

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
	public UviewRoiAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaUViewROI = CorbaUViewROIHelper.narrow(obj);
	}

	@Override
	public void setBounds(int x, int y, int width, int height) throws IOException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaUViewROI.setBounds(x, y, width, height);

			} catch (COMM_FAILURE cf) {
				corbaUViewROI = CorbaUViewROIHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				// throw new IOException(ex.message);
			}
		}
		// throw new IOException("Communication failure: retry failed");
	}

	@Override
	public void setLocation(int x, int y) throws IOException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaUViewROI.setLocation(x, y);
				return;
			} catch (COMM_FAILURE cf) {
				corbaUViewROI = CorbaUViewROIHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IOException(ex.message);
			}
		}
		throw new IOException("Communication failure: retry failed");

	}

	@Override
	public void setSize(int x, int y) throws IOException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaUViewROI.setLocation(x, y);
			} catch (COMM_FAILURE cf) {
				corbaUViewROI = CorbaUViewROIHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new IOException(ex.message);
			}
		}
		throw new IOException("Communication failure: retry failed");

	}

}
