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

import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_SCAN_REQUEST_JSON;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;

/**
 * A customised submit section for the Mapping view, written for I08/I08-1 but available to all beamlines, to submit a
 * scan to be run with different polarisations.<br>
 * It adds a check box which, if checked, serialises the ScanRequest, puts it in the Jython namespace and calls the
 * script defined in the variable scriptFilePath. A suitable script must be written for the beamline.<br>
 * If the check box is not checked, the scan will be submitted in the normal way.
 */
public class PolarisationSubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(PolarisationSubmitScanSection.class);

	private static final int NUM_COLUMNS = 4;

	/**
	 * Check box to control whether a polarisation scan or "normal" scan will be submitted.
	 */
	private Button polarisationCheckbox;

	/**
	 * Script to run when the {@code Submit} button is pressed, if {@link polarisationCheckbox} is checked.
	 * <p>
	 * This is configurable in Spring if necessary, but should generally not be changed.<br>
	 */
	private String scriptFilePath = "polarisation/submit_polarisation_scan.py";

	@Override
	protected void createSubmitSection() {
		final Composite submitComposite = new Composite(getComposite(), SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(submitComposite);

		createSubmitButton(submitComposite);

		// Check box for polarisation or "normal" scan
		polarisationCheckbox = new Button(submitComposite, SWT.CHECK);
		GridDataFactory.swtDefaults().applyTo(polarisationCheckbox);
		polarisationCheckbox.setText("Polarisation scan");

		createStopButton(submitComposite);
	}

	@Override
	protected void submitScan() {
		if (!polarisationCheckbox.getSelection()) {
			// Ordinary mapping scan
			super.submitScan();
			return;
		}

		final IScriptService scriptService = getService(IScriptService.class);
		final ScanRequest scanRequest = getScanRequest(getBean());

		try {
			// Serialise ScanRequest to JSON and put in the Jython namespace.
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
			return;
		}

		Async.execute(() -> runScript(scriptFilePath, "Polarisation scanning script"));
	}

	public void setScriptFilePath(String scriptFilePath) {
		this.scriptFilePath = scriptFilePath;
	}
}
