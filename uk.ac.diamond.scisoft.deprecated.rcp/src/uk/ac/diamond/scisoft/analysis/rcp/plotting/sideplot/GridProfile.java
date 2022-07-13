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
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.roi.data.GridROIData;
import org.dawb.common.ui.plot.roi.data.ROIData;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.GridPreferences;
import org.eclipse.dawnsci.analysis.dataset.roi.GridROI;
import org.eclipse.dawnsci.analysis.dataset.roi.GridROIList;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.GridROIHandler;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.HandleStatus;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayType;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.VectorOverlayStyles;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.primitives.PrimitiveType;
import org.eclipse.dawnsci.plotting.api.jreality.tool.IImagePositionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.GridROITableViewer;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.ROIDataList;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.rcp.util.FloatSpinner;

/**
 * A GridProfile side plot for marking regions of interest with a configurable grid layout This side plot is not
 * activated by default. This allows you to subclass and add you own controls (for starting the scan or status views
 * etc.)
 */
@Deprecated
public class GridProfile extends SidePlotProfile {
	private static Logger logger = LoggerFactory.getLogger(GridProfile.class);
	
	private GridPreferences gridPrefs;

	private Color rColour = new Color(255, 0, 0); // default circle colour: red

	private List<Integer> beamlineIDs;

	private IPropertyChangeListener propListener;

	private HandleStatus hStatus = HandleStatus.NONE;
	private FloatSpinner spsx, spsy;
	private FloatSpinner resx, resy;
	private FloatSpinner splmaj, splmin, spang;
	private Button midpoint;
	private Button gridpoint;

	private Color pointColor = Color.WHITE;

	private Text tdim;

