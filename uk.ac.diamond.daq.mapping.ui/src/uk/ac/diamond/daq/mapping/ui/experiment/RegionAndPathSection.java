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

import static com.google.common.base.Functions.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.ac.diamond.daq.mapping.ui.MappingImageConstants.IMG_GEAR;
import static uk.ac.diamond.daq.mapping.ui.MappingImageConstants.IMG_MAP_PENCIL;
import static uk.ac.diamond.daq.mapping.ui.MappingImageConstants.IMG_MAP_PIN;
import static uk.ac.gda.ui.tool.ClientMessages.SCAN_PATH_SHAPE_TP;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.points.models.IMapPathModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import gda.rcp.GDAClientActivator;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.ui.MultiFunctionButton;
import uk.ac.diamond.daq.mapping.ui.experiment.RegionAndPathController.RegionPathState;
import uk.ac.diamond.daq.mapping.ui.path.AbstractPathEditor;
import uk.ac.diamond.daq.mapping.ui.path.PathEditorProvider;
import uk.ac.diamond.daq.mapping.ui.region.RegionEditorProvider;

/**
 * A section for configuring the region to scan and the path of the mapping scan.
 */
public class RegionAndPathSection extends AbstractMappingSection {

	private static final Logger logger = LoggerFactory.getLogger(RegionAndPathSection.class);
	private static final String MAPPING_STAGE_KEY_JSON = "mappingStageAxes.json";

	/** The property id we want to listen to for changes*/
	private static final String PROPERTY_REGION_AND_PATH = "uk.ac.diamond.daq.mapping.ui.experiment.regionandpathsection.regionandpath";

	private Composite regionAndPathComposite;
	private AbstractRegionPathModelEditor<? extends IMapPathModel> pathEditor;
	private AbstractRegionPathModelEditor<? extends IMappingScanRegionShape> regionEditor;
	private ComboViewer regionSelector;
	private ComboViewer pathSelector;
	private Optional<String> selectedMalcolmDeviceName = Optional.empty();

	private RegionAndPathController controller;
	private Consumer<RegionPathState> viewUpdater;	// These variables must be used to store the relevant functions so
	private Consumer<String> statusUpdater;			// they can be passed to the controller on dispose allowing it to
													// remove them from its list of active consumers

	private final IPropertyChangeListener propertyChangeListener = this::handlePropertyChange;

	/** The scan regions available in the combo box, keyed by name */
	private Map<String, IMappingScanRegionShape> scanRegionMap;

