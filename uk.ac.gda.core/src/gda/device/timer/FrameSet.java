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

package gda.device.timer;

import java.io.Serializable;

/**
 * A class to represent all information pertaining to one time frame set of the time frame generator.
 * Times in the class are assumed to be milliseconds.
 */
public class FrameSet implements Serializable{
	public int frameCount;
	public double requestedDeadTime;
	public double requestedLiveTime;
	public int deadPort;
	public int livePort;
	public int deadPause;
	public int livePause;
	public FrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime, int deadPort, int livePort,
			int deadPause, int livePause) {
		this.frameCount = frameCount;
		this.requestedDeadTime = requestedDeadTime;
		this.requestedLiveTime = requestedLiveTime;
		this.deadPort = deadPort;
		this.livePort = livePort;
		this.deadPause = deadPause;
		this.livePause = livePause;
	}
	public FrameSet(int frameCount, double requestedDeadTime, double requestedLiveTime) {
		this(frameCount, requestedDeadTime, requestedLiveTime, 0, 0, 0, 0);
	}

	public int getFrameCount() {
		return frameCount;
	}

	/**
	 * @return the requested live time (this may be different from what is achievable by the h/w
	 */
	public double getRequestedLiveTime() {
		return requestedLiveTime;
	}

	/**
	 * @return the pause bits for the dead frame
	 */
	public int getDeadPause() {
		return deadPause;
	}

	/**
	 * @return the level settings for the lemo output ports during the dead frame
	 */
	public int getDeadPort() {
		return deadPort;
	}

	/**
	 * @return the pause bits for the live frame
	 */
	public int getLivePause() {
		return livePause;
	}

	/**
	 * @return the level settings for the lemo output ports during the live frame
	 */
	public int getLivePort() {
		return livePort;
	}

	/**
	 * @return the requested live time (this may be different from what is achievable by the h/w
	 */
	public double getRequestedDeadTime() {
		return requestedDeadTime;
	}

	/**
	 * @param requestedLiveTime
	 *            set the live time (this may be different from what is achievable by the h/w
	 */
	public void setRequestedLiveTime(double requestedLiveTime) {
		this.requestedLiveTime = requestedLiveTime;
	}

	/**
	 * @param requestedDeadTime
	 *            set the dead time (this may be different from what is achievable by the h/w
	 */
	public void setRequestedDeadTime(double requestedDeadTime) {
		this.requestedDeadTime = requestedDeadTime;
	}

	@Override
	public String toString() {
		//TODO use format that gives more precision G and output live and dead ports
		return String.format("frames: %d   dead time: %6.3f s   live time: %6.3f s",frameCount, requestedDeadTime / 1000, requestedLiveTime / 1000);
	}
}