package uk.ac.diamond.daq.mapping.ui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.ScanningException;
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
import gda.jython.JythonServerFacade;
import uk.ac.diamond.daq.mapping.api.ScanRequestSavedEvent;
import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.mapping.api.document.ScanRequestFactory;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionRunEvent;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionSaveEvent;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.service.message.ScanningAcquisitionMessage;
import uk.ac.diamond.daq.mapping.ui.services.ScanningAcquisitionFileService;
import uk.ac.diamond.daq.mapping.ui.stage.IStageController;
import uk.ac.diamond.daq.mapping.ui.stage.StageConfiguration;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.Position;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionControllerException;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceSaveEvent;
import uk.ac.gda.api.exception.GDAException;
import uk.ac.gda.client.UIHelper;
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
@Controller
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

	@Override
	public ScanningAcquisition getAcquisition() {
		return acquisition;
	}

	@Override
	public void saveAcquisitionConfiguration() throws AcquisitionControllerException {
		String acquisitionDocument;
		try {
			acquisitionDocument = DocumentMapper.toJSON(getAcquisition());
			save(formatConfigurationFileName(getAcquisition().getName()), acquisitionDocument);
		} catch (GDAException e) {
			throw new AcquisitionControllerException(e);
		}
	}

	@Override
	public void runAcquisition() throws AcquisitionControllerException {
		ScanningAcquisitionMessage tomographyRunMessage = createScanningMessage();
		publishRun(tomographyRunMessage);
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
		ScanningAcquisitionControllerHelper.onApplicationEvent(event, this);
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
			ScanningAcquisitionControllerHelper.updateExposure(this);
			return new ScanningAcquisitionMessage(DocumentMapper.toJSON(getAcquisition()));
		} catch (GDAException | DeviceException e) {
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
		return String.format("TOMOGRAPHY_%s", fn);
	}

	private void save(String fileName, String acquisitionDocument) {
		try {
			Path path = getFileService().saveTextDocument(acquisitionDocument, fileName,
					AcquisitionConfigurationResourceType.TOMO.getExtension());
			SpringApplicationContextProxy
					.publishEvent(new AcquisitionConfigurationResourceSaveEvent(this, path.toUri().toURL()));
			publishScanRequestSavedEvent(fileName);
		} catch (IOException e) {
			UIHelper.showError("Cannot save the configuration", e);
		}
		publishSave(getAcquisition().getName(), acquisitionDocument, getAcquisitionScript().getAbsolutePath());
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

	@SuppressWarnings("unused") // to be used in the near future
	private StageConfiguration generateStageConfiguration(ScanningAcquisition acquisition)
			throws AcquisitionControllerException {
		try {
			ScanningAcquisitionControllerHelper.updateExposure(this);
		} catch (DeviceException e) {
			throw new AcquisitionControllerException("Acquisition cannot acquire the active camera exposure time");
		}
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

	private File getAcquisitionScript() {
		String scriptPath = JythonServerFacade.getInstance().locateScript("tomographyScan.py");
		return new File(scriptPath);
	}

	private ScanningAcquisitionFileService getFileService() {

		if (fileService == null) {
			fileService = new ScanningAcquisitionFileService();
		}
		return fileService;
	}

	private void publishSave(String name, String acquisition, String scriptPath) {
		SpringApplicationContextProxy.publishEvent(new ScanningAcquisitionSaveEvent(this, name, acquisition));
	}

	private void publishRun(ScanningAcquisitionMessage tomographyRunMessage) {
		SpringApplicationContextProxy.publishEvent(new ScanningAcquisitionRunEvent(this, tomographyRunMessage));
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