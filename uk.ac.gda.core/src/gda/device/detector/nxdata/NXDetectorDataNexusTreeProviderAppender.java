/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.nxdata;

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.detector.NXDetectorData;

/**
 * Loosely based on the NXDetectorDataChildNodeAppender, but adding the ability to copy over plottable values if the
 * tree provider is an NXDetectorData object.
 */
public class NXDetectorDataNexusTreeProviderAppender implements NXDetectorDataAppender {

	private final NexusTreeProvider treeProvider;

	public NXDetectorDataNexusTreeProviderAppender(NexusTreeProvider treeProvider) {
		this.treeProvider = treeProvider;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) {
		INexusTree detTree = data.getDetTree(detectorName);
		INexusTree treeToAppend = treeProvider.getNexusTree();
		detTree.addChildNode(treeToAppend);

		if (treeProvider instanceof NXDetectorData) {
			String[] extraNames = ((NXDetectorData) treeProvider).getExtraNames();
			Double[] plottableValues = ((NXDetectorData) treeProvider).getDoubleVals();
			for (int index = 0; index < extraNames.length && index < plottableValues.length; index++) {
				data.setPlottableValue(extraNames[index], plottableValues[index]);
			}
		}
	}
}
