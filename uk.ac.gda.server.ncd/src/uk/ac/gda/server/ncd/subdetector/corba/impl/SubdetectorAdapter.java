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

package uk.ac.gda.server.ncd.subdetector.corba.impl;

import java.util.ArrayList;
import java.util.List;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.detector.DataDimension;
import gda.device.detector.NXDetectorData;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.omg.CORBA.Any;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

import uk.ac.gda.server.ncd.subdetector.INcdSubDetector;
import uk.ac.gda.server.ncd.subdetector.corba.CorbaNcdsubDetector;
import uk.ac.gda.server.ncd.subdetector.corba.CorbaNcdsubDetectorHelper;

/**
 * A client side implementation of the adapter pattern for the Detector class
 */
public class SubdetectorAdapter extends DeviceAdapter implements INcdSubDetector, Findable, Device {
	private CorbaNcdsubDetector corbaNcdDetector;

	/**
	 * @param obj
	 * @param name
	 * @param netService
	 */
	public SubdetectorAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(obj);
	}

	@Override
	public int getMemorySize() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaNcdDetector.getMemorySize();
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getDetectorType() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaNcdDetector.getDetectorType();
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	/**
	 * @param d
	 * @throws DeviceException
	 */
	@Override
	public void setDataDimensions(int[] d) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaNcdDetector.setDataDimensions(d);
				return;
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public List<DataDimension> getSupportedDimensions() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				Any result = corbaNcdDetector.getSupportedDimensions();
				DataDimension[] dd = (DataDimension[]) result.extract_Value();
				ArrayList<DataDimension> alist = new ArrayList<DataDimension>();
				for (DataDimension d : dd) {
					alist.add(d);
				}
				return alist;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void writeout(int frames, NXDetectorData dataTree) throws DeviceException {
		throw new DeviceException("unsupported via corba");
	}

	@Override
	public void clear() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaNcdDetector.clear();
				return;
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public void start() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaNcdDetector.start();
				return;
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public void stop() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaNcdDetector.stop();
				return;
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaNcdDetector.getDataDimensions();
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getPixelSize() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaNcdDetector.getPixelSize();
			} catch (COMM_FAILURE cf) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

    @Override
    public void atScanEnd() throws DeviceException {
            for (int i = 0; i < NetService.RETRY; i++) {
                    try {
                            corbaNcdDetector.atScanEnd();
                    } catch (COMM_FAILURE cf) {
                            corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
                    } catch (TRANSIENT ct) {
                            corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
                    } catch (CorbaDeviceException ex) {
                            throw new DeviceException(ex.message);
                    }
            }
            throw new DeviceException("Communication failure: retry failed");
    }

	@Override
	public void setTimer(Timer timer) throws DeviceException {
		throw new DeviceException("not implemented over corba (yet) - request if needed");
	}

	@Override
	public void atScanStart() throws DeviceException {
        for (int i = 0; i < NetService.RETRY; i++) {
            try {
                    corbaNcdDetector.atScanStart();
            } catch (COMM_FAILURE cf) {
                    corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
            } catch (TRANSIENT ct) {
                    corbaNcdDetector = CorbaNcdsubDetectorHelper.narrow(netService.reconnect(name));
            } catch (CorbaDeviceException ex) {
                    throw new DeviceException(ex.message);
            }
    }
    throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public DetectorProperties getDetectorProperties() throws DeviceException {
        throw new DeviceException("not implemented over corba (yet) - request if needed");
	}
}