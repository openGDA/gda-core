package org.opengda.detector.electronanalyser.lenstable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Table;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.FindableBase;

/**
 * Validate a given region against analyser's energy range for specified element set.
 * This class of objects requires a map of element set name to its
 * corresponding look up table file.
 *
 * @author fy65
 */
public class RegionValidator extends FindableBase {
	private static final Logger logger = LoggerFactory.getLogger(RegionValidator.class);
	private Map<String, String> lookupTablePathMap = new HashMap<>();
	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private Scannable pgmEnergy;
	private Scannable dcmEnergy;
	private boolean offlineValidation;
	private String errorMessage;

	/**
	 * Check if a given region is valid or not for a given element set. The region's excitation energy is used to convert binding energy to kinetic energy
	 * before performing validation.
	 *
	 * @param region
	 * @param elementset
	 * @return boolean true or false
	 */
	public boolean isValidRegion(Region region, String elementset) {
		try {
			// For running validation from creator perspective just use cached excitation energy
			final double currentExcitationEnergy;
			if (offlineValidation) {
				currentExcitationEnergy = region.getExcitationEnergy();
				return isValidRegion(region, elementset, currentExcitationEnergy);
			}
			// Need to decide if we want to validate against the hard or soft beam energy
			// Get the current beam energies to avoid caching issues
			if (regionDefinitionResourceUtil.isSourceSelectable()) {
				if (region.getExcitationEnergy() > regionDefinitionResourceUtil.getXRaySourceEnergyLimit()) {
					// Hard
					currentExcitationEnergy = (double) dcmEnergy.getPosition() * 1000; // keV -> eV;
				} else {
					// Soft
					currentExcitationEnergy = (double) pgmEnergy.getPosition();
				}
			} else {
				// Assume hard
				currentExcitationEnergy = (double) dcmEnergy.getPosition();
			}

			return isValidRegion(region, elementset, currentExcitationEnergy);
		} catch (DeviceException e) {
			logger.error("Failed to get photon energy to validate region. Assuming region is valid", e);
			return true; // Fail by allowing region
		}
	}

	/**
	 * check if the given region is valid or not for the given element_set and required excitation energy.
	 *
	 * @param elementset
	 * @param region
	 * @return
	 */
	private boolean isValidRegion(Region region, String elementset, double excitationEnergy) {
		logger.debug("About to validate with element set: {} and photon energy: {}", elementset, excitationEnergy);
		final String energyrange = getEnergyRange(region, elementset);
		// FIXME Could have invalid string here if lookupTable was null in getEnergyRange
		final List<String> limits = Splitter.on("-").splitToList(energyrange);
		final double lowerKeLimit = Double.parseDouble(limits.get(0));
		final double upperKeLimit = Double.parseDouble(limits.get(1));
		logger.debug("For lens mode = {} and pass energy = {} Limits are {} to {}", region.getLensMode(), region.getPassEnergy(), lowerKeLimit, upperKeLimit);

		boolean valid = true;
		errorMessage = "";

		if (region.getEnergyMode() == ENERGY_MODE.KINETIC) {
			if (!(region.getLowEnergy() >= lowerKeLimit && region.getHighEnergy() <= upperKeLimit)) {
				logger.error("Start energy = {}, End energy = {}", region.getLowEnergy(), region.getHighEnergy());

				errorMessage = "Region '" + region.getName() + "' has a kinetic energy range of " + region.getLowEnergy() + " to " + region.getHighEnergy()
								+ " which is outside the energy range (" + energyrange + ") permitted for Element Set: '"
								+ elementset + "', Pass Energy: '" + region.getPassEnergy() + "' and Lens Mode: '" + region.getLensMode() + "'.";

				valid = false;
			}
		} else {
			double startEnergy = excitationEnergy - region.getHighEnergy();
			double endEnergy = excitationEnergy - region.getLowEnergy();

			if (startEnergy < endEnergy) {
				if (!(startEnergy >= lowerKeLimit && endEnergy <= upperKeLimit)) {
					logger.error("Start energy = {}, End energy = {}, at excitation energy = {}", startEnergy, endEnergy, excitationEnergy);
					valid = false;
				}
			} else {
				if (!(endEnergy >= lowerKeLimit && startEnergy <= upperKeLimit)) {
					logger.error("Start energy = {}, End energy = {}, at excitation energy = {}", startEnergy, endEnergy, excitationEnergy);
					valid = false;
				}
			}

			if (!valid) {
				double lowEnergy = Double.parseDouble(energyrange.split("-")[1]);
				double highEnergy = Double.parseDouble(energyrange.split("-")[0]);
				errorMessage = "Region '" + region.getName() + "' has a binding energy range of " + (region.getLowEnergy()) + " to " + (region.getHighEnergy())
						+ " which is  outside the energy range (" + Double.toString(region.getExcitationEnergy() - lowEnergy) + "-" + Double.toString(region.getExcitationEnergy() - highEnergy) + ") permitted for Element Set: '"
						+ elementset + "', Pass Energy: '" + region.getPassEnergy() + "' and Lens Mode: '" + region.getLensMode() + "'.";
			}
		}
		return valid;
	}

	public String getEnergyRange(Region region, String elementset) {
		Table<String, String, String> lookupTable = getLookupTable(elementset);
		if (lookupTable == null) {
			logger.warn("Analyser Kinetic energy range lookup table for '{}' element set is not available.", elementset);
			return "No lookup table";
		}
		final String energyrange = lookupTable.get(region.getLensMode(), String.valueOf(region.getPassEnergy()));
		return energyrange;
	}

	/**
	 * create an energy range look up table for specified element set.
	 *
	 * @param elementset
	 * @return Table<String, String, String> of <LensMode, Pass_Energy, Energy_range>
	 */
	public Table<String, String, String> getLookupTable(String elementset) {
		String tablePath = lookupTablePathMap.get(elementset);
		if (tablePath == null || tablePath.isEmpty()) {
			logger.error("Lookup table for Element Set '{}' is not specified.", elementset);
			throw new IllegalStateException("Lookup table for Element Set '" + elementset + "' is not specified.");
		}
		File file = new File(tablePath);
		if (!file.exists()) {
			logger.error("Cannot find the lookup table : {} ", tablePath);
			throw new IllegalStateException("Cannot find the lookup table : " + tablePath);
		} else {
			return new TwoDimensionalLookupTable().createTable(file);
		}
	}

	public Map<String, String> getLookupTablePathMap() {
		return lookupTablePathMap;
	}

	public void setLookupTablePathMap(Map<String, String> lookupTablePathMap) {
		this.lookupTablePathMap = lookupTablePathMap;
	}

	public RegionDefinitionResourceUtil getRegionDefinitionResourceUtil() {
		return regionDefinitionResourceUtil;
	}

	public void setRegionDefinitionResourceUtil(RegionDefinitionResourceUtil regionDefinitionResourceUtil) {
		this.regionDefinitionResourceUtil = regionDefinitionResourceUtil;
	}

	public Scannable getPgmEnergy() {
		return pgmEnergy;
	}

	public void setPgmEnergy(Scannable pgmEnergy) {
		this.pgmEnergy = pgmEnergy;
	}

	public Scannable getDcmEnergy() {
		return dcmEnergy;
	}

	public void setDcmEnergy(Scannable dcmEnergy) {
		this.dcmEnergy = dcmEnergy;
	}

	public void setOfflineValidation(boolean offlineValidation) {
		this.offlineValidation = offlineValidation;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

}
