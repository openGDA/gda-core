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

import org.dawb.common.ui.plot.roi.data.LinearROIData;
import org.dawb.common.ui.plot.roi.data.ROIData;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.jreality.print.PlotExportUtil;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROIList;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.HandleStatus;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.LinearROIHandler;
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
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.DropDownAction;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.LinearROITableViewer;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.ROIDataList;
import uk.ac.diamond.scisoft.analysis.rcp.util.FloatSpinner;
import uk.ac.diamond.scisoft.analysis.rcp.util.ResourceProperties;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.StaticScanPlotView;

import com.swtdesigner.SWTResourceManager;

/**
 * Composite to show line profiles of main plotter
 */
@Deprecated
public class LineProfile extends SidePlotProfile {
	private static Logger logger = LoggerFactory.getLogger(LineProfile.class);

	private SidePlotter1D lpPlotter;

	private static final double lineStep = 0.5;

	private DropDownAction pushPlottingData;
	private Action pushPlottingDataPlot1;
	private Action pushPlottingDataPlot2;
	private Action addtoHistory;
	private Action removefromHistory;
	private Action saveGraph;
	private Action copyGraph;
	private Action printGraph;
	private String printButtonText = ResourceProperties.getResourceString("PRINT_BUTTON");
	private String printToolTipText = ResourceProperties.getResourceString("PRINT_TOOLTIP");
	private String printImagePath = ResourceProperties.getResourceString("PRINT_IMAGE_PATH");
	private String copyButtonText = ResourceProperties.getResourceString("COPY_BUTTON");
	private String copyToolTipText = ResourceProperties.getResourceString("COPY_TOOLTIP");
	private String copyImagePath = ResourceProperties.getResourceString("COPY_IMAGE_PATH");
	private String saveButtonText = ResourceProperties.getResourceString("SAVE_BUTTON");
	private String saveToolTipText = ResourceProperties.getResourceString("SAVE_TOOLTIP");
	private String saveImagePath = ResourceProperties.getResourceString("SAVE_IMAGE_PATH");

	private Composite parent;

	private Spinner spsx, spsy;
	private FloatSpinner spex, spey, splen, spang;
	private Text txSum;

	public LineProfile() {
		super();
		roiClass = LinearROI.class;
		roiListClass = LinearROIList.class;
	}

