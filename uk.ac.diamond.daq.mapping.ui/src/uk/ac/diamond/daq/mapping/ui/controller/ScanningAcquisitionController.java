package uk.ac.diamond.daq.mapping.ui.controller;

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;
import static uk.ac.gda.ui.tool.rest.ClientRestServices.getScanningAcquisitionRestServiceClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import uk.ac.diamond.daq.mapping.api.ScanRequestSavedEvent;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.ScanRequestFactory;
import uk.ac.diamond.daq.mapping.api.document.helper.ImageCalibrationHelper;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.AcquisitionReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.ImageCalibrationReader;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceDeleteEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceSaveEvent;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.AcquisitionControllerException;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionKeys;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.acquisition.AcquisitionTemplateConfiguration;
import uk.ac.gda.client.properties.mode.Modes;
import uk.ac.gda.client.properties.mode.TestMode;
import uk.ac.gda.client.properties.mode.TestModeElement;
import uk.ac.gda.client.properties.stage.ScannableProperties;
import uk.ac.gda.client.properties.stage.position.Position;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue;
import uk.ac.gda.ui.tool.controller.AcquisitionController;
import uk.ac.gda.ui.tool.document.ClientPropertiesHelper;
import uk.ac.gda.ui.tool.document.DocumentFactory;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;
import uk.ac.gda.ui.tool.rest.ConfigurationsRestServiceClient;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * A controller for ScanningAcquisition views.
 * <p>
 * The class supports a view to load, save, delete or run a {@link ScanningAcquisition} instance. This class is based on
 * several Spring features
 * <ul>
 * <li>it is annotated with {@link Controller} in order to be auto-detected through classpath scanning by Spring</li>
 * <li>it is annotated with {@link Scope} as prototype, so any invocation of
 * {@code SpringApplicationContextProxy.getBean(ScanningAcquisitionController.class)} returns a new instance</li>
 * </ul>
 * </p>
 *
 * @author Maurizio Nagni
 */
