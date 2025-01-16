package org.opengda.detector.electronanalyser.lenstable;

import org.opengda.detector.electronanalyser.api.SESRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.pes.api.AnalyserEnergyRangeConfiguration;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Validate a given region against analyser's energy range for specified element set.
 * This class of objects requires a map of element set name to its
 * corresponding look up table file.
 *
 * @author fy65
 */
@ServiceInterface(IRegionValidator.class)
public class RegionValidator implements IRegionValidator {
	private static final Logger logger = LoggerFactory.getLogger(RegionValidator.class);
	private AnalyserEnergyRangeConfiguration energyRange;
	private String errorMessage;
	private String name;

	/**
	 * Check if the given region is valid or not for the given element_set and required excitation energy (in eV).
	 *
	 * @param elementSet
	 * @param region
	 * @param excitationEnergy
	 * @return true if region is value, false otherwise.
	 */
	@Override
	public boolean isValidRegion(SESRegion region, String elementSet, double excitationEnergy) {
		errorMessage = "";
		boolean valid = false;
		String message = "";
		try {
			final Double lowerKeLimit = getMinKE(elementSet, region);
			final Double upperKeLimit = getMaxKE(elementSet, region);
			if (lowerKeLimit == null || upperKeLimit == null) {
				message = generateMessage(region, elementSet, excitationEnergy, valid);
			} else {
				final boolean isEnergyModeKinetic = region.isEnergyModeKinetic();
				final double regionStartEnergy = isEnergyModeKinetic ? region.getLowEnergy() : excitationEnergy - region.getHighEnergy();
				final double regionEndEnergy = isEnergyModeKinetic ? region.getHighEnergy() : excitationEnergy - region.getLowEnergy();
				valid = getEnergyRange().isKEValid(elementSet, region.getLensMode(), region.getPassEnergy(), regionStartEnergy);
				valid = valid && getEnergyRange().isKEValid(elementSet, region.getLensMode(), region.getPassEnergy(), regionEndEnergy);
				message = generateMessage(region, elementSet, excitationEnergy, regionStartEnergy, regionEndEnergy, valid);
			}
			if(valid) logger.info(message); else logger.warn(message);
		} catch (IllegalArgumentException e) {
			valid = false;
			errorMessage = e.getLocalizedMessage();
                        logger.warn(errorMessage);
		}
		return valid;
	}

	@Override
	public Double getMinKE(String elementSet, SESRegion region) {
		return getEnergyRange().getMinKE(elementSet, region.getLensMode(), region.getPassEnergy());
	}

	@Override
	public Double getMaxKE(String elementSet, SESRegion region) {
		return getEnergyRange().getMaxKE(elementSet, region.getLensMode(), region.getPassEnergy());
	}

	@Override
	public Double getMinBindingEnergy(String elementSet, SESRegion region, double excitationEnergy) {
		final Double maxKE = getEnergyRange().getMaxKE(elementSet, region.getLensMode(), region.getPassEnergy());
		if (maxKE == null) return maxKE;
		return excitationEnergy - getEnergyRange().getMaxKE(elementSet, region.getLensMode(), region.getPassEnergy());
	}

	@Override
	public Double getMaxBindingEnergy(String elementSet, SESRegion region, double excitationEnergy) {
		final Double minKE = getEnergyRange().getMinKE(elementSet, region.getLensMode(), region.getPassEnergy());
		if (minKE == null) return minKE;
		return excitationEnergy - getEnergyRange().getMinKE(elementSet, region.getLensMode(), region.getPassEnergy());
	}

	private String generateMessage(SESRegion region, String elementSet, double excitationEnergy, boolean valid) {
		return generateMessage(region, elementSet, excitationEnergy, null, null, valid);
	}

	private String generateMessage(SESRegion region, String elementSet, double excitationEnergy, Double regionStartEnergy, Double regionEndEnergy, boolean valid) {
		final boolean hasEnergyRange = regionStartEnergy != null && regionEndEnergy != null;
		final boolean isEnergyModeKinetic = region.isEnergyModeKinetic();
		final boolean isEnergyModeBinding = region.isEnergyModeBinding();
		final String excitationEnergyString = isEnergyModeKinetic ? "" : String.format("Excitation energy: %.4f eV,", excitationEnergy);
		final String energyMode = isEnergyModeKinetic ? "kinetic" : "binding";
		String energyRangeString = "energy range is 'none'";
		if (hasEnergyRange) {
			final Double minEnergy = isEnergyModeBinding ? getMinBindingEnergy(elementSet, region, excitationEnergy) : getMinKE(elementSet, region);
			final Double maxEnergy = isEnergyModeBinding ? getMaxBindingEnergy(elementSet, region, excitationEnergy) : getMaxKE(elementSet, region);
			energyRangeString = String.format("energy range (%.0f-%.0f)", minEnergy, maxEnergy);
		}
		if (valid) {
			energyRangeString = "which is valid. The " + energyRangeString + " at ";
		} else if (hasEnergyRange){
			energyRangeString = "which is outside the " + energyRangeString + " permitted for ";
		} else {
			energyRangeString = ". The " + energyRangeString + " for ";
		}
		final String message = String.format(
			"'%s' has a %s energy range of %.4f to %.4f %s Element Set: '%s', %s Pass Energy: '%d', and Lens Mode: '%s'.",
			region.getName(), energyMode, region.getLowEnergy(), region.getHighEnergy(), energyRangeString, elementSet, excitationEnergyString, region.getPassEnergy(), region.getLensMode()
		);
		if(!valid) {
			errorMessage = message;
		}
		return message;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	public AnalyserEnergyRangeConfiguration getEnergyRange() {
		return energyRange;
	}

	public void setEnergyRange(AnalyserEnergyRangeConfiguration energyRange) {
		this.energyRange = energyRange;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

}