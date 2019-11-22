/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.xanes.ui;

import static com.github.tschoonj.xraylib.Xraylib.K_SHELL;
import static com.github.tschoonj.xraylib.Xraylib.L3_SHELL;
import static uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.TrackingMethod.EDGE;
import static uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.TrackingMethod.REFERENCE;
import static uk.ac.diamond.daq.mapping.xanes.ui.XanesScanningUtils.getOuterScannable;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_ELEMENT_AND_EDGE;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_ELEMENT_AND_EDGE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_LINES_TO_TRACK;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_LINES_TO_TRACK_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_SCAN_PARAMETERS;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_USE_EDGE;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_USE_REFERENCE;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tschoonj.xraylib.Xraylib;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.swtdesigner.SWTResourceManager;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.LinesToTrackEntry;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.TrackingMethod;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractHideableMappingSection;
import uk.ac.diamond.daq.mapping.ui.experiment.OuterScannablesSection;

/**
 * View to allow the user to input the additional parameters required for the XANES scanning script.
 * <p>
 * There are:
 * <ul>
 * <li>Element/edge combination - chosen from a combo box populated from the elementsAndEdges member</li>
 * <li>Use edge/use reference radio buttons</li>
 * </ul>
 * <p>
 * {@link XanesSubmitScanSection} combines these with the standard parameters from the Mapping view (x & y coordinates, detector etc.) and
 * passed to the appropriate script.
 */
public class XanesEdgeParametersSection extends AbstractHideableMappingSection {
	private static final Logger logger = LoggerFactory.getLogger(XanesEdgeParametersSection.class);

	private static final String PROPERTY_NAME_XANES_SCAN_KEY = "xanes.scan.key";
	private static final String DEFAULT_XANES_SCAN_KEY = "XanesScan.json";

	/**
	 * The key under which Eclipse stores/restores XANES parameters
	 */
	private static final String XANES_SCAN_KEY = LocalProperties.get(PROPERTY_NAME_XANES_SCAN_KEY, DEFAULT_XANES_SCAN_KEY);

	private static final int NUM_COLUMNS = 6;

	/**
	 * The edge parameters to pass to the XANES script
	 */
	private XanesEdgeParameters scanParameters;

	/**
	 * The element/edge combinations the user can choose to track
	 */
	private List<ElementAndEdges> elementsAndEdges;

	/**
	 * The name of the energy scannable for this beamline
	 */
	private String energyScannableName;

	/**
	 * Maps shell as string (as set in {@link #elementsAndEdges} to the corresponding {@link Xraylib} constant
	 */
	private static final Map<String, Integer> edgeMap = ImmutableMap.of("K", K_SHELL, "L", L3_SHELL);

	/**
	 * The energy steps around the edge energy (EE)
	 * <p>
	 * For example, <code>-0.1, -0.020, 0.008</code> means "from (EE - 0.1) to (EE - 0.020), move in steps of 0.008"
	 */
	private static final double[][] xanesStepRanges = {
			{ -0.1, -0.020, 0.008 },
			{ -0.019, +0.020, 0.0005 },
			{ +0.021, +0.040, 0.001 },
			{ +0.041, +0.080, 0.002 },
			{ +0.084, +0.130, 0.004 },
			{ +0.136, +0.200, 0.006 } };

	/**
	 * Combo box allowing a choice of lines to track<p>
	 * Populated from the currently-selected processing files
	 */
	private ComboViewer linesToTrackCombo;

	@SuppressWarnings("unchecked")
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		dataBindingContext = new DataBindingContext();

		// Report if bean is not properly configured, but continue creating the view
		if (elementsAndEdges == null || elementsAndEdges.isEmpty()) {
			logger.error("No element/edge combinations have been set");
		}
		if (energyScannableName == null || energyScannableName.isEmpty()) {
			logger.error("Energy scannable has not been set");
		}

		// If loadState() has not loaded saved parameters, create empty object
		if (scanParameters == null) {
			scanParameters = new XanesEdgeParameters();
		}

		content = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(content);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(content);
		content.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		// Title
		createLabel(content, getMessage(XANES_SCAN_PARAMETERS), NUM_COLUMNS);

