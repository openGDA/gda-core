package uk.ac.diamond.daq.experiment.ui.plan.preview;

import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;

public class TriggerLocatorFactory {
	
	private final Dataset xDataset;
	private final Dataset yDataset;
	
	public TriggerLocatorFactory(Dataset xDataset, Dataset yDataset) {
		this.xDataset = xDataset;
		this.yDataset = yDataset;
	}
	
	/**
	 * The parameterised type will be <ul>
	 * <li>{@code Double} for SINGLE execution policy
	 * <li>{@code List<Double>} for REPEATING execution policy
	 */
	public TriggerLocator<?> getTriggerLocator(TriggerRequest trigger) {
		
		switch (trigger.getSignalSource()) {
		
		case POSITION:
			switch (trigger.getExecutionPolicy()) {
			
			case REPEATING:
				return new RepeatingPositionBasedTriggerLocator(trigger, xDataset, yDataset);
			
			case SINGLE:
				return new SinglePositionBasedTriggerLocator(trigger, xDataset, yDataset);
			
			default:
				throw new IllegalArgumentException("Unsupported execution policy: " + trigger.getExecutionPolicy());
			}
			
		case TIME:
			switch (trigger.getExecutionPolicy()) {
			
			case REPEATING:
				return new RepeatingTimeBasedTriggerLocator(trigger, xDataset, yDataset);
			
			case SINGLE:
				return new SingleTimeBasedTriggerLocator(trigger, xDataset, yDataset);
			
			default:
				throw new IllegalArgumentException("Unsupported execution policy: " + trigger.getExecutionPolicy());
			}
			
		default:
			throw new IllegalArgumentException("Unsupported signal source: " + trigger.getSignalSource());
		
		}
		
	}

}
