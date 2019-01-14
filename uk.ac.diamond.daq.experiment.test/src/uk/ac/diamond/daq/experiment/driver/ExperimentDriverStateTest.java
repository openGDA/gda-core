package uk.ac.diamond.daq.experiment.driver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static uk.ac.diamond.daq.experiment.api.driver.DriverState.IDLE;
import static uk.ac.diamond.daq.experiment.api.driver.DriverState.PAUSED;
import static uk.ac.diamond.daq.experiment.api.driver.DriverState.RUNNING;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.experiment.api.driver.DriverState;

public class ExperimentDriverStateTest {

	private NoImplDriver driver;

	/////////////// TESTING VALID STATES ///////////////

	@Test
	public void startValidFromIdle() {
		assertThat(driver.getState(), is(IDLE));
		driver.start();
		assertThat(driver.getState(), is(RUNNING));
	}

	@Test
	public void pauseValidFromRunning() {
		driver.setState(RUNNING);
		driver.pause();
		assertThat(driver.getState(), is(PAUSED));
	}

	@Test
	public void resumeValidFromPaused() {
		driver.setState(PAUSED);
		driver.resume();
		assertThat(driver.getState(), is(RUNNING));
	}

	@Test
	public void abortValidFromRunningAndPause() {
		for (DriverState validState : Arrays.asList(RUNNING, PAUSED)) {
			driver.setState(validState);
			driver.abort();
			assertThat(driver.getState(), is(IDLE));
		}
	}


	////////////// TESTING INVALID STATES //////////////

	@Test
	public void startInvalidStates() {
		for (DriverState invalidState : Arrays.asList(RUNNING, PAUSED)) {
			driver.setState(invalidState);
			try {
				driver.start();
				fail("start() succesfully called from invalid state '" + invalidState.toString() + "'");
			} catch (IllegalStateException e) {
				assertThat(driver.getState(), is(invalidState));
			}
		}
	}

	@Test
	public void pauseInvalidStates() {
		for (DriverState invalidState : Arrays.asList(IDLE, PAUSED)) {
			driver.setState(invalidState);
			try {
				driver.pause();
				fail("pause() successfully called from invalid state '" + invalidState.toString() + "'");
			} catch (IllegalStateException e) {
				assertThat(driver.getState(), is(invalidState));
			}
		}
	}

	@Test
	public void resumeInvalidStates() {
		for (DriverState invalidState : Arrays.asList(IDLE, RUNNING)) {
			driver.setState(invalidState);
			try {
				driver.resume();
				fail("resume() successfully called from invalid state '" + invalidState.toString() + "'");
			} catch (IllegalStateException e) {
				assertThat(driver.getState(), is(invalidState));
			}
		}
	}

	@Test (expected = IllegalStateException.class)
	public void abortInvalidFromIdle() {
		driver.abort();
	}

	@Before
	public void initDriver() {
		driver = new NoImplDriver();
	}

}
