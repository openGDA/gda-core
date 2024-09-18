package uk.ac.diamond.daq.experiment.ui.plan.preview;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.junit.Test;

import uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;

public class RepeatingTimeBasedTriggerLocatorTest {

	private static final double ERROR = 1e-9;

	@Test
	public void triggersOccur() {
		Dataset xData = data(0.0, 0.5, 1.0);
		Dataset yData = data(1.0, 0.0, 2.0);

		double interval = 25; // seconds

		TriggerLocator<List<Double>> locator = getLocator(interval, xData, yData);
		locator.search(0, 1);

		int numberOfTriggers = 2; // at 25s and at 50s

		double expectedX1 = 25/60.0;

		// first line params:
		double m = -1 / 0.5;
		double c = - m * 0.5;
		double expectedY1 = m * expectedX1 + c;

		double expectedX2 = 50/60.0;

		// second line:
		m =  2/0.5;
		c = 2 - m * 1;
		double expectedY2 = m * expectedX2 + c;

		assertThat(locator.getX().isPresent(), is(true));
		List<Double> xs = locator.getX().get();
		List<Double> ys = locator.getY().get();
		assertThat(xs.size(), is(equalTo(numberOfTriggers)));
		assertThat(xs.get(0), is(closeTo(expectedX1, ERROR)));
		assertThat(ys.get(0), is(closeTo(expectedY1, ERROR)));
		assertThat(xs.get(1), is(closeTo(expectedX2, ERROR)));
		assertThat(ys.get(1), is(closeTo(expectedY2, ERROR)));
	}

	@Test
	public void noTriggersOccur() {
		Dataset xData = data(0.0, 0.5, 1.0);
		Dataset yData = data(1.0, 0.0, 2.0);

		double interval = 25; // seconds

		TriggerLocator<List<Double>> locator = getLocator(interval, xData, yData);
		locator.search(0.5, 0.7);

		assertThat(locator.getX().isPresent(), is(false));
		assertThat(locator.getY().isPresent(), is(false));
	}

	@Test
	public void noCalculationForZeroInterval() {
		Dataset xData = data(0.0, 0.5);
		Dataset yData = data(0.0, 0.5);

		double interval = 0;

		TriggerLocator<List<Double>> locator = getLocator(interval, xData, yData);
		locator.search(0.0, 0.5);

		assertThat(locator.getX().isPresent(), is(false));
		assertThat(locator.getY().isPresent(), is(false));
	}

	@Test
	public void triggersWithOffsetAllBoundWithinSegment() {

		var limit = 1.0; // in min

		var interval = 5; // in s
		var offset = 1; // in s

		Dataset xData = data(0.0, limit);
		Dataset yData = data(0.0, 0.0);

		TriggerLocator<List<Double>> locator = getLocator(interval, offset, xData, yData);
		locator.search(0.0, limit);

		var xResults = locator.getX().get();

		var allBoundWithinSegment = xResults.stream().noneMatch(x -> x > limit);
		assertTrue("At least one trigger found outside the segment bounds!", allBoundWithinSegment);

		var expectedTriggers = (int) Math.floor((limit * 60 - offset) / interval);
		assertThat(xResults.size(), is(equalTo(expectedTriggers)));
	}

	@Test
	public void negativeOffsetCorrectlyHandled() {
		// in min:
		var limit = 1; // 60 s

		// in seconds:
		var period = 10;
		var offset = -2;

		// without offset we would expect 5 triggers
		// (6th would be exactly on the limit so would not occur)

		Dataset xData = data(0.0, limit);
		Dataset yData = data(0.0, 0.0);

		TriggerLocator<List<Double>> locator = getLocator(period, offset, xData, yData);
		locator.search(0.0, limit);

		var xResults = locator.getX().get();

		var allBoundWithinSegment = xResults.stream().noneMatch(x -> x > limit);
		assertTrue("At least one trigger found outside the segment bounds!", allBoundWithinSegment);

		// with the above offset we would expect 6 triggers
		assertThat(xResults.size(), is(equalTo(6)));

		// first at 8 seconds...
		assertEquals(8/60.0, xResults.getFirst(), ERROR);

		// last at 58 seconds...
		assertEquals(58/60.0, xResults.getLast(), ERROR);
	}

	private TriggerLocator<List<Double>> getLocator(double interval, Dataset xData, Dataset yData) {
		return getLocator(interval, 0, xData, yData);
	}

	private TriggerLocator<List<Double>> getLocator(double interval, double offset, Dataset xData, Dataset yData) {
		TriggerDescriptor trigger = new TriggerDescriptor();
		trigger.setSignalSource(SignalSource.TIME);
		trigger.setExecutionPolicy(ExecutionPolicy.REPEATING);
		trigger.setInterval(interval);
		trigger.setOffset(offset);

		return new RepeatingTimeBasedTriggerLocator(trigger, xData, yData);
	}

	private Dataset data(double... points) {
		return DatasetFactory.createFromObject(points);
	}

}
