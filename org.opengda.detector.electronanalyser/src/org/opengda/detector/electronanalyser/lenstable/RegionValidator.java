package org.opengda.detector.electronanalyser.lenstable;

import gda.factory.Findable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Table;

/**
 * validate a given region against analyser's energy range for specified element set.
 * object of this class requires a map of element set name to its corresponding look up table file.
 * @author fy65
 *
 */
public class RegionValidator implements Findable {
	private static final Logger logger=LoggerFactory.getLogger(RegionValidator.class);
	private Map<String, String> lookupTablePathMap=new HashMap<String, String>();
	private String name;
	private String energyrange;
	
	public RegionValidator() {
	}
	/**
	 * Check if a given region is valid or not for a given element set.
	 * The region's excitation energy is used to convert binding energy to kinetic energy before performing validation.
	 * @param region
	 * @param elementset
	 * @return boolean true or false
	 */
	public boolean isValidRegion(Region region, String elementset) {
		return isValidRegion(region, elementset, region.getExcitationEnergy());
	}
	/**
	 * check if the given region is valid or not for the given element_set and required excitation energy.
	 * @param elementset
	 * @param region
	 * @return
	 */
	public boolean isValidRegion(Region region, String elementset, double excitationEnergy) {
		com.google.common.collect.Table<String, String, String> lookupTable = getLookupTable(elementset);
		if (lookupTable==null) {
			logger.warn("Analyser Kinetic energy range lookup table for '{}' element set is not available.",elementset);
			return true;
		}
		energyrange=lookupTable.get(region.getLensMode(), String.valueOf(region.getPassEnergy()));
		List<String> limits=Splitter.on("-").splitToList(energyrange);
		if (region.getEnergyMode()==ENERGY_MODE.KINETIC) {
			if (!(region.getLowEnergy()>=Double.parseDouble(limits.get(0)) && region.getHighEnergy()<=Double.parseDouble(limits.get(1)))) {
				return false;
			}
		} else {
			double startEnergy=excitationEnergy-region.getHighEnergy();
			double endEnergy=excitationEnergy-region.getLowEnergy();
			if (startEnergy<endEnergy) {
				if (!(startEnergy>=Double.parseDouble(limits.get(0)) && endEnergy<=Double.parseDouble(limits.get(1)))) {
					logger.error("Start energy = {}, End energy = {}, at excitation energy = {}", startEnergy, endEnergy, excitationEnergy);
					return false;
				}
			} else {
				if (!(endEnergy>=Double.parseDouble(limits.get(0)) && startEnergy<=Double.parseDouble(limits.get(1)))) {
					logger.error("Start energy = {}, End energy = {}, at excitation energy = {}", startEnergy, endEnergy, excitationEnergy);
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * create an energy range look up table for specified element set.
	 * @param elementset
	 * @return Table<String, String, String> of <LensMode, Pass_Energy, Energy_range>
	 */
	public Table<String, String, String> getLookupTable(String elementset) {
		String tablePath=lookupTablePathMap.get(elementset);
		if (tablePath==null || tablePath.isEmpty()) {
			logger.error("Lookup table for Element Set '{}' is not specified.",elementset);
			throw new IllegalStateException("Lookup table for Element Set '"+elementset+"' is not specified.");
		}
		File file = new File(tablePath);
		if (!file.exists()) {
			logger.error("Cannot find the lookup table : {} ",tablePath);
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
	@Override
	public void setName(String name) {
		this.name=name;		
	}
	@Override
	public String getName() {
		return name;
	}
	public String getEnergyrange() {
		return energyrange;
	}
}
