/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client;

import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;

public class PlotServerImpl implements PlotServer {
	private static PlotServer delegate;
	static {
		System.out.println("Starting GDA PlotServer service.");
	}	
	
	
	private PlotServer getDelegate(){
		if( delegate == null){
			delegate = Finder.getInstance().find("plot_server");
			if( delegate == null)
				throw new RuntimeException("Unable to find plot_server. Has the connection to the server been established!");
		}
		return delegate;
		
	}
	public PlotServerImpl() {
		// Important do nothing here, OSGI may start the service more than once.
	}

	@Override
	public void updateGui(String guiName, GuiBean guiData) throws Exception {
		getDelegate().updateGui(guiName, guiData);
	}

	@Override
	public void addIObserver(IObserver observer) {
		getDelegate().addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		getDelegate().deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		getDelegate().deleteIObservers();
	}

	@Override
	public void setData(String guiName, DataBean plotData) throws Exception {
		getDelegate().setData(guiName, plotData);
	}

	@Override
	public void updateData(String guiName) throws Exception {
		getDelegate().updateData(guiName);
	}

	@Override
	public GuiBean getGuiState(String guiName) throws Exception {
		return getDelegate().getGuiState(guiName);
	}

	@Override
	public boolean isServerLocal() throws Exception {
		return getDelegate().isServerLocal();
	}

	@Override
	public String[] getGuiNames() throws Exception {
		return getDelegate().getGuiNames();
	}

	@Override
	public DataBean getData(String guiName) throws Exception {
		return getDelegate().getData(guiName);
	}
	
}
