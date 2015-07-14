/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector.xmap.corba.impl;

import java.util.Collections;
import java.util.List;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.Object;
import org.omg.CORBA.TRANSIENT;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.XmapDetector;
import gda.device.corba.CorbaDeviceException;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.xmap.corba.CorbaXmapDetector;
import gda.device.detector.xmap.corba.CorbaXmapDetectorHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

public class XmapAdapter extends DetectorAdapter implements Detector, XmapDetector, Findable, Scannable {

	private static final long serialVersionUID = -3788520814633324994L;
	private CorbaXmapDetector corbaXmapDetector;
	/**
	 * @param obj
	 * @param name
	 * @param netService
	 */
	public XmapAdapter(Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaXmapDetector = CorbaXmapDetectorHelper.narrow(obj);
	}

	@Override
	public void clear() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaXmapDetector.clear();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}

	}

	@Override
	public void clearAndStart() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaXmapDetector.clearAndStart();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getAcquisitionTime() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXmapDetector.getAcquisitionTime();

			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public int[] getData(int mcaNumber) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXmapDetector.getChannelData(mcaNumber);

			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int[][] getData() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXmapDetector.getData();

			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getNumberOfBins() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXmapDetector.getNumberOfBins();

			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getReadRate() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXmapDetector.getReadRate();

			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getRealTime() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXmapDetector.getRealTime();

			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getStatusRate() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaXmapDetector.getStatusRate();

			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setAcquisitionTime(double time) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				 corbaXmapDetector.setAcquisitionTime(time);
				 return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setNumberOfBins(int numberOfBins) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				 corbaXmapDetector.setNumberOfBins(numberOfBins);
				 return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setReadRate(double readRate) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				 corbaXmapDetector.setReadRate(readRate);
				 return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public void setStatusRate(double statusRate) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				 corbaXmapDetector.setStatusRate(statusRate);
				 return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void start() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				 corbaXmapDetector.start();
				 return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void stop() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				 corbaXmapDetector.stop();
				 return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");

	}

	@Override
	public int getNumberOfROIs() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				 return corbaXmapDetector.getNumberOfROIs();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double[] getROIsSum() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				 return corbaXmapDetector.getROIsSum();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				 corbaXmapDetector.setROIs(rois);
				 return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getNumberOfMca() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				 return corbaXmapDetector.getNumberOfMca();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setNthROI(double[][] rois, int roiIndex) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				 corbaXmapDetector.setNthROI(rois,roiIndex);
				 return ;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaXmapDetector = CorbaXmapDetectorHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	/**
	 * Does nothing, remove implementations do not need the xml channel labels.
	 */
	@Override
	public List<String> getChannelLabels() {
		return Collections.emptyList();
	}

	@Override
	public double[] getROICounts(int iRoi) throws DeviceException {
		throw new DeviceException("Not implemented yet!");
	}

	@Override
	public double readoutScalerData() throws DeviceException {
		throw new DeviceException("readoutScalerData not implemented for remote!");
	}

}
