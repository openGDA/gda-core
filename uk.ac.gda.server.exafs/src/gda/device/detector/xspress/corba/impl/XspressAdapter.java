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

package gda.device.detector.xspress.corba.impl;

import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.xspress.XspressDetector;
import gda.device.detector.xspress.corba.CorbaDetectorElement;
import gda.device.detector.xspress.corba.CorbaXspress;
import gda.device.detector.xspress.corba.CorbaXspressHelper;
import gda.factory.corba.util.NetService;

import java.util.ArrayList;

import org.omg.CORBA.Any;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

import uk.ac.gda.beans.xspress.DetectorElement;

/**
 * A client side implementation of the adapter pattern for the Xspress class
 */
public class XspressAdapter extends DetectorAdapter implements XspressDetector {
	private CorbaXspress corbaXspress;

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
	public XspressAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaXspress = CorbaXspressHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public int getNumberOfDetectors() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXspress.getNumberOfDetectors();
			} catch (COMM_FAILURE cf) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int[][][] getMCData(int time) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXspress.getMCData(time);
			} catch (COMM_FAILURE cf) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setDetectorWindow(int which, int start, int end) throws DeviceException {
		try {
			corbaXspress.setDetectorWindow(which, start, end);
		} catch (CorbaDeviceException e) {
			throw new DeviceException(e.message);
		} catch (COMM_FAILURE cf) {
			corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
		} catch (TRANSIENT ct) {
			corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
		}
	}

	@Override
	public DetectorElement getDetector(int which) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				CorbaDetectorElement cd = corbaXspress.getDetector(which);
				DetectorElement d = DetectorElementConverter.toDetectorElement(cd);
				return d;
			} catch (COMM_FAILURE cf) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void saveDetectors(String filename) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaXspress.saveDetectors(filename);
				return;
			} catch (COMM_FAILURE cf) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void loadAndInitializeDetectors(String filename) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaXspress.loadAndInitializeDetectors(filename);
				return;
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			} catch (TRANSIENT ct) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public Object readout() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				Any result = corbaXspress.readout();
				return result.extract_Value();
			} catch (COMM_FAILURE cf) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void clear() throws DeviceException {
		try {
			corbaXspress.clear();
		} catch (CorbaDeviceException e) {
			throw new DeviceException(e.message);
		} catch (COMM_FAILURE cf) {
			corbaXspress= CorbaXspressHelper.narrow(netService.reconnect(name));
		} catch (TRANSIENT ct) {
			corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
		}
	}

	@Override
	public void start() throws DeviceException {
		try {
			corbaXspress.start();
		} catch (CorbaDeviceException e) {
			throw new DeviceException(e.message);
		} catch (COMM_FAILURE cf) {
			corbaXspress= CorbaXspressHelper.narrow(netService.reconnect(name));
		} catch (TRANSIENT ct) {
			corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
		}
	}

	@Override
	public Object readoutScalerData() throws DeviceException {
		return null;
	}

	@Override
	public ArrayList<String> getChannelLabels() {
		// Not implemented as it is not needed in the client.
		return null;
	}

	@Override
	public int getNumberofGrades() {
		// Not implemented as it is not needed in the client.
		return 16;
	}

	@Override
	public String getResGrade() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXspress.getResGrade();
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			} catch (COMM_FAILURE cf) {
				corbaXspress= CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setResGrade(String grade) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaXspress.setResGrade(grade);
				return;
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			} catch (COMM_FAILURE cf) {
				corbaXspress= CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getReadoutMode() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXspress.getReadoutMode();
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			} catch (COMM_FAILURE cf) {
				corbaXspress= CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setReadoutMode(String grade) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaXspress.setReadoutMode(grade);
				return;
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			} catch (COMM_FAILURE cf) {
				corbaXspress= CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int[] getRawScalerData() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXspress.getRawScalerData();
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			} catch (COMM_FAILURE cf) {
				corbaXspress= CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public Double getDeadtimeCalculationEnergy() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXspress.getDeadtimeCalculationEnergy();
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			} catch (COMM_FAILURE cf) {
				corbaXspress= CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setDeadtimeCalculationEnergy(Double energy) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaXspress.setDeadtimeCalculationEnergy(energy);
				return;
			} catch (CorbaDeviceException e) {
				throw new DeviceException(e.message);
			} catch (COMM_FAILURE cf) {
				corbaXspress= CorbaXspressHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

}