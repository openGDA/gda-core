/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.diamond.scisoft.analysis.rcp.views.plot;

import java.util.Collection;
import java.util.Map;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.jreality.core.AxisMode;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DAppearance;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DStyles;
import org.eclipse.dawnsci.plotting.api.jreality.util.PlotColorUtility;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;

/**
 * Class is extended by classes that require the ability to take a graph snap shot and put it into a static plot. Not
 * currently an interface as intention is to add some common methods and fields here.
 */
@Deprecated
public abstract class AbstractPlotView extends ViewPart implements PlotView {

	protected StackLayout stack;
	protected Composite stackComposite;
	protected Label lblNoDataMessage;
	protected IPlottingSystem<Composite> system;

	protected abstract String getYAxisName();

	protected abstract String getXAxisName();

	protected abstract String getGraphTitle();

	@Override
	public void init(IViewSite site) throws PartInitException {
		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			throw new PartInitException("Exception creating PlottingSystem", e);
		}
		super.init(site);
	}

	/**
	 * Use this after the first data is received to hide the default message and show the plotter.
	 */
	protected void showPlotter() {
		if (stack.topControl != system.getPlotComposite()) {
			stack.topControl = system.getPlotComposite();
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					stackComposite.layout();
				}
			});
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		stackComposite = new Composite(parent, SWT.NONE);

		stack = new StackLayout();
		stackComposite.setLayout(stack);

		system.createPlotPart(stackComposite, getTitle(), // Title also used as
				// unique id for plot.
				getViewSite().getActionBars(), PlotType.XY, this);
		system.setRescale(true);

		lblNoDataMessage = new Label(stackComposite, SWT.NONE);
		lblNoDataMessage.setText("No data to plot.");

		configurePlot(system);

		stack.topControl = lblNoDataMessage;

	}

	/**
	 * Optionally override if extra plot config needed.
	 * 
	 * @param system2
	 */
	public void configurePlot(@SuppressWarnings("unused") final IPlottingSystem system2) {
		// Does nothing
	}

	/**
	 * Create legends from DataSet Map
	 * 
	 * @param p
	 * @param sets
	 */
	public static void createMultipleLegend(final DataSetPlotter p, final Map<String, ? extends Dataset> sets) {
		int iplot = 1;
		for (String name : sets.keySet()) {
			final Plot1DAppearance app = new Plot1DAppearance(PlotColorUtility.getDefaultColour(iplot, name),
					Plot1DStyles.SOLID, 1, name);
			p.getColourTable().addEntryOnLegend(app);
			++iplot;
		}
	}

	/**
	 * Create legends from DataSet Map
	 * 
	 * @param p
	 * @param sets
	 */
	public static void createMultipleLegend(final DataSetPlotter p, final Collection<Dataset> sets) {
		p.getColourTable().clearLegend();
		int iplot = 1;
		for (IDataset set : sets) {
			final Plot1DAppearance app = new Plot1DAppearance(PlotColorUtility.getDefaultColour(iplot, set.getName()),
					Plot1DStyles.SOLID, 1, set.getName());
			p.getColourTable().addEntryOnLegend(app);
			++iplot;
		}
	}

	/**
	 * Optionally override to change xAxis mode.
	 * 
	 * @return mode.
	 */
	public AxisMode getXAxisMode() {
		return AxisMode.CUSTOM;
	}

	/**
	 * Optionally override to change yAxis mode.
	 * 
	 * @return mode.
	 */
	public AxisMode getYAxisMode() {
		return AxisMode.LINEAR;
	}

	@Override
	public void setFocus() {
//		system.setFocus();
	}

	@Override
	public void dispose() {
		if (system != null) {
			system.dispose();
		}
		super.dispose();
	}

	public IPlottingSystem getPlottingSystem() {
		return system;
	}
}
