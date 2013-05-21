package org.opengda.detector.electronanalyser.scan;

import gda.scan.ScanPositionProvider;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionPositionProvider implements ScanPositionProvider {
	List<Region> points=new ArrayList<Region>();
	RegionDefinitionResourceUtil regionResourceutil=new RegionDefinitionResourceUtil();
	private static final Logger logger=LoggerFactory.getLogger(RegionPositionProvider.class);


	public RegionPositionProvider(String filename) {
		//String filepath = FilenameUtils.separatorsToUnix(filename);

		logger.debug("Sequence file changed to {}{}", FilenameUtils.getFullPath(filename), FilenameUtils.getName(filename));
		try {
			List<Region> regions=regionResourceutil.getRegions(filename);
			for (Region region : regions) {
				if (region.isEnabled()) { // only add selected/enabled region to the list of points to collect
					this.points.add(region);
				}
			}
		} catch (Exception e) {
			logger.error("Cannot get region list from file.", e);
		}
	}
	@Override
	public Object get(int index) {
		return points.get(index);
	}
	
	@Override
	public int size() {
		return points.size();
	}
}
