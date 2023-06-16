package uk.ac.diamond.daq.experiment.driver;

import uk.ac.diamond.daq.experiment.api.driver.DriverState;

/**
 * Implementation for tests which only Base logic
 * e.g. state transitions
 */
public class NoImplDriver extends ExperimentDriverBase {

	private boolean hasRun = false;

	@Override
	protected void doZero() { /*do nothing*/ }

	@Override
	protected void doStart() {
		hasRun = true;
	}

	@Override
	protected void doPause() { /*do nothing*/ }

	@Override
	protected void doResume() { /*do nothing*/ }

	@Override
	protected void doAbort() { /*do nothing*/ }

	void setState(DriverState state) {
		this.state = state;
	}

	public boolean hasRun() {
		return hasRun;
	}

}