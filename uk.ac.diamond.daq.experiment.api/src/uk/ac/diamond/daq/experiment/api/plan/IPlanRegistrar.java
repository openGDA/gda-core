package uk.ac.diamond.daq.experiment.api.plan;

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
	 * @param triggeringSignal
	 */
	void triggerOccurred(ITrigger trigger, double triggeringSignal);
	
	
	/**
	 * Called to report that the specified {@link ISegment} has become active
	 * 
	 * @param segment
	 */
	void segmentActivated(ISegment segment);
	
	
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
