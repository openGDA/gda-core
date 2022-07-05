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

package uk.ac.diamond.daq.mapping.ui.xanes;

import static uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.TrackingMethod.EDGE;
import static uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.TrackingMethod.REFERENCE;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.createModelFromEdgeSelection;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.getOuterScannable;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.roundDouble;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_ENFORCE_SHAPE;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_LINES_TO_TRACK;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_LINES_TO_TRACK_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_SCAN_PARAMETERS;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_USE_EDGE;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_USE_REFERENCE;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.swtdesigner.SWTResourceManager;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.EdgeToEnergy;
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

	private static final int NUM_COLUMNS = 5;

	private static final int MAX_NUMBER_OFFSET = 1000;

	/**
	 * The edge parameters to pass to the XANES script
	 */
	private XanesEdgeParameters scanParameters;

	/**
	 * The name of the energy scannable for this beamline
	 */
	private String energyScannableName;

	/**
	 * Combo box allowing a choice of lines to track<p>
	 * Populated from the currently-selected processing files
	 */
	private ComboViewer linesToTrackCombo;

	private Spinner energyOffsetSpinner;
	private Text edgeEnergyText;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		final DataBindingContext dataBindingContext = getDataBindingContext();

		// Report if bean is not properly configured, but continue creating the view
		if (energyScannableName == null || energyScannableName.isEmpty()) {
			logger.error("Energy scannable has not been set");
		}

		// If loadState() has not loaded saved parameters, create empty object
		if (scanParameters == null) {
			scanParameters = new XanesEdgeParameters();
		}

		content = createComposite(parent, NUM_COLUMNS, false);
		GridDataFactory.swtDefaults().applyTo(content);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(content);
		content.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		// Title
		createLabel(content, getMessage(XANES_SCAN_PARAMETERS), NUM_COLUMNS);

		// Element/edge drop-down list
		final XanesEdgeCombo elementsAndEdgeCombo = new XanesEdgeCombo(content);
		elementsAndEdgeCombo.addSelectionChangedListener(e -> handleEdgeSelectionChanged(elementsAndEdgeCombo.getSelection()));

		// Energy offset
		createLabel(content, "Energy Offset (eV)", 0);
		energyOffsetSpinner = new Spinner(content, SWT.BORDER);
		energyOffsetSpinner.setMaximum(Integer.MAX_VALUE);
		energyOffsetSpinner.setMinimum(Integer.MIN_VALUE);
		energyOffsetSpinner.setDigits(0);
		energyOffsetSpinner.addModifyListener(e -> handleEdgeSelectionChanged(elementsAndEdgeCombo.getSelection()));

		createLabel(content, "Edge Energy (keV)", 0);
		edgeEnergyText = new Text(content, SWT.BORDER);
		edgeEnergyText.setEditable(false);

		Composite composite = createComposite(parent, NUM_COLUMNS, false);
		GridDataFactory.swtDefaults().applyTo(composite);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(composite);
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		// Lines to track combo box
		createLabel(composite, getMessage(XANES_LINES_TO_TRACK), 0);
		linesToTrackCombo = new ComboViewer(composite);
		linesToTrackCombo.getCombo().setToolTipText(getMessage(XANES_LINES_TO_TRACK_TOOLTIP));
		GridDataFactory.fillDefaults().indent(7, SWT.NONE).hint(87, SWT.DEFAULT).applyTo(linesToTrackCombo.getCombo());

		linesToTrackCombo.setContentProvider(ArrayContentProvider.getInstance());
		linesToTrackCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((LinesToTrackEntry) element).getLine();
			}
		});
		updateControls();

		// Bind combo boxes to model
		final IObservableValue<EdgeToEnergy> edgeComboObservable = elementsAndEdgeCombo.getObservableValue();
		final IObservableValue<EdgeToEnergy> edgeModelObservable = PojoProperties.value("edgeToEnergy", EdgeToEnergy.class).observe(scanParameters);
		dataBindingContext.bindValue(edgeComboObservable, edgeModelObservable);

		final IObservableValue<LinesToTrackEntry> linesComboObservable = ViewerProperties.singleSelection(LinesToTrackEntry.class).observe(linesToTrackCombo);
		final IObservableValue<LinesToTrackEntry> linesModelObservable = PojoProperties.value("linesToTrack", LinesToTrackEntry.class).observe(scanParameters);
		dataBindingContext.bindValue(linesComboObservable, linesModelObservable);

		// Radio buttons to choose tracking method (reference/edge)
		final SelectObservableValue<String> radioButtonObservable = new SelectObservableValue<>();
		final Button btnUseReference = createRadioButton(composite, getMessage(XANES_USE_REFERENCE));
		radioButtonObservable.addOption(REFERENCE.toString(), WidgetProperties.buttonSelection().observe(btnUseReference));
		final Button btnUseEdge = createRadioButton(composite, getMessage(XANES_USE_EDGE));
		radioButtonObservable.addOption(EDGE.toString(), WidgetProperties.buttonSelection().observe(btnUseEdge));

		// Bind radio buttons to model
		final IObservableValue<TrackingMethod> radioButtonModelObservable = PojoProperties.value("trackingMethod", TrackingMethod.class).observe(scanParameters);
		dataBindingContext.bindValue(radioButtonObservable, radioButtonModelObservable);

		if (scanParameters.getTrackingMethod().equals(REFERENCE.toString())) {
			btnUseReference.setSelection(true);
		} else if (scanParameters.getTrackingMethod().equals(EDGE.toString())) {
			btnUseEdge.setSelection(true);
		}

		// Check box to switch Step -> Points models to prevent floating point issues changing the shape of a scan
		Button enforcedShape = createCheckButton(composite, getMessage(XANES_ENFORCE_SHAPE));
		enforcedShape.setSelection(true);

		// Bind check box to model
		final IObservableValue<Boolean> enforcedShapeModelObservable = PojoProperties.value("enforcedShape", boolean.class).observe(scanParameters);
		final IObservableValue<Boolean> enforcedShapeObservable = WidgetProperties.buttonSelection().observe(enforcedShape);
		dataBindingContext.bindValue(enforcedShapeObservable, enforcedShapeModelObservable);

		// Set initial visibility
		setContentVisibility();
	}

	private static Button createRadioButton(Composite parent, String text) {
		final Button button = new Button(parent, SWT.RADIO);
		button.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		button.setText(text);
		return button;
	}

	private static Button createCheckButton(Composite parent, String text) {
		final Button button = new Button(parent, SWT.CHECK);
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

	@Override
	public void updateControls() {
		updateLinesToTrack();
	}

	/**
	 * Populate the "lines to track" drop-down list with the lines available in the selected processing files (if any)
	 */
	private void updateLinesToTrack() {
		// Save the current selection
		final IStructuredSelection currentSelection = linesToTrackCombo.getStructuredSelection();

		// Read all selected processing files and extract lines to track
		final SortedMap<String, SortedSet<String>> linesToTrack = getLinesToTrack(getBean().getProcessingRequest());

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
			// Add blank line to start of the list
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
	 * Read the "lines to track" available in selected processing file(s)
	 *
	 * @param processingRequest
	 *            processing request included in the mapping bean
	 * @return map of processing files to the line(s) tracked by each one
	 */
	@SuppressWarnings("unchecked")
	private static SortedMap<String, SortedSet<String>> getLinesToTrack(final Map<String, Collection<Object>> processingRequest) {
		final SortedMap<String, SortedSet<String>> linesToTrack = new TreeMap<>();

		final Collection<?> dawnEntry = processingRequest.get("dawn");
		if (dawnEntry != null) {
			for (String jsonFilePath : (Collection<String>) dawnEntry) {
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
		return linesToTrack;
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
	 *            path to the processing file (in Nexus format)
	 */
	private static List<String> getProcessingLinesFromFile(String processingFilePath) {
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
			final String xrfLinesMember = "lineGroupsXRF";
			if (!jObject.has(xrfLinesMember)) {
				throw new NexusException("No XRF line data");
			}
			for (JsonElement lineGroups : jObject.getAsJsonArray(xrfLinesMember)) {
				lines.add(lineGroups.getAsString().replace('\u03B1', 'a').replace('\u03B2', 'b'));
			}
		} catch (NexusException e) {
			logger.warn("Cannot get processing data from file {}", processingFilePath, e);
		}
		return lines;
	}

	/**
	 * Update {@link #energyScannable} when the user selects an edge
	 *
	 * @param edgeEnergy
	 *            Energy of the edge selected by the user
	 */
	private void handleEdgeSelectionChanged(IStructuredSelection selection) {
		final EdgeToEnergy selectedEdge = (EdgeToEnergy) selection.getFirstElement();
		if (selectedEdge == null) {
			return;
		}

		final double edgeEnergy = getEdgeEnergy(selectedEdge.getEnergy());
		edgeEnergyText.setText(String.valueOf(edgeEnergy));
		final IAxialModel scanPathModel = createModelFromEdgeSelection(edgeEnergy, energyScannableName);

		final IScanModelWrapper<IAxialModel> energyScannable = getOuterScannable(getBean(), energyScannableName);
		if (energyScannable != null) {
			energyScannable.setModel(scanPathModel);
		}

		// Refresh outer scannables section to update text box
		getView().getSection(OuterScannablesSection.class).updateControls();
	}

	private double getEdgeEnergy(double edgeEnergy) {
		double energyOffset = Double.parseDouble(energyOffsetSpinner.getText()) / 1000;
		return roundDouble(edgeEnergy + energyOffset);
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

	public String getEnergyScannableName() {
		return energyScannableName;
	}

	public void setEnergyScannableName(String energyScannableName) {
		this.energyScannableName = energyScannableName;
	}
}
