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

package gda.device.detector.xspress;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import gda.device.TangoDeviceProxy;
import gda.factory.FactoryException;
import gda.device.DeviceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a set of Xspress1 boards and detectors. 
 */
public class TangoXspress1DetectorImpl implements XspressDetectorImpl {
	
	private static final Logger logger = LoggerFactory.getLogger(TangoXspress1DetectorImpl.class);
	private TangoDeviceProxy tangoDeviceProxy;
	private int numberOfDetectors;
	private int numberOfScalers = 4;
	private int collectionTime = 0;

	public TangoXspress1DetectorImpl() {
	}

	@Override
	public void configure() throws FactoryException {
		try {
			tangoDeviceProxy.isAvailable();
			numberOfDetectors = tangoDeviceProxy.getAttributeAsInt("numberOfDetectors");
		} catch(DevFailed e) {
			throw new FactoryException(e.getMessage(), e);
		} catch(DeviceException e) {
			throw new FactoryException(e.getMessage(), e);
		}
	}

	public TangoDeviceProxy getTangoDeviceProxy() {
		return tangoDeviceProxy;
	}

	public void setTangoDeviceProxy(TangoDeviceProxy tangoDeviceProxy) {
		this.tangoDeviceProxy = tangoDeviceProxy;
	}

	@Override
	public int getNumberOfDetectors() throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			return tangoDeviceProxy.getAttributeAsInt("numberOfDetectors");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;			
		}
	}

	@Override
	public void setWindows(int detector, int winStart, int winEnd) throws DeviceException {
		try {
			int[] argin = new int[3];
			argin[0] = detector;
			argin[1] = winStart;
			argin[2] = winEnd;
			DeviceData args = new DeviceData();
			args.insert(argin);
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.command_inout("SetWindows", args);
			logger.debug("Setwindows() detector "+ detector + " window start " + winStart + " window end "  + winEnd);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public void setCollectionTime(int time) throws DeviceException {
		try {
			tangoDeviceProxy.setAttribute("collectionTime",time);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public void clear() throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.set_timeout_millis(60000);
			tangoDeviceProxy.command_inout("Clear");
			tangoDeviceProxy.set_timeout_millis(3000);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public void start() {
		// no implementation required
	}

	@Override
	public void stop() {
		// no implementation required
	}

	@Override
	public void close() {
		// no implementation required
	}

	@Override
	public void reconfigure() throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.command_inout("reset");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public int[] readoutHardwareScalers(int startFrame, int numberOfFrames) throws DeviceException {
		int[] value = null;
		try {
			int[] argin = new int[6];
			argin[0] = 0;
			argin[1] = 0;
			argin[2] = startFrame;
			argin[3] = numberOfScalers;
			argin[4] = numberOfDetectors;
			argin[5] = numberOfFrames;
			DeviceData args = new DeviceData();
			args.insert(argin);
			tangoDeviceProxy.isAvailable();
			DeviceData argout = tangoDeviceProxy.command_inout("ReadScalers", args);
			value = argout.extractLongArray();
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
		return value;
	}

	@Override
	public int[] readoutMca(int detector, int startFrame, int numberOfFrames, int mcaSize) throws DeviceException {
		int[] value = null;
		try {
			int[] argin = new int[6];
			argin[0] = 0;
			argin[1] = detector;
			argin[2] = startFrame;
			argin[3] = mcaSize;
			argin[4] = 1;
			argin[5] = numberOfFrames;
			DeviceData args = new DeviceData();
			args.insert(argin);
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.set_timeout_millis(collectionTime+collectionTime/10);
			DeviceData argout = tangoDeviceProxy.command_inout("ReadMca", args);
			tangoDeviceProxy.set_timeout_millis(3000);
			value = argout.extractLongArray();
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
		return value;
	}
}