	public GridProfile() {
		super();
		roiClass = GridROI.class;
		roiListClass = GridROIList.class;
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createPartControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		final SashForm ss = new SashForm(container, SWT.VERTICAL);

		// GUI creation and layout

		final Composite grid = new Composite(ss, SWT.NONE);
		grid.setLayout(new FillLayout(SWT.HORIZONTAL));
		{

			final Group groupGridSettings = new Group(grid, SWT.NONE);
			groupGridSettings.setLayout(new GridLayout(3, false));
			groupGridSettings.setText("Grid Settings");
			{
				// Widget to set resolution
				new Label(groupGridSettings, SWT.NONE).setText("X-axis resolution:");
				resx = new FloatSpinner(groupGridSettings, SWT.BORDER, 6, 2);
				resx.setMinimum(0);
				resx.addSelectionListener(resolutionListener);
				new Label(groupGridSettings, SWT.NONE).setText("microns.");

				new Label(groupGridSettings, SWT.NONE).setText("Y-axis resolution:");
				resy = new FloatSpinner(groupGridSettings, SWT.BORDER, 6, 2);
				resy.setMinimum(0);
				resy.addSelectionListener(resolutionListener);
				new Label(groupGridSettings, SWT.NONE).setText("microns.");

				// Widget to toggle grid mid-points
				GridData gdaa = new GridData();
				gdaa.horizontalSpan = 4;
				midpoint = new Button(groupGridSettings, SWT.CHECK);
				midpoint.setLayoutData(gdaa);
				midpoint.setText("Display grid mid-points");
				midpoint.setToolTipText("Toggles the visibility of the grid mid-points");
				midpoint.addSelectionListener(midpointListener);

				// Widget to toggle grid mid-points
				GridData gdab = new GridData();
				gdab.horizontalSpan = 4;
				gridpoint = new Button(groupGridSettings, SWT.CHECK);
				gridpoint.setLayoutData(gdab);
				gridpoint.setText("Display gridlines");
				gridpoint.setToolTipText("Toggles the visibility of the gridlines");
				gridpoint.addSelectionListener(gridpointListener);

				GridData gdb = new GridData();
				gdb.horizontalSpan = 4;
				final Button invert = new Button(groupGridSettings, SWT.CHECK);
				invert.setLayoutData(gdb);
				invert.setText("Invert brightness");
				invert.setToolTipText("Invert overlay brightness");
				invert.addSelectionListener(brightnessButtonListener);
			}

			addControlWidgets(grid);
		}

		final Composite controls = new Composite(ss, SWT.NONE);
		controls.setLayout(new FillLayout(SWT.VERTICAL));
		{
			final Group groupCurrent = new Group(controls, SWT.NONE);
			groupCurrent.setLayout(new GridLayout(6, false));
			groupCurrent.setText("Current ROI");
			{
				// 1st row
				new Label(groupCurrent, SWT.NONE).setText("Start x:");
				spsx = new FloatSpinner(groupCurrent, SWT.BORDER, 6, 2);
				spsx.addSelectionListener(startPosListener);

				new Label(groupCurrent, SWT.NONE).setText("Length major:");
				splmaj = new FloatSpinner(groupCurrent, SWT.BORDER, 6, 2);
				splmaj.addSelectionListener(lensListener);

				Label lang = new Label(groupCurrent, SWT.LEFT);
				lang.setText("Angle:");
				GridData gd_spang = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
				lang.setLayoutData(gd_spang);
				spang = new FloatSpinner(groupCurrent, SWT.BORDER, 7, 2);
				spang.setMinimum(0.0);
				spang.setMaximum(360.0);
				spang.addSelectionListener(angListener);

				// 2nd row
				new Label(groupCurrent, SWT.NONE).setText("Start y:");
				spsy = new FloatSpinner(groupCurrent, SWT.BORDER, 6, 2);
				spsy.addSelectionListener(startPosListener);

				new Label(groupCurrent, SWT.NONE).setText("Length minor:");
				splmin = new FloatSpinner(groupCurrent, SWT.BORDER, 6, 2);
				splmin.addSelectionListener(lensListener);

				new Label(groupCurrent, SWT.NONE).setText("Grid dimensions:");
				tdim = new Text(groupCurrent, SWT.BORDER);
				GridData gd_tdim = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
				gd_tdim.widthHint = 145;
				tdim.setLayoutData(gd_tdim);
				tdim.setEditable(false);

				// 3rd row
				GridData gda = new GridData();
				gda.horizontalSpan = 2;
				final Button copyToTable = new Button(groupCurrent, SWT.PUSH);
				copyToTable.setLayoutData(gda);
				copyToTable.setText("Copy current to table");
				copyToTable.addSelectionListener(copyButtonListener);
			}
		}

		final Composite table = new Composite(ss, SWT.NONE);
		ss.setWeights(new int[] { 20, 15, 15 });

		table.setLayout(new FillLayout());

		tViewer = new GridROITableViewer(table, this, this, this);
		// end of GUI creation

		// initialize ROI and data
		updateAllSpinnersInt((GridROI) roi);
		roiIDs = new ArrayList<Integer>();
		dragIDs = new ArrayList<Integer>();
		beamlineIDs = new ArrayList<Integer>();

		// initialize ROIs
		if (roiDataList == null)
			roiDataList = new ROIDataList();
		roisIDs = new ArrayList<Integer>();

		tViewer.setInput(roiDataList);

		// handle areas
		roiHandler = new GridROIHandler((GridROI) roi);

		// default colour: green
		dColour = new Color(0, 255, 0);

		// invert overlay colour
		float[] hsb;
		hsb = Color.RGBtoHSB(dColour.getRed(), dColour.getGreen(), dColour.getBlue(), null);
		cColour = Color.getHSBColor(hsb[0], hsb[1], (float) (1.0 - 0.7 * hsb[2]));
		oColour = dColour;

		addPropertyListeners();
	}

	/**
	 * to be overridden to enrich this with beamline specific control
	 * 
	 * @param parent
	 *            the composite to add controls to
	 */
	protected void addControlWidgets(@SuppressWarnings("unused") Composite parent) {
		// meant to be overridden
	}

