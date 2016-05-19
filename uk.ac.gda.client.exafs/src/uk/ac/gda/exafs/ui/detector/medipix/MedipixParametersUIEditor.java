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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
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
import uk.ac.gda.epics.adviewer.views.TwoDArrayView;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public class MedipixParametersUIEditor extends RichBeanEditorPart {

	private MedipixParameters medipixParameters;
	private TwoDArrayView twoDArrayView;
	public static final String MEDIPIX_VIEW_SECONDARY_ID = "medipix";
	private static final Logger logger = LoggerFactory.getLogger(MedipixParametersUIEditor.class);
	private Table roiTable;

	public MedipixParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		this.medipixParameters = (MedipixParameters) editingBean;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getRichEditorTabText() {
		return "Medipix";
	}

	@Override
	public void createPartControl(Composite parent) {

		try {
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 4;
			parent.setLayout(gridLayout);

			Composite left = new Composite(parent, SWT.NONE);
			left.setLayout(new GridLayout(1, false));
			left.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

			Group regionsGroup = new Group(left, SWT.NONE);
			regionsGroup.setText("Regions");
			GridLayoutFactory.swtDefaults().numColumns(2).applyTo(regionsGroup);


			// Button to setup detector (i.e. TwoDArrayView) ROIs from GUI values
			final Button setRoiButton = new Button(regionsGroup, SWT.NONE);
			setRoiButton.setText("Set detector ROIs");

			setRoiButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					System.out.println("Set detector ROIs ");
					addRoisToPlot(medipixParameters.getRegionList());
					// setRoi( new ROIRegion("testRoi", 10, 10, 50, 50) );
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
				}
			});

			// Button to copy ROIs from TwoDArrayView into local parameters
			final Button getRoiButton = new Button(regionsGroup, SWT.NONE);
			getRoiButton.setText("Get detector ROIs");

			getRoiButton.addSelectionListener(new SelectionListener() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
					System.out.println("Get detector ROIs ");
					//getRoisFromPlot_NX();
					List<ROIRegion> regionList = getRoisFromPlot();
					medipixParameters.setRegionList(regionList);
					updateRoiDetailsTable();

					// Notify richbeans the ROI values have been modified, so they can be saved to xml.
					ValueEvent evt = new ValueEvent(medipixParameters, "medipix ROI updated from plot" );
					valueChangePerformed(evt);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
				}
			});

			//tableTest(left);
			addRoiListComposite(left);
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

	    for( ROIRegion roi : medipixParameters.getRegionList() ) {

		    TableItem row = new TableItem(roiTable, SWT.NONE);
		    row.setText(0, roi.getRoiName() );
		    row.setText(1, Integer.toString(roi.getXRoi().getRoiStart()) );
		    row.setText(2, Integer.toString(roi.getYRoi().getRoiStart()) );
		    row.setText(3, Integer.toString(roi.getXRoi().getRoiEnd() - roi.getXRoi().getRoiStart()) );
		    row.setText(4, Integer.toString(roi.getYRoi().getRoiEnd() - roi.getYRoi().getRoiStart()) );

	    }

	    // update size of tables, rows to fit latest contents
		for (int i = 0; i < roiTable.getColumnCount(); i++) {
			roiTable.getColumn(i).pack();
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

		roiTable = new Table( roiListGroup, SWT.BORDER | SWT.MULTI );
		roiTable.setLinesVisible(true);
		roiTable.setHeaderVisible(true);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		roiTable.setLayoutData(gd);

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
				logger.error("Medipix does not have any ROIs set\n");
		} catch (Exception e1) {
			logger.error("Problem getting Medipix ROI information" + e1.toString());
		}
	}

	private void setTwoDArrayViewRef() throws PartInitException {
		try {
			IViewReference viewRef = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(TwoDArrayView.Id, MEDIPIX_VIEW_SECONDARY_ID);
			twoDArrayView = (TwoDArrayView) viewRef.getView(false);
		} catch (NullPointerException npException) {
			logger.warn(" showArrayView caught Null Pointer exception (" + npException + " - area detector view probably not initialized yet");
		}
	}

	private void showArrayView() throws PartInitException {
		try {
			if ( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(TwoDArrayView.Id) == null )
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(TwoDArrayView.Id, MEDIPIX_VIEW_SECONDARY_ID, IWorkbenchPage.VIEW_VISIBLE);
		}
		catch (NullPointerException npException) {
			logger.warn(" showArrayView caught Null Pointer exception (" + npException + " - area detector view probably not initialized yet");
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
			logger.error("TODO put description of error here", e);
		}
	}

}
