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

package uk.ac.diamond.daq.devices.specs.phoibos.ui;

import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveDataUpdate;

public abstract class SpecsLivePlot extends ViewPart implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(SpecsLivePlot.class);

	protected ISpecsPhoibosAnalyser analyser;
	protected IPlottingSystem<Composite> plottingSystem;
	protected static IPlottingService plottingService;
	protected boolean displayInBindingEnergy;
	protected SpecsPhoibosLiveDataUpdate lastUpdate;

	protected IActionBars actionBars;

	public static synchronized void setPlottingService(IPlottingService plottingService) {
		SpecsLivePlot.plottingService = plottingService;
		logger.trace("IPlottingService injected: {}", plottingService);
	}

	@Override
	public void createPartControl(Composite parent) {

		List<ISpecsPhoibosAnalyser> analysers = Finder.getInstance()
				.listLocalFindablesOfType(ISpecsPhoibosAnalyser.class);
		if (analysers.size() != 1) {
			logger.error("No Analyser was found! (Or more than 1)");
			throw new RuntimeException("No Analyser was found! (Or more than 1)");
		}
		analyser = analysers.get(0);
		analyser.addIObserver(this);

		logger.debug("Now observing analyser for data");

		// Setup the basics of the plotting
		try {
			plottingSystem = plottingService.createPlottingSystem();
			actionBars = getViewSite().getActionBars();
			actionBars.getToolBarManager().add(new KeBeSwich());
			plottingSystem.setShowLegend(false);
		} catch (Exception e) {
			logger.error("Couldn't setup plotting system", e);
		}
	}

	@Override
	public void setFocus() {
		plottingSystem.setFocus();
	}

	@Override
	public void dispose() {
		plottingSystem.dispose();
		analyser.deleteIObserver(this);
		super.dispose();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		// This is messy but its how the plotting system tools work so have to implement it
		T object = plottingSystem.getAdapter(adapter);
		if(object != null) {
			return object;
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void update(Object source, Object arg) {
		logger.trace("Received update from analyser");

		if (arg instanceof SpecsPhoibosLiveDataUpdate) {
			updatePlot((SpecsPhoibosLiveDataUpdate) arg);
		}
	}

	abstract void updatePlot(SpecsPhoibosLiveDataUpdate update);

	private class KeBeSwich extends Action {

		@Override
		public void run() {
			logger.trace("KE/BE Pressed");
			displayInBindingEnergy = !displayInBindingEnergy;
			updatePlot(lastUpdate);

			// Perform re-scale to keep plot visible needs UI thread
			PlatformUI.getWorkbench().getDisplay().asyncExec(plottingSystem::autoscaleAxes);
		}

		@Override
		public String getText() {
			return "KE/BE";
		}

		@Override
		public boolean isChecked() {
			return displayInBindingEnergy;
		}
	}

}