package uk.ac.diamond.daq.experiment.ui.plan.preview;

import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;

public class RepeatingTimeBasedTriggerLocator extends RepeatingTriggerLocator {

	public RepeatingTimeBasedTriggerLocator(TriggerRequest trigger, Dataset xDataset, Dataset yDataset) {
		super(trigger, xDataset, yDataset);
	}

	@Override
	public void search(double xStart, double xEnd) {
		
		if (interval == 0.0) return; 
		
		x.clear();
		y.clear();
		
		int numberOfTriggers = (int) Math.floor((xEnd-xStart)*60/interval);
		if (numberOfTriggers < 1) return;
		
		for (int index = 0; index < numberOfTriggers; index++) {
			x.add(xStart + (index + 1) * interval / 60.0);
			y.add(interpolator.getY(x.get(index)));
		}
	}

}
