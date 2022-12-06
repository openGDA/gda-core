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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.IMapPathModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisPtychographyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.mapping.api.ConfigWrapper;
import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;
import uk.ac.diamond.daq.mapping.ui.experiment.RegionAndPathController;

/**
 * Mapping view section to define a ptychography scan and submit the resulting {@link ScanRequest} to a script.<br>
 * It also contains radio buttons to select a low- or high-resolution scan.
 * <p>
 * When the section is shown, it will ensure that the Raster scan path is selected.<br>
 * See comments on individual fields below for an indication of how the view can be configured.
 */
public class PtychographySubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(PtychographySubmitScanSection.class);

	/**
	 * Step sizes to set in the raster model
	 */
	private static final double DEFAULT_STEP_SIZE = 2.0e-4;
	private double xStepSize = DEFAULT_STEP_SIZE;
	private double yStepSize = DEFAULT_STEP_SIZE;

	/**
	 * Post processing configuration
	 */
	private ConfigWrapper processingConfiguration;
	private static final String YAML_VISIT_PATH_KEY = "visit_path";

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

		if (processingConfiguration != null) {
			updateProcessingFileConfiguration();
			getBean().addProcessingRequest(processingConfiguration);
		}

		getView().updateControls();
		selectDetector(getDetectorName(), true);
	}

	@Override
	protected void onHide() {
		if (processingConfiguration != null) {
			removeProcessingRequest();
		}
		selectDetector(getDetectorName(), false);
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

	public void setProcessingConfiguration(ConfigWrapper processingConfiguration) {
		this.processingConfiguration = processingConfiguration;
	}
}
