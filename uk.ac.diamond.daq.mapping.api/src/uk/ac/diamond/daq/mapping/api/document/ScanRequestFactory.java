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

package uk.ac.diamond.daq.mapping.api.document;

import static org.eclipse.scanning.api.points.models.AxialStepModel.createStaticAxialModel;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel;
import org.eclipse.scanning.api.scan.ScanningException;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionConfigurationBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionParametersBase;
import uk.ac.diamond.daq.mapping.api.document.handlers.processing.ProcessingRequestHandlerService;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.AcquisitionConfigurationReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.AcquisitionEngineReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.AcquisitionParametersReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.AcquisitionReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.DarkCalibrationReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.FlatCalibrationReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.ImageCalibrationReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.ScanpathDocumentReader;
import uk.ac.diamond.daq.mapping.api.document.model.AcquisitionTemplateFactory;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.exception.GDAException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * A helper class to generate a {@link ScanRequest} from a {@link ScanRequestDocument}.
 *
 * @author Maurizio Nagni
 */
public class ScanRequestFactory {

	private final AcquisitionReader acquisitionReader;

	public ScanRequestFactory(
			AcquisitionBase<? extends AcquisitionConfigurationBase<? extends AcquisitionParametersBase>> acquisition) {
		super();
		this.acquisitionReader = new AcquisitionReader(() -> acquisition);
	}

	public ScanRequest createScanRequest(IRunnableDeviceService runnableDeviceService) throws ScanningException {
		// Populate the {@link ScanRequest} with the assembled objects
		ScanRequest scanRequest = new ScanRequest();

		Optional.ofNullable(getAcquisition().getAcquisitionLocation())
			.map(URL::getPath)
			.ifPresent(scanRequest::setFilePath);

		try {
			scanRequest.setCompoundModel(createCompoundModel());
		} catch (GDAException e) {
			throw new ScanningException("Cannot create compound model", e);
		}

		parseAcquisitionEngine(scanRequest, runnableDeviceService);
		addStartPosition(scanRequest);

		scanRequest.setMonitorNamesPerPoint(parseMonitorNamesPerPoint());
		scanRequest.setTemplateFilePaths(parseTemplateFilePaths());
		scanRequest.setProcessingRequest(parseProcessingRequest());
		return scanRequest;
	}

	private void addStartPosition(ScanRequest scanRequest) {
		final IPosition iPosition = createPositionMap(getAcquisitionParameters().getPosition());
		Optional.ofNullable(iPosition)
			.ifPresent(scanRequest::setStartPosition);
	}

	private CompoundModel createCompoundModel() throws GDAException {
		AcquisitionTemplate acquisitionTemplate = AcquisitionTemplateFactory
				.buildModelDocument(getScanpathDocument().getData());

		// The second condition, ONE_DIMENSION_LINE is not strictly necessary but the breakpoints for malcom is such a
		// custom implementation that at the moment is preferred to be considered as an exception more than a possible case.
		if (requireDarkOrFlat()
				&& AcquisitionTemplateType.ONE_DIMENSION_LINE.equals(getScanpathDocument().getModelDocument())) {
			return createInterpolatedCompoundModel(acquisitionTemplate);
		}
		return createCompoundModel(acquisitionTemplate);
	}

	private boolean requireDarkOrFlat() {
		return Optional.ofNullable(getDarkCalibration().getNumberExposures() > 0)
					.orElseGet(() -> false)
				||  Optional.ofNullable(getFlatCalibration().getNumberExposures() > 0)
					.orElseGet(() -> false);
	}

	private CompoundModel createCompoundModel(AcquisitionTemplate modelDocument) {
		CompoundModel compoundModel = new CompoundModel();
		compoundModel.setModels(new ArrayList<>());
		compoundModel.setRegions(new ArrayList<>());
		compoundModel.setMutators(new ArrayList<>());

		compoundModel.setData(modelDocument.getIScanPointGeneratorModel(), modelDocument.getROI());
		return compoundModel;
	}

