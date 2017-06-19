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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.dawnsci.plotting.roi.IRegionRow;
import org.dawnsci.plotting.roi.ROIEditTable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IMappingRegionManager;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegion;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

/**
 * A section for configuring the region to scan and the path of the mapping scan.
 */
public class RegionAndPathSection extends AbstractMappingSection {

	private class RegionSelectorListener implements ISelectionChangedListener {

		private final PropertyChangeListener regionBeanPropertyChangeListener;

		private RegionSelectorListener() {
			this.regionBeanPropertyChangeListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					plotter.updatePlotRegionFrom(scanRegion);
					updatePoints();
					if (!updatingRegion) regionEditor.setRegion(scanRegion.toROI(), null, null);
				}
			};
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			logger.debug("Region selection event: {}", event);

			// Get the new selection.
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			IMappingScanRegionShape selectedRegion = (IMappingScanRegionShape) selection.getFirstElement();
			changeRegion(selectedRegion);

			// Need default regions for testing
			switch (selectedRegion.getName()) {

			case "Rectangle":
				regionEditor.setRegion(new RectangularROI(1.0, 0.0), null, null);
				break;
			case "Circle":
				regionEditor.setRegion(new CircularROI(1.0), null, null);
				break;
			case "Line":
				regionEditor.setRegion(new LinearROI(1.0, 0.0), null, null);
				break;
			case "Point":
				regionEditor.setRegion(new PointROI(0.0,0.0), null, null);
				break;
			}


		}

		private void changeRegion(IMappingScanRegionShape newRegion) {
			// We're going to replace the scan region with a new one
			// If the existing one is non-null, remove the property change listener from it
			if (scanRegion != null) {
				scanRegion.removePropertyChangeListener(regionBeanPropertyChangeListener);
			}

			// Set the new scan region
			scanRegion = newRegion;
			getMappingBean().getScanDefinition().getMappingScanRegion().setRegion(scanRegion);

			// Update the path selector with paths valid for the new region type
			// (The listener on the path selector will take care of propagating the change appropriately, and updating the GUI)
			// Do this before starting drawing the region (+ path ) with the plotting system because changing path after breaks the region drawing
			List<IScanPathModel> scanPathList = mappingRegionManager.getValidPaths(scanRegion);
			pathSelector.setInput(scanPathList);
			if (scanPathList.contains(scanPathModel)) {
				pathSelector.setSelection(new StructuredSelection(scanPathModel), true);
			} else if (!scanPathList.isEmpty()) {
				// Select the first path by default
				pathSelector.setSelection(new StructuredSelection(scanPathList.get(0)), true);
			} else {
				pathSelector.setSelection(StructuredSelection.EMPTY, true);
			}

			// If new scan region is non-null, add it to the plot and add the property change listener
			if (scanRegion != null) {
				plotter.createNewPlotRegion(scanRegion);
				scanRegion.addPropertyChangeListener(regionBeanPropertyChangeListener);
			}
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(RegionAndPathSection.class);

	private final PropertyChangeListener pathBeanPropertyChangeListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			updatePoints();
		}
	};


	private Composite regionAndPathComposite;
	private Composite rightComposite;
	private Composite pathComposite;
	private IMappingScanRegionShape scanRegion = null;
	private IScanPathModel scanPathModel = null;
	private PathInfoCalculatorJob pathCalculationJob;
	private PlottingController plotter;
	private IMappingRegionManager mappingRegionManager;

	private ComboViewer regionSelector;

	private ComboViewer pathSelector;

	private ROIEditTable regionEditor;
	private boolean updatingRegion = false;


	@Override
	protected void initialize(MappingExperimentView mappingView) {
		super.initialize(mappingView);
		plotter = getService(PlottingController.class);
		mappingRegionManager = getService(IMappingRegionManager.class);
		pathCalculationJob = createPathCalculationJob();
	}

	private PathInfoCalculatorJob createPathCalculationJob() {
		PathInfoCalculatorJob job = ContextInjectionFactory.make(PathInfoCalculatorJob.class, getEclipseContext());
		UISynchronize uiSync = getService(UISynchronize.class);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void running(IJobChangeEvent event) {
				uiSync.asyncExec(() -> {
					setStatusMessage("Scan path calculation in progress");
					plotter.removePath();
				});
			}
			@Override
			public void done(final IJobChangeEvent event) {
				uiSync.asyncExec(() -> {
					IStatus result = event.getResult();
					if (result.getSeverity() == IStatus.CANCEL) {
						setStatusMessage("Scan path calculation was cancelled");
					} else if (!result.isOK()) {
						setStatusMessage("Error in scan path calculation - see log for details");
						logger.warn("Error in scan path calculation", result.getException());
					}
					// else, calculation completed normally and the status text will be updated from the new PathInfo
				});
			}
		});

		return job;
	}

	@Override
	public void createControls(Composite parent) {
		// Make a custom section for handling the mapping region
		regionAndPathComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(regionAndPathComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).spacing(0, 0).applyTo(regionAndPathComposite);

		// Add a listener to update the scrolled composite when the region and path composite changes
		// This will set the initial size as well when the region and path composite is first drawn
		regionAndPathComposite.addListener(SWT.Resize, event -> mappingView.recalculateMinimumSize());

		// Prepare a grid data factory for controls which will need to grab space horizontally
		GridDataFactory horizontalGrabGridData = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false);

		// Make the region selection
		Composite regionComposite = new Composite(regionAndPathComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(regionComposite);
		//horizontalGrabGridData.applyTo(regionComposite);
		Composite regionComboComposite = new Composite(regionComposite, SWT.NONE);
		horizontalGrabGridData.applyTo(regionComboComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(regionComboComposite);
		Label regionLabel = new Label(regionComboComposite, SWT.NONE);
		regionLabel.setText("Region shape:");
		regionSelector = new ComboViewer(regionComboComposite);
		horizontalGrabGridData.applyTo(regionSelector.getControl());
		regionSelector.getCombo().setToolTipText("Select a scan region shape. The shape can then be drawn on the map, or you can type numbers below.");
		regionEditor = new ROIEditTable(60,  new int[]{135, 55},false);
		regionEditor.setEnabledCellConfigurator(this::uneditableRotation);
		regionEditor.createPartControl(regionComposite);

		// Make the path selection
		rightComposite = new Composite(regionAndPathComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(rightComposite);
		horizontalGrabGridData.applyTo(rightComposite);
		Composite pathComboComposite = new Composite(rightComposite, SWT.NONE);
		horizontalGrabGridData.applyTo(pathComboComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(pathComboComposite);
		Label pathLabel = new Label(pathComboComposite, SWT.NONE);
		pathLabel.setText("Scan path:");
		pathSelector = new ComboViewer(pathComboComposite);
		horizontalGrabGridData.applyTo(pathSelector.getControl());

		// Add logic
		regionSelector.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IMappingScanRegionShape) {
					IMappingScanRegionShape mappingRegion = (IMappingScanRegionShape) element;
					return mappingRegion.getName();
				}
				return super.getText(element);
			}
		});

		regionSelector.setContentProvider(ArrayContentProvider.getInstance());
		List<IMappingScanRegionShape> regionList = mappingRegionManager.getRegions();
		regionSelector.setInput(regionList.toArray());

		regionSelector.addSelectionChangedListener(new RegionSelectorListener());

		regionEditor.addROIListener(new IROIListener.Stub() {
			@Override
			public void roiChanged(org.eclipse.dawnsci.plotting.api.region.ROIEvent evt) {
					updatingRegion = true;
					scanRegion.updateFromROI(evt.getROI());
					updatingRegion = false;
			}
		});

		pathSelector.setContentProvider(ArrayContentProvider.getInstance());
		pathSelector.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IScanPathModel) {
					IScanPathModel scanPath = (IScanPathModel) element;
					return scanPath.getName();
				}
				return super.getText(element);
			}
		});


		pathSelector.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				logger.debug("Path selection event: {}", event);

				// Get the new selection.
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				IScanPathModel selectedPath = (IScanPathModel) selection.getFirstElement();
				changePath(selectedPath);
			}
		});

		// Setting the first region will trigger the listeners to update the mapping section and point count
		regionSelector.setSelection(new StructuredSelection(regionList.get(0)), true);

		// Plot the scan region. This will cancel the region drawing event in the plotting system to avoid user confusion at startup
		// TODO check if this is the preferred behaviour, or better to leave the drawing event active
		plotter.updatePlotRegionFrom(scanRegion);
	}

	private void changePath(IScanPathModel newPath) {
		logger.debug("Changing path to {}", newPath);

		// We're going to replace the scan path with a new one
		// If the existing one is non-null, remove the property change listener from it
		if (scanPathModel != null) {
			scanPathModel.removePropertyChangeListener(pathBeanPropertyChangeListener);
		}

		// Set the new scan path. If non-null, add the property change listener
		scanPathModel = newPath;
		getMappingBean().getScanDefinition().getMappingScanRegion().setScanPath(scanPathModel);
		if (scanPathModel != null) {
			scanPathModel.addPropertyChangeListener(pathBeanPropertyChangeListener);
		}

		// Update the GUI to reflect the path changes
		rebuildMappingSection();
		updatePoints();
	}

	@Override
	protected void updateControls() {
		IMappingScanRegion mappingScanRegion = getMappingBean().getScanDefinition().getMappingScanRegion();
		scanRegion = mappingScanRegion.getRegion();
		scanPathModel = mappingScanRegion.getScanPath();

		// Replace the scan path model of the same class with the new one
		List<IScanPathModel> scanPathList = mappingRegionManager.getValidPaths(scanRegion);
		for (int i = 0; i < scanPathList.size(); i++) {
			if (scanPathList.get(i).getClass().equals(scanPathModel.getClass())) {
				scanPathList.set(i, scanPathModel);
			}
		}
		pathSelector.setInput(scanPathList);

		// Recreate the contents of the beans
		rebuildMappingSection();

		// Set the selection on the combo viewers (has to be done after the above)
		regionSelector.setSelection(new StructuredSelection(scanRegion));
		pathSelector.setSelection(new StructuredSelection(scanPathModel));
	}

	/**
	 * Call this to rebuild the mapping section. Only required when the underlying beans are swapped.
	 */
	private void rebuildMappingSection() {

		if (pathComposite != null) {
			pathComposite.dispose();
			pathComposite = null;
		}

		IGuiGeneratorService guiGenerator = getService(IGuiGeneratorService.class);

		// Scan Path
		Object scanPath = getMappingBean().getScanDefinition().getMappingScanRegion().getScanPath();
		pathComposite = guiGenerator.generateGui(scanPath, rightComposite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(pathComposite);
		mappingView.relayout();
	}

	private void updatePoints() {
		pathCalculationJob.cancel();
		if (scanPathModel != null && scanRegion != null) {
			pathCalculationJob.setScanPathModel(scanPathModel);
			pathCalculationJob.setScanRegion(scanRegion);
			pathCalculationJob.schedule();
		}
	}

	@Override
	public void setFocus() {
		if (regionAndPathComposite != null) {
			regionAndPathComposite.setFocus();
		}
	}

	private void uneditableRotation(IRegionRow row) {
		if ("Rotation (°)".equals(row.getName())) row.setEnabled(false);
	}

}
