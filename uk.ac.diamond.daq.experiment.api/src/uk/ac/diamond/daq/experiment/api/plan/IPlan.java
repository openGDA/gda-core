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

}
