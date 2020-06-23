package uk.ac.gda.tomography.ui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.jython.JythonServerFacade;
import uk.ac.diamond.daq.mapping.api.ScanRequestSavedEvent;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.DetectorDocument;
import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.mapping.api.document.ScanRequestFactory;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.diamond.daq.mapping.ui.properties.DetectorHelper;
import uk.ac.diamond.daq.mapping.ui.properties.DetectorHelper.AcquisitionType;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionControllerException;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceSaveEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.properties.DetectorProperties;
import uk.ac.gda.tomography.base.TomographyConfiguration;
import uk.ac.gda.tomography.base.TomographyParameterAcquisition;
import uk.ac.gda.tomography.base.TomographyParameters;
import uk.ac.gda.tomography.event.TomographyRunAcquisitionEvent;
import uk.ac.gda.tomography.event.TomographySaveEvent;
import uk.ac.gda.tomography.model.ImageCalibration;
import uk.ac.gda.tomography.model.MultipleScans;
import uk.ac.gda.tomography.model.MultipleScansType;
import uk.ac.gda.tomography.model.ScanType;
import uk.ac.gda.tomography.service.TomographyFileService;
import uk.ac.gda.tomography.service.message.TomographyRunMessage;
import uk.ac.gda.tomography.stage.IStageController;
import uk.ac.gda.tomography.stage.StageConfiguration;
import uk.ac.gda.tomography.stage.enumeration.Position;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * The basic controller for Tomography scan. Uses Spring to publish
 * <ul>
 * <li>{@link TomographySaveEvent} when an acquisition configuration is saved</li>
 * <li>{@link TomographyRunAcquisitionEvent} when an acquisition starts</li>
 * </ul>
 *
 * @author Maurizio Nagni
 */
@Component
public class TomographyParametersAcquisitionController implements AcquisitionController<TomographyParameterAcquisition>, ApplicationListener<ApplicationEvent> {
	private static final Logger logger = LoggerFactory.getLogger(TomographyParametersAcquisitionController.class);

	@Autowired
	private IStageController stageController;
	private TomographyParameterAcquisition acquisition;

	private TomographyFileService fileService;
	// @Autowired
	// private TomographyService tomographyService;

	@Autowired
	private DocumentMapper documentMapper;

	public TomographyParametersAcquisitionController() {

	}

	@Override
	public TomographyParameterAcquisition getAcquisition() {
		if (acquisition == null) {
			acquisition = TomographyParametersAcquisitionController.createNewAcquisition();
		}
		return acquisition;
	}

	@Override
	public void saveAcquisitionConfiguration() throws AcquisitionControllerException {
		StageConfiguration sc = generateStageConfiguration(getAcquisition());
		String acquisitionDocument = dataToJson(sc);
		save(formatConfigurationFileName(getAcquisition().getName()), acquisitionDocument);
	}

	@Override
	public void runAcquisition(URL outputPath) throws AcquisitionControllerException {
		runAcquisition();
	}

	@Override
	public void runAcquisition() throws AcquisitionControllerException {
		TomographyRunMessage tomographyRunMessage = createTomographyRunMessage();
		publishRun(tomographyRunMessage);
	}

	@Override
	public void loadAcquisitionConfiguration(URL url) throws AcquisitionControllerException {
		acquisition = parseAcquisitionConfiguration(url).getResource();
		SpringApplicationContextProxy.publishEvent(new AcquisitionConfigurationResourceLoadEvent(this, url));
	}

	@Override
	public void loadAcquisitionConfiguration(TomographyParameterAcquisition acquisition) throws AcquisitionControllerException {
		this.acquisition = acquisition;
	}

	@Override
	public AcquisitionConfigurationResource<TomographyParameterAcquisition> parseAcquisitionConfiguration(URL url) throws AcquisitionControllerException {
		return new AcquisitionConfigurationResource(url, parseJsonData(getAcquisitionBytes(url)).getAcquisition());
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		TomographyParametersAcquisitionControllerHelper.onApplicationEvent(event, this);
	}

	@Override
	public void deleteAcquisitionConfiguration(URL url) throws AcquisitionControllerException {
		throw new AcquisitionControllerException("Delete not implemented");
		// SpringApplicationContextProxy.publishEvent(new AcquisitionConfigurationResourceSaveEvent(url));
	}

