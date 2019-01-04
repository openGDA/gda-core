package uk.ac.diamond.daq.experiment.plan;

import java.time.ZonedDateTime;

import uk.ac.diamond.daq.experiment.api.plan.ISegmentAccount;

/**
 * Keep track of when a particular ISegment begins and ends, and what signal caused it to terminate
 * 
 * @author Douglas Winter
 */
public class SegmentAccount implements ISegmentAccount {
	
	private final String segmentName;
	private final ZonedDateTime startTime;
	private ZonedDateTime endTime;
	
	private double terminationSignal;
	
	SegmentAccount(String segmentName) {
		startTime = ZonedDateTime.now();
		this.segmentName = segmentName;
	}
	
	@Override
	public void terminated(double terminationSignal) {
		endTime = ZonedDateTime.now();
		this.terminationSignal = terminationSignal;
	}
	
	@Override
	public String getSegmentName() {
		return segmentName;
	}
	
	@Override
	public ZonedDateTime getStartTime() {
		return startTime;
	}
	
	@Override
	public ZonedDateTime getEndTime() {
		return endTime;
	}
	
	@Override
	public double getTerminationSignal() {
		return terminationSignal;
	}
}
