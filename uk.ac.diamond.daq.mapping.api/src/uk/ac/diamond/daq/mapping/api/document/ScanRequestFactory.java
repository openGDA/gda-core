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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel.ImageType;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.scan.ScanningException;

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionConfigurationBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionParametersBase;
import uk.ac.diamond.daq.mapping.api.document.handlers.processing.ProcessingRequestHandlerService;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.AcquisitionConfigurationReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.AcquisitionEngineReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.AcquisitionParametersReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.AcquisitionReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.DarkCalibrationReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.DetectorDocumentReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.FlatCalibrationReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.ImageCalibrationReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.ScanpathDocumentReader;
import uk.ac.diamond.daq.mapping.api.document.model.AcquisitionTemplateFactory;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.common.exception.GDAException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * A helper class to generate a {@link ScanRequest} from a {@link ScanRequestDocument}.
 *
 * @author Maurizio Nagni
 */
public class ScanRequestFactory {

	private final AcquisitionReader acquisitionReader;

	public ScanRequestFactory(AcquisitionBase<? extends AcquisitionConfigurationBase<? extends AcquisitionParametersBase>> acquisition) {
		this.acquisitionReader = new AcquisitionReader(() -> acquisition);
	}

	public ScanRequest createScanRequest(IRunnableDeviceService runnableDeviceService) throws ScanningException {
		// Populate the {@link ScanRequest} with the assembled objects
		var scanRequest = new ScanRequest();
		scanRequest.setTemplateFilePaths(new HashSet<>());

		Optional.ofNullable(getAcquisition().getAcquisitionLocation())
			.map(URL::getPath)
			.ifPresent(scanRequest::setFilePath);

		try {
			scanRequest.setCompoundModel(createCompoundModel());
		} catch (GDAException e) {
			throw new ScanningException("Cannot create compound model", e);
		}

		parseAcquisitionEngine(scanRequest, runnableDeviceService);
		addPosition(createStartPosition(), scanRequest::setStartPosition);
		addPosition(createEndPosition(), scanRequest::setEnd);

		scanRequest.setMonitorNamesPerPoint(parseMonitorNamesPerPoint());

		scanRequest.setProcessingRequest(new ProcessingRequest());
		scanRequest.getProcessingRequest().setRequest(new HashMap<>());
		for (ProcessingRequestPair<?> request : getAcquisitionConfiguration().getProcessingRequest()) {
			getProcessingRequestHandlerService().handle(request, scanRequest);
		}

		return scanRequest;
	}

	private CompoundModel createCompoundModel() throws GDAException {
		var acquisitionTemplate = AcquisitionTemplateFactory
				.buildModelDocument(getScanpathDocument().getData());

		if (getFlatCalibration().getNumberExposures() + getDarkCalibration().getNumberExposures() > 0) {
			return createInterpolatedCompoundModel(acquisitionTemplate);
		}

		return createCompoundModel(acquisitionTemplate);
	}

	private CompoundModel createCompoundModel(AcquisitionTemplate modelDocument) {
		var generators = modelDocument.getIScanPointGeneratorModels();
		var compoundModel = new CompoundModel(generators);

		var roi = modelDocument.getROI();
		if (roi != null) {
			var innerGenerator = generators.get(generators.size() - 1);
			var scannables = innerGenerator.getScannableNames();

			var region = new ScanRegion(roi, scannables);
			compoundModel.setRegions(List.of(region));
		}

		return compoundModel;
	}

