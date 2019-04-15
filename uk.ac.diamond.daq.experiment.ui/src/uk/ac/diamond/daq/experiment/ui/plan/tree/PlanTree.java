package uk.ac.diamond.daq.experiment.ui.plan.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.event.status.Status;

import uk.ac.diamond.daq.experiment.api.plan.IPlan;
import uk.ac.diamond.daq.experiment.api.plan.event.PlanStatusBean;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerRecord;

/**
 * Tree representation of a {@link IPlan} consisting of two levels: segments and their triggers.
 */
public class PlanTree {
	
	private final List<SegmentNode> segments;
	
	/**
	 * The map enables us to restore the status of a triggered scan
	 */
	public PlanTree(PlanStatusBean bean, Map<String, TriggerNode> triggerStatuses) {
		segments = new ArrayList<>();
		bean.getSegments().forEach(segment -> {
			List<TriggerRecord> triggers = new ArrayList<>();
			final Status status;
			if (segment.getEndTime() < segment.getStartTime()) {
				// segment is ongoing
				triggers = bean.getTriggers().stream()
					.filter(t -> t.getEvents().stream()
							.anyMatch(e -> e.getTimestamp() >= segment.getStartTime()))
					.collect(Collectors.toList());
				status = Status.RUNNING;
			} else {
				// segment has finished
				triggers = bean.getTriggers().stream()
					.filter(t -> t.getEvents().stream()
							.filter(e -> e.getTimestamp() >= segment.getStartTime())
							.anyMatch(e -> e.getTimestamp() < segment.getEndTime()))
					.collect(Collectors.toList());
				status = Status.COMPLETE;
			}
			segments.add(new SegmentNode(segment, triggers, status));
		});
		
		getTriggers().forEach(trigger -> populateStatus(trigger, triggerStatuses));
	}
	
	private void populateStatus(TriggerNode trigger, Map<String, TriggerNode> knownStatuses) {
		if (knownStatuses.containsKey(trigger.getId())) {
			trigger.setStatus(knownStatuses.get(trigger.getId()).getStatus());
		}
	}

	public List<SegmentNode> getSegments() {
		return segments;
	}
	
	public List<TriggerNode> getTriggers() {
		return getSegments().stream().flatMap(s -> s.getTriggerEvents().stream()).collect(Collectors.toList());
	}
	
}
