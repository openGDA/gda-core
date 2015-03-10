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
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.TangoDeviceProxy;
import gda.device.detector.countertimer.corba.impl.CountertimerAdapter;
import gda.device.detector.countertimer.corba.impl.CountertimerImpl;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

@CorbaAdapterClass(CountertimerAdapter.class)
@CorbaImplClass(CountertimerImpl.class)
public class TangoCounterTimer extends gda.device.detector.DetectorBase implements Detector {
//	private static final Logger logger = LoggerFactory.getLogger(TangoCounterTimer.class);

	// Attribute names defined by the Tango CounterTimer API
	private static final String collectionTimeAttributeName = "Count_time"; //$NON-NLS-1$
	private static final String countValueAttributeName = "Count_value"; //$NON-NLS-1$
	private static final String noiseAttributeName = "Noise"; //$NON-NLS-1$

	private TangoDeviceProxy deviceProxy;
//	private int totalChans = 1;
	private boolean timeChannelRequired;

	@Override
	public void configure() throws FactoryException {
		try {
			deviceProxy.isAvailable();
		} catch (Exception e) {
			// Do nothing for now
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
			deviceProxy.write_attribute(new DeviceAttribute(collectionTimeAttributeName,time));
		} catch (DevFailed e) {
			throw new DeviceException("failed to write collection time", e);
		}
	}
	
	@Override
	public void setCollectionTime(double collectionTimeInSeconds) {
		this.collectionTime = collectionTimeInSeconds;
	}
	
//	@Override
	public void start() throws DeviceException {
		deviceProxy.isAvailable();
		try {
			deviceProxy.command_inout("Start");
		} catch (DevFailed e) {
			throw new DeviceException("failed to start", e);
		}
	}

	@Override
	public void stop() throws DeviceException {
		deviceProxy.isAvailable();
		try {
			deviceProxy.command_inout("Abort");
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
//Why here?			readChans();
			DevState state = deviceProxy.state();
			switch (state.value())
			{
			case DevState._ON:
				status = Detector.BUSY;
				break;
			case DevState._OFF:
				status = Detector.IDLE;
				break;
			default:
				status = Detector.FAULT;
				break;
			}
		} catch (DevFailed e) {
			throw new DeviceException("failed to get counterTimer state", e);
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
			double[] values = new double[1];
			values[0] = deviceProxy.read_attribute(countValueAttributeName).extractDouble();
			return values;
		} catch (DevFailed e) {
			throw new DeviceException("failed to read channel value", e);
		}
	}

	public double[] readNoise() throws DeviceException {
		deviceProxy.isAvailable();
		try {
			double[] values = new double[1];
			values[0] = deviceProxy.read_attribute(noiseAttributeName).extractDouble();
			return values;
		} catch (DevFailed e) {
			throw new DeviceException("failed to read noise value", e);
		}
	}
	
//	@Override
//	public int getCurrentCycle() throws DeviceException {
//		logger.debug("Not implemented");
//		return 0;
//	}
//
//	@Override
//	public int getCurrentFrame() throws DeviceException {
//		logger.debug("Not implemented");
//		return 0;
//	}
//
//	@Override
//	public int getMaximumFrames() throws DeviceException {
//		logger.debug("Not implemented");
//		return 0;
//	}
//
//	@Override
//	public void addFrameSet(int frameCount, double requestedLiveTime, double requestedDeadTime) throws DeviceException {
//		logger.debug("Not implemented");
//	}
//
//	@Override
//	public void addFrameSet(int frameCount, double requestedLiveTime, double requestedDeadTime, int deadPort,
//			int livePort, int deadPause, int livePause) throws DeviceException {
//		logger.debug("Not implemented");
//	}
//
//	@Override
//	public void clearFrameSets() throws DeviceException {
//		logger.debug("Not implemented");
//	}
//
//
//	@Override
//	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException {
//		logger.debug("Not implemented");
//		return null;
//	}
//
//	@Override
//	public void restart() throws DeviceException {
//		logger.debug("Not implemented");
//	}
//
//	@Override
//	public void setCycles(int cycles) throws DeviceException {
//		logger.debug("Not implemented");
//	}
//
//	@Override
//	public void setFrameNumber(int frameNumber) throws DeviceException {
//		logger.debug("Not implemented");
//	}
//
//	@Override
//	public void loadFrameSets() throws DeviceException {
//		logger.debug("Not implemented");
//	}
//
//	@Override
//	public void loadPresetChans(double[] value) throws DeviceException {
//		logger.debug("Not implemented");
//	}
//
//	@Override
//	public double[] readChannel(int startFrame, int frameCount, int channel) throws DeviceException {
//		logger.debug("Not implemented");
//		return null;
//	}
}
