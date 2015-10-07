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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Vector3d;

import org.dawb.common.ui.plot.roi.ResolutionRingList;
import org.dawb.common.ui.plot.roi.data.ROIData;
import org.dawnsci.plotting.jreality.impl.DataSet3DPlot3D;
import org.dawnsci.plotting.jreality.tool.ImagePositionEvent;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ROIList;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.HandleStatus;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.LinearROIHandler;
import org.eclipse.dawnsci.analysis.dataset.roi.handler.RectangularROIHandler;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayType;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.VectorOverlayStyles;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.primitives.PrimitiveType;
import org.eclipse.dawnsci.plotting.api.jreality.tool.IImagePositionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.DSpacing;
import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.HistogramUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.IPlotUI;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.roi.ROIDataList;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import de.jreality.ui.viewerapp.SelectionEvent;
import de.jreality.ui.viewerapp.SelectionListener;

/**
 * Replaced by org.dawnsci.plotting.tools.diffraction
 */
@Deprecated
public class DiffractionViewer extends SidePlotProfile implements SelectionListener {
	private static Logger logger = LoggerFactory.getLogger(DiffractionViewer.class);
	public static String ID = "uk.ac.diamond.scisoft.analysis.rcp.DiffractionViewer";
	public static final double lineStep = 0.5;

	// data that is being plotted
	public DetectorProperties detConfig;
	public DiffractionCrystalEnvironment diffEnv;

	public DiffractionViewerSpotFit diffSpotFit;
	public HashMap<String, Object> metadata;
	private DiffractionViewerMetadata diffViewMetadata;
	private DiffractionPeakViewer peakViewer;
	private DiffractionSpotExaminer spotEximiner;
	private DiffractionNumericalSpotExaminer numericalSpotEximiner;

	private RectangularROI rectROI = null;
	private RectangularROIHandler rectRioHandler;
	private Action setDiffractionMode;

	@SuppressWarnings("unused")
	private String standardName, standardDistances;

	private DiffractionViewerResolutionRings resRingTable;
	private ArrayList<Integer> boxIDs;
	private ArrayList<Integer> ringID;
	private boolean isLine;
	private int[] beamCentrePrimitive = { -1, -1 };
	private int maskPrimID = -1;
	private boolean drawMask;
	public boolean beamVisible = false;
	private static int MAX_SIZE_3D_PLOT; // variable to control the max size of the 3D overlay
	@SuppressWarnings("unused")
	private int[] tempMousePos;
	private HistogramUpdate histoUpdate = null;
	private TabFolder peakAndMetadata;
	private int tabFolderIndex = 2;
	private double threshold = 0;
	private Action diffractionViewer;
	private int rectROISizeX, rectROISizeY;
	private boolean rectRoiBox = false;
	@SuppressWarnings("unused")
	private boolean draggedRectROI;

	private boolean beamRePosition;

	public DiffractionViewer() {
		super();
	}

	/**
	 * @param parent
	 * @wbp.parser.entryPoint
	 */
	@Override
	public void createPartControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		peakAndMetadata = new TabFolder(container, SWT.NONE);

		TabItem lineProfile = new TabItem(peakAndMetadata, SWT.NONE);
		lineProfile.setText("Line Profile");
		diffSpotFit = new DiffractionViewerSpotFit(peakAndMetadata, SWT.NONE, this);
		addPropertyListeners();
		setFittingDefaults();
		setThresholdDefaullts();
		lineProfile.setControl(diffSpotFit);

		TabItem resRing = new TabItem(peakAndMetadata, SWT.NONE);
		resRing.setText("Resolution Ring");
		resRingTable = new DiffractionViewerResolutionRings(peakAndMetadata, SWT.NONE, this);
		resRing.setControl(resRingTable);

		peakAndMetadata.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {

			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				setRectRegionSize(peakAndMetadata.getSelectionIndex());
			}

