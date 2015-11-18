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

package uk.ac.gda.exafs.ui.detectors.plotter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;

public class DetectorPlotter extends Composite {
	private SashForm sash;
	private Composite plotterComposite;
	private IPlottingSystem<Composite> plottingsystem;
	private IRegion regionOnDisplay;
	private Text statusLabel;
	private Dataset[] dataSets;

	public DetectorPlotter(Composite parent, int style, final IWorkbenchPart part) throws Exception {
		super(parent, style);
		sash = new SashForm(parent, SWT.VERTICAL);
		createPlotterComposite(part);
		createFeedbackComposite();
		sash.setWeights(new int[] { 100, 10 });
	}

	private void createPlotterComposite(final IWorkbenchPart part) throws Exception {
		plotterComposite = new Composite(sash, SWT.NONE);
		ActionBarWrapper wrapper = ActionBarWrapper.createActionBars(plotterComposite, null);
		plottingsystem = PlottingFactory.createPlottingSystem();
		plottingsystem.createPlotPart(plotterComposite, "", null, PlotType.XY, part);
		plottingsystem.setRescale(true);
		plottingsystem.getPlotActionSystem().fillZoomActions(wrapper.getToolBarManager());
		plottingsystem.getPlotActionSystem().fillPrintActions(wrapper.getToolBarManager());
		plottingsystem.getPlotActionSystem().fillToolActions(wrapper.getToolBarManager(), ToolPageRole.ROLE_1D);
		plottingsystem.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createRegionToDisplay();
	}

	private void createRegionToDisplay() throws Exception {
		this.regionOnDisplay = plottingsystem.createRegion("ROI", RegionType.XAXIS);
		regionOnDisplay.setRegionColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		plottingsystem.addRegion(regionOnDisplay);
		regionOnDisplay.setMobile(true);
	}

	/**
	 * To be given updates when the user drags the ROI
	 *
	 * @param regionListener
	 */
	public void addRegionListener(IROIListener regionListener) {
		regionOnDisplay.addROIListener(regionListener);
	}

	private void createFeedbackComposite() {
		statusLabel = new Text(sash, SWT.WRAP | SWT.V_SCROLL);
		statusLabel.setEditable(false);
	}

	/**
	 * Tell this what to plot
	 *
	 * @param dataSets
	 */
	public void setDataSets(Dataset... dataSets) {
		this.dataSets = dataSets;
	}

	/**
	 * Plots the data given by setDatasets
	 */
	public void plotData() {
		plottingsystem.clear();
		List<IDataset> ys = new ArrayList<IDataset>();
		for (Dataset ds : dataSets)
			ys.add(ds);
		plottingsystem.createPlot1D(null, ys, null);
		plottingsystem.setTitle("");
	}

	/**
	 * Add to the status messages to the user at the bottom
	 *
	 * @param text
	 * @param logger
	 */
	public void appendStatus(final String text, Logger logger) {
		if (logger != null)
			logger.info(text);
		if (getShell() == null)
			return;
		if (getShell().isDisposed())
			return;
		getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				statusLabel.append(DateFormat.getDateTimeInstance().format(new Date()));
				statusLabel.append(" ");
				statusLabel.append(text);
				statusLabel.append("\n");
			}
		});
	}

	@Override
	public void dispose() {
		if (plottingsystem != null)
			plottingsystem.dispose();
	}

}