/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.gda.ui.tool.ClientMessages.CONFIGURE;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.util.Map;

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
import uk.ac.diamond.daq.mapping.api.TomographyCalibrationData;
import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;
import uk.ac.diamond.daq.mapping.ui.experiment.StatusPanel;

public class TomographySubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(TomographySubmitScanSection.class);

	private static final int NUM_COLUMNS = 5;

	private final String fileDirectory;

	private TomographyConfigurationDialog dialog;

	public TomographySubmitScanSection(String fileDirectory) {
		this.fileDirectory = fileDirectory;
	}

	@Override
	protected void createSubmitSection() {
		final Composite submitComposite = new Composite(getComposite(), SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(submitComposite);

		createSubmitButton(submitComposite);

		dialog = new TomographyConfigurationDialog(getShell(), fileDirectory);
		var centreDialog = new TomographyCentreRotationDialog(getShell());

		// Button to show configuration dialogue
		final Button configButton = new Button(submitComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(configButton);
		configButton.setText(getMessage(CONFIGURE));
		configButton.addSelectionListener(widgetSelectedAdapter(e -> dialog.open()));

		final Button centreButton = new Button(submitComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(configButton);
		centreButton.setText("Centre of rotation");
		centreButton.addSelectionListener(widgetSelectedAdapter(e -> centreDialog.open()));

		createStopButton(submitComposite);
	}

	@Override
	protected void submitScan() {
		final IScriptService scriptService = getService(IScriptService.class);
		final IMarshallerService marshallerService = getService(IMarshallerService.class);

		final ScanRequest scanRequest = getScanRequest(getBean());

		final Map<String, TomographyCalibrationData> calibrationData = dialog.getTomographyCalibrationData();
		if (calibrationData.isEmpty()) {
			logAndDisplayError("Calibration data not available. Please configure.");
			return;
		}

		final TomographyAngleSection section = getParametersSection();

		final StatusPanel statusPanel = getStatusPanel();
		final double scanEstimatedTime = statusPanel.getTotalEstimatedTime();
		final int numProjections = section.getNumProjections();
		final double totalEstimatedTime = scanEstimatedTime * numProjections;

		try {
			// set scan request
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));

			// set angle information from angle section
			scriptService.setNamedValue("startAngle", section.getStartAngle());
			scriptService.setNamedValue("stopAngle", section.getStopAngle());
			scriptService.setNamedValue("stepAngle", section.getStepAngle());
			scriptService.setNamedValue("angleMeasured", section.getAngleMeasured());
			scriptService.setNamedValue("zCentre", section.getZValue());
			scriptService.setNamedValue("scanEstimatedTime", scanEstimatedTime);
			scriptService.setNamedValue("totalEstimatedTime", totalEstimatedTime);

			// set calibration data from dialog
			scriptService.setNamedValue("includeY", dialog.isIncludeY());
			for (Map.Entry<String, TomographyCalibrationData> entry : calibrationData.entrySet()) {
				scriptService.setNamedValue(entry.getKey(), marshallerService.marshal(entry.getValue()));
			}
		} catch (NumberFormatException e) {
			logAndDisplayError("Angle information is not in a valid numeric format.", e);
			return;
		} catch (Exception e) {
			logAndDisplayError("The scan could not be submitted. See the error log for more details.", e);
			return;
		}
		Async.execute(() -> runScript(getScriptFilePath(), getDescription()));
	}

	private TomographyAngleSection getParametersSection() {
		return getView().getSection(TomographyAngleSection.class);
	}

	private StatusPanel getStatusPanel() {
		return getView().getSection(StatusPanel.class);
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
		parametersSection.setVisible(onShow);
		relayoutView();
	}

	private void logAndDisplayError(String errorMessage) {
		logAndDisplayError(errorMessage, null);
	}

	private void logAndDisplayError(String errorMessage, Exception e) {
		logger.error("Scan submission failed", e);
		MessageDialog.openError(getShell(), "Error Submitting Scan", errorMessage);
	}
}