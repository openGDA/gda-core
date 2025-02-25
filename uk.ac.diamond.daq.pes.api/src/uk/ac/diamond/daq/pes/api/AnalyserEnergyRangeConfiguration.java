/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.pes.api;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
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

public class AnalyserEnergyRangeConfiguration implements Serializable {
	/**
	 * Random serial UID
	 */
	private static final long serialVersionUID = -6629590341931839963L;

	/**
	 * A map containing the EnergyRange objects. It is indexed by PSU mode, lens mode, then pass energy
	 */
	private final Map<String, Map<String, Map<Integer, List<EnergyRange>>>> energyRangeMap;

	public AnalyserEnergyRangeConfiguration(Map<String, Map<String, Map<Integer, List<EnergyRange>>>> capabilitiesMap) {
		this.energyRangeMap = capabilitiesMap;
	}

	/**
	 * Provide a list of files to read in to add to the map containing the EnergyRange object indexed by
	 * PSU mode, lens mode, then pass energy. An example file is below:
	 * High Pass (XPS)		1		2		5		10		20		50		100		200
	 * Transmission			none	none	1-160	2-320	5-640	12-1407	25-1305	63-955
	 * Angular14			none	none	2-190	5-381	10-761	25-1234	50-1467	265-1345
	 * Angular7NF			none	none	3-190	12-761	12-761	30-369	59-216	171-193
	 * Angular30			none	none	2-113	4-226	7-453	18-800	35-1037	110-951
	 * Angular30_SmallSpot	none	none	1-40	1-80	1-160	3-248	3-248	15-48, 144-196
	 * Angular14_SmallSpot	none	none	1-48	2-95	2-190	6-182	12-149	37-53
	 */
	public AnalyserEnergyRangeConfiguration(final List<String> files) throws IOException {
		final Map<String, Map<String, Map<Integer, List<EnergyRange>>>> psuModeToLensMode = new HashMap<>();
		for (final String filePath : files) {
			final String[] readlines = Files.readAllLines(Paths.get(filePath)).stream()
				.map(l -> l.trim().replaceAll("[\\t]+", "\t"))
				.filter(l -> !l.startsWith("#"))
				.toArray(String[]::new);
			final Map<String, Map<Integer, List<EnergyRange>>> lensModeToPassEnergy = new HashMap<>();
			final String SPLIT_VALUE = "\t";
			final String[] passEnergies = readlines[0].split(SPLIT_VALUE);
			final String psuMode = passEnergies[0];
			for (int i = 1; i < readlines.length; i++) {
				final String[] row = readlines[i].split(SPLIT_VALUE);
				final String lensMode = row[0];
				final HashMap<Integer, List<EnergyRange>> passEnergyToEnergyRange = new HashMap<>();
				for (int j = 1; j < row.length; j++) {
					final Integer passEnergy = Integer.valueOf(passEnergies[j]);
					final String energyRangeStr = row[j];
					final List<EnergyRange> energyRanges = extractEnergyRangesFromString(energyRangeStr);
					passEnergyToEnergyRange.put(passEnergy, energyRanges);
				}
				lensModeToPassEnergy.put(lensMode, passEnergyToEnergyRange);
			}
			psuModeToLensMode.put(psuMode, lensModeToPassEnergy);
		}
		this.energyRangeMap = psuModeToLensMode;
	}

	private List<EnergyRange> extractEnergyRangesFromString(String energyRangesString) {
		final String[] rangesArray = energyRangesString.split(",");
		final List<EnergyRange> energyRanges = new ArrayList<>();
		for (String ranges : rangesArray) {
			final String[] limits = ranges.split("-");
			final EnergyRange energyRange = ranges.equals("none") || ranges.equals("-") ? null : new EnergyRange(Double.valueOf(limits[0]), Double.valueOf(limits[1]));
			if (energyRange != null) {
				energyRanges.add(energyRange);
			}
		}
		return energyRanges;
	}

