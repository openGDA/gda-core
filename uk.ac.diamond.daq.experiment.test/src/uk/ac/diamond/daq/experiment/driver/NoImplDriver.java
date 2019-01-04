package uk.ac.diamond.daq.experiment.driver;

import uk.ac.diamond.daq.experiment.api.driver.DriverState;
import uk.ac.diamond.daq.experiment.driver.ExperimentDriverBase;

/**
 * Implementation for tests which only Base logic
 * e.g. state transitions
 */
public class NoImplDriver extends ExperimentDriverBase {

	@Override
	protected void doZero() { /*do nothing*/ }

	@Override
	protected void doStart() { /*do nothing*/ }

	@Override
	protected void doPause() { /*do nothing*/ }

	@Override
	protected void doResume() { /*do nothing*/ }

	@Override
	protected void doAbort() { /*do nothing*/ }
	
	void setState(DriverState state) {
		this.state = state;
	}
	
}