/*-
 * Copyright © 2016 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.devices.vgscienta;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A class used to provide the valid kinetic energy (KE) range to stay inside the lens table. It has one nested map which contains all of the data. The map is
 * indexed by PSU mode, lens mode, then pass energy. If this is a allowed combination there will be a EnergyRange object there which holds the min and max KE
 * for that operating mode.
 * <p>
 * This class is intended to be instantiated by Spring and there is a utility Python script <i>VGScientaEnergyRangeParser.py</i> which can take the output from
 * SES (Calibration -> Voltages -> View -> Energy Range -> Copy To Clipboard) and auto generate the Spring XML configuration file, as this can be large.
 * <p>
 * The intension is to make the class immutable, after instantiation by Spring it shouldn't be possible to change the internal state!
 *
 * @author James Mudd
 */
public class VGScientaAnalyserEnergyRange implements Serializable {

	/**
	 * Class representing the energy range for one PSU mode, lens mode and pass energy combination.
	 * <p>
	 * Instances of this type shouldn't be leaked from the enclosing class.
	 */
	public static class EnergyRange implements Serializable {

		/**
		 * Random serial UID
		 */
		private static final long serialVersionUID = -3902556661078340600L;

		private final double minimumKineticEnergy;
		private final double maximumKineticEnergy;

		public EnergyRange(double minKE, double maxKE) {
			this.minimumKineticEnergy = minKE;
			this.maximumKineticEnergy = maxKE;
		}

		public double getMaxKE() {
			return maximumKineticEnergy;
		}

		public double getMinKE() {
			return minimumKineticEnergy;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(maximumKineticEnergy);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(minimumKineticEnergy);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
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
			if (Double.doubleToLongBits(maximumKineticEnergy) != Double.doubleToLongBits(other.maximumKineticEnergy))
				return false;
			if (Double.doubleToLongBits(minimumKineticEnergy) != Double.doubleToLongBits(other.minimumKineticEnergy))
				return false;
			return true;
		}

	}

	/**
	 * Random serial UID
	 */
	private static final long serialVersionUID = -6629590341931839963L;

	/**
	 * A map containing the EnergyRange objects. It is indexed by PSU mode, lens mode, then pass energy
	 */
	private final Map<String, Map<String, Map<Integer, EnergyRange>>> energyRangeMap;

	public VGScientaAnalyserEnergyRange(Map<String, Map<String, Map<Integer, EnergyRange>>> capabilitiesMap) {
		this.energyRangeMap = capabilitiesMap;
	}

	private void checkPsuModeAndLensModeValid(String psuMode, String lensMode) throws IllegalArgumentException {
		checkPsuModeValid(psuMode);
		if (!getLensModes(psuMode).contains(lensMode)) {
			throw new IllegalArgumentException("The lens mode: '" + lensMode + "' is not valid");
		}
	}

	private void checkPsuModeLensModeAndPassEnergyValid(String psuMode, String lensMode, int passEnergy) throws IllegalArgumentException {
		checkPsuModeAndLensModeValid(psuMode, lensMode);
		if (!getPassEnergies(psuMode, lensMode).contains(passEnergy)) {
			throw new IllegalArgumentException("The pass energy: '" + passEnergy + "' is not valid");
		}
	}

	private void checkPsuModeValid(String psuMode) throws IllegalArgumentException {
		if (!getAllPsuModes().contains(psuMode)) {
			throw new IllegalArgumentException("The PSU mode: '" + psuMode + "' is not valid");
		}
	}

	/**
	 * This looks at all of the PSU modes available and returns a copy the lens modes offered by any of them.
	 *
	 * @return All lens modes in the map
	 */
	public Set<String> getAllLensModes() {
		Set<String> lensModes = new LinkedHashSet<>();
		for (Map<String, Map<Integer, EnergyRange>> lensModeMap : energyRangeMap.values()) {
			lensModes.addAll(lensModeMap.keySet());
		}
		return lensModes;
	}

	/**
	 * This looks across all PSU modes and lens modes and returns a ordered set of all know pass energies.
	 *
	 * @return All pass energies in the map
	 */
	public Set<Integer> getAllPassEnergies() {
		// Use TreeSet to enforce ordering
		Set<Integer> passEnergies = new TreeSet<>();
		for (Map<String, Map<Integer, EnergyRange>> lensModeMap : energyRangeMap.values()) {
			for (Map<Integer, EnergyRange> passEnergyMap : lensModeMap.values()) {
				passEnergies.addAll(passEnergyMap.keySet());
			}
		}
		return passEnergies;
	}

