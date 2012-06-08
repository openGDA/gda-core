/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.excalibur;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.device.detector.GDANexusDetectorData;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nexusprocessor.DataSetProcessorBase;

import java.util.Collection;

import org.nexusformat.NexusFile;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class ExcaliburNDArraySimulatorParameterReader extends DataSetProcessorBase implements InitializingBean {
	ExcaliburNDArraySimulator sim;

	public ExcaliburNDArraySimulator getSim() {
		return sim;
	}

	public void setSim(ExcaliburNDArraySimulator sim) {
		this.sim = sim;
	}

	@Override
	public GDANexusDetectorData process(String detectorName, String dataName, AbstractDataset dataset) throws Exception {
		NXDetectorData res = new NXDetectorData();
		INexusTree detTree = res.getDetTree(detectorName);
		int[] dims = new int[] { sim.widthUsed, sim.heightUsed };
		// construct NexusGroupData explicitly to ensure isDetectorEntryData and isPointDependent are false
		// as this is calibration data
		detTree.addChildNode(new NexusTreeNode(dataName + ".heights", NexusExtractor.SDSClassName, null, new NexusGroupData(dims,
				NexusFile.NX_FLOAT64, sim.heights)));
		detTree.addChildNode(new NexusTreeNode(dataName + ".centres", NexusExtractor.SDSClassName, null, new NexusGroupData(dims,
				NexusFile.NX_FLOAT64, sim.centres)));
		detTree.addChildNode(new NexusTreeNode(dataName + ".widths", NexusExtractor.SDSClassName, null, new NexusGroupData(dims,
				NexusFile.NX_FLOAT64, sim.widths)));

		return res;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if( sim == null)
			throw new IllegalArgumentException("sim is null");

	}

	@Override
	protected Collection<String> _getExtraNames() {
		return null;
	}

	@Override
	protected Collection<String> _getOutputFormat() {
		return null;
	}

}