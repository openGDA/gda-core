/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.polarisation;

import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_POLARISATION_SCAN_PARAMS_JSON;
import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_SCAN_REQUEST_JSON;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.DecimalFormat;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.PolarisationScanParameters;
import uk.ac.diamond.daq.mapping.api.PolarisationScanParameters.Polarisation;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.EdgeToEnergy;
import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;
import uk.ac.diamond.daq.mapping.ui.xanes.XanesEdgeCombo;

/**
 * A customised submit section for the Mapping view, written for I08/I08-1 but available to all beamlines, to submit a
 * scan to be run with different polarisations.<br>
 * It adds a check box which, if checked, serialises the ScanRequest, puts it in the Jython namespace and calls the
 * script defined in the variable scriptFilePath. A suitable script must be written for the beamline.<br>
 * If the check box is not checked, the scan will be submitted in the normal way.
 */
public class PolarisationSubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(PolarisationSubmitScanSection.class);

	private static final int NUM_COLUMNS_POLARISATION_SECTION = 5;
	private static final int NUM_COLUMNS_SUBMIT_SECTION = 4;

	private static final String PROPERTY_NAME_POLARISATION_SCAN_KEY = "polarisation.scan.key";
	private static final String DEFAULT_POLARISATION_SCAN_KEY = "PolarisationScan.json";

	/** The key under which Eclipse stores/restores the parameters for this section */
	private static final String POLARISATION_SCAN_KEY = LocalProperties.get(PROPERTY_NAME_POLARISATION_SCAN_KEY, DEFAULT_POLARISATION_SCAN_KEY);

	/** Parameters to pass to server to submit the scan */
	private PolarisationScanParameters scanParameters;

	/** Mapping of absorption edge to corresponding phase motor position */
	private Map<String, Double> edgeToPhasePosition = Collections.emptyMap();

	/** Text box (read-only) to show phase corresponding to the edge chosen */
	private Label phase;
	/**
	 * Script to run when the {@code Submit} button is pressed, if {@link polarisationCheckbox} is checked.<br>
	 * This is configurable in Spring if necessary, but should generally not be changed.
	 */
	private String scriptFilePath = "polarisation/submit_polarisation_scan.py";

	@Override
	protected void createSubmitSection() {
		// Polarisation-specific controls
		createPolarisationComposite();

		// Standard "submit to script" controls
		var submitComposite = new Composite(getComposite(), SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS_SUBMIT_SECTION).applyTo(submitComposite);

		createSubmitButton(submitComposite);
	}

	/**
	 * GUI to set up polarisation parameters:
	 * <ul>
	 * <li>checkbox to choose polarisation scan rather than "normal" scan</li>
	 * <li>drop-down box to choose absorption edge</li>
	 * </ul>
	 */
	private void createPolarisationComposite() {
		var polarisationComposite = new Composite(getComposite(), SWT.NONE);
		GridDataFactory.swtDefaults().span(2, 1).applyTo(polarisationComposite);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS_POLARISATION_SECTION).spacing(15, 0).applyTo(polarisationComposite);

		dataBindingContext = new DataBindingContext();

		// Report if section is not properly configured, but continue creating the view
		if (edgeToPhasePosition == null || edgeToPhasePosition.isEmpty()) {
			logger.error("ID gap mapping has not been set");
		}

		// If loadState() has not loaded saved parameters, create empty object
		if (scanParameters == null) {
			scanParameters = new PolarisationScanParameters();
		}

		// Check box for polarisation or "normal" scan
		var polarisationCheckbox = new Button(polarisationComposite, SWT.CHECK);
		GridDataFactory.swtDefaults().applyTo(polarisationCheckbox);
		polarisationCheckbox.setText("Polarisation scan");

		var polarisationParams = new Composite(polarisationComposite, SWT.NONE);
		GridDataFactory.fillDefaults().span(4, 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(polarisationParams);
		GridLayoutFactory.fillDefaults().numColumns(NUM_COLUMNS_POLARISATION_SECTION-1).applyTo(polarisationParams);

		 // Combo box to choose the absorption edge to be scanned
		var elementsAndEdgeCombo = new XanesEdgeCombo(polarisationParams);
		elementsAndEdgeCombo.addSelectionChangedListener(this::handleEdgeSelectionChanged);

		phase = new Label(polarisationParams, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(phase);

		// Combo box to choose which polarisation to run first
		var runFirstComposite = new Composite(polarisationParams, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(runFirstComposite);
		GridLayoutFactory.swtDefaults().numColumns(Polarisation.values().length + 1).applyTo(runFirstComposite);

		var runFirstText = new Label(runFirstComposite, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(runFirstText);
		runFirstText.setText("Run first:");

		// observable to track radio selection
		SelectObservableValue<Polarisation> runFirstSelection = new SelectObservableValue<>();

		Arrays.stream(Polarisation.values())
		 .forEach(direction -> {
			 var button = new Button(runFirstComposite, SWT.RADIO);
			 button.setText(direction.toString());
			 runFirstSelection.addOption(direction, WidgetProperties.buttonSelection().observe(button));
		 });

		// Bind check box to model
		final IObservableValue<Boolean> polarisationScanModelObservable = PojoProperties.value("polarisationScan", boolean.class).observe(scanParameters);
		final IObservableValue<Boolean> polarisationScanCheckboxObservable = WidgetProperties.buttonSelection().observe(polarisationCheckbox);
		dataBindingContext.bindValue(polarisationScanCheckboxObservable, polarisationScanModelObservable);

		// Enable/disable polarisation controls with state of checkbox
		ISideEffect.create(polarisationScanCheckboxObservable::getValue, enabled -> {
			elementsAndEdgeCombo.setEnabled(enabled);
			phase.setEnabled(enabled);
			Arrays.stream(runFirstComposite.getChildren()).forEach(control -> control.setEnabled(enabled));
		});

		// Bind absorption edge combo box to model (must do this after creating ID gap text box, as it updates the ID gap)
		final IObservableValue<EdgeToEnergy> edgeComboObservable = elementsAndEdgeCombo.getObservableValue();
		final IObservableValue<EdgeToEnergy> edgeModelObservable = PojoProperties.value("edgeToEnergy", EdgeToEnergy.class).observe(scanParameters);
		dataBindingContext.bindValue(edgeComboObservable, edgeModelObservable);

		// Bind polarisation radio selection to model
		final IObservableValue<Polarisation> polarisationModelObservable =
				PojoProperties.value("runFirst", Polarisation.class).observe(scanParameters);
		dataBindingContext.bindValue(runFirstSelection, polarisationModelObservable);
	}

	/**
	 * Update ID gap & phase when the user selects an absorption edge
	 */
	private void handleEdgeSelectionChanged(SelectionChangedEvent selectionEvent) {
		var edgeToEnergy = (EdgeToEnergy) selectionEvent.getStructuredSelection().getFirstElement();
		if (edgeToEnergy == null) {
			return;
		}
		final String edge = edgeToEnergy.getEdge();
		final Double phasePosition = edgeToPhasePosition.get(edge);
		scanParameters.setPhasePosition(phasePosition);
		if (phasePosition == null) {
			logger.warn("No phase motor positions defined for edge {}", edge);
			return;
		}

		var df = new DecimalFormat("#.0#");
		phase.setText("Phase: " + df.format(phasePosition));
	}

	@Override
	protected void submitScan() {
		if (!scanParameters.isPolarisationScan()) {
			// Ordinary mapping scan
			super.submitScan();
			return;
		}

		if (scanParameters.getEdgeToEnergy() == null) {
			MessageDialog.openError(getShell(), "No absorption edge selected", "You must select an absorption edge for the polarisation scan");
			return;
		}

		final IScriptService scriptService = getService(IScriptService.class);
		var scanRequest = getScanRequest(getBean());
		try {
			// Serialise ScanRequest and edge parameters to JSON and put in the Jython namespace.
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));
			scriptService.setNamedValue(VAR_NAME_POLARISATION_SCAN_PARAMS_JSON, marshallerService.marshal(scanParameters));
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
			return;
		}

		logger.info("Running polarisation scan with parameters {}", scanParameters);
		Async.execute(() -> runScript(scriptFilePath, "Polarisation scanning script"));
	}

	@Override
	public void saveState(Map<String, String> persistedState) {
		try {
			logger.debug("Saving polarisation parameters");
			final IMarshallerService marshaller = getService(IMarshallerService.class);
			persistedState.put(POLARISATION_SCAN_KEY, marshaller.marshal(scanParameters));
		} catch (Exception e) {
			logger.error("Error saving polarisation scan parameters", e);
		}
	}

	@Override
	public void loadState(Map<String, String> persistedState) {
		final String json = persistedState.get(POLARISATION_SCAN_KEY);
		if (json == null || json.isEmpty()) { // This happens when client is reset
			logger.debug("No polarisation parameters to load");
			return;
		}

		try {
			logger.debug("Loading polarisation parameters");
			final IMarshallerService marshaller = getService(IMarshallerService.class);
			scanParameters = marshaller.unmarshal(json, PolarisationScanParameters.class);
		} catch (Exception e) {
			logger.error("Error restoring polarisation scan parameters", e);
		}
	}

	public void setScriptFilePath(String scriptFilePath) {
		this.scriptFilePath = scriptFilePath;
	}

	public void setEdgeToPhasePosition(Map<String, Double> edgeToPhasePosition) {
		this.edgeToPhasePosition = edgeToPhasePosition;
	}
}