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

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.CONTROLS_WIDTH;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.createModelFromEdgeSelection;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.getOuterScannable;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.roundDouble;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_SCAN_PARAMETERS;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_USE_EDGE;
import static uk.ac.gda.ui.tool.ClientMessages.XANES_USE_REFERENCE;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

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
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tschoonj.xraylib.Xraylib;
import com.github.tschoonj.xraylib.XraylibException;
import com.swtdesigner.SWTResourceManager;

import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.EdgeToEnergy;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.LineToTrack;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.SparseParameters;
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
public class XanesParametersSection extends AbstractHideableMappingSection {
	private static final Logger logger = LoggerFactory.getLogger(XanesParametersSection.class);

	private static final int NUM_COLUMNS = 5;

	private static final String XANES_SCAN_KEY = "XanesScan.json";
	/**
	 * Maps Siegbahn lines notation with the IUPAC transition macros
	 */
	private static final SortedMap<String, Integer> LINE_MACROS = new TreeMap<>(Map.of("Ka", Xraylib.KL3_LINE, "La", Xraylib.L3M5_LINE));

	/**
	 * Maximum energy of K-alpha and L-alpha transitions that could be reached
	 */
	private static final double MAX_KEV_ENERGY = 20;

	private static final int MIN_EV_ENERGY_OFFSET = -1000;
	private static final int MAX_EV_ENERGY_OFFSET = 1000;

	/**
	 * The edge parameters to pass to the XANES script
	 */
	private XanesEdgeParameters scanParameters;

	/**
	 * The name of the energy scannable for this beamline
	 */
	private String energyScannableName;

	private Spinner energyOffsetSpinner;

	private ComboViewer linesCombo;

	private XanesElementsList elementAndEdgesList;
	private XanesElementsList linesToTrackList;

	private GridData gridData;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		dataBindingContext = getDataBindingContext();

		// Report if bean is not properly configured, but continue creating the view
		if (energyScannableName == null || energyScannableName.isEmpty()) {
			logger.error("Energy scannable has not been set");
		}

		// If loadState() has not loaded saved parameters, create empty object
		if (scanParameters == null) {
			scanParameters = new XanesEdgeParameters();
		}

		content = createComposite(parent, 1);

		gridData = new GridData();
		gridData.widthHint = CONTROLS_WIDTH;

		Composite elementsAndEdgeComposite = createComposite(content, NUM_COLUMNS);

		createLabel(elementsAndEdgeComposite, getMessage(XANES_SCAN_PARAMETERS), NUM_COLUMNS);

		final XanesEdgeCombo elementsAndEdgeCombo = new XanesEdgeCombo(elementsAndEdgeComposite, elementAndEdgesList);
		elementsAndEdgeCombo.addSelectionChangedListener(e -> handleEdgeSelectionChanged(elementsAndEdgeCombo.getSelection()));

		createLabel(elementsAndEdgeComposite, "Energy Offset (eV)", 1);
		energyOffsetSpinner = new Spinner(elementsAndEdgeComposite, SWT.BORDER);
		energyOffsetSpinner.addModifyListener(e -> handleEdgeSelectionChanged(elementsAndEdgeCombo.getSelection()));
		energyOffsetSpinner.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		energyOffsetSpinner.setMinimum(MIN_EV_ENERGY_OFFSET);
		energyOffsetSpinner.setMaximum(MAX_EV_ENERGY_OFFSET);
		energyOffsetSpinner.setIncrement(1);
		energyOffsetSpinner.setSelection(0);

		// Bind combo boxes to model
		final IObservableValue<EdgeToEnergy> edgeComboObservable = elementsAndEdgeCombo.getObservableValue();
		final IObservableValue<EdgeToEnergy> edgeModelObservable = PojoProperties.value("edgeToEnergy", EdgeToEnergy.class).observe(scanParameters);
		dataBindingContext.bindValue(edgeComboObservable, edgeModelObservable);

		if (linesToTrackList != null) {
			createLinesToTrackSection();
		} else {
			createSparseSection();
		}

		updateControls();

