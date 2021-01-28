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

public class ProcessingServerCondition extends RateLimitedServerCondition {
	private static final Logger logger = LoggerFactory.getLogger(ProcessingServerCondition.class);

	private Scannable processingMonitor;

	@Override
	public void configure() throws FactoryException {
		super.configure();
		if (processingMonitor == null) {
			throw new FactoryException("No monitor configured for processing");
		}
	}

	@Override
	protected boolean isServiceRunning() {
		if (processingMonitor == null) {
			setErrorMessage("Processing monitor is not configured");
			return false;
		}
		try {
			return processingMonitor.getPosition().equals("Running");
		} catch (DeviceException e) {
			logger.error("Cannot read processing monitor", e);
			return false;
		}
	}

	public void setProcessingMonitor(Scannable processingMonitor) {
		this.processingMonitor = processingMonitor;
	}
}
