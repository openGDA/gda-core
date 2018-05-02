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

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

import gda.factory.Findable;

public abstract class BeamConditionBase implements BeamCondition, Findable {
	private static Logger logger = LoggerFactory.getLogger(BeamConditionBase.class);
	private static final String TEMPLATE = "%s - %s";

	private String name;

	@Override
	public void waitForBeam() {
		RateLimiter logLimit = RateLimiter.create(0.1);
		while (!beamOn()) {
			if (logLimit.tryAcquire()) {
				logger.debug("{} - Waiting for correct beamline conditions", name);
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				logger.error("Thread interrupted while '{}' waiting for correct beam conditions", name, e);
				Thread.currentThread().interrupt();
				break;
			}
		}
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return format(TEMPLATE, name, beamOn() ? "✔" : "✘");
	}
}
