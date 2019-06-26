package uk.ac.diamond.daq.client.gui.camera.energy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnergyController {
	public enum EnergySelectionType {
		pinkBeamOnly, monoBeamOnly, pinkAndMonoBeams
	}
	
	public enum EnergyType {
		mono, pink
	}
	
	private EnergySelectionType energySelectionType;
	private EnergyType energyType;
	
	private List<DiscreteEnergy> discreteEnergies = new ArrayList<>();
	private double monoEnergy;
	private DiscreteEnergy discreteEnergy;
	
	private List<EnergyControllerListener> listeners = new ArrayList<>();
	
	public EnergyController (EnergySelectionType energySelectionType) {
		this.energySelectionType = energySelectionType;
		energyType = EnergyType.mono;
		
		monoEnergy = 10.0;
		
		DiscreteEnergy lowEnergy = new DiscreteEnergy(1.2, 2.9, "low");
		discreteEnergy = lowEnergy;
		
		discreteEnergies.add(lowEnergy);
		discreteEnergies.add(new DiscreteEnergy(2.5, 3.7, "Mid"));
		discreteEnergies.add(new DiscreteEnergy(3.1, 5.6, "first"));
	}
	
	public void addListener(EnergyControllerListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(EnergyControllerListener listener) {
		listeners.remove(listener);
	}
	
	public EnergySelectionType getEnergySelectionType() {
		return energySelectionType;
	}
	
	public EnergyType getEnergyType() {
		return energyType;
	}
	
	public List<DiscreteEnergy> getDiscreteEnergies() {
		return Collections.unmodifiableList(discreteEnergies);
	}

	public double getMonoEnergy() {
		return monoEnergy;
	}
	
	public void setMonoEnergy(double monoEnergy) {
		energyType = EnergyType.mono;
		this.monoEnergy = monoEnergy;
		notifyListeners();
	}
	
	public DiscreteEnergy getDiscreteEnergy() {
		return discreteEnergy;
	}

	public void setDiscreteEnergy(DiscreteEnergy discreteEnergy) {
		energyType = EnergyType.pink;
		this.discreteEnergy = discreteEnergy;
		notifyListeners();
	}
	
	public void notifyListeners () {
		for (EnergyControllerListener listener : listeners) {
			if (energyType == EnergyType.mono) {
				listener.setMonoEnergy(monoEnergy);
			}
			if (energyType == EnergyType.pink) {
				listener.setDiscreteEnergy(discreteEnergy);
			}
		}
	}
}
