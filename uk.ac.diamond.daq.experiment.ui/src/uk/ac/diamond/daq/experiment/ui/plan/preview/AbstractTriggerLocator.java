package uk.ac.diamond.daq.experiment.ui.plan.preview;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;

import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;

public abstract class AbstractTriggerLocator<T> implements TriggerLocator<T> {

	final TriggerRequest trigger;
	final Dataset xDataset;
	final Dataset yDataset;
	final LinearInterpolator interpolator;
	
	public AbstractTriggerLocator(TriggerRequest trigger, Dataset xDataset, Dataset yDataset) {
		this.trigger = trigger;
		this.xDataset = xDataset;
		this.yDataset = yDataset;
		this.interpolator = new LinearInterpolator(xDataset, yDataset);
	}
	
	/**
	 * returns the previous index, unless x <= x[0], in which case we return 0
	 */
	protected int getIndex(double x) {
		int index = DatasetUtils.findIndexGreaterThan(xDataset, x) - 1;
		return Math.max(index, 0);
	}

}