	public AnalyserEnergyRangeConfiguration(final String file) throws IOException {
		this(Arrays.asList(file));
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
		final Set<String> lensModes = new LinkedHashSet<>();
		for (Map<String, Map<Integer, List<EnergyRange>>> lensModeMap : energyRangeMap.values()) {
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
		final Set<Integer> passEnergies = new TreeSet<>();
		for (Map<String, Map<Integer, List<EnergyRange>>> lensModeMap : energyRangeMap.values()) {
			for (Map<Integer, List<EnergyRange>> passEnergyMap : lensModeMap.values()) {
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
	 * Gets the maximum valid kinetic energies (KE) for this PSU mode, lens mode, pass energy combination
	 *
	 * @param psuMode
	 *            Power supply mode
	 * @param lensMode
	 *            Lens mode
	 * @param passEnergy
	 *            Pass energy
	 * @return The minimum valid KEs for this PSU mode, lens mode, pass energy combination
	 * @throws IllegalArgumentException
	 *             If the psuMode, lensMode or passEnergy are invalid
	 */
	public List<Double> getMaxKEs(String psuMode, String lensMode, int passEnergy) {
		checkPsuModeLensModeAndPassEnergyValid(psuMode, lensMode, passEnergy);
		final List<EnergyRange> energyRanges = energyRangeMap.get(psuMode).get(lensMode).get(passEnergy);
		final List<Double> maxKEList = new ArrayList<>();
		for (final EnergyRange energyRange : energyRanges) {
			maxKEList.add(energyRange == null ? null : energyRange.getMaxKE());
		}
		return maxKEList;
	}

	/**
	 * Gets the minimum valid kinetic energies (KE) for this PSU mode, lens mode, pass energy combination
	 *
	 * @param psuMode
	 *            Power supply mode
	 * @param lensMode
	 *            Lens mode
	 * @param passEnergy
	 *            Pass energy
	 * @return The minimum valid KEs for this PSU mode, lens mode, pass energy combination
	 * @throws IllegalArgumentException
	 *             If the psuMode, lensMode or passEnergy are invalid
	 */
	public List<Double> getMinKEs(String psuMode, String lensMode, int passEnergy) {
		checkPsuModeLensModeAndPassEnergyValid(psuMode, lensMode, passEnergy);
		final List<EnergyRange> energyRanges = energyRangeMap.get(psuMode).get(lensMode).get(passEnergy);
		final List<Double> minKEList = new ArrayList<>();
		for (final EnergyRange energyRange : energyRanges) {
			minKEList.add(energyRange == null ? null : energyRange.getMinKE());
		}
		return minKEList;
	}

	/**
	 * Gets the {@link EnergyRange} list which contain the min and max valid kinetic energy values at this PSU mode, lens mode, pass energy combination
	 *
	 * @param psuMode
	 *            Power supply mode
	 * @param lensMode
	 *            Lens mode
	 * @param passEnergy
	 *            Pass energy
	 * @return The list of {@link EnergyRange} for his PSU mode, lens mode, pass energy combination
	 */
	public List<EnergyRange> getEnergyRanges(String psuMode, String lensMode, int passEnergy) {
		checkPsuModeLensModeAndPassEnergyValid(psuMode, lensMode, passEnergy);
		return new ArrayList<>(energyRangeMap.get(psuMode).get(lensMode).get(passEnergy));
	}

	/**
	 * Gets the {@link EnergyRange} list which contain the min and max valid binding energy values at this PSU mode, lens mode, pass energy combination
	 *
	 * @param psuMode
	 *            Power supply mode
	 * @param lensMode
	 *            Lens mode
	 * @param passEnergy
	 *            Pass energy
	 * @return The list of {@link EnergyRange} in binding energy for his PSU mode, lens mode, pass energy combination
	 */
	public List<EnergyRange> getEnergyRangesAsBindingEnergy(String psuMode, String lensMode, int passEnergy, double excitationEnergy) {
		checkPsuModeLensModeAndPassEnergyValid(psuMode, lensMode, passEnergy);
		final List<EnergyRange> keEnergyRanges = energyRangeMap.get(psuMode).get(lensMode).get(passEnergy);
		Collections.reverse(keEnergyRanges);
		return keEnergyRanges.stream().map(
			energyRange -> new EnergyRange(
				energyRange.getMinKE(),
				energyRange.getMaxKE(),
				excitationEnergy
			)
		).toList();
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

		Map<String, Map<Integer, List<EnergyRange>>> lensModeMap = energyRangeMap.get(psuMode);
		Map<Integer, List<EnergyRange>> passEnergyMap = lensModeMap.get(lensMode);

		// Use TreeSet ensure ordering
		return new TreeSet<>(passEnergyMap.keySet());
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
		final List<EnergyRange> energyRanges = getEnergyRanges(psuMode, lensMode, passEnergy);
		for (final EnergyRange energyRange : energyRanges) {
			final double minKE = energyRange.getMinKE();
			final double maxKE = energyRange.getMaxKE();
			if (energy >= minKE && energy <= maxKE) {
				return true;
			}
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
		AnalyserEnergyRangeConfiguration other = (AnalyserEnergyRangeConfiguration) obj;
		if (energyRangeMap == null) {
			if (other.energyRangeMap != null)
				return false;
		} else if (!energyRangeMap.equals(other.energyRangeMap))
			return false;
		return true;
	}
}
