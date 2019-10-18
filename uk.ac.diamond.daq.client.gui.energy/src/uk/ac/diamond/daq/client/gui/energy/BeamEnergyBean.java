package uk.ac.diamond.daq.client.gui.energy;

public class BeamEnergyBean {

	private EnergyType type = EnergyType.MONOCHROMATIC;
	private double monoEnergy;
	private EnergyBand polyEnergy;

	public EnergyType getType() {
		return type;
	}

	public void setType(EnergyType type) {
		this.type = type;
	}

	public double getMonoEnergy() {
		return monoEnergy;
	}

	public void setMonoEnergy(double monoEnergy) {
		this.monoEnergy = monoEnergy;
	}

	public EnergyBand getPolyEnergy() {
		return polyEnergy;
	}

	public void setPolyEnergy(EnergyBand polyEnergy) {
		this.polyEnergy = polyEnergy;
	}

}
