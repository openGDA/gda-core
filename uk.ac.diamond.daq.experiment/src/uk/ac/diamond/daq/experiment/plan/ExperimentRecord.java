package uk.ac.diamond.daq.experiment.plan;

import java.util.ArrayList;
import java.util.List;

import uk.ac.diamond.daq.experiment.api.plan.IExperimentRecord;
import uk.ac.diamond.daq.experiment.api.plan.IPlan;
import uk.ac.diamond.daq.experiment.api.plan.ISegmentAccount;
import uk.ac.diamond.daq.experiment.api.plan.ITriggerAccount;
import uk.ac.diamond.daq.experiment.api.plan.ITriggerEvent;


/**
 * Instances of this class keep track of what's going on during an {@link IPlan} run
 * 
 * @author Douglas Winter
 */
public class ExperimentRecord implements IExperimentRecord {
	
	private List<ISegmentAccount> segmentAccounts = new ArrayList<>();
	private List<ITriggerAccount> triggerAccounts = new ArrayList<>();
	
	protected void segmentActivated(String segmentName) {
		segmentAccounts.add(new SegmentAccount(segmentName));
	}
	
	protected void segmentComplete(String segmentName, double terminationSignal) {
		getSegmentAccount(segmentName).terminated(terminationSignal);
	}
	
	protected void triggerOccurred(String triggerName, double triggeringSignal) {
		final ITriggerAccount account = triggerAccounts.stream()
									.filter(t -> t.getTriggerName().equals(triggerName))
									.findFirst().orElse(new TriggerAccount(triggerName));
		
		account.triggered(triggeringSignal);
		if (!triggerAccounts.contains(account)) triggerAccounts.add(account);
	}
	
	@Override
	public List<ISegmentAccount> getSegmentAccounts() {
		return segmentAccounts;
	}
	
	@Override
	public List<ITriggerAccount> getTriggerAccounts() {
		return triggerAccounts;
	}
	
	@Override
	public ISegmentAccount getSegmentAccount(String segmentName) {
		return segmentAccounts.stream()
					.filter(a -> a.getSegmentName().equals(segmentName))
					.findFirst()
					.orElseThrow(()->new IllegalArgumentException("No account for segment '" + segmentName + "' found"));
	}
	
	@Override
	public ITriggerAccount getTriggerAccount(String triggerName) {
		return triggerAccounts.stream()
				.filter(a -> a.getTriggerName().equals(triggerName))
				.findFirst()
				.orElseThrow(()->new IllegalArgumentException("No account for trigger '" + triggerName + "' found"));
	}
	
	@Override
	public String summary() {
		StringBuilder summary = new StringBuilder("Summary:\n");
		
		for (ISegmentAccount segment : segmentAccounts) {
			summary.append("Segment '").append(segment.getSegmentName())
				.append("' activate between ").append(segment.getStartTime())
				.append(" and ").append(segment.getEndTime())
				.append("\n");
		}
		
		for (ITriggerAccount trigger : triggerAccounts) {
			summary.append("Trigger '").append(trigger.getTriggerName()).append("' fired at the following times:\n");
			for (ITriggerEvent event : trigger.getEvents()) {
				summary.append(event.getTimestamp()).append('\n');
			}
		}
		
		return summary.toString();
	}
}
