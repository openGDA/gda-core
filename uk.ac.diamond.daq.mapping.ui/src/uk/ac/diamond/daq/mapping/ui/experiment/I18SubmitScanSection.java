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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;

public class I18SubmitScanSection  extends SubmitScanSection {
	private static final Logger logger = LoggerFactory.getLogger(I18SubmitScanSection.class);

	private String fastAxisName = "t1x";
	private String slowAxisName = "t1theta";
	private List<String> axes = Collections.emptyList();

	@Override
	protected void onShow() {
		var mappingStage = getMappingStageInfo();

		// save the names of the axes currently being used
		axes = Arrays.asList(mappingStage.getPlotXAxisName(), mappingStage.getPlotYAxisName());
		logger.debug("Saving current axes names : {}", Arrays.asList(axes));

		// set the ones to be used for the scan
		mappingStage.setPlotXAxisName(fastAxisName);
		mappingStage.setPlotYAxisName(slowAxisName);

		// update the 'region and path' section to show the selected axes
		updateRegionAndPathSection();
	}

	@Override
	protected void onHide() {
		if (!axes.isEmpty()) {
			restoreAxesNames();
			updateRegionAndPathSection();
		}
	}

	private MappingStageInfo getMappingStageInfo() {
		return getService(MappingStageInfo.class);
	}

	private void restoreAxesNames() {
		// restore the original axes names in the 'region and path' section
		if (axes.size() < 2) {
			logger.warn("Not restoring axis names. Stored axes is {}, but expected 2 values", Arrays.asList(axes));
			return;
		}
		logger.debug("Restoring axes names : {}", Arrays.asList(axes));
		var mappingStage = getMappingStageInfo();
		mappingStage.setPlotXAxisName(axes.get(0));
		mappingStage.setPlotYAxisName(axes.get(1));
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

	@Override
	public void saveState(Map<String, String> persistedState) {
		// Restore the originally selected mapping stage motors in MappingStageInfo
		restoreAxesNames();

		// Call RegionAndPathSection#saveState to update the persisted MappingStageInfo values
		final RegionAndPathSection regionAndPathSection = getView().getSection(RegionAndPathSection.class);
		regionAndPathSection.saveState(persistedState);
	}
}
