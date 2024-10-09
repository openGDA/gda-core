/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;

public class I18SubmitScanSection  extends SubmitScanSection {
	private static final Logger logger = LoggerFactory.getLogger(I18SubmitScanSection.class);

	private String fastAxisName = "t1x";
	private String slowAxisName = "t1theta";
	private String[] axes = {};

	@Override
	protected void onShow() {
		var mappingStage = getMappingStageInfo();

		// save the names of the axes currently being used
		axes = new String[] {mappingStage.getPlotXAxisName(), mappingStage.getPlotYAxisName()};
		logger.debug("Saving current axes names : {}", Arrays.asList(axes));

		// set the ones to be used for the scan
		mappingStage.setPlotXAxisName(fastAxisName);
		mappingStage.setPlotYAxisName(slowAxisName);

		// update the 'region and path' section to show the selected axes
		updateRegionAndPathSection();
	}

	@Override
	protected void onHide() {
		if (axes.length == 2) {
			// restore the original axes names in the 'region and path' section
			logger.debug("Restoring axes names : {}", Arrays.asList(axes));
			var mappingStage = getMappingStageInfo();
			mappingStage.setPlotXAxisName(axes[0]);
			mappingStage.setPlotYAxisName(axes[1]);
			updateRegionAndPathSection();
		}
	}

	private MappingStageInfo getMappingStageInfo() {
		return getService(MappingStageInfo.class);
	}

	private void updateRegionAndPathSection() {
		final MappingExperimentView mappingView = getView();
		final RegionAndPathSection regionAndPathSection = mappingView.getSection(RegionAndPathSection.class);
		regionAndPathSection.updateControls();
	}

	public void setSlowAxisName(String slowAxisName) {
		this.slowAxisName = slowAxisName;
	}

	public void setFastAxisName(String fastAxisName) {
		this.fastAxisName = fastAxisName;
	}
}