@Controller
public class ScanningAcquisitionController
		implements AcquisitionController<ScanningAcquisition> {
	private static final Logger logger = LoggerFactory.getLogger(ScanningAcquisitionController.class);

	public static final String DEFAULT_CONFIGURATION_NAME = "UntitledConfiguration";

	@Autowired
	private StageController stageController;

	@Autowired
	private ClientRemoteServices remoteServices;

	@Autowired
	private ConfigurationsRestServiceClient configurationService;

	@Autowired
	private ClientPropertiesHelper clientPropertiesHelper;

	@Autowired
	private ScanningAcquisitionTemporaryHelper tempHelper;

	@Autowired
	private DocumentFactory documentFactory;

	private ScanningAcquisition acquisition;

	private AcquisitionKeys acquisitionKeys;

	private ImageCalibrationHelper imageCalibrationHelper;

	private AcquisitionReader acquisitionReader;

	public ScanningAcquisitionController() {
		this(new AcquisitionKeys(AcquisitionPropertyType.DEFAULT, AcquisitionTemplateType.STATIC_POINT));
	}

	/**
	 * Creates a controller based on specific {@link AcquisitionKeys} in order to retrieves the associates
	 * cameras and acquisition engines.
	 *
	 * @param acquisitionKey
	 *
	 * @see AcquisitionPropertyType DetectorHelper
	 */
	public ScanningAcquisitionController(AcquisitionKeys acquisitionKey) {
		super();
		this.acquisitionKeys = acquisitionKey;
	}

	@Override
	public ScanningAcquisition getAcquisition() {
		return acquisition;
	}

	@Override
	public void saveAcquisitionConfiguration() throws AcquisitionControllerException {
		finalizeAcquisition();
		save(formatConfigurationFileName(getAcquisition().getName()));
	}

	@Override
	public RunAcquisitionResponse runAcquisition() throws AcquisitionControllerException {
		finalizeAcquisition();
		ResponseEntity<RunAcquisitionResponse> responseEntity;
		try {
			responseEntity = getScanningAcquisitionRestServiceClient().run(getAcquisition());
		} catch (GDAClientRestException e) {
			throw new AcquisitionControllerException(e);
		}
		RunAcquisitionResponse response = responseEntity.getBody();
		if (HttpStatus.OK.equals(responseEntity.getStatusCode())) {
			return response;
		}
		throw new AcquisitionControllerException("RunAcquisitionResponse [submitted=" + response.isSubmitted() + ", message=" + response.getMessage() + "]");
	}

	@Override
	public void loadAcquisitionConfiguration(ScanningAcquisition acquisition) throws AcquisitionControllerException {
		var acquisitionKeysToload = tempHelper.getAcquisitionKeys(acquisition);
		if (!getAcquisitionKeys().getPropertyType().equals(acquisitionKeysToload.getPropertyType())) {
			var reason = String.format("Cannot load a %s file into a %s configuration view", acquisitionKeysToload, getAcquisitionKeys());
			UIHelper.showWarning("Cannot load", reason);
		} else {
			updateAcquisitionConfiguration(acquisition, acquisitionKeysToload);
		}
	}

	private void loadAcquisitionConfiguration(AcquisitionKeys acquisitionKeys) throws AcquisitionControllerException {
		var acquisitionToLoad = documentFactory.newScanningAcquisition(acquisitionKeys);
		updateAcquisitionConfiguration(acquisitionToLoad, acquisitionKeys);
	}

	/**
	 * Handles the case when a new {@link ScanningAcquisition} has to be created from its identifying {@link AcquisitionKeys}
	 * @param acquisitionKeys
	 * @throws AcquisitionControllerException
	 */
	private void updateAcquisitionConfiguration(ScanningAcquisition acquisition, AcquisitionKeys acquisitionKeys) {
		setAcquisition(acquisition);
		this.acquisitionKeys = acquisitionKeys;
		publishEvent(new AcquisitionConfigurationResourceLoadEvent(this, acquisition.getUuid()));
	}

	@Override
	public AcquisitionConfigurationResource<ScanningAcquisition> createAcquisitionConfigurationResource(UUID uuid)
			throws AcquisitionControllerException {
		try {
			return new AcquisitionConfigurationResource<>(configurationService.getDocumentURL(uuid),
					(ScanningAcquisition)configurationService.getDocument(uuid.toString()));
		} catch (GDAClientRestException e) {
			throw new AcquisitionControllerException("Error in clientRest", e);
		}
	}

	@Override
	public void deleteAcquisitionConfiguration(UUID uuid) throws AcquisitionControllerException {
		try {
			configurationService.deleteDocument(uuid.toString());
			publishEvent(new AcquisitionConfigurationResourceDeleteEvent(this, uuid));
		} catch (GDAClientRestException e) {
			logger.error("Cannot delete scanning acquisiton", e);
		}
	}

	@Override
	public void newScanningAcquisition(AcquisitionKeys acquisitionKeys) throws AcquisitionControllerException {
		loadAcquisitionConfiguration(acquisitionKeys);
	}

	@Override
	public AcquisitionKeys getAcquisitionKeys() {
		return acquisitionKeys;
	}

	private String formatConfigurationFileName(String fileName) {
		return Optional.ofNullable(fileName)
			.map(n -> fileName.replaceAll("\\s", ""))
			.filter(n -> n.length() > 0)
			.orElseGet(() -> "noNameConfiguration");
	}

	private void save(String fileName) throws AcquisitionControllerException {
		ScanningAcquisition savedAcquisition = null;
		AcquisitionConfigurationResourceType type = AcquisitionConfigurationResourceType.DEFAULT;
		try {
			if (AcquisitionPropertyType.TOMOGRAPHY.equals(acquisitionKeys.getPropertyType())) {
				savedAcquisition = configurationService.insertImaging(getAcquisition());
				type = AcquisitionConfigurationResourceType.TOMO;
			} else if (AcquisitionPropertyType.DIFFRACTION.equals(acquisitionKeys.getPropertyType())) {
				savedAcquisition = configurationService.insertDiffraction(getAcquisition());
				type = AcquisitionConfigurationResourceType.MAP;
			}
		} catch (GDAClientRestException e) {
			logger.error("Cannot save the configuration", e);
		}
		if (savedAcquisition != null) {
			loadAcquisitionConfiguration(savedAcquisition);
		}

		publishEvent(new AcquisitionConfigurationResourceSaveEvent(this, getAcquisition().getUuid(), type));
		publishScanRequestSavedEvent(fileName);
	}

	/**
	 * Creates a ScanRequest for sake of the fully automated implementation.
	 * @param fileName
	 *
	 * @deprecated the fully automated can, and should, use an approach consistent with the ScanningAcquisitionController
	 * and compose its operations using the saved ScanningAcquisition.
	 * While this class is a client side controller, this methods keeps this class dependent from objects,
	 * like ScanRequestFactory, that instead live naturally in the server side.
	 * This inconsistency is fixed in K11-1313
	 */
	@Deprecated
	private void publishScanRequestSavedEvent(String fileName) {
		try {
			var srf = new ScanRequestFactory(getAcquisition());
			var scanRequest = srf.createScanRequest(remoteServices.getIRunnableDeviceService());
			publishEvent(new ScanRequestSavedEvent(this, fileName, scanRequest));
		} catch (ScanningException e) {
			logger.error("Canot create scanRequest", e);
		}
	}

	/**
	 * Finalises the acquisition so that is possible to save or run.
	 *
	 * <p>
	 * When an acquisition is created does not contain element describing on which start/end positions the acquisition should happen.
	 * This method is called before save or run, or any other output, that should describe a {@code ScanningAcquisition}) valid to be used as acquisition request
	 * </p>
	 *
	 * @throws AcquisitionControllerException
	 */
	private void finalizeAcquisition() throws AcquisitionControllerException {
		// Saves the scannables defined in the client.positions with position equal to Position.Start
		Set<DevicePositionDocument> startPosition = stageController.reportPositions(Position.START);
		updateStartPosition(startPosition);
		updateImageCalibrationStartPosition(startPosition);
	}

	private void updateStartPosition(Set<DevicePositionDocument> startPosition) throws AcquisitionControllerException {
		AcquisitionTemplateType templateType = tempHelper.getSelectedAcquisitionTemplateType()
				.orElseThrow(() -> new AcquisitionControllerException("The actual scanning acquisition has no defined templateType"));

		var instancePosition = acquisition.getAcquisitionConfiguration().getAcquisitionParameters().getStartPosition();
		startPosition.removeIf(doc -> instancePosition.stream().anyMatch(instanceDoc -> doc.getDevice().equals(instanceDoc.getDevice())));
		startPosition.addAll(instancePosition);

		updateAcquisitionPositions(startPosition,
				new AcquisitionKeys(acquisitionKeys.getPropertyType(), templateType),
				AcquisitionConfigurationProperties::getStartPosition, AcquisitionTemplateConfiguration::getStartPosition,
				getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters()::setStartPosition);

		// Guarantee that all the motors return to the start positions.
		updateAcquisitionPositions(startPosition,
				new AcquisitionKeys(acquisitionKeys.getPropertyType(), templateType),
				AcquisitionConfigurationProperties::getEndPosition, null,
				getAcquisition().getAcquisitionConfiguration()::setEndPosition);
	}

	/**
	 * There are two elements FlatCalibration and DarkCalibration.
	 * For the FlatCalibration
	 * <ol>
	 * <li>
	 * open the shutter
	 * </li>
	 * <li>
	 * move to the predefined OUT_OF_BEAM position
	 * </li>
	 * </ol>
	 * For the DarkCalibration
	 * <ol>
	 * <li>
	 * close the shutter
	 * </li>
	 * </ol>
	 */
	private void updateImageCalibrationStartPosition(Set<DevicePositionDocument> startPosition) {
		ImageCalibrationReader ic = getAcquisitionReader().getAcquisitionConfiguration().getImageCalibration();
		if (ic.getFlatCalibration().isAfterAcquisition() || ic.getFlatCalibration().isBeforeAcquisition()) {
			updateAcquisitionPositions(startPosition,
					new AcquisitionKeys(AcquisitionPropertyType.CALIBRATION, AcquisitionTemplateType.FLAT),
					AcquisitionConfigurationProperties::getStartPosition, AcquisitionTemplateConfiguration::getStartPosition,
					getImageCalibrationHelper()::updateFlatDetectorPositionDocument);
		}

		if (ic.getDarkCalibration().isAfterAcquisition() || ic.getDarkCalibration().isBeforeAcquisition()) {
			updateAcquisitionPositions(startPosition,
					new AcquisitionKeys(AcquisitionPropertyType.CALIBRATION, AcquisitionTemplateType.DARK),
					AcquisitionConfigurationProperties::getStartPosition, AcquisitionTemplateConfiguration::getStartPosition,
					getImageCalibrationHelper()::updateDarkDetectorPositionDocument);
		}
	}

	/**
	 * Updates the scanning acquisition positions.
	 *
	 * <p>
	 * This methods implements the finalisation of a scanning acquisition positions and the logic can be described as
	 * <ol>
	 * <li>
	 * the {@code startPosition} is the {@code Set} of {@link DevicePositionDocument} generated by START in {@link ClientSpringProperties#getPositions()}
	 * </li>
	 * <li>
	 * using {@code acquistionType} and {@code templateType}, retrieves {@link AcquisitionConfigurationProperties} and the specific {@link AcquisitionTemplateConfiguration}
	 * </li>
	 * <li>
	 * from retrieved acquisition properties document, a list of required either start or end positions are extracted using respectively the @code {mapperAcquisition} and @code {mapperAcquisition}.
	 * This will generate a specific acquisition {@code DevicePositionDocument} list
	 * </li>
	 * <li>
	 * the generated list will then override the documents in {@code startPosition} and finally will be set by the {@code consumer}
	 * </li>
	 * </ol>
	 * </p>
	 * @param startPosition the actual beamline positions for the scannable defined in START in {@link ClientSpringProperties#getPositions()}
	 * @param acquistionType the scanning acquisition acquisition type
	 * @param mapperAcquisition getter functions for an acquisition properties position list
	 * @param mapperType getter functions for an acquisition type properties position list
	 * @param consumer setter functions for the scanning acquisition type position list
	 */
	private void updateAcquisitionPositions(Set<DevicePositionDocument> startPosition,
			AcquisitionKeys acquisitionKey,
			Function<AcquisitionConfigurationProperties, List<ScannablePropertiesValue>> mapperAcquisition,
			Function<AcquisitionTemplateConfiguration, List<ScannablePropertiesValue>> mapperType,
			Consumer<Set<DevicePositionDocument>> consumer) {

		Set<DevicePositionDocument> positions = new HashSet<>(startPosition);
		processAcquisitionPositions(positions, acquisitionKey, mapperAcquisition, mapperType);
		updatePositionDocument(positions, consumer);
	}

	/**
	 * Overrides/amend the {@code positions} with the position defined in the acquisition type/template positions
	 *
	 * <p>
	 * The logic can be described as
	 * <ol>
	 * using {@code acquistionType} and {@code templateType}, retrieves {@link AcquisitionConfigurationProperties} and the specific {@link AcquisitionTemplateConfiguration}
	 * </li>
	 * <li>
	 * from retrieved acquisition properties document, a list of required either start or end positions are extracted using respectively the @code {mapperAcquisition} and @code {mapperType}.
	 * This will generate a specific acquisition {@code DevicePositionDocument} list
	 * </li>
	 * <li>
	 * the generated list will then override the documents in {@code startPosition}
	 * </li>
	 * </ol>
	 * </p>
	 * @param positions a set of default positions
	 * @param acquistionType the scanning acquisition acquisition type
	 * @param templateType the specific template for the acquisition type
	 * @param mapperAcquisition getter functions for an acquisition properties position list
	 * @param mapperType getter functions for an acquisition type properties position list
	 */
	private void processAcquisitionPositions(Set<DevicePositionDocument> positions,
			AcquisitionKeys acquisitionKey,
			Function<AcquisitionConfigurationProperties, List<ScannablePropertiesValue>> mapperAcquisition,
			Function<AcquisitionTemplateConfiguration, List<ScannablePropertiesValue>> mapperType) {

		List<ScannablePropertiesValue> positionFromAcquisition = clientPropertiesHelper.getAcquisitionConfigurationProperties(acquisitionKey.getPropertyType())
			.map(mapperAcquisition).orElseGet(ArrayList::new);

		if (mapperType != null) {
			// TODO refactor so that this is never null
			List<ScannablePropertiesValue> positionFromType = clientPropertiesHelper.getAcquisitionTemplateConfiguration(acquisitionKey)
				.map(mapperType).orElse(Collections.emptyList());

			positionFromAcquisition.removeAll(positionFromType);
			positionFromAcquisition.addAll(positionFromType);
		}

		processAcquisitionTemplatePositions(positions, positionFromAcquisition);
	}

	private void processAcquisitionTemplatePositions(Set<DevicePositionDocument> positions, List<ScannablePropertiesValue> spv) {
		var documentValue = spv.stream()
				.map(stageController::createDevicePositionDocument)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		// Removes the START positions (client.positions[0].position = START)
		// existing in documentValue
		positions.removeAll(documentValue);
		// Append document value to the remaining START positions
		positions.addAll(documentValue);
		applyModesToScannables(positions);
	}

	private void applyModesToScannables(Set<DevicePositionDocument> positions) {
		boolean isActive = Optional.ofNullable(clientPropertiesHelper.getModes())
			.map(Modes::getTest)
			.map(TestMode::isActive)
			.orElse(false);
		if (!isActive)
			return;

		List<String> toExclude = clientPropertiesHelper.getModes().getTest().getElements().stream()
			.filter(TestModeElement::isExclude)
			.map(TestModeElement::getDevice)
			.map(stageController::getScannablePropertiesDocument)
			.filter(Objects::nonNull)
			.map(ScannableProperties::getScannable)
			.collect(Collectors.toList());

		positions.removeIf(p -> toExclude.contains(p.getDevice()));
	}

	private void updatePositionDocument(Set<DevicePositionDocument> positions, Consumer<Set<DevicePositionDocument>> consumer) {
		consumer.accept(positions);
	}

	public ScanningParameters getAcquisitionParameters() {
		return getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
	}

	private void setAcquisition(ScanningAcquisition acquisition) {
		// eventually release already acquired resources, eventually
		releaseResources();
		this.acquisition = acquisition;
	}

	@Override
	public void releaseResources() {
		// DO NOTHING
	}

	private ImageCalibrationHelper getImageCalibrationHelper() {
		return Optional.ofNullable(imageCalibrationHelper)
				.orElseGet(this::initializeImageCalibrationHelper);
	}

	private ImageCalibrationHelper initializeImageCalibrationHelper() {
		imageCalibrationHelper = new ImageCalibrationHelper(() -> getAcquisition().getAcquisitionConfiguration());
		return imageCalibrationHelper;
	}

	private AcquisitionReader getAcquisitionReader() {
		if (this.acquisitionReader == null) {
			this.acquisitionReader = new AcquisitionReader(this::getAcquisition);
		}
		return this.acquisitionReader;
	}
}