	/**
	 * Implementation based on {@code org.eclipse.scanning.test.scan.nexus.MalcolmMultiScanTest}
	 *
	 * @param acquisitionTemplate
	 * @return
	 */
	private CompoundModel createInterpolatedCompoundModel(AcquisitionTemplate acquisitionTemplate) {
		final var multiScanModel = new InterpolatedMultiScanModel();

		// --- Preparation ---
		// Each new multiscanModel must set this start position and image type
		// --- Positions List ---
		final List<IPosition> interpolationPositions = new ArrayList<>();
		// --- Images Type ---
		final List<ImageType> imageTypes = new ArrayList<>();

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

		// -- Model Definition
		// Flat Before Acquisition
		if (getFlatCalibration().isBeforeAcquisition() && getFlatCalibration().getNumberExposures() > 0) {
			var preScanFlats = getCalibrationModel(trackDocument.getScannable(),
					posBeforeMainScan, getFlatCalibration().getNumberExposures());
			multiScanModel.addModel(preScanFlats);
			addPosition(flatPos, interpolationPositions::add);
			imageTypes.add(ImageType.FLAT);
		}
		// Dark Before Acquisition
		if (getDarkCalibration().isBeforeAcquisition() && getDarkCalibration().getNumberExposures() > 0) {
			var preScanDarks = getCalibrationModel(trackDocument.getScannable(), posBeforeMainScan, getDarkCalibration().getNumberExposures());
			multiScanModel.addModel(preScanDarks);
			addPosition(darkPos, interpolationPositions::add);
			imageTypes.add(ImageType.DARK);
		}

		// Acquisition
		addPosition(createStartPosition(), interpolationPositions::add);
		var innerModel = acquisitionTemplate.getIScanPointGeneratorModels().get(acquisitionTemplate.getIScanPointGeneratorModels().size() - 1);
		multiScanModel.addModel(innerModel);
		imageTypes.add(ImageType.NORMAL);

		// Flat After Acquisition
		if (getFlatCalibration().isAfterAcquisition() && getFlatCalibration().getNumberExposures() > 0) {
			var postScanFlats = getCalibrationModel(trackDocument.getScannable(), posAfterMainScan, getFlatCalibration().getNumberExposures());
			multiScanModel.addModel(postScanFlats);
			addPosition(flatPos, interpolationPositions::add);
			imageTypes.add(ImageType.FLAT);
		}

		// Dark After Acquisition
		if (getDarkCalibration().isAfterAcquisition() && getDarkCalibration().getNumberExposures() > 0) {
			var posScanDarks = getCalibrationModel(trackDocument.getScannable(), posAfterMainScan, getDarkCalibration().getNumberExposures());
			multiScanModel.addModel(posScanDarks);
			addPosition(darkPos, interpolationPositions::add);
			imageTypes.add(ImageType.DARK);
		}

		multiScanModel.setContinuous(getScanpathDocument().getScannableTrackDocuments().stream().anyMatch(ScannableTrackDocument::isContinuous));
		multiScanModel.setInterpolatedPositions(interpolationPositions);
		multiScanModel.setImageTypes(imageTypes);
		return new CompoundModel(multiScanModel);
	}

	private IScanPointGeneratorModel getCalibrationModel(String scannable, double position, int points) {
		if (scannable == null) {
			return new StaticModel(points);
		}
		return new AxialPointsModel(scannable, position, points);
	}

	private void addPosition(IPosition startPos, Consumer<IPosition> consumer) {
		consumer.accept(startPos);
	}

	private IPosition createEndPosition() {
		return createPositionMap(getAcquisitionConfiguration().getEndPosition());
	}

	private IPosition createStartPosition() {
		return createPositionMap(getAcquisitionParameters().getPosition());
	}

	private IPosition createPositionMap(Set<DevicePositionDocument> devicePositions) {
		var positionMap = devicePositions.stream()
			.collect(Collectors.toMap(DevicePositionDocument::getDevice,
									  DevicePositionDocument::getPosition));
		return new MapPosition(positionMap);
	}

	private void parseAcquisitionEngine(ScanRequest scanRequest, IRunnableDeviceService runnableDeviceService)
			throws ScanningException {
		var acquisitionEngineType = Optional.ofNullable(getAcquisitionEngine())
			.map(AcquisitionEngineReader::getType)
			.orElseThrow(() -> new ScanningException("The document does not containan AcquisitionEngine section"));
		switch (acquisitionEngineType) {
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

		String id = Optional.ofNullable(getAcquisitionEngine())
				.map(AcquisitionEngineReader::getId)
				.orElseThrow(() -> new ScanningException("The AcquisitionEngine section does not contain the device id"));

		IRunnableDevice<IDetectorModel> detector = runnableDeviceService.getRunnableDevice(id);
		IDetectorModel imodel = Optional.ofNullable(detector.getModel())
				.orElseThrow(() -> new ScanningException(String.format("Could not get model for detector %s",
						detector.getName())));

		if (!(imodel instanceof IMalcolmModel))
			throw new ScanningException(String.format("Detector model is not an instance of of type %s", IMalcolmModel.class));

		final IMalcolmModel model = IMalcolmModel.class.cast(imodel);
		setDetectorsExposures(model);
		ret.put(detector.getName(), model);
	}

	private void setDetectorsExposures(IMalcolmModel model) {
		List<DetectorDocumentReader> detectors = getAcquisitionParameters().getDetectors();

		// Even if looking at the moment support only one detector (K11-1214)
		model.getDetectorModels().stream()
			.forEach(detectorModel -> setDetectorExposure(detectorModel, detectors));

		// Asks Malcolm to automatically estimate the duration
		model.setExposureTime(0);
	}

	private void setDetectorExposure(IMalcolmDetectorModel malcolmDetectorModel, List<DetectorDocumentReader> detectors) {
		detectors.stream()
		.filter(d -> d.getMalcolmDetectorName().equals(malcolmDetectorModel.getName()))
		.findFirst()
		.ifPresent(d -> malcolmDetectorModel.setExposureTime(d.getExposure()));
	}

	private Collection<String> parseMonitorNamesPerPoint() {
		return new ArrayList<>();
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
