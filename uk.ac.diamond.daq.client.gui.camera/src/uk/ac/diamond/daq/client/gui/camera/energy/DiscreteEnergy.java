package uk.ac.diamond.daq.client.gui.camera.energy;

public class DiscreteEnergy {
	private double low;
	private double high;
	private String code;
	
	public DiscreteEnergy(double low, double high, String code) {
		super();
		this.low = low;
		this.high = high;
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public double getLow() {
		return low;
	}
	
	public double getHigh() {
		return high;
	}
	
	@Override
	public String toString() {
		return String.format("%4.1fkeV - %4.1fkeV", low, high);
	}
}
