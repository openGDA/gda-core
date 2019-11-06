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

package uk.ac.diamond.daq.mapping.tomography.ui;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.mapping.tomography.ui.TomographyUtils.CALIBRATION_FILE_PATH;
import static uk.ac.diamond.daq.mapping.tomography.ui.TomographyUtils.getClientMessage;
import static uk.ac.diamond.daq.mapping.tomography.ui.TomographyUtils.getProcessingFilesAs;
import static uk.ac.diamond.daq.mapping.tomography.ui.TomographyUtils.populateScriptService;
import static uk.ac.gda.ui.tool.ClientMessages.CONFIGURE;
import static uk.ac.gda.ui.tool.ClientMessages.ERROR;
import static uk.ac.gda.ui.tool.ClientMessages.TOMO_CALIBRATE_SUBMIT_ERROR;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.io.BufferedReader;
import java.nio.file.Files;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.TomographyCalibrationData;
import uk.ac.diamond.daq.mapping.api.TomographyParams;
import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;

public class TomographySubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(TomographySubmitScanSection.class);

	private static final int NUM_COLUMNS = 4;

	// Names of the motors whose values are to be used in configuration
	private final String xMotor;
	private final String yMotor;
	private final String zMotor;
	private final String rotationMotor;

	// Path to script for a full scan
	private final String tomoScanScript;

	// Path to script for a dry run (e.g. without data collection)
	private final String tomoDryRunScript;

	public TomographySubmitScanSection(String xMotor, String yMotor, String zMotor, String rotationMotor, String tomoScanScript, String tomoDryRunScript) {
		this.xMotor = xMotor;
		this.yMotor = yMotor;
		this.zMotor = zMotor;
		this.rotationMotor = rotationMotor;
		this.tomoScanScript = tomoScanScript;
		this.tomoDryRunScript = tomoDryRunScript;
	}

	@Override
	public void createControls(Composite parent) {
		setButtonColour(new RGB(255, 204, 128));
		super.createControls(parent);
	}

	@Override
	protected void createSubmitSection() {
		final Composite submitComposite = new Composite(getComposite(), SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(submitComposite);

		createSubmitButton(submitComposite);

		// Button to show configuration dialogue
		final Button configButton = new Button(submitComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(configButton);
		configButton.setText(getMessage(CONFIGURE));
		configButton.addSelectionListener(widgetSelectedAdapter(e -> showConfigurationDialog()));

		createStopButton(submitComposite);
	}

	@Override
	protected void submitScan() {
		// Read parameters from file
		try (BufferedReader reader = Files.newBufferedReader(CALIBRATION_FILE_PATH)) {
			final IScriptService scriptService = getService(IScriptService.class);
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			final IMappingExperimentBean mappingBean = getMappingBean();
			final TomographyCalibrationData calibrationParams = marshallerService.unmarshal(reader.readLine(), TomographyCalibrationData.class);
			final ScanRequest scanRequest = getScanRequest(mappingBean);

			final TomographyParams tomoParams = new TomographyParams();
			tomoParams.setTomographyCalibration(calibrationParams);
			tomoParams.setProcessingFiles(getProcessingFilesAs(mappingBean));
			tomoParams.setVisitId(InterfaceProvider.getBatonStateProvider().getBatonHolder().getVisitID());

			populateScriptService(scriptService, marshallerService, scanRequest, tomoParams);
		} catch (Exception e) {
			handleException(getClientMessage(TOMO_CALIBRATE_SUBMIT_ERROR), e);
			return;
		}

		Async.execute(() -> runScript(tomoScanScript, "tomography scanning script"));
	}

	private void handleException(String errorMessage, Exception e) {
		final IStatus status = new Status(IStatus.ERROR, "uk.ac.diamond.daq.mapping.xanes.ui", errorMessage, e);
		ErrorDialog.openError(getShell(), getClientMessage(ERROR), errorMessage, status);
		logger.error(errorMessage, e);
	}

	private void showConfigurationDialog() {
		final TomographyConfigurationDialog dialog = new TomographyConfigurationDialog(getShell(), rotationMotor,
				xMotor, yMotor, zMotor, getMappingView(), tomoDryRunScript);
		dialog.open();
	}
}
