package uk.ac.diamond.daq.experiment.ui.plan.preview;

import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;

public class SingleTimeBasedTriggerLocator extends SingleTriggerLocator {

	public SingleTimeBasedTriggerLocator(TriggerRequest trigger, Dataset xDataset, Dataset yDataset) {
		super(trigger, xDataset, yDataset);
	}

	@Override
	public void search(double xStart, double xEnd) {
		double requestedX = xStart + target / 60.0;
		if (requestedX > xEnd) return; 
		x = requestedX;
		y = interpolator.getY(requestedX);
	}

}
