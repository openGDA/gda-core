package uk.ac.diamond.daq.experiment.ui.plan.preview;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import java.util.Optional;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.junit.Test;

import uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;

public class SinglePositionBasedTriggerLocatorTest {

	private static final double ERROR = 1e-9;

	@Test
	public void increasingSignal() {
		// easy maths: single line with m = 1, c = 0
		Dataset xData = data(0.0, 1.0);
		Dataset yData = data(0.0, 1.0);

		double target = 0.4;
		double tolerance = 0.01;

		double expectedX = target - tolerance;
		double expectedY = target - tolerance;

		test(xData, yData, target, tolerance, expectedX, expectedY);
	}

	@Test
	public void decreasingSignal() {
		// easy maths: single line with m = -1, c = 1
		Dataset xData = data(0.0, 1.0);
		Dataset yData = data(1.0, 0.0);

		double target = 0.4;
		double tolerance = 0.01;

		double predictedY = target + tolerance;
		double predictedX = (predictedY - 1) * -1;

		test(xData, yData, target, tolerance, predictedX, predictedY);
	}

	@Test
	public void searchMultipleDataPoints() {
		Dataset xData = data(0.0, 0.5, 1.0);
		Dataset yData = data(1.0, 2.5, 0.0);

		double target = 0.4;
		double tolerance = 0.01;

		double m = -2.5/0.5;
		double c = -m;

		double expectedY = target + tolerance;
		double expectedX = (expectedY - c) / m;

		test(xData, yData, target, tolerance, expectedX, expectedY);
	}

	@Test
	public void triggerRightAtTheStart() {
		Dataset xData = data(0.0, 1.0);
		Dataset yData = data(5.5, 5.4);

		double target = 5.45;
		double tolerance = 0.05;

		double predictedX = 0;
		double predictedY = 5.5;

		test(xData, yData, target, tolerance, predictedX, predictedY);
	}

	@Test
	public void rightAtTheStartTargetAboveDecreasingSignalButWithinTolerance() {
		Dataset xData = data(0.0, 1.0);
		Dataset yData = data(5.5, 5.4);

		double target = 5.55;
		double tolerance = 0.05;

		double predictedX = 0;
		double predictedY = 5.5;

		test(xData, yData, target, tolerance, predictedX, predictedY);
	}

	@Test
	public void triggerNotFound() {
		Dataset xData = data(0.0, 1.0);
		Dataset yData = data(2.0, 1.9);

		double target = 1.8;
		double tolerance = 0.05;

		TriggerDescriptor trigger = getTrigger(target, tolerance);
		SinglePositionBasedTriggerLocator locator = new SinglePositionBasedTriggerLocator(trigger, xData, yData);
		locator.search(0, 1);

		assertThat(locator.getX().isPresent(), is(false));
		assertThat(locator.getY().isPresent(), is(false));
	}

	@Test
	public void startBetweenPoints() {
		Dataset xData = data(0.0, 0.1, 0.2, 0.3);
		Dataset yData = data(0.0, 1.0, 1.0, 0.0);

		double target = 1.0;
		double tolerance = 0.0;

		double segmentStart = 0.05;
		double segmentEnd = 0.25;

		double expectedX = 0.1;

		test(xData, yData, target, tolerance, segmentStart, segmentEnd, expectedX, target);
	}

	@Test
	public void segmentLimitsWithinTheSameDatasetLine() {
		Dataset xData = data(0.0, 0.1, 0.2);
		Dataset yData = data(1.0, 2.0, 4.0);

		double segmentStart = 0.1;
		double segmentEnd = 0.16;

		double target = 3.0;
		double tolerance = 0;

		double expectedX = 0.15;

		test(xData, yData, target, tolerance, segmentStart, segmentEnd, expectedX, target);
	}

	@Test
	public void segmentSpanningMoreThanOneDataPointTargetInLastBit() {
		Dataset xData = data(0.0, 0.1, 0.2, 0.3, 0.4, 0.5);
		Dataset yData = data(0.0, 1.0, 2.0, 4.0, 8.0, 10.0);

		double target = 8.5;
		double tolerance = 0.01;

		double segmentStart = 0.2;
		double segmentEnd = 0.46;

		double expectedY = 8.49;
		double m = (10-8)/0.1;
		double c = 10 - m*0.5;
		double expectedX = (expectedY - c) / m;

		test(xData, yData, target, tolerance, segmentStart, segmentEnd, expectedX, expectedY);
	}

	@Test
	public void targetPreciselyAtSearchStart() {
		Dataset xData = data(0.0, 0.2);
		Dataset yData = data(0.0, 1);

		double target = 0.0;
		double tolerance = 0.5; // huge unambiguous tolerance

		// We should find the point at y = target, not at y = tolerance
		test(xData, yData, target, tolerance, 0.0, 0.0);
	}

	@Test
	public void onlyMagnitudeOfToleranceImportant() {
		Dataset xData = data(0.0, 1.0);

		double target = 0.1;
		double tolerance = -0.01;

		double expected = target - Math.abs(tolerance);

		test(xData, xData, target, tolerance, expected, expected);
	}

	private void test(Dataset xData, Dataset yData, double target, double tolerance, double expectedX, double expectedY) {
		test(xData, yData, target, tolerance, xData.getElementDoubleAbs(0), xData.getElementDoubleAbs(xData.getSize()-1), expectedX, expectedY);
	}

	private void test(Dataset xData, Dataset yData, double target, double tolerance, double segmentStart, double segmentEnd, double expectedX, double expectedY) {
		TriggerDescriptor trigger = getTrigger(target, tolerance);

		SinglePositionBasedTriggerLocator locator = new SinglePositionBasedTriggerLocator(trigger, xData, yData);
		locator.search(segmentStart, segmentEnd);

		Optional<Double> x = locator.getX();
		Optional<Double> y = locator.getY();

		assertThat(x.isPresent(), is(true));
		assertThat(y.isPresent(), is(true));

		assertThat(x.get(), is(closeTo(expectedX, ERROR)));
		assertThat(y.get(), is(closeTo(expectedY, ERROR)));
	}

	private TriggerDescriptor getTrigger(double target, double tolerance) {
		TriggerDescriptor trigger = new TriggerDescriptor();
		trigger.setExecutionPolicy(ExecutionPolicy.SINGLE);
		trigger.setSignalSource(SignalSource.POSITION);
		trigger.setTarget(target);
		trigger.setTolerance(tolerance);
		return trigger;
	}

	private Dataset data(double... values) {
		return DatasetFactory.createFromObject(values);
	}
}
