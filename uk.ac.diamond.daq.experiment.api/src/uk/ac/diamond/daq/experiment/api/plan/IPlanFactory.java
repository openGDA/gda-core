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

package uk.ac.diamond.daq.experiment.api.plan;

import java.util.function.DoubleSupplier;

import gda.device.Scannable;

public interface IPlanFactory {


	/* ~~~~~~~~~~~~  Sample Environment Variables  ~~~~~~~~~~~~ */


	/**
	 * Creates an {@link ISampleEnvironmentVariable} which samples the specified {@link Scannable}.
	 *
	 * @param scannable
	 * @return reference to created sev
	 */
	ISampleEnvironmentVariable addSEV(Scannable scannable);


	/**
	 * Creates an {@link ISampleEnvironmentVariable} which samples the specified {@link DoubleSupplier}.
	 *
	 * @param DoubleSupplier will be wrapped inside a {@link Scannable}
	 * @return reference to the created sev
	 */
	ISampleEnvironmentVariable addSEV(DoubleSupplier signalSource);

	/**
	 * Gets a timer to use as an {@link ISampleEnvironmentVariable}.
	 */
	ISampleEnvironmentVariable addTimer();


	/* ~~~~~~~~~~~~~~~~~~~~~~  Segments  ~~~~~~~~~~~~~~~~~~~~~~ */


	/**
	 * Creates the next segment in the experiment (segments are executed in order of creation).
	 *
	 * @param name				The name of this segment.
	 * @param sev 				The {@link ISampleEnvironmentVariable} acting as source for the limiting signal
	 * @param limit 			The {@link LimitCondition} which will terminate this segment once met.
	 * @param triggers			The triggers which should be enabled during this segment
	 *
	 * @return 					Reference to the created segment
	 */
	ISegment addSegment(String name, ISampleEnvironmentVariable sev, LimitCondition limit, ITrigger... triggers);


	/**
	 * Creates the next time-based segment with the given duration
	 *
	 * @param name				The name of this segment.
	 * @param sev 				The {@link ISampleEnvironmentVariable} acting as source for the limiting signal
	 * @param duration			The duration in seconds of this segment
	 * @param triggers			The triggers which should be enabled during this segment
	 *
	 * @return Reference to the created segment
	 */
	ISegment addSegment(String name, ISampleEnvironmentVariable sev, double duration, ITrigger... triggers);


	/* ~~~~~~~~~~~~~~~~~~~~~~  Triggers  ~~~~~~~~~~~~~~~~~~~~~~ */


	/**
	 * Create a trigger that will perform an operation only once,
	 * when signal from {@code sev} = {@code target} ± {@code tolerance}
	 *
	 * @param name 				Name of this trigger
	 * @param payload			Request to handle upon triggering
	 * @param sev 				The {@link ISampleEnvironmentVariable} used as triggering signal source
	 * @param target			The optimal triggering signal
	 * @param tolerance			The acceptable tolerance around the target signal
	 *
	 * @return 					Reference to the created trigger
	 */
	ITrigger addTrigger(String name, Payload payload, ISampleEnvironmentVariable sev, double target, double tolerance);


	/**
	 * Create a trigger that will perform an operation only once,
	 * when signal from {@code sev} = {@code target} ± {@code tolerance}
	 *
	 * @param name 				Name of this trigger
	 * @param payload			Request to handle upon triggering
	 * @param sev 				The {@link ISampleEnvironmentVariable} used as triggering signal source
	 * @param target			The optimal triggering signal
	 * @param tolerance			The acceptable tolerance around the target signal
	 *
	 * @return 					Reference to the created trigger
	 */
	ITrigger addTrigger(String name, Object payload, ISampleEnvironmentVariable sev, double target, double tolerance);


	/**
	 * Creates a trigger which will execute the given operation in specified intervals of signal from sample environment variable provided
	 *
	 * @param name of this trigger
	 * @param payload to handle upon triggering
	 * @param sev the {@link ISampleEnvironmentVariable} this trigger will listen to
	 * @param interval or period between triggers
	 *
	 * @return reference to the created trigger
	 */
	ITrigger addTrigger(String name, Payload payload, ISampleEnvironmentVariable sev, double interval);


	/**
	 * Creates a trigger which will execute the given operation in specified intervals of signal from sample environment variable provided
	 *
	 * @param name of this trigger
	 * @param payload to handle upon triggering
	 * @param sev the {@link ISampleEnvironmentVariable} this trigger will listen to
	 * @param interval or period between triggers
	 *
	 * @return reference to the created trigger
	 */
	ITrigger addTrigger(String name, Object payload, ISampleEnvironmentVariable sev, double interval);


	/**
	 * The registrar is used for creating components which need to report their events.
	 * @param registrar
	 */
	void setRegistrar(IPlanRegistrar registrar);

}
