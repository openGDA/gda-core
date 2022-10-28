/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.ptychography;

import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_PTYCHO_PARAMS_JSON;
import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_SCAN_REQUEST_JSON;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.IMapPathModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisPtychographyModel;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.ConfigWrapper;
import uk.ac.diamond.daq.mapping.api.PtychographyParams;
import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;
import uk.ac.diamond.daq.mapping.ui.experiment.RegionAndPathController;
import uk.ac.gda.ui.tool.ClientMessages;

/**
 * Mapping view section to define a ptychography scan and submit the resulting {@link ScanRequest} to a script.<br>
 * It also contains radio buttons to select a low- or high-resolution scan.
 * <p>
 * When the section is shown, it will ensure that the Raster scan path is selected.<br>
 * See comments on individual fields below for an indication of how the view can be configured.
 */
public class PtychographySubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(PtychographySubmitScanSection.class);

	private static final int NUM_COLUMNS = 5;

	// Step sizes (in mm) to set in the raster step model
	private static final double DEFAULT_STEP_SIZE = 2.0e-4;
	private double xStepSize = DEFAULT_STEP_SIZE;
	private double yStepSize = DEFAULT_STEP_SIZE;

	// Name of detector to use for ptychography
	private String detectorName;

	/**
	 * Set to {@code false} to set low resolution when the view is opened or {@code true} to set high resolution
	 */
	private boolean defaultToHighResolution = false;

	/**
	 * Script to run when the {@code Submit} button is pressed.
	 * <p>
	 * This is configurable in Spring, but should generally not be changed.<br>
	 * Rather, the beamline should implement the function {@code run_ptychography_scan_request()}, to which the
	 * {@link ScanRequest} is passed in JSON format.
	 */
	private String scriptFilePath = "scanning/submit_ptychography_scan.py";

	private Button lowResButton;
	private Button highResButton;

	/**
	 * Post processing configuration
	 */
	private ConfigWrapper processingConfiguration;
	private static final String YAML_VISIT_PATH_KEY = "visit_path";

	@Override
	public void createControls(Composite parent) {
		setButtonColour(new RGB(140, 217, 179));
		super.createControls(parent);
	}

	@Override
	protected void createSubmitSection() {
		final Composite submitComposite = new Composite(getComposite(), SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(submitComposite);

		createSubmitButton(submitComposite);
		createStopButton(submitComposite);

		final Label resolutionLabel = new Label(submitComposite, SWT.NONE);
		resolutionLabel.setText(getMessage(ClientMessages.RESOLUTION));
		GridDataFactory.swtDefaults().applyTo(resolutionLabel);

		lowResButton = createResolutionRadioButton(submitComposite, ClientMessages.LOW);
		highResButton = createResolutionRadioButton(submitComposite, ClientMessages.HIGH);
	}

	private Button createResolutionRadioButton(Composite composite, ClientMessages text) {
		final Button button = new Button(composite, SWT.RADIO);
		GridDataFactory.swtDefaults().applyTo(button);
		button.setText(getMessage(text));
		return button;
	}

	@Override
	protected void onShow() {
		// Ensure the scan path is set to Raster
		final TwoAxisGridStepModel stepModel = getRasterStepModel();
		if (stepModel == null) {
			logger.error("No Raster scanning model is defined");
			return;
		}

		// Set x & y step size
		// Ideally, the corresponding controls should be made read-only, but there is currently no way to do this.
		stepModel.setxAxisStep(xStepSize);
		stepModel.setyAxisStep(yStepSize);

		// Select XRF/Imaging detector: deselect other detectors
		// Ideally, all other detectors should be hidden, but there is currently no way to do this.
		getBean().getDetectorParameters().stream()
			.forEach(d -> d.setIncludeInScan(d.getName().equals(detectorName)));

		// Redraw mapping section
		getView().updateControls();

		// Set initial resolution
		if (defaultToHighResolution) {
			lowResButton.setSelection(false);
			highResButton.setSelection(true);
		} else {
			lowResButton.setSelection(true);
			highResButton.setSelection(false);
		}

		if (processingConfiguration != null) {
			updateProcessingFileConfiguration();
			addProcessingRequest();
		}
	}

	@Override
	protected void onHide() {
		if (processingConfiguration != null) {
			removeProcessingRequest();
		}
	}

	private void addProcessingRequest() {
		getBean().addProcessingRequest(processingConfiguration);
		getView().updateControls();
	}

	private void removeProcessingRequest() {
		getBean().getProcessingConfigs().removeAll(List.of(processingConfiguration));
		getView().updateControls();
	}

	/**
	 * Updates the visit data directory path in the processing file configuration
	 */
	private void updateProcessingFileConfiguration() {
		Yaml yaml = new Yaml();
		// current visit directory
		String visitDirectory = InterfaceProvider.getPathConstructor().getVisitDirectory();
		try(InputStream inputStream = new FileInputStream(new File(processingConfiguration.getPathToConfig()))){
			Map<String, Object> data = yaml.load(inputStream);
			// replace the visit path value with the current visit path
			data.put(YAML_VISIT_PATH_KEY, visitDirectory);
			PrintWriter writer = new PrintWriter(new File(processingConfiguration.getPathToConfig()));
			yaml.dump(data, writer);
		} catch (Exception e) {
			logger.error("Could not update processing configuration file", e);
		}
	}

	/**
	 * Get the {@link TwoAxisGridStepModel} that is configured for this client (for raster scanning).
	 * <p>
	 * This may already be set in the mapping bean: if not, we need to obtain it from the controller and redraw the GUI
	 * accordingly.
	 *
	 * @return the model, or {@code null} in the (unlikely) event that none is configured in the client.
	 */
	private TwoAxisGridStepModel getRasterStepModel() {
		final IMapPathModel scanPath = getBean().getScanDefinition().getMappingScanRegion().getScanPath();
		if (scanPath instanceof TwoAxisPtychographyModel) {
			return (TwoAxisGridStepModel) scanPath;
		}

		final RegionAndPathController controller = getService(RegionAndPathController.class);
		final TwoAxisGridStepModel model = (TwoAxisGridStepModel) controller.getScanPathListAndLinkPath().stream()
				.filter(TwoAxisGridStepModel.class::isInstance)
				.findFirst()
				.orElse(null);

		if (model != null) {
			controller.changePath(model);
		}
		return model;
	}

	@Override
	protected void submitScan() {
		final IScriptService scriptService = getService(IScriptService.class);
		final ScanRequest scanRequest = getScanRequest(getBean());
		try {
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));

			final PtychographyParams ptychographyParams = new PtychographyParams();
			final PtychographyParams.Resolution resolution = lowResButton.getSelection()
					? PtychographyParams.Resolution.LOW
					: PtychographyParams.Resolution.HIGH;
			ptychographyParams.setResolution(resolution);
			scriptService.setNamedValue(VAR_NAME_PTYCHO_PARAMS_JSON, marshallerService.marshal(ptychographyParams));
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
			return;
		}

		Async.execute(() -> runScript(scriptFilePath, "Ptychography scanning script"));
	}

	public void setDefaultToHighResolution(boolean defaultToHighResolution) {
		this.defaultToHighResolution = defaultToHighResolution;
	}

	public void setScriptFilePath(String scriptFilePath) {
		this.scriptFilePath = scriptFilePath;
	}

	public void setxStepSize(double xStepSize) {
		this.xStepSize = xStepSize;
	}

	public void setyStepSize(double yStepSize) {
		this.yStepSize = yStepSize;
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	public void setProcessingConfiguration(ConfigWrapper processingConfiguration) {
		this.processingConfiguration = processingConfiguration;
	}
}
