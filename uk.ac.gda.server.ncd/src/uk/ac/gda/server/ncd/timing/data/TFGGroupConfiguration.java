/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.timing.data;

public class TFGGroupConfiguration {
	private int frameCount;
	private double deadTime;
	private double liveTime;
	private int deadPort;
	private int livePort;
	private int deadPause;
	private int livePause;

	public int getFrameCount() {
		return frameCount;
	}

	public void setFrameCount(int frameCount) {
		this.frameCount = frameCount;
	}

	public double getDeadTime() {
		return deadTime;
	}

	public void setDeadTime(double deadTime) {
		this.deadTime = deadTime;
	}

	public double getLiveTime() {
		return liveTime;
	}

	public void setLiveTime(double liveTime) {
		this.liveTime = liveTime;
	}

	public int getDeadPort() {
		return deadPort;
	}

	public void setDeadPort(int deadPort) {
		this.deadPort = deadPort;
	}

	public int getLivePort() {
		return livePort;
	}

	public void setLivePort(int livePort) {
		this.livePort = livePort;
	}

	public int getDeadPause() {
		return deadPause;
	}

	public void setDeadPause(int deadPause) {
		this.deadPause = deadPause;
	}

	public int getLivePause() {
		return livePause;
	}

	public void setLivePause(int livePause) {
		this.livePause = livePause;
	}
}