	public static TomographyParameterAcquisition createNewAcquisition() {
		TomographyParameterAcquisition newConfiguration = new TomographyParameterAcquisition();
		newConfiguration.setAcquisitionConfiguration(new TomographyConfiguration());
		newConfiguration.setName("Default name");
		TomographyParameters acquisitionParameters = new TomographyParameters();
		Optional<List<DetectorProperties>> dp = DetectorHelper.getAcquistionDetector(AcquisitionType.TOMOGRAPHY);
		int index = 0; // in future may be parametrised
		if (dp.isPresent()) {
			DetectorDocument dd = new DetectorDocument(dp.get().get(index).getDetectorBean(), 0);
			acquisitionParameters.setDetector(dd);
		}
		acquisitionParameters.setScanType(ScanType.FLY);
		acquisitionParameters.setImageCalibration(new ImageCalibration());

		ScanpathDocument.Builder scanpathBuilder = new ScanpathDocument.Builder();
		scanpathBuilder.withModelDocument(AcquisitionTemplateType.ONE_DIMENSION_LINE);
		ScannableTrackDocument.Builder scannableTrackBuilder = new ScannableTrackDocument.Builder();
		scannableTrackBuilder.withStart(0.0);
		scannableTrackBuilder.withStop(180.0);
		scannableTrackBuilder.withPoints(1);
		List<ScannableTrackDocument> scannableTrackDocuments = new ArrayList<>();
		scannableTrackDocuments.add(scannableTrackBuilder.build());
		scanpathBuilder.withScannableTrackDocuments(scannableTrackDocuments);
		acquisitionParameters.setScanpathDocument(scanpathBuilder.build());

		MultipleScans multipleScan = new MultipleScans();
		multipleScan.setMultipleScansType(MultipleScansType.REPEAT_SCAN);
		multipleScan.setNumberRepetitions(1);
		multipleScan.setWaitingTime(0);
		acquisitionParameters.setMultipleScans(multipleScan);

		newConfiguration.getAcquisitionConfiguration().setAcquisitionParameters(acquisitionParameters);
		return newConfiguration;
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
			Path path = getFileService().saveTextDocument(acquisitionDocument, fileName, AcquisitionConfigurationResourceType.TOMO.getExtension());
			SpringApplicationContextProxy.publishEvent(new AcquisitionConfigurationResourceSaveEvent(this, path.toUri().toURL()));
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
		ImageCalibration ic = sc.getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters().getImageCalibration();
		return ((ic.getNumberFlat() > 0 && (ic.isAfterAcquisition() || ic.isBeforeAcquisition()))
				&& !sc.getMotorsPositions().containsKey(Position.OUT_OF_BEAM));
	}

	public TomographyParameters getAcquisitionParameters() {
		return getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
	}

	private StageConfiguration generateStageConfiguration(TomographyParameterAcquisition acquisition) throws AcquisitionControllerException {
		try {
			TomographyParametersAcquisitionControllerHelper.updateExposure(this);
		} catch (DeviceException e) {
			throw new AcquisitionControllerException("Acquisition cannot acquire the active camera exposure time");
		}
		stageController.savePosition(Position.START);
		StageConfiguration sc = new StageConfiguration(acquisition, stageController.getStageDescription(), stageController.getMotorsPositions());
		if (requiresOutOfBeamPosition(sc)) {
			throw new AcquisitionControllerException("Acquisition needs a OutOfBeam position to acquire flat images");
		}
		return sc;
	}

	private String dataToJson(StageConfiguration acquisition) throws AcquisitionControllerException {
		return intDataToJson(acquisition);
	}

	private String intDataToJson(Object acquisition) throws AcquisitionControllerException {
		// --- all this should be externalised to a service
		try {
			String json = documentMapper.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(acquisition);
			logger.debug(String.format("Saving acquisition: %s", json));
			return json;
		} catch (JsonProcessingException e) {
			throw new AcquisitionControllerException("Cannot parse json document", e);
		}
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


	private TomographyRunMessage createTomographyRunMessage() throws AcquisitionControllerException {
		return new TomographyRunMessage(intDataToJson(getAcquisition()));
	}

	private File getAcquisitionScript() {
		String scriptPath = JythonServerFacade.getInstance().locateScript("tomographyScan.py");
		return new File(scriptPath);
	}

	private TomographyFileService getFileService() {

		if (fileService == null) {
			fileService = new TomographyFileService();
		}
		return fileService;
	}

	private void publishSave(String name, String acquisition, String scriptPath) {
		SpringApplicationContextProxy.publishEvent(new TomographySaveEvent(this, name, acquisition, scriptPath));
	}

	private void publishRun(TomographyRunMessage tomographyRunMessage) {
		SpringApplicationContextProxy.publishEvent(new TomographyRunAcquisitionEvent(this, tomographyRunMessage));
	}

	private byte[] getAcquisitionBytes(URL url) throws AcquisitionControllerException {
		try {
			return getFileService().loadFileAsBytes(url);
		} catch (IOException e) {
			throw new AcquisitionControllerException("Cannot load the file", e);
		}
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