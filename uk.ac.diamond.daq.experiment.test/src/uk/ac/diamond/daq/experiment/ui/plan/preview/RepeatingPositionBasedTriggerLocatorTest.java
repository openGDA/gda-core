package uk.ac.diamond.daq.experiment.ui.plan.preview;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Optional;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.junit.Test;

import uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;

public class RepeatingPositionBasedTriggerLocatorTest {

	private static final double ERROR = 1e-9;

	@Test
	public void increasingSignal() {
		Dataset xData = data(0.0, 0.5);
		Dataset yData = data(0.0, 1.0);

		double interval = 0.2;

		List<Double> expectedY = asList(0.2, 0.4, 0.6, 0.8, 1.0);
		List<Double> expectedX = asList(0.1, 0.2, 0.3, 0.4, 0.5);

		test(xData, yData, interval, expectedX, expectedY);
	}

	@Test
	public void decreasingSignal() {
		Dataset xData = data(0.0, 0.5);
		Dataset yData = data(1.0, 0.0);

		double interval = 0.2;

		List<Double> expectedY = asList(0.8, 0.6, 0.4, 0.2, 0.0);
		List<Double> expectedX = asList(0.1, 0.2, 0.3, 0.4, 0.5);

		test(xData, yData, interval, expectedX, expectedY);
	}

	@Test
	public void trapezoidSignal() {
		Dataset xData = data(1.0, 2.5, 3.0, 4.5);
		Dataset yData = data(1.0, 2.5, 2.5, 1.0);

		double interval = 0.5;

		List<Double> expectedY = asList(1.5, 2.0, 2.5, 2.0, 1.5, 1.0);
		List<Double> expectedX = asList(1.5, 2.0, 2.5, 3.5, 4.0, 4.5);

		test(xData, yData, interval, expectedX, expectedY);
	}

	@Test
	public void trapezoidWithLimitedRange() {
		Dataset xData = data(1.0, 2.5, 3.0, 4.5);
		Dataset yData = data(1.0, 2.5, 2.5, 1.0);

		double interval = 0.5;

		List<Double> expectedY = asList(2.3, 1.8, 1.3);
		List<Double> expectedX = asList(2.3, 3.7, 4.2);
		test(xData, yData, interval, 1.8, 4.2, expectedX, expectedY);
	}

	@Test
	public void startAndEndWithinSameLine() {
		Dataset xData = data(0.0, 1.0);
		Dataset yData = data(0.0, 1.0);

		double interval = 0.1;

		double startX = 0;
		double stopX = 0.5;

		List<Double> expectedY = asList(0.1, 0.2, 0.3, 0.4, 0.5);
		List<Double> expectedX = expectedY; // because m = 1, c = 0 therefore y(x) = x

		test(xData, yData, interval, startX, stopX, expectedX, expectedY);
	}

	@Test
	public void noTriggerFound() {
		Dataset xData = data(0.1, 0.2, 0.3);
		Dataset yData = data(10, 7, 8.4);

		double interval = 3.5;

		TriggerDescriptor trigger = getTrigger(interval);
		RepeatingPositionBasedTriggerLocator locator = new RepeatingPositionBasedTriggerLocator(trigger, xData, yData);
		locator.search(0.1, 0.3);

		assertThat(locator.getX().isPresent(), is(false));
		assertThat(locator.getY().isPresent(), is(false));
	}

	@Test
	public void noCalculationForZeroInterval() {
		Dataset xData = data(0.0, 1.0);
		double interval = 0.0;

		TriggerLocator<List<Double>> locator = new RepeatingPositionBasedTriggerLocator(getTrigger(interval), xData, xData);
		locator.search(0.0, 1.0);

		assertThat(locator.getX().isPresent(), is(false));
		assertThat(locator.getY().isPresent(), is(false));
	}

	private void test(Dataset xData, Dataset yData, double interval, List<Double> expectedX, List<Double> expectedY) {
		test(xData, yData, interval, xData.getElementDoubleAbs(0), xData.getElementDoubleAbs(xData.getSize()-1), expectedX, expectedY);
	}

	private void test(Dataset xData, Dataset yData, double interval, double startX, double stopX, List<Double> expectedX, List<Double> expectedY) {
		TriggerDescriptor trigger = getTrigger(interval);
		RepeatingPositionBasedTriggerLocator locator = new RepeatingPositionBasedTriggerLocator(trigger, xData, yData);
		locator.search(startX, stopX);

		Optional<List<Double>> x = locator.getX();
		Optional<List<Double>> y = locator.getY();

		assertThat(x.isPresent(), is(true));
		assertThat(y.isPresent(), is(true));

		assertThat(x.get().size(), is(equalTo(expectedX.size())));
		assertThat(y.get().size(), is(equalTo(expectedY.size())));

		for (int index = 0; index < expectedX.size(); index++) {
			assertThat(x.get().get(index), is(closeTo(expectedX.get(index), ERROR)));
			assertThat(y.get().get(index), is(closeTo(expectedY.get(index), ERROR)));
		}
	}
	private TriggerDescriptor getTrigger(double interval) {
		TriggerDescriptor trigger = new TriggerDescriptor();
		trigger.setExecutionPolicy(ExecutionPolicy.REPEATING);
		trigger.setSignalSource(SignalSource.POSITION);
		trigger.setInterval(interval);
		return trigger;
	}

	private Dataset data(double... values) {
		return DatasetFactory.createFromObject(values);
	}

}
