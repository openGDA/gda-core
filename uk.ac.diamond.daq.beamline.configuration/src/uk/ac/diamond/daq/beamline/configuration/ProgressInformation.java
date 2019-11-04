package uk.ac.diamond.daq.beamline.configuration;

import uk.ac.diamond.daq.beamline.configuration.api.ConfigurationWorkflow;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowItem;

/**
 * Each instance of this class should be associated with a {@link WorkflowItem}
 * so that the {@link ConfigurationWorkflow} can broadcast user-friendly messages
 * regarding its progress
 */
public class ProgressInformation {

	private final String description;
	private final double percentage;

	/**
	 * @param description What is happening while the associated item is running?
	 * @param percentage What should the running percentage of the workflow be once the associated item is complete?
	 */
	public ProgressInformation(String description, double percentage) {
		this.description = description;
		this.percentage = percentage;
	}

	public String getDescription() {
		return description;
	}

	public double getPercentage() {
		return percentage;
	}

}
