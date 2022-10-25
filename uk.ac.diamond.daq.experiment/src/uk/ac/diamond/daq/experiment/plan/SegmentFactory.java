/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.plan;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;

import gda.device.Scannable;
import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;


/**
 * For simple and descriptive definitions of segments.
 *
 * Segments are declared either time-based or position-based,
 * and enabled by the segment are specified with {@link #activating(TriggerFactory...)}.<p>
 *
 * Examples:
 *
 * <ol>
 * <li>Scannable-based segment (given scannable 'temperature')
 * <pre>
 * creator.addSegment("heating").tracking(temperature).until(lambda t: t >= target_temp)
 * </pre>
 *
 * <li>Time-based segment
 * <pre>
 * creator.addSegment("10-minute cooling").ofDurationInMinutes(10).activating(diffraction_probe)
 * </pre>
 *
 * @see PlanCreator#addSegment(String)
 */
public class SegmentFactory extends PlanComponentFactory<ISegment> {

	private double duration;

	private LimitCondition limit;

	private List<TriggerFactory> triggers = new ArrayList<>();

	public SegmentFactory(String name) {
		super(name);
	}

	/**
	 * Specify the duration of this segment,
	 * in seconds.
	 */
	public SegmentFactory lastingInSeconds(double seconds) {
		setDuration(seconds);
		return this;
	}

	/**
	 * Specify the duration of this segment,
	 * in minutes.
	 */
	public SegmentFactory lastingInMinutes(double minutes) {
		setDuration(minutes * 60.0);
		return this;
	}

	/**
	 * Specify the duration of this segment,
	 * in hours.
	 */
	public SegmentFactory lastingInHours(double hours) {
		setDuration(hours * 60.0 * 60.0);
		return this;
	}

	private void setDuration(double seconds) {
		setTimerSev();
		this.duration = seconds;
	}

	/**
	 * Specify the scannable whose position
	 * governs the lifetime of this segment.
	 *
	 * Used alongside {@link #until(LimitCondition)}.
	 */
	public SegmentFactory tracking(Scannable scannable) {
		setScannableSev(scannable);
		return this;
	}

	/**
	 * Specify a custom signal source
	 * to govern the lifetime of this segment.
	 *
	 * Used alongside {@link #until(LimitCondition)}
	 */
	public SegmentFactory tracking(DoubleSupplier signalSource) {
		setCustomSev(signalSource);
		return this;
	}

	/**
	 * Specify a custom signal source
	 * to govern the lifetime of this segment.
	 *
	 * Used alongside {@link #until(LimitCondition)}
	 */
	public SegmentFactory tracking(DoubleSupplier signalSource, String name) {
		setCustomSev(signalSource, name);
		return this;
	}

	/**
	 * Specify the condition defining the end of this segment,
	 * in terms of position of tracking scannable.
	 *
	 * @see #tracking(Scannable)
	 * @see #tracking(DoubleSupplier)
	 */
	public SegmentFactory until(LimitCondition limit) {
		this.limit = limit;
		return this;
	}

	/**
	 * Specify the triggers to be activated
	 * during the execution of this segment
	 *
	 * @see PlanCreator#createTrigger(String)
	 */
	public SegmentFactory activating(TriggerFactory... triggers) {
		this.triggers = List.of(triggers);
		return this;
	}

	@Override
	ISegment build(IPlanRegistrar registrar) {
		requireNonNull(getSampleEnvironmentVariable(), "A signal source must be specified for segment: " + getName());

		ISegment segment = createSegment(registrar);
		segment.setName(getName());

		triggers.stream()
			.map(builder -> builder.build(registrar))
			.forEach(segment::enable);

		return segment;
	}

	private ISegment createSegment(IPlanRegistrar registrar) {
		switch (getSource()) {
		case POSITION:
			return new SimpleSegment(registrar, getSampleEnvironmentVariable(), requireNonNull(limit, "Limit condition must be specified for segment:" + getName()));
		case TIME:
			return new FixedDurationSegment(registrar, getSampleEnvironmentVariable(), duration);
		default:
			throw new IllegalStateException("Unsupported signal source: " + getSource());
		}
	}

}
