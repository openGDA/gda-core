/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.medipix;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.beans.exafs.DetectorROI;
import uk.ac.gda.beans.medipix.MedipixParameters;
import uk.ac.gda.beans.medipix.ROIRegion;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.LiveStreamView;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.FauxRichBeansEditor;

public class MedipixParametersUIEditor extends FauxRichBeansEditor<MedipixParameters> {

	private static final Logger logger = LoggerFactory.getLogger(MedipixParametersUIEditor.class);
	private MedipixParameters medipixParameters;
	public static final String MEDIPIX_CAMERA_CONFIG_NAME = "medipix_camera_config";
	private TableViewer roiTableViewer;
	private Composite widgetComposite;
	private Composite parent;

	public MedipixParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, MedipixParameters editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		this.medipixParameters = editingBean;
	}

	@Override
	protected String getRichEditorTabText() {
		return "Medipix";
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		parent.setLayout(new GridLayout() );
		parent.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));

		widgetComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(widgetComposite);

		Group regionsGroup = new Group(widgetComposite, SWT.NONE);
		regionsGroup.setText("Regions");
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(regionsGroup);

		// Button to open TwoDArrayView window for detector
		final Button openRoiButton = new Button(regionsGroup, SWT.NONE);
		openRoiButton.setText("Open Medipix");
		openRoiButton.setToolTipText("Open detector ROI window and apply ROI values to detector");
		openRoiButton.addListener(SWT.Selection, e -> {
			if (!showLiveStreamWarning()) {
				addRoisToPlot(medipixParameters.getRegionList());
			}
		});

		// Button to copy ROIs from TwoDArrayView into local parameters
		final Button getRoiButton = new Button(regionsGroup, SWT.NONE);
		getRoiButton.setText("Get ROI from Medipix");
		getRoiButton.setToolTipText("Get ROIs currently set on detector and copy into table");
		getRoiButton.addListener(SWT.Selection, e -> {
			if (!showLiveStreamWarning()) {
				List<ROIRegion> regionList = getRoisFromPlot();
				medipixParameters.setRegionList(regionList);
				updateGui();
			}
		});

		// Button to setup detector (i.e. TwoDArrayView) ROIs from GUI values
		final Button setRoiButton = new Button(regionsGroup, SWT.NONE);
		setRoiButton.setText("Apply ROI from table");
		setRoiButton.setToolTipText("Apply ROI values from table to detector");
		setRoiButton.addListener(SWT.Selection, e -> {
			if (!showLiveStreamWarning()) {
				addRoisToPlot(medipixParameters.getRegionList());
			}
		});

		roiTableViewer = createTable(widgetComposite);
		roiTableViewer.setInput(medipixParameters.getRegionList());

		createAddDeleteRoiControls(widgetComposite);

		// Force a re-layout of the widget now that everything has been added
		GridUtils.layoutFull(widgetComposite.getParent());
	}

	/**
	 * Enum with names of the columns in the ROI region table
	 */
	private enum RoiTableColumns {
		ROI_NAME("ROI name"),
		X_START("X start"),
		Y_START("Y start"),
		WIDTH("Width"),
		HEIGHT("Height");

		private final String name;

		private RoiTableColumns(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		/**
		 * Extract a value from ROIRegion for this column
		 *
		 * @param roi
		 * @return string containing the value
		 */
		public String getValue(ROIRegion roi) {
			Object val = switch(this) {
				case ROI_NAME -> roi.getRoiName();
				case X_START -> roi.getXRoi().getRoiStart();
				case Y_START -> roi.getYRoi().getRoiStart();
				case WIDTH -> roi.getXRoi().getRoiSize();
				case HEIGHT -> roi.getYRoi().getRoiSize();
			};
			return String.valueOf(val);
		}

		/**
		 *  Update the name, x/y start/end values in a ROIregion based on supplied value.
		 *
		 * @param roiRegion
		 * @param newValue
		 */
		public void updateRoiRegion(ROIRegion roiRegion, Object newValue) {
			if (this == RoiTableColumns.ROI_NAME) {
				roiRegion.setRoiName(newValue.toString());
				return;
			}

			// get the DetectorROI for the x or y direction
			DetectorROI detRoi = this == RoiTableColumns.X_START || this == RoiTableColumns.WIDTH
								? roiRegion.getXRoi() : roiRegion.getYRoi();

			int intValue = Integer.parseInt(newValue.toString());

			if (this == RoiTableColumns.X_START || this == RoiTableColumns.Y_START) {
				// update start position (update end position to keep size the same)
				int currentRoiSize = detRoi.getRoiSize();
				detRoi.setRoiStart(intValue);
				detRoi.setRoiEnd(intValue + currentRoiSize);
			} else {
				//update the size (keep start position fixed, change roiEnd to get new size)
				detRoi.setRoiEnd(detRoi.getRoiStart() + intValue);
			}
		}
	}

	private static class RoiRowLabelProvider extends ColumnLabelProvider {
		private RoiTableColumns roiColumn;
		public RoiRowLabelProvider(RoiTableColumns t) {
			roiColumn = t;
		}
		@Override
		public String getText(Object element) {
			ROIRegion rowValue = (ROIRegion) element;
			return roiColumn.getValue(rowValue);
		}
	}

	private static class RoiRowCellEditor extends TextCellEditor {
		private boolean integerValues;

		public RoiRowCellEditor(Composite composite) {
			super(composite);
		}
		public void setIntegerValues(boolean integerValues) {
			this.integerValues = integerValues;
			if (integerValues) {
				setValidator(this::isValidInteger);
			}
		}

		@Override
		public Object doGetValue() {
			// Get value from widget (cast from string to integer if necessary)
			Object value = super.doGetValue();
			logger.debug("Set value : {}", value);
			if (integerValues) {
				value = Integer.parseInt((String)value);
			}
			return value;
		}

		@Override
		public void doSetValue(Object value) {
			// value to set is single value to be applied to Textbox?
			logger.debug("Set value : {}", value);
			super.doSetValue(value.toString());
		}

		// Validator to check for valid integer values
		private String isValidInteger(Object object) {
			if (object instanceof Integer) {
				return null;
			} else {
				try {
					String string = (String) object;
					int value = Integer.parseInt(string);
					if (value < 0) {
						return "Value cannot be less than 0";
					}
					return null;
				} catch (NumberFormatException exception) {
					return exception.getMessage();
				}
			}
		}
	}

	private class RoiRowEditingSupport extends EditingSupport {
		private RoiTableColumns roiColumn; // the type of data to be edited in this column of the table

		public RoiRowEditingSupport(ColumnViewer viewer, RoiTableColumns roiColumn) {
			super(viewer);
			this.roiColumn = roiColumn;
		}

		// Called to update value in model from value in edited cell of table
		@Override
		public void setValue(Object element, Object value) {
			ROIRegion param = (ROIRegion) element;
			roiColumn.updateRoiRegion(param, value);
			getViewer().update(param, null);
			updateGui();
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			RoiRowCellEditor editor = new RoiRowCellEditor((Composite) getViewer().getControl());
			editor.setIntegerValues(roiColumn != RoiTableColumns.ROI_NAME);
			return editor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			ROIRegion param = (ROIRegion) element;
			return roiColumn.getValue(param);
		}
	}

	/**
	 * Create the JFace {@link TableViewer} to edit timing groups. Adds columns to the table, sets up up listener used to initiate editing.
	 * @return TableViewer object
	 */
	private TableViewer createTable(Composite parent) {
		int style = SWT.BORDER | SWT.FULL_SELECTION |SWT.MULTI;
		TableViewer tableView = new TableViewer(parent, style);
		tableView.getTable().setHeaderVisible(true);
		tableView.getTable().setLinesVisible(true);
		tableView.setContentProvider(new ArrayContentProvider());
		// set layout on the Table so it fills rest of composite
		tableView.getTable().setLayout(new FillLayout());
		tableView.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// add columns to the table
		for(RoiTableColumns columnType : RoiTableColumns.values()) {
			TableColumn column = new TableColumn(tableView.getTable(), SWT.NONE);
			column.setText(columnType.getName());
			column.setWidth(100);
			TableViewerColumn columnViewer = new TableViewerColumn(tableView, column);
			columnViewer.setLabelProvider(new RoiRowLabelProvider(columnType));
			columnViewer.setEditingSupport(new RoiRowEditingSupport(tableView, columnType));
		}
		return tableView;
	}

	/**
	 * Return index in parameters ROI list corresponding to ROI currently selected in the table.
	 * @return
	 */
	private int getIndexOfSelectedTableRoi() {
		var selection = (StructuredSelection) roiTableViewer.getSelection();
		if (selection != StructuredSelection.EMPTY) {
			ROIRegion reg = (ROIRegion) selection.getFirstElement();
			return medipixParameters.getRegionList().indexOf(reg);
		}
		return -1;
	}

	/**
	 * Update the ROI tableview with latest ROIs from MedipixParameter object.
	 * Mark editor as 'dirty' so the bean can be save to XML.
	 *
	 */
	private void updateGui() {
		// Update tableview
		roiTableViewer.setInput(medipixParameters.getRegionList());
		roiTableViewer.refresh();

		// ensure Richbeans has latest bean,
		setEditingBean(medipixParameters);

		// Send value changed event (so XML can be saved)
		beanChanged();
	}

	/**
	 * Recreate GUI from bean when it has been changed in the XML view
	 */
	@Override
	public void linkUI(boolean tf) {
		widgetComposite.dispose();
		createPartControl(parent);
	}

	/**
	 * Create controls for adding and deleting ROIs
	 * @param parent
	 */
	private void createAddDeleteRoiControls(Composite parent) {
		final Composite widgets = new Composite(parent, SWT.NONE);
		widgets.setLayout(new GridLayout(2, false));
		widgets.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

		final Button addRoiButton = new Button(widgets, SWT.NONE);
		addRoiButton.setText("Add ROI");
		addRoiButton.addListener(SWT.Selection, e -> {
			ROIRegion newRoiRegion = new ROIRegion("New region", 0, 0, 100, 100);
			medipixParameters.getRegionList().add(newRoiRegion);
			updateGui();
			GridUtils.layoutFull(widgetComposite.getParent());
		});

		final Button deleteRoiButton = new Button(widgets, SWT.NONE);
		deleteRoiButton.setText("Delete ROI");

		deleteRoiButton.addListener(SWT.Selection, e -> {
			int index = getIndexOfSelectedTableRoi();
			if (index == -1) {
				return;
			}
			ROIRegion roi = medipixParameters.getRegionList().get(index);
			MessageBox messageBox = new MessageBox(widgets.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			messageBox.setText("Delete ROI");
			messageBox.setMessage("Are you sure you want to delete the selected ROI ("+roi.getRoiName()+")");
			if (messageBox.open() == SWT.YES) {
				medipixParameters.getRegionList().remove(index);
				updateGui();
			}
		});
	}

	/**
	 * Make a new RectangularROI from a ROIRegion object. (this is RectangularROI from
	 * org.eclipse.dawnsci.analysis.dataset.roi.)
	 *
	 * @param roiRegion
	 * @return RectangularROI
 	 * @since 11/5/2016
	 *
	 */
	private RectangularROI makeRectangularROI(ROIRegion roiRegion) {
		DetectorROI xRoi = roiRegion.getXRoi();
		DetectorROI yRoi = roiRegion.getYRoi();
		return new RectangularROI(xRoi.getRoiStart(), yRoi.getRoiStart(), xRoi.getRoiSize(), yRoi.getRoiSize(), 0.0);
	}

	/**
	 * Make new ROIRegion from RectangularROI
	 * @param rectRoi
	 * @return
	 * @since 11/5/2016
	 *
	 */
	private ROIRegion makeROIRegion(RectangularROI rectRoi ) {
		int xMax = (int) (rectRoi.getPointX() + rectRoi.getLength(0));
		int yMax = (int) (rectRoi.getPointY() + rectRoi.getLength(1));
		return new ROIRegion( rectRoi.getName(), (int)rectRoi.getPointX(), (int)rectRoi.getPointY(), xMax, yMax );
	}

	/**
	 * Extract ROI information from TwoDArrayView plot
	 * @return List of ROIRegions
	 * @since 11/5/2016
	 */
	private List<ROIRegion> getRoisFromPlot() {

		List<ROIRegion> roiRegionList = new ArrayList<>();

		try {
			IPlottingSystem<Composite> plotter = getPlottingSystem();
			Collection<IRegion> regionList = plotter.getRegions(RegionType.BOX);
			for( IRegion region : regionList ) {
				ROIRegion roiReg = makeROIRegion( (RectangularROI) region.getROI() );
				roiRegionList.add( roiReg );
			}
		} catch (PartInitException e) {
			logger.warn("Problem getting ROI information from area detector plot view", e);
		}

		return roiRegionList;
	}

	/**
	 * Add current set of ROIs to TwoDArrayView plot; clears current set of ROIs from plot first.
	 * @param roiRegionsList
	 * @since 11/5/2016
	 */
	private void addRoisToPlot(List<ROIRegion> roiRegionsList) {
		if (roiRegionsList.isEmpty())
			return;

		try {
			IPlottingSystem<Composite> plotter = getPlottingSystem();

			// clear current regions before starting.
			plotter.clearRegions();
			for (ROIRegion roiRegion : roiRegionsList) {
				// make new roi
				RectangularROI rectangularRoi = makeRectangularROI(roiRegion);
				rectangularRoi.setPlot( true ); // set to true so that ROI is propagated to NX detector ROI plugin correctly

				// make new region
				IRegion plotRegion = plotter.createRegion(roiRegion.getRoiName(), RegionType.BOX);

				// add roi to region
				plotRegion.setROI(rectangularRoi);

				// add region to plotter
				plotter.addRegion(plotRegion);
			}
		} catch (Exception e1) {
			logger.error("Problem getting Medipix ROI information", e1);
		}
	}

	private IPlottingSystem<Composite> getPlottingSystem() throws PartInitException {
		LiveStreamView liveStreamView = getLiveStreamViewRef();

		// Open the view
		if (liveStreamView == null) {
			showLiveStreamView();
			liveStreamView = getLiveStreamViewRef();
		}

		if (liveStreamView != null) {
			IPlottingSystem<Composite> plotSystem = liveStreamView.getPlottingSystem();
			setShowAxes(plotSystem, true);
			return plotSystem;
		} else {
			return null;
		}
	}

	/**
	 * Get name of camera configuration object to be used to open detector 'live stream' view.
	 * If medipixParameters object has a detector name set, return detectorName+"_camera_config",
	 * otherwise return value of {@link #MEDIPIX_CAMERA_CONFIG_NAME}
	 *
	 * @return name of the medipix live stream view configuration
	 */
	private String getCameraConfigName() {
		if (medipixParameters.getDetectorName() != null) {
			return medipixParameters.getDetectorName()+"_camera_config";
		}
		return MEDIPIX_CAMERA_CONFIG_NAME;
	}

	private LiveStreamView getLiveStreamViewRef() throws PartInitException {
		try {
			String configName = getCameraConfigName();
			return (LiveStreamView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(
					LiveStreamView.ID,
					configName + StreamType.EPICS_ARRAY.secondaryIdSuffix()).getView(false);
		} catch (Exception npException) {
			logger.warn("Caught exception while trying to get reference to medipix LiveStreamView", npException);
		}
		return null;
	}
	/**
	 * Check to see if client has {@link CameraConfiguration} object for medipix (called {@link #MEDIPIX_CAMERA_CONFIG_NAME}.
	 * Show a warning dialog box if it couldn't be found
	 *
	 * @return true if warning was shown (i.e. config was not found), false otherwise.
	 */
	private boolean showLiveStreamWarning() {
		String configName = getCameraConfigName();
		final Map<String, CameraConfiguration> cameras = Finder.getLocalFindablesOfType(CameraConfiguration.class);
		if (!cameras.containsKey(configName)) {
			String msg = "Could not open Medipix camera view - camera configuration called "+configName+" was not found";
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Could not open medipix camera view", msg);
			logger.warn(msg);
			return true;
		}
		return false;
	}

	private void showLiveStreamView() throws PartInitException {
		try {
			String configName = getCameraConfigName();
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
					LiveStreamView.ID,
					configName + StreamType.EPICS_ARRAY.secondaryIdSuffix(),
					IWorkbenchPage.VIEW_VISIBLE);
		}
		catch (NullPointerException npException) {
			logger.warn("Could not open LiveStreamView for detector", npException);
		}
	}

	/**
	 * Switch the plot axes on/off
	 * @param plottingSystem
	 * @param show true = show axes
	 */
	private void setShowAxes(IPlottingSystem<Composite> plottingSystem, boolean show) {
		plottingSystem.getAxes().forEach( axis -> axis.setVisible(show));
	}

	@Override
	public void setFocus() {
		widgetComposite.setFocus();
	}
}
