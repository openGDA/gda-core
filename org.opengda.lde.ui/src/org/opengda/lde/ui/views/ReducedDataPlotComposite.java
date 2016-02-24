/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package org.opengda.lde.ui.views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.lde.events.NewDataFileEvent;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;
import gda.util.Sleep;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

/**
 * a composite to plot reduced data from data file triggered by data reduction process event {@link NewDataFileEvent}.
 */
public class ReducedDataPlotComposite extends Composite implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(ReducedDataPlotComposite.class);

	private static final String SPECTRUM_PLOT = "Pattern";
	private IPlottingSystem plottingSystem;
	private ILineTrace profileLineTrace;

	private Scriptcontroller eventAdmin;
	private String eventAdminName;
	private String plotName;
	private LDEResourceUtil resUtil;
	private List<Sample> samples;

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public ReducedDataPlotComposite(IWorkbenchPart part, Composite parent, int style) throws Exception {
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
		plottingSystem.createPlotPart(plotComposite, getPlotName(), part instanceof IViewPart ? ((IViewPart) part).getViewSite().getActionBars() : null,
				PlotType.XY_STACKED, part);
		plottingSystem.setTitle(SPECTRUM_PLOT);
		plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
		plottingSystem.getSelectedXAxis().setFormatPattern("#####.#");
	}

	public void initialise() {
		if (eventAdminName!=null) {
			eventAdmin=Finder.getInstance().find(eventAdminName);
			if (eventAdmin != null) eventAdmin.addIObserver(this);
		}
		if (resUtil!=null) {
			try {
				samples=resUtil.getSamples();
			} catch (Exception e) {
				logger.error("Cannot retieve sample definitions from LDE resource util.", e);
			}
		}
	}
	@Override
	public void dispose() {
		if (!plottingSystem.isDisposed()) {
			plottingSystem.clear();
		}
		if (eventAdmin!=null) {
			eventAdmin.deleteIObserver(this);
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

	private void updatePlot(final IProgressMonitor monitor, String value, final String sampleName) {
		File file= new File(value);
		long starttime=System.currentTimeMillis();
		long timer=0;
		while (!file.exists() && timer < 10000) {
			Sleep.sleep(50);
			timer=System.currentTimeMillis()-starttime;
		}
		if (timer >= 10000) {
			try {
				logger.error("Timeout: Cannot find file "+value +" within 10 seconds.");
				throw new java.util.concurrent.TimeoutException("Cannot find file "+value +" within 10 seconds.");
			} catch (java.util.concurrent.TimeoutException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		IDataHolder dataHolder = null;
		try {
			dataHolder = LoaderFactory.getData(value);
		} catch (Exception e) {
			logger.error("Exception on load data from file {}",value);
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		String[] names = dataHolder.getNames();
		IDataset xAxis = dataHolder.getDataset(0);
		xAxis.setName(names[0]);
		IDataset yds = dataHolder.getDataset(1);
		yds.setName(names[1]);
//		Dataset error=(Dataset) dataHolder.getDataset(2);
//		error.setName(names[2]);
//		yds.setError(error);
		ArrayList<IDataset> plotDataSets = new ArrayList<IDataset>();
		plotDataSets.add(yds);
		plottingSystem.clear();
		final List<ITrace> profileLineTraces = plottingSystem.createPlot1D(xAxis, plotDataSets, monitor);
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {

					if (!profileLineTraces.isEmpty()) {
						plottingSystem.setShowLegend(false);
						// plottingSystem.getSelectedYAxis().setTitle(dataset.getName());
						plottingSystem.setTitle(sampleName);
						profileLineTrace = (ILineTrace) profileLineTraces.get(0);
						profileLineTrace.setTraceColor(ColorConstants.blue);
					}
					// plottingSystem.autoscaleAxes();
				}
			});
		}
	}

	@Override
	public void update(Object source, final Object arg) {
		if (source == eventAdmin) {
			if (arg instanceof NewDataFileEvent) {

				Display.getDefault().asyncExec(new Runnable() {


					@Override
					public void run() {
						String sampleID = ((NewDataFileEvent)arg).getSampleID();
						String filename = ((NewDataFileEvent)arg).getFilename();
						if (sampleID!=null) {
							for (Sample sample : samples) {
								if (sample.getSampleID().equalsIgnoreCase(sampleID)) {
									updatePlot(new NullProgressMonitor(), filename,sample.getName()+" - "+FilenameUtils.getName(filename));
								}
							}
						} else {
							updatePlot(new NullProgressMonitor(), filename,FilenameUtils.getName(filename));
						}
					}
				});
			}
			//TODO handling FAIL and WARN event.
		}
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	public String getEventAdminName() {
		return eventAdminName;
	}

	public void setEventAdminName(String eventAdminName) {
		this.eventAdminName = eventAdminName;
	}

	public LDEResourceUtil getResUtil() {
		return resUtil;
	}

	public void setResUtil(LDEResourceUtil resUtil) {
		this.resUtil = resUtil;
	}
}
