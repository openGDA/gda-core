/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;

/**
 * Command launched from plot toolbar to centre plot around stage values
 * keeping the current zoom level
 */
public class FindBeamInPlot extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(FindBeamInPlot.class);
	private static MappingStageInfo mappingStageInfo;
	private static PlottingController plotter;

	public static void setMappingStageInfo(MappingStageInfo mappingStageInfo) {
		FindBeamInPlot.mappingStageInfo = mappingStageInfo;
	}

	public static void setPlottingController(PlottingController plotter) {
		FindBeamInPlot.plotter = plotter;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		centrePlotAroundBeam();
		return null;
	}

	private void centrePlotAroundBeam() {
		Scannable xAxis = Finder.getInstance().find(mappingStageInfo.getActiveFastScanAxis());
		Scannable yAxis = Finder.getInstance().find(mappingStageInfo.getActiveSlowScanAxis());
		try {
			plotter.centrePlotAroundPoint((double) xAxis.getPosition(), (double) yAxis.getPosition());
		} catch (DeviceException de) {
			logger.error("Could not read axes positions",de);
		}
	}


}
