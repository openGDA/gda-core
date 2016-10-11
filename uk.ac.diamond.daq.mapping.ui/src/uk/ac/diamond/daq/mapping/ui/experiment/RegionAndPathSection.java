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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
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
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

/**
 * A section for configuring the region to scan and the path of the mapping scan.
 */
public class RegionAndPathSection extends AbstractMappingSection {

	private class RegionSelectorListener implements ISelectionChangedListener {

		private final ComboViewer pathSelector;
		private final PropertyChangeListener regionBeanPropertyChangeListener;

		private RegionSelectorListener(ComboViewer pathSelector) {
			this.pathSelector = pathSelector;
			this.regionBeanPropertyChangeListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					plotter.updatePlotRegionFrom(scanRegion);
					updatePoints();
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
		}

		private void changeRegion(IMappingScanRegionShape newRegion) {
			// We're going to replace the scan region with a new one
			// If the existing one is non-null, remove the property change listener from it
			if (scanRegion != null) {
				scanRegion.removePropertyChangeListener(regionBeanPropertyChangeListener);
			}

			// Set the new scan region
			scanRegion = newRegion;
			mappingBean.getScanDefinition().getMappingScanRegion().setRegion(scanRegion);

			// Update the path selector with paths valid for the new region type
			// (The listener on the path selector will take care of propagating the change appropriately, and updating the GUI)
			// Do this before starting drawing the region (+ path ) with the plotting system because changing path after breaks the region drawing
			List<IScanPathModel> scanPathList = mappingRegionManager.getValidPaths(scanRegion);
			pathSelector.setInput(scanPathList);
			if (scanPathList.contains(scanPathModel)) {
				pathSelector.setSelection(new StructuredSelection(scanPathModel), true);
			} else if (scanPathList.size() > 0) {
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
	private Composite pathComposite;
	private IMappingScanRegionShape scanRegion = null;
	private IScanPathModel scanPathModel = null;
	private final PathInfoCalculatorJob pathCalculationJob;
	private final PlottingController plotter;
	private final IMappingRegionManager mappingRegionManager;

	private Composite regionComposite;

	RegionAndPathSection(MappingExperimentView mappingView, IEclipseContext context) {
		super(mappingView, context);
		plotter = context.get(PlottingController.class);
		mappingRegionManager = context.get(IMappingRegionManager.class);
		pathCalculationJob = createPathCalculationJob();
	}

	private PathInfoCalculatorJob createPathCalculationJob() {
		PathInfoCalculatorJob job = ContextInjectionFactory.make(PathInfoCalculatorJob.class, context);
		UISynchronize uiSync = context.get(UISynchronize.class);
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
		regionAndPathComposite.addListener(SWT.Resize, event -> {
			mappingView.recalculateMinimumSize();
		});

		// Prepare a grid data factory for controls which will need to grab space horizontally
		GridDataFactory horizontalGrabGridData = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

		// Make the region selection
		Composite regionComboComposite = new Composite(regionAndPathComposite, SWT.NONE);
		horizontalGrabGridData.applyTo(regionComboComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(regionComboComposite);
		Label regionLabel = new Label(regionComboComposite, SWT.NONE);
		regionLabel.setText("Region shape:");
		ComboViewer regionSelector = new ComboViewer(regionComboComposite);
		horizontalGrabGridData.applyTo(regionSelector.getControl());
		regionSelector.getCombo().setToolTipText("Select a scan region shape. The shape can then be drawn on the map, or you can type numbers below.");

		// Make the path selection
		Composite pathComboComposite = new Composite(regionAndPathComposite, SWT.NONE);
		horizontalGrabGridData.applyTo(pathComboComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(pathComboComposite);
		Label pathLabel = new Label(pathComboComposite, SWT.NONE);
		pathLabel.setText("Scan path:");
		final ComboViewer pathSelector = new ComboViewer(pathComboComposite);
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

		regionSelector.addSelectionChangedListener(new RegionSelectorListener(pathSelector));

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
		mappingBean.getScanDefinition().getMappingScanRegion().setScanPath(scanPathModel);
		if (scanPathModel != null) {
			scanPathModel.addPropertyChangeListener(pathBeanPropertyChangeListener);
		}

		// Update the GUI to reflect the path changes
		rebuildMappingSection();
		updatePoints();
	}

	/**
	 * Call this to rebuild the mapping section. Only required when the underlying beans are swapped.
	 */
	private void rebuildMappingSection() {
		// Remove the old controls
		if (regionComposite != null) {
			regionComposite.dispose();
			regionComposite = null;
		}
		if (pathComposite != null) {
			pathComposite.dispose();
			pathComposite = null;
		}

		// Scan Region
		IGuiGeneratorService guiGenerator = context.get(IGuiGeneratorService.class);
		Object mappingScanRegion = mappingBean.getScanDefinition().getMappingScanRegion().getRegion();
		regionComposite = (Composite) guiGenerator.generateGui(mappingScanRegion, regionAndPathComposite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(regionComposite);

		// Scan Path
		Object scanPath = mappingBean.getScanDefinition().getMappingScanRegion().getScanPath();
		pathComposite = (Composite) guiGenerator.generateGui(scanPath, regionAndPathComposite);
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

	public void setFocus() {
		if (regionAndPathComposite != null) {
			regionAndPathComposite.setFocus();
		}
	}

}
