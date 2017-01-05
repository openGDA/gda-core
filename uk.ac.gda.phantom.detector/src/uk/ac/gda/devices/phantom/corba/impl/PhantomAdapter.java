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

package uk.ac.gda.devices.phantom.corba.impl;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.phantom.corba.CorbaPhantom;
import gda.device.detector.phantom.corba.CorbaPhantomHelper;
import gda.factory.corba.util.NetService;
import uk.ac.gda.devices.phantom.Phantom;

/**
 * A client side implementation of the adapter pattern for the motor class
 */
public class PhantomAdapter extends DetectorAdapter implements Phantom {

	private static final long serialVersionUID = -4229939347699682147L;

	private CorbaPhantom corbaPhantom;

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
	public PhantomAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaPhantom = CorbaPhantomHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public void setUpForCollection(int numberOfFrames, int framesPerSecond, int width, int height)
			throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaPhantom.setUpForCollection(numberOfFrames, framesPerSecond, width, height);
				return;
			} catch (COMM_FAILURE cf) {
				corbaPhantom = CorbaPhantomHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPhantom = CorbaPhantomHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("PrepareForCollection error in the PhantomAdapter");
	}

	@Override
	public Object retrieveData(int cineNumber, int start, int count) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
		try {
			org.omg.CORBA.Any any = corbaPhantom.retrieveData(cineNumber, start, count);
			return any.extract_Value();
		} catch (COMM_FAILURE cf) {
			corbaPhantom = CorbaPhantomHelper.narrow(netService.reconnect(name));
		} catch (TRANSIENT ct) {
			corbaPhantom = CorbaPhantomHelper.narrow(netService.reconnect(name));
		} catch (CorbaDeviceException ex) {
			throw new DeviceException(ex.message);
		}

	}
	throw new DeviceException("retrieveData error in the PhantomAdapter");
	}

	@Override
	public String command(String commandString) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPhantom.command(commandString);
			} catch (COMM_FAILURE cf) {
				corbaPhantom = CorbaPhantomHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPhantom = CorbaPhantomHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}

		}
		throw new DeviceException("retrieveData error in the PhantomAdapter");
	}

}
