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

package gda.device.detector.countertimer;

import gda.device.CounterTimer;
import gda.device.DeviceException;
import gda.device.Memory;
import gda.factory.FactoryException;
import gda.factory.Finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a Tfg and HY8513 Incremental Encoder Counter Industry Pack combination. Since the Tfg will generally also
 * be part of a TfgScaler combination there is a slave mode. In this mode methods which set things on the Tfg do
 * nothing.
 */
public class TfgEncoderCounter extends TFGCounterTimer implements CounterTimer {
	
	private static final Logger logger = LoggerFactory.getLogger(TfgEncoderCounter.class);

	private Memory encoderCounter = null;
	private String encoderCounterName;

	@Override
	public void configure() throws FactoryException {
		logger.debug("Finding: " + encoderCounterName);
		if ((encoderCounter = (Memory) Finder.getInstance().find(encoderCounterName)) == null) {
			logger.error("encoderCounter " + encoderCounterName + " not found");
		}
		super.configure();
	}

	/**
	 * @param encoderCounterName
	 */
	public void setEncoderCounterName(String encoderCounterName) {
		this.encoderCounterName = encoderCounterName;
	}

	/**
	 * @return encoderCounterName
	 */
	public String getEncoderCounterName() {
		return encoderCounterName;
	}

	/**
	 * Returns the total number of available counter-timer readout channels that will be returned by calls to
	 * readChans() For a time-framing device it is the number of channels per frame. All counter-timers must fully
	 * implement this.
	 * 
	 * @return total number of readout channels
	 * @throws DeviceException
	 */
	public int getTotalChans() throws DeviceException {
		return encoderCounter.getDimension()[0];
	}

	public void countAsync(double time) throws DeviceException {
		encoderCounter.clear();
		encoderCounter.start();
		if (!slave)
			timer.countAsync(time);
	}

	@Override
	public void start() throws DeviceException {
		encoderCounter.clear();
		encoderCounter.start();
		if (!slave){
			timer.start();
		}
	}

	@Override
	public void stop() throws DeviceException {
		if (!slave){
			timer.stop();
		}
		encoderCounter.stop();
	}

	/**
	 * Obtain an array of available readout channels. This should be available at any time. If the hardware does not
	 * allow it during active counter-timing periods, it should return zero values. High level counter-timers may return
	 * values in user units. All counter-timers must fully implement this.
	 * 
	 * @return array of all channel readout values
	 * @throws DeviceException
	 */
	public double[] readChans() throws DeviceException {
		return encoderCounter.read(0, 0, 0, encoderCounter.getDimension()[0], 1, 1);
	}

	/**
	 * For a time framing counter-timer, read out a specified channel beginning from the specified start frame number
	 * using the requested frame count.
	 * 
	 * @return array of requested readout counter-timer data
	 * @param startFrame
	 *            starting frame number (1st=0)
	 * @param frameCount
	 *            number of frames to read the counter data out from
	 * @param channel
	 *            read this channel
	 * @throws DeviceException
	 */
	@Override
	public double[] readChannel(int startFrame, int frameCount, int channel) throws DeviceException {
		return encoderCounter.read(channel, 0, startFrame, 1, 1, frameCount);
	}

	/**
	 * For a time framing counter-timer, read out a specified channel beginning from the specified start frame number
	 * using the requested frame count.
	 * 
	 * @return array of requested readout counter-timer data
	 * @param startChannel
	 *            starting channel number (1st=0)
	 * @param channelCount
	 *            number of channels to read the counter data out from
	 * @param frame
	 *            read this frame
	 * @throws DeviceException
	 */
	@Override
	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException {
		// NB To read frame 1 we read 1 increment from location 0 and so on.
		return encoderCounter.read(startChannel, 0, frame - 1, channelCount, 1, 1);
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		if (!slave)
			timer.setAttribute(attributeName, value);
		encoderCounter.setAttribute(attributeName, value);
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		Object obj;
		if ((obj = timer.getAttribute(attributeName)) == null)
			obj = encoderCounter.getAttribute(attributeName);
		return obj;
	}

	/**
	 * @throws DeviceException
	 */
	@Override
	public void collectData() throws DeviceException {
		countAsync(collectionTime);
	}

	/**
	 * @see gda.device.Detector#readout()
	 */
	@Override
	public double[] readout() throws DeviceException {
		return readChans();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Tfg Encoder Counter";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "unknown";
	}

}
