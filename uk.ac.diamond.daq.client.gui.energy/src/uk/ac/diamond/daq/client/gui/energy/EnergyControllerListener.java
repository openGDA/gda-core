package uk.ac.diamond.daq.client.gui.energy;

public interface EnergyControllerListener {

	void workflowStarted();

	void workflowFinished();

	void workflowFailed(String message);

}