			@Override
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
				widgetSelected(e);
			}
		});
		TabItem metadata = new TabItem(peakAndMetadata, SWT.NONE);
		metadata.setText("Image Metadata");
		diffViewMetadata = new DiffractionViewerMetadata(peakAndMetadata, SWT.NONE, this);
		metadata.setControl(diffViewMetadata);

		TabItem peakTab = new TabItem(peakAndMetadata, SWT.NONE);
		peakTab.setText("Peak Profile");
		peakViewer = new DiffractionPeakViewer(peakAndMetadata, SWT.NONE);
		peakTab.setControl(peakViewer);

		TabItem spotTab = new TabItem(peakAndMetadata, SWT.NONE);
		spotTab.setText("Spot View");
		spotEximiner = new DiffractionSpotExaminer(peakAndMetadata, SWT.NONE);
		spotTab.setControl(spotEximiner);

		TabItem numericalSpotTab = new TabItem(peakAndMetadata, SWT.NONE);
		numericalSpotTab.setText("Numerical View");
		numericalSpotEximiner = new DiffractionNumericalSpotExaminer(peakAndMetadata, SWT.NONE);
		numericalSpotTab.setControl(numericalSpotEximiner);

		peakAndMetadata.pack();

		// end of GUI construction
		if (histoUpdate != null)
			peakViewer.sendHistogramUpdate(histoUpdate);

		MAX_SIZE_3D_PLOT = (int) (DataSet3DPlot3D.MAXDIM * DataSet3DPlot3D.MAXDIM * 0.7);

		roiHandler = new LinearROIHandler((LinearROI) roi);

		roiIDs = new ArrayList<Integer>();
		dragIDs = new ArrayList<Integer>();
		ringID = new ArrayList<Integer>();

		if (roiDataList == null)
			roiDataList = new ROIDataList();
		roisIDs = new ArrayList<Integer>();

		rectRioHandler = new RectangularROIHandler(rectROI);
		boxIDs = new ArrayList<Integer>();

		// colours
		oColour = new Color(0, 255, 0);
	}

	protected void setRectRegionSize(int tabSelected) {
		// there can only be a single selection but check

		// if (tabSelected <= 2) {
		// rectROISizeX = (int) Math.floor(Math.sqrt(MAX_SIZE_3D_PLOT) * 0.1);
		// rectROISizeY = rectROISizeX;
		// }
		if (tabSelected <= 4) {
			rectROISizeX = 50;
			rectROISizeY = 50;
		}
		if (tabSelected == 5) {
			rectROISizeX = 30;
			rectROISizeY = 30;
		}

	}

	private void setThresholdDefaullts() {
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		threshold = preferenceStore.getInt(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD);
	}

	private void setFittingDefaults() {
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		String peak = preferenceStore.getString(PreferenceConstants.DIFFRACTION_VIEWER_PEAK_TYPE);
		int maxNum = preferenceStore.getInt(PreferenceConstants.DIFFRACTION_VIEWER_MAX_PEAK_NUM);
		Boolean autoStopping = preferenceStore.getBoolean(PreferenceConstants.DIFFRACTION_VIEWER_AUTOSTOPPING);
		int threashold = preferenceStore.getInt(PreferenceConstants.DIFFRACTION_VIEWER_STOPPING_THRESHOLD);
		diffSpotFit.pushPreferences(peak, maxNum, autoStopping, threashold);
	}

	@Override
	public void dispose() {
		super.dispose();
		// resRingTable.dispose();
		diffViewMetadata.dispose();
		diffSpotFit.dispose();
		peakViewer.dispose();
		spotEximiner.dispose();
	}

	/*
	 * methods inherited from sideplot
	 */
	@Override
	public void addToHistory() {
		// do nothing

	}

	@Override
	public Action createSwitchAction(final int index, final IPlotUI plotUI) {
		Action action = super.createSwitchAction(index, plotUI);

		action.setText("Diffraction Viewer");
		action.setToolTipText("Switch side plot to Diffraction Viewer");
		action.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/ruby.png"));
		return action;
	}

	@Override
	public void processPlotUpdate() {
		updateDataList();
		if (oProvider != null) {

			showOverlays(); // draggedOverlays on main plot
			updatePlot(); // updates the peakFitting sideplot
			updatePlot3D(); // updates the area dragging components
		}
	}

	@Override
	public void removeFromHistory() {
		// do nothing
	}

	@Override
	public ROIList<? extends IROI> createROIList() {
		return null;
	}

	@Override
	public ROIData createNewROIData(IROI roi) {
		return null;
	}

	@Override
	public int updateGUI(GuiBean bean) {
		int update = 0;

		if (bean == null)
			return update;

		if (bean.containsKey(GuiParameters.ROIDATA)) {
			Object obj = bean.get(GuiParameters.ROIDATA);

			if (obj instanceof LinearROI) {
				hideCurrent();
				roi = (LinearROI) obj;
				roiHandler.setROI(roi);
				getControl().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						updatePlot();
						updatePlot3D();
						drawCurrentOverlay();
						drawMask();
						if (rectROI != null)
							drawRectangleOverlay(rectROI);
					}
				});
				update |= ROI;
			}
		}

		return update;
	}

	@Override
	public void showOverlays() {
		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				drawCurrentOverlay();
				drawOverlays();
				resRingTable.redrawExistingRings();
				drawBeamCentre(beamVisible);
				showMask(resRingTable.isMaskToggled());

			}
		});
	}

	@Override
	public void showSidePlot() {
		// override superclass behaviour not to get GUI bean
		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				updateDataList();
				updatePlot();
				updatePlot3D();
				peakAndMetadata.setSelection(tabFolderIndex);
			}
		});

		showOverlays();
	}

	@Override
	protected void drawOverlays() {
		if (rectROI != null)
			drawRectangleOverlay(rectROI);
	}

	@Override
	protected void drawCurrentOverlay() {

		if (oProvider == null || roi == null)
			return;

		if (roisIDs.isEmpty()) {
			roisIDs.add(-1); // arrow
		}

		int id, index = 0;

		final LinearROI lroi = (LinearROI) roi;
		int[] spt = lroi.getIntPoint();
		int[] ept = lroi.getIntEndPoint();

		// draw arrow
		oProvider.begin(OverlayType.VECTOR2D);
		id = roisIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.ARROW);
			roisIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		oProvider.drawArrow(id, spt[0], spt[1], ept[0], ept[1]);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);
		oProvider.setLineThickness(id, oThickness);
		oProvider.end(OverlayType.VECTOR2D);
	}

	@Override
	protected void updatePlot(IROI roi) {
		// this will pass a dataset to the gui that will peakfit and plot the lineraROI
		if (roi != null)
			diffSpotFit.processROI(data, (LinearROI) roi);
	}

	private void updatePlot3D() {
		if (rectROI == null || mainPlotter == null)
			return;
		getControl().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				spotEximiner.processROI(data, rectROI);

			}
		});
		getControl().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				peakViewer.processROI(data, rectROI);
			}
		});
		getControl().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				numericalSpotEximiner.populateTable(data, rectROI);
			}
		});
	}

	/*
	 * This section will control what happens when there is an action on the image
	 */

	@Override
	public void imageStart(IImagePositionEvent event) {
		HandleStatus hStatus = HandleStatus.NONE;

		if (roi == null) {
			roi = new LinearROI();
			roi.setPlot(true);
			roiHandler.setROI(roi);
			setROIName("Diffraction ", roi);
			// roiHandler = new LinearROIHandler((LinearROI) roi);
		}

		int id = ((ImagePositionEvent) event).getPrimitiveID();
		final short flags = ((ImagePositionEvent) event).getFlags();
		cpt = ((ImagePositionEvent) event).getImagePosition();

		if ((flags & IImagePositionEvent.LEFTMOUSEBUTTON) != 0) {
			if ((flags & IImagePositionEvent.CTRLKEY) != 0 && maskPrimID != -1) {
				if (maskPrimID == id) {
					drawMask = true;
					dragging = true;
					isLine = false;
				}
			} else if ((flags & IImagePositionEvent.SHIFTKEY) != 0 && beamVisible) {
				beamRePosition = true;
				dragging = true;
				isLine = false;
				diffViewMetadata.cacheMetadata();
				// drawBeamCentre(true);

			} else {
				int dragHandle = -1;
				if (id == -1 || !roiHandler.contains(id)) {
					// new ROI mode
					diffSpotFit.removePrimitives();
					roi.setPoint(cpt);
					hideCurrent();

					dragging = true;
					isLine = true;
				} else if (roiHandler.contains(id)) {
					int h = roiHandler.indexOf(id);

					if (h == 0 || h == 2) {
						hStatus = HandleStatus.RESIZE;
						if ((flags & IImagePositionEvent.SHIFTKEY) != 0) {
							hStatus = HandleStatus.REORIENT;
						}
					} else if (h == 1) {
						hStatus = HandleStatus.RMOVE;
						if ((flags & IImagePositionEvent.SHIFTKEY) != 0) {
							hStatus = HandleStatus.ROTATE;
						}
					}
					hideCurrent();
					drawDraggedOverlay(roi);
					dragging = true;
					dragHandle = h; // store dragged handle
					isLine = true;
				}
				roiHandler.configureDragging(dragHandle, hStatus);
			}
		}
		if ((flags & IImagePositionEvent.RIGHTMOUSEBUTTON) != 0) {

			if (mainPlotter instanceof DataSetPlotter) {
				((DataSetPlotter) mainPlotter).getComposite().getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						((DataSetPlotter) mainPlotter).getComposite().setFocus();
					}
				});
			}
			if (rectROI == null) {
				rectROI = new RectangularROI();
			}
			if ((flags & IImagePositionEvent.CTRLKEY) != 0) {
				rectROI.setPoint(cpt);
				dragging = true;
				isLine = false;
				draggedRectROI = true;
				rectRioHandler.configureDragging(-1, HandleStatus.CMOVE);
				rectRoiBox = false;
			} else {
				double[] point = new double[] { cpt[0], cpt[1] };
				rectROI.setMidPoint(point);
				rectROI.setLengths(rectROISizeX, rectROISizeY);
				drawRectangleOverlay(rectROI);
				rectRoiBox = true;
				draggedRectROI = false;
				dragging = false;
			}
		}
	}

	@Override
	public void imageDragged(IImagePositionEvent event) {
		if (rectRoiBox) {
			if (System.currentTimeMillis() >= nextTime) {
				nextTime = System.currentTimeMillis() + updateInterval;
				double[] temp = { ((ImagePositionEvent) event).getImagePosition()[0],
						((ImagePositionEvent) event).getImagePosition()[1] };
				rectROI.setMidPoint(temp);
				rectROI.setLengths(rectROISizeX, rectROISizeY);
				drawRectangleOverlay(rectROI);
			}
		} else if (dragging) {

			if (beamRePosition) {
				if (System.currentTimeMillis() >= nextTime) {
					nextTime = System.currentTimeMillis() + updateInterval;
					updateBeamCentre(event);
				}
			} else if (drawMask && maskPrimID != -1) {
				if (System.currentTimeMillis() >= nextTime) {
					nextTime = System.currentTimeMillis() + updateInterval;
					updateMask(event);
				}
			} else {
				if (isLine) {
					final IROI croi = roiHandler.interpretMouseDragging(cpt, ((ImagePositionEvent) event).getImagePosition());
					if (croi != null) {
						drawDraggedOverlay(croi);
						if (System.currentTimeMillis() >= nextTime) {
							nextTime = System.currentTimeMillis() + updateInterval;
							// updatePlot(croi); don't update plot with dragging
							sendCurrentROI(croi);
						}
					}
				} else {
					final RectangularROI rroi = rectRioHandler.interpretMouseDragging(cpt, ((ImagePositionEvent) event)
							.getImagePosition());
					if (rroi != null) {
						if (System.currentTimeMillis() >= nextTime) {
							nextTime = System.currentTimeMillis() + updateInterval;
							if ((rroi.getIntLengths()[0] * rroi.getIntLengths()[1]) < MAX_SIZE_3D_PLOT) {
								tempMousePos = event.getImagePosition();
								drawRectangleOverlay(rroi);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void imageFinished(IImagePositionEvent event) {
		if (dragging) {
			// draggedRectROI = false;
			dragging = false;
			if (drawMask) {
				drawMask = false;
			}
			if (beamRePosition) {
				beamRePosition = false;
				updateBeamCentre(event);
			} else if (isLine) {
				hideIDs(dragIDs);
				roi = roiHandler.interpretMouseDragging(cpt, ((ImagePositionEvent) event).getImagePosition());
				roiHandler.setROI(roi);
				roiHandler.unconfigureDragging();

				drawCurrentOverlay();
				// sendCurrentROI(roi);
				updatePlot();
			} else {
				rectROI = rectRioHandler.interpretMouseDragging(cpt, ((ImagePositionEvent) event).getImagePosition());
				// rectROI = interpretMouseDraggingRectangle(tempMousePos);
				rectRioHandler.setROI(rectROI);
				rectRioHandler.unconfigureDragging();

				// sendCurrentROI(rectROI);
				updatePlot3D();
			}
		}
		if (rectRoiBox) {
			rectRoiBox = false;
			// draggedRectROI = false;
			dragging = false;
			rectRioHandler.setROI(rectROI);
			drawRectangleOverlay(rectROI);
			updatePlot3D();
		}
	}

	private void updateMask(IImagePositionEvent event) {
		int[] point = event.getImagePosition();
		int[] beam = detConfig.pixelCoords(detConfig.getBeamCentrePosition());
		int radius = (int) Math.sqrt(((point[0] - beam[0]) * (point[0] - beam[0]))
				+ ((point[1] - beam[1]) * (point[1] - beam[1])));

		oProvider.begin(OverlayType.VECTOR2D);
		try {
			oProvider.drawRing(maskPrimID, beam[0], beam[1], radius, detConfig.distToClosestEdgeInPx() * 2);
		} catch (IllegalStateException e) {
			// do nothing when no ring
		}
		oProvider.end(OverlayType.VECTOR2D);
	}

	private void updateBeamCentre(IImagePositionEvent event) {
		int[] point = event.getImagePosition();
		point[0] = (int) (point[0] * detConfig.getHPxSize());
		point[1] = (int) (point[1] * detConfig.getVPxSize());
		Vector3d newOri = new Vector3d(point[0], point[1], detConfig.getOrigin().z);
		detConfig.setOrigin(newOri);
		diffViewMetadata.updateBeamPositionFromDragging();
		updateDiffractionObjects(true);
	}

	/**
	 * Draw dragged out overlay for given region of interest
	 * 
	 * @param roib
	 */
	private void drawDraggedOverlay(IROI roib) {
		// draws the line
		if (oProvider == null)
			return;

		final LinearROI lroi = (LinearROI) roib;
		int[] spt = lroi.getIntPoint();
		int[] ept = lroi.getIntEndPoint();

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
		oProvider.end(OverlayType.VECTOR2D);
	}

	private void drawRectangleOverlay(RectangularROI rroi) {
		if (oProvider == null)
			return;

		int[] spx = rroi.getIntPoint();
		double[] epx = rroi.getEndPoint();
		if (boxIDs.isEmpty()) {
			boxIDs.add(-1);
			boxIDs.add(-1);
		}

		int id, index;
		index = 0;

		// box
		id = boxIDs.get(index);
		if (id == -1) {
			id = oProvider.registerPrimitive(PrimitiveType.BOX);
			boxIDs.set(index, id);
			if (id == -1)
				return;
		} else
			oProvider.setPrimitiveVisible(id, true);
		index++;

		oProvider.begin(OverlayType.VECTOR2D);
		oProvider.drawBox(id, spx[0], spx[1], (int) epx[0], (int) epx[1]);
		oProvider.setColour(id, oColour);
		oProvider.setTransparency(id, oTransparency);
		oProvider.end(OverlayType.VECTOR2D);

	}

	public void clearRings() {
		removeIDs(ringID);
		ringID.clear();
	}

	/*
	 * handle ring drawing, removal and clearing
	 */
	private void drawRings(ResolutionRingList ringList) {
		oProvider.begin(OverlayType.VECTOR2D);
		for (int i = 0; i < ringList.size(); i++) {
			int tempID = oProvider.registerPrimitive(PrimitiveType.CIRCLE);
			int[] beam = detConfig.pixelCoords(detConfig.getBeamCentrePosition());
			int radius = (int) DSpacing.radiusFromDSpacing(detConfig, diffEnv, ringList.get(i).getResolution());
			oProvider.setColour(tempID, ringList.get(i).getAWTColour());
			oProvider.setStyle(tempID, VectorOverlayStyles.OUTLINE);
			oProvider.setTransparency(tempID, oTransparency);
			oProvider.setLineThickness(tempID, oThickness);
			oProvider.setPrimitiveVisible(tempID, ringList.get(i).isVisible());
			oProvider.drawCircle(tempID, beam[0], beam[1], radius);
			ringID.add(tempID);
		}
		oProvider.end(OverlayType.VECTOR2D);
	}

	public void drawBeamCentre(boolean visible) {
		diffViewMetadata.showBeamCentre(visible);
		resRingTable.showBeamCentre(visible);

		oProvider.begin(OverlayType.VECTOR2D);
		if (visible && detConfig != null) {
			if (beamCentrePrimitive[0] == -1)
				beamCentrePrimitive[0] = oProvider.registerPrimitive(PrimitiveType.CIRCLE);
			if (beamCentrePrimitive[1] == -1)
				beamCentrePrimitive[1] = oProvider.registerPrimitive(PrimitiveType.CIRCLE);
			int[] beamCentre = detConfig.pixelCoords(detConfig.getBeamCentrePosition());
			int radius = (int) (1 + Math.sqrt(detConfig.getPx() * detConfig.getPx() + detConfig.getPy()
					* detConfig.getPy()) * 0.01);
			oProvider.setColour(beamCentrePrimitive[0], Color.RED);
			oProvider.setTransparency(beamCentrePrimitive[0], 0.6);
			oProvider.setLineThickness(beamCentrePrimitive[0], 3);
			oProvider.setOutlineColour(beamCentrePrimitive[0], Color.MAGENTA);
			oProvider.setPrimitiveVisible(beamCentrePrimitive[0], true);
			oProvider.drawCircle(beamCentrePrimitive[0], beamCentre[0], beamCentre[1], radius);
			// Inner circle
			oProvider.setColour(beamCentrePrimitive[1], Color.YELLOW);
			oProvider.setTransparency(beamCentrePrimitive[1], 0.4);
			oProvider.setPrimitiveVisible(beamCentrePrimitive[1], true);
			oProvider.drawCircle(beamCentrePrimitive[1], beamCentre[0], beamCentre[1], radius * 0.3);
		}
		if (!visible && beamCentrePrimitive[0] != -1 && beamCentrePrimitive[1] != -1) {
			oProvider.unregisterPrimitive(beamCentrePrimitive[0]);
			oProvider.unregisterPrimitive(beamCentrePrimitive[1]);
			beamCentrePrimitive[0] = -1;
			beamCentrePrimitive[1] = -1;
		}
		oProvider.end(OverlayType.VECTOR2D);
	}

	private void hideBeamCentre() {
		oProvider.setPrimitiveVisible(beamCentrePrimitive[0], false);
		oProvider.setPrimitiveVisible(beamCentrePrimitive[1], false);
	}

	//
	// public void removeBeamCentre() {
	// oProvider.unregisterPrimitive(beamCentrePrimative[0]);
	// oProvider.unregisterPrimitive(beamCentrePrimative[1]);
	// beamCentrePrimative[0] = -1;
	// beamCentrePrimative[1] = -1;
	// }

	public void updateRings(ResolutionRingList ringList) {
		clearRings();
		drawRings(ringList);
	}

	public void ringsVisible(boolean visible) {
		if (ringID.isEmpty())
			return;
		for (int i = 0; i < ringID.size(); i++) {
			oProvider.setPrimitiveVisible(ringID.get(i), visible);
		}

		getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				drawCurrentOverlay();
			}
		});

	}

	/*
	 * Control what happens when an editor is changed -- not currently in use.
	 */
	@Override
	public void selectionChanged(SelectionEvent arg0) {
	}

	@Override
	public void applyEditorValue() {
	}

	@Override
	public void processHistogramUpdate(HistogramUpdate update) {
		histoUpdate = update;
		peakViewer.sendHistogramUpdate(update);
		spotEximiner.sendHistogramUpdate(update);
	}

	public void drawMask() {
		if (detConfig == null)
			return;

		int[] beamCentre = detConfig.pixelCoords(detConfig.getBeamCentrePosition());
		oProvider.begin(OverlayType.VECTOR2D);
		if (maskPrimID == -1) {
			try {
				int radius = detConfig.distToClosestEdgeInPx();
				maskPrimID = oProvider.registerPrimitive(PrimitiveType.RING);
				oProvider.setColour(maskPrimID, Color.RED);
				oProvider.setTransparency(maskPrimID, 0.8);
				oProvider.setLineThickness(maskPrimID, 3);
				oProvider.drawRing(maskPrimID, beamCentre[0], beamCentre[1], radius, radius * 5);
			} catch (IllegalStateException e) {
				// do nothing when no ring
			}
		}
		oProvider.setPrimitiveVisible(maskPrimID, true);
		oProvider.end(OverlayType.VECTOR2D);
	}

	public void hideMask() {
		oProvider.begin(OverlayType.VECTOR2D);
		oProvider.setPrimitiveVisible(maskPrimID, false);
		oProvider.end(OverlayType.VECTOR2D);
	}

	private void showMask(boolean maskEnabled) {
		if (maskEnabled && resRingTable.isRingToggled())
			drawMask();
		if (!maskEnabled)
			hideMask();
	}

	public void removeMask() {
		oProvider.unregisterPrimitive(maskPrimID);
		maskPrimID = -1;
	}

	@Override
	protected void updateAllSpinners(IROI roi) {

	}

	@Override
	public void hideOverlays() {
		if (oProvider == null)
			return;

		super.hideOverlays();
		hideBeamCentre();
		hideMask();
		hideIDs(boxIDs);
		hideIDs(ringID);
	}

	@Override
	public void removePrimitives() {
		tabFolderIndex = peakAndMetadata.isDisposed() ? 0 : peakAndMetadata.getSelectionIndex();

		super.removePrimitives();
		if (oProvider == null)
			return;

		oProvider.unregisterPrimitive(beamCentrePrimitive[0]);
		oProvider.unregisterPrimitive(beamCentrePrimitive[1]);
		oProvider.unregisterPrimitive(boxIDs);
		boxIDs.clear();
		clearRings();
		removeMask();
	}

	@Override
	protected void updateDataList() {
		boolean metadataSetup = false;
		if (getDataset()) {

			diffViewMetadata.setDatasetInformation(data.max().doubleValue(), data.min().doubleValue(),
					(Double) data.mean());
			setThreshold();
			try {
				IMetadata localMetaData = data.getMetadata();
				if (localMetaData instanceof IDiffractionMetadata) {
					IDiffractionMetadata localDiffractionMetaData = (IDiffractionMetadata)localMetaData;
					detConfig = localDiffractionMetaData.getDetector2DProperties();
					diffEnv = localDiffractionMetaData.getDiffractionCrystalEnvironment();
					diffViewMetadata.setMetadata();
					metadataSetup = true;
				}
			} catch (Exception e) {
				logger.error("Could not create diffraction experiment objects");
			}

			if (!metadataSetup) {
				diffViewMetadata.setDatasetInformation(0.0, 0.0, 0.0);
				cleanUpDiffractionObjects();
				logger.warn("The metadata associated with this image appears to be corrupt or "
						+ "not in the expected format");
			}
		}
	}

	private void setThreshold() {
		IMetadata md = data.getMetadata();
		if (md != null) {
			if (mainPlotter instanceof DataSetPlotter) {
				try {
					Serializable s = md.getMetaValue("NXdetector:pixel_overload");
					Double threshold = null;

					if (s instanceof String) {
						threshold = Double.valueOf((String) s);
					} else if (s instanceof Number) {
						threshold = ((Number) s).doubleValue();
					}

					if (threshold != null) {
						((DataSetPlotter) mainPlotter).setOverloadThreshold(threshold);
						diffViewMetadata.setThreshold(threshold);
					}
					return;
				} catch (Exception e) {
				}
			}
		}

		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		double thresholdFromPrefs;
		if (preferenceStore.isDefault(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD))
			thresholdFromPrefs = preferenceStore
					.getDefaultDouble(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD);
		else
			thresholdFromPrefs = preferenceStore
					.getDouble(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD);
		if (mainPlotter instanceof DataSetPlotter) {
			((DataSetPlotter) mainPlotter).setOverloadThreshold(thresholdFromPrefs);
			diffViewMetadata.setThreshold(thresholdFromPrefs);
		}
	}

	private void cleanUpDiffractionObjects() {
		detConfig = null;
		diffEnv = null;
		diffViewMetadata.setMetadata();
	}

	private void addPropertyListeners() {
		AnalysisRCPActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
				if (property.equals(PreferenceConstants.DIFFRACTION_VIEWER_PEAK_TYPE)
						|| property.equals(PreferenceConstants.DIFFRACTION_VIEWER_MAX_PEAK_NUM)
						|| property.equals(PreferenceConstants.DIFFRACTION_VIEWER_AUTOSTOPPING)
						|| property.equals(PreferenceConstants.DIFFRACTION_VIEWER_STOPPING_THRESHOLD)) {

					String peakName;
					if (preferenceStore.isDefault(PreferenceConstants.DIFFRACTION_VIEWER_PEAK_TYPE))
						peakName = preferenceStore.getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_PEAK_TYPE);
					else
						peakName = preferenceStore.getString(PreferenceConstants.DIFFRACTION_VIEWER_PEAK_TYPE);

					int numPeaks;
					if (preferenceStore.isDefault(PreferenceConstants.DIFFRACTION_VIEWER_MAX_PEAK_NUM))
						numPeaks = preferenceStore.getDefaultInt(PreferenceConstants.DIFFRACTION_VIEWER_MAX_PEAK_NUM);
					else
						numPeaks = preferenceStore.getInt(PreferenceConstants.DIFFRACTION_VIEWER_MAX_PEAK_NUM);

					boolean autoStopping;
					if (preferenceStore.isDefault(PreferenceConstants.DIFFRACTION_VIEWER_AUTOSTOPPING))
						autoStopping = preferenceStore
								.getDefaultBoolean(PreferenceConstants.DIFFRACTION_VIEWER_AUTOSTOPPING);
					else
						autoStopping = preferenceStore.getBoolean(PreferenceConstants.DIFFRACTION_VIEWER_AUTOSTOPPING);

					int stoppingThreashold;
					if (preferenceStore.isDefault(PreferenceConstants.DIFFRACTION_VIEWER_STOPPING_THRESHOLD))
						stoppingThreashold = preferenceStore
								.getDefaultInt(PreferenceConstants.DIFFRACTION_VIEWER_STOPPING_THRESHOLD);
					else
						stoppingThreashold = preferenceStore
								.getInt(PreferenceConstants.DIFFRACTION_VIEWER_STOPPING_THRESHOLD);

					diffSpotFit.pushPreferences(peakName, numPeaks, autoStopping, stoppingThreashold);

				} else if (property.equals(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES)
						|| property.equals(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME)) {

					if (preferenceStore.isDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME))
						standardName = preferenceStore
								.getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME);
					else
						standardName = preferenceStore.getDefaultString(preferenceStore
								.getString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME));

					if (preferenceStore.isDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES))
						standardDistances = preferenceStore
								.getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES);
					else
						standardDistances = preferenceStore
								.getString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES);
				} else if (property.equals(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD)) {
					if (preferenceStore.isDefault(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD))
						threshold = preferenceStore
								.getDefaultInt(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD);
					else
						threshold = preferenceStore
								.getInt(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD);
					if (mainPlotter instanceof DataSetPlotter) {
						((DataSetPlotter) mainPlotter).setOverloadThreshold(threshold);
						diffViewMetadata.setThreshold(threshold);
					}
				}
			}
		});
	}

	@Override
	public void generateMenuActions(IMenuManager manager, final IWorkbenchPartSite site) {

		diffractionViewer = new Action() {
			@Override
			public void run() {
				IHandlerService handler = (IHandlerService) site.getService(IHandlerService.class);
				try {
					handler.executeCommand("uk.ac.diamond.scisoft.analysis.rcp.ViewDiffractionViewerSettings", null);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
		};
		diffractionViewer.setText("Preferences");

		manager.add(diffractionViewer);
	}

	@Override
	public void generateToolActions(IToolBarManager manager) {
		setDiffractionMode = new Action("", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (mainPlotter instanceof DataSetPlotter) {
					((DataSetPlotter) mainPlotter).setDiffractionMode(setDiffractionMode.isChecked());
					((DataSetPlotter) mainPlotter).setOverloadThreshold(threshold);
				}
			}
		};
		setDiffractionMode.setText("Enable/Disable diffraction mode");
		setDiffractionMode.setDescription("Enable / Disable diffraction mode");
		setDiffractionMode.setImageDescriptor(AnalysisRCPActivator.getImageDescriptor("icons/flag_red.png"));
		manager.add(setDiffractionMode);
	}

	public void updateDiffractionObjects(final boolean beamCentreVisible) {
		getControl().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				resRingTable.redrawExistingRings();
				drawBeamCentre(beamCentreVisible || resRingTable.isBeamCentreToggled());
				updatePlot();
				showMask(resRingTable.isMaskToggled());
				if (mainPlotter instanceof DataSetPlotter) {
					if (detConfig != null && diffEnv != null)
						((DataSetPlotter) mainPlotter).setQSpace(new QSpace(detConfig, diffEnv));
					else
						((DataSetPlotter) mainPlotter).setQSpace(null);
				}
			}
		});

	}
}
