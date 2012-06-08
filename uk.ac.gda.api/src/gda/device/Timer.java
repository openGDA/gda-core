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

package gda.device;

/**
 * Interface for objects which control Timers.
 * <p>
 * Timers can have collections of time intervals to operate in known as frame sets.
 */
public interface Timer extends Device {
	/**
	 * Timer is idle
	 */
	public static final int IDLE = Detector.IDLE;

	/**
	 * Timer is running
	 */
	public static final int ACTIVE = Detector.BUSY;

	/**
	 * Timer is paused
	 */
	public static final int PAUSED = Detector.PAUSED;

	/**
	 * Timer is paused
	 */
	public static final int ARMED = Detector.MONITORING;
	
	/**
	 * Returns the current state of the timer All timers must fully implement this.
	 * 
	 * @return ACTIVE if the timer has not finished the requested operation(s), IDLE if in an completely idle state and
	 *         PAUSED if temporarily suspended.
	 * @throws DeviceException
	 */
	public int getStatus() throws DeviceException;

	/**
	 * Returns the total number of frame pairs supported (each specifying a live and a dead period)
	 * 
	 * @return maximum number of time frames available
	 * @throws DeviceException
	 */
	public int getMaximumFrames() throws DeviceException;

	/**
	 * Returns the current frame number.
	 * 
	 * @return the current frame counter number (1st=0)
	 * @throws DeviceException
	 */
	public int getCurrentFrame() throws DeviceException;

	/**
	 * Returns the current cycle number. If not implemented this should return a default of 1.
	 * 
	 * @return the current cycle number
	 * @throws DeviceException
	 */
	public int getCurrentCycle() throws DeviceException;

	/**
	 * sets the number of times the timer cycles through the framesets. Default is 1 if h/w does not allow the
	 * implementation
	 * 
	 * @param cycles
	 *            sets the cycle count to the specified number
	 * @throws DeviceException
	 */
	public void setCycles(int cycles) throws DeviceException;

	/**
	 * For a time framing counter-timer, this initiates framing, starting from the current frame number (specified by
	 * setFrameNumber())
	 * 
	 * @throws DeviceException
	 */
	public void start() throws DeviceException;

	/**
	 * Aborts any current timing and returns it to an idle state.
	 * 
	 * @throws DeviceException
	 */
	public void stop() throws DeviceException;

	/**
	 * Restarts framing from the paused state
	 * 
	 * @throws DeviceException
	 */
	public void restart() throws DeviceException;

	/**
	 * Create a single frameSet object for a specified live and dead time. A count for identical frames is specified by
	 * the frameCount.
	 * 
	 * @param requestedDeadTime
	 *            the requested frame dead time in milliseconds
	 * @param requestedLiveTime
	 *            the requested frame live time in milliseconds
	 * @param frameCount
	 *            the requested number of frames required of this type
	 * @throws DeviceException
	 */
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime) throws DeviceException;

	/**
	 * Create a single frameSet object for a specified live and dead time. A count for identical frames is specified by
	 * the frameCount.
	 * 
	 * @param frameCount
	 *            the requested number of frames required of this type
	 * @param requestedDeadTime
	 *            the requested frame dead time in milliseconds
	 * @param requestedLiveTime
	 *            the requested frame live time in milliseconds
	 * @param deadPort
	 *            the wait period output level 0 or 1
	 * @param livePort
	 *            the run period output level 0 or 1
	 * @param deadPause
	 *            the pause before wait period 0 or 1
	 * @param livePause
	 *            the pause before run period 0 or 1
	 * @throws DeviceException
	 */
	public void addFrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime, int deadPort,
			int livePort, int deadPause, int livePause) throws DeviceException;

	/**
	 * Clear all current frameSets
	 * 
	 * @throws DeviceException
	 */
	public void clearFrameSets() throws DeviceException;

	/**
	 * Load an array of frameSets obtained from calls to makeFrameSet() are loaded into the timer.
	 * 
	 * @throws DeviceException
	 */
	public void loadFrameSets() throws DeviceException;

	/**
	 * Initiates a single specified timing period and allows the timer to proceed asynchronously. The end of period can
	 * be determined by calls to getStatus() returning IDLE.
	 * 
	 * @param time
	 *            the requested counting time in milliseconds
	 * @throws DeviceException
	 */
	public void countAsync(double time) throws DeviceException;

	/**
	 * Output data from timer directly to file
	 * 
	 * @param file
	 *            is the fully qualified file name
	 * @throws DeviceException
	 */
	public void output(String file) throws DeviceException;
}
