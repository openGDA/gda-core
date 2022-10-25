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

/**
 * Factory methods for {@link ISegment}s and {@link ITrigger}s which do not specify {@link ISampleEnvironmentVariable}s.
 * The implementation caches the last {@link ISampleEnvironmentVariable} registered to an {@link IPlan}
 * and assumes you want to use that one with the methods here.
 */
public interface ConveniencePlanFactory {

	/**
	 * Creates the next segment in the experiment (segments are executed in order of creation).
	 *
	 * @param name				The name of this segment.
	 * @param limit 			The {@link LimitCondition} which will terminate this segment once met.
	 * @param triggers			The triggers which should be enabled during this segment
	 *
	 * @return 					Reference to the created segment
	 */
	ISegment addSegment(String name, LimitCondition limit, ITrigger... triggers);


	/**
	 * Creates the next time-based segment with the given duration
	 *
	 * @param name				The name of this segment.
	 * @param duration			The duration in seconds of this segment
	 * @param triggers			The triggers which should be enabled during this segment
	 *
	 * @return 					Reference to the created segment
	 */
	ISegment addSegment(String name, double duration, ITrigger... triggers);


	/**
	 * Create a trigger that will perform an operation only once,
	 * when signal from {@code sev} = {@code target} ± {@code tolerance}
	 *
	 * @param name 				Name of this trigger
	 * @param payload			Request to handle upon triggering
	 * @param target			The optimal triggering signal
	 * @param tolerance			The acceptable tolerance around the target signal
	 *
	 * @return 					Reference to the created trigger
	 */
	ITrigger addTrigger(String name, Payload payload, double target, double tolerance);


	/**
	 * Create a trigger that will perform an operation only once,
	 * when signal from {@code sev} = {@code target} ± {@code tolerance}
	 *
	 * @param name 				Name of this trigger
	 * @param payload			Request to handle upon triggering
	 * @param target			The optimal triggering signal
	 * @param tolerance			The acceptable tolerance around the target signal
	 *
	 * @return 					Reference to the created trigger
	 */
	ITrigger addTrigger(String name, Object payload, double target, double tolerance);


	/**
	 * Creates a trigger which will execute the given operation in specified intervals of signal from sample environment variable provided
	 *
	 * @param name of this trigger
	 * @param payload to handle upon triggering
	 * @param interval or period between triggers
	 *
	 * @return reference to the created trigger
	 */
	ITrigger addTrigger(String name, Payload payload, double interval);


	/**
	 * Creates a trigger which will execute the given operation in specified intervals of signal from sample environment variable provided
	 *
	 * @param name of this trigger
	 * @param payload to handle upon triggering
	 * @param interval or period between triggers
	 *
	 * @return reference to the created trigger
	 */
	ITrigger addTrigger(String name, Object payload, double interval);

}
