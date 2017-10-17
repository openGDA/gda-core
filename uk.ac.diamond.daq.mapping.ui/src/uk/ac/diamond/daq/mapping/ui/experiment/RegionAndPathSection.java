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

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
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
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.points.models.IMapPathModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingRegionManager;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegion;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.ui.path.AbstractPathComposite;
import uk.ac.diamond.daq.mapping.ui.path.PathCompositeProvider;
import uk.ac.diamond.daq.mapping.ui.region.RegionCompositeProvider;

/**
 * A section for configuring the region to scan and the path of the mapping scan.
 */
public class RegionAndPathSection extends AbstractMappingSection {

	private class RegionSelectorListener implements ISelectionChangedListener {

		private final PropertyChangeListener regionBeanPropertyChangeListener;

		private RegionSelectorListener() {
			this.regionBeanPropertyChangeListener = evt -> {
				plotter.updatePlotRegionFrom(scanRegion);
				updatePoints();
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

	private final PropertyChangeListener pathBeanPropertyChangeListener = evt -> updatePoints();

	private Composite regionAndPathComposite;
	private Composite pathComposite;
	private IMappingScanRegionShape scanRegion = null;
	private IScanPathModel scanPathModel = null;
	private PathInfoCalculatorJob pathCalculationJob;
	private PlottingController plotter;
	private IMappingRegionManager mappingRegionManager;

	private Composite regionComposite;

	private ComboViewer regionSelector;

	private ComboViewer pathSelector;

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

		// Prepare a grid data factory for controls which will need to grab space horizontally
		GridDataFactory horizontalGrabGridData = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

		// Make the region selection
		Composite regionComboComposite = new Composite(regionAndPathComposite, SWT.NONE);
		horizontalGrabGridData.applyTo(regionComboComposite);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(regionComboComposite);
		Label regionLabel = new Label(regionComboComposite, SWT.NONE);
		regionLabel.setText("Region shape:");
		regionSelector = new ComboViewer(regionComboComposite);
		horizontalGrabGridData.applyTo(regionSelector.getControl());
		regionSelector.getCombo().setToolTipText("Select a scan region shape. The shape can then be drawn on the map, or you can type numbers below.");
		Button redrawRegion = new Button(regionComboComposite, SWT.NONE);
		redrawRegion.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/map--pencil.png")));
		redrawRegion.setToolTipText("Draw/Redraw region");
		redrawRegion.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Reselect the same region type to cause a redraw event
				regionSelector.setSelection(regionSelector.getSelection());
			}
		});

		// Make the path selection
		Composite pathComboComposite = new Composite(regionAndPathComposite, SWT.NONE);
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

		pathSelector.addSelectionChangedListener(event -> {
			logger.debug("Path selection event: {}", event);

			// Get the new selection.
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			IScanPathModel selectedPath = (IScanPathModel) selection.getFirstElement();
			changePath(selectedPath);
		});

		updateControls();
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

		// Replace the region model of the same class with the new region from the mapping bean
		List<IMappingScanRegionShape> regionList = mappingRegionManager.getRegions();
		if (scanRegion == null) {
			scanRegion = regionList.get(0);
		} else {
			for (int i = 0; i < regionList.size(); i++) {
				if (regionList.get(i).getClass().equals(scanRegion.getClass())) {
					regionList.set(i, scanRegion);
				}
			}
		}
		regionSelector.setInput(regionList.toArray());

		// Replace the scan path model of the same class with the new scan path model from the mapping bean
		List<IScanPathModel> scanPathList = mappingRegionManager.getValidPaths(scanRegion);
		if (scanPathModel == null) {
			scanPathModel = scanPathList.get(0);
		} else {
			for (int i = 0; i < scanPathList.size(); i++) {
				if (scanPathList.get(i).getClass().equals(scanPathModel.getClass())) {
					scanPathList.set(i, scanPathModel);
				}
			}
		}
		pathSelector.setInput(scanPathList);

		// Recreate the contents of the beans
		rebuildMappingSection();

		// Set the selection on the combo viewers (has to be done after the above)
		regionSelector.setSelection(new StructuredSelection(scanRegion));
		pathSelector.setSelection(new StructuredSelection(scanPathModel));

		// Plot the scan region. This will cancel the region drawing event in the plotting system to avoid user confusion at startup
		// TODO check if this is the preferred behaviour, or better to leave the drawing event active
		plotter.updatePlotRegionFrom(scanRegion);
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
		final IMappingScanRegionShape mappingScanRegion = getMappingBean().getScanDefinition().getMappingScanRegion().getRegion();
		if (mappingScanRegion == null) {
			return; // We can't build a UI to edit null
		}
		regionComposite = RegionCompositeProvider.createRegionComposite(regionAndPathComposite, mappingScanRegion);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(regionComposite);

		// Scan Path
		final IScanPathModel scanPath = getMappingBean().getScanDefinition().getMappingScanRegion().getScanPath();
		if (scanPath == null) {
			return; // We can't build a UI to edit null
		}
		pathComposite = PathCompositeProvider.createPathComposite(regionAndPathComposite, scanPath);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(pathComposite);

		detectorsChanged(getMappingBean().getDetectorParameters().stream()
							.filter(IDetectorModelWrapper::isIncludeInScan)
							.collect(Collectors.toList()));

		relayoutMappingView();
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

	public void detectorsChanged(List<IDetectorModelWrapper> selectedDetectors) {
		final boolean isMalcolm = selectedDetectors.stream()
									.map(IDetectorModelWrapper::getModel)
									.anyMatch(IMalcolmModel.class::isInstance);

		if (pathComposite instanceof AbstractPathComposite) {
			((AbstractPathComposite) pathComposite).setContinuousEnabled(isMalcolm);
		} else if (scanPathModel instanceof IMapPathModel) {
				((IMapPathModel) scanPathModel).setContinuous(isMalcolm);
				// We need to update the checkbox as well as because we used the gui generator
				// we don't have the DataBindingsContext to call updateTargets. The only way to find the
				// 'continuous' checkbox and label is to go through the controls on the pathComposite and
				// look for them.
				if (pathComposite != null) {
					final Control[] pathControls = pathComposite.getChildren();
					for (int i = 0; i < pathControls.length - 1; i++) {
						// find the 'Continuous label' with an adjacent checkbox
						if (pathControls[i] instanceof Label && "Continuous:".equals(((Label) pathControls[i]).getText())
								&& pathControls[i+1] instanceof Button && (pathControls[i+1].getStyle() & SWT.CHECK) != 0) {
							// if malcolm the button is enabled and selected, if not (software scan) disabled and deselected
							// as we do not support continuous scans in software
							((Button) pathControls[i+1]).setSelection(isMalcolm);
							pathControls[i].setEnabled(isMalcolm);
							pathControls[i+1].setEnabled(isMalcolm);
						}
					}

				}
		}
	}
}

