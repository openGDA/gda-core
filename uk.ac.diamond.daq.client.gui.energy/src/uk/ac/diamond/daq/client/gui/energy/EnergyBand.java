package uk.ac.diamond.daq.client.gui.energy;

public class EnergyBand {

	private String label;
	private double low;
	private double high;

	public EnergyBand(String label, double low, double high) {

		this.label = label;
		this.low = low;
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public double getHigh() {
		return high;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public String toString() {
		return "EnergyBand [label=" + label + ", low=" + low + ", high=" + high + "]";
	}
}
