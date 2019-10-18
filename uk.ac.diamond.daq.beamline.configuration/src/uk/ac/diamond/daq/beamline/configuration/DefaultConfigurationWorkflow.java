package uk.ac.diamond.daq.beamline.configuration;

import java.util.List;
import java.util.Properties;

import uk.ac.diamond.daq.beamline.configuration.api.ConfigurationWorkflow;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowItem;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(ConfigurationWorkflow.class)
public class DefaultConfigurationWorkflow implements ConfigurationWorkflow {

	private String name;
	private List<WorkflowItem> workflowItems;

	private volatile boolean running;
	private volatile boolean aborting;

	public DefaultConfigurationWorkflow(List<WorkflowItem> workflowItems) {
		this.workflowItems = workflowItems;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void start(Properties workflowProperties) throws WorkflowException {
		try {
			running = true;
			for (WorkflowItem workflowItem : workflowItems) {
				checkForAbort();
				workflowItem.start(workflowProperties);
				checkForAbort();
			}
		} finally {
			running = false;
		}
	}

	private void checkForAbort() throws WorkflowException {
		if (aborting) {
			aborting = false;
			throw new WorkflowException("Stopped by user");
		}
	}

	@Override
	public void abort() throws WorkflowException {
		aborting = true;
		for (WorkflowItem workflowItem : workflowItems) {
			workflowItem.abort();
		}
	}

	@Override
	public boolean isRunning() {
		return running;
	}
}
