/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device;

/**
 * Interface for Detector classes which read their data from scalers (channels) driven by an external Timer object
 * (mainly the TFG card). The Timer interface has collections of timing periods known as FrameSets.
 * <p>
 * The methods in this interface are to allow those Detector objects to have the same FrameSets functionality provided
 * in the Timer interface.
 * 
 * @see gda.device.Timer
 */
public interface CounterTimer extends Detector {

	/**
	 * For a time framing counter-timer, this returns the total number of frame pairs supported (each specifying a live
	 * and a dead period).
	 * 
	 * @return maximum number of time frames available
	 * @throws DeviceException
	 */
	public int getMaximumFrames() throws DeviceException;

	/**
	 * For a time framing counter-timer, this returns the current frame number pointer. During counting this can give an
	 * idea of progress.
	 * 
	 * @return the current frame counter number (1st=0)
	 * @throws DeviceException
	 */
	public int getCurrentFrame() throws DeviceException;

	/**
	 * For a time framing counter-timer, this returns the current cycle number. During counting this can give an idea of
	 * progress. If not implemented this should return a default of 1.
	 * 
	 * @return the current cycle number
	 * @throws DeviceException
	 */
	public int getCurrentCycle() throws DeviceException;

	/**
	 * For a time framing counter-timer this sets the number of times the counter-timer cycles through the framesets.
	 * Default is 1 if h/w does not allow the implementation.
	 * 
	 * @param cycles
	 *            sets the cycle count to the specified number
	 * @throws DeviceException
	 */
	public void setCycles(int cycles) throws DeviceException;

	/**
	 * For a time framing counter-timer this initiates framing.
	 * 
	 * @throws DeviceException
	 */
	public void start() throws DeviceException;
	
	/**
	 * For a time framing counter-timer this restarts framing, from the paused state.
	 * 
	 * @throws DeviceException
	 */
	public void restart() throws DeviceException;

	/**
	 * For a time framing counter-timer a single frameSet object is created for a specified live and dead time. A count
	 * for identical frames is specified by the frameCount.
	 * 
	 * @param requestedLiveTime
	 *            frame live time in milliseconds
	 * @param requestedDeadTime
	 *            frame dead time in milliseconds
	 * @param frameCount
	 *            number of frames required of this type
	 * @throws DeviceException
	 */
	public void addFrameSet(int frameCount, double requestedLiveTime, double requestedDeadTime) throws DeviceException;

	/**
	 * For a time framing counter-timer a single frameSet object is created for a specified live and dead time. A count
	 * for identical frames is specified by the frameCount.
	 * 
	 * @param frameCount
	 *            requested number of frames required of this type
	 * @param requestedLiveTime
	 *            requested frame live time in milliseconds
	 * @param requestedDeadTime
	 *            requested frame dead time in milliseconds
	 * @param deadPort
	 *            wait period output level 0 or 1
	 * @param livePort
	 *            run period output level 0 or 1
	 * @param deadPause
	 *            pause before wait period 0 or 1
	 * @param livePause
	 *            pause before run period 0 or 1
	 * @throws DeviceException
	 */
	public void addFrameSet(int frameCount, double requestedLiveTime, double requestedDeadTime, int deadPort,
			int livePort, int deadPause, int livePause) throws DeviceException;

	/**
	 * For a time framing counter-timer all current frameSets are cleared.
	 * 
	 * @throws DeviceException
	 */
	public void clearFrameSets() throws DeviceException;

	/**
	 * For a time framing counter-timer an array of frameSets obtained from calls to makeFrameSet() is loaded into the
	 * counter-timer. The current mode specified in setMode() is used to create the real frames before actual loading to
	 * the timer.
	 * 
	 * @throws DeviceException
	 */
	public void loadFrameSets() throws DeviceException;

	/**
	 * For a time framing counter-timer read out a specified channel, beginning from the specified start frame number
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
	public double[] readChannel(int startFrame, int frameCount, int channel) throws DeviceException;

	/**
	 * For a time framing counter-timer read out a specified frame, beginning from the specified start channel number
	 * using the requested channel count.
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
	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException;

	/**
	 * If there are multiple detectors in the same scan operated by the same Timer then only one CounterTimer object
	 * should drive the Timer class. In that case the other CounterTimers should have their slave flag set to true.
	 * 
	 * @return true if the underlying
	 */
	public boolean isSlave() throws DeviceException;

	/**
	 * @param slave
	 */
	public void setSlave(boolean slave) throws DeviceException;

}
