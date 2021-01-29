/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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
import gda.factory.FactoryException;

/**
 * Check that the Malcolm process is running
 */
public class MalcolmProcessCondition extends RateLimitedServerCondition {
	private static final Logger logger = LoggerFactory.getLogger(MalcolmProcessCondition.class);

	private Scannable malcolmMonitor;

	@Override
	public void configure() throws FactoryException {
		super.configure();
		if (malcolmMonitor == null) {
			throw new FactoryException("Malcolm status monitor not set");
		}
	}

	@Override
	protected boolean isServiceRunning() {
		if (malcolmMonitor == null) {
			setErrorMessage("Malcolm status monitor is not configured");
			return false;
		}
		try {
			return malcolmMonitor.getPosition().equals("NO_ALARM");
		} catch (DeviceException e) {
			logger.error("Cannot read Malcolm status monitor", e);
			return false;
		}
	}

	public void setMalcolmMonitor(Scannable malcolmMonitor) {
		this.malcolmMonitor = malcolmMonitor;
	}
}
