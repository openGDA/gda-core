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

package gda.device.xspress.corba.impl;

import gda.device.DeviceException;
import gda.device.Xspress;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.xspress.Detector;
import gda.device.xspress.DetectorReading;
import gda.device.xspress.corba.CorbaDetector;
import gda.device.xspress.corba.CorbaDetectorReading;
import gda.device.xspress.corba.CorbaXspress;
import gda.device.xspress.corba.CorbaXspressHelper;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.Any;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Xspress class
 */
public class XspressAdapter extends DeviceAdapter implements Xspress {
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
	public DetectorReading readDetector(int which) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				CorbaDetectorReading cdr = corbaXspress.readDetector(which);
				DetectorReading dr = DetectorReadingConverter.toDetectorReading(cdr);
				return dr;
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
	public DetectorReading[] readDetectors() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				CorbaDetectorReading[] cdrArray = corbaXspress.readDetectors();
				DetectorReading[] drArray = new DetectorReading[cdrArray.length];
				for (int j = 0; j < cdrArray.length; j++)
					drArray[j] = DetectorReadingConverter.toDetectorReading(cdrArray[j]);
				return drArray;
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
	public void quit() throws DeviceException {
		try {
			corbaXspress.quit();
		} catch (CorbaDeviceException e) {
			throw new DeviceException(e.message);
		} catch (COMM_FAILURE cf) {
			corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
		} catch (TRANSIENT ct) {
			corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
		}
	}

	@Override
	public Object getMCData(int which, int start, int end, int time) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				Any result = corbaXspress.getMCData(which, start, end, time);
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
	public Detector getDetector(int which) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				CorbaDetector cd = corbaXspress.getDetector(which);
				Detector d = DetectorConverter.toDetector(cd);
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

	/**
	 * @see gda.device.Xspress#loadAndInitializeDetectors(java.lang.String)
	 */
	@Override
	public String loadAndInitializeDetectors(String filename) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXspress.loadAndInitializeDetectors(filename);

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
	public void setDetectorGain(int detector, double gain) throws DeviceException {
		try {
			corbaXspress.setDetectorGain(detector, gain);
		} catch (CorbaDeviceException e) {
			throw new DeviceException(e.message);
		} catch (COMM_FAILURE cf) {
			corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
		} catch (TRANSIENT ct) {
			corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
		}
	}

	@Override
	public void setDetectorOffset(int detector, double offset) throws DeviceException {
		try {
			corbaXspress.setDetectorOffset(detector, offset);
		} catch (CorbaDeviceException e) {
			throw new DeviceException(e.message);
		} catch (COMM_FAILURE cf) {
			corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
		} catch (TRANSIENT ct) {
			corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
		}

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
	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				Any result = corbaXspress.readFrame(startChannel, channelCount, frame);
				return (double[]) result.extract_Value();
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
	public void setReadoutMode(int newMode) throws DeviceException {
		try {
			corbaXspress.setReadoutMode(newMode);
		} catch (CorbaDeviceException e) {
			throw new DeviceException(e.message);
		} catch (COMM_FAILURE cf) {
			corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
		} catch (TRANSIENT ct) {
			corbaXspress = CorbaXspressHelper.narrow(netService.reconnect(name));
		}
	}
}