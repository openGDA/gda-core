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
import org.dawb.common.ui.plot.roi.data.RectangularROIData;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.jreality.print.PlotExportUtil;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROIList;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.HandleStatus;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.RectangularROIHandler;
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
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.DropDownAction;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.ROIDataList;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.RectangularROITableViewer;
import uk.ac.diamond.scisoft.analysis.rcp.queue.InteractiveJobAdapter;
import uk.ac.diamond.scisoft.analysis.rcp.util.FloatSpinner;
import uk.ac.diamond.scisoft.analysis.rcp.util.ResourceProperties;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.StaticScanPlotView;

import com.swtdesigner.SWTResourceManager;

@Deprecated
public class BoxProfile extends SidePlotProfile {

	private static Logger logger = LoggerFactory.getLogger(BoxProfile.class);

	private DataSetPlotter majPlotter;
	private DataSetPlotter minPlotter;

	private Composite parent;
	private DropDownAction saveGraph;
	private DropDownAction copyGraph;
	private DropDownAction printGraph;
	private Action saveMin;
	private Action saveMaj;
	private Action copyMin;
	private Action copyMaj;
	private Action printMaj;
	private Action printMin;
	private MenuManager saveMenu;
	private MenuManager copyMenu;
	private MenuManager printMenu;
	private DropDownAction pushPlottingData;
	private Action pushMajorPlottingDataPlot1;
	private Action pushMajorPlottingDataPlot2;
	private Action pushMinorPlottingDataPlot1;
	private Action pushMinorPlottingDataPlot2;
	private Action removefromHistory;
	private Action addtoHistory;
	private String printButtonText = ResourceProperties.getResourceString("PRINT_BUTTON");
	private String printToolTipText = ResourceProperties.getResourceString("PRINT_TOOLTIP");
	private String printImagePath = ResourceProperties.getResourceString("PRINT_IMAGE_PATH");
	private String copyButtonText = ResourceProperties.getResourceString("COPY_BUTTON");
	private String copyToolTipText = ResourceProperties.getResourceString("COPY_TOOLTIP");
	private String copyImagePath = ResourceProperties.getResourceString("COPY_IMAGE_PATH");
	private String saveButtonText = ResourceProperties.getResourceString("SAVE_BUTTON");
	private String saveToolTipText = ResourceProperties.getResourceString("SAVE_TOOLTIP");
	private String saveImagePath = ResourceProperties.getResourceString("SAVE_IMAGE_PATH");

	private Spinner spsx, spsy;
	private FloatSpinner splmaj, splmin, spang;
	private Text txSum;

	private class BoxJob extends InteractiveJobAdapter {
		private RectangularROI rroi = null;
		private boolean subsample;

		public BoxJob(RectangularROI rroi, boolean quick) {
			this.rroi = rroi;
			subsample = quick;
		}

		@Override
		public void run(IProgressMonitor monitor) {
			if (isNull())
				return;

			if (monitor != null) monitor.worked(1);

			if (subsample)
				roiData = new RectangularROIData(rroi, subData, mask, subFactor);
			else {
				if (oProvider!=null) oProvider.setPlotAreaCursor(SWT.CURSOR_WAIT);

				roiData = new RectangularROIData(rroi, data, mask);

				if (oProvider!=null) oProvider.restoreDefaultPlotAreaCursor();
			}
			if (monitor != null) {
				if (monitor.isCanceled())
					return;

				monitor.worked(1);
			}

			if (roiData.isPlot())
				drawPlots(rroi);

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
			return rroi == null;
		}
	}