	private void addPropertyListeners() {
		AnalysisRCPActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();

				if (property.equals(PreferenceConstants.GRIDSCAN_RESOLUTION_X)
						|| property.equals(PreferenceConstants.GRIDSCAN_RESOLUTION_Y)
						|| property.equals(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX)
						|| property.equals(PreferenceConstants.GRIDSCAN_BEAMLINE_POSY)) {
					IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();

					double gridScanResolutionX;
					if (preferenceStore.isDefault(PreferenceConstants.GRIDSCAN_RESOLUTION_X)) {
						gridScanResolutionX = preferenceStore
								.getDefaultDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_X);
					} else {
						gridScanResolutionX = preferenceStore.getDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_X);
					}
					getGridPrefs().setResolutionX(gridScanResolutionX);

					double gridScanResolutionY;
					if (preferenceStore.isDefault(PreferenceConstants.GRIDSCAN_RESOLUTION_Y)) {
						gridScanResolutionY = preferenceStore
								.getDefaultDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_Y);
					} else {
						gridScanResolutionY = preferenceStore.getDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_Y);
					}
					getGridPrefs().setResolutionY(gridScanResolutionY);

					double xBeamPos;
					if (preferenceStore.isDefault(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX)) {
						xBeamPos = preferenceStore.getDefaultDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX);
					} else {
						xBeamPos = preferenceStore.getDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX);
					}
					getGridPrefs().setBeamlinePosX(xBeamPos);

					double yBeamPos;
					if (preferenceStore.isDefault(PreferenceConstants.GRIDSCAN_BEAMLINE_POSY)) {
						yBeamPos = preferenceStore.getDefaultDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSY);
					} else {
						yBeamPos = preferenceStore.getDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSY);
					}
					getGridPrefs().setBeamlinePosY(yBeamPos);

					sendPreferences(getGridPrefs());
					updateAllSpinners(roi);
					getControl().getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							tViewer.refresh();
							drawEverything();
						}
					});
				}
			}
		});
	}

	@Override
	public void dispose() {
		super.dispose();
		if (propListener != null) {
			AnalysisRCPActivator.getDefault().getPreferenceStore().removePropertyChangeListener(propListener);
		}
	}

	@Override
	public void unregisterProvider() {
		super.unregisterProvider();
		hideIDs(beamlineIDs);
	}

	@Override
	public void hideOverlays() {
		super.hideOverlays();
		hideIDs(beamlineIDs);
	}

	@Override
	public void showOverlays() {
		super.showOverlays();

		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				drawBeamlineCentre();
			}
		});
	}


	@Override
	public void removePrimitives() {
		if (oProvider == null) {
			return;
		}
		super.removePrimitives();
		removeIDs(beamlineIDs);
	}

	@Override
	protected void updatePlot(IROI roib) {
		final GridROI groi = (GridROI) roib;
		getDataset();

		if (data == null) {
			logger.warn("No data");
			return;
		}

		if (groi != null) {
			roiData = new GridROIData(groi, data);

			if (!roiData.isPlot()) {
				return;
			}

			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					// txSum.setText(String.format("%.3e", roiData.getProfileSum()));
				}
			});
		}
	}

	/**
	 * Draw dragged out overlay for given region of interest
	 * @param roib
	 */
	private void drawDraggedOverlay(IROI roib) {
		if (oProvider == null) {
			return;
		}

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
			if (id == -1) {
				return;
			}
		} else {
			oProvider.setPrimitiveVisible(id, true);
		}
		index++;

		final GridROI groi = (GridROI) roib;
		int[] spt = groi.getIntPoint();
		int[] len = groi.getIntLengths();

		oProvider.begin(OverlayType.VECTOR2D);

		oProvider.drawBox(id, spt[0], spt[1], spt[0] + len[0], spt[1] + len[1]);
		oProvider.rotatePrimitive(id, -groi.getAngle(), spt[0], spt[1]);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);

		oProvider.end(OverlayType.VECTOR2D);
	}

	@Override
	protected void drawCurrentOverlay() {
		if (oProvider == null || roi == null) {
			return;
		}

		if (roiIDs.isEmpty()) {
			roiIDs.add(-1); // box
			roiIDs.add(-1); // major
			roiIDs.add(-1); // minor
		}

		int id, index;
		index = 0;

		final GridROI groi = (GridROI) roi;
		int[] spt = groi.getIntPoint();
		int[] len = groi.getIntLengths();

		oProvider.begin(OverlayType.VECTOR2D);

		// box
		id = roiIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.BOX);
			roiIDs.set(index, id);
			if (id == -1) {
				return;
			}
		} else {
			oProvider.setPrimitiveVisible(id, true);
		}
		index++;

		oProvider.drawBox(id, spt[0], spt[1], spt[0] + len[0], spt[1] + len[1]);
		oProvider.rotatePrimitive(id, -groi.getAngle(), spt[0], spt[1]);
		oProvider.setColour(id, oColour);
		// oProvider.setOutlineColour(id, oColour);
		// oProvider.setLineThickness(id, oThickness);
		oProvider.setTransparency(id, 0.9);
		// oProvider.setOutlineTransparency(id, oTransparency);
		oProvider.setStyle(id, VectorOverlayStyles.FILLED);

		// major axis
		id = roiIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.ARROW);
			roiIDs.set(index, id);
			if (id == -1) {
				return;
			}
		} else {
			oProvider.setPrimitiveVisible(id, true);
		}
		index++;

		oProvider.drawArrow(id, spt[0], spt[1], spt[0] + len[0], spt[1], 2. / 3);
		oProvider.rotatePrimitive(id, -groi.getAngle(), spt[0], spt[1]);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);

		// minor axis
		id = roiIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.ARROW);
			roiIDs.set(index, id);
			if (id == -1) {
				return;
			}
		} else {
			oProvider.setPrimitiveVisible(id, true);
		}
		index++;

		oProvider.drawArrow(id, spt[0], spt[1], spt[0], spt[1] + len[1], 1. / 3);
		oProvider.rotatePrimitive(id, -groi.getAngle(), spt[0], spt[1]);
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
			} else {
				oProvider.setPrimitiveVisible(hid, true);
			}

			double[] hspt = roiHandler.getHandlePoint(h, hsize);
			if (hspt == null) {
				oProvider.setPrimitiveVisible(hid, false);
				continue;
			}
			oProvider.drawBox(hid, hspt[0], hspt[1], hspt[0] + hsize, hspt[1] + hsize);
			hspt = roiHandler.getAnchorPoint(h, hsize);
			oProvider.rotatePrimitive(hid, -groi.getAngle(), hspt[0], hspt[1]);
			oProvider.setAnchorPoints(hid, hspt[0], hspt[1]);
			oProvider.setStyle(hid, VectorOverlayStyles.FILLED_WITH_OUTLINE);
			oProvider.setColour(hid, oColour);
			oProvider.setOutlineColour(hid, oColour);
			oProvider.setLineThickness(hid, oThickness);
			oProvider.setTransparency(hid, 0.9);
			oProvider.setOutlineTransparency(hid, oTransparency);
		}

		// Grid points
		if (roiIDs.size() == index) {
			roiIDs.add(-1);
		}
		id = roiIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.POINTLIST);
			roiIDs.set(index, id);
			if (id == -1) {
				return;
			}
		} else {
			oProvider.setPrimitiveVisible(id, true);
		}
		index++;

		if (groi.isMidPointOn()) {
			double[][] gridPoints = groi.getGridPoints();
			int xGrids = gridPoints[0].length;
			int yGrids = gridPoints[1].length;
			if (xGrids > 0 && yGrids > 0) {
				int numPoints = xGrids * yGrids;
				int[] xIntPoints = new int[numPoints];
				int[] yIntPoints = new int[numPoints];
				double[] xPoints = new double[numPoints];
				double[] yPoints = new double[numPoints];

				int cnt = 0;
				for (int i = 0; i < xGrids; i++) {
					for (int j = 0; j < yGrids; j++) {
						xIntPoints[cnt] = (int) gridPoints[0][i];
						yIntPoints[cnt] = (int) gridPoints[1][j];
						xPoints[cnt] = gridPoints[0][i];
						yPoints[cnt] = gridPoints[1][j];
						cnt++;
					}
				}

				oProvider.drawPoints(id, xPoints, yPoints);
				oProvider.setThickPoints(id, true);
				oProvider.rotatePrimitive(id, -groi.getAngle(), spt[0], spt[1]);
				oProvider.setColour(id, pointColor);
				oProvider.setTransparency(id, 0);
			} else {
				oProvider.setPrimitiveVisible(id, false);
			}
		} else {
			oProvider.setPrimitiveVisible(id, false);
		}

		// Grid lines
		if (groi.isGridLineOn()) {
			double[][] intGridLines = groi.getGridLines();
			int xGrids = intGridLines[0].length;
			int yGrids = intGridLines[1].length;
			if (xGrids != 0 && yGrids != 0) {
				for (int i = 0; i < xGrids; i++) {
					if (roiIDs.size() == index) {
						roiIDs.add(-1);
					}
					id = roiIDs.get(index);
					if (id == -1) {
						id = oProvider.registerPrimitive(PrimitiveType.LINE);
						roiIDs.set(index, id);
						if (id == -1) {
							return;
						}
					} else {
						oProvider.setPrimitiveVisible(id, true);
					}
					index++;

					// oProvider.drawLine(id, (int)intGridLines[0][i], spt[1], (int)intGridLines[0][i], spt[1] +
					// len[1]);
					oProvider.drawLine(id, intGridLines[0][i], spt[1], intGridLines[0][i], spt[1] + len[1]);
					oProvider.rotatePrimitive(id, -groi.getAngle(), spt[0], spt[1]);
					oProvider.setColour(id, Color.BLACK);
					oProvider.setTransparency(id, 0);

				}
				for (int i = 0; i < yGrids; i++) {
					if (roiIDs.size() == index) {
						roiIDs.add(-1);
					}
					id = roiIDs.get(index);
					if (id == -1) {
						id = oProvider.registerPrimitive(PrimitiveType.LINE);
						roiIDs.set(index, id);
						if (id == -1) {
							return;
						}
					} else {
						oProvider.setPrimitiveVisible(id, true);
					}
					index++;

					// oProvider.drawLine(id, spt[0], (int)intGridLines[1][i], spt[0] + len[0],
					// (int)intGridLines[1][i]);
					oProvider.drawLine(id, spt[0], intGridLines[1][i], spt[0] + len[0], intGridLines[1][i]);
					oProvider.rotatePrimitive(id, -groi.getAngle(), spt[0], spt[1]);
					oProvider.setColour(id, Color.BLACK);
					oProvider.setTransparency(id, 0);

				}
			}
		}

		// end the drawing
		oProvider.end(OverlayType.VECTOR2D);

		// there may be some leftover lines when we had a higher resolution grid earlier,
		// remove them now
		List<Integer> subList = roiIDs.subList(index, roiIDs.size());
		oProvider.unregisterPrimitive(subList);
		for (; index < roiIDs.size(); index++) {
			roiIDs.set(index, -1);
		}
	}

	/**
	 * Draws all ROIs
	 */
	@Override
	protected void drawOverlays() {
		if (oProvider == null) {
			return;
		}

		if (roiDataList.size() == 0) {
			return;
		}

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
			} else {
				oProvider.setPrimitiveVisible(id, true);
			}
			GridROI rroi = (GridROI) roiDataList.get(r).getROI();
			int[] spt = rroi.getIntPoint();
			int[] len = rroi.getIntLengths();

			oProvider.drawBox(id, spt[0], spt[1], spt[0] + len[0], spt[1] + len[1]);
			oProvider.rotatePrimitive(id, -rroi.getAngle(), spt[0], spt[1]);

			oProvider.setColour(id, oColour);
			oProvider.setTransparency(id, oTransparency);
		}

		oProvider.end(OverlayType.VECTOR2D);
	}

	/**
	 * Draws beamline overlay
	 */
	private void drawBeamlineCentre() {
		if (oProvider == null) {
			return;
		}

		if (beamlineIDs.isEmpty()) {
			beamlineIDs.add(-1);
			beamlineIDs.add(-1);
			beamlineIDs.add(-1);
		}

		int id, index;
		index = 0;

		// make beam centre size dependent on image size
		int[] dims;
		if (mainPlotter == null) {
			dims = new int[] { 100, 100 };
		} else {
			getDataset();
			dims = data.getShape();
		}
		int dimension = calcHandleSize(dims);

		// circle
		id = beamlineIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.CIRCLE, true);
			beamlineIDs.set(index, id);
			if (id == -1) {
				return;
			}
		} else {
			oProvider.setPrimitiveVisible(id, true);
		}
		index++;

		oProvider.begin(OverlayType.VECTOR2D);

		int xBeamCentre = (int) getGridPrefs().getBeamlinePosX();
		int yBeamCentre = (int) getGridPrefs().getBeamlinePosY();
		oProvider.drawCircle(id, xBeamCentre, yBeamCentre, dimension / 2);
		oProvider.setAnchorPoints(id, xBeamCentre, yBeamCentre);
		oProvider.setColour(id, rColour);
		oProvider.setStyle(id, VectorOverlayStyles.FILLED_WITH_OUTLINE);
		oProvider.setOutlineColour(id, rColour);
		oProvider.setLineThickness(id, oThickness);
		oProvider.setTransparency(id, 0.9);
		oProvider.setOutlineTransparency(id, oTransparency);

		// centre mark - horizontal
		id = beamlineIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.LINE, true);
			beamlineIDs.set(index, id);
			if (id == -1) {
				return;
			}
		} else {
			oProvider.setPrimitiveVisible(id, true);
		}
		index++;

		oProvider.drawLine(id, xBeamCentre - dimension / 2, yBeamCentre, xBeamCentre + dimension / 2, yBeamCentre);
		oProvider.setAnchorPoints(id, xBeamCentre, yBeamCentre);
		oProvider.setColour(id, rColour);
		oProvider.setTransparency(id, oTransparency);

		// centre mark - vertical
		id = beamlineIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.LINE, true);
			beamlineIDs.set(index, id);
			if (id == -1) {
				return;
			}
		} else {
			oProvider.setPrimitiveVisible(id, true);
		}
		index++;

		oProvider.drawLine(id, xBeamCentre, yBeamCentre - dimension / 2, xBeamCentre, yBeamCentre + dimension / 2);
		oProvider.setAnchorPoints(id, xBeamCentre, yBeamCentre);
		oProvider.setColour(id, rColour);
		oProvider.setTransparency(id, oTransparency);

		oProvider.end(OverlayType.VECTOR2D);
	}

	@Override
	public void imageStart(IImagePositionEvent event) {
		hStatus = HandleStatus.NONE;

		if (roi == null) {
			roi = new GridROI(gridPrefs);
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
				hideCurrent();
				getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						spsx.setDouble(getGridPrefs().getXMicronsFromPixelsCoord(roi.getPointX()));
						spsy.setDouble(getGridPrefs().getYMicronsFromPixelsCoord(roi.getPointY()));
					}
				});
				hStatus = HandleStatus.RESIZE;
				dragging = true;
			} else if (roiHandler.contains(id)) {
				int h = roiHandler.indexOf(id);

				if (h != 4) {
					hStatus = HandleStatus.RESIZE;
					if ((flags & IImagePositionEvent.SHIFTKEY) != 0) {
						hStatus = HandleStatus.REORIENT;
						if ((h % 2) == 1) {
							hStatus = HandleStatus.ROTATE;
						}
					}
				} else if (h == 4) {
					hStatus = HandleStatus.RMOVE;
					if ((flags & IImagePositionEvent.SHIFTKEY) != 0) {
						hStatus = HandleStatus.ROTATE;
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
					hStatus = HandleStatus.REORIENT;
					if ((h % 2) == 1) {
						hStatus = HandleStatus.ROTATE;
					}
				} else if (h == 4) {
					hStatus = HandleStatus.ROTATE;
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

			roi = roiHandler.interpretMouseDragging(cpt, event.getImagePosition());
			roiHandler.setROI(roi);
			roiHandler.unconfigureDragging();

			drawCurrentOverlay();
			sendCurrentROI(roi);

			updateAllSpinners(roi);
		}
	}

	// more GUI listeners
	private SelectionListener midpointListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final GridROI groi = (GridROI) roi;

			if (((Button) e.widget).getSelection()) {
				groi.setMidPointOn(true);
			} else {
				groi.setMidPointOn(false);
			}
			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updatePlot();
					drawCurrentOverlay();
				}
			});
		}
	};

	private SelectionListener gridpointListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			final GridROI groi = (GridROI) roi;

			if (((Button) e.widget).getSelection()) {
				groi.setGridLineOn(true);
			} else {
				groi.setGridLineOn(false);
			}
			getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updatePlot();
					drawCurrentOverlay();
				}
			});
		}
	};

	@Override
	protected void updateAllSpinners(IROI roib) {
		if (roib == null) {
			return;
		}

		final GridROI groi = (GridROI) roib;
		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				updateAllSpinnersInt(groi);
			}
		});
	}

	protected void updateAllSpinnersInt(final GridROI groi) {
		if (groi == null)
			return;

		resx.setDouble(getGridPrefs().getXMicronsFromPixelsLen(groi.getxSpacing()));
		resy.setDouble(getGridPrefs().getYMicronsFromPixelsLen(groi.getySpacing()));
		midpoint.setSelection(groi.isMidPointOn());
		gridpoint.setSelection(groi.isGridLineOn());
		spsx.setDouble(getGridPrefs().getXMicronsFromPixelsCoord(groi.getPointX()));
		spsy.setDouble(getGridPrefs().getYMicronsFromPixelsCoord(groi.getPointY()));
		splmaj.setDouble(getGridPrefs().getXMicronsFromPixelsLen(groi.getLength(0)));
		splmin.setDouble(getGridPrefs().getYMicronsFromPixelsLen(groi.getLength(1)));
		spang.setDouble(groi.getAngleDegrees());
		tdim.setText(String.format("%d x %d = %d point%s", groi.getDimensions()[0], groi.getDimensions()[1], groi
				.getDimensions()[0]
				* groi.getDimensions()[1], groi.getDimensions()[0] * groi.getDimensions()[1] == 1 ? "" : "s"));
	}

	// spinner listeners
	private SelectionListener resolutionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			double xSpacing = getGridPrefs().getXPixelsFromMicronsLen(resx.getDouble());
			double ySpacing = getGridPrefs().getYPixelsFromMicronsLen(resy.getDouble());
			final GridROI groi = (GridROI) roi;

			if  (groi!=null) {
				groi.setxySpacing(xSpacing, ySpacing);
				sendCurrentROI(roi);
	
				getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						updatePlot();
						drawCurrentOverlay();
					}
				});
			}
		}
	};

	private SelectionListener startPosListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			
		    if (roi==null) return;
			roi.setPoint(getGridPrefs().getXPixelsFromMicronsCoord(spsx.getDouble()), getGridPrefs()
					.getYPixelsFromMicronsCoord(spsy.getDouble()));
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
			final GridROI groi = (GridROI) roi;

			if (groi!=null) {
				groi.setLengths(getGridPrefs().getXPixelsFromMicronsLen(splmaj.getDouble()), getGridPrefs()
						.getYPixelsFromMicronsLen(splmin.getDouble()));
				sendCurrentROI(roi);
	
				getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						splmaj.setDouble(getGridPrefs().getXMicronsFromPixelsLen(groi.getLengths()[0]));
						splmin.setDouble(getGridPrefs().getYMicronsFromPixelsLen(groi.getLengths()[1]));
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
			final GridROI groi = (GridROI) roi;
			if (groi!=null) {
				groi.setAngleDegrees(spang.getDouble());
				sendCurrentROI(roi);
	
				getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						updatePlot();
						drawCurrentOverlay();
					}
				});
			}
		}
	};

	@Override
	public GridROIList createROIList() {
		GridROIList list = new GridROIList();
		if (roiDataList != null) {
			for (ROIData rd: roiDataList) {
				list.add((GridROI) rd.getROI());
			}
		}
		return list;
	}

	@Override
	public ROIData createNewROIData(IROI roi) {
		return new GridROIData((GridROI) roi, data);
	}

	@Override
	public int updateGUI(GuiBean bean) {
		int update = 0;

		if (bean == null) {
			return update;
		}

		update = super.updateGUI(bean);

		if (bean.containsKey(GuiParameters.GRIDPREFERENCES)) {
			Object obj = bean.get(GuiParameters.GRIDPREFERENCES);

			if (obj instanceof GridPreferences) {
				GridPreferences prefs = (GridPreferences) obj;
				setGridPrefs(prefs);
				getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						updatePlot();
						drawEverything();
					}
				});
				updateAllSpinners(roi);

				update |= PREFS;
			}
		}

		return update;
	}

	private void sendPreferences(GridPreferences prefs) {
		if (prefs == null || roi == null) {
			return;
		}

		// plotView.pushGUIUpdate(GuiParameters.ROIDATA, new GridPreferences(prefs));
		((GridROI) roi).setGridPreferences(prefs);
		sendCurrentROI(roi);
	}

	@Override
	public Action createSwitchAction(final int index, final IPlotUI plotUI) {
		Action action = super.createSwitchAction(index, plotUI);
		action.setText("Mapping");
		action.setToolTipText("Switch side plot to grid scan mode");
		action.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/ProfileGrid.png"));

		return action;
	}

	@Override
	public void processPlotUpdate() {
		updateDataList();

		if (oProvider != null) {
			updatePlot();
			tViewer.setInput(roiDataList);
			drawEverything();
		}
	}

	@Override
	public void addToHistory() {
		// do nothing - grid scan does not use plots or history
	}

	@Override
	public void removeFromHistory() {
		// do nothing - grid scan does not use plots or history
	}

	private void drawEverything() {
		drawOverlays();
		drawCurrentOverlay();
		drawBeamlineCentre();
	}

	/**
	 * @param gridPrefs
	 *            The gridPrefs to set.
	 */
	private void setGridPrefs(GridPreferences gridPrefs) {

		if (gridPrefs == null) {
			this.gridPrefs = new GridPreferences();
		} else {
			this.gridPrefs = gridPrefs;
		}

		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.GRIDSCAN_RESOLUTION_X,  this.gridPrefs.getResolutionX());
		preferenceStore.setValue(PreferenceConstants.GRIDSCAN_RESOLUTION_Y,  this.gridPrefs.getResolutionY());
		preferenceStore.setValue(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX, this.gridPrefs.getBeamlinePosX());
		preferenceStore.setValue(PreferenceConstants.GRIDSCAN_BEAMLINE_POSY, this.gridPrefs.getBeamlinePosY());
	}

	/**
	 * @return Returns the gridPrefs.
	 */
	public GridPreferences getGridPrefs() {
		if (gridPrefs == null) {
			if (roi != null) {
				setGridPrefs(((GridROI) roi).getGridPreferences());
			} else {
				GridPreferences preferences = new GridPreferences();

				IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();

				preferences
						.setResolutionX(preferenceStore.isDefault(PreferenceConstants.GRIDSCAN_RESOLUTION_X) ? preferenceStore
								.getDefaultDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_X)
								: preferenceStore.getDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_X));

				preferences
						.setResolutionY(preferenceStore.isDefault(PreferenceConstants.GRIDSCAN_RESOLUTION_Y) ? preferenceStore
								.getDefaultDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_Y)
								: preferenceStore.getDouble(PreferenceConstants.GRIDSCAN_RESOLUTION_Y));

				preferences
						.setBeamlinePosX(preferenceStore.isDefault(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX) ? preferenceStore
								.getDefaultDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX)
								: preferenceStore.getDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSX));

				preferences
						.setBeamlinePosY(preferenceStore.isDefault(PreferenceConstants.GRIDSCAN_BEAMLINE_POSY) ? preferenceStore
								.getDefaultDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSY)
								: preferenceStore.getDouble(PreferenceConstants.GRIDSCAN_BEAMLINE_POSY));
				setGridPrefs(preferences);

			}
		}
		return gridPrefs;
	}

	@Override
	public void generateToolActions(IToolBarManager manager) {
		// TODO Auto-generated method stub
		
	}
}
