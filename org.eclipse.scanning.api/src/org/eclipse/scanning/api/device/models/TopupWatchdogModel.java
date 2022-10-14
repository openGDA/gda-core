/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.device.models;

import java.time.Duration;
import java.util.Objects;

public class TopupWatchdogModel extends AbstractDeviceWatchdogModel {

	private static final long DEFAULT_PERIOD = Duration.ofMinutes(10).toMillis();
	private static final long DEFAULT_TOPUP_TIME = Duration.ofSeconds(15).toMillis();

	/**
	 * e.g. "topup", "countdown" PV likely to be SR-CS-FILL-01:COUNTDOWN which is in s
	 */
	private String countdownName;

	/**
	 * time in ms before topup for which the scan should be paused
	 */
	private long cooloff;

	/**
	 * time in ms after topup the scan should wait before starting
	 */
	private long warmup;

	/**
	 * period in ms, default is 10 min
	 */
	private long period = DEFAULT_PERIOD; // Period in ms, default is 10min

	/**
	 * the time that a topup takes. This is varible but in normal mode <= 15s
	 */
	private long topupTime = DEFAULT_TOPUP_TIME; // The time that a topup takes. This is varible but in normal mode <= 15s

	/**
	 * the name of the mode pv, if any<br>
	 * If this is set the PV will be checked to ensure that the topup mode is as expected.
	 */
	private String modeName;

	/**
	 * the name of the state pv, if any<br>
	 * If this is set the PV will be checked to check whether beam is on decay mode
	 * and a scan will not be paused while there is a top up event
	 */
	private String stateName;

	public String getCountdownName() {
		return countdownName;
	}

	public void setCountdownName(String monitorName) {
		this.countdownName = monitorName;
	}

	public long getCooloff() {
		return cooloff;
	}

	public void setCooloff(long cooloff) {
		this.cooloff = cooloff;
	}

	public long getWarmup() {
		return warmup;
	}

	public void setWarmup(long warmup) {
		this.warmup = warmup;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public long getTopupTime() {
		return topupTime;
	}

	public void setTopupTime(long topupTime) {
		this.topupTime = topupTime;
	}

	public String getModeName() {
		return modeName;
	}

	public void setModeName(String modeName) {
		this.modeName = modeName;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(cooloff, countdownName, modeName, period, stateName, topupTime, warmup);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TopupWatchdogModel other = (TopupWatchdogModel) obj;
		return cooloff == other.cooloff && Objects.equals(countdownName, other.countdownName)
				&& Objects.equals(modeName, other.modeName) && period == other.period
				&& Objects.equals(stateName, other.stateName) && topupTime == other.topupTime && warmup == other.warmup;
	}

	@Override
	public String toString() {
		return "TopupWatchdogModel [countdownName=" + countdownName + ", cooloff=" + cooloff + ", warmup=" + warmup
				+ ", period=" + period + ", topupTime=" + topupTime + ", modeName=" + modeName + ", stateName="
				+ stateName + "]";
	}
}
