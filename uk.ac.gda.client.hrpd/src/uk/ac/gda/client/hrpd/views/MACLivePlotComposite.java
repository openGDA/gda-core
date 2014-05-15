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

package uk.ac.gda.client.hrpd.views;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataArrayListener;

public class MACLivePlotComposite extends Composite {

	private IPlottingSystem plottingSystem;
	private String PLOT_TITLE = "MAC";
	private List<Pair<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> listeners=new ArrayList<Pair<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>>();
	private Logger logger = LoggerFactory.getLogger(MACLivePlotComposite.class);

	public MACLivePlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
		super(parent, style);
		this.setBackground(ColorConstants.white);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		this.setLayout(layout);

		Composite plotComposite = new Composite(this, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());

		plottingSystem = PlottingFactory.createPlottingSystem();
		plottingSystem.createPlotPart(plotComposite, "MAC", part instanceof IViewPart ? ((IViewPart) part)
				.getViewSite().getActionBars() : null, PlotType.XY, part);
		plottingSystem.setTitle(PLOT_TITLE);
		plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
		plottingSystem.getSelectedXAxis().setFormatPattern("###.###");
		plottingSystem.setShowLegend(false);
		plottingSystem.getSelectedXAxis().setRange(0.0, 150.0);
	}

	@Override
	public void dispose() {
		if (!plottingSystem.isDisposed()) {
			plottingSystem.clear();
		}
		for (Pair<EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> listener : listeners) {
			listener.getKey().dispose();
			listener.getValue().dispose();
		}
		super.dispose();
	}

	public void clearPlots() {
		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				plottingSystem.setTitle("");
				plottingSystem.reset();
			}
		});
	}
	// #TODO what triggers the plot update or replot?
	//#TODO resolve potential race condition in setPlotted(boolean) call?
	class LivePlotUpdater implements Runnable {

		@Override
		public void run() {
			if (!getDisplay().isDisposed()) {
				getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						boolean visible = MACLivePlotComposite.this.isVisible();
						if (visible) {
							for (Pair<EpicsDoubleDataArrayListener,EpicsDoubleDataArrayListener> listener : listeners) {
								EpicsDoubleDataArrayListener x = listener.getKey();
								EpicsDoubleDataArrayListener y = listener.getValue();
								if (!y.isPlotted()) {
									if (x.getName().contains("mac1")) {
										plot(ArrayUtils.subarray(x.getValue(), 16501, 65000),
												ArrayUtils.subarray(y.getValue(), 16501, 65000),true);
									} else {
										plot(x.getValue(), y.getValue(), false);
									}
									y.setPlotted(true);
								}
							}
						}
					}
				});
			}
		}

		private void plot(double[] x, double[] y, boolean clear) {
			DoubleDataset x2 = new DoubleDataset(x, new int[] { x.length });
			List<AbstractDataset> plotDatasets = new ArrayList<AbstractDataset>();
			DoubleDataset y2 = new DoubleDataset(y, new int[] { y.length });
			plotDatasets.add(y2);
			if (clear) {
				plottingSystem.clear();
			}
			plottingSystem.createPlot1D(x2, plotDatasets, new NullProgressMonitor());
		}
	}
}
