package com.opengda.detector.electronalyser.server.api;

import java.util.ArrayList;
import java.util.List;

import org.opengda.detector.electroanalyser.api.Region;
import org.opengda.detector.electroanalyser.api.RegionDefinition;

import com.opengda.detector.electronalyser.server.model.regiondefinition.resource.RegionDefinitionResourceUtil;

public class RegionDefinitionImpl implements RegionDefinition {

	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;

	public void setRegionDefinitionResourceUtil(
			RegionDefinitionResourceUtil regionDefinitionResourceUtil) {
		this.regionDefinitionResourceUtil = regionDefinitionResourceUtil;
	}

	@Override
	public List<Region> getRegions() {
		List<Region> regionsToReturn = new ArrayList<Region>();
		if (regionDefinitionResourceUtil != null) {
			List<com.opengda.detector.electronalyser.server.model.regiondefinition.Region> regions = regionDefinitionResourceUtil
					.getRegions(false);

			for (com.opengda.detector.electronalyser.server.model.regiondefinition.Region region : regions) {
				regionsToReturn.add(new RegionImpl(region.getName(), region
						.getLensmode()));
			}

		}
		return regionsToReturn;
	}

}
