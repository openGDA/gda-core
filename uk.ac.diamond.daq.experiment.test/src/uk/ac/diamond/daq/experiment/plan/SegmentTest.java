package uk.ac.diamond.daq.experiment.plan;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;

public class SegmentTest {
	private SegmentBase segment;


	@Before
	public void setUp() {
		segment = new SimpleSegment(mock(Plan.class), mock(ISampleEnvironmentVariable.class), signal -> signal < 0);
	}

	@Test
	public void testActivationLogic() {
		assertFalse(segment.isActivated());
		segment.activate();
		assertTrue(activeAfterSignalChange(5));
		assertFalse(activeAfterSignalChange(-1));
	}

	@Test
	public void inactiveFollowingAbort() {
		segment.activate();
		segment.abort();
		assertFalse(segment.isActivated());
	}

	@Test
	public void controllingTriggers() {
		ITrigger enabledTP = mock(ITrigger.class);
		segment.enable(enabledTP);
		segment.activate();
		segment.signalChanged(-2.5);
		verify(enabledTP, times(1)).setEnabled(true); // on activate()
		verify(enabledTP, times(1)).setEnabled(false); // at segment end
	}

	@Test
	public void triggersDisabledFollowingAbort() {
		var trigger = mock(ITrigger.class);
		segment.enable(trigger);
		segment.activate();
		segment.abort();
		assertFalse(trigger.isEnabled());
	}

	private boolean activeAfterSignalChange(double signal) {
		segment.signalChanged(signal);
		return segment.isActivated();
	}

}
