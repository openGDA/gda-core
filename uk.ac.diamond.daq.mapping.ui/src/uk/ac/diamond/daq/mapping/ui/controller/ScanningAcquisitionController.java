package uk.ac.diamond.daq.mapping.ui.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.observable.IObserver;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.event.CameraEventUtils;
import uk.ac.diamond.daq.client.gui.camera.event.ExposureChangeEvent;
import uk.ac.diamond.daq.mapping.api.ScanRequestSavedEvent;
import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.mapping.api.document.ScanRequestFactory;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionRunEvent;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionSaveEvent;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.service.message.ScanningAcquisitionMessage;
import uk.ac.diamond.daq.mapping.ui.properties.DetectorHelper;
import uk.ac.diamond.daq.mapping.ui.properties.DetectorHelper.AcquisitionType;
import uk.ac.diamond.daq.mapping.ui.services.ScanningAcquisitionFileService;
import uk.ac.diamond.daq.mapping.ui.stage.IStageController;
import uk.ac.diamond.daq.mapping.ui.stage.StageConfiguration;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.Position;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionControllerException;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceSaveEvent;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.api.exception.GDAException;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.properties.CameraProperties;
import uk.ac.gda.client.properties.DetectorProperties;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * A controller for ScanningAcquisition views.
 * <p>
 * The class supports a view to load, save, delete or run a {@link ScanningAcquisition} instance. This class is based on
 * several Spring features
 * <ul>
 * <li>it is annotated with {@link Controller} in order to be autodetected through classpath scanning by Spring</li>
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
		implements AcquisitionController<ScanningAcquisition>, ApplicationListener<ApplicationEvent> {

	private static final Logger logger = LoggerFactory.getLogger(ScanningAcquisitionController.class);

	@SuppressWarnings("unused") // to be used for dark/flat field acquisition in the near future
	@Autowired
	private IStageController stageController;

	private ScanningAcquisition acquisition;

	private ScanningAcquisitionFileService fileService;

	private Supplier<ScanningAcquisition> newAcquisitionSupplier;

	private List<DetectorProperties> detectorProperties;
	private AcquisitionType acquisitionType;

	/**
	 * These are the camera associated with this acquisition controller. Some specific acquisition may use more than one
	 * camera, as BeamSelector scan in DIAD (K11)
	 */
	private List<CameraControl> camerasControl;

	public ScanningAcquisitionController() {
		super();
		this.acquisitionType = AcquisitionType.DEFAULT;
	}

	/**
	 * Creates a controller based on specific {@link AcquisitionType} in order to retrieves the associates cameras.
	 *
	 * @param acquisitionType
	 *
	 * @see AcquisitionType DetectorHelper
	 */
	public ScanningAcquisitionController(AcquisitionType acquisitionType) {
		super();
		this.acquisitionType = acquisitionType;
		this.detectorProperties = DetectorHelper.getAcquistionDetector(acquisitionType).orElse(new ArrayList<>());
	}

	@Override
	public ScanningAcquisition getAcquisition() {
		return acquisition;
	}

	@Override
	public void saveAcquisitionConfiguration() throws AcquisitionControllerException {
		String acquisitionDocument;
		// StageConfiguration sc = generateStageConfiguration(getAcquisition());
		try {
			updateExposures();
			save(formatConfigurationFileName(getAcquisition().getName()), DocumentMapper.toJSON(getAcquisition()));
		} catch (GDAException e) {
			throw new AcquisitionControllerException(e);
		}
	}

	@Override
	public void runAcquisition() throws AcquisitionControllerException {
		updateExposures();
		publishRun(createScanningMessage());
	}

	@Override
	public void loadAcquisitionConfiguration(URL url) throws AcquisitionControllerException {
		loadAcquisitionConfiguration(parseAcquisitionConfiguration(url).getResource());
	}

	@Override
	public void loadAcquisitionConfiguration(ScanningAcquisition acquisition) throws AcquisitionControllerException {
		this.acquisition = acquisition;
		SpringApplicationContextProxy.publishEvent(new AcquisitionConfigurationResourceLoadEvent(this, acquisition.getAcquisitionLocation()));
	}

	@Override
	public AcquisitionConfigurationResource<ScanningAcquisition> parseAcquisitionConfiguration(URL url)
			throws AcquisitionControllerException {
		try {
			return new AcquisitionConfigurationResource<>(url, DocumentMapper.fromJSON(url, ScanningAcquisition.class));
		} catch (GDAException e) {
			throw new AcquisitionControllerException(e);
		}
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (ExposureChangeEvent.class.isInstance(event)) {
			ScanningParameters tp = getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
			tp.setDetector(new DetectorDocument(tp.getDetector().getName(),
					ExposureChangeEvent.class.cast(event).getExposureTime()));
		}
	}

	@Override
	public void deleteAcquisitionConfiguration(URL url) throws AcquisitionControllerException {
		throw new AcquisitionControllerException("Delete not implemented");
	}

	@Override
	public void createNewAcquisition() {
		try {
			loadAcquisitionConfiguration(getDefaultNewAcquisitionSupplier().get());
		} catch (AcquisitionControllerException e) {
			// We do not expect this to happen
			logger.error("Could not create new acquisition configuration");
		}
		setTemplateDetector();
	}

	@Override
	public void setDefaultNewAcquisitionSupplier(Supplier<ScanningAcquisition> newAcquisitionSupplier) {
		this.newAcquisitionSupplier = newAcquisitionSupplier;
	}

	private Supplier<ScanningAcquisition> getDefaultNewAcquisitionSupplier() {
		return Optional.ofNullable(newAcquisitionSupplier).orElse(ScanningAcquisition::new);
	}

	private ScanningAcquisitionMessage createScanningMessage() throws AcquisitionControllerException {
		try {
			updateExposures();
			return new ScanningAcquisitionMessage(DocumentMapper.toJSON(getAcquisition()));
		} catch (GDAException e) {
			throw new AcquisitionControllerException(e);
		}
	}

	private String formatConfigurationFileName(String fileName) {
		String fn = "";
		if (fileName != null) {
			fn = fileName.replaceAll("\\s", "");
		}
		if (fn.length() == 0) {
			fn = "noNameConfiguration";
		}

		return String.format("%s_%s", Optional.ofNullable(acquisitionType).orElse(AcquisitionType.DEFAULT).name(), fn);
	}

	private void save(String fileName, String acquisitionDocument) {
		try {
			Path path = getPath(fileName, acquisitionDocument);
			SpringApplicationContextProxy
					.publishEvent(new AcquisitionConfigurationResourceSaveEvent(this, path.toUri().toURL()));
			publishScanRequestSavedEvent(fileName);
		} catch (IOException e) {
			UIHelper.showError("Cannot save the configuration", e);
		}
		publishSave(getAcquisition().getName(), acquisitionDocument);
	}

	private Path getPath(String fileName, String acquisitionDocument) throws IOException {
		switch (acquisitionType) {
		case TOMOGRAPHY:
			return getFileService().saveTextDocument(acquisitionDocument, fileName,
					AcquisitionConfigurationResourceType.TOMO.getExtension());
		case DIFFRACTION:
		case BEAM_SELECTOR:
			return getFileService().saveTextDocument(acquisitionDocument, fileName,
					AcquisitionConfigurationResourceType.MAP.getExtension());
		default:
			return getFileService().saveTextDocument(acquisitionDocument, fileName,
					AcquisitionConfigurationResourceType.DEFAULT.getExtension());
		}
	}

	private void publishScanRequestSavedEvent(String fileName) {
		try {
			ScanRequestFactory srf = new ScanRequestFactory(getAcquisition());
			ScanRequest scanRequest = srf.createScanRequest(getIRunnableDeviceService());
			SpringApplicationContextProxy.publishEvent(new ScanRequestSavedEvent(this, fileName, scanRequest));
		} catch (ScanningException e) {
			logger.error("Canot create scanRequest", e);
		}
	}

	/**
	 * Verifies if a {@link Positions#OUT_OF_BEAM} is present, if the user wants to acquire flat images.
	 *
	 * @param sc
	 *            the stage configuration
	 * @return true if the stage configuration is consistent, false otherwise
	 */
	private boolean requiresOutOfBeamPosition(StageConfiguration sc) {
		ImageCalibration ic = sc.getAcquisition().getAcquisitionConfiguration().getImageCalibration();
		return ((ic.getNumberFlat() > 0 && (ic.isAfterAcquisition() || ic.isBeforeAcquisition()))
				&& !sc.getMotorsPositions().containsKey(Position.OUT_OF_BEAM));
	}

	private StageConfiguration generateStageConfiguration(ScanningAcquisition acquisition)
			throws AcquisitionControllerException {
		stageController.savePosition(Position.START);
		StageConfiguration sc = new StageConfiguration(acquisition, stageController.getStageDescription(),
				stageController.getMotorsPositions());
		if (requiresOutOfBeamPosition(sc)) {
			throw new AcquisitionControllerException("Acquisition needs a OutOfBeam position to acquire flat images");
		}
		return sc;
	}

	public ScanningParameters getAcquisitionParameters() {
		return getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
	}

	protected StageConfiguration parseJsonData(String jsonData) throws AcquisitionControllerException {
		return parseJsonData(jsonData.getBytes());
	}

	protected StageConfiguration parseJsonData(byte[] jsonData) throws AcquisitionControllerException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(jsonData, StageConfiguration.class);
		} catch (JsonParseException e) {
			logger.error("Cannot parse JSON document", e);
		} catch (JsonMappingException e) {
			logger.error("Cannot map JSON document", e);
		} catch (IOException e) {
			logger.error("Cannot read JSON document", e);
		}
		throw new AcquisitionControllerException("Cannot parse json document");
	}

	private ScanningAcquisitionFileService getFileService() {

		if (fileService == null) {
			fileService = new ScanningAcquisitionFileService();
		}
		return fileService;
	}

	private void publishSave(String name, String acquisition) {
		SpringApplicationContextProxy.publishEvent(new ScanningAcquisitionSaveEvent(this, name, acquisition));
	}

	private void publishRun(ScanningAcquisitionMessage tomographyRunMessage) {
		SpringApplicationContextProxy.publishEvent(new ScanningAcquisitionRunEvent(this, tomographyRunMessage));
	}

	private void setTemplateDetector() {
		if (detectorProperties.isEmpty())
			return;
		int index = 0; // in future may be parametrised
		DetectorProperties dp = detectorProperties.get(index);
		DetectorDocument dd = new DetectorDocument(dp.getDetectorBean(), 0);
		getAcquisitionParameters().setDetector(dd);

		camerasControl = new ArrayList<>();
		dp.getCameras().stream().map(CameraHelper::getCameraPropertiesByID)
				.filter(Optional::isPresent).map(Optional::get).map(CameraProperties::getIndex)
				.map(CameraHelper::getCameraControl).filter(Optional::isPresent).map(Optional::get)
				.forEach(cc -> {
					cc.addIObserver(cameraControlObserver);
					camerasControl.add(cc);
				});
	}

	private Consumer<CameraControllerEvent> consumeExposure = cce -> Display.getDefault()
			.asyncExec(() -> updateExposures(cce.getAcquireTime()));

	private final IObserver cameraControlObserver = CameraEventUtils.cameraControlEventObserver(consumeExposure);

	private void updateExposures() {
		try {
			// Actually ScanningParameters has only a single detector (camera)
			updateExposures(camerasControl.get(0).getAcquireTime());
		} catch (DeviceException e) {
			logger.error("Cannot update caemra exposures", e);
		}
	}

	/**
	 * Updates the scanning parameters detectors exposure time
	 */
	private void updateExposures(double exposure) {
		ScanningParameters tp = getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
		tp.setDetector(new DetectorDocument(tp.getDetector().getName(), exposure));
	}

	// --- temporary solution ---//
	private static IRunnableDeviceService getIRunnableDeviceService() {
		return getRemoteService(IRunnableDeviceService.class);
	}

	private static <T> T getRemoteService(Class<T> klass) {
		IEventService eventService = PlatformUI.getWorkbench().getService(IEventService.class);
		try {
			URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
			return eventService.createRemoteService(jmsURI, klass);
		} catch (Exception e) {
			return null;
		}
	}
	// --- temporary solution ---//
}