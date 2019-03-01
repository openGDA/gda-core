package uk.ac.diamond.daq.experiment.driver;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import uk.ac.diamond.daq.experiment.api.driver.AbortCondition;
import uk.ac.diamond.daq.experiment.api.driver.DriverState;
import uk.ac.diamond.daq.experiment.plan.MockSEV;

public class AbortConditionTest {

	@Test
	public void driverAbortedWhenAbortConditionReached() {
		TestDriver driver = new TestDriver();
		MockSEV sev = new MockSEV();

		driver.getModel().addAbortCondition(new AbortCondition(driver, sev, s -> s > 100));
		driver.start();

		// NoImplDriver finishes immediately...
		assertThat(driver.hasRun(), is(true));

		// ...so let's pretend it's still going
		driver.setState(DriverState.RUNNING);

		sev.broadcast(101); // over the limit!

		assertThat(driver.aborted, is(true));
		assertThat(driver.getState(), is(DriverState.IDLE));
	}

	private class TestDriver extends NoImplDriver {

		boolean aborted = false;

		@Override
		protected void doAbort() {
			aborted = true;
		}
	}

}
