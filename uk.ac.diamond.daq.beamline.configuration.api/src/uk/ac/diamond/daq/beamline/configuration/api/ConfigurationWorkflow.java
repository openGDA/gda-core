package uk.ac.diamond.daq.beamline.configuration.api;

import java.util.Properties;

import gda.factory.Findable;
import gda.observable.IObservable;

public interface ConfigurationWorkflow extends Findable, IObservable {

	void start(Properties properties);

	void abort();

	WorkflowUpdate getState();
}
