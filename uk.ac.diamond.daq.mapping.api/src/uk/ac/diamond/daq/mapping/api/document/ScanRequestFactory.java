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
import java.util.Arrays;
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
import org.eclipse.scanning.api.scan.models.ScanMetadata;

import gda.autoprocessing.AutoProcessingBean;
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
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanningParametersUtils;
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

		Map<String, Collection<Object>> request = new HashMap<>();


		for (var bean : getAcquisitionConfiguration().getProcessingRequest()) {
			if (bean.isActive()) {
				var config = getProcessingConfig(bean);
				if (request.containsKey(bean.getAppName())) {
					request.get(bean.getAppName()).add(config);
				} else {
					var configList = new ArrayList<>(Arrays.asList(config));
					request.put(bean.getAppName(), configList);
				}
			}
		}

		var processingRequest = new ProcessingRequest();
		processingRequest.setRequest(request);

		scanRequest.setProcessingRequest(processingRequest);

		scanRequest.setTemplateFilePaths(getAcquisitionConfiguration().getNexusTemplatePaths());

		scanRequest.addScanMetadata(createSampleMetadata(getAcquisition().getData().getDescription()));

		return scanRequest;
	}

	private ScanMetadata createSampleMetadata(String name) {
		var metadata = new ScanMetadata(ScanMetadata.MetadataType.SAMPLE);
		metadata.addField("name", name);
		return metadata;
	}

	/**
	 * If the config object in {@code processingBean} is a map,
	 * we convert it to HashMap to get around serialisation issues.
	 */
	private Object getProcessingConfig(AutoProcessingBean processingBean) {
		if (processingBean.getConfig() instanceof Map<?, ?> configMap) {
			return new HashMap<>(configMap);
		}
		return processingBean.getConfig();
	}

	private CompoundModel createCompoundModel() throws GDAException {
		var acquisitionTemplates = AcquisitionTemplateFactory
				.buildModelDocument(getScanpathDocument().getData());

		if (getFlatCalibration().getNumberExposures() + getDarkCalibration().getNumberExposures() > 0) {
			return createInterpolatedCompoundModel(acquisitionTemplates.get(0)); // until use case arises, assume a single template in this case
		}

		return createCompoundModel(acquisitionTemplates);
	}

	private CompoundModel createCompoundModel(List<AcquisitionTemplate> acquisitionTemplates) {
		List<IScanPointGeneratorModel> generators = new ArrayList<>();
		List<ScanRegion> regions = new ArrayList<>();

		for (var template : acquisitionTemplates) {
			var generator = template.getIScanPointGeneratorModel();
			generators.add(generator);
			var roi = template.getROI();
			if (roi != null) {
				regions.add(new ScanRegion(roi, generator.getScannableNames()));
			}
		}

		var compoundModel = new CompoundModel(generators);
		compoundModel.setRegions(regions);
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
		var axes = ScanningParametersUtils.getAxesDocuments(getScanpathDocument().getData());
		ScannableTrackDocument trackDocument =  axes.get(0);
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
		var innerModel = acquisitionTemplate.getIScanPointGeneratorModel();
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

		multiScanModel.setContinuous(axes.stream().anyMatch(ScannableTrackDocument::isContinuous));
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

	private void prepareMalcolmAcquisitionEngine(ScanRequest scanRequest, IRunnableDeviceService runnableDeviceService) throws ScanningException {

		String deviceId = getAcquisitionEngine().getId();

		IRunnableDevice<IDetectorModel> detector = runnableDeviceService.getRunnableDevice(deviceId);
		var model = detector.getModel();
		if (model instanceof IMalcolmModel malcolmModel) {
			setDetectorsExposures(malcolmModel);
			Map<String, IDetectorModel> detectors = Map.of(detector.getName(), malcolmModel);
			scanRequest.setDetectors(detectors);
		} else {
			throw new ScanningException(String.format("Detector model is not an instance of of type %s", IMalcolmModel.class));
		}
	}

	private void setDetectorsExposures(IMalcolmModel model) throws ScanningException {
		var detectorsAndTheirExposures = getAcquisitionParameters().getDetectors().stream()
			.collect(Collectors.toMap(DetectorDocumentReader::getMalcolmDetectorName, DetectorDocumentReader::getExposure));

		var detectorModels = model.getDetectorModels();
		if (detectorModels == null) {
			throw new ScanningException("Malcolm scan not reporting any detectors! Try restarting Malcolm");
		}
		detectorModels.stream().forEach(detector ->
			detector.setExposureTime(detectorsAndTheirExposures.getOrDefault(detector.getName(), 0.0))); // if det not ref'd in params, set its exposure to 0

		// Asks Malcolm to automatically estimate the duration
		model.setExposureTime(0);
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
