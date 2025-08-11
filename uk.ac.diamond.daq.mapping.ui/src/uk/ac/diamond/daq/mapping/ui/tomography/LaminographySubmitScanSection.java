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

package uk.ac.diamond.daq.mapping.ui.tomography;

import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_SCAN_REQUEST_JSON;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.script.IScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;

public class LaminographySubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(LaminographySubmitScanSection.class);

	@Override
	protected void submitScan() {
		final IScriptService scriptService = getService(IScriptService.class);
		final IMarshallerService marshallerService = getService(IMarshallerService.class);

		final ScanRequest scanRequest = getScanRequest(getBean());
		final LaminographySection section = getParametersSection();

		try {
			// set scan request
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));

			// set angle information from angle section
			scriptService.setNamedValue("startAngle", section.getStartAngle());
			scriptService.setNamedValue("stopAngle", section.getStopAngle());
			scriptService.setNamedValue("stepAngle", section.getStepAngle());
		} catch (NumberFormatException e) {
			logAndDisplayError("Angle information is not in a valid numeric format.", e);
			return;
		} catch (Exception e) {
			logAndDisplayError("The scan could not be submitted. See the error log for more details.", e);
			return;
		}
		Async.execute(() -> runScript(getScriptFilePath(), getDescription()));
	}

	private LaminographySection getParametersSection() {
		return getView().getSection(LaminographySection.class);
	}

	@Override
	protected void onShow() {
		updateView(true);
	}
	@Override
	protected void onHide() {
		updateView(false);
	}

	protected void updateView(boolean onShow) {
		var parametersSection = getParametersSection();
		if (parametersSection == null) {
			if (onShow) {
				logger.error("Tomography UI is not configured.");
				MessageDialog.openError(getShell(), "Error Displaying Tomography UI", "Tomography UI is not correctly configured.");
			}
			return;
		}
		parametersSection.clearInputs();
		parametersSection.setVisible(onShow);
		relayoutView();
	}
}