	/**
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		container = new Composite(this.parent, SWT.NONE);
		container.setLayout(new FillLayout());

		final SashForm ss = new SashForm(container, SWT.VERTICAL);

		lpPlotter = new SidePlotter1D(ss, "Line profile");
		lpPlotter.setAxisModes(AxisMode.CUSTOM, AxisMode.LINEAR, AxisMode.LINEAR);
		lpPlotter.setXAxisLabel("Distance along line");

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

				new Label(groupCurrent, SWT.NONE).setText("End x:");
				spex = new FloatSpinner(groupCurrent, SWT.BORDER, 6, 2);
				spex.addSelectionListener(endPosListener);

				new Label(groupCurrent, SWT.NONE).setText("Length:");
				splen = new FloatSpinner(groupCurrent, SWT.BORDER, 7, 2);
				splen.addSelectionListener(lenAngListener);

				// 2nd row
				new Label(groupCurrent, SWT.NONE).setText("Start y:");
				spsy = new Spinner(groupCurrent, SWT.BORDER);
				spsy.setMinimum(-10000);
				spsy.setMaximum(10000);
				spsy.setIncrement(1);
				spsy.setPageIncrement(5);
				spsy.addSelectionListener(startPosListener);

				new Label(groupCurrent, SWT.NONE).setText("End y:");
				spey = new FloatSpinner(groupCurrent, SWT.BORDER, 6, 2);
				spey.addSelectionListener(endPosListener);

				new Label(groupCurrent, SWT.NONE).setText("Angle:");
				spang = new FloatSpinner(groupCurrent, SWT.BORDER, 5, 2);
				spang.setMinimum(0.0);
				spang.setMaximum(360.0);
				spang.addSelectionListener(lenAngListener);

				// 3rd row
				new Label(groupCurrent, SWT.NONE).setText("Sum:");
				txSum = new Text(groupCurrent, SWT.READ_ONLY | SWT.BORDER);
				txSum.setTextLimit(12);

				new Label(groupCurrent, SWT.NONE).setText("");
				new Label(groupCurrent, SWT.NONE).setText("");
				new Label(groupCurrent, SWT.NONE).setText("");
				new Label(groupCurrent, SWT.NONE).setText("");

				// 4th row
				GridData gda = new GridData();
				gda.horizontalSpan = 2;
				final Button invert = new Button(groupCurrent, SWT.CHECK);
				invert.setLayoutData(gda);
				invert.setText("Invert brightness");
				invert.setToolTipText("Invert overlay brightness");
				invert.addSelectionListener(brightnessButtonListener);

				GridData gdb = new GridData();
				gdb.horizontalSpan = 2;
				final Button cross = new Button(groupCurrent, SWT.CHECK);
				cross.setLayoutData(gdb);
				cross.setText("Cross hair");
				cross.setToolTipText("Add line 90 degrees to current line");
				cross.addSelectionListener(crossButtonListener);

				new Label(groupCurrent, SWT.NONE).setText("");
				new Label(groupCurrent, SWT.NONE).setText("");

				// 5th row
				GridData gdc = new GridData();
				gdc.horizontalSpan = 2;
				Button copyToTable = new Button(groupCurrent, SWT.PUSH);
				copyToTable.setLayoutData(gdc);
				copyToTable.setText("Copy current to table");
				copyToTable.addSelectionListener(copyButtonListener);

				GridData gde = new GridData();
				gde.horizontalSpan = 2;
				Button deleteCurrent = new Button(groupCurrent, SWT.PUSH);
				deleteCurrent.setLayoutData(gde);
				deleteCurrent.setText("Delete current");
				deleteCurrent.addSelectionListener(deleteButtonListener);
			}
		}

		final Composite table = new Composite(ss, SWT.NONE);
		ss.setWeights(new int[] {50, 30, 20});

		table.setLayout(new FillLayout());

		tViewer = new LinearROITableViewer(table, this, this);

		scomp.setContent(controls);
		controls.setSize(controls.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		// end of GUI creation

		lpPlotter.refresh(false);

		// initialize ROI and data
		updateAllSpinnersInt((LinearROI) roi);
		roiIDs = new ArrayList<Integer>();
		dragIDs = new ArrayList<Integer>();

		// initialize ROIs
		if (roiDataList == null)
			roiDataList = new ROIDataList();
		roisIDs = new ArrayList<Integer>();

		tViewer.setInput(roiDataList);

		// handle areas
		roiHandler = new LinearROIHandler((LinearROI) roi);

		// default colour: cyan
		dColour = new Color(0, 255, 255);

		// invert overlay colour
		float[] hsb;
		hsb = Color.RGBtoHSB(dColour.getRed(), dColour.getGreen(), dColour.getBlue(), null);
		cColour = Color.getHSBColor(hsb[0], hsb[1], (float) (1.0 - 0.7 * hsb[2]));
		oColour = dColour;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (lpPlotter != null) lpPlotter.cleanUp();
	}

	@Override
	protected void updatePlot(IROI roib) {
		final LinearROI lroi = (LinearROI) roib;

		updateDataList();

		if (data == null) {
			logger.warn("No data");
			return;
		}

		if (lroi != null) {
			roiData = new LinearROIData(lroi, data, lineStep);

			if (!roiData.isPlot())
				return;

			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					txSum.setText(String.format("%.2f", roiData.getProfileSum()));
				}
			});
		}

		Plot1DGraphTable colourTable;
		Plot1DAppearance newApp;
		int p, l;
		int nHistory;
		List<IDataset> plots = new ArrayList<IDataset>();
		List<AxisValues> paxes = new ArrayList<AxisValues>();

		l = p = 0;
		colourTable = lpPlotter.getColourTable();
		nHistory = lpPlotter.getNumHistory();

		if (lroi != null) {
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

			if (lroi.isCrossHair()) {
				l++;
				if (l + nHistory >= colourTable.getLegendSize()) {
					newApp = new Plot1DAppearance(PlotColorUtility.getDefaultColour(p), Plot1DStyles.DASHED,
							"Cross Line 1");
					colourTable.addEntryOnLegend(l, newApp);
				} else {
					newApp = colourTable.getLegendEntry(l);
					newApp.setColour(PlotColorUtility.getDefaultColour(p));
					newApp.setStyle(Plot1DStyles.DASHED);
					newApp.setName("Cross Line 1");
				}

				plots.add(roiData.getProfileData(1));
				paxes.add(roiData.getXAxis(1));
			}
			l++;
			p++;
		}

		for (int i = 0, imax = roiDataList.size(); i < imax; i++) {
			LinearROIData rd = (LinearROIData) roiDataList.get(i);
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

				if (rd.getROI().isCrossHair()) {
					l++;
					plots.add(rd.getProfileData(1));
					paxes.add(rd.getXAxis(1));
					if (l + nHistory  >= colourTable.getLegendSize()) {
						newApp = new Plot1DAppearance(colour, Plot1DStyles.DASHED, "Cross Line " + (p + 1));
						colourTable.addEntryOnLegend(l, newApp);
					} else {
						newApp = colourTable.getLegendEntry(l);
						newApp.setColour(colour);
						newApp.setStyle(Plot1DStyles.DASHED);
						newApp.setName("Cross Line " + (p + 1));
					}
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
			lpPlotter.replaceAllPlots(plots, paxes);
		} catch (PlotException e) {
			e.printStackTrace();
		}

		lpPlotter.updateAllAppearance();
		lpPlotter.refresh(false);
	}

	/**
	 * Draw dragged out overlay for given region of interest
	 * @param roib
	 */
	private void drawDraggedOverlay(IROI roib) {
		if (oProvider == null)
			return;

		final LinearROI lroi = (LinearROI) roib;
		double[] spt = lroi.getPointRef();
		double[] ept = lroi.getEndPoint();

		if (dragIDs.isEmpty()) {
			dragIDs.add(-1);
			dragIDs.add(-1);
		}

		int id, index;
		index = 0;

		// box
		id = dragIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.LINE);
			dragIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		oProvider.begin(OverlayType.VECTOR2D);