	/**
	 * Implementation based on {@code org.eclipse.scanning.test.scan.nexus.MalcolmMultiScanTest}
	 *
	 * @param acquisitionTemplate
	 * @return
	 */
	private CompoundModel createInterpolatedCompoundModel(AcquisitionTemplate acquisitionTemplate) {
		final InterpolatedMultiScanModel multiScanModel = new InterpolatedMultiScanModel();
		multiScanModel.setContinuous(Optional.ofNullable(getScanpathDocument().getMutators().containsKey(Mutator.CONTINUOUS))
			.orElse(false));

		// --- Preparation ---
		// --- Flat Positions ---
		// Note - The shutter "close" position is added by the ScanningAcquisitionController
		final IPosition flatPos = createPositionMap(getFlatCalibration().getPosition());

		// --- Dark Positions ---
		// Note - The shutter "close" position is added by the ScanningAcquisitionController
		final IPosition darkPos = createPositionMap(getDarkCalibration().getPosition());

		// --- Steps estimation
		ScannableTrackDocument trackDocument =  getScanpathDocument().getScannableTrackDocuments().get(0);
		 // darks and flats should be (step / 2) before the start of the main scan, and the same after
		final double posBeforeMainScan = trackDocument.getStart() - trackDocument.calculatedStep() / 2;
		final double posAfterMainScan = trackDocument.getStop() + trackDocument.calculatedStep() / 2;

		// --- Positions List ---
		final List<IPosition> interpolationPositions = new ArrayList<>();
		multiScanModel.setInterpolationPositions(interpolationPositions);



		// -- Model Definition
		// Flat Before Acquisition
		if (getFlatCalibration().isBeforeAcquisition() && getFlatCalibration().getNumberExposures() > 0) {
			multiScanModel.addModel(createStaticAxialModel(trackDocument.getScannable(),
					posBeforeMainScan, getFlatCalibration().getNumberExposures()));
			interpolationPositions.add(flatPos);
		}
		// Dark Before Acquisition
		if (getDarkCalibration().isBeforeAcquisition() && getDarkCalibration().getNumberExposures() > 0) {
			multiScanModel.addModel(createStaticAxialModel(trackDocument.getScannable(),
					posBeforeMainScan, getDarkCalibration().getNumberExposures()));
			interpolationPositions.add(darkPos);
		}

		// Acquisition
		final IPosition startPos = createPositionMap(getAcquisitionParameters().getPosition());
		multiScanModel.addModel(acquisitionTemplate.getIScanPointGeneratorModel());
		interpolationPositions.add(startPos);

		// Flat After Acquisition
		if (getFlatCalibration().isAfterAcquisition() && getFlatCalibration().getNumberExposures() > 0) {
			multiScanModel.addModel(createStaticAxialModel(trackDocument.getScannable(),
					posAfterMainScan, getFlatCalibration().getNumberExposures()));
			interpolationPositions.add(flatPos);
		}
		// Dark After Acquisition
		if (getDarkCalibration().isAfterAcquisition() && getDarkCalibration().getNumberExposures() > 0) {
			multiScanModel.addModel(createStaticAxialModel(trackDocument.getScannable(),
					posAfterMainScan, getDarkCalibration().getNumberExposures()));
			interpolationPositions.add(darkPos);
		}
		return new CompoundModel(multiScanModel);
	}

	private IPosition createPositionMap(Set<DevicePositionDocument> devicePositions) {
		final Map<String, Object> positionMap = new HashMap<>();
		devicePositions.forEach(p -> {
			if (DevicePositionDocument.ValueType.LABELLED.equals(p.getValueType())) {
				positionMap.put(p.getDevice(), p.getLabelledPosition());
			} else if (DevicePositionDocument.ValueType.NUMERIC.equals(p.getValueType())) {
				positionMap.put(p.getDevice(), p.getPosition());
			}
		});
		return new MapPosition(positionMap);
	}

	private void parseAcquisitionEngine(ScanRequest scanRequest, IRunnableDeviceService runnableDeviceService)
			throws ScanningException {
		switch (getAcquisitionEngine().getType()) {
			case MALCOLM:
				prepareMalcolmAcquisitionEngine(scanRequest, runnableDeviceService);
				break;
			case SCRIPT:
				throw new ScanningException("Script Acquisition engine not supported");
			case SERVICE:
				throw new ScanningException("Service Acquisition engine not supported");
			default:
		}
	}

	private void prepareMalcolmAcquisitionEngine(ScanRequest scanRequest, IRunnableDeviceService runnableDeviceService)
			throws ScanningException {
		final Map<String, IDetectorModel> ret = new HashMap<>();
		scanRequest.setDetectors(ret);

		IRunnableDevice<IDetectorModel> detector = runnableDeviceService.getRunnableDevice(getAcquisitionEngine().getId());
		final IDetectorModel model = detector.getModel();

		if (model == null) {
			throw new ScanningException(String.format("Could not get model for detector %s",
					detector.getName()));
		}
		ret.put(detector.getName(), model);
	}

	private Collection<String> parseMonitorNamesPerPoint() {
		return new ArrayList<>();
	}

	private Set<String> parseTemplateFilePaths() {
		return new HashSet<>();
	}

	private ProcessingRequest parseProcessingRequest() {
		Map<String, Collection<Object>> requests = new HashMap<>();
		getAcquisitionConfiguration().getProcessingRequest()
			.forEach(p -> requests.put(p.getKey(), getProcessingRequestHandlerService().translateToCollection(p)));
		ProcessingRequest pr = new ProcessingRequest();
		pr.setRequest(requests);
		return pr;
	}

	private ProcessingRequestHandlerService getProcessingRequestHandlerService() {
		return SpringApplicationContextFacade.getBean(ProcessingRequestHandlerService.class);
	}

	private AcquisitionReader getAcquisition() {
		return acquisitionReader;
	}

	private AcquisitionEngineReader getAcquisitionEngine() {
		return getAcquisition().getAcquisitionEngine();
	}

	private AcquisitionConfigurationReader getAcquisitionConfiguration() {
		return getAcquisition().getAcquisitionConfiguration();
	}

	private AcquisitionParametersReader getAcquisitionParameters() {
		return getAcquisitionConfiguration().getAcquisitionParameters();
	}

	private ScanpathDocumentReader getScanpathDocument() {
		return getAcquisitionParameters().getScanpathDocument();
	}

	private ImageCalibrationReader getImageCalibration() {
		return getAcquisitionConfiguration().getImageCalibration();
	}

	private DarkCalibrationReader getDarkCalibration() {
		return getImageCalibration().getDarkCalibration();
	}

	private FlatCalibrationReader getFlatCalibration() {
		return getImageCalibration().getFlatCalibration();
	}
}
