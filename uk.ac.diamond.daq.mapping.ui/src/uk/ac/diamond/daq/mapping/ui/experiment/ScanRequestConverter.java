/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.eclipse.scanning.api.script.ScriptLanguage;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IClusterProcessingModelWrapper;
import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegion;
import uk.ac.diamond.daq.mapping.api.ISampleMetadata;
import uk.ac.diamond.daq.mapping.api.IScanPathModelWrapper;
import uk.ac.diamond.daq.mapping.api.IScriptFiles;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.impl.SimpleSampleMetadata;

public class ScanRequestConverter {

	private static final Logger logger = LoggerFactory.getLogger(ScanRequestConverter.class);

	private MappingStageInfo mappingStageInfo;

	public void setMappingStageInfo(MappingStageInfo mappingStageInfo) {
		this.mappingStageInfo = mappingStageInfo;
	}

	/**
	 * Convert an IMappingExperimentBean to a ScanRequest.
	 * <p>
	 * This will include setting the mapping scan axes with the names from the mapping axis manager.
	 * <p>
	 * This method is made <code>public</code> to allow testing.
	 *
	 * @param mappingExperimentBean
	 *            the IMappingExperimentBean to be converted
	 * @return the ScanRequest
	 */
	public ScanRequest<IROI> convertToScanRequest(IMappingExperimentBean mappingExperimentBean) {
		ScanRequest<IROI> scanRequest = new ScanRequest<IROI>();

		final IMappingScanRegion scanRegion = mappingExperimentBean.getScanDefinition().getMappingScanRegion();
		final IScanPathModel scanPath = scanRegion.getScanPath();

		final String fastAxis;
		final String slowAxis;
		if (mappingStageInfo != null) {
			fastAxis = mappingStageInfo.getActiveFastScanAxis();
			slowAxis = mappingStageInfo.getActiveSlowScanAxis();
			// Change the axis on the scanPathModel
			if (scanPath instanceof IBoundingBoxModel) {
				IBoundingBoxModel boxModel = (IBoundingBoxModel) scanPath;
				boxModel.setFastAxisName(fastAxis);
				boxModel.setSlowAxisName(slowAxis);
			} else {
				final String message = "Could not set fast and slow axis!";
				logger.error(message);
				throw new RuntimeException(message);
			}

		} else {
			logger.warn("No mapping axis manager is set - the scan request will use default axis names!");

			if (scanPath instanceof IBoundingBoxModel) {
				IBoundingBoxModel boxModel = (IBoundingBoxModel) scanPath;
				fastAxis = boxModel.getFastAxisName();
				slowAxis = boxModel.getSlowAxisName();
			} else {
				final String message = "Could not determine fast and slow axis!";
				logger.error(message);
				throw new RuntimeException(message);
			}
		}

		// Build the list of models for the scan
		final List<IScanPathModel> models = new ArrayList<>();
		// If there are outer scannables to be included, add them to the list
		// TODO currently there is no support for outer scannables ROIs
		for (IScanPathModelWrapper scanPathModelWrapper : mappingExperimentBean.getScanDefinition()
				.getOuterScannables()) {
			if (scanPathModelWrapper.isIncludeInScan()) {
				final IScanPathModel model = scanPathModelWrapper.getModel();
				if (model == null) {
					logger.warn("Outer scannables contained a null model for: {}. It wont be included in the scan!",
							scanPathModelWrapper.getName());
				} else {
					models.add(model);
				}
			}
		}

		// Add the actual map path model last, it's the inner most model
		models.add(scanRegion.getScanPath());

		// Convert the list of models into a compound model
		final CompoundModel<IROI> compoundModel = new CompoundModel<>(models);

		// Add the ROI for the mapping region
		ScanRegion<IROI> region = new ScanRegion<IROI>(scanRegion.getRegion().toROI(), slowAxis, fastAxis);

		// Convert to a List of ScanRegion<IROI> containing one item to avoid unsafe varargs warning
		compoundModel.setRegions(Arrays.asList(region));

		// Set the model on the scan request
		scanRequest.setCompoundModel(compoundModel);

		// set the beamline start position
		Map<String, Object> beamlineConfiguration = mappingExperimentBean.getBeamlineConfiguration();
		if (beamlineConfiguration != null) {
			scanRequest.setStart(new MapPosition(beamlineConfiguration));
		}

		// add the required detectors to the scan
		for (IDetectorModelWrapper detectorWrapper : mappingExperimentBean.getDetectorParameters()) {
			if (detectorWrapper.isIncludeInScan()) {
				IDetectorModel detectorModel = detectorWrapper.getModel();
				scanRequest.putDetector(detectorModel.getName(), detectorModel);
			}
		}

		// add the required cluster processing steps
		if (mappingExperimentBean.getClusterProcessingConfiguration() != null) {
			for (IClusterProcessingModelWrapper processingWrapper : mappingExperimentBean.getClusterProcessingConfiguration()) {
				String name = processingWrapper.getName();
				if (scanRequest.getDetectors() != null && scanRequest.getDetectors().containsKey(name)) {
					throw new IllegalArgumentException(MessageFormat.format("A device or processing step with the name {0} is already included in the scan", name));
				}
				scanRequest.putDetector(processingWrapper.getName(), processingWrapper.getModel());
			}
		}

		// set the scripts to run before and after the scan if any
		if (mappingExperimentBean.getScriptFiles() != null) {
			IScriptFiles scriptFiles = mappingExperimentBean.getScriptFiles();
			scanRequest.setBefore(getScriptRequest(scriptFiles.getBeforeScanScript()));
			scanRequest.setAfter(getScriptRequest(scriptFiles.getAfterScanScript()));
		}

		// add the sample metadata
		if (mappingExperimentBean.getSampleMetadata() != null) {
			setSampleMetadata(mappingExperimentBean, scanRequest);
		}

		return scanRequest;
	}

	private void setSampleMetadata(IMappingExperimentBean mappingExperimentBean, ScanRequest<IROI> scanRequest) {
		final ISampleMetadata sampleMetadata = mappingExperimentBean.getSampleMetadata();
		String sampleName = sampleMetadata.getSampleName();
		if (sampleName == null || sampleName.trim().isEmpty()) {
			sampleName = "Unnamed Sample";
		}

		final ScanMetadata scanMetadata = new ScanMetadata(MetadataType.SAMPLE);
		scanMetadata.addField("name", sampleName);
		if (sampleMetadata instanceof SimpleSampleMetadata) {
			String description = ((SimpleSampleMetadata) sampleMetadata).getDescription();
			if (description == null || description.trim().isEmpty()) {
				description = "No description provided.";
			}
			scanMetadata.addField("description", description);
		}
		scanRequest.setScanMetadata(Arrays.asList(scanMetadata));
	}

	private ScriptRequest getScriptRequest(String scriptFile) {
		if (scriptFile == null || scriptFile.isEmpty()) {
			return null;
		}

		final ScriptRequest scriptRequest = new ScriptRequest();
		scriptRequest.setLanguage(ScriptLanguage.SPEC_PASTICHE);
		scriptRequest.setFile(scriptFile);
		return scriptRequest;
	}

}