		// Element/edge drop-down list
		createLabel(content, getMessage(XANES_ELEMENT_AND_EDGE), 1);
		final ComboViewer elementsAndEdgeCombo = new ComboViewer(content);
		elementsAndEdgeCombo.setContentProvider(ArrayContentProvider.getInstance());
		elementsAndEdgeCombo.setInput(createEdgeToEnergyList());
		elementsAndEdgeCombo.getCombo().setToolTipText(getMessage(XANES_ELEMENT_AND_EDGE_TOOLTIP));
		elementsAndEdgeCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((EdgeToEnergy) element).getEdge();
			}
		});
		elementsAndEdgeCombo.addSelectionChangedListener(e -> handleEdgeSelectionChanged(
				(EdgeToEnergy) elementsAndEdgeCombo.getStructuredSelection().getFirstElement()));

		// Lines to track combo box
		createLabel(content, getMessage(XANES_LINES_TO_TRACK), 1);
		linesToTrackCombo = new ComboViewer(content);
		linesToTrackCombo.getCombo().setToolTipText(getMessage(XANES_LINES_TO_TRACK_TOOLTIP));
		GridDataFactory.fillDefaults().hint(80, SWT.DEFAULT).applyTo(linesToTrackCombo.getCombo());

		linesToTrackCombo.setContentProvider(ArrayContentProvider.getInstance());
		linesToTrackCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((LinesToTrackEntry) element).getLine();
			}
		});

		// Bind to model
		final IViewerObservableValue comboObservable = ViewersObservables.observeSingleSelection(linesToTrackCombo);
		final IObservableValue<XanesEdgeParameters> modelObservable = BeanProperties.value("linesToTrack").observe(scanParameters);
		dataBindingContext.bindValue(comboObservable, modelObservable);

		// Radio buttons to choose tracking method (reference/edge)
		final SelectObservableValue<String> radioButtonObservable = new SelectObservableValue<>();
		final Button btnUseReference = createRadioButton(content, getMessage(XANES_USE_REFERENCE));
		radioButtonObservable.addOption(REFERENCE.toString(), WidgetProperties.selection().observe(btnUseReference));
		final Button btnUseEdge = createRadioButton(content, getMessage(XANES_USE_EDGE));
		radioButtonObservable.addOption(EDGE.toString(), WidgetProperties.selection().observe(btnUseEdge));

		// Bind to model
		final IObservableValue<XanesEdgeParameters> radioButtonModelObservable = PojoProperties.value(XanesEdgeParameters.class, "trackingMethod", TrackingMethod.class).observe(scanParameters);
		dataBindingContext.bindValue(radioButtonObservable, radioButtonModelObservable);

		if (scanParameters.getTrackingMethod().equals(REFERENCE.toString())) {
			btnUseReference.setSelection(true);
		} else if (scanParameters.getTrackingMethod().equals(EDGE.toString())) {
			btnUseEdge.setSelection(true);
		}

		// Set initial visibility
		setContentVisibility();
	}

	private static Button createRadioButton(Composite parent, String text) {
		final Button button = new Button(parent, SWT.RADIO);
		button.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		button.setText(text);
		return button;
	}

	private static Label createLabel(Composite parent, String text, int span) {
		final Label label = new Label(parent, SWT.WRAP);
		GridDataFactory.swtDefaults().span(span, 1).applyTo(label);
		label.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		label.setText(text);
		return label;
	}

	/**
	 * Create a list of {@link EdgeToEnergy} objects from {@link #elementsAndEdges} set when the bean is created.<br>
	 * Used as input to the combo viewer
	 * <p>
	 * Radioactive elements are indicated with asterisks around the entry.
	 *
	 * @return list of EdgeToEnergy to set in the combo viewer
	 */
	private List<EdgeToEnergy> createEdgeToEnergyList() {
		final List<EdgeToEnergy> result = new ArrayList<>(elementsAndEdges.size());

		// Iterate over elements
		for (ElementAndEdges elementEntry : elementsAndEdges) {
			final String element = elementEntry.getElementName();
			final int atomicNumber = Xraylib.SymbolToAtomicNumber(element);

			// Iterate over the edges of this element
			for (String edge : elementEntry.getEdges()) {
				final Integer edgeNumber = edgeMap.get(edge);
				if (edgeNumber == null) {
					logger.error("Unknown edge {}", edge);
					continue;
				}
				final String entryFormat = elementEntry.isRadioactive() ? "*%s-%s*" : "%s-%s";
				final String comboEntry = String.format(entryFormat, element, edge);
				result.add(new EdgeToEnergy(comboEntry, Xraylib.EdgeEnergy(atomicNumber, edgeNumber)));
			}
		}
		return result;
	}

	@Override
	public void updateControls() {
		// Save the current selection
		final IStructuredSelection currentSelection = linesToTrackCombo.getStructuredSelection();

		// Read all selected processing files and extract lines to track
		final Map<String, Object> processingRequest = getMappingBean().getProcessingRequest();
		final SortedMap<String, SortedSet<String>> linesToTrack = new TreeMap<>();

		for (Map.Entry<String, Object> entry : processingRequest.entrySet()) {
			if (entry.getKey().equals("dawn")) {
				@SuppressWarnings("unchecked")
				final List<String> jsonFiles = (List<String>) entry.getValue();
				for (String jsonFilePath : jsonFiles) {
					try {
						// Get the path of the processing file and the tracking lines it contains
						final String json = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
						final JsonObject jObject = new JsonParser().parse(json).getAsJsonObject();
						final String processingFilePath = jObject.get("processingFile").getAsString();
						final List<String> lines = getProcessingLinesFromFile(processingFilePath);

						// Add tracking lines and the corresponding processing file path
						for (String line : lines) {
							final SortedSet<String> filePaths = linesToTrack.computeIfAbsent(line, k -> new TreeSet<>());
							filePaths.add(processingFilePath);
						}
					} catch (IOException e) {
						logger.error("Error opening JSON file {}", jsonFilePath, e);
					}
				}
			}
		}

		// Add lines and restore current selection if possible
		final int numLines = linesToTrack.size();
		if (numLines == 0) {
			linesToTrackCombo.setInput(null);
		} else {
			final LinesToTrackEntry[] comboEntries = new LinesToTrackEntry[numLines];
			int i = 0;
			for (Map.Entry<String, SortedSet<String>> entry : linesToTrack.entrySet()) {
				comboEntries[i++] = new LinesToTrackEntry(entry.getKey(), entry.getValue());
			}
			linesToTrackCombo.setInput(comboEntries);
			linesToTrackCombo.insert(new LinesToTrackEntry(), 0);

			// Restore previous selection if possible
			if (currentSelection != null && !currentSelection.isEmpty()) {
				final String line = ((LinesToTrackEntry) currentSelection.getFirstElement()).getLine();
				for (LinesToTrackEntry entry : comboEntries) {
					if (entry.getLine().equals(line)) {
						linesToTrackCombo.setSelection(new StructuredSelection(entry));
						break;
					}
				}
			}
		}
	}

	/**
	 * Extract the lines defined in a processing file and add to the combo box
	 * <p>
	 * We expect the node <code>/entry/process/0/data</code> to contain a string of the form:<br>
	 * <code>{"lineGroupsXRF":["Fe-Kα","Cs-Kβ"],"minExcitationEnergy":1.0,"maxExcitationEnergy":50.0,"plotOptions":"CURRENT_DATASET","roiWidth":10,"externalFilename":null,"externalDatasetName":null}]</code>
	 * <p>
	 * We want to extract the lines from <code>lineGroupsXRF</code>, but to convert Greek letters to the equivalent
	 * Latin characters, as used elsewhere in the Nexus file
	 *
	 * @param processingFilePath
	 *            path to he processing file (in Nexus format)
	 */
	private List<String> getProcessingLinesFromFile(String processingFilePath) {
		final String dataNodePath = "/entry/process/0/data";
		final INexusFileFactory nexusFileFactory = ServiceHolder.getNexusFileFactory();
		final List<String> lines = new ArrayList<>();
		try (NexusFile nexusFile = nexusFileFactory.newNexusFile(processingFilePath)) {
			nexusFile.openToRead();
			final DataNode dataNode = nexusFile.getData(dataNodePath);
			if (dataNode == null) {
				throw new NexusException("Missing data node " + dataNodePath);
			}
			final JsonObject jObject = new JsonParser().parse(dataNode.getString()).getAsJsonObject();
			if (jObject == null) {
				throw new NexusException("Cannot parse data node " + dataNode.toString() + " as JSON");
			}
			for (JsonElement lineGroups : jObject.getAsJsonArray("lineGroupsXRF")) {
				lines.add(lineGroups.getAsString().replace('\u03B1', 'a').replace('\u03B2', 'b'));
			}
		} catch (NexusException e) {
			logger.error("Cannot read file {}", processingFilePath, e);
		}
		return lines;
	}

	/**
	 * Update {@link #energyScannableName} when the user selects an edge
	 *
	 * @param newSelection
	 *            The user selection
	 */
	private void handleEdgeSelectionChanged(EdgeToEnergy selection) {
		logger.debug("Element/edge selection changed to {}", selection);

		final double edgeEnergy = selection.getEnergy();
		final List<AxialStepModel> stepModels = new ArrayList<>(xanesStepRanges.length);

		// Create a step model for each range of energies around the edge
		for (double[] range : xanesStepRanges) {
			stepModels.add(new AxialStepModel(energyScannableName, roundDouble(edgeEnergy + range[0]), roundDouble(edgeEnergy + range[1]), range[2]));
		}

		// Create a multi-step model containing these step models
		final AxialMultiStepModel multiStepModel = new AxialMultiStepModel(energyScannableName, stepModels);

		final IScanModelWrapper<IScanPathModel> energyScannable = getOuterScannable(getMappingBean(), energyScannableName);
		if (energyScannable != null) {
			energyScannable.setModel(multiStepModel);
		}

		// Refresh outer scannables section to update text box
		getMappingView().getSection(OuterScannablesSection.class).updateControls();

		// Update this section
		this.updateControls();
	}

	private double roundDouble(double input) {
		return BigDecimal.valueOf(input).setScale(7, RoundingMode.HALF_UP).doubleValue();
	}

	@Override
	public void saveState(Map<String, String> persistedState) {
		try {
			logger.debug("Saving XANES parameters");
			final IMarshallerService marshaller = getService(IMarshallerService.class);
			persistedState.put(XANES_SCAN_KEY, marshaller.marshal(scanParameters));
		} catch (Exception e) {
			logger.error("Error saving XANES scan parameters", e);
		}
	}

	@Override
	public void loadState(Map<String, String> persistedState) {
		final String json = persistedState.get(XANES_SCAN_KEY);
		if (json == null || json.isEmpty()) { // This happens when client is reset || if no detectors are configured.
			logger.debug("No XANES parameters to load");
			return;
		}

		try {
			logger.debug("Loading XANES parameters");
			final IMarshallerService marshaller = getService(IMarshallerService.class);
			scanParameters = marshaller.unmarshal(json, XanesEdgeParameters.class);
		} catch (Exception e) {
			logger.error("Error restoring XANES scan parameters", e);
		}
	}

	public XanesEdgeParameters getScanParameters() {
		return scanParameters;
	}

	public void setElementsAndEdges(List<ElementAndEdges> elementsAndEdges) {
		this.elementsAndEdges = elementsAndEdges;
	}

	public String getEnergyScannableName() {
		return energyScannableName;
	}

	public void setEnergyScannableName(String energyScannableName) {
		this.energyScannableName = energyScannableName;
	}

	/**
	 * Maps element/edge in user-readable format (e.g. "Fe-K") to the corresponding edge energy<br>
	 * Used as input for the combo box for the user to choose the edge to scan
	 */
	private static class EdgeToEnergy {
		private final String edge;
		private final double energy;

		public EdgeToEnergy(String edge, double energy) {
			this.edge = edge;
			this.energy = energy;
		}

		public String getEdge() {
			return edge;
		}

		public double getEnergy() {
			return energy;
		}

		@Override
		public String toString() {
			return "EdgeToEnergy [edge=" + edge + ", energy=" + energy + "]";
		}
	}
}
