package uk.ac.diamond.daq.client.gui.camera.energy;

public interface EnergyControllerListener {
	void setDiscreteEnergy(DiscreteEnergy discreteEnergy);
	
	void setMonoEnergy(double energy);
}
