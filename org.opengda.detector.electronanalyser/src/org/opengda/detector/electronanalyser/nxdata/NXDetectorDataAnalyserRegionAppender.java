package org.opengda.detector.electronanalyser.nxdata;

import gda.data.nexus.tree.INexusTree;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link NXDetectorDataAppender} for collecting data from VGScienta Electron analyser.
 * It is compatible with the standard GDA scan {@link NexusDataWriter} and supports data collections over
 * multiple analyser regions of different data sizes and produce single nexus data file per scan.
 * 
 * It also provides plots of total intensity count over the scanned scannable, i.e. excitation energies for energy scan. 
 * 
 * @author fy65
 * 
 */
public class NXDetectorDataAnalyserRegionAppender implements NXDetectorDataAppender {
	// region name
	private List<INexusTree> regionDataList;
	private Logger logger = LoggerFactory.getLogger(NXDetectorDataAnalyserRegionAppender.class);
	private List<Double> totalIntensity=new ArrayList<Double>();

	public NXDetectorDataAnalyserRegionAppender(List<INexusTree> regionDataList, List<Double> totalIntensity) {
		this.regionDataList = regionDataList;
		this.totalIntensity = totalIntensity;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName)
			throws DeviceException {
		synchronized (regionDataList) {
			for (int i = 0; i < regionDataList.size(); i++) {
				INexusTree regiontree=regionDataList.get(i);
				for (INexusTree regionchild : regiontree) {
					logger.debug("add {} to region node {} in detector tree.",regionchild.getName(), regiontree.getName());
					data.addData(regiontree.getName(), regionchild.getName(), regionchild.getData());
				}
				String regionName = regiontree.getName();
				Double regionIntensity = totalIntensity.get(i);
				data.setPlottableValue(regionName, regionIntensity);
			}
		}
	}
}
