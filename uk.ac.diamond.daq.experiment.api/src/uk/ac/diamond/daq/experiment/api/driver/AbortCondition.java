package uk.ac.diamond.daq.experiment.api.driver;

import java.util.function.Predicate;

import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.SEVListener;

/**
 * When an {@link IExperimentDriver}'s profile reaches the end, the experiment ends.
 * There may be additional conditions which should automatically abort.
 *
 * @author Douglas Winter
 *
 */
public class AbortCondition implements SEVListener {

	private final IExperimentDriver driver;
	private final ISampleEnvironmentVariable sev;
	private final Predicate<Double> condition;

	public AbortCondition(IExperimentDriver driver, ISampleEnvironmentVariable sev, Predicate<Double> condition) {
		this.driver = driver;
		this.sev = sev;
		this.condition = condition;
	}

	public void activate() {
		sev.addListener(this);
	}

	 public void deactivate() {
		 sev.removeListener(this);
	 }

	@Override
	public void signalChanged(double signal) {
		if (condition.test(signal)) driver.abort();
	}

	@Override
	public String getSampleEnvironmentName() {
		return sev.getName();
	}

}
