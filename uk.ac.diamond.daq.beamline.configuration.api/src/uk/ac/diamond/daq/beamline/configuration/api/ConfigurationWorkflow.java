package uk.ac.diamond.daq.beamline.configuration.api;

import java.util.Properties;

import gda.factory.Findable;

public interface ConfigurationWorkflow extends Findable {

	void start(Properties properties) throws WorkflowException;

	void abort() throws WorkflowException;

	boolean isRunning();
}
