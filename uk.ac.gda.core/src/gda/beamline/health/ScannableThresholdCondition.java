/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.beamline.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.diamond.daq.beamcondition.BeamCondition;
import uk.ac.diamond.daq.beamcondition.ScannableThresholdCheck;

/**
 * Wraps a {@link ScannableThresholdCheck} to determine the health status of the scannable
 */
public class ScannableThresholdCondition extends ScannableHealthCondition {
	private static final Logger logger = LoggerFactory.getLogger(ScannableThresholdCondition.class);

	/**
	 * The condition we want to check.<br>
	 */
	private ScannableThresholdCheck condition;

	@Override
	public String readCurrentState() {
		final Scannable scannable = condition.getScannable();
		try {
			return scannable.getPosition().toString();
		} catch (DeviceException e) {
			logger.error("Error getting position of {}", scannable.getName(), e);
			return "(error)";
		}
	}

	public void setCondition(ScannableThresholdCheck condition) {
		this.condition = condition;
	}

	@Override
	protected BeamCondition getCondition() {
		return condition;
	}
}