	/**
	 * This returns a copy of the lens modes offered by the supplied PSU mode.
	 *
	 * @return Lens modes in this PSU mode
	 */
	public Set<String> getLensModes(String psuMode) {
		checkPsuModeValid(psuMode);
		return Collections.unmodifiableSet(energyRangeMap.get(psuMode).keySet());
	}

	/**
	 * Gets the maximum valid kinetic energy (KE) for this PSU mode, lens mode, pass energy combination
	 *
	 * @param psuMode
	 *            Power supply mode
	 * @param lensMode
	 *            Lens mode
	 * @param passEnergy
	 *            Pass energy
	 * @return The minimum valid KE for this PSU mode, lens mode, pass energy combination
	 * @throws IllegalArgumentException
	 *             If the psuMode, lensMode or passEnergy are invalid
	 */
	public double getMaxKE(String psuMode, String lensMode, int passEnergy) {
		checkPsuModeLensModeAndPassEnergyValid(psuMode, lensMode, passEnergy);
		return energyRangeMap.get(psuMode).get(lensMode).get(passEnergy).getMaxKE();
	}

	/**
	 * Gets the minimum valid kinetic energy (KE) for this PSU mode, lens mode, pass energy combination
	 *
	 * @param psuMode
	 *            Power supply mode
	 * @param lensMode
	 *            Lens mode
	 * @param passEnergy
	 *            Pass energy
	 * @return The minimum valid KE for this PSU mode, lens mode, pass energy combination
	 * @throws IllegalArgumentException
	 *             If the psuMode, lensMode or passEnergy are invalid
	 */
	public double getMinKE(String psuMode, String lensMode, int passEnergy) {
		checkPsuModeLensModeAndPassEnergyValid(psuMode, lensMode, passEnergy);
		return energyRangeMap.get(psuMode).get(lensMode).get(passEnergy).getMinKE();
	}

	/**
	 * Gets the valid pass energies for this PSU mode, lens mode combination, will be ordered ascending.
	 *
	 * @param psuMode
	 *            Power supply mode
	 * @param lensMode
	 *            Lens mode
	 * @return The valid pass energies for this PSU mode, lens mode combination
	 * @throws IllegalArgumentException
	 *             If the psuMode or lensMode are invalid
	 */
	public Set<Integer> getPassEnergies(String psuMode, String lensMode) {
		checkPsuModeAndLensModeValid(psuMode, lensMode);

		Map<String, Map<Integer, EnergyRange>> lensModeMap = energyRangeMap.get(psuMode);
		Map<Integer, EnergyRange> passEnergyMap = lensModeMap.get(lensMode);

		// Use TreeSet ensure ordering
		return new TreeSet<Integer>(passEnergyMap.keySet());
	}

	/**
	 * Gets all the power supply (PSU) modes. Returns a copy.
	 *
	 * @return All PSU Modes
	 */
	public Set<String> getAllPsuModes() {
		return Collections.unmodifiableSet(energyRangeMap.keySet());
	}

	/**
	 * Checks if a kinetic energy is valid (inside the energy range) with the supplied PSU mode, lens mode, pass energy combination.
	 *
	 * @param psuMode
	 *            Power supply mode
	 * @param lensMode
	 *            Lens mode
	 * @param passEnergy
	 *            Pass energy
	 * @param energy
	 *            Kinetic energy to check
	 * @return True if the energy is valid, false otherwise
	 * @throws IllegalArgumentException
	 *             If the psuMode, lensMode or passEnergy are invalid
	 */
	public boolean isKEValid(String psuMode, String lensMode, int passEnergy, double energy) {
		checkPsuModeLensModeAndPassEnergyValid(psuMode, lensMode, passEnergy);
		EnergyRange energyRange = energyRangeMap.get(psuMode).get(lensMode).get(passEnergy);
		if (energy >= energyRange.getMinKE() && energy <= energyRange.getMaxKE()) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((energyRangeMap == null) ? 0 : energyRangeMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VGScientaAnalyserEnergyRange other = (VGScientaAnalyserEnergyRange) obj;
		if (energyRangeMap == null) {
			if (other.energyRangeMap != null)
				return false;
		} else if (!energyRangeMap.equals(other.energyRangeMap))
			return false;
		return true;
	}

}
