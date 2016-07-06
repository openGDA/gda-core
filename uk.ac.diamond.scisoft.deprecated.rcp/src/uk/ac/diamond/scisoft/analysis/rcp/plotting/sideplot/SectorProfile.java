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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.roi.data.ROIData;
import org.dawb.common.ui.plot.roi.data.SectorROIData;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.jreality.print.PlotExportUtil;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.impl.function.Centroid;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROIList;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.HandleStatus;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.SectorROIHandler;
import org.eclipse.dawnsci.plotting.api.jreality.core.AxisMode;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DAppearance;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DGraphTable;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DStyles;
import org.eclipse.dawnsci.plotting.api.jreality.impl.PlotException;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayType;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.VectorOverlayStyles;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.primitives.PrimitiveType;
import org.eclipse.dawnsci.plotting.api.jreality.tool.IImagePositionEvent;
import org.eclipse.dawnsci.plotting.api.jreality.util.PlotColorUtility;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPartSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
import uk.ac.diamond.scisoft.analysis.plotserver.DataBeanException;
import uk.ac.diamond.scisoft.analysis.plotserver.DatasetWithAxisInformation;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.DropDownAction;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.ROIDataList;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.SectorROITableViewer;
import uk.ac.diamond.scisoft.analysis.rcp.queue.InteractiveJobAdapter;
import uk.ac.diamond.scisoft.analysis.rcp.util.FloatSpinner;
import uk.ac.diamond.scisoft.analysis.rcp.util.ResourceProperties;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.StaticScanPlotView;

import com.swtdesigner.SWTResourceManager;

@Deprecated
public class SectorProfile extends SidePlotProfile {

	private static Logger logger = LoggerFactory.getLogger(SectorProfile.class);

	private DataSetPlotter radPlotter;
	private DataSetPlotter aziPlotter;

	private Composite parent;

	private Action saveRad;
	private Action saveAzi;
	private Action copyRad;
	private Action copyAzi;
	private Action printRad;
	private Action printAzi;
	private Action addtoHistory;
	private Action removefromHistory;
	private Action pushRadialPlottingDataPlot1;
	private Action pushAzimuthPlottingDataPlot1;
	private Action pushRadialPlottingDataPlot2;
	private Action pushAzimuthPlottingDataPlot2;
	private DropDownAction saveGraph;
	private DropDownAction copyGraph;
	private DropDownAction printGraph;
	private DropDownAction pushPlottingData;
	private MenuManager saveMenu;
	private MenuManager copyMenu;
	private MenuManager printMenu;
	private String printButtonText = ResourceProperties.getResourceString("PRINT_BUTTON");
	private String printToolTipText = ResourceProperties.getResourceString("PRINT_TOOLTIP");
	private String printImagePath = ResourceProperties.getResourceString("PRINT_IMAGE_PATH");
	private String copyButtonText = ResourceProperties.getResourceString("COPY_BUTTON");
	private String copyToolTipText = ResourceProperties.getResourceString("COPY_TOOLTIP");
	private String copyImagePath = ResourceProperties.getResourceString("COPY_IMAGE_PATH");
	private String saveButtonText = ResourceProperties.getResourceString("SAVE_BUTTON");
	private String saveToolTipText = ResourceProperties.getResourceString("SAVE_TOOLTIP");
	private String saveImagePath = ResourceProperties.getResourceString("SAVE_IMAGE_PATH");

	private FloatSpinner spsx, spsy, spsr, sper, spsang, speang;
	private Text txSum;
	private Spinner spgrn, spgrs; // guide ring number and spacing

	private int grNumber = 2; // guide ring parameters
	private int grSpacing = 40;

	List<Button> radioButtons;

	private Button combine;
	private Button centreLock, centreReset, centreCentroid;
	private boolean lockCentre;

	private IJobManager jobManager;
	private String spinnerJobName = "Spinner Update Job";

	private class SectorJob extends InteractiveJobAdapter {
		private SectorROI sroi = null;
		private boolean subsample;

		public SectorJob(SectorROI sroi, boolean quick) {
			this.sroi = sroi;
			subsample = quick;
		}

