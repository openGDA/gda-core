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
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataArrayListener;

public class MACLivePlotComposite extends Composite {
	private Logger logger = LoggerFactory.getLogger(MACLivePlotComposite.class);
	private String PLOT_TITLE = "MAC Live Data";

	private IPlottingSystem plottingSystem;
	private List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> dataListeners = new ArrayList<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>>();

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
		for (Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> listener : getDataListeners()) {
			listener.getValue1().dispose();
			listener.getValue2().dispose();
		}
		dataDisplayers.clear();
		dataListeners.clear();
		plottingSystem.dispose();
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

	@Override
	public boolean setFocus() {
		plottingSystem.setFocus();
		return super.setFocus();
	}

	private void createTraces() {
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					for (Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener> item : getDataListeners()) {
						ILineTrace trace = plottingSystem.createLineTrace(item.getValue0());
						Quartet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, ILineTrace> with = item.add(trace);
						plottingSystem.addTrace(trace);
						dataDisplayers.add(with);
					}
				}
			});
		}
	}

	public List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> getDataListeners() {
		return dataListeners;
	}

	public void setDataListeners(
			List<Triplet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener>> dataListeners) {
		this.dataListeners = dataListeners;
	}

	private List<Quartet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, ILineTrace>> dataDisplayers = new ArrayList<Quartet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, ILineTrace>>();

	private void updatePlot() {
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					boolean visible = MACLivePlotComposite.this.isVisible();
					if (visible) {
						for (Quartet<String, EpicsDoubleDataArrayListener, EpicsDoubleDataArrayListener, ILineTrace> listener : dataDisplayers) {
							String traceName = listener.getValue0();
							EpicsDoubleDataArrayListener x = listener.getValue1();
							EpicsDoubleDataArrayListener y = listener.getValue2();
							ILineTrace trace = listener.getValue3();
							if (traceName.equalsIgnoreCase("mac1")) {
								trace.setData(new DoubleDataset(ArrayUtils.subarray(x.getValue(), 16501, 65000)),
										new DoubleDataset(ArrayUtils.subarray(y.getValue(), 16501, 65000)));
							} else {
								trace.setData(new DoubleDataset(x.getValue()), new DoubleDataset(y.getValue()));
							}
							
						}
						plottingSystem.repaint();
					}
				}
			});
		}
	}

}
