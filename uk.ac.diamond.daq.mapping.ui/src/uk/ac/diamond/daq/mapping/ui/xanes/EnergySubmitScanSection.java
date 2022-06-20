/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_CUSTOM_PARAMS;
import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_SCAN_REQUEST_JSON;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.createModelFromEdgeSelection;
import static uk.ac.diamond.daq.mapping.ui.xanes.XanesScanningUtils.getOuterScannable;

import java.util.Collections;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.EdgeToEnergy;
import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;
import uk.ac.diamond.daq.mapping.ui.experiment.OuterScannablesSection;

public class EnergySubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(EnergySubmitScanSection.class);

	private String energyScannableName;
	private String scriptFilePath = "scanning/submit_energy_scan.py";
	private XanesEdgeCombo elementsAndEdgeCombo;

	@Override
	public void createControls(Composite parent) {
		setButtonColour(new RGB(179, 204, 255));
		super.createControls(parent);
	}

	@Override
	protected void createSubmitSection() {
		createEnergyParameters();

		final Composite submitComposite = new Composite(getComposite(), SWT.NONE);
		GridDataFactory.swtDefaults().span(2,1).applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(submitComposite);

		createSubmitButton(submitComposite);
		createStopButton(submitComposite);
	}

	private void createEnergyParameters() {
		var energyComposite = new Composite(getComposite(), SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(energyComposite);
		GridLayoutFactory.swtDefaults().applyTo(energyComposite);

		elementsAndEdgeCombo = new XanesEdgeCombo(energyComposite);
		elementsAndEdgeCombo.addSelectionChangedListener(e -> handleEdgeSelectionChanged(elementsAndEdgeCombo.getSelection()));
	}

	@Override
	protected void submitScan() {
		final IScriptService scriptService = getService(IScriptService.class);
		final IAxialModel energyFocusModel = getEnergyFocusModel();

		if (energyFocusModel == null) {
			logger.error("Scan submission failed");
			MessageDialog.openError(getShell(), "Error Submitting Scan", "Energy scannable is not selected.");
			return;
		}

		var outerScannables = getBean().getScanDefinition().getOuterScannables();
		getBean().getScanDefinition().setOuterScannables(Collections.emptyList());

		final ScanRequest scanRequest = getScanRequest(getBean());

		// Restore outer scannables selection
		getBean().getScanDefinition().setOuterScannables(outerScannables);

		try {
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));
			scriptService.setNamedValue(VAR_NAME_CUSTOM_PARAMS, marshallerService.marshal(energyFocusModel));
		} catch(Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
			return;
		}

		Async.execute(() -> runScript(scriptFilePath, "Energy focus scanning script"));
	}

	private  IAxialModel getEnergyFocusModel(){
		return getBean().getScanDefinition().getOuterScannables().stream()
				.filter(model -> model.getName().equals(energyScannableName) && model.isIncludeInScan())
				.map(IScanModelWrapper::getModel)
				.findFirst()
				.orElse(null);
	}

	private void deselectOuterScannables() {
		getBean().getScanDefinition().getOuterScannables().stream()
				.map(IScanModelWrapper::getName)
				.filter(name -> !name.equals(energyScannableName))
				.collect(Collectors.toList())
				.forEach(name -> selectOuterScannable(name, false));
	}

	@Override
	protected void onShow() {
		selectOuterScannable(energyScannableName, true);
		deselectOuterScannables();
		elementsAndEdgeCombo.setSelection(elementsAndEdgeCombo.getSelection());
		relayoutView();
	}

	@Override
	protected void onHide() {
		selectOuterScannable(energyScannableName, false);
		relayoutView();
	}

	public void setEnergyScannableName(String energyScannableName) {
		this.energyScannableName = energyScannableName;
	}

	public void setScriptFilePath(String scriptFilePath) {
		this.scriptFilePath = scriptFilePath;
	}

	private void handleEdgeSelectionChanged(IStructuredSelection selection) {
		final EdgeToEnergy selectedEdge = (EdgeToEnergy) selection.getFirstElement();
		if (selectedEdge == null) {
			return;
		}
		final IAxialModel scanPathModel = createModelFromEdgeSelection(selectedEdge.getEnergy(), energyScannableName);

		final IScanModelWrapper<IAxialModel> energyScannable = getOuterScannable(getBean(), energyScannableName);
		if (energyScannable != null) {
			energyScannable.setModel(scanPathModel);
		}

		// Refresh outer scannables section to update text box
		getView().getSection(OuterScannablesSection.class).updateControls();
	}
}