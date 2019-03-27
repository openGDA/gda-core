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

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.scanning.api.script.ScriptLanguage;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters;
import uk.ac.diamond.daq.mapping.ui.experiment.SubmitScanSection;

/**
 * Submit a XANES scan
 * <p>
 * This combines the standard {@link ScanRequest} with the specific parameters from the
 * {@link XanesEdgeParametersSection} and calls the script <code>{beamline config}/scanning/submit_xanes_scan.py</code>
 * <p>
 * The parameters are passed in JSON format to avoid serialisation problems.
 */
public class XanesSubmitScanSection extends SubmitScanSection {
	private static final Logger logger = LoggerFactory.getLogger(XanesSubmitScanSection.class);

	private static final String SCRIPT_FILE = "scanning/submit_xanes_scan.py";

	@Override
	public void createControls(Composite parent) {
		setButtonColour(new RGB(179, 204, 255));
		super.createControls(parent);
	}

	@Override
	protected void submitScan() {
		final IScriptService scriptService = getService(IScriptService.class);
		final ScanRequest<IROI> scanRequest = getScanRequest(getMappingBean());
		final XanesEdgeParametersSection paramsSection = getMappingView().getSection(XanesEdgeParametersSection.class);
		final XanesEdgeParameters xanesEdgeParameters = paramsSection.getScanParameters();
		final ScriptRequest scriptRequest = new ScriptRequest(SCRIPT_FILE, ScriptLanguage.SPEC_PASTICHE);

		try {
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			scriptService.setNamedValue(IScriptService.VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));
			scriptService.setNamedValue(IScriptService.VAR_NAME_XANES_EDGE_PARAMS_JSON, marshallerService.marshal(xanesEdgeParameters));
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
			return;
		}

		Async.execute(() -> {
			try {
				scriptService.execute(scriptRequest);
			} catch (Exception e) {
				logger.error("Error running XANES scanning script", e);
			}
		});
	}

	@Override
	protected void onShow() {
		setParametersVisibility(true);
	}

	@Override
	protected void onHide() {
		setParametersVisibility(false);
	}

	/*
	 * Show or hide the corresponding parameters section
	 */
	private void setParametersVisibility(boolean visible) {
		final XanesEdgeParametersSection xanesParams = getMappingView().getSection(XanesEdgeParametersSection.class);

		if (xanesParams == null) {
			logger.error("No XANES parameters section found");
		} else {
			xanesParams.setVisible(visible);
			relayoutMappingView();
		}
	}
}
