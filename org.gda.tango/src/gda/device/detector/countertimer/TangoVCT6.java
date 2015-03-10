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

package gda.device.detector.countertimer;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.TangoDeviceProxy;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

@CorbaAdapterClass(DetectorAdapter.class)
@CorbaImplClass(DetectorImpl.class)
public class TangoVCT6 extends gda.device.detector.DetectorBase implements Detector {
//	private static final Logger logger = LoggerFactory.getLogger(TangoVCT6.class);

	private TangoDeviceProxy deviceProxy;
	private int totalChans = 6;
	private int masterChannel = 5;
	private boolean timeChannelRequired;
	private short[] masterInitValues = {0, 5, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	private short[] slaveInitValues =  {1, 5, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	@Override
	public void configure() throws FactoryException {
		try {
			deviceProxy.isAvailable();
		} catch (Exception e) {
			// Do nothing for now
		}
	}
	
	public void reset() throws DevFailed {		
		// Initialise the master channel
		DeviceData argin = new DeviceData();
		argin.insert(masterInitValues);
		deviceProxy.command_inout("DevCntInit", argin);

		// now initialise the slave channels
		String masterName = deviceProxy.get_name();
		DeviceData argin2 = new DeviceData();
		argin2.insert(slaveInitValues);
		for (int i=1; i<=totalChans; i++) {
			if (i != masterChannel) {
				String slaveName = masterName.substring(0, masterName.length()-1)+ i;
				TangoDeviceProxy slaveDeviceProxy = new TangoDeviceProxy(slaveName);
				slaveDeviceProxy.command_inout("DevCntInit", argin2);
				slaveDeviceProxy.command_inout("DevCntStart");
			}
		}
	}

	/**
	 * @return Returns the Tango device proxy.
	 */
	public TangoDeviceProxy getTangoDeviceProxy() {
		return deviceProxy;
	}

	/**
	 * @param deviceProxy The Tango device proxy to set.
	 */
	public void setTangoDeviceProxy(TangoDeviceProxy deviceProxy) {
		this.deviceProxy = deviceProxy;
	}

	/**
	 * @return Returns the timeChannelRequired.
	 */
	public boolean isTimeChannelRequired() {
		return timeChannelRequired;
	}

	/**
	 * @param timeChannelRequired
	 *            The timeChannelRequired to set.
	 */
	public void setTimeChannelRequired(boolean timeChannelRequired) {
		this.timeChannelRequired = timeChannelRequired;
	}

	public void countAsync(double time) throws DeviceException {
		writeCollectionTime(time);
		start();
	}

	private void writeCollectionTime(double time) throws DeviceException{
		deviceProxy.isAvailable();
		try {
			deviceProxy.write_attribute(new DeviceAttribute("DevCntPresetTime",time));
		} catch (DevFailed e) {
			throw new DeviceException("failed to write collection time", e);
		}
	}
	
	@Override
	public void setCollectionTime(double collectionTimeInSeconds) {
		this.collectionTime = collectionTimeInSeconds;
	}
	
	private void start() throws DeviceException {
		deviceProxy.isAvailable();
		try {
			deviceProxy.command_inout("DevCntStart");
		} catch (DevFailed e) {
			throw new DeviceException("failed to start", e);
		}
	}

	@Override
	public void stop() throws DeviceException {
		deviceProxy.isAvailable();
		try {
			deviceProxy.command_inout("DevCntStopAll");
		} catch (DevFailed e) {
			throw new DeviceException("failed to abort", e);
		}
	}

	@Override
	public void collectData() throws DeviceException {
		countAsync(collectionTime);
	}

	@Override
	public Object readout() throws DeviceException {
		double[] output = readChans();

		if (timeChannelRequired) {
			double[] values = new double[output.length+1];
			values[0] = getCollectionTime();
			for (int i=0; i< output.length; i++)
				values[i+1] = output[i];
			return values;
		}
		return output;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "CounterTimer";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "Abstract";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "TangoCounterTimer";
	}

	@Override
	public int getStatus() throws DeviceException {
		int status = Detector.IDLE;
		deviceProxy.isAvailable();
		try {
			int istat = deviceProxy.read_attribute("DevCntStatus").extractShort();
			if ((istat & (1<<11)) == 2048)
				status = Detector.BUSY;
			else if (istat == 0 || (istat & (1<<3)) == 8)
				status = Detector.IDLE;
			else
				status = Detector.FAULT;
		} catch (DevFailed e) {
			throw new DeviceException("failed to get countertimer status", e);
		}
		return status;
	}

//	@Override
//	public int getTotalChans() throws DeviceException {
//		return (timeChannelRequired) ? totalChans + 1 : totalChans;
//	}

//	@Override
	public double[] readChans() throws DeviceException {
		deviceProxy.isAvailable();
		try {
			double[] values = new double[totalChans];
			long[] ldata;
			ldata = deviceProxy.read_attribute("DevCntReadAll").extractULongArray();
			for (int i=0; i<totalChans; i++) {
				values[i] = ldata[(i*2)+1];
			}
			return values;
		} catch (DevFailed e) {
			throw new DeviceException("failed to read channel value", e);
		}
	}
}
