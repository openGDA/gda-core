package uk.ac.diamond.daq.experiment.ui.plan.preview;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;

/**
 * This locator is reusable; x and y should be cleared at the start of every search
 */
public abstract class RepeatingTriggerLocator extends AbstractTriggerLocator<List<Double>> {

	protected final List<Double> x;
	protected final List<Double> y;
	
	protected final double interval;
	
	public RepeatingTriggerLocator(TriggerRequest trigger, Dataset xDataset, Dataset yDataset) {
		super(trigger, xDataset, yDataset);
		x = new ArrayList<>();
		y = new ArrayList<>();
		interval = trigger.getInterval(); 
	}
	
	@Override
	public Optional<List<Double>> getX() {
		if (x.isEmpty()) return Optional.empty();
		return Optional.of(x);
	}

	@Override
	public Optional<List<Double>> getY() {
		if (y.isEmpty()) return Optional.empty();
		return Optional.of(y);
	}

}
