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


import gda.observable.Observable;
import gda.observable.Observer;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;

// TODO: Fails to get the corba PlotServer and instead creates an RMI one. Other views have a CORBA service injected via
// AnalysisRCPActivator.start(). Works if one of these views update their bean first

/**
 * On Observer<Map<GuiParameters, Serializable>> which updates a named {@link GuiBean} on the PlotServer.
 * 
 * @author zrb13439
 */
public class PlotServerGuiBeanUpdater implements Observer<Map<GuiParameters, Serializable>> {

	private static final Logger logger = LoggerFactory.getLogger(PlotServerGuiBeanUpdater.class);

	private final String guiName;

	/**
	 * Create the PlotServerGuiBeanUpdater for the named gui.
	 * 
	 * @param guiName
	 */
	public PlotServerGuiBeanUpdater(String guiName) {
		this.guiName = guiName;
		//We need to activate the SciSoftRCP bundle as that sets up the PlotServer
		AnalysisRCPActivator.getDefault();
	}

	/**
	 * Update existing GuiBean or create one to update if required.
	 */
	@Override
	public void update(Observable<Map<GuiParameters, Serializable>> source,
			Map<GuiParameters, Serializable> parameterMap) {

		PlotServer plotServer = PlotServerProvider.getPlotServer();
		logger.debug("'" + guiName + "' updating PlotServer: " + plotServer);
		try {
			GuiBean guiBean = plotServer.getGuiState(guiName);
			if (guiBean == null) {
				guiBean = new GuiBean();
			}
			guiBean.putAll(parameterMap);
			plotServer.updateGui(guiName, guiBean);
		} catch (Exception e) {
			throw new RuntimeException(MessageFormat.format(
					"Could not update the GUI state bean for ''{0}'' from plot server: ''{1}''", guiName, plotServer),
					e);
		}
	}

}
