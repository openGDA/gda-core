package uk.ac.diamond.daq.mapping.ui.controller;

import static uk.ac.gda.client.properties.stage.DefaultManagedScannable.BEAM_SELECTOR;
import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;
import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;
import static uk.ac.gda.ui.tool.rest.ClientRestServices.getScanningAcquisitionRestServiceClient;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
import uk.ac.diamond.daq.mapping.api.document.ScanRequestFactory;
import uk.ac.diamond.daq.mapping.api.document.helper.ImageCalibrationHelper;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.AcquisitionReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.ImageCalibrationReader;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.Position;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionControllerException;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceDeleteEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceSaveEvent;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.mode.Modes;
import uk.ac.gda.client.properties.mode.TestMode;
import uk.ac.gda.client.properties.mode.TestModeElement;
import uk.ac.gda.client.properties.stage.ManagedScannable;
import uk.ac.gda.client.properties.stage.ScannablesPropertiesHelper;
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
@Controller(value = "scanningAcquisitionController")
@Scope("prototype")
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
	private ClientSpringProperties clientProperties;

	private ScanningAcquisition acquisition;

	private Supplier<ScanningAcquisition> newAcquisitionSupplier;

	private AcquisitionPropertyType acquisitionType;

	private ImageCalibrationHelper imageCalibrationHelper;
	private ScanningAcquisitionControllerDetectorHelper detectorsHelper;

	private AcquisitionReader acquisitionReader;

	public ScanningAcquisitionController() {
		super();
		setAcquisitionType(AcquisitionPropertyType.DEFAULT);
	}

	/**
	 * Creates a controller based on specific {@link AcquisitionPropertyType} in order to retrieves the associates
	 * cameras and acquisition engines.
	 *
	 * @param acquisitionType
	 *
	 * @see AcquisitionPropertyType DetectorHelper
	 */
	public ScanningAcquisitionController(AcquisitionPropertyType acquisitionType) {
		super();
		setAcquisitionType(acquisitionType);
	}

	@Override
	public ScanningAcquisition getAcquisition() {
		return acquisition;
	}

	@Override
	public void saveAcquisitionConfiguration() throws AcquisitionControllerException {
		updateImageCalibration();
		save(formatConfigurationFileName(getAcquisition().getName()));
	}

	@Override
	public RunAcquisitionResponse runAcquisition() throws AcquisitionControllerException {
		updateImageCalibration();
		updateStartPosition();
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
		setAcquisition(acquisition);
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
			// Removed the document actually loaded, creates a new acquisition
			if (getAcquisition().getUuid().equals(uuid)) {
				createNewAcquisition();
			}
		} catch (GDAClientRestException e) {
			logger.error("Cannot delete scanning acquisiton", e);
		}
	}

	@Override
	public void createNewAcquisition() {
		try {
			// load the new acquisition in the controller
			loadAcquisitionConfiguration(getDefaultNewAcquisitionSupplier().get());
		} catch (AcquisitionControllerException e) {
			// We do not expect this to happen
			logger.error("Could not create new acquisition configuration");
		}
	}

	@Override
	public void setDefaultNewAcquisitionSupplier(Supplier<ScanningAcquisition> newAcquisitionSupplier) {
		this.newAcquisitionSupplier = newAcquisitionSupplier;
	}

	private Supplier<ScanningAcquisition> getDefaultNewAcquisitionSupplier() {
		return Optional.ofNullable(newAcquisitionSupplier).orElse(ScanningAcquisition::new);
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
			if (AcquisitionPropertyType.TOMOGRAPHY.equals(getAcquisitionType())) {
				savedAcquisition = configurationService.insertImaging(getAcquisition());
				type = AcquisitionConfigurationResourceType.TOMO;
			} else if (AcquisitionPropertyType.DIFFRACTION.equals(getAcquisitionType())) {
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
	private void updateImageCalibration() throws AcquisitionControllerException {
		ImageCalibrationReader ic = getAcquisitionReader().getAcquisitionConfiguration().getImageCalibration();
		validateFlatCalibrationParameters(ic);
		validateDarkCalibrationParameters(ic);
	}

	private void validateFlatCalibrationParameters(ImageCalibrationReader ic) throws AcquisitionConfigurationException {
		// Note - Uses a read-only acquisition object to avoid Null Pointer in ic
		Set<DevicePositionDocument> flatPosition = stageController.getPositionDocuments(Position.OUT_OF_BEAM, detectorsHelper.getOutOfBeamScannables());
		if ((ic.getFlatCalibration().isAfterAcquisition() || ic.getFlatCalibration().isBeforeAcquisition())
				&& flatPosition.isEmpty()) {
			throw new AcquisitionConfigurationException("Save an OutOfBeam position to acquire flat images");
		}
		addPosition(stageController.createShutterOpenRequest(), flatPosition::add);
		positionsPostProcess(flatPosition);
		updatePositionDocument(flatPosition, getImageCalibrationHelper()::updateFlatDetectorPositionDocument);
	}

	private void validateDarkCalibrationParameters(ImageCalibrationReader ic) {
		// Note - Uses a read-only acquisition object to avoid Null Pointer in ic
		if (ic.getDarkCalibration().isAfterAcquisition() || ic.getDarkCalibration().isBeforeAcquisition()) {
			Set<DevicePositionDocument> darkPosition = new HashSet<>();
			darkPosition.add(stageController.createShutterClosedRequest());
			positionsPostProcess(darkPosition);
			updatePositionDocument(darkPosition, getImageCalibrationHelper()::updateDarkDetectorPositionDocument);
		}
	}

	private void positionsPostProcess(Set<DevicePositionDocument> positions) {
		Optional.ofNullable(getBeamSelector())
			.filter(ManagedScannable::isAvailable)
			.map(stageController::createDevicePositionDocument)
			.ifPresent(d -> addPosition(d, positions::add));

		filterTestScannable(positions);
	}



	private void filterTestScannable(Set<DevicePositionDocument> positions) {
		boolean isActive = Optional.ofNullable(clientProperties.getModes())
			.map(Modes::getTest)
			.map(TestMode::isActive)
			.orElse(false);
		if (!isActive)
			return;

		List<String> toExclude = clientProperties.getModes().getTest().getElements().stream()
			.filter(TestModeElement::isExclude)
			.map(TestModeElement::getDevice)
			.collect(Collectors.toList());

		positions.removeIf(p -> toExclude.contains(p.getDevice()));
	}

	private ManagedScannable<String> getBeamSelector() {
		return getBean(ScannablesPropertiesHelper.class)
				.getManagedScannable(BEAM_SELECTOR, BEAM_SELECTOR.getScannableType());
	}

	private void updateStartPosition() {
		if (getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters() == null)
			return;
		// Saves ALL the devices position and mark this set as Position.Start
		// This collection is stored in the stage controller as it is responsible
		// for uk.ac.diamond.daq.mapping.ui.stage.enumeration.Position values
		if (!stageController.hasPosition(Position.START))
			stageController.savePosition(Position.START);
		// Filters out from the Position.START positions, the position document from AcquisitionPropertiesDocument::getOutOfBeamScannables
		// See AcquisitionPropertiesDocument#outOfBeamScannables
		Set<DevicePositionDocument> startPosition = stageController.getPositionDocuments(Position.START, detectorsHelper.getOutOfBeamScannables());
		addPosition(stageController.createShutterOpenRequest(), startPosition::add);
		positionsPostProcess(startPosition);
		updatePositionDocument(startPosition, getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters()::setPosition);
	}

	private void addPosition(DevicePositionDocument position, Consumer<DevicePositionDocument> consumer) {
		consumer.accept(position);
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
		// associate a new helper with the new acquisition
		this.detectorsHelper = new ScanningAcquisitionControllerDetectorHelper(getAcquisitionType(),
				this::getAcquisition);
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

	public AcquisitionPropertyType getAcquisitionType() {
		return acquisitionType;
	}

	private void setAcquisitionType(AcquisitionPropertyType acquisitionType) {
		this.acquisitionType = acquisitionType;
	}
}