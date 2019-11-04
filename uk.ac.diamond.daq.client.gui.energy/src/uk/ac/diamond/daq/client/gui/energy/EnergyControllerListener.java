package uk.ac.diamond.daq.client.gui.energy;

public interface EnergyControllerListener {

	void operationStarted();

	void progressMade(String message, double percentage);

	void operationFinished();

	void operationFailed(String message);

}
