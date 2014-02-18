/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.subdetector;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.DeviceException;
import gda.device.currentamplifier.ScalingAndOffset;
import gda.device.detector.NXDetectorData;

import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to represent a detector for NCD.
 */
public class SingleScalerWithScalingAndOffset extends NcdScalerDetector implements IHaveExtraNames {

	private static final Logger logger = LoggerFactory.getLogger(SingleScalerWithScalingAndOffset.class);

	private boolean wasFixed = true;
	private String label;
	private String units = "counts";
	private int channel;
	private ScalingAndOffset scalingAndOffset;
	
	@Override
	public void writeout(int frames, NXDetectorData nxdata) throws DeviceException {
		int[] datadims = new int[] {frames };
		float[] data = readFloat(channel, 0, 0, 1, 1, frames);
		NexusGroupData ngd;

		float[] tweakeddata = new float[frames];
		Double offset = scalingAndOffset.getOffset();
		Double scaling = scalingAndOffset.getScaling();
		
		double sum = 0;
		for (int frame=0; frame<frames; frame++) {
			tweakeddata[frame] = (float) (data[frame] * scaling + offset);
			sum += tweakeddata[frame];
		}
		
		nxdata.setPlottableValue(getName(), sum);
		
		ngd = new NexusGroupData(datadims, NexusFile.NX_FLOAT32, tweakeddata);
		ngd.isDetectorEntryData = true;
		addMonitorData(nxdata, getName(), label, ngd, units, 1,	scalingAndOffset.getDescription());
		
		//FIXME add frame axis
	}

	/**
	 * Adds the specified data to the named detector
	 * @param monitorName The name of the detector to add data to
	 * @param data_sds The implementation of NexusGroupData to be reported as the data
	 * @param units  - if not null a units attribute is added
	 * @param signalVal - if not null a signal attribute is added
	 */
	public static void addMonitorData(NXDetectorData nxdata, String monitorName, String dataName, NexusGroupData data_sds, String units, Integer signalVal, String description) {
		INexusTree monTree = getMonitorTree(nxdata, monitorName);
		NexusTreeNode data = new NexusTreeNode(dataName, NexusExtractor.SDSClassName, null, data_sds);
		data.setIsPointDependent(data_sds.isDetectorEntryData);
		if (units != null) {
			data.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, data, new NexusGroupData(units)));
		}
		if (signalVal != null) {
			Integer[] signalValArray = {signalVal};
			data.addChildNode(new NexusTreeNode("signal",NexusExtractor.AttrClassName, data, 
					new NexusGroupData(new int[] {signalValArray.length}, NexusFile.NX_INT32, signalValArray)));
		}
		if (description != null) {
			monTree.addChildNode(new NexusTreeNode("description",NexusExtractor.SDSClassName, monTree, 
					new NexusGroupData(description)));
		}
		monTree.addChildNode(data);			
	}
	
	/**
	 * returns the names detectors tree
	 * @param monitorName if null or empty it returns the first 
	 * @return the NexusTree associated with the named detector
	 */
	public static INexusTree getMonitorTree(NXDetectorData nxdata, String monitorName) {
		for (INexusTree branch : nxdata.getNexusTree()) {
			if (branch.getNxClass().equals(NexusExtractor.NXMonitorClassName)) {
				if (branch.getName().equals(monitorName)){
					return branch;
				}
			}
		}
		//else add item and return
		NexusTreeNode detTree = new NexusTreeNode(monitorName, NexusExtractor.NXMonitorClassName, null);
		detTree.setIsPointDependent(true);
		nxdata.getNexusTree().addChildNode(detTree);
		return detTree;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public ScalingAndOffset getScalingAndOffset() {
		return scalingAndOffset;
	}

	public void setScalingAndOffset(ScalingAndOffset scalingAndOffset) {
		this.scalingAndOffset = scalingAndOffset;
	}
	
	@Override
	public void atScanStart() throws DeviceException {
		wasFixed = scalingAndOffset.isFixed();
		scalingAndOffset.setFixed(true);
		super.atScanStart();
	}
	
	@Override
	public void atScanEnd() throws DeviceException {
		scalingAndOffset.setFixed(wasFixed);
		super.atScanEnd();
	}
	
	@Override
	public void stop() throws DeviceException {
		scalingAndOffset.setFixed(wasFixed);
		super.stop();
	}

	@Override
	public String[] getExtraNames() {
		return new String[] {getName()};
	}
}