		setContentVisibility();
	}

	private void createSparseSection() {
		XanesEdgeParameters xanesParameters = getScanParameters();
		SparseParameters sparseParameters = new SparseParameters();
		xanesParameters.setSparseParameters(sparseParameters);

		Composite composite = createComposite(content, 2);

		createLabel(composite, "Percentage (%)", 1);

		Spinner spinner = new Spinner(composite, SWT.BORDER);
		spinner.setToolTipText("Set percentage of y positions to scan");
		spinner.addModifyListener(e -> xanesParameters.getSparseParameters().setPercentage(spinner.getSelection()));
		spinner.setSelection(xanesParameters.getSparseParameters().getPercentage());
		spinner.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
	}

	private void createLinesToTrackSection() {
		Composite composite = createComposite(content, NUM_COLUMNS);

		Button linesToTrackBtn = createButton(composite, SWT.CHECK, "Tracking");
		linesToTrackBtn.setLayoutData(gridData);
		linesToTrackBtn.setSelection(true);

		var elementNames = linesToTrackList.getXanesElements().stream().map(XanesScanningUtils::getComboEntry).toList();
		ComboViewer elementsCombo = new ComboViewer(composite);
		elementsCombo.getCombo().setLayoutData(gridData);
		elementsCombo.getCombo().setToolTipText("Select element to track");
		elementsCombo.setContentProvider(ArrayContentProvider.getInstance());
		elementsCombo.setInput(elementNames);
		elementsCombo.addSelectionChangedListener(this::findAtomicNumber);

		linesCombo = new ComboViewer(composite);
		linesCombo.getCombo().setLayoutData(gridData);
		linesCombo.getCombo().setToolTipText("Select XRF line");
		linesCombo.setContentProvider(ArrayContentProvider.getInstance());

		final IObservableValue<LineToTrack> linesComboObservable = ViewerProperties.singleSelection(LineToTrack.class).observe(linesCombo);
		final IObservableValue<LineToTrack> linesModelObservable = PojoProperties.value("lineToTrack", LineToTrack.class).observe(scanParameters);
		dataBindingContext.bindValue(linesComboObservable, linesModelObservable);

		Button useReferenceBtn = createButton(composite, SWT.RADIO, getMessage(XANES_USE_REFERENCE));
		Button useEdgeBtn = createButton(composite, SWT.RADIO, getMessage(XANES_USE_EDGE));

		if (scanParameters.getTrackingMethod().equals(TrackingMethod.REFERENCE)) {
			useReferenceBtn.setSelection(true);
		} else if (scanParameters.getTrackingMethod().equals(TrackingMethod.EDGE)) {
			useEdgeBtn.setSelection(true);
		}

		final IObservableValue<TrackingMethod> radioButtonModelObservable = PojoProperties.value("trackingMethod", TrackingMethod.class).observe(scanParameters);
		final SelectObservableValue<TrackingMethod> radioButtonObservable = new SelectObservableValue<>();
		radioButtonObservable.addOption(TrackingMethod.REFERENCE, WidgetProperties.buttonSelection().observe(useReferenceBtn));
		radioButtonObservable.addOption(TrackingMethod.EDGE, WidgetProperties.buttonSelection().observe(useEdgeBtn));
		dataBindingContext.bindValue(radioButtonObservable, radioButtonModelObservable);

		linesToTrackBtn.addSelectionListener(widgetSelectedAdapter(e -> {
			var selected = linesToTrackBtn.getSelection();

			elementsCombo.getCombo().setEnabled(selected);
			linesCombo.getCombo().setEnabled(selected);

			useEdgeBtn.setSelection(!selected);
			useReferenceBtn.setSelection(selected);
			useReferenceBtn.notifyListeners(SWT.Selection, new Event());
			useEdgeBtn.notifyListeners(SWT.Selection, new Event());

			if (selected) {
				elementsCombo.setInput(elementNames);
			} else {
				elementsCombo.setInput(Collections.emptyList());
				linesCombo.setInput(Collections.emptyList());
				linesCombo.setSelection(null);
			}
		}));
	}

	private Composite createComposite(Composite parent, int numColumns) {
		final Composite composite = createComposite(parent, numColumns, false);
		GridDataFactory.swtDefaults().applyTo(parent);
		GridLayoutFactory.swtDefaults().numColumns(numColumns).applyTo(composite);
		return composite;
	}

	private static Button createButton(Composite parent, int style, String text) {
		final Button button = new Button(parent, style);
		button.setText(text);
		return button;
	}

	private static Label createLabel(Composite parent, String text, int span) {
		final Label label = new Label(parent, SWT.WRAP);
		GridDataFactory.swtDefaults().span(span, 1).applyTo(label);
		label.setText(text);
		return label;
	}

	private void findAtomicNumber(SelectionChangedEvent event) {
		String elementName = String.valueOf(event.getStructuredSelection().getFirstElement()).replace("*", "");
		try {
			int atomicNumber = Xraylib.SymbolToAtomicNumber(elementName);
			updateLinesCombo(elementName, atomicNumber);
		} catch (XraylibException e) {
			linesCombo.setInput(null);
		}
	}

	private void updateLinesCombo(String elementName, int atomicNumber) {
		List<LineToTrack> lines = new ArrayList<>();
		for (Map.Entry<String, Integer> lineMacro : LINE_MACROS.entrySet()) {
			try {
				double energy = Xraylib.LineEnergy(atomicNumber, lineMacro.getValue());
				if (energy < MAX_KEV_ENERGY) {
					var lineToTrack = new LineToTrack(elementName, lineMacro.getKey());
					lines.add(lineToTrack);
				}
			} catch (XraylibException e) {
				// go to next line
			}
		}

		linesCombo.setInput(lines);
		linesCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((LineToTrack) element).getLine();
			}
		});
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

		final Optional<IScanModelWrapper<IAxialModel>> energyScannable = getOuterScannable(getBean(), energyScannableName);
		if (energyScannable.isPresent()) {
			double energyOffset = Double.parseDouble(energyOffsetSpinner.getText()) / 1000;
			double edgeEnergy = roundDouble(selectedEdge.getEnergy() + energyOffset);
			energyScannable.get().setModel(createModelFromEdgeSelection(edgeEnergy, energyScannableName));
		}

		// Refresh outer scannables section to update text box
		getView().getSection(OuterScannablesSection.class).updateControls();
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

	public void setEnergyScannableName(String energyScannableName) {
		this.energyScannableName = energyScannableName;
	}

	public XanesElementsList getElementAndEdgesList() {
		return elementAndEdgesList;
	}

	public void setElementAndEdgesList(XanesElementsList elementAndEdgesList) {
		this.elementAndEdgesList = elementAndEdgesList;
	}

	public XanesElementsList getLinesToTrackList() {
		return linesToTrackList;
	}

	public void setLinesToTrackList(XanesElementsList linesToTrackList) {
		this.linesToTrackList = linesToTrackList;
	}

}
