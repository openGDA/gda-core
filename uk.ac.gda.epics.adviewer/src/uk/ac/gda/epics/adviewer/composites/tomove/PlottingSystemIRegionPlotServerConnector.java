/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer.composites.tomove;

import org.dawnsci.plotting.api.IPlottingSystem;

public class PlottingSystemIRegionPlotServerConnector {

	private RegionGuiParameterAdapter regionParameterObservable;
	private PlotServerGuiBeanUpdater plotServerGuiBeanUpdater;

	public PlottingSystemIRegionPlotServerConnector(IPlottingSystem plottingSystem, String guiName) {

		regionParameterObservable = new RegionGuiParameterAdapter(plottingSystem);

		plotServerGuiBeanUpdater = new PlotServerGuiBeanUpdater(guiName);
		
		try {
			regionParameterObservable.addObserver(plotServerGuiBeanUpdater);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		regionParameterObservable.fireCurrentRegionList();	
	}

	public void disconnect() {
		if( regionParameterObservable != null && plotServerGuiBeanUpdater!= null)
			regionParameterObservable.removeObserver(plotServerGuiBeanUpdater);
		regionParameterObservable = null;
		plotServerGuiBeanUpdater = null;
	}
	

}
