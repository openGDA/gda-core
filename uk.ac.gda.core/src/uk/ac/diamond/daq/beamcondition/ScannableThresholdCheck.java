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

package uk.ac.diamond.daq.beamcondition;

import static java.lang.Double.isNaN;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.observable.IObserver;

public class ScannableThresholdCheck extends BeamConditionBase implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(ScannableThresholdCheck.class);

	private Scannable scannable;
	private double lowerLimit = Double.NaN;
	private double upperLimit = Double.NaN;

	private volatile boolean beamStatus = false;

	@Override
	public boolean beamOn() {
		return beamStatus;
	}

	@Override
	public void update(Object source, Object arg) {
		logger.trace("{} - update from {}: {}", getName(), source, arg);
		updateBeamStatus();
	}

	public void setScannable(Scannable scannable) {
		if (this.scannable != null) {
			this.scannable.deleteIObserver(this);
		}
		this.scannable = scannable;
		if (scannable != null) {
			this.scannable.addIObserver(this);
			updateBeamStatus();
		} else {
			beamStatus = true;
		}
		updateName();
	}

	public void setLowerLimit(double limit) {
		lowerLimit = limit;
		updateBeamStatus();
		updateName();
	}

	public void setUpperLimit(double limit) {
		upperLimit = limit;
		updateBeamStatus();
		updateName();
	}

	private void updateBeamStatus() {
		try {
			Double position = (Double) scannable.getPosition();
			beamStatus = (Double.isNaN(upperLimit) || position < upperLimit)
						&& (Double.isNaN(lowerLimit) || position >= lowerLimit);
		} catch (ClassCastException | DeviceException e) {
			logger.error("Could not get position of scannable {}", e);
		}
	}

	private void updateName() {
		StringBuilder sb = new StringBuilder();
		sb.append(scannable == null ? "???" : scannable.getName());
		if (!isNaN(lowerLimit)) {
			sb.insert(0, " < ");
			sb.insert(0, lowerLimit);
		}
		if (!isNaN(upperLimit)) {
			sb.append(" < ");
			sb.append(upperLimit);
		}
		if (isNaN(lowerLimit) && isNaN(upperLimit)) { // no limits
			sb.append(" (no limits)");
		}
		sb.insert(0, "ScannableThresholdCheck(");
		sb.append(")");
		setName(sb.toString());
	}
}
