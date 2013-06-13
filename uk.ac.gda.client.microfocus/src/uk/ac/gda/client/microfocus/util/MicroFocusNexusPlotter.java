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

package uk.ac.gda.client.microfocus.util;

import gda.analysis.RCPPlotter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class MicroFocusNexusPlotter {
	private static final Logger logger = LoggerFactory.getLogger(MicroFocusNexusPlotter.class);
	private MicroFocusMappableDataProvider dataProvider;
	public MicroFocusMappableDataProvider getDataProvider() {
		return dataProvider;
	}

	public void setDataProvider(MicroFocusMappableDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	private String plottingWindowName;

	public String getPlottingWindowName() {
		return plottingWindowName;
	}

	public void setPlottingWindowName(String plottingWindowName) {
		this.plottingWindowName = plottingWindowName;
	}

	@SuppressWarnings("static-access")
	public void plotElement(String elementName) {
		dataProvider.setSelectedElement(elementName);
		double[][] mapData = dataProvider.constructMappableData();
		AbstractDataset plotSet = AbstractDataset.array(mapData);
		try {
			RCPPlotter.imagePlot(plottingWindowName, plotSet);
		} catch (Exception e) {
			logger.error("Error plotting the dataset in MicroFocusNexusPlotter", e);
		}
		
	}
	
	public void plotDataset(AbstractDataset dataset) {
		try {
			RCPPlotter.imagePlot(plottingWindowName, dataset);
		} catch (Exception e) {
			logger.error("Error plotting the dataset in MicroFocusNexusPlotter", e);
		}
		
	}

}
