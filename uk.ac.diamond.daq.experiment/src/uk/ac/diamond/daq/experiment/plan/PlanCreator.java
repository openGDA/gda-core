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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.diamond.daq.experiment.api.plan.PayloadService;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * Plan wrapper for simple and descriptive definitions in user scripts
 * <p>
 * Example:
 * <pre>
 * creator = PlanCreator("Battery charging")
 * creator.addSegment("Charging").tracking(battery_current).until(lambda i: i > i_charged).activating(
 * 	creator.createTrigger("probe").tracking(battery_current).executing(scan5).every(2.5))
 * plan = creator.create()
 * plan.start()
 * </pre>
 */
public class PlanCreator {

	private String name;
	private List<SegmentFactory> segmentsToBuild;

	private PayloadService payloadService;

	public PlanCreator(String planName) {
		this.name = planName;
		segmentsToBuild = new LinkedList<>();
	}

	/**
	 * Add a new segment according to the parameters passed to the returned factory.
	 * After the final segment has been added, call {@link #create()}.
	 */
	public SegmentFactory addSegment(String segmentName) {
		var segment = new SegmentFactory(segmentName);
		segmentsToBuild.add(segment);
		return segment;
	}

	/**
	 * Create new trigger according to the parameters passed to the returned factory.
	 * Note that no reference is kept internally, so the trigger must be manually passed
	 * to one or several segments
	 *
	 * @see SegmentFactory#activating(TriggerFactory...)
	 */
	public TriggerFactory createTrigger(String triggerName) {
		return new TriggerFactory(triggerName, getPayloadService());
	}

	/**
	 * One the last segment has been specified ({@link #addSegment(String)},
	 * this method will return a ready-to-run experiment plan.
	 */
	public Plan create() {
		var plan = new Plan(name);

		var segments = segmentsToBuild.stream().map(segment -> segment.build(plan)).collect(Collectors.toList());
		plan.setSegments(segments);

		return plan;
	}

	private PayloadService getPayloadService() {
		if (payloadService == null) {
			payloadService = SpringApplicationContextFacade.getBean(PayloadService.class);
		}
		return payloadService;
	}

}