		oProvider.drawLine(id, spt[0], spt[1], ept[0], ept[1]);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);

		// bisector
		id = dragIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.LINE);
			dragIDs.set(index, id);
			if (id == -1)
				return;
		}
		index++;

		if (lroi.isCrossHair()) {
			spt = lroi.getPerpendicularBisectorPoint(0.0);
			ept = lroi.getPerpendicularBisectorPoint(1.0);

			oProvider.setPrimitiveVisible(id, true);
			oProvider.drawLine(id, spt[0], spt[1], ept[0], ept[1]);
			oProvider.setColour(id, oColour);
			oProvider.setTransparency(id, oTransparency);
		} else {
			oProvider.setPrimitiveVisible(id, false);
		}

		oProvider.end(OverlayType.VECTOR2D);
	}

	@Override
	protected void drawCurrentOverlay() {
		if (oProvider == null || roi == null)
			return;

		if (roiIDs.isEmpty()) {
			roiIDs.add(-1); // arrow
			roiIDs.add(-1); // bisector
		}

		int id, index;
		index = 0;

		final LinearROI lroi = (LinearROI) roi;
		final double[] spt = roi.getPointRef();
		final double[] ept = lroi.getEndPoint();
		oProvider.begin(OverlayType.VECTOR2D);

		// arrow
		id = roiIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.ARROW);
			roiIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		oProvider.drawArrow(id, spt[0], spt[1], ept[0], ept[1], 2./3);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);

		// bisector
		id = roiIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.LINE);
			roiIDs.set(index, id);
			if (id == -1)
				return;
		}
		index++;

		if (lroi.isCrossHair()) {
			double[] mpt = lroi.getPerpendicularBisectorPoint(0.0);
			double[] bpt = lroi.getPerpendicularBisectorPoint(1.0);

			oProvider.setPrimitiveVisible(id, true);
			oProvider.drawLine(id, mpt[0], mpt[1], bpt[0], bpt[1]);
			oProvider.setColour(id, oColour);
			oProvider.setTransparency(id, oTransparency);
		} else {
			oProvider.setPrimitiveVisible(id, false);
		}

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
			if (pt == null)
				continue;
			oProvider.drawBox(hid, pt[0], pt[1], pt[0] + hsize, pt[1] + hsize);
			pt = roiHandler.getAnchorPoint(h, hsize);
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
				id = oProvider.registerPrimitive(PrimitiveType.ARROW);
				roisIDs.set(r, id);
			} else
				oProvider.setPrimitiveVisible(id, true);

			final LinearROI lroi = (LinearROI) roiDataList.get(r).getROI();
			final double[] spt = lroi.getPointRef();
			double[] ept = lroi.getEndPoint();

			oProvider.drawArrow(id, spt[0], spt[1], ept[0], ept[1]);
			oProvider.setColour(id, oColour);
			oProvider.setTransparency(id, oTransparency);
		}

		oProvider.end(OverlayType.VECTOR2D);
	}

	@Override
	public void imageStart(IImagePositionEvent event) {
		HandleStatus hStatus = HandleStatus.NONE;

		if (roi == null) {
			roi = new LinearROI();
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
				dragging = true;
			} else if (roiHandler.contains(id)) {
				int h = roiHandler.indexOf(id);

				if (h == 0 || h == 2) {
					if ((flags & IImagePositionEvent.SHIFTKEY) != 0) {
						hStatus = HandleStatus.REORIENT;
						oProvider.setPlotAreaCursor(SWT.CURSOR_IBEAM);
					} else {
						hStatus = HandleStatus.RESIZE;
						oProvider.setPlotAreaCursor(SWT.CURSOR_SIZEALL);
					}
				} else if (h == 1) {
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
			}
			roiHandler.configureDragging(dragHandle, hStatus);
		} else if ((flags & IImagePositionEvent.RIGHTMOUSEBUTTON) != 0) {
			if (roiHandler.contains(id)) {
				int h = roiHandler.indexOf(id);

				if (h == 0 || h == 2) {
					hStatus = HandleStatus.REORIENT;
					oProvider.setPlotAreaCursor(SWT.CURSOR_IBEAM);
				} else if (h == 1) {
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
		}
	}

	// more GUI listeners
	private SelectionListener crossButtonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final LinearROI lroi = (LinearROI) roi;

			if (((Button) e.widget).getSelection()) {
				lroi.setCrossHair(true);
			} else {
				lroi.setCrossHair(false);
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

	@Override
	protected void updateAllSpinners(IROI roib) {
		final LinearROI lroi = (LinearROI) roib;
		if (lroi == null)
			return;

		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				updateAllSpinnersInt(lroi);
			}
		});
	}

	private void updateAllSpinnersInt(final LinearROI lroi) {
		if (lroi == null)
			return;
		isBulkUpdate = true;
		spsx.setSelection(lroi.getIntPoint()[0]);
		spsy.setSelection(lroi.getIntPoint()[1]);
		splen.setDouble(lroi.getLength());
		spang.setDouble(lroi.getAngleDegrees());
		spex.setDouble(lroi.getEndPoint()[0]);
		isBulkUpdate = false;
		spey.setDouble(lroi.getEndPoint()[1]);
	}

	// spinner listeners
	private SelectionListener startPosListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final LinearROI lroi = (LinearROI) roi;
			double[] ept = lroi.getEndPoint();

			lroi.setPoint(spsx.getSelection(), spsy.getSelection());
			lroi.setEndPoint(ept);

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

	private SelectionListener endPosListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final LinearROI lroi = (LinearROI) roi;

			if (lroi!=null) {
				lroi.setEndPoint(new double[] { spex.getDouble(), spey.getDouble() });

				if (!isBulkUpdate)
					sendCurrentROI(roi);
	
				getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						splen.setDouble(lroi.getLength());
						spang.setDouble(lroi.getAngleDegrees());
						if (isBulkUpdate)
							return;
						updatePlot();
						drawCurrentOverlay();
					}
				});
			}
		}
	};

	private SelectionListener lenAngListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final LinearROI lroi = (LinearROI) roi;

			if (lroi!=null) {
				lroi.setLength(splen.getDouble());
				lroi.setAngleDegrees(spang.getDouble());
				if (!isBulkUpdate)
					sendCurrentROI(roi);
	
				getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						spex.setDouble(lroi.getEndPoint()[0]);
						spey.setDouble(lroi.getEndPoint()[1]);
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
	public LinearROIList createROIList() {
		LinearROIList list = new LinearROIList();
		if (roiDataList != null) {
			for (ROIData rd: roiDataList) {
				list.add((LinearROI) rd.getROI());
			}
		}
		return list;
	}

	@Override
	public ROIData createNewROIData(IROI roi) {
		return new LinearROIData((LinearROI) roi, data, lineStep);
	}

	@Override
	public Action createSwitchAction(final int index, final IPlotUI plotUI) {
		Action action = super.createSwitchAction(index, plotUI);
		action.setText("Line profile");
		action.setToolTipText("Switch side plot to line profile mode");
		action.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/ProfileLine.png"));

		return action;
	}

	@Override
	public void addToHistory() {
		Plot1DAppearance plotApp = 
			new Plot1DAppearance(PlotColorUtility.getDefaultColour(lpPlotter.getColourTable().getLegendSize()),
					             Plot1DStyles.SOLID, "History " + lpPlotter.getNumHistory());
		lpPlotter.getColourTable().addEntryOnLegend(plotApp);
		lpPlotter.pushGraphOntoHistory();
	}

	@Override
	public void removeFromHistory() {
		if (lpPlotter.getNumHistory() > 0) {
			lpPlotter.getColourTable().deleteLegendEntry(lpPlotter.getColourTable().getLegendSize()-1);					
		    lpPlotter.popGraphFromHistory();
		    lpPlotter.refresh(true);
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
		manager.add(new Separator(getClass().getName()+"historyActions"));
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
		manager.add(saveGraph);
		manager.add(copyGraph);
		manager.add(printGraph);
		manager.add(new Separator(getClass().getName()+"historyActions"));
		manager.add(addtoHistory);
		manager.add(removefromHistory);
		manager.add(new Separator(getClass().getName()+"pushPlotActions"));
		manager.add(pushPlottingDataPlot1);
		manager.add(pushPlottingDataPlot2);
	
	}

	private void createExportActions(){
		saveGraph = new Action() {
			
			// Cache file name otherwise they have to keep
			// choosing the folder.
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

				lpPlotter.saveGraph(filename, PlotExportUtil.FILE_TYPES[dialog.getFilterIndex()]);
			}
		};
		saveGraph.setText(saveButtonText);
		saveGraph.setToolTipText(saveToolTipText);
		saveGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(saveImagePath));
		
		copyGraph = new Action() {
			@Override
			public void run() {
				lpPlotter.copyGraph();
			}
		};
		copyGraph.setText(copyButtonText);
		copyGraph.setToolTipText(copyToolTipText);
		copyGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(copyImagePath));
		
		printGraph = new Action() {
			@Override
			public void run() {
				lpPlotter.printGraph();
			}
		};
		
		printGraph.setText(printButtonText);
		printGraph.setToolTipText(printToolTipText);
		printGraph.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor(printImagePath));

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
		pushPlottingDataPlot1 = new Action() {
			@Override
			public void run() {
					pushPlottingData(site, fullPlotID+"1",0);
			}
		};
		pushPlottingDataPlot1.setText("Push line profiles to plot 1");
		final org.eclipse.swt.graphics.Image icon = SWTResourceManager.getImage(StaticScanPlotView.class,"/icons/chart_curve_add.png");
		final ImageDescriptor d = ImageDescriptor.createFromImage(icon);
		pushPlottingDataPlot1.setImageDescriptor(d);
		pushPlottingDataPlot1.setToolTipText("Push line profiles to plot 1");
		
		pushPlottingDataPlot2 = new Action() {
			@Override
			public void run() {
					pushPlottingData(site, fullPlotID+"2",0);
			}
		};
		pushPlottingDataPlot2.setText("Push line profiles to plot 2");
		pushPlottingDataPlot2.setImageDescriptor(d);
		pushPlottingDataPlot2.setToolTipText("Push line profiles to plot 2");
		
		pushPlottingData = new DropDownAction();
		pushPlottingData.setToolTipText("Push plotting to plot 1 or 2");
		pushPlottingData.setImageDescriptor(d);
		pushPlottingData.add(pushPlottingDataPlot1);
		pushPlottingData.add(pushPlottingDataPlot2);

	}
}
