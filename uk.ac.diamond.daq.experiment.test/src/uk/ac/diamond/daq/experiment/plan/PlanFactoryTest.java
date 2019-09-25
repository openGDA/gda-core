package uk.ac.diamond.daq.experiment.plan;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.ISegment;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.LimitCondition;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;

/**
 * These tests document the standard procedure for creating
 * the various implementations of {@link ISegment}s and {@link ITrigger}s.
 */
public class PlanFactoryTest {

	private final static String SEGMENT_NAME = "a segment";
	private final static String TRIGGER_NAME = "a trigger";

	private PlanFactory factory;
	private ISampleEnvironmentVariable sev;
	private ISampleEnvironmentVariable timer;

	@Before
	public void setup() {
		factory = new PlanFactory();
		sev = factory.addSEV(()->0.0);
		timer = factory.addTimer();
	}

	/**
	 * Creates a position based segment.
	 *
	 * Once active, this segment will listen to an {@link ISampleEnvironmentVariable}
	 * and test its {@link LimitCondition} with each broadcast, terminating when the condition is met
	 */
	@Test
	public void positionBasedSegment() {
		ISegment positionBasedSegment = factory.addSegment(SEGMENT_NAME, sev, sevSignal -> sevSignal >= 10);
		assertThat(positionBasedSegment, is(instanceOf(SimpleSegment.class)));
	}

	/**
	 * Creates a time based segment which remains active for a given duration in seconds.
	 */
	@Test
	public void timeBasedSegment() {
		ISegment timeBasedSegment = factory.addSegment(SEGMENT_NAME, timer, 60);
		assertThat(timeBasedSegment, is(instanceOf(FixedDurationSegment.class)));
	}

	/**
	 * Creates a trigger which fires once when signal from {@link ISampleEnvironmentVariable}
	 * equals the given triggering signal +/- given tolerance.
	 *
	 * See {@link #singleTimeTrigger()} for time special case
	 */
	@Test
	public void singleTrigger() {
		ITrigger singleGenericTrigger = factory.addTrigger(TRIGGER_NAME, // trigger name
															this::work,  // triggerable
															sev,		 // signal source
															3.5,		 // triggering signal
															0.01);		 // tolerance
		assertThat(singleGenericTrigger, is(instanceOf(SingleTrigger.class)));
	}

	/**
	 * Creates a trigger which will fire once at given time after activation.
	 *
	 * A special case of single trigger when the {@link ISampleEnvironmentVariable} is the one
	 * given by {@code factory.addTimer()}.
	 */
	@Test
	public void singleTimeTrigger() {
		ITrigger singleTimeTrigger = factory.addTrigger(TRIGGER_NAME, this::work, timer, 5, 0.2);
		assertThat(singleTimeTrigger, is(instanceOf(SingleTimeBasedTrigger.class)));
	}

	/**
	 * Creates a trigger which triggers a generic {@link Triggerable}
	 * in specified intervals of signal from given {@link ISampleEnvironmentVariable}.
	 *
	 * Unlike with single triggers, there is no special implementation for time-based triggering.
	 */
	@Test
	public void repeatingTrigger() {
		ITrigger repeatingTrigger = factory.addTrigger(TRIGGER_NAME, this::work, sev, 2.5);
		assertThat(repeatingTrigger, is(instanceOf(RepeatingTrigger.class)));
	}

	/**
	 * placeholder Triggerable
	 */
	private Object work() {
		return null;
	}

}
