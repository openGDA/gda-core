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

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to represent a detector for NCD.
 */
public class SingleScalerWithScalingAndOffset extends NcdScalerDetector {

	private static final Logger logger = LoggerFactory.getLogger(SingleScalerWithScalingAndOffset.class);

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
		for (int frame=0; frame<frames; frame++) {
			tweakeddata[frame] = (float) (data[frame] * scaling + offset);
		}
		
		ngd = new NexusGroupData(datadims, NexusFile.NX_FLOAT32, tweakeddata);
		ngd.isDetectorEntryData = true;
		nxdata.addData(getName(), label, ngd, units, 1);
		scalingAndOffset.getDescription(); // FIXME this should go somewhere
		//FIXME add frame axis
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
}