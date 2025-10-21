/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.ui.Colour;
import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;

/**
 * Submit section that includes two buttons to submit scans.
 * The data driven scan is a two part scan where an initial XANES scan is submitted.
 * After the post processing for the first scan is completed, we get a list of energy points
 * and y positions. The second button in this section will trigger the script that runs
 * scans with the generated energy and y positions, read from an external JSON file.
 */
public class DataDrivenSubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(DataDrivenSubmitScanSection.class);

	private static final int NUM_COLUMNS = 3;

	private String secondScriptFilePath;

	protected XanesParametersSection xanesParametersSection;

	@Override
	protected void createSubmitSection() {
		final Composite submitComposite = new Composite(getComposite(), SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(submitComposite);

		// first button
		createSubmitButton(submitComposite);
		// second button
		createDataDrivenSubmitScanButton(submitComposite);
		createStopButton(submitComposite);
	}

	protected void createDataDrivenSubmitScanButton(Composite parent) {
		var button = new Button(parent, SWT.PUSH);
		button.setText("Submit data driven scan");
		button.setBackground(new Color(Display.getDefault(), Colour.LIGHT_YELLOW.getRGB()));
		GridDataFactory.swtDefaults().applyTo(button);
		button.addSelectionListener(widgetSelectedAdapter(e -> submitDataDrivenScan()));
	}

	@Override
	protected void submitScan() {
		final IScriptService scriptService = getService(IScriptService.class);
		final ScanRequest scanRequest = getScanRequest(getBean());

		try {
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));
			scriptService.setNamedValue(VAR_NAME_CUSTOM_PARAMS, marshallerService.marshal(xanesParametersSection.getScanParameters()));
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
			return;
		}

		Async.execute(() -> runScript(scriptFilePath, getDescription()));
	}

	private void submitDataDrivenScan() {
		// unselect the outer scannable as the energies points will be defined in the json file
		selectOuterScannable(getOuterScannableName(), false);

		final IScriptService scriptService = getService(IScriptService.class);
		final ScanRequest scanRequest = getScanRequest(getBean());

		try {
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
			return;
		}

		Async.execute(() -> runScript(secondScriptFilePath, getDescription()));
	}

	@Override
	protected void onShow() {
		setParametersVisibility(true);
	}

	@Override
	protected void onHide() {
		setParametersVisibility(false);
	}

	protected void setParametersVisibility(boolean visible) {
		if (xanesParametersSection != null) {
			xanesParametersSection.setVisible(visible);

			relayoutView();

			selectOuterScannable(getOuterScannableName(), visible);
			selectDetector(getDetectorName(), visible);
		}
	}

	/**
	 * Sets the XanesParametersSection that needs to be shown or hidden in the mapping view
	 * @param xanesParametersSection
	 */
	public void setXanesParametersSection(XanesParametersSection xanesParametersSection) {
		this.xanesParametersSection = xanesParametersSection;
	}

	public void setSecondScriptFilePath(String secondScriptFilePath) {
		this.secondScriptFilePath = secondScriptFilePath;
	}
}
