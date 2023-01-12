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

package uk.ac.gda.excalibur.config.model.util;

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.detector.NXDetectorData;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.excalibur.ExcaliburNodeWrapper.ReadoutNodeWrapper;
import uk.ac.gda.devices.excalibur.ExcaliburSummaryAdbase;
import uk.ac.gda.excalibur.config.model.ExcaliburConfig;
import uk.ac.gda.excalibur.config.model.SummaryNode;

/**
 * Provides the excalibur configuration information in a nexus tree.
 */
public class ExcaliburConfigNxsTreeProvider implements NexusTreeProvider {

	private static final Logger logger = LoggerFactory.getLogger(ExcaliburConfigNxsTreeProvider.class);

	private String detectorName;

	private List<ReadoutNodeWrapper> readoutNodeWrappers;

	private ExcaliburSummaryAdbase excaliburSummaryAdbase;

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	@Override
	public INexusTree getNexusTree() {
		INexusTree nexusTree = null;
		try {
			if (readoutNodeWrappers != null) {
				ExcaliburConfig excaliburConfig = ExcaliburConfigModelHelper.INSTANCE
						.createExcaliburConfig(readoutNodeWrappers);

				if (excaliburSummaryAdbase != null) {
					SummaryNode summaryAdBaseModel = ExcaliburConfigModelHelper.INSTANCE
							.createSummaryAdBaseModel(excaliburSummaryAdbase);

					excaliburConfig.setSummaryNode(summaryAdBaseModel);
				}

				NXDetectorData detectorData = NexusTreeHelper.INSTANCE
						.saveToDetectorData(detectorName, excaliburConfig);
				nexusTree = detectorData.getNexusTree();
			}
		} catch (Exception e) {
			logger.error("Problem creating NexusTree", e);
		}
		return nexusTree;
	}

	public void setReadoutNodeWrappers(List<ReadoutNodeWrapper> readoutNodeWrappers) {
		this.readoutNodeWrappers = readoutNodeWrappers;
	}

	public void setExcaliburSummaryAdbase(ExcaliburSummaryAdbase excaliburSummaryAdbase) {
		this.excaliburSummaryAdbase = excaliburSummaryAdbase;
	}
}