	@Override
	public void initialize(MappingExperimentView mappingView) {
		super.initialize(mappingView);
		controller = getService(RegionAndPathController.class);
		viewUpdater = this::updatePathControl;
		statusUpdater = this::setStatusMessage;
		controller.initialise(Optional.of(viewUpdater), Optional.of(statusUpdater));
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		// Make a custom section for handling the mapping region
		regionAndPathComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(regionAndPathComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 5).applyTo(regionAndPathComposite);

		// Prepare a grid data factory for controls which will need to grab space horizontally
		final GridDataFactory horizontalGrabGridData = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

		// Make the region selection
		final Composite regionComboComposite = new Composite(regionAndPathComposite, SWT.NONE);
		horizontalGrabGridData.span(1, 1).applyTo(regionComboComposite);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(regionComboComposite);
		final Label regionLabel = new Label(regionComboComposite, SWT.NONE);
		regionLabel.setText("Region shape:");
		regionSelector = new ComboViewer(regionComboComposite);
		horizontalGrabGridData.applyTo(regionSelector.getControl());
		regionSelector.getCombo().setToolTipText("Select a scan region shape. The shape can then be drawn on the map, or you can type numbers below.");

		final MultiFunctionButton newRegion = new MultiFunctionButton();
		newRegion.addFunction("Draw region", "Draw region by dragging on map",
				getImage(IMG_MAP_PENCIL),
				()-> regionSelector.setSelection(regionSelector.getSelection()));
		newRegion.addFunction("Place default region", "Place the default region on current stage position",
				getImage(IMG_MAP_PIN),
				this::createDefaultRegionAtStagePosition);
		newRegion.draw(regionComboComposite);

		// Make the path selection
		final Composite pathComboComposite = new Composite(regionAndPathComposite, SWT.NONE);
		horizontalGrabGridData.applyTo(pathComboComposite);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(pathComboComposite);
		final Label pathLabel = new Label(pathComboComposite, SWT.NONE);
		pathLabel.setText("Scan path:");
		pathSelector = new ComboViewer(pathComboComposite);
		pathSelector.getCombo().setToolTipText(getMessage(SCAN_PATH_SHAPE_TP));
		horizontalGrabGridData.applyTo(pathSelector.getControl());

		final Button configureStageButton = new Button(pathComboComposite, SWT.PUSH);
		configureStageButton.setToolTipText("Configure mapping stage");
		configureStageButton.setImage(getImage(IMG_GEAR));
		configureStageButton.addListener(SWT.Selection, event -> {
			final MappingStageInfo mappingStage = getService(MappingStageInfo.class);
			final EditMappingStageDialog dialog = new EditMappingStageDialog(getShell(), mappingStage, selectedMalcolmDeviceName);
			if (dialog.open() == Window.OK) {
				rebuildMappingSection();
			}
		});

		// Add logic
		regionSelector.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IMappingScanRegionShape) {
					final IMappingScanRegionShape mappingRegion = (IMappingScanRegionShape) element;
					return mappingRegion.getName();
				}
				return super.getText(element);
			}
		});

		regionSelector.setContentProvider(ArrayContentProvider.getInstance());

		final List<IMappingScanRegionShape> scanRegions = controller.getTemplateRegions();
		scanRegionMap = scanRegions.stream()
				.collect(toMap(IMappingScanRegionShape::getName, identity()));
		regionSelector.setInput(scanRegions.toArray());

		regionSelector.addSelectionChangedListener(controller.getRegionSelectorListener());

		pathSelector.setContentProvider(ArrayContentProvider.getInstance());
		pathSelector.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IScanPathModel) {
					final IScanPathModel scanPath = (IScanPathModel) element;
					return scanPath.getName();
				}
				return super.getText(element);
			}
		});

		pathSelector.addSelectionChangedListener(event -> {
			logger.debug("Path selection event: {}", event);

			// Get the new selection.
			final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			final IMapPathModel selectedPath = (IMapPathModel) selection.getFirstElement();
			controller.changePath(selectedPath);
			rebuildMappingSection();
		});

		/*
		 * We disable mouse wheel events on these combo boxes
		 * because, being on a scrollable composite,
		 * users frequently mouse scroll on them by mistake
		 */
		regionSelector.getCombo().addListener(SWT.MouseWheel, this::disableEvent);
		pathSelector.getCombo().addListener(SWT.MouseWheel, this::disableEvent);

		// Listen for property changes that affect this section
		GDAClientActivator.getDefault().getPreferenceStore().addPropertyChangeListener(propertyChangeListener);

		updateControls();
	}

	private void disableEvent(Event event) {
		event.doit = false;
	}

	private void createDefaultRegionAtStagePosition() {
		controller.createDefaultRegionAtStagePosition();
		updateControls();
	}

	@Override
	public void updateControls() {
		controller.refreshFromMappingBean();

		// Replace the region model of the same class with the new region from the mapping bean
		regionSelector.setInput(controller.getRegionListAndLinkRegion().toArray());

		// Replace the scan path model of the same class with the new scan path model from the mapping bean
		pathSelector.setInput(controller.getScanPathListAndLinkPath());

		// Recreate the contents of the beans
		rebuildMappingSection();

		// Set the selection on the combo viewers (has to be done after the above)
		regionSelector.setSelection(new StructuredSelection(controller.getScanRegionShape()));
		pathSelector.setSelection(new StructuredSelection(controller.getScanPathModel()));

		// Plot the scan region. This will cancel the region drawing event in the plotting system to avoid user confusion at startup
		controller.updatePlotRegion();
	}

	/**
	 * Call this to rebuild the mapping section. Only required when the underlying beans are swapped.
	 */
	protected void rebuildMappingSection() {
		// Remove the old controls
		if (regionEditor != null) {
			regionEditor.dispose();
		}
		if (pathEditor != null) {
			pathEditor.dispose();
		}

		// Scan Region
		final IMappingScanRegionShape mappingScanRegion = controller.getScanRegionFromBean().getRegion();
		if (mappingScanRegion == null) {
			return; // We can't build a UI to edit null
		}

		regionEditor = RegionEditorProvider.createRegionEditor(mappingScanRegion, controller.getMappingRegionUnits(), getEclipseContext());
		regionEditor.createEditorPart(regionAndPathComposite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(regionAndPathComposite);

		// Scan Path
		final IScanPathModel scanPath = controller.getScanRegionFromBean().getScanPath();
		if (scanPath == null) {
			return; // We can't build a UI to edit null
		}

		pathEditor = (AbstractRegionPathModelEditor<? extends IMapPathModel>) PathEditorProvider.createPathComposite(scanPath, getEclipseContext());
		pathEditor.createEditorPart(regionAndPathComposite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(regionAndPathComposite);

		detectorsChanged(getBean().getDetectorParameters().stream()
							.filter(IScanModelWrapper<IDetectorModel>::isIncludeInScan)
							.collect(toList()));

		relayoutView();
	}

	@Override
	public void setFocus() {
		if (regionAndPathComposite != null) {
			regionAndPathComposite.setFocus();
		}
	}

	public void detectorsChanged(List<IScanModelWrapper<IDetectorModel>> selectedDetectors) {
		selectedMalcolmDeviceName = selectedDetectors.stream()
									.map(IScanModelWrapper<IDetectorModel>::getModel)
									.filter(IMalcolmModel.class::isInstance)
									.map(INameable::getName)
									.findFirst();
		((AbstractPathEditor) pathEditor).setContinuousEnabled(selectedMalcolmDeviceName.isPresent());
	}

	@Override
	public void saveState(Map<String, String> persistedState) {
		final IMarshallerService marshaller = getService(IMarshallerService.class);
		final MappingStageInfo mappingStage = getService(MappingStageInfo.class);
		try {
			persistedState.put(MAPPING_STAGE_KEY_JSON, marshaller.marshal(mappingStage));
		} catch (Exception e) {
			logger.error("Error saving mapping stage axes selection", e);
		}
	}

	@Override
	public void loadState(Map<String, String> persistedState) {
		final String json = persistedState.get(MAPPING_STAGE_KEY_JSON);
		if (json == null || json.isEmpty()) return;
		final IMarshallerService marshaller = getService(IMarshallerService.class);
		try {
			final MappingStageInfo savedStage = marshaller.unmarshal(json, MappingStageInfo.class);
			final MappingStageInfo mappingStage = getService(MappingStageInfo.class);
			mappingStage.setPlotXAxisName(savedStage.getPlotXAxisName());
			mappingStage.setPlotYAxisName(savedStage.getPlotYAxisName());
			mappingStage.setAssociatedAxis(savedStage.getAssociatedAxis());
		} catch (Exception e) {
			logger.error("Error restoring mapping stage axes selection", e);
		}
	}

	@Override
	public void dispose() {
		GDAClientActivator.getDefault().getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
		regionSelector.removeSelectionChangedListener(controller.getRegionSelectorListener());
		controller.detachViewUpdater(viewUpdater);
		controller.detachStatusMessageConsumer(statusUpdater);
	}

	/**
	 * This is the function that should be run to modify the Path control when the shape is changed. It is passed to the
	 * controller to be executed. It must also manually reset the combo selection as the change may not have been
	 * triggered from this section. In order to do this, it must remove the listener first, update the control with the
	 * current list of region objects, set the selection and then add back the listener.
	 *
	 * @param scanPathList
	 * @param scanPath
	 */
	private final void updatePathControl(RegionPathState updated) {
		pathSelector.setInput(updated.scanPathList());
		if (updated.scanPathList().contains(updated.scanPathModel())) {
			pathSelector.setSelection(new StructuredSelection(updated.scanPathModel()), true);
		} else if (!updated.scanPathList().isEmpty()) {
			// Select the first path by default
			pathSelector.setSelection(new StructuredSelection(updated.scanPathList().get(0)), true);
		} else {
			pathSelector.setSelection(StructuredSelection.EMPTY, true);
		}
		regionSelector.removeSelectionChangedListener(controller.getRegionSelectorListener());
		logger.debug("Setting region combo selection to : {}", updated.scanRegionShape());
		regionSelector.setInput(updated.scanRegionList().toArray());
		regionSelector.setSelection(new StructuredSelection(updated.scanRegionShape()));
		regionSelector.addSelectionChangedListener(controller.getRegionSelectorListener());
	}

	/**
	 * Listen for property change events that may affect this section
	 */
	private void handlePropertyChange(PropertyChangeEvent event) {
		final String propertyId = event.getProperty();
		if (propertyId.equals(PROPERTY_REGION_AND_PATH)) {
			final Object newValue = event.getNewValue();
			logger.debug("Region and path change event received: {}", newValue);
			if (newValue instanceof String && !((String) newValue).isEmpty()) {
				handleRegionAndPathEvent((String) newValue);
			}
		}
	}

	private void handleRegionAndPathEvent(String value) {
		try {
			final Gson gson = new Gson();
			@SuppressWarnings("unchecked")
			final Map<String, Object> regionAndPathMap = gson.fromJson(value, HashMap.class);

			// Change to region
			final String region = (String) regionAndPathMap.get("region");
			if (!StringUtils.isEmpty(region)) {
				final IMappingScanRegionShape scanRegion = scanRegionMap.get(region);
				if (scanRegion == null) {
					throw new IllegalArgumentException("Invalid region " + region);
				}
				syncExec(() -> controller.getRegionSelectorListener().handleRegionChange(scanRegion));
			}

			// Change to path
			final String path = (String) regionAndPathMap.get("path");
			if (!StringUtils.isEmpty(path)) {
				// Get the available scan paths for the current region
				final Map<String, IMapPathModel> scanPathMap = controller.getScanPathListAndLinkPath().stream()
						.collect(toMap(IScanPointGeneratorModel::getName, identity()));

				final IMapPathModel scanPath = scanPathMap.get(path);
				if (scanPath == null) {
					throw new IllegalArgumentException("No scan path corresponding to " + path);
				}
				syncExec(() -> {
					final IMappingScanRegionShape scanRegion = controller.getScanRegionShape();
					controller.updateMappingBeanScanRegion(scanRegion, scanPath);
					controller.getRegionSelectorListener().handleRegionChange(scanRegion);
				});
			}

			// Changes to scan region properties
			controller.getScanRegionShape().updateFromPropertiesMap(regionAndPathMap);

			// Changes to scan path properties
			controller.getScanPathModel().updateFromPropertiesMap(regionAndPathMap);

			// Update GUI
			asyncExec(this::rebuildMappingSection);
		} catch (Exception e) {
			logger.error("Invalid region/path parameter: {}", value, e);
		}
	}
}
