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

package uk.ac.diamond.daq.mapping.ui.exafs;

import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_SCAN_REQUEST_JSON;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.script.IScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;

public class ExafsSubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(ExafsSubmitScanSection.class);

	private ExafsParametersSection exafsParametersSection;
	private ExafsTimeParametersSection exafsTimeParametersSection;

	@Override
	protected void submitScan() {
		final ScanRequest scanRequest = getScanRequest(getBean());
		runScript(scanRequest);
	}

	private void runScript(ScanRequest scanRequest) {
		final IScriptService scriptService = getService(IScriptService.class);

		try {
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			logger.info("Setting Jython variables");
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));

			// set exafs section parameters
			scriptService.setNamedValue("edgeElement", exafsParametersSection.getEdgeElement());
			scriptService.setNamedValue("edgeEnergy", exafsParametersSection.getEdgeEnergy());
			scriptService.setNamedValue("edgeStep", exafsParametersSection.getEdgeStep());
			scriptService.setNamedValue("rowPercentage", exafsParametersSection.getPercentage());
			scriptService.setNamedValue("kMin", exafsParametersSection.getkMin());
			scriptService.setNamedValue("kMax", exafsParametersSection.getkMax());
			scriptService.setNamedValue("kStep", exafsParametersSection.getkStep());
			scriptService.setNamedValue("startTime", exafsTimeParametersSection.getStartTime());
			scriptService.setNamedValue("endTime", exafsTimeParametersSection.getEndTime());
			scriptService.setNamedValue("kWeight", exafsTimeParametersSection.getkWeight());

			logger.info("Succesfully set Jython variables");
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
			return;
		}
		logger.info("Running script");
		Async.execute(() -> runScript(getScriptFilePath(), "EXAFS scanning script"));

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
		if (exafsParametersSection != null && exafsTimeParametersSection != null) {
			exafsParametersSection.setVisible(visible);
			exafsTimeParametersSection.setVisible(visible);

			relayoutView();

			selectDetector(getDetectorName(), visible);
		}
	}

	public void setExafsParametersSection(ExafsParametersSection exafsParametersSection) {
		this.exafsParametersSection = exafsParametersSection;
	}

	public void setExafsTimeParametersSection(ExafsTimeParametersSection exafsTimeParametersSection) {
		this.exafsTimeParametersSection = exafsTimeParametersSection;
	}
}
