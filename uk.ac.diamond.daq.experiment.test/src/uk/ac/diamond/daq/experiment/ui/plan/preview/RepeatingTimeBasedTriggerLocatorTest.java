package uk.ac.diamond.daq.experiment.ui.plan.preview;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

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

	private TriggerLocator<List<Double>> getLocator(double interval, Dataset xData, Dataset yData) {
		TriggerDescriptor trigger = new TriggerDescriptor();
		trigger.setSignalSource(SignalSource.TIME);
		trigger.setExecutionPolicy(ExecutionPolicy.REPEATING);
		trigger.setInterval(interval);

		return new RepeatingTimeBasedTriggerLocator(trigger, xData, yData);
	}

	private Dataset data(double... points) {
		return DatasetFactory.createFromObject(points);
	}

}
