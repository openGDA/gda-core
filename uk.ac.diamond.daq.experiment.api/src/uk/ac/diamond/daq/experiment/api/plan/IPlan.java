package uk.ac.diamond.daq.experiment.api.plan;

import gda.factory.Findable;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;

/**
 * Centralised object to coordinate the experiment. This should make accounting after the fact a lot easier.
 * <p>
 * Using the factory methods to create sevs, triggers, etc will register the components after creation
 * and return the reference so that e.g. enabling/disabling triggers can be done outside of this instance.
 */
public interface IPlan extends IPlanFactory, Findable {


	/**
	 * We can optionally set an experiment driver to the plan, which is started when the plan starts
	 */
	void setDriver(IExperimentDriver experimentDriver);

	/**
	 * Once the entire plan is defined, this method will initiate it by activating the first {@link ISegment}
	 */
	void start();


	/**
	 * Test whether the plan is still running
	 * @return {@code true} if there is an active {@link ISegment}, else {@code false}
	 */
	boolean isRunning();


	/**
	 * Sets the factory which creates the plan components
	 * @param factory
	 */
	void setFactory(IPlanFactory factory);


	// Convenience factory methods for SEV-based components

	/**
	 * Creates the next segment in the experiment (segments are executed in order of creation).
	 *
	 * @param limit The {@link LimitCondition} which will terminate this segment once met. Signal provided by the last
	 * {@link ISampleEnvironmentVariable} added to this plan.
	 * @return reference to the created segment
	 */
	ISegment addSegment(String name, LimitCondition limit, ITrigger... triggers);


	/**
	 * Creates a trigger which will execute the given operation in specified intervals of signal from last defined
	 * {@link ISampleEnvironmentVariable}
	 *
	 * @param runnable which this trigger will perform
	 * @param triggerInterval
	 * @return reference to the created trigger
	 */
	ITrigger addTrigger(String name, Triggerable triggerable, double triggerInterval);


	/**
	 * Create an {@link ITrigger} that will trigger an {@code operation} only once,
	 * when signal from {@code sev} = {@code triggerSignal} ± {@code tolerance}
	 *
	 * @param runnable
	 * @param triggerSignal
	 * @param tolerance
	 * @return reference to the created trigger
	 */
	ITrigger addTrigger(String name, Triggerable triggerable, double triggerSignal, double tolerance);

}
