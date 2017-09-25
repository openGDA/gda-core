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

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
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
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.nxdetector.roi.PlotServerROISelectionProvider;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.exafs.i20.MedipixParameters;
import uk.ac.gda.beans.exafs.i20.ROIRegion;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.epics.adviewer.views.TwoDArrayView;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class MedipixParametersUIEditor extends RichBeanEditorPart {

	private MedipixParameters medipixParameters;
	private TwoDArrayView twoDArrayView;
	public static final String MEDIPIX_VIEW_SECONDARY_ID = "medipix";
	private static final Logger logger = LoggerFactory.getLogger(MedipixParametersUIEditor.class);
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
			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(regionsGroup);

			// Button to setup detector (i.e. TwoDArrayView) ROIs from GUI values
			final Button setRoiButton = new Button(regionsGroup, SWT.NONE);
			setRoiButton.setText("Set detector ROIs");

			setRoiButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					addRoisToPlot(medipixParameters.getRegionList());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			// Button to copy ROIs from TwoDArrayView into local parameters
			final Button getRoiButton = new Button(regionsGroup, SWT.NONE);
			getRoiButton.setText("Get detector ROIs");

			getRoiButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					List<ROIRegion> regionList = getRoisFromPlot();
					medipixParameters.setRegionList(regionList);
					updateRoiDetailsTable();
					sendValueChangedEvent("medipix ROIs update from plot");
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			addRoiListComposite(widgetComposite);
			createAddDeleteRoiControls(widgetComposite);

			// Force a re-layout of the widget now that everything has been added
			GridUtils.layoutFull(widgetComposite.getParent());

			setTwoDArrayViewRef();

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
		addRoiButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ROIRegion newRoiRegion = new ROIRegion("New region", 0, 0, 100, 100);
				medipixParameters.getRegionList().add(newRoiRegion);
				updateRoiDetailsTable();
				sendValueChangedEvent("ROI added to table");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});

		final Button deleteRoiButton = new Button(widgets, SWT.NONE);
		deleteRoiButton.setText("Delete ROI");

		deleteRoiButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
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
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
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

					// Get updated contents of Text box
					String newText = text.getText();
					if (newText==null || newText.length()==0) {
						newText = "0";
					}
					// Update table with the new text
					TableItem row = tableCursor.getRow();
					int column = tableCursor.getColumn();
					row.setText(column, newText);

					text.dispose();

					// return keyboard focus to cursor (so keyboard can be used to move to another cell after editing has finished)
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

		// Add verify listener to make sure input is an Integer
		// (But not for column 0, the ROI name)
		if (column>0) {
			text.addVerifyListener(new VerifyListener() {
				@Override
				public void verifyText(VerifyEvent e) {
					final String oldText = text.getText();
					String newInput = oldText.substring(0, e.start) + e.text + oldText.substring(e.end);
					if (newInput.length()==0) {
						newInput="0";
					}
					try {
						Integer intVal = new Integer(newInput);
						if (intVal >= 0) {
							e.doit = true;
						} else {
							e.doit = false;
						}
					} catch (final NumberFormatException numberFormatException) {
						// value is not integer
						e.doit = false;
					}
				}
			});
		}
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


	private void setRoi(ROIRegion roiRegion) {
		List<ROIRegion> list = new ArrayList<ROIRegion>();
		list.add(roiRegion);
		addRoisToPlot(list);
	}

	/**
	 * Extract ROI information from TwoDArrayView plot
	 * @return List of ROIRegions
	 * @since 11/5/2016
	 */
	private List<ROIRegion> getRoisFromPlot() {

		List<ROIRegion> roiRegionList = new ArrayList<ROIRegion>();

		IPlottingSystem plotter;
		try {
			plotter = getPlottingSystem();
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
			IPlottingSystem plotter = getPlottingSystem();

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

			PlotServerROISelectionProvider roiProvider = new PlotServerROISelectionProvider(MEDIPIX_VIEW_SECONDARY_ID, 1);
			if (roiProvider.getRoi(0) == null)
				logger.error("Medipix does not have any ROIs set");
		} catch (Exception e1) {
			logger.error("Problem getting Medipix ROI information", e1);
		}
	}

	private void setTwoDArrayViewRef() throws PartInitException {
		try {
			IViewReference viewRef = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(TwoDArrayView.ID, MEDIPIX_VIEW_SECONDARY_ID);
			twoDArrayView = (TwoDArrayView) viewRef.getView(false);
		} catch (NullPointerException npException) {
			logger.warn("setTwoDArrayViewRef caught Null Pointer exception - area detector view probably not initialized yet", npException);
		}
	}

	private void showArrayView() throws PartInitException {
		try {
			if ( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(TwoDArrayView.ID) == null )
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(TwoDArrayView.ID, MEDIPIX_VIEW_SECONDARY_ID, IWorkbenchPage.VIEW_VISIBLE);
		}
		catch (NullPointerException npException) {
			logger.warn("showArrayView caught Null Pointer exception - area detector view probably not initialized yet", npException);
		}
	}

	private IPlottingSystem getPlottingSystem() throws PartInitException {
		setTwoDArrayViewRef();
		if (twoDArrayView != null && twoDArrayView.getTwoDArray() != null) {
			return twoDArrayView.getTwoDArray().getPlottingSystem();
		} else
			return null;
	}

	@Override
	public void setFocus() {
		// Show array view when editor gets focus
		try {
			showArrayView();
		} catch (PartInitException e) {
			logger.error("Problem setting focus", e);
		}
	}

}