		@Override
		public void run(IProgressMonitor monitor) {
			if (isNull())
				return;

			if (monitor != null) monitor.worked(1);

			if (subsample)
				roiData = new SectorROIData(sroi, subData, mask, subFactor);
			else {
				if (oProvider!=null) oProvider.setPlotAreaCursor(SWT.CURSOR_WAIT);

				roiData = new SectorROIData(sroi, data, mask);

				if (oProvider!=null) oProvider.restoreDefaultPlotAreaCursor();
			}
			if (monitor != null) {
				if (monitor.isCanceled())
					return;

				monitor.worked(1);
			}

			if (roiData.isPlot())
				drawPlots(sroi);

			if (monitor != null) monitor.worked(1);

			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					txSum.setText(String.format("%.3e", roiData.getProfileSum()));
				}
			});
		}

		@Override
		public boolean isNull() {
			return sroi == null;
		}
	}

	public SectorProfile() {
		super();
		roiClass = SectorROI.class;
		roiListClass = SectorROIList.class;
		
		jobManager = Job.getJobManager();
	}

	/**
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		super.createPartControl(this.parent);
		container = new Composite(this.parent, SWT.NONE);
		container.setLayout(new FillLayout());

		final SashForm ss = new SashForm(container, SWT.VERTICAL);
		ss.setLayout(new FillLayout());

		GridData gridData;

		radPlotter = new SidePlotter1D(ss, "Radial profile");
		radPlotter.setAxisModes(AxisMode.CUSTOM, AxisMode.LINEAR, AxisMode.LINEAR);
		radPlotter.setXAxisLabel("Distance along radial axis");

		aziPlotter = new SidePlotter1D(ss, "Azimuthal profile");
		aziPlotter.setAxisModes(AxisMode.CUSTOM, AxisMode.LINEAR, AxisMode.LINEAR);
		aziPlotter.setXAxisLabel("Azimuthal angle, degrees");

		// GUI creation and layout
		final ScrolledComposite scomp = new ScrolledComposite(ss, SWT.VERTICAL | SWT.HORIZONTAL);

		final Composite controls = new Composite(scomp, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, true);
		controls.setLayout(gridLayout);
		{
			final Group groupCurrent = new Group(controls, SWT.NONE);
			gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			groupCurrent.setLayoutData(gridData);
			groupCurrent.setLayout(new GridLayout(6, false));
			groupCurrent.setText("Current ROI");
			{
				// 1st row
				new Label(groupCurrent, SWT.NONE).setText("Centre x:");
				spsx = new FloatSpinner(groupCurrent, SWT.BORDER, 6, 2);
				spsx.addSelectionListener(spinnerListener);

				new Label(groupCurrent, SWT.NONE).setText("Start radius:");
				spsr = new FloatSpinner(groupCurrent, SWT.BORDER, 6, 2);
				spsr.addSelectionListener(spinnerListener);

				new Label(groupCurrent, SWT.NONE).setText("End radius:");
				sper = new FloatSpinner(groupCurrent, SWT.BORDER, 6, 2);
				sper.addSelectionListener(spinnerListener);

				// 2nd row
				new Label(groupCurrent, SWT.NONE).setText("Centre y:");
				spsy = new FloatSpinner(groupCurrent, SWT.BORDER, 6, 2);
				spsy.addSelectionListener(spinnerListener);

				new Label(groupCurrent, SWT.NONE).setText("Start angle:");
				spsang = new FloatSpinner(groupCurrent, SWT.BORDER, 7, 2);
				spsang.setMinimum(-361.0);
				spsang.setMaximum(361.0);
				spsang.addSelectionListener(spinnerListener);

				new Label(groupCurrent, SWT.NONE).setText("End angle:");
				speang = new FloatSpinner(groupCurrent, SWT.BORDER, 7, 2);
				speang.setMinimum(-1.0);
				speang.setMaximum(721.0);
				speang.addSelectionListener(spinnerListener);

				// 3rd row
				new Label(groupCurrent, SWT.NONE).setText("Sum:");
				txSum = new Text(groupCurrent, SWT.READ_ONLY | SWT.BORDER);
				txSum.setTextLimit(10);

				GridData gda = new GridData();
				gda.horizontalSpan = 2;
				combine = new Button(groupCurrent, SWT.CHECK);
				combine.setLayoutData(gda);
				combine.setText("Combine symmetry");
				combine.setToolTipText("Add symmetric sector to profile");
				combine.addSelectionListener(combineButtonListener);

				GridData gdb = new GridData();
				gdb.horizontalSpan = 2;
				final Button clip = new Button(groupCurrent, SWT.CHECK);
				clip.setLayoutData(gdb);
				clip.setText("Clipping comp");
				clip.setToolTipText("Compensate for clipping of region of interests");
				clip.addSelectionListener(clippingButtonListener);

				// 4th row
				GridData gdc = new GridData();
				gdc.horizontalSpan = 2;
				final Button invert = new Button(groupCurrent, SWT.CHECK);
				invert.setLayoutData(gdc);
				invert.setText("Invert brightness");
				invert.setToolTipText("Invert overlay brightness");
				invert.addSelectionListener(brightnessButtonListener);

				// blank columns
				new Label(groupCurrent, SWT.NONE).setText("");
				new Label(groupCurrent, SWT.NONE).setText("");
				new Label(groupCurrent, SWT.NONE).setText("");
				new Label(groupCurrent, SWT.NONE).setText("");

				// 5th row
				GridData gdd = new GridData();
				gdd.horizontalSpan = 2;
				Button copyToTable = new Button(groupCurrent, SWT.PUSH);
				copyToTable.setLayoutData(gdd);
				copyToTable.setText("Copy current to table");
				copyToTable.addSelectionListener(copyButtonListener);

				GridData gde = new GridData();
				gde.horizontalSpan = 2;
				Button deleteCurrent = new Button(groupCurrent, SWT.PUSH);
				deleteCurrent.setLayoutData(gde);
				deleteCurrent.setText("Delete current");
				deleteCurrent.addSelectionListener(deleteButtonListener);
			}

			// guide rings and centring
			final Group groupGuide = new Group(controls, SWT.NONE);
			gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			groupGuide.setLayoutData(gridData);
			groupGuide.setLayout(new GridLayout(8, false));
			groupGuide.setText("Guide and Centring");
			{
				new Label(groupGuide, SWT.NONE).setText("Rings:");
				spgrn = new Spinner(groupGuide, SWT.BORDER);
				spgrn.setMinimum(0);
				spgrn.setMaximum(8);
				spgrn.setIncrement(1);
				spgrn.setPageIncrement(2);
				spgrn.setSelection(grNumber);
				spgrn.addSelectionListener(grSpinnerListener);

				new Label(groupGuide, SWT.NONE).setText("Spacing:");
				spgrs = new Spinner(groupGuide, SWT.BORDER);
				spgrs.setMinimum(10);
				spgrs.setMaximum(500);
				spgrs.setIncrement(10);
				spgrs.setPageIncrement(50);
				spgrs.setSelection(grSpacing);
				spgrs.addSelectionListener(grSpinnerListener);

				// blank column
				new Label(groupGuide, SWT.NONE).setText("      ");

				centreLock = new Button(groupGuide, SWT.CHECK);
				centreLock.setText("Lock centre");
				centreLock.setToolTipText("Lock position of sector centre");
				centreLock.addSelectionListener(centreListener);

				centreReset = new Button(groupGuide, SWT.PUSH);
				centreReset.setText("Reset");
				centreReset.setToolTipText("Reset sector centre to image centre");
				centreReset.addSelectionListener(centreListener);

				centreCentroid = new Button(groupGuide, SWT.PUSH);
				centreCentroid.setText("Centroid");
				centreCentroid.setToolTipText("Set sector centre to image centroid");
				centreCentroid.addSelectionListener(centreListener);
			}

			// symmetry
			final Group groupSymmetry = new Group(controls, SWT.NONE);
			gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			groupSymmetry.setLayoutData(gridData);
			groupSymmetry.setLayout(new RowLayout());
			groupSymmetry.setText("Symmetry");
			{
				Button btn;
				radioButtons = new ArrayList<Button>();

				btn = new Button(groupSymmetry, SWT.RADIO);
				btn.setText("None");
				btn.setToolTipText("Do nothing");
				btn.setSelection(true);
				btn.addSelectionListener(radioListener);
				radioButtons.add(SectorROI.NONE, btn);

				btn = new Button(groupSymmetry, SWT.RADIO);
				btn.setText("Full");
				btn.setToolTipText("Use full circle");
				btn.addSelectionListener(radioListener);
				radioButtons.add(SectorROI.FULL, btn);

				btn = new Button(groupSymmetry, SWT.RADIO);
				btn.setText("L/R reflect");
				btn.setToolTipText("Reflect in y-axis");
				btn.addSelectionListener(radioListener);
				radioButtons.add(SectorROI.XREFLECT, btn);

				btn = new Button(groupSymmetry, SWT.RADIO);
				btn.setText("U/D reflect");
				btn.setToolTipText("Reflect in x-axis");
				btn.addSelectionListener(radioListener);
				radioButtons.add(SectorROI.YREFLECT, btn);

				btn = new Button(groupSymmetry, SWT.RADIO);
				btn.setText("+90");
				btn.setToolTipText("Rotate by +90");
				btn.addSelectionListener(radioListener);
				radioButtons.add(SectorROI.CNINETY, btn);

				btn = new Button(groupSymmetry, SWT.RADIO);
				btn.setText("-90");
				btn.setToolTipText("Rotate by -90");
				btn.addSelectionListener(radioListener);
				radioButtons.add(SectorROI.ACNINETY, btn);

				btn = new Button(groupSymmetry, SWT.RADIO);
				btn.setText("Invert");
				btn.setToolTipText("Invert through centre");
				btn.addSelectionListener(radioListener);
				radioButtons.add(SectorROI.INVERT, btn);
			}
		}

		final Composite table = new Composite(ss, SWT.NONE);
		ss.setWeights(new int[] { 25, 25, 30, 20 });
		table.setLayout(new FillLayout());

		tViewer = new SectorROITableViewer(table, this, this);

		scomp.setContent(controls);
		controls.setSize(controls.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		// end of GUI creation

		radPlotter.refresh(false);
		aziPlotter.refresh(false);

		// initialize ROI and data

		combine.setEnabled(false);

		updateAllSpinnersInt((SectorROI) roi);
		roiIDs = new ArrayList<Integer>();
		dragIDs = new ArrayList<Integer>();

		// initialize ROIs
		if (roiDataList == null)
			roiDataList = new ROIDataList();
		roisIDs = new ArrayList<Integer>();

		tViewer.setInput(roiDataList);

		// handle areas
		roiHandler = new SectorROIHandler((SectorROI) roi);

		// default colour: red
		dColour = new Color(255, 0, 0);

		// invert overlay colour
		float[] hsb;
		hsb = Color.RGBtoHSB(dColour.getRed(), dColour.getGreen(), dColour.getBlue(), null);
		cColour = Color.getHSBColor(hsb[0], hsb[1], (float) (1.0 - 0.7 * hsb[2]));
		oColour = dColour;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (radPlotter != null)
			radPlotter.cleanUp();
		if (aziPlotter != null)
			aziPlotter.cleanUp();
	}
	
	@Override
	protected void updatePlot(IROI roib) {
		final SectorROI sroi = (SectorROI) roib;
		setRadioButtons(sroi);
		getDataset();

		if (data == null) {
			logger.warn("No data");
			return;
		}
		
		if (data.getRank() != 2) {
			logger.warn("Unsuitable data for sector plot");
			return;
		}

		if (sroi != null) {
			if (dragging && subData != null) {
				try {
					final SectorJob obj = new SectorJob(sroi, true);
					roiQueue.addJob(obj);
				} catch (Exception e) {
					logger.error("Cannot generate ROI data", e);
				}
			} else {
				try {
					final SectorJob obj = new SectorJob(sroi, false);
					roiQueue.addJob(obj);
				} catch (Exception e) {
					logger.error("Cannot generate ROI data", e);
				}
			}
		}
	}

	public void drawPlots(SectorROI sroi) {
		Plot1DGraphTable colourTable;
		Plot1DAppearance newApp;
		int p, l;
		int nHistory;

		List<IDataset> plots = new ArrayList<IDataset>();
		List<AxisValues> paxes = new ArrayList<AxisValues>();
		
		l = p = 0;
		colourTable = radPlotter.getColourTable();
		nHistory = radPlotter.getNumHistory();

		if (sroi != null && roiData.getProfileData() != null) {
			if (l + nHistory >= colourTable.getLegendSize()) {
				newApp = new Plot1DAppearance(PlotColorUtility.getDefaultColour(p), Plot1DStyles.SOLID, "Line 1");
				colourTable.addEntryOnLegend(l, newApp);
			} else {
				newApp = colourTable.getLegendEntry(l);
				newApp.setColour(PlotColorUtility.getDefaultColour(p));
				newApp.setStyle(Plot1DStyles.SOLID);
				newApp.setName("Line 1");
			}

			plots.add(roiData.getProfileData(0));
			paxes.add(roiData.getXAxis(0));

			if (sroi.hasSeparateRegions()) {
				l++;
				if (l + nHistory >= colourTable.getLegendSize()) {
					newApp = new Plot1DAppearance(PlotColorUtility.getDefaultColour(p), Plot1DStyles.DASHED,
							"Symmetry Line 1");
					colourTable.addEntryOnLegend(l, newApp);
				} else {
					newApp = colourTable.getLegendEntry(l);
					newApp.setColour(PlotColorUtility.getDefaultColour(p));
					newApp.setStyle(Plot1DStyles.DASHED);
					newApp.setName("Symmetry Line 1");
				}

				plots.add(roiData.getProfileData(2));
				paxes.add(roiData.getXAxis(2));
			}
			l++;
			p++;
		}

		for (int i = 0, imax = roiDataList.size(); i < imax; i++) {
			SectorROIData rd = (SectorROIData) roiDataList.get(i);
			Color colour = PlotColorUtility.getDefaultColour(i + 1);
			RGB rgb = new RGB(colour.getRed(), colour.getGreen(), colour.getBlue());
			rd.setPlotColourRGB(rgb);

			if (rd.isPlot() && rd.getProfileData() != null) {
				plots.add(rd.getProfileData(0));
				paxes.add(rd.getXAxis(0));
				if (l + nHistory >= colourTable.getLegendSize()) {
					newApp = new Plot1DAppearance(colour, Plot1DStyles.SOLID, "Line " + (p + 1));
					colourTable.addEntryOnLegend(l, newApp);
				} else {
					newApp = colourTable.getLegendEntry(l);
					newApp.setColour(colour);
					newApp.setStyle(Plot1DStyles.SOLID);
					newApp.setName("Line " + (p + 1));
				}

				if (rd.getROI().hasSeparateRegions()) {
					l++;
					if (l + nHistory >= colourTable.getLegendSize()) {
						newApp = new Plot1DAppearance(colour, Plot1DStyles.DASHED, "Symmetry Line " + (p + 1));
						colourTable.addEntryOnLegend(l, newApp);
					} else {
						newApp = colourTable.getLegendEntry(l);
						newApp.setColour(colour);
						newApp.setStyle(Plot1DStyles.DASHED);
						newApp.setName("Symmetry Line " + (p + 1));
					}

					plots.add(rd.getProfileData(2));
					paxes.add(rd.getXAxis(2));
				}
				l++;
				p++;
			}
		}

		while (nHistory-- > 0) { // tidy up history colours
			newApp = colourTable.getLegendEntry(l++);
			newApp.setColour(PlotColorUtility.getDefaultColour(p++));
		}

		try {
			radPlotter.replaceAllPlots(plots, paxes);
		} catch (PlotException e) {
			e.printStackTrace();
		}

		plots.clear();
		paxes.clear();

		l = p = 0;
		colourTable = aziPlotter.getColourTable();
		nHistory = aziPlotter.getNumHistory();
		if (sroi != null && roiData.getProfileData() != null) {
			if (l + nHistory >= colourTable.getLegendSize()) {
				newApp = new Plot1DAppearance(PlotColorUtility.getDefaultColour(p), Plot1DStyles.SOLID, "Line 1");
				colourTable.addEntryOnLegend(l, newApp);
			} else {
				newApp = colourTable.getLegendEntry(l);
				newApp.setColour(PlotColorUtility.getDefaultColour(p));
				newApp.setStyle(Plot1DStyles.SOLID);
				newApp.setName("Line 1");
			}

			plots.add(roiData.getProfileData(1));
			paxes.add(roiData.getXAxis(1));

			if (sroi.hasSeparateRegions()) {
				l++;
				if (l + nHistory >= colourTable.getLegendSize()) {
					newApp = new Plot1DAppearance(PlotColorUtility.getDefaultColour(p), Plot1DStyles.DASHED,
							"Symmetry Line 1");
					colourTable.addEntryOnLegend(l, newApp);
				} else {
					newApp = colourTable.getLegendEntry(l);
					newApp.setColour(PlotColorUtility.getDefaultColour(p));
					newApp.setStyle(Plot1DStyles.DASHED);
					newApp.setName("Symmetry Line 1");
				}

				plots.add(roiData.getProfileData(3));
				paxes.add(roiData.getXAxis(3));
			}
			l++;
			p++;
		}

		for (int i = 0, imax = roiDataList.size(); i < imax; i++) {
			SectorROIData rd = (SectorROIData) roiDataList.get(i);
			if (rd.isPlot() && rd.getProfileData() != null) {
				plots.add(rd.getProfileData(1));
				paxes.add(rd.getXAxis(1));
				if (l + nHistory >= colourTable.getLegendSize()) {
					newApp = new Plot1DAppearance(rd.getPlotColour(), Plot1DStyles.SOLID, "Line " + (p + 1));
					colourTable.addEntryOnLegend(l, newApp);
				} else {
					newApp = colourTable.getLegendEntry(l);
					newApp.setColour(rd.getPlotColour());
					newApp.setStyle(Plot1DStyles.SOLID);
					newApp.setName("Line " + (p + 1));
				}

				if (rd.getROI().hasSeparateRegions()) {
					l++;
					if (l + nHistory >= colourTable.getLegendSize()) {
						newApp = new Plot1DAppearance(rd.getPlotColour(), Plot1DStyles.DASHED, "Symmetry Line "
								+ (p + 1));
						colourTable.addEntryOnLegend(l, newApp);
					} else {
						newApp = colourTable.getLegendEntry(l);
						newApp.setColour(rd.getPlotColour());
						newApp.setStyle(Plot1DStyles.DASHED);
						newApp.setName("Symmetry Line " + (p + 1));
					}

					plots.add(rd.getProfileData(3));
					paxes.add(rd.getXAxis(3));
				}
				l++;
				p++;
			}
		}

		while (nHistory-- > 0) { // tidy up history colours
			newApp = colourTable.getLegendEntry(l++);
			newApp.setColour(PlotColorUtility.getDefaultColour(p++));
		}

		try {
			aziPlotter.replaceAllPlots(plots, paxes);
		} catch (PlotException e) {
			e.printStackTrace();
		}

		radPlotter.updateAllAppearance();
		radPlotter.refresh(false);
		aziPlotter.updateAllAppearance();
		aziPlotter.refresh(false);

	}

	/**
	 * Draw dragged out overlay for given region of interest
	 * 
	 * @param roib
	 */
	private void drawDraggedOverlay(IROI roib) {
		if (oProvider == null)
			return;

		if (dragIDs.isEmpty()) {
			dragIDs.add(-1);
			dragIDs.add(-1);
			dragIDs.add(-1);
			dragIDs.add(-1);
		}

		int id, index;
		index = 0;

		// sector
		id = dragIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.SECTOR);
			dragIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		final SectorROI sroi = (SectorROI) roib;
		final double[] spt = sroi.getPointRef();
		final double[] rad = sroi.getRadii();
		final double[] ang = sroi.getAnglesDegrees();

		oProvider.begin(OverlayType.VECTOR2D);

		oProvider.drawSector(id, spt[0], spt[1], rad[0], rad[1], -ang[1], -ang[0]);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);

		// image size dependent handle size
		getDataset();
		int hsize = calcHandleSize(data.getShape());

		// centre circle
		id = dragIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.CIRCLE);
			dragIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		oProvider.drawCircle(id, spt[0], spt[1], hsize / 2);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);

		// centre mark - horizontal
		id = dragIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.LINE);
			dragIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		oProvider.drawLine(id, spt[0] - hsize / 2, spt[1], spt[0] + hsize / 2, spt[1]);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);

		// centre mark - vertical
		id = dragIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.LINE);
			dragIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		oProvider.drawLine(id, spt[0], spt[1] - hsize / 2, spt[0], spt[1] + hsize / 2);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);

		oProvider.end(OverlayType.VECTOR2D);
	}

	@Override
	protected void drawCurrentOverlay() {
		if (oProvider == null || roi == null)
			return;

		if (roiIDs.isEmpty()) {
			roiIDs.add(-1);
			roiIDs.add(-1);
			roiIDs.add(-1);
			roiIDs.add(-1);
		}

		int id, index;
		index = 0;

		// sector
		id = roiIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.SECTOR);
			roiIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		final SectorROI sroi = (SectorROI) roi;
		final double[] spt = roi.getPointRef();
		final double[] rad = sroi.getRadii();
		final double[] ang = sroi.getAnglesDegrees();

		oProvider.begin(OverlayType.VECTOR2D);

		oProvider.drawSector(id, spt[0], spt[1], rad[0], rad[1], -ang[1], -ang[0]);
		oProvider.setStyle(id, VectorOverlayStyles.FILLED_WITH_OUTLINE);
		oProvider.setColour(id, oColour);
		oProvider.setOutlineColour(id, oColour);
		oProvider.setLineThickness(id, oThickness);
		oProvider.setTransparency(id, 0.9);
		oProvider.setOutlineTransparency(id, oTransparency);

		// symmetry sector
		id = roiIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.SECTOR);
			roiIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		if (sroi.getSymmetry() == SectorROI.NONE) {
			oProvider.setPrimitiveVisible(id, false);
		} else {
			double[] nang = sroi.getSymmetryAngles();
			oProvider.drawSector(id, spt[0], spt[1], rad[0], rad[1],
					-Math.toDegrees(nang[1]), -Math.toDegrees(nang[0]));
			oProvider.setStyle(id, VectorOverlayStyles.OUTLINE);
			oProvider.setColour(id, oColour);
			oProvider.setOutlineColour(id, oColour);
			oProvider.setLineThickness(id, oThickness);
			oProvider.setTransparency(id, 0.9);
			oProvider.setOutlineTransparency(id, oTransparency);
		}

		// image size dependent handle size
		getDataset();
		int hsize = calcHandleSize(data.getShape());

		// centre mark - horizontal
		id = roiIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.LINE, true);
			roiIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		oProvider.drawLine(id, spt[0] - hsize / 2, spt[1], spt[0] + hsize / 2, spt[1]);
		oProvider.setAnchorPoints(id, spt[0], spt[1]);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);

		// centre mark - vertical
		id = roiIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.LINE, true);
			roiIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		oProvider.drawLine(id, spt[0], spt[1] - hsize / 2, spt[0], spt[1] + hsize / 2);
		oProvider.setAnchorPoints(id, spt[0], spt[1]);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);

		// guide rings
		for (int i = 1; i <= grNumber; i++) {
			if (index == roiIDs.size()) {
				roiIDs.add(-1);
			} else if (index > roiIDs.size()) {
				logger.debug("Guide ring: primitive IDs list out of synch!");
				break;
			}

			id = roiIDs.get(index);
			if (id == -1) {
				id = oProvider.registerPrimitive(PrimitiveType.CIRCLE);
				roiIDs.set(index, id);
				if (id == -1)
					return;
			} else
				oProvider.setPrimitiveVisible(id, true);
			index++;

			oProvider.drawCircle(id, spt[0], spt[1], i * grSpacing);
			oProvider.setStyle(id, VectorOverlayStyles.OUTLINE);
			oProvider.setColour(id, oColour);
			oProvider.setTransparency(id, oTransparency);
		}
		if (index < roiIDs.size()) {
			// hide unwanted rings
			for (; index < roiIDs.size(); index++) {
				id = roiIDs.get(index);
				if (id != -1)
					oProvider.setPrimitiveVisible(id, false);
				roiIDs.set(index, -1);
			}
		}

		// handle areas
		double[] hpt;
		double psize = hsize / sroi.getRadius(1); // angular extent
		double[] apt;
		for (int h = 0, hmax = roiHandler.size() - 1; h < hmax; h++) {
			int hid = roiHandler.get(h);
			if (hid == -1) {
				hid = oProvider.registerPrimitive(PrimitiveType.SECTOR, true);
				roiHandler.set(h, hid);
			} else
				oProvider.setPrimitiveVisible(hid, true);

			hpt = ((SectorROIHandler) roiHandler).getSectorPoint(h, hsize, psize);
			oProvider.drawSector(hid, spt[0], spt[1], hpt[0], hpt[0] + hsize, -Math.toDegrees(hpt[1]
					+ psize), -Math.toDegrees(hpt[1]));
			apt = roiHandler.getAnchorPoint(h, hsize);
			oProvider.setAnchorPoints(hid, apt[0], apt[1]);
			oProvider.setStyle(hid, VectorOverlayStyles.FILLED_WITH_OUTLINE);
			oProvider.setColour(hid, oColour);
			oProvider.setOutlineColour(hid, oColour);
			oProvider.setLineThickness(hid, oThickness);
			oProvider.setTransparency(hid, 0.9);
			oProvider.setOutlineTransparency(hid, oTransparency);
		}

		// centre mark handle
		int h = roiHandler.size() - 1;
		int hid = roiHandler.get(h);
		if (hid == -1) {
			hid = oProvider.registerPrimitive(PrimitiveType.CIRCLE, true);
			roiHandler.set(h, hid);
		} else
			oProvider.setPrimitiveVisible(hid, true);

		oProvider.drawCircle(hid, spt[0], spt[1], hsize / 2);
		oProvider.setAnchorPoints(hid, spt[0], spt[1]);
		oProvider.setStyle(hid, VectorOverlayStyles.FILLED_WITH_OUTLINE);
		oProvider.setColour(hid, oColour);
		oProvider.setOutlineColour(hid, oColour);
		oProvider.setLineThickness(hid, oThickness);
		oProvider.setTransparency(hid, 0.9);
		oProvider.setOutlineTransparency(hid, oTransparency);

		oProvider.end(OverlayType.VECTOR2D);
	}

	@Override
	protected void drawOverlays() {
		if (oProvider == null)
			return;

		if (roiDataList.size() == 0)
			return;

		if (roisIDs.size() != roiDataList.size()) {
			logger.warn("Mismatch in number of primitives and ROIs!");
		}

		oProvider.begin(OverlayType.VECTOR2D);

		for (int r = 0, rmax = roiDataList.size(); r < rmax; r++) {
			int id = -1;
			try {
				id = roisIDs.get(r);
			} catch (IndexOutOfBoundsException e) {
				roisIDs.add(r, -1);
			}
			if (id == -1) {
				id = oProvider.registerPrimitive(PrimitiveType.SECTOR);
				roisIDs.set(r, id);
			} else
				oProvider.setPrimitiveVisible(id, true);

			final SectorROI sroi = (SectorROI) roiDataList.get(r).getROI();
			final double[] spt = sroi.getPointRef();
			final double[] rad = sroi.getRadii();
			final double[] ang = sroi.getAnglesDegrees();

			oProvider.drawSector(id, spt[0], spt[1], rad[0], rad[1], -ang[1], -ang[0]);

			oProvider.setColour(id, oColour);
			oProvider.setTransparency(id, oTransparency);
		}

		oProvider.end(OverlayType.VECTOR2D);
	}

	@Override
	public void imageStart(IImagePositionEvent event) {
		HandleStatus hStatus = HandleStatus.NONE;

		if (roi == null) {
			roi = new SectorROI();
			roi.setPlot(true);
			roiHandler.setROI(roi);
			setROIName(roi);
		}

		int id = event.getPrimitiveID();
		short flags = event.getFlags();
		cpt = event.getImagePosition();

		int dragHandle = -1;
		if ((flags & IImagePositionEvent.LEFTMOUSEBUTTON) != 0) {
			if (id == -1 || !roiHandler.contains(id)) {
				if (!lockCentre) {
					// new ROI mode
					roi.setPoint(cpt);
					hideCurrent();
					getControl().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							spsx.setDouble(roi.getPointX());
							spsy.setDouble(roi.getPointY());
						}
					});
					hStatus = HandleStatus.CMOVE;
					oProvider.setPlotAreaCursor(SWT.CURSOR_CROSS);
					dragging = true;
					dragHandle = roiHandler.size() - 1;
				} else {
					hideCurrent();
					hStatus = HandleStatus.RESIZE;
					oProvider.setPlotAreaCursor(SWT.CURSOR_SIZEALL);
					dragging = true;
				}
			} else if (roiHandler.contains(id)) {
				int h = roiHandler.indexOf(id);

				if (h == roiHandler.size() - 1) {
					if (!lockCentre) {
						hStatus = HandleStatus.CMOVE;
						oProvider.setPlotAreaCursor(SWT.CURSOR_CROSS);
					} else
						return;
				} else if (h == 4) {
					if ((flags & IImagePositionEvent.SHIFTKEY) != 0) {
						hStatus = HandleStatus.ROTATE;
						oProvider.setPlotAreaCursor(SWT.CURSOR_APPSTARTING);
					} else {
						hStatus = HandleStatus.RMOVE;
						oProvider.setPlotAreaCursor(SWT.CURSOR_HAND);
					}
				} else {
					if ((flags & IImagePositionEvent.SHIFTKEY) != 0) {
						hStatus = HandleStatus.CRMOVE;
						oProvider.setPlotAreaCursor(SWT.CURSOR_IBEAM);
					} else {
						hStatus = HandleStatus.RESIZE;
						oProvider.setPlotAreaCursor(SWT.CURSOR_SIZEALL);
					}
				}
				hideCurrent();
				drawDraggedOverlay(roi);
				dragging = true;
				dragHandle = h; // store dragged handle
			}
			roiHandler.configureDragging(dragHandle, hStatus);
		} else if ((flags & IImagePositionEvent.RIGHTMOUSEBUTTON) != 0) {
			if (roiHandler.contains(id)) {
				int h = roiHandler.indexOf(id);

				if (h == 4) {
					hStatus = HandleStatus.ROTATE;
					oProvider.setPlotAreaCursor(SWT.CURSOR_APPSTARTING);
				} else if (h != roiHandler.size() - 1) {
					hStatus = HandleStatus.CRMOVE;
					oProvider.setPlotAreaCursor(SWT.CURSOR_IBEAM);
				}
				hideCurrent();
				drawDraggedOverlay(roi);
				dragging = true;
				dragHandle = h; // store dragged handle
				logger.debug("Selected handle {}", h);
				roiHandler.configureDragging(dragHandle, hStatus);
			}
		}
	}

	@Override
	public void imageDragged(IImagePositionEvent event) {
		if (dragging) {
			final IROI croi = roiHandler.interpretMouseDragging(cpt, event.getImagePosition());

			if (croi != null) {
				drawDraggedOverlay(croi);
				if (System.currentTimeMillis() >= nextTime) {
					nextTime = System.currentTimeMillis() + updateInterval;
					updatePlot(croi);
					sendCurrentROI(croi);

					updateAllSpinners(croi);
				}
			}
		}
	}

	@Override
	public void imageFinished(IImagePositionEvent event) {
		if (dragging) {
			dragging = false;
			hideIDs(dragIDs);
			oProvider.restoreDefaultPlotAreaCursor();

			roi = roiHandler.interpretMouseDragging(cpt, event.getImagePosition());
			roiHandler.setROI(roi);
			roiHandler.unconfigureDragging();

			drawCurrentOverlay();
			sendCurrentROI(roi);

			updateAllSpinners(roi);

			updatePlot();
		}
	}

	// more GUI listeners
	private SelectionListener clippingButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final SectorROI sroi = (SectorROI) roi;
            
			if (sroi!=null) {
				if (((Button) e.widget).getSelection()) {
					sroi.setClippingCompensation(true);
				} else {
					sroi.setClippingCompensation(false);
				}
				
				sendCurrentROI(roi);
				
				getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						updatePlot();
					}
				});
			}
		}
	};

	private SelectionListener centreListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Button b = (Button) e.widget;

			if (b == centreLock) {
				if (b.getSelection()) {
					lockCentre = true;
					spsx.setEnabled(false);
					spsy.setEnabled(false);
					centreReset.setEnabled(false);
					centreCentroid.setEnabled(false);
					spsx.removeSelectionListener(spinnerListener);
					spsy.removeSelectionListener(spinnerListener);
				} else {
					lockCentre = false;
					spsx.addSelectionListener(spinnerListener);
					spsy.addSelectionListener(spinnerListener);
					spsx.setEnabled(true);
					spsy.setEnabled(true);
					centreReset.setEnabled(true);
					centreCentroid.setEnabled(true);
				}
			} else if (b == centreReset) {
				if (!lockCentre) {
					getDataset();
					int[] size = data.getShape();

					roi.setPoint(size[1] / 2, size[0] / 2);
					updateAllSpinners(roi);
					sendCurrentROI(roi);
				}
			} else if (b == centreCentroid) {
				if (!lockCentre) {
					getDataset();
					// calculate centroid
					Centroid cen = new Centroid();
					List<Double> csets = cen.value(data);
					roi.setPoint(csets.get(1), csets.get(0));
					updateAllSpinners(roi);
					sendCurrentROI(roi);
				}
			}

			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					drawCurrentOverlay();
					updatePlot();
				}
			});
		}
	};

	/**
	 * Validate and set symmetry attribute
	 * 
	 * @param btn
	 * @return false if symmetry is not compatible with ROI
	 */
	private boolean validateSymButtons(Button btn) {
		int sym = radioButtons.lastIndexOf(btn);
		SectorROI sroi = (SectorROI) roi;
        if (sroi!=null) {
			if (sroi.checkSymmetry(sym)) {
				sroi.setSymmetry(sym);
				sendCurrentROI(roi);
				return true;
			}
        }
		return false;
	}

	private SelectionListener radioListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final SectorROI sroi = (SectorROI) roi;
			Button btn = (Button) e.widget;

			if (!btn.getSelection()) {
				btn.setSelection(false);
				return;
			}

			if (validateSymButtons(btn)) {
				btn.setSelection(true);
			} else {
				radioButtons.get(sroi.getSymmetry()).setSelection(true);
				btn.setSelection(false);
			}
			if (sroi.getSymmetry() == SectorROI.NONE || sroi.getSymmetry() == SectorROI.FULL) {
				sroi.setCombineSymmetry(false);
				combine.setSelection(false);
				combine.setEnabled(false);
			} else {
				combine.setEnabled(true);
			}

			sendCurrentROI(roi);
			
			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					drawCurrentOverlay();
					updatePlot();
				}
			});
		}
	};

	private void setRadioButtons(final SectorROI sroi) {
		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (sroi == null) {
					return;
				}
				for (Button b : radioButtons) {
					b.setEnabled(sroi.checkSymmetry(radioButtons.indexOf(b)));
				}
			}
		});
	}

	private SelectionListener combineButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final SectorROI sroi = (SectorROI) roi;
			Button btn = (Button) e.widget;

			if (btn.getSelection()) {
				if (sroi.getSymmetry() == SectorROI.NONE || sroi.getSymmetry() == SectorROI.FULL) {
					btn.setSelection(false);
					return;
				}
				sroi.setCombineSymmetry(true);
			} else {
				sroi.setCombineSymmetry(false);
			}
			
			sendCurrentROI(roi);
			
			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					drawCurrentOverlay();
					updatePlot();
				}
			});
		}
	};

	@Override
	protected void updateAllSpinners(final IROI roib) {
		if (roib == null)
			return;

		final SectorROI sroi = (SectorROI) roib;

		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				updateAllSpinnersInt(sroi);
			}
		});
	}

	protected void updateAllSpinnersInt(final SectorROI sroi) {
		if (sroi == null)
			return;

		isBulkUpdate = true;
		spsx.setDouble(sroi.getPointX());
		spsy.setDouble(sroi.getPointY());
		spsr.setDouble(sroi.getRadius(0));
		sper.setDouble(sroi.getRadius(1));
		spsang.setDouble(sroi.getAngleDegrees(0));
		isBulkUpdate = false;
		speang.setDouble(sroi.getAngleDegrees(1));
	}

	// spinner listener
	private SelectionListener spinnerListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final SectorROI sroi = (SectorROI) roi;

			if (sroi!=null) {
				sroi.setPoint(spsx.getDouble(), spsy.getDouble());
				sroi.setRadii(spsr.getDouble(), sper.getDouble());
				sroi.setAnglesDegrees(spsang.getDouble(), speang.getDouble());

				if (!isBulkUpdate)
					sendCurrentROI(roi);

				Job spinnerJob = new Job(spinnerJobName) {
					
					@Override
					public IStatus run(IProgressMonitor monitor) {
						updateAllSpinners(roi); // in case of branch cut
						
						if (isBulkUpdate)
							return Status.OK_STATUS;

						getControl().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								updatePlot();
								drawCurrentOverlay();
							}
						});
						return Status.OK_STATUS;
					}
					
					@Override
					public boolean belongsTo(Object family) {
						return family == spinnerJobName;
					}
				};
				
				spinnerJob.setPriority(Job.LONG);
				spinnerJob.setSystem(true);
				if (jobManager.find(spinnerJobName).length > 5) {
					jobManager.cancel(spinnerJobName);
					spinnerJob.schedule();
				} else {
					spinnerJob.schedule(2000);
				}
			}
		}
	};

	// guide ring spinner listener
	private SelectionListener grSpinnerListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			grNumber = spgrn.getSelection();
			grSpacing = spgrs.getSelection();
			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					drawCurrentOverlay();
				}
			});
		}
	};

	@Override
	public SectorROIList createROIList() {
		SectorROIList list = new SectorROIList();
		if (roiDataList != null) {
			for (ROIData rd: roiDataList) {
				list.add((SectorROI) rd.getROI());
			}
		}
		return list;
	}

	@Override
	public ROIData createNewROIData(IROI roi) {
		return new SectorROIData((SectorROI) roi, data, mask);
	}

	@Override
	public Action createSwitchAction(final int index, final IPlotUI plotUI) {
		Action action = super.createSwitchAction(index, plotUI);
		action.setId("uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot.SectorProfileAction");
		action.setText("Sector profile");
		action.setToolTipText("Switch side plot to sector profile mode");
		action.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/ProfileSector.png"));

		return action;
	}

	@Override
	public void addToHistory() {
		Plot1DAppearance plotApp = new Plot1DAppearance(PlotColorUtility.getDefaultColour(radPlotter.getColourTable()
				.getLegendSize()), Plot1DStyles.SOLID, "History " + radPlotter.getNumHistory());
		radPlotter.getColourTable().addEntryOnLegend(plotApp);
		radPlotter.pushGraphOntoHistory();

		plotApp = new Plot1DAppearance(PlotColorUtility.getDefaultColour(aziPlotter.getColourTable().getLegendSize()),
				Plot1DStyles.SOLID, "History " + aziPlotter.getNumHistory());
		aziPlotter.getColourTable().addEntryOnLegend(plotApp);
		aziPlotter.pushGraphOntoHistory();
	}

	@Override
	public void removeFromHistory() {
		if (radPlotter.getNumHistory() > 0) {
			radPlotter.getColourTable().deleteLegendEntry(radPlotter.getColourTable().getLegendSize() - 1);
			radPlotter.popGraphFromHistory();
			radPlotter.refresh(true);
		}
		if (aziPlotter.getNumHistory() > 0) {
			aziPlotter.getColourTable().deleteLegendEntry(aziPlotter.getColourTable().getLegendSize() - 1);
			aziPlotter.popGraphFromHistory();
			aziPlotter.refresh(true);
		}
	}

	@Override
	DataBean getPlottingData(int profileNr) {
		DataBean dBean = null;
		
		if (roiData != null && roiData.getProfileData().length > profileNr) {
			dBean = new DataBean(GuiPlotMode.ONED);
			DatasetWithAxisInformation axisData = new DatasetWithAxisInformation();
			AxisMapBean axisMapBean = new AxisMapBean();

			dBean.addAxis(AxisMapBean.XAXIS, roiData.getXAxis(profileNr).toDataset());
			axisMapBean.setAxisID(new String[] {AxisMapBean.XAXIS});
			axisData.setData(roiData.getProfileData(profileNr));
			axisData.setAxisMap(axisMapBean);

			try {
				dBean.addData(axisData);
			} catch (DataBeanException e) {
				logger.debug("Could not add data to bean");
				e.printStackTrace();
				dBean = null;
			}
		}
		return dBean;
	}
	
	@Override
	public void generateToolActions(IToolBarManager manager) {
		createExportActions();
		createHistoryActions();
		createPushPlotActions();
		
		manager.add(new Separator(getClass().getName()+printButtonText));
		manager.add(saveGraph);
		manager.add(copyGraph);
		manager.add(printGraph);
		manager.add(new Separator(getClass().getName()+"pushHistoryActions"));
		manager.add(addtoHistory);
		manager.add(removefromHistory);
		manager.add(new Separator(getClass().getName()+"pushPlotActions"));
		manager.add(pushPlottingData);
	}
	
	@Override
	public void generateMenuActions(IMenuManager manager,final IWorkbenchPartSite site) {
		createExportActions();
		createHistoryActions();
		createPushPlotActions();
		
		manager.add(new Separator(getClass().getName()+printButtonText));
		saveMenu = new MenuManager(saveButtonText, AnalysisRCPActivator.getImageDescriptor(saveImagePath), saveButtonText);
		saveMenu.add(saveRad);
		saveMenu.add(saveAzi);
		manager.add(saveMenu);
		copyMenu = new MenuManager(copyButtonText, AnalysisRCPActivator.getImageDescriptor(copyImagePath), copyButtonText);
		copyMenu.add(copyRad);
		copyMenu.add(copyAzi);
		manager.add(copyMenu);
		printMenu = new MenuManager(printButtonText, AnalysisRCPActivator.getImageDescriptor(printImagePath), printButtonText);
		printMenu.add(printRad);
		printMenu.add(printAzi);
		manager.add(printMenu);
		manager.add(new Separator(getClass().getName()+"pushHistoryActions"));
		manager.add(addtoHistory);
		manager.add(removefromHistory);
		manager.add(new Separator(getClass().getName()+"pushPlotActions"));
		manager.add(pushRadialPlottingDataPlot1);
		manager.add(pushAzimuthPlottingDataPlot1);	
		manager.add(pushRadialPlottingDataPlot2);
		manager.add(pushAzimuthPlottingDataPlot2);	

	}
		
	private void createExportActions(){
		saveRad = new Action("Save radial datasets plot") {
			private String filename;
			@Override
			public void run() {
				FileDialog dialog = new FileDialog (parent.getShell(), SWT.SAVE);
				String [] filterExtensions = new String [] {"*.jpg;*.JPG;*.jpeg;*.JPEG;*.png;*.PNG", "*.ps;*.eps","*.svg;*.SVG"};
				if (filename!=null) {
					dialog.setFilterPath((new File(filename)).getParent());
				} else {
					String filterPath = "/";
					String platform = SWT.getPlatform();
					if (platform.equals("win32") || platform.equals("wpf")) {
						filterPath = "c:\\";
					}
					dialog.setFilterPath (filterPath);
				}
				dialog.setFilterNames (PlotExportUtil.FILE_TYPES);
				dialog.setFilterExtensions (filterExtensions);
				filename = dialog.open();
				if (filename == null)
					return;
				radPlotter.saveGraph(filename, PlotExportUtil.FILE_TYPES[dialog.getFilterIndex()]);
			}
		};
		saveAzi = new Action("Save azimuth datasets plot") {
			private String filename;
			@Override
			public void run() {
				FileDialog dialog = new FileDialog (parent.getShell(), SWT.SAVE);
				String [] filterExtensions = new String [] {"*.jpg;*.JPG;*.jpeg;*.JPEG;*.png;*.PNG", "*.ps;*.eps","*.svg;*.SVG"};
				if (filename!=null) {
					dialog.setFilterPath((new File(filename)).getParent());
				} else {
					String filterPath = "/";
					String platform = SWT.getPlatform();
					if (platform.equals("win32") || platform.equals("wpf")) {
						filterPath = "c:\\";
					}
					dialog.setFilterPath (filterPath);
				}
				dialog.setFilterNames (PlotExportUtil.FILE_TYPES);
				dialog.setFilterExtensions (filterExtensions);
				filename = dialog.open();
				if (filename == null)
					return;
				aziPlotter.saveGraph(filename, PlotExportUtil.FILE_TYPES[dialog.getFilterIndex()]);
			}
		};
		saveGraph = new DropDownAction();
		saveGraph.setText(saveButtonText);
		saveGraph.setToolTipText(saveToolTipText);
		saveGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(saveImagePath));
		saveGraph.add(saveRad);
		saveGraph.add(saveAzi);
		
		copyRad = new Action("Copy radial datasets plot"){
			@Override
			public void run(){
				radPlotter.copyGraph();
			}
		};
		copyAzi = new Action("Copy azimuth datasets plot") {
			@Override
			public void run() {
				aziPlotter.copyGraph();
			}
		};
		copyGraph = new DropDownAction();
		copyGraph.setText(copyButtonText);
		copyGraph.setToolTipText(copyToolTipText);
		copyGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(copyImagePath));
		copyGraph.add(copyRad);
		copyGraph.add(copyAzi);
		
		printRad = new Action("Print radial datasets plot"){
			@Override
			public void run(){
				radPlotter.printGraph();
			}
		};
		printAzi = new Action("Print azimuth datasets plot") {
			@Override
			public void run() {
				aziPlotter.printGraph();
			}
		};
		printGraph = new DropDownAction();
		printGraph.setToolTipText(printToolTipText);
		printGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(printImagePath));
		printGraph.add(printRad);
		printGraph.add(printAzi);

	}
	
	private void createHistoryActions(){
		addtoHistory = new Action() {
			@Override
			public void run() {
				addToHistory();
			}
		};
		addtoHistory.setText("Add current profiles to history");
		addtoHistory.setToolTipText("Adds the current profiles to the plot history");
		addtoHistory.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/basket_put.png"));

		removefromHistory = new Action() {
			@Override
			public void run() {
				removeFromHistory();
			}
		};
		removefromHistory.setText("Remove last profiles from history");
		removefromHistory.setToolTipText("Remove the last profiles from the plot history");
		removefromHistory.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/basket_remove.png"));
	}

	private void createPushPlotActions() {
		final String fullPlotID = "uk.ac.diamond.scisoft.analysis.rcp.plotView";
		final IWorkbenchPartSite site = EclipseUtils.getPage().getActivePart().getSite();
		final org.eclipse.swt.graphics.Image icon = SWTResourceManager.getImage(StaticScanPlotView.class,"/icons/chart_curve_add.png");
		final ImageDescriptor d = ImageDescriptor.createFromImage(icon);
		pushRadialPlottingDataPlot1 = new Action() {
			@Override
			public void run() {
					pushPlottingData(site, fullPlotID+"1",0);

			}
		};
		pushRadialPlottingDataPlot1.setText("Push radial datasets to plot 1");
		pushRadialPlottingDataPlot1.setImageDescriptor(d);
		pushRadialPlottingDataPlot1.setToolTipText("Push radial datasets to plot 1");
		pushAzimuthPlottingDataPlot1 = new Action() {
			@Override
			public void run() {
					pushPlottingData(site, fullPlotID+"1",1);

			}
		};
		pushAzimuthPlottingDataPlot1.setText("Push azimuth datasets to plot 1");
		pushAzimuthPlottingDataPlot1.setImageDescriptor(d);
		pushAzimuthPlottingDataPlot1.setToolTipText("Push azimuth datasets to plot 1");
		pushRadialPlottingDataPlot2 = new Action() {
			@Override
			public void run() {
					pushPlottingData(site, fullPlotID+"2",0);

			}
		};
		pushRadialPlottingDataPlot2.setText("Push radial datasets to plot 2");
		pushRadialPlottingDataPlot2.setImageDescriptor(d);
		pushRadialPlottingDataPlot2.setToolTipText("Push radial datasets to plot 2");
		pushAzimuthPlottingDataPlot2 = new Action() {
			@Override
			public void run() {
					pushPlottingData(site, fullPlotID+"2",1);

			}
		};
		pushAzimuthPlottingDataPlot2.setText("Push azimuth datasets to plot 2");
		pushAzimuthPlottingDataPlot2.setImageDescriptor(d);
		pushAzimuthPlottingDataPlot2.setToolTipText("Push azimuth datasets to plot 2");
		
		pushPlottingData = new DropDownAction();
		pushPlottingData.setToolTipText("Push plotting to plot 1 or 2");
		pushPlottingData.setImageDescriptor(d);
		pushPlottingData.add(pushRadialPlottingDataPlot1);
		pushPlottingData.add(pushAzimuthPlottingDataPlot1);
		pushPlottingData.add(pushRadialPlottingDataPlot2);
		pushPlottingData.add(pushAzimuthPlottingDataPlot2);
	}
}
