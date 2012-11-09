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

package uk.ac.gda.server.ncd.detectorsystem.corba.impl;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.Device;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

import uk.ac.gda.server.ncd.detectorsystem.NcdDetector;
import uk.ac.gda.server.ncd.detectorsystem.corba.CorbaNcdDetectorsystem;
import uk.ac.gda.server.ncd.detectorsystem.corba.CorbaNcdDetectorsystemHelper;
import uk.ac.gda.server.ncd.subdetector.INcdSubDetector;

/**
 * A client side implementation of the adapter pattern for the Detector class
 */
public class DetectorsystemAdapter extends DetectorAdapter implements NcdDetector, Detector, Findable, Device, Scannable {
	private CorbaNcdDetectorsystem corbaNcdDetector;

	/**
	 * @param obj
	 * @param name
	 * @param netService
	 */
	public DetectorsystemAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaNcdDetector = CorbaNcdDetectorsystemHelper.narrow(obj);
	}

	@Override
	public String getDetectorType() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaNcdDetector.getDetectorType();
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdDetectorsystemHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdDetectorsystemHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void stop() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaNcdDetector.stop();
				return;
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdDetectorsystemHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdDetectorsystemHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void start() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaNcdDetector.start();
				return;
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdDetectorsystemHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdDetectorsystemHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void clear() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaNcdDetector.clear();
				return;
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdDetectorsystemHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdDetectorsystemHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getNumberOfFrames() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaNcdDetector.getNumberOfFrames();
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdDetectorsystemHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdDetectorsystemHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getTfgName() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaNcdDetector.getTfgName();
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdDetectorsystemHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdDetectorsystemHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
	
	@Override
	public void addDetector(INcdSubDetector det) throws DeviceException {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeDetector(INcdSubDetector det) {
		// TODO Auto-generated method stub
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}
}