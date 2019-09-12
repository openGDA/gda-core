package uk.ac.diamond.daq.beamline.configuration.api;

import java.util.Properties;

/**
 * WorkflowItems are sequential blocks in a {@link ConfigurationWorkflow}.
 */
public interface WorkflowItem {

	void start(Properties workflowProperties) throws WorkflowException;

	void abort() throws WorkflowException;

}