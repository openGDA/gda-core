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
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.medipix.MedipixParameters;
import uk.ac.gda.beans.medipix.ROIRegion;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.LiveStreamView;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class MedipixParametersUIEditor extends RichBeanEditorPart {

	private static final Logger logger = LoggerFactory.getLogger(MedipixParametersUIEditor.class);
	private MedipixParameters medipixParameters;
	public static final String MEDIPIX_CAMERA_CONFIG_NAME = "medipix_camera_config";
	private Table roiTable;
	private TableCursor tableCursor;
	private int indexOfRoiBeingEdited = 0;
	private Composite widgetComposite;

	public MedipixParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		this.medipixParameters = (MedipixParameters) editingBean;
	}

	@Override
	protected String getRichEditorTabText() {
		return "Medipix";
	}

	@Override
	public void createPartControl(Composite parent) {

		try {
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
					updateRoiDetailsTable();
					sendValueChangedEvent("medipix ROIs update from plot");
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
			addRoiListComposite(widgetComposite);
			createAddDeleteRoiControls(widgetComposite);

			// Force a re-layout of the widget now that everything has been added
			GridUtils.layoutFull(widgetComposite.getParent());

		} catch (Exception ex) {
			// Creating the PlottingSystem in SashFormPlotComposite can throw Exception
			logger.warn("Exception creating MedipixParametersUIEditor", ex);
		}
	}

	/**
	 * Populate roi information table with current values from medipixParameters
	 * @since 11/5/2016
	 */
	private void updateRoiDetailsTable() {

		// remove all previous rows of roi information
		roiTable.removeAll();

		for (ROIRegion roi : medipixParameters.getRegionList()) {
			TableItem row = new TableItem(roiTable, SWT.NONE);
			row.setText(0, roi.getRoiName());
			row.setText(1, Integer.toString(roi.getXRoi().getRoiStart()));
			row.setText(2, Integer.toString(roi.getYRoi().getRoiStart()));
			row.setText(3, Integer.toString(roi.getXRoi().getRoiEnd() - roi.getXRoi().getRoiStart()));
			row.setText(4, Integer.toString(roi.getYRoi().getRoiEnd() - roi.getYRoi().getRoiStart()));
		}

		// update size of tables, rows to fit latest contents
		for (int i = 0; i < roiTable.getColumnCount(); i++) {
			roiTable.getColumn(i).pack();
		}

		// Re-layout to adjust sizes & positions of widgets to accommodate the new table
		GridUtils.layoutFull(widgetComposite.getParent());
	}

	/**
	 * Create new ROIRegion object from a TableItem
	 * @param tableItem
	 * @return ROIRegion
	 */
	private ROIRegion getRoiRegionFromTableItem(TableItem tableItem) {
		if (tableItem == null) {
			return null;
		}
		try {
			String name = tableItem.getText(0);
			int xstart = Integer.parseInt(tableItem.getText(1));
			int ystart = Integer.parseInt(tableItem.getText(2));
			int xsize = Integer.parseInt(tableItem.getText(3));
			int ysize = Integer.parseInt(tableItem.getText(4));
			return new ROIRegion(name, xstart, ystart, xstart+xsize, ystart+ysize);
		}
		catch(NumberFormatException nfe) {
			logger.warn("Problem converting numbers from table", nfe);
			return null;
		}
	}

	/**
	 * Add Table of ROI information to composite.
	 * @param parent
	 */
	private void addRoiListComposite( Composite parent ) {
		Group roiListGroup = new Group(parent, SWT.BORDER);
		roiListGroup.setText("ROI region list");
		roiListGroup.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true ));
		roiListGroup.setLayout(new GridLayout());

		roiTable = new Table( roiListGroup, SWT.BORDER | SWT.SINGLE);
		roiTable.setLinesVisible(true);
		roiTable.setHeaderVisible(true);

		roiTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		tableCursor = new TableCursor(roiTable, SWT.NONE);
		final ControlEditor editor = new ControlEditor(tableCursor);
		editor.grabHorizontal = true;
		editor.grabVertical = true;

		tableCursor.addSelectionListener(new SelectionListener() {
			// Called when user changes selected cell with keyboard
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("cursor widgetSelected");
				roiTable.setSelection(new TableItem[] {tableCursor.getRow()});
			}

			// Called when user presses enter over selected cell
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				logger.debug("cursor widgetDefaultSelected");
				// Create Text box to edit cell contents
				final Text text = getTextBoxWithEditingListeners();
				editor.setEditor(text);
				text.setFocus();

			}
		});

		tableCursor.addMouseListener(new MouseAdapter() {
			// Called when already selected cell is clicked again with mouse
			@Override
			public void mouseDown(MouseEvent e) {
				logger.debug("tableCursor Mouse down event");
				final Text text = getTextBoxWithEditingListeners();
				editor.setEditor(text);
				text.setFocus();
			}
		});

		// Set column names
		String[] colNames = new String[] { "ROI Name", "X start", "Y start", "Width", "Height" };

		for (String name : colNames) {
			TableColumn column = new TableColumn(roiTable, SWT.NONE);
			column.setText(name);
			column.setAlignment(SWT.CENTER);
		}

		// Add rows of ROI information to table
		updateRoiDetailsTable();
	}

	/**
	 * Return index in parameters ROI list corresponding to ROI currently selected in the Table.
	 * @return
	 */
	private int getIndexOfSelectedTableRoi() {
		TableItem row = tableCursor.getRow();
		ROIRegion currentRoi = getRoiRegionFromTableItem(row);
		return medipixParameters.getRegionList().indexOf(currentRoi);
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
			updateRoiDetailsTable();
			sendValueChangedEvent("ROI added to table");
		});

		final Button deleteRoiButton = new Button(widgets, SWT.NONE);
		deleteRoiButton.setText("Delete ROI");

		deleteRoiButton.addListener(SWT.Selection, e -> {
			int index = getIndexOfSelectedTableRoi();
			if (index != -1) {
				MessageBox messageBox = new MessageBox(widgets.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				messageBox.setText("Delete ROI");
				ROIRegion roi = medipixParameters.getRegionList().get(index);
				messageBox.setMessage("Are you sure you want to delete the selected ROI ("+roi.getRoiName()+")");
				int delete = messageBox.open();
				if (delete == SWT.YES) {
					medipixParameters.getRegionList().remove(index);
					updateRoiDetailsTable();
					sendValueChangedEvent("ROI deleted from table");
				}
			}
		});
	}

	/**
	 * Notify Richbeans that values have been modified, so they can be saved to xml.
	 * @param message
	 */
	private void sendValueChangedEvent(String message) {
		ValueEvent evt = new ValueEvent(medipixParameters, message);
		valueChangePerformed(evt);
	}

	/**
	 * Create a Text box for editing cell contents inside a Table.
	 * Listeners are attached for updating the medipix parameters with the updated ROI from the edited table cell
	 * @return Text box
	 */
	private Text getTextBoxWithEditingListeners() {
		// Contents of currently selected cell in table
		TableItem row = tableCursor.getRow();
		int column = tableCursor.getColumn();

		// Determine which ROI is being edited
		indexOfRoiBeingEdited = getIndexOfSelectedTableRoi();
		logger.debug("Index of ROI being edited : {}", indexOfRoiBeingEdited);

		// Begin an editing session
		// Notice that the parent of the Text is the TableCursor, not the Table
		final Text text = new Text(tableCursor, SWT.NONE);
		// initialise with contents of cell
		text.setText(row.getText(column));

		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				// close the text editor and copy the data over
				// when the user hits "ENTER"
				if (e.character == SWT.CR) {
					logger.debug("tableCursor text CR pressed");

					// update Table cell with updated contents of Text box
					TableItem row = tableCursor.getRow();
					int column = tableCursor.getColumn();
					row.setText(column, text.getText());

					text.dispose();

					// return keyboard focus to cursor (so can use keyboard to move to another cell after editing has finished)
					tableCursor.setFocus();

					// Update ROI list with updated parameters
					ROIRegion editedRoiRegion = getRoiRegionFromTableItem(row);
					medipixParameters.getRegionList().set(indexOfRoiBeingEdited, editedRoiRegion);

					// Notify richbeans the ROI values have been modified, so they can be saved to xml.
					sendValueChangedEvent("medipix ROI updated from table");
				}

				// close the text editor when the user hits "ESC"
				if (e.character == SWT.ESC) {
					logger.debug("tableCursor text ESC pressed");
					text.dispose();
					tableCursor.setFocus();
				}
			}
		});
		// close the text editor when the user clicks away
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				logger.debug("tableCursor text focusLost");
				text.dispose();
			}
		});

		return text;
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
		DetectorROI xRoi = roiRegion.getXRoi(), yRoi = roiRegion.getYRoi();
		int width = xRoi.getRoiEnd() - xRoi.getRoiStart();
		int height = yRoi.getRoiEnd() - yRoi.getRoiStart();

		return new RectangularROI(xRoi.getRoiStart(), yRoi.getRoiStart(), width, height, 0.0);
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
		if (roiRegionsList.size() == 0)
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

	private LiveStreamView getLiveStreamViewRef() throws PartInitException {
		try {
			return (LiveStreamView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(
					LiveStreamView.ID,
					MEDIPIX_CAMERA_CONFIG_NAME + StreamType.EPICS_ARRAY.secondaryIdSuffix()).getView(false);
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
		final Map<String, CameraConfiguration> cameras = Finder.getLocalFindablesOfType(CameraConfiguration.class);
		if (!cameras.containsKey(MEDIPIX_CAMERA_CONFIG_NAME)) {
			String msg = "Could not open Medipix camera view - camera configuration called "+MEDIPIX_CAMERA_CONFIG_NAME+" was not found";
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Could not open medipix camera view", msg);
			logger.warn(msg);
			return true;
		}
		return false;
	}

	private void showLiveStreamView() throws PartInitException {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
					LiveStreamView.ID,
					MEDIPIX_CAMERA_CONFIG_NAME + StreamType.EPICS_ARRAY.secondaryIdSuffix(),
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
		// Don't open on focus - view can now be opened by 'Open ROI region' button.

		// Show array view when editor gets focus.
//		try {
//			showArrayView();
//		} catch (PartInitException e) {
//			logger.error("Problem setting focus", e);
//		}
	}

}
