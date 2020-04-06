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
import static java.util.Optional.empty;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

import gda.factory.Findable;

public abstract class BeamConditionBase implements BeamCondition, Findable {
	private static Logger logger = LoggerFactory.getLogger(BeamConditionBase.class);
	private static final String TEMPLATE = "%s - %s";

	/** Name of this beam condition */
	private Optional<String> name = empty();
	/** Name to use if one has not been set explicitly */
	private String fallbackName = "BeamCondition";

	@Override
	public void waitForBeam() throws InterruptedException {
		RateLimiter logLimit = RateLimiter.create(0.1);
		while (!beamOn()) {
			if (logLimit.tryAcquire()) {
				logger.debug("{} - Waiting for correct beamline conditions", getName());
			}
			Thread.sleep(50);
		}
	}

	/**
	 * Set the name of this beam condition.
	 * Setting the name to null or empty will make this condition revert to its default name.
	 */
	@Override
	public void setName(String name) {
		this.name = Optional.ofNullable(name).filter(s -> !s.isEmpty());
	}

	/** Set the name of this condition if it hasn't been named explicitly */
	protected void setFallbackName(String fallback) {
		if (fallback == null || fallback.isEmpty()) {
			throw new IllegalArgumentException("Fallback name must not be null or empty");
		}
		fallbackName = fallback;
	}

	@Override
	public String getName() {
		return name.orElse(fallbackName);
	}

	@Override
	public String toString() {
		return format(TEMPLATE, getName(), beamOn() ? "✔" : "✘");
	}
}
