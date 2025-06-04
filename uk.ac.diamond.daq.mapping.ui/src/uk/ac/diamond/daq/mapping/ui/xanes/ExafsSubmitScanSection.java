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

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.script.IScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters;

public class ExafsSubmitScanSection extends XanesSubmitScanSection {
	private static final Logger logger = LoggerFactory.getLogger(ExafsSubmitScanSection.class);

	private ExafsParametersSection exafsParametersSection;

	@Override
	protected void onShow() {
		setParametersVisibility(true);
	}

	@Override
	protected void onHide() {
		setParametersVisibility(false);
	}

	@Override
	protected void setParametersVisibility(boolean visible) {
		if (xanesParametersSection != null && exafsParametersSection != null) {
			xanesParametersSection.setVisible(visible);
			exafsParametersSection.setVisible(visible);

			relayoutView();

			selectOuterScannable(getOuterScannableName(), visible);
			selectDetector(getDetectorName(), visible);
		}
	}

	public void setExafsParametersSection(ExafsParametersSection exafsParametersSection) {
		this.exafsParametersSection = exafsParametersSection;
	}

	@Override
	protected void runScript(ScanRequest scanRequest, XanesEdgeParameters xanesEdgeParameters) {
		final IScriptService scriptService = getService(IScriptService.class);

		try {
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));
			scriptService.setNamedValue(VAR_NAME_CUSTOM_PARAMS, marshallerService.marshal(xanesEdgeParameters));

			// set exafs section parameters
			scriptService.setNamedValue("kMin", exafsParametersSection.getkMin());
			scriptService.setNamedValue("kMax", exafsParametersSection.getkMax());
			scriptService.setNamedValue("kStep", exafsParametersSection.getkStep());
			scriptService.setNamedValue("kWeight", exafsParametersSection.getkWeight());
			scriptService.setNamedValue("edgeStep", exafsParametersSection.getEdgeStep());
			scriptService.setNamedValue("startTime", exafsParametersSection.getStartTime());
			scriptService.setNamedValue("endTime", exafsParametersSection.getEndTime());
			scriptService.setNamedValue("restartK", exafsParametersSection.getRestartK());

		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
			return;
		}

		Async.execute(() -> runScript(getScriptFilePath(), "XANES scanning script"));

	}

}
