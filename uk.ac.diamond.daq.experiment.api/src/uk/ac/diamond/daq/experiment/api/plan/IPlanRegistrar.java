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

import uk.ac.diamond.daq.experiment.api.plan.event.TriggerEvent;

/**
 * The registrar is responsible for keeping a record of everything that happens during
 * the course of an {@link IPlan}. {@link ISegment}s will tell the registrar
 * when they become active/innactive, and {@link ITrigger}s will report when they fire, along
 * with the signal which caused them to do so.
 *
 * @author Douglas Winter
 *
 */
public interface IPlanRegistrar {


	/**
	 * Called to report that the specified {@link ITrigger} fired due to the specified signal
	 *
	 * @param trigger
	 */
	void triggerOccurred(ITrigger trigger);


	/**
	 * Complementary method to {@link #triggerOccurred(ITrigger, double)} called when a trigger ends
	 * either successfully or unsuccessfully
	 *
	 * @param trigger
	 * @param event
	 */
	void triggerComplete(ITrigger trigger, TriggerEvent event, String sampleEnvironmentName);


	/**
	 * Called to report that the specified {@link ISegment} has become active
	 *
	 * @param segment
	 * @param sampleEnvironmentName the name of the sample environment this segment depends on
	 */
	void segmentActivated(ISegment segment, String sampleEnvironmentName);


	/**
	 * Called to report that the specified {@link ISegment} has completed due to the specified signal
	 *
	 * @param segment
	 * @param terminatingSignal
	 */
	void segmentComplete(ISegment segment, double terminatingSignal);


	/**
	 * Gets the ExperimentRecord once the experiment is complete.
	 * @throws IllegalStateException if called while the experiment is still running
	 */
	IExperimentRecord getExperimentRecord();

}
