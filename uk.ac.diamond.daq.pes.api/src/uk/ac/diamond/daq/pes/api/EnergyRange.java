package uk.ac.diamond.daq.pes.api;

import java.io.Serializable;
import java.util.Objects;

/**
 * Class representing the energy range for one PSU mode, lens mode and pass energy combination.
 * <p>
 */
public class EnergyRange implements Serializable {

	private static final long serialVersionUID = -3902556661078340600L;

	private final double minKE;
	private final double maxKE;
	private Double excitationEnergy = null;

	public EnergyRange(double minKE, double maxKE) {
		this.minKE = minKE;
		this.maxKE = maxKE;
	}

	public EnergyRange(double minKE, double maxKE, Double excitationEnergy) {
		this(minKE, maxKE);
		this.excitationEnergy = excitationEnergy;
	}

	public boolean isBindingEnergy() {
		return excitationEnergy != null;
	}

	public Double getExcitationEnergy() {
		return excitationEnergy;
	}

	/**
	 * Helper function to the max energy and automatically convert to binding energy if excitationEnergy is not null
	 * @return energy
	 */
	public double getMaxEnergy() {
		if (excitationEnergy != null) {
			return excitationEnergy - minKE;
		}
		return maxKE;
	}

	/**
	 * Helper function to the min energy and automatically convert to binding energy if excitationEnergy is not null
	 * @return energy
	 */
	public double getMinEnergy() {
		if (excitationEnergy != null) {
			return excitationEnergy - maxKE;
		}
		return minKE;
	}

	public double getMaxKE() {
		return maxKE;
	}

	public double getMinKE() {
		return minKE;
	}

	@Override
	public int hashCode() {
		return Objects.hash(excitationEnergy, maxKE, minKE);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnergyRange other = (EnergyRange) obj;
		return Objects.equals(excitationEnergy, other.excitationEnergy)
				&& Double.doubleToLongBits(maxKE) == Double.doubleToLongBits(other.maxKE)
				&& Double.doubleToLongBits(minKE) == Double.doubleToLongBits(other.minKE);
	}

	@Override
	public String toString() {
		return "EnergyRange [minKE=" + minKE + ", maxKE=" + maxKE + (excitationEnergy != null ? ", excitationEnergy=" + excitationEnergy : "") + "]";
	}

	public String toFormattedString() {
		return String.format("%.4f-%.4f", getMinEnergy(), getMaxEnergy());
	}
}