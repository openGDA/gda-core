package uk.ac.diamond.daq.experiment.ui.plan.preview;

import java.util.Optional;

import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;

public abstract class SingleTriggerLocator extends AbstractTriggerLocator<Double> {
	
	protected Double x;
	protected Double y;
	
	protected final double target;
	protected final double tolerance;

	public SingleTriggerLocator(TriggerRequest trigger, Dataset xDataset, Dataset yDataset) {
		super(trigger, xDataset, yDataset);
		this.target = trigger.getTarget();
		this.tolerance = trigger.getTolerance();
	}

	@Override
	public Optional<Double> getX() {
		return Optional.ofNullable(x);
	}

	@Override
	public Optional<Double> getY() {
		return Optional.ofNullable(y);
	}
	
}
