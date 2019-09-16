/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package uk.ac.diamond.scisoft.analysis.plotserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerDevice;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * This is the fundamental class for all the PlotServer functionality, it is basically a store of all the GUI specific
 * information so that they are synchronised across all the servers, as well as containing the plot information which
 * should be displayed on all the GUI windows.
 */
@ServiceInterface(PlotServerDevice.class)
public class PlotServerBase extends DeviceBase implements PlotServerDevice {

	private static final Logger logger = LoggerFactory.getLogger(PlotServerBase.class);

	PlotServer plotServerImpl;

	{
		try {
			plotServerImpl = new RMIPlotServer();

		} catch (Exception e) {
			logger.warn("Not creating RMI PlotServer. May be disabled.", e);
			plotServerImpl = new GDASimplePlotServer();
		}
	}

	@Override
	public void configure() {
		PlotServerProvider.setPlotServer(this);
	}

	public static PlotServer getPlotServer() {
		return Finder.getInstance().find("plot_server");
	}

	@Override
	public DataBean getData(String guiName) throws Exception {
		return plotServerImpl.getData(guiName);
	}

	@Override
	public GuiBean getGuiState(String guiName) throws Exception {
		return plotServerImpl.getGuiState(guiName);
	}

	@Override
	public void setData(String guiName, DataBean data) throws Exception {
		plotServerImpl.setData(guiName, data);
	}

	@Override
	public void updateData(String guiName) throws Exception {
		plotServerImpl.updateData(guiName);
	}

	@Override
	public void updateGui(String guiName, GuiBean guiData) throws Exception {
		plotServerImpl.updateGui(guiName, guiData);
	}

	@Override
	public void addIObserver(IObserver observer) {
		plotServerImpl.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		plotServerImpl.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		plotServerImpl.deleteIObservers();
	}

	@Override
	public boolean isServerLocal() throws Exception {
		return plotServerImpl.isServerLocal();
	}

	@Override
	public String[] getGuiNames() throws Exception {
		return plotServerImpl.getGuiNames();
	}

	@Override
	public String toString() {
		return "PlotServerBase wrapping " + plotServerImpl.toString();
	}
}
