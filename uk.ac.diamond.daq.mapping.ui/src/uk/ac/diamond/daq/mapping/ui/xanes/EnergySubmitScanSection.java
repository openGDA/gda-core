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

import java.util.Collections;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.scanning.api.script.IScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;

public class EnergySubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(EnergySubmitScanSection.class);

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

		Async.execute(() -> runScript(getScriptFilePath(), "Energy focus scanning script"));
	}

	private IAxialModel getEnergyFocusModel(){
		return getBean().getScanDefinition().getOuterScannables().stream()
				.filter(model -> model.getName().equals(getOuterScannableName()) && model.isIncludeInScan())
				.map(IScanModelWrapper::getModel)
				.findFirst()
				.orElse(null);
	}

	private void deselectOuterScannables() {
		getBean().getScanDefinition().getOuterScannables().stream()
				.map(IScanModelWrapper::getName)
				.filter(name -> !name.equals(getOuterScannableName()))
				.toList()
				.forEach(name -> selectOuterScannable(name, false));
	}

	@Override
	protected void onShow() {
		selectOuterScannable(getOuterScannableName(), true);
		deselectOuterScannables();
		relayoutView();
	}

	@Override
	protected void onHide() {
		selectOuterScannable(getOuterScannableName(), false);
		relayoutView();
	}
}