	public BoxProfile() {
		super();
		roiClass = RectangularROI.class;
		roiListClass = RectangularROIList.class;
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

		majPlotter = new SidePlotter1D(ss, "Major integrated profile");
		majPlotter.setAxisModes(AxisMode.CUSTOM, AxisMode.LINEAR, AxisMode.LINEAR);
		majPlotter.setXAxisLabel("Distance along major axis");

		minPlotter = new SidePlotter1D(ss, "Minor integrated profile");
		minPlotter.setAxisModes(AxisMode.CUSTOM, AxisMode.LINEAR, AxisMode.LINEAR);
		minPlotter.setXAxisLabel("Distance along minor axis");

		// GUI creation and layout
		final ScrolledComposite scomp = new ScrolledComposite(ss, SWT.VERTICAL | SWT.HORIZONTAL);

		final Composite controls = new Composite(scomp, SWT.NONE);
		controls.setLayout(new FillLayout(SWT.VERTICAL));
		{
			final Group groupCurrent = new Group(controls, SWT.NONE);
			groupCurrent.setLayout(new GridLayout(6, false));
			groupCurrent.setText("Current ROI");
			{
				// 1st row
				new Label(groupCurrent, SWT.NONE).setText("Start x:");
				spsx = new Spinner(groupCurrent, SWT.BORDER);
				spsx.setMinimum(-10000);
				spsx.setMaximum(10000);
				spsx.setIncrement(1);
				spsx.setPageIncrement(5);
				spsx.addSelectionListener(startPosListener);

				new Label(groupCurrent, SWT.NONE).setText("Length major:");
				splmaj = new FloatSpinner(groupCurrent, SWT.BORDER, 6, 2);
				splmaj.addSelectionListener(lensListener);

				new Label(groupCurrent, SWT.NONE).setText("Angle:");
				spang = new FloatSpinner(groupCurrent, SWT.BORDER, 7, 2);
				spang.setMinimum(0.0);
				spang.setMaximum(360.0);
				spang.addSelectionListener(angListener);

				// 2nd row
				new Label(groupCurrent, SWT.NONE).setText("Start y:");
				spsy = new Spinner(groupCurrent, SWT.BORDER);
				spsy.setMinimum(-10000);
				spsy.setMaximum(10000);
				spsy.setIncrement(1);
				spsy.setPageIncrement(5);
				spsy.addSelectionListener(startPosListener);

				new Label(groupCurrent, SWT.NONE).setText("Length minor:");
				splmin = new FloatSpinner(groupCurrent, SWT.BORDER, 6, 2);
				splmin.addSelectionListener(lensListener);

				new Label(groupCurrent, SWT.NONE).setText("Sum:");
				txSum = new Text(groupCurrent, SWT.READ_ONLY | SWT.BORDER);
				txSum.setTextLimit(12);

				// 3rd row
				GridData gda = new GridData();
				gda.horizontalSpan = 2;
				final Button invert = new Button(groupCurrent, SWT.CHECK);
				invert.setLayoutData(gda);
				invert.setText("Invert brightness");
				invert.setToolTipText("Invert overlay brightness");
				invert.addSelectionListener(brightnessButtonListener);

				GridData gdb = new GridData();
				gdb.horizontalSpan = 2;
				final Button clip = new Button(groupCurrent, SWT.CHECK);
				clip.setLayoutData(gdb);
				clip.setText("Clipping comp");
				clip.setToolTipText("Compensate for clipping of region of interests");
				clip.addSelectionListener(clippingButtonListener);

				new Label(groupCurrent, SWT.NONE).setText("");
				new Label(groupCurrent, SWT.NONE).setText("");

				// 4th row
				GridData gdc = new GridData();
				gdc.horizontalSpan = 2;
				final Button copyToTable = new Button(groupCurrent, SWT.PUSH);
				copyToTable.setLayoutData(gdc);
				copyToTable.setText("Copy current to table");
				copyToTable.addSelectionListener(copyButtonListener);

				GridData gdd = new GridData();
				gdd.horizontalSpan = 2;
				Button deleteCurrent = new Button(groupCurrent, SWT.PUSH);
				deleteCurrent.setLayoutData(gdd);
				deleteCurrent.setText("Delete current");
				deleteCurrent.addSelectionListener(deleteButtonListener);
			}
		}

		final Composite table = new Composite(ss, SWT.NONE);
		ss.setWeights(new int[] {25, 25, 30, 20});

		table.setLayout(new FillLayout());

		tViewer = new RectangularROITableViewer(table, this, this);

		scomp.setContent(controls);
		controls.setSize(controls.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		// end of GUI creation

		majPlotter.refresh(false);
		minPlotter.refresh(false);

		// initialize ROI and data
		updateAllSpinnersInt((RectangularROI) roi);
		roiIDs = new ArrayList<Integer>();
		dragIDs = new ArrayList<Integer>();

		// initialize ROIs
		if (roiDataList == null)
			roiDataList = new ROIDataList();
		roisIDs = new ArrayList<Integer>();

		tViewer.setInput(roiDataList);

		// handle areas
		roiHandler = new RectangularROIHandler((RectangularROI) roi);

		// default colour: green
		dColour = new Color(0, 255, 0);

		// invert overlay colour
		float[] hsb;
		hsb = Color.RGBtoHSB(dColour.getRed(), dColour.getGreen(), dColour.getBlue(), null);
		cColour = Color.getHSBColor(hsb[0], hsb[1], (float) (1.0 - 0.7 * hsb[2]));
		oColour = dColour;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (majPlotter != null) majPlotter.cleanUp();
		if (minPlotter != null) minPlotter.cleanUp();
	}

	@Override
	protected void updatePlot(IROI roib) {
		final RectangularROI rroi = (RectangularROI) roib;

		getDataset();

		if (data == null) {
			logger.warn("No data");
			return;
		}

		if (rroi != null) {
			if (dragging && subData != null) {
				try {
					final BoxJob obj = new BoxJob(rroi, true);
					roiQueue.addJob(obj);
				} catch (Exception e) {
					logger.error("Cannot generate ROI data", e);
				}
			} else {
				try {
					final BoxJob obj = new BoxJob(rroi, false);
					roiQueue.addJob(obj);
				} catch (Exception e) {
					logger.error("Cannot generate ROI data", e);
				}
			}
		}
	}

	private void drawPlots(RectangularROI rroi) {
		Plot1DGraphTable colourTable;
		Plot1DAppearance newApp;
		int p, l;
		int nHistory;
		List<IDataset> plots = new ArrayList<IDataset>();
		List<AxisValues> paxes = new ArrayList<AxisValues>();

		l = p = 0;
		colourTable = majPlotter.getColourTable();
		nHistory = majPlotter.getNumHistory();

		if (rroi != null) {
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
			l++;
			p++;
		}

		for (int i = 0, imax = roiDataList.size(); i < imax; i++) {
			RectangularROIData rd = (RectangularROIData) roiDataList.get(i);
			Color colour = PlotColorUtility.getDefaultColour(i+1);
			RGB rgb = new RGB(colour.getRed(), colour.getGreen(), colour.getBlue());
			rd.setPlotColourRGB(rgb);

			if (rd.isPlot()) {
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
				l++;
				p++;
			}
		}

		while (nHistory-- > 0) { // tidy up history colours
			newApp = colourTable.getLegendEntry(l++);
			newApp.setColour(PlotColorUtility.getDefaultColour(p++));
		}

		try {
			majPlotter.replaceAllPlots(plots, paxes);
		} catch (PlotException e) {
			e.printStackTrace();
		}

		plots.clear();
		paxes.clear();

		l = p = 0;
		colourTable = minPlotter.getColourTable();
		nHistory = minPlotter.getNumHistory();
		if (rroi != null) {
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
			l++;
			p++;
		}

		for (int i = 0, imax = roiDataList.size(); i < imax; i++) {
			RectangularROIData rd = (RectangularROIData) roiDataList.get(i);

			if (rd.isPlot()) {
				plots.add(rd.getProfileData(1));
				paxes.add(rd.getXAxis(1));
				if (p >= colourTable.getLegendSize()) {
					newApp = new Plot1DAppearance(rd.getPlotColour(), Plot1DStyles.SOLID, "Line " + (p + 1));
					colourTable.addEntryOnLegend(l, newApp);
				} else {
					newApp = colourTable.getLegendEntry(l);
					newApp.setColour(rd.getPlotColour());
					newApp.setStyle(Plot1DStyles.SOLID);
					newApp.setName("Line " + (p + 1));
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
			minPlotter.replaceAllPlots(plots, paxes);
		} catch (PlotException e) {
			e.printStackTrace();
		}

		majPlotter.updateAllAppearance();
		majPlotter.refresh(false);
		minPlotter.updateAllAppearance();
		minPlotter.refresh(false);
	}

	/**
	 * Draw dragged out overlay for given region of interest
	 * @param roib
	 */
	private void drawDraggedOverlay(IROI roib) {
		if (oProvider == null)
			return;

		if (dragIDs.isEmpty()) {
			dragIDs.add(-1);
		}

		int id, index;
		index = 0;

		// box
		id = dragIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.BOX);
			dragIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		final RectangularROI rroi = (RectangularROI) roib;
		final double[] spt = rroi.getPointRef();
		final double[] len = rroi.getLengths();

		oProvider.begin(OverlayType.VECTOR2D);

		oProvider.drawBox(id, spt[0], spt[1], spt[0] + len[0], spt[1] + len[1]);
		oProvider.rotatePrimitive(id, -rroi.getAngle(), spt[0], spt[1]);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);

		oProvider.end(OverlayType.VECTOR2D);
	}

	@Override
	protected void drawCurrentOverlay() {
		if (oProvider == null || roi == null)
			return;

		if (roiIDs.isEmpty()) {
			roiIDs.add(-1); // box
			roiIDs.add(-1); // major
			roiIDs.add(-1); // minor
		}

		int id, index;
		index = 0;

		final RectangularROI rroi = (RectangularROI) roi;
		final double[] spt = roi.getPointRef();
		final double[] len = rroi.getLengths();

		oProvider.begin(OverlayType.VECTOR2D);

		// box
		id = roiIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.BOX);
			roiIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		oProvider.drawBox(id, spt[0], spt[1], spt[0] + len[0], spt[1] + len[1]);
		oProvider.rotatePrimitive(id, -rroi.getAngle(), spt[0], spt[1]);
		oProvider.setColour(id, oColour);
//		oProvider.setOutlineColour(id, oColour);
//		oProvider.setLineThickness(id, oThickness);
		oProvider.setTransparency(id, 0.9);
//		oProvider.setOutlineTransparency(id, oTransparency);
		oProvider.setStyle(id, VectorOverlayStyles.FILLED);

		// major axis
		id = roiIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.ARROW);
			roiIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		oProvider.drawArrow(id, spt[0], spt[1], spt[0] + len[0], spt[1], 2./3);
		oProvider.rotatePrimitive(id, -rroi.getAngle(), spt[0], spt[1]);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);

		// minor axis
		id = roiIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.ARROW);
			roiIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		oProvider.drawArrow(id, spt[0], spt[1], spt[0], spt[1] + len[1], 1./3);
		oProvider.rotatePrimitive(id, -rroi.getAngle(), spt[0], spt[1]);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);

		// image size dependent handle size
		getDataset();
		int hsize = calcHandleSize(data.getShape());

		// handle areas
		for (int h = 0, hmax = roiHandler.size(); h < hmax; h++) {
			int hid = roiHandler.get(h);
			if (hid == -1) {
				hid = oProvider.registerPrimitive(PrimitiveType.BOX, true);
				roiHandler.set(h, hid);
			} else
				oProvider.setPrimitiveVisible(hid, true);

			double[] pt = roiHandler.getHandlePoint(h, hsize);
			if (pt == null) {
				oProvider.setPrimitiveVisible(hid, false);
				continue;
			}
			oProvider.drawBox(hid, pt[0], pt[1], pt[0] + hsize, pt[1] + hsize);
			pt = roiHandler.getAnchorPoint(h, hsize);
			oProvider.rotatePrimitive(hid, -rroi.getAngle(), pt[0], pt[1]);
			oProvider.setAnchorPoints(hid, pt[0], pt[1]);
			oProvider.setStyle(hid, VectorOverlayStyles.FILLED_WITH_OUTLINE);
			oProvider.setColour(hid, oColour);
			oProvider.setOutlineColour(hid, oColour);
			oProvider.setLineThickness(hid, oThickness);
			oProvider.setTransparency(hid, 0.9);
			oProvider.setOutlineTransparency(hid, oTransparency);
		}

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
				id = oProvider.registerPrimitive(PrimitiveType.BOX);
				roisIDs.set(r, id);
			} else
				oProvider.setPrimitiveVisible(id, true);
			final RectangularROI rroi = (RectangularROI) roiDataList.get(r).getROI();
			final double[] spt = rroi.getPointRef();
			final double[] len = rroi.getLengths();

			oProvider.drawBox(id, spt[0], spt[1], spt[0] + len[0], spt[1] + len[1]);
			oProvider.rotatePrimitive(id, -rroi.getAngle(), spt[0], spt[1]);

			oProvider.setColour(id, oColour);
			oProvider.setTransparency(id, oTransparency);
		}

		oProvider.end(OverlayType.VECTOR2D);
	}

	@Override
	public void imageStart(IImagePositionEvent event) {
		HandleStatus hStatus = HandleStatus.NONE;

		if (roi == null) {
			roi = new RectangularROI(10, 0);
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
				// new ROI mode
				roi.setPoint(cpt);
				if (oProvider!=null) oProvider.setPlotAreaCursor(SWT.CURSOR_CROSS);
				hideCurrent();
				getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						spsx.setSelection(roi.getIntPoint()[0]);
						spsy.setSelection(roi.getIntPoint()[1]);
					}
				});
				hStatus = HandleStatus.RESIZE;
				dragging = true;
			} else if (roiHandler.contains(id)) {
				int h = roiHandler.indexOf(id);

				if (h != 4) {
					if ((flags & IImagePositionEvent.SHIFTKEY) != 0) {
						if ((h % 2) == 1) {
							hStatus = HandleStatus.ROTATE;
							oProvider.setPlotAreaCursor(SWT.CURSOR_APPSTARTING);
						} else {
							hStatus = HandleStatus.REORIENT;
							oProvider.setPlotAreaCursor(SWT.CURSOR_IBEAM);
						}
					} else {
						hStatus = HandleStatus.RESIZE;
						oProvider.setPlotAreaCursor(SWT.CURSOR_SIZEALL);
					}
				} else if (h == 4) {
					if ((flags & IImagePositionEvent.SHIFTKEY) != 0) {
						hStatus = HandleStatus.ROTATE;
						oProvider.setPlotAreaCursor(SWT.CURSOR_APPSTARTING);
					} else {
						hStatus = HandleStatus.RMOVE;
						oProvider.setPlotAreaCursor(SWT.CURSOR_HAND);
					}
				}
				hideCurrent();
				drawDraggedOverlay(roi);
				dragging = true;
				dragHandle = h; // store dragged handle
				logger.debug("Selected handle {}", h);
			}
			roiHandler.configureDragging(dragHandle, hStatus);
		} else if ((flags & IImagePositionEvent.RIGHTMOUSEBUTTON) != 0) {
			if (roiHandler.contains(id)) {
				int h = roiHandler.indexOf(id);

				if (h != 4) {
					if ((h % 2) == 1) {
						hStatus = HandleStatus.ROTATE;
						oProvider.setPlotAreaCursor(SWT.CURSOR_APPSTARTING);
					} else {
						hStatus = HandleStatus.REORIENT;
						oProvider.setPlotAreaCursor(SWT.CURSOR_IBEAM);
					}
				} else if (h == 4) {
					hStatus = HandleStatus.ROTATE;
					oProvider.setPlotAreaCursor(SWT.CURSOR_APPSTARTING);
				}
				hideCurrent();
				drawDraggedOverlay(roi);
				dragging = true;
				dragHandle = h; // store dragged handle
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
			final RectangularROI rroi = (RectangularROI) roi;

			if (((Button) e.widget).getSelection()) {
				rroi.setClippingCompensation(true);
			} else {
				rroi.setClippingCompensation(false);
			}
			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updatePlot();
				}
			});
		}
	};

	@Override
	protected void updateAllSpinners(IROI roib) {
		if (roib == null)
			return;

		final RectangularROI rroi = (RectangularROI) roib;

		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				updateAllSpinnersInt(rroi);
			}
		});
	}

	private void updateAllSpinnersInt(final RectangularROI rroi) {
		if (rroi == null)
			return;
		isBulkUpdate = true;
		spsx.setSelection(rroi.getIntPoint()[0]);
		spsy.setSelection(rroi.getIntPoint()[1]);
		splmaj.setDouble(rroi.getLengths()[0]);
		splmin.setDouble(rroi.getLengths()[1]);
		isBulkUpdate = false;
		spang.setDouble(rroi.getAngleDegrees());
	}

	// spinner listeners
	private SelectionListener startPosListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			roi.setPoint(spsx.getSelection(), spsy.getSelection());

			if (isBulkUpdate)
				return;

			sendCurrentROI(roi);

			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updatePlot();
					drawCurrentOverlay();
				}
			});
		}
	};

	private SelectionListener lensListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final RectangularROI rroi = (RectangularROI) roi;
			if (rroi!=null) {
				rroi.setLengths(splmaj.getDouble(), splmin.getDouble());

				if (!isBulkUpdate)
					sendCurrentROI(roi);
	
				getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						splmaj.setDouble(rroi.getLengths()[0]);
						splmin.setDouble(rroi.getLengths()[1]);
						if (isBulkUpdate)
							return;
						updatePlot();
						drawCurrentOverlay();
					}
				});
			}
		}
	};

	private SelectionListener angListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final RectangularROI rroi = (RectangularROI) roi;
			if (rroi != null) {
				rroi.setAngleDegrees(spang.getDouble());

				if (!isBulkUpdate)
					sendCurrentROI(roi);
	
				getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (isBulkUpdate)
							return;
						updatePlot();
						drawCurrentOverlay();
					}
				});
			}
		}
	};

	@Override
	public RectangularROIList createROIList() {
		RectangularROIList list = new RectangularROIList();
		if (roiDataList != null) {
			for (ROIData rd: roiDataList) {
				list.add((RectangularROI) rd.getROI());
			}
		}
		return list;
	}

	@Override
	public ROIData createNewROIData(IROI roi) {
		return new RectangularROIData((RectangularROI) roi, data, mask);
	}

	@Override
	public Action createSwitchAction(final int index, final IPlotUI plotUI) {
		Action action = super.createSwitchAction(index, plotUI);
		action.setText("Box profile");
		action.setToolTipText("Switch side plot to box profile mode");
		action.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/ProfileBox.png"));

		return action;
	}

	@Override
	public void addToHistory() {
		Plot1DAppearance plotApp = new Plot1DAppearance(PlotColorUtility.getDefaultColour(majPlotter.getColourTable().getLegendSize()),
				Plot1DStyles.SOLID, "History " + majPlotter.getNumHistory());
		majPlotter.getColourTable().addEntryOnLegend(plotApp);
		majPlotter.pushGraphOntoHistory();

		plotApp = new Plot1DAppearance(PlotColorUtility.getDefaultColour(minPlotter.getColourTable().getLegendSize()),
				Plot1DStyles.SOLID, "History " + minPlotter.getNumHistory());
		minPlotter.getColourTable().addEntryOnLegend(plotApp);
		minPlotter.pushGraphOntoHistory();
	}

	@Override
	public void removeFromHistory() {
		if (majPlotter.getNumHistory() > 0) {
			majPlotter.getColourTable().deleteLegendEntry(majPlotter.getColourTable().getLegendSize()-1);
			majPlotter.popGraphFromHistory();
			majPlotter.refresh(true);
		}
		if (minPlotter.getNumHistory() > 0) {
			minPlotter.getColourTable().deleteLegendEntry(minPlotter.getColourTable().getLegendSize()-1);
			minPlotter.popGraphFromHistory();
			minPlotter.refresh(true);
		}
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
	public void generateMenuActions(IMenuManager manager, final IWorkbenchPartSite site) {
		createExportActions();
		createHistoryActions();
		createPushPlotActions();
		
		manager.add(new Separator(getClass().getName()+printButtonText));
		saveMenu = new MenuManager(saveButtonText, AnalysisRCPActivator.getImageDescriptor(saveImagePath), saveButtonText);
		saveMenu.add(saveMaj);
		saveMenu.add(saveMin);
		manager.add(saveMenu);
		copyMenu = new MenuManager(copyButtonText, AnalysisRCPActivator.getImageDescriptor(copyImagePath), copyButtonText);
		copyMenu.add(copyMaj);
		copyMenu.add(copyMin);
		manager.add(copyMenu);
		printMenu = new MenuManager(printButtonText, AnalysisRCPActivator.getImageDescriptor(printImagePath), printButtonText);
		printMenu.add(printMaj);
		printMenu.add(printMin);
		manager.add(printMenu);
		manager.add(new Separator(getClass().getName()+"pushHistoryActions"));
		manager.add(addtoHistory);
		manager.add(removefromHistory);
		manager.add(new Separator(getClass().getName()+"pushPlotActions"));
		manager.add(pushMajorPlottingDataPlot1);
		manager.add(pushMinorPlottingDataPlot1);
		manager.add(pushMajorPlottingDataPlot2);
		manager.add(pushMinorPlottingDataPlot2);
	
	}
	
	private void createExportActions(){
		saveMaj = new Action("Save major profiles plot") {
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
				majPlotter.saveGraph(filename, PlotExportUtil.FILE_TYPES[dialog.getFilterIndex()]);
			}
		};
		saveMin = new Action("Save minor profiles plot") {
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
				minPlotter.saveGraph(filename, PlotExportUtil.FILE_TYPES[dialog.getFilterIndex()]);
			}
		};
		saveGraph = new DropDownAction();
		saveGraph.setText(saveButtonText);
		saveGraph.setToolTipText(saveToolTipText);
		saveGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(saveImagePath));
		saveGraph.add(saveMaj);
		saveGraph.add(saveMin);
		
		copyMaj = new Action("Copy major profiles plot"){
			@Override
			public void run(){
				majPlotter.copyGraph();
			}
		};
		copyMin = new Action("Copy minor profiles plot") {
			@Override
			public void run() {
				minPlotter.copyGraph();
			}
		};
		copyGraph = new DropDownAction();
		copyGraph.setText(copyButtonText);
		copyGraph.setToolTipText(copyToolTipText);
		copyGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(copyImagePath));
		copyGraph.add(copyMaj);
		copyGraph.add(copyMin);
		
		printMaj = new Action("Print major profiles plot"){
			@Override
			public void run(){
				majPlotter.printGraph();
			}
		};
		printMin = new Action("Print minor profiles plot") {
			@Override
			public void run() {
				minPlotter.printGraph();
			}
		};
		printGraph = new DropDownAction();
		printGraph.setToolTipText(printToolTipText);
		printGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(printImagePath));
		printGraph.add(printMaj);
		printGraph.add(printMin);

	}
	
	private void createHistoryActions(){
		addtoHistory = new Action() {
			@Override
			public void run() {
				addToHistory();
			}
		};
		addtoHistory.setText("Add current profiles to history");
		addtoHistory.setToolTipText("Add the current profiles to the plot history");
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
		final IWorkbenchPartSite site = EclipseUtils.getPage().getActivePart().getSite();
		final String fullPlotID = "uk.ac.diamond.scisoft.analysis.rcp.plotView";
		pushMajorPlottingDataPlot1 = new Action() {
			@Override
			public void run() {
					pushPlottingData(site, fullPlotID+"1",0);
			}
		};
		pushMajorPlottingDataPlot1.setText("Push major profiles to plot 1");
		final org.eclipse.swt.graphics.Image icon = SWTResourceManager.getImage(StaticScanPlotView.class,"/icons/chart_curve_add.png");
		final ImageDescriptor d = ImageDescriptor.createFromImage(icon);
		pushMajorPlottingDataPlot1.setImageDescriptor(d);
		pushMajorPlottingDataPlot1.setToolTipText("Push major profiles to plot 1");
		
		pushMajorPlottingDataPlot2 = new Action() {
			@Override
			public void run() {
					pushPlottingData(site, fullPlotID+"2",0);
			}
		};
		pushMajorPlottingDataPlot2.setText("Push major profiles to plot 2");
		pushMajorPlottingDataPlot2.setImageDescriptor(d);
		pushMajorPlottingDataPlot2.setToolTipText("Push major profiles to plot 2");
		
		pushMinorPlottingDataPlot1 = new Action() {
			@Override
			public void run() {
					pushPlottingData(site, fullPlotID+"1",1);
			}
		};
		pushMinorPlottingDataPlot1.setText("Push minor profiles to plot 1");
		pushMinorPlottingDataPlot1.setImageDescriptor(d);
		pushMinorPlottingDataPlot1.setToolTipText("Push minor profiles to plot 1");
		
		pushMinorPlottingDataPlot2 = new Action() {
			@Override
			public void run() {
					pushPlottingData(site, fullPlotID+"2",1);
			}
		};
		pushMinorPlottingDataPlot2.setText("Push minor profiles to plot 2");
		pushMinorPlottingDataPlot2.setImageDescriptor(d);
		pushMinorPlottingDataPlot2.setToolTipText("Push minor profiles to plot 2");
		
		pushPlottingData = new DropDownAction();
		pushPlottingData.setToolTipText("Push plotting to plot 1 or 2");
		pushPlottingData.setImageDescriptor(d);
		pushPlottingData.add(pushMajorPlottingDataPlot1);
		pushPlottingData.add(pushMajorPlottingDataPlot2);
		pushPlottingData.add(pushMinorPlottingDataPlot1);
		pushPlottingData.add(pushMinorPlottingDataPlot2);
	}
}