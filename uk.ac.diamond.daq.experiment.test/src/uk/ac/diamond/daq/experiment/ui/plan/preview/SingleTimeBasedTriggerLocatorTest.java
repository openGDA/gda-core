package uk.ac.diamond.daq.experiment.ui.plan.preview;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.junit.Test;

import uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;

public class SingleTimeBasedTriggerLocatorTest {

	private final static double ERROR = 1e-9;

	@Test
	public void triggerOccurs() {
		Dataset xData = data(0.0, 0.5, 1.0);
		Dataset yData = data(1.0, 0.5, 1.0);

		double target = 40; // seconds

		TriggerLocator<Double> locator = getLocator(target, xData, yData);
		locator.search(0, 1);

		double expectedX = target / 60.0;
		double m = 1;
		double c = 1 - m;
		double expectedY = m * expectedX + c;

		assertThat(locator.getX().isPresent(), is(true));
		assertThat(locator.getX().get(), is(closeTo(expectedX, ERROR)));
		assertThat(locator.getY().isPresent(), is(true));
		assertThat(locator.getY().get(), is(closeTo(expectedY, ERROR)));
	}

	@Test
	public void triggerDoesNotOccur() {
		Dataset xData = data(0.0, 0.5, 1.0);
		Dataset yData = data(1.0, 0.5, 1.0);

		double target = 40; // seconds

		TriggerLocator<Double> locator = getLocator(target, xData, yData);
		locator.search(0, 0.5); // we search in the first 30 seconds only

		assertThat(locator.getX().isPresent(), is(false));
		assertThat(locator.getY().isPresent(), is(false));
	}

	private TriggerLocator<Double> getLocator(double target, Dataset xData, Dataset yData) {
		TriggerDescriptor trigger = new TriggerDescriptor();
		trigger.setSignalSource(SignalSource.TIME);
		trigger.setExecutionPolicy(ExecutionPolicy.SINGLE);
		trigger.setTarget(target);

		return new SingleTimeBasedTriggerLocator(trigger, xData, yData);
	}

	private Dataset data(double... values) {
		return DatasetFactory.createFromObject(values);
	}

}
