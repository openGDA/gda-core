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
import static uk.ac.gda.ui.tool.ClientMessages.XANES_SCAN_PARAMETERS;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_USE_EDGE;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_USE_REFERENCE;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tschoonj.xraylib.Xraylib;
import com.github.tschoonj.xraylib.XraylibException;
import com.swtdesigner.SWTResourceManager;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.EdgeToEnergy;
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
	 * Maps Siegbahn lines notation with the IUPAC transition macros
	 */
	private static final Map<String, Integer> lineMacros = Map.of(
			"Ka", Xraylib.KL3_LINE,
			"La", Xraylib.L3M5_LINE,
			"Ma", Xraylib.M5N7_LINE,
			"Kb", Xraylib.KM3_LINE,
			"Lb", Xraylib.L2M4_LINE
			);

	/**
	 * The edge parameters to pass to the XANES script
	 */
	private XanesEdgeParameters scanParameters;

	/**
	 * The name of the energy scannable for this beamline
	 */
	private String energyScannableName;

	private Text edgeEnergyText;
	private Spinner energyOffsetSpinner;

	private ComboViewer linesCombo;
	private Composite linesToTrackComposite;
	private GridData linesToTrackGridData;

	private Composite percentageComposite;
	private GridData percentageGridData;


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

		content = createComposite(parent, 1, false);
		GridDataFactory.swtDefaults().applyTo(content);
		GridLayoutFactory.swtDefaults().applyTo(content);
		content.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		Composite elementsAndEdgeComposite = createComposite(content, NUM_COLUMNS, false);
		GridDataFactory.swtDefaults().applyTo(elementsAndEdgeComposite);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(elementsAndEdgeComposite);
		elementsAndEdgeComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		// Lines to track combo box
		GridData comboGridData = new GridData();
		comboGridData.horizontalIndent = 7;
		comboGridData.widthHint = 88;

		// Title
		createLabel(elementsAndEdgeComposite, getMessage(XANES_SCAN_PARAMETERS), NUM_COLUMNS);

		// Element/edge drop-down list
		final XanesEdgeCombo elementsAndEdgeCombo = new XanesEdgeCombo(elementsAndEdgeComposite);
		elementsAndEdgeCombo.addSelectionChangedListener(e -> handleEdgeSelectionChanged(elementsAndEdgeCombo.getSelection()));

		// Energy offset
		createLabel(elementsAndEdgeComposite, "Energy Offset (eV)", 0);
		energyOffsetSpinner = new Spinner(elementsAndEdgeComposite, SWT.BORDER);
		energyOffsetSpinner.setMaximum(Integer.MAX_VALUE);
		energyOffsetSpinner.setMinimum(Integer.MIN_VALUE);
		energyOffsetSpinner.setDigits(0);
		energyOffsetSpinner.addModifyListener(e -> handleEdgeSelectionChanged(elementsAndEdgeCombo.getSelection()));

		createLabel(elementsAndEdgeComposite, "Edge Energy (keV)", 0);
		edgeEnergyText = new Text(elementsAndEdgeComposite, SWT.BORDER);
		edgeEnergyText.setEditable(false);

		// Lines to track
		linesToTrackComposite = createComposite(content, NUM_COLUMNS, false);
		linesToTrackGridData = new GridData();
		linesToTrackComposite.setLayoutData(linesToTrackGridData);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(linesToTrackComposite);
		linesToTrackComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		createLabel(linesToTrackComposite, getMessage(XANES_LINES_TO_TRACK), 0);
		ComboViewer elementsCombo = new ComboViewer(linesToTrackComposite);
		elementsCombo.getCombo().setLayoutData(comboGridData);
		elementsCombo.getCombo().setToolTipText("Select element to track");
		elementsCombo.setContentProvider(ArrayContentProvider.getInstance());
		elementsCombo.setInput(getElementNames());
		elementsCombo.addSelectionChangedListener(this::findAtomicNumber);

		linesCombo = new ComboViewer(linesToTrackComposite);
		linesCombo.getCombo().setLayoutData(comboGridData);
		linesCombo.getCombo().setToolTipText("Select XRF line");
		linesCombo.setContentProvider(ArrayContentProvider.getInstance());

		updateControls();

		// Bind combo boxes to model
		final IObservableValue<EdgeToEnergy> edgeComboObservable = elementsAndEdgeCombo.getObservableValue();
		final IObservableValue<EdgeToEnergy> edgeModelObservable = PojoProperties.value("edgeToEnergy", EdgeToEnergy.class).observe(scanParameters);
		dataBindingContext.bindValue(edgeComboObservable, edgeModelObservable);

		final IObservableValue<String> elementComboObservable = ViewerProperties.singleSelection(String.class).observe(elementsCombo);
		final IObservableValue<String> elementModelObservable = PojoProperties.value("element", String.class).observe(scanParameters);
		dataBindingContext.bindValue(elementComboObservable, elementModelObservable);

		final IObservableValue<String> lineComboObservable = ViewerProperties.singleSelection(String.class).observe(linesCombo);
		final IObservableValue<String> lineModelObservable = PojoProperties.value("line", String.class).observe(scanParameters);
		dataBindingContext.bindValue(lineComboObservable, lineModelObservable);

		// Radio buttons to choose tracking method (reference/edge)
		final SelectObservableValue<String> radioButtonObservable = new SelectObservableValue<>();
		final Button btnUseReference = createRadioButton(linesToTrackComposite, getMessage(XANES_USE_REFERENCE));
		radioButtonObservable.addOption(REFERENCE.toString(), WidgetProperties.buttonSelection().observe(btnUseReference));
		final Button btnUseEdge = createRadioButton(linesToTrackComposite, getMessage(XANES_USE_EDGE));
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
		Button enforcedShape = createCheckButton(linesToTrackComposite, getMessage(XANES_ENFORCE_SHAPE));
		enforcedShape.setSelection(true);

		// Bind check box to model
		final IObservableValue<Boolean> enforcedShapeModelObservable = PojoProperties.value("enforcedShape", boolean.class).observe(scanParameters);
		final IObservableValue<Boolean> enforcedShapeObservable = WidgetProperties.buttonSelection().observe(enforcedShape);
		dataBindingContext.bindValue(enforcedShapeObservable, enforcedShapeModelObservable);

		percentageComposite = new Composite(content, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(percentageComposite);
		percentageGridData = new GridData();
		percentageComposite.setLayoutData(percentageGridData);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(percentageComposite);

		createLabel(percentageComposite, "Percentage (%)", 0);
		Spinner percentageSpinner = new Spinner(percentageComposite, SWT.BORDER);
		percentageSpinner.setMinimum(0);
		percentageSpinner.setMaximum(100);
		percentageSpinner.setToolTipText("Set percentage of y positions to scan");
		percentageSpinner.addModifyListener(e -> scanParameters.setPercentage(percentageSpinner.getSelection()));
		percentageSpinner.setSelection(scanParameters.getPercentage());

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

	private List<String> getElementNames() {
		final Map<String, ElementAndEdgesList> elementsAndEdgesMap = Finder.getLocalFindablesOfType(ElementAndEdgesList.class);
		if (elementsAndEdgesMap == null || elementsAndEdgesMap.isEmpty()) {
			logger.error("No elements have been set");
			return Collections.emptyList();
		}

		List<String> elementNames = elementsAndEdgesMap.values()
				.iterator().next().getElementsAndEdges().stream()
				.map(ElementAndEdges::getElementName).toList();

		return Stream.concat(List.of("None").stream(), elementNames.stream()).toList();
	}

	private void findAtomicNumber(SelectionChangedEvent event) {
		String element = String.valueOf(event.getStructuredSelection().getFirstElement());
		try {
			int atomicNumber = Xraylib.SymbolToAtomicNumber(element);
			updateLinesCombo(atomicNumber);
		} catch (XraylibException e) {
			linesCombo.setInput(null);
		}
	}

	private void updateLinesCombo(int atomicNumber) {
		List<String> lines = new ArrayList<>();
		for (Map.Entry<String, Integer> lineMacro : lineMacros.entrySet()) {
			try {
				Xraylib.LineEnergy(atomicNumber, lineMacro.getValue());
				lines.add(lineMacro.getKey());
			} catch (XraylibException e) {
				// go to next line
			}
		}
		linesCombo.setInput(lines);
		linesCombo.setSelection(new StructuredSelection(lines.get(0)));
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

	public void setLinesToTrackVisible(boolean visible) {
		linesToTrackComposite.setVisible(visible);
		linesToTrackGridData.exclude = !visible;
	}

	public void setPercentageVisible(boolean visible) {
		percentageComposite.setVisible(visible);
		percentageGridData.exclude = !visible;
	}

}
