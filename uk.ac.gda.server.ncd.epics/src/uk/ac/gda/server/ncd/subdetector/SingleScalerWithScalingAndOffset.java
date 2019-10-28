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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.DeviceException;
import gda.device.currentamplifier.ScalingAndOffset;
import gda.device.detector.NXDetectorData;
import gda.jython.InterfaceProvider;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * A class to represent a detector for NCD.
 */
@ServiceInterface(INcdSubDetector.class)
public class SingleScalerWithScalingAndOffset extends NcdScalerDetector implements IHaveExtraNames {

	private static final Logger logger = LoggerFactory.getLogger(SingleScalerWithScalingAndOffset.class);

	private boolean wasFixed = true;
	private String label;
	private String units = "counts";
	private int channel;
	private ScalingAndOffset scalingAndOffset;

	@Override
	public void writeout(int frames, NXDetectorData nxdata) throws DeviceException {
		float[] data;
		try {
			data = readFloat(channel, 0, 0, 1, 1, frames);
		} catch (DeviceException de) {
			logger.warn("Could not read scalar data, defaulting to [1...1]", de);
			InterfaceProvider.getTerminalPrinter().print(
					String.format(
							"ERROR reading from scalars for %s, defaulting to [1...1]",
							getName()
					)
			);
			data = new float[frames];
			Arrays.fill(data, 1.0f);
		}
		NexusGroupData ngd;
		String desc = scalingAndOffset.getDescription();

		double sum = 0;
		for (float i: data) {
			sum += i;
		}
		ngd = new NexusGroupData(data);
		ngd.isDetectorEntryData = true;
		addMonitorData(nxdata, getName(), label, ngd, units, 1, desc, null);

		nxdata.setPlottableValue(getName(), sum);
		ngd = new NexusGroupData(sum);
		ngd.isDetectorEntryData = true;
		addMonitorData(nxdata, getName(), "framesetsum", ngd, units, null, null, "ncddetectors."+getName());
		//FIXME hardcoding of ncddetectors
		//FIXME add frame axis
	}

	/**
	 * Adds the specified data to the named detector
	 * @param monitorName The name of the detector to add data to
	 * @param data_sds The implementation of NexusGroupData to be reported as the data
	 * @param units  - if not null a units attribute is added
	 * @param signalVal - if not null a signal attribute is added
	 */
	public static void addMonitorData(NXDetectorData nxdata, String monitorName, String dataName, NexusGroupData data_sds, String units, Integer signalVal, String description, String local_name) {
		INexusTree monTree = getMonitorTree(nxdata, monitorName);
		NexusTreeNode data = new NexusTreeNode(dataName, NexusExtractor.SDSClassName, null, data_sds);
		data.setIsPointDependent(data_sds.isDetectorEntryData);
		if (units != null) {
			data.addChildNode(new NexusTreeNode("units",NexusExtractor.AttrClassName, data, new NexusGroupData(units)));
		}
		if (signalVal != null) {
			data.addChildNode(new NexusTreeNode("signal",NexusExtractor.AttrClassName, data,
					new NexusGroupData(signalVal)));
		}
		if (local_name != null) {
			data.addChildNode(new NexusTreeNode("local_name",NexusExtractor.AttrClassName, data,
					new NexusGroupData(local_name)));
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

	private double applyOffsetAndScaling(double value) {
		return value*scalingAndOffset.getScaling() + scalingAndOffset.getOffset();
	}
	
	private float[] applyOffsetAndScaling(float[] values) {
		for (int i = 0; i < values.length; i++) {
			values[i] = (float) applyOffsetAndScaling(values[i]);
		}
		return values;
	}
	private double[] applyOffsetAndScaling(double[] values) {
		for (int i = 0; i < values.length; i++) {
			values[i] = applyOffsetAndScaling(values[i]);
		}
		return values;
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

	@Override
	public float[] readFloat(int frame) throws DeviceException {
		float[] data = super.readFloat(channel,0,0,1,1,frame);
		logger.trace("{} - Raw data before scaling: {}", getName(), data);
		return applyOffsetAndScaling(data);
	}
	@Override
	public double[] read(int frame) throws DeviceException {
		double[] data = super.read(channel,0,0,1,1,frame);
		logger.trace("{} - Raw data before scaling: {}", getName(), data);
		return applyOffsetAndScaling(data);
	}
	@Override
	public float[] readFloat(int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		float[] data = super.readFloat(x, y, t, dx, dy, dt);
		logger.trace("{} - Raw data before scaling: {}", getName(), data);
		return applyOffsetAndScaling(data);
	}
	@Override
	public double[] read(int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		double[] data = super.read(x, y, t, dx, dy, dt);
		logger.trace("{} - Raw data before scaling: {}", getName(), data);
		return applyOffsetAndScaling(data);
	}
}