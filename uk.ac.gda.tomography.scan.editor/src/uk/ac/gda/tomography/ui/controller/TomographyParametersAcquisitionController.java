package uk.ac.gda.tomography.ui.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gda.device.DeviceException;
import gda.jython.JythonServerFacade;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.tomography.base.TomographyConfiguration;
import uk.ac.gda.tomography.base.TomographyParameterAcquisition;
import uk.ac.gda.tomography.base.TomographyParameters;
import uk.ac.gda.tomography.controller.AcquisitionController;
import uk.ac.gda.tomography.controller.AcquisitionControllerException;
import uk.ac.gda.tomography.event.TomographyRunAcquisitionEvent;
import uk.ac.gda.tomography.event.TomographySaveEvent;
import uk.ac.gda.tomography.model.DevicePosition;
import uk.ac.gda.tomography.model.EndAngle;
import uk.ac.gda.tomography.model.ImageCalibration;
import uk.ac.gda.tomography.model.MultipleScans;
import uk.ac.gda.tomography.model.MultipleScansType;
import uk.ac.gda.tomography.model.Projections;
import uk.ac.gda.tomography.model.RangeType;
import uk.ac.gda.tomography.model.ScanType;
import uk.ac.gda.tomography.model.StartAngle;
import uk.ac.gda.tomography.service.TomographyFileService;
import uk.ac.gda.tomography.service.TomographyService;
import uk.ac.gda.tomography.service.TomographyServiceException;
import uk.ac.gda.tomography.service.impl.TomographyServiceImpl;
import uk.ac.gda.tomography.service.message.TomographyRunMessage;
import uk.ac.gda.tomography.ui.StageConfiguration;
import uk.ac.gda.tomography.ui.mode.StageDescription;
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
@Controller
public class TomographyParametersAcquisitionController implements AcquisitionController<TomographyParameterAcquisition>, ApplicationListener<ApplicationEvent> {
	private static final Logger logger = LoggerFactory.getLogger(TomographyParametersAcquisitionController.class);

	public enum Positions {
		DEFAULT, OUT_OF_BEAM, START, END;
	}

	public enum METADATA {
		EXPOSURE;
	}

	private StageDescription stageDescription;
	private TomographyParameterAcquisition acquisition;
	private final Map<Positions, Set<DevicePosition<Double>>> motorsPosition = new EnumMap<>(Positions.class);

	private TomographyFileService fileService;

	private final TomographyService tomographyService;

	public TomographyParametersAcquisitionController() {
		this.tomographyService = new TomographyServiceImpl();
	}

	public TomographyParametersAcquisitionController(TomographyService tomographyService) {
		this.tomographyService = tomographyService;
	}

	@Override
	public void loadAcquisitionConfiguration(String configurationFile) throws AcquisitionControllerException {
		try {
			byte[] data = getFileService().loadFileAsBytes(formatConfigurationFileName(configurationFile), "json");
			StageConfiguration stageConfiguration = parseJsonData(data);
			acquisition = stageConfiguration.getAcquisition();
		} catch (IOException e) {
			throw new AcquisitionControllerException("Cannot load the file", e);
		}
	}

	@Override
	public void saveAcquisitionConfiguration() throws AcquisitionControllerException {
		StageConfiguration sc = generateStageConfiguration(getAcquisition());
		String acquisitionDocument = dataToJson(sc);
		save(formatConfigurationFileName(getAcquisitionParameters().getName()), acquisitionDocument);
	}

	private String formatConfigurationFileName(String fileName) {
		return String.format("TOMOGRAPHY_%s", Optional.ofNullable(fileName.replaceAll("\\s", "")).orElse("noNameConfiguration"));
	}

	@Override
	public void runAcquisition() throws AcquisitionControllerException {
		try {
			StageConfiguration sc = generateStageConfiguration(getAcquisition());
			TomographyRunMessage tomographyRunMessage = createTomographyRunMessage(sc);
			tomographyService.runAcquisition(tomographyRunMessage, getAcquisitionScript(), null, null);
			publishRun(tomographyRunMessage);
		} catch (TomographyServiceException e) {
			logger.error("Error submitting tomoscan to queue", e);
		}
	}

	private void save(String fileName, String acquisitionDocument) {
		try {
			getFileService().saveTextDocument(acquisitionDocument, fileName, TomographyFileService.JSON_EXTENSION);
		} catch (IOException e) {
			UIHelper.showError("Cannot save the configuration", e);
		}
		publishSave(getAcquisitionParameters().getName(), acquisitionDocument, getAcquisitionScript().getAbsolutePath());
	}

	private void populateMetadata() {
		getStageDescription().getMetadata().put(METADATA.EXPOSURE.name(), Double.toString(0.1));
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
				&& !sc.getMotorsPositions().containsKey(Positions.OUT_OF_BEAM));
	}

	// @Override
	public URL takeFlatImage(TomographyParameterAcquisition acquisition) throws AcquisitionControllerException {
		try {
			return tomographyService.takeFlatImage(createTomographyRunMessage(generateStageConfiguration(acquisition)), getAcquisitionScript());
		} catch (TomographyServiceException e) {
			throw new AcquisitionControllerException("Error acquiring Flat Image", e);
		}
	}

	// @Override
	public URL takeDarkImage(TomographyParameterAcquisition acquisition) throws AcquisitionControllerException {
		try {
			return tomographyService.takeDarkImage(createTomographyRunMessage(generateStageConfiguration(acquisition)), getAcquisitionScript());
		} catch (TomographyServiceException e) {
			throw new AcquisitionControllerException("Error acquiring Dark Image", e);
		}
	}

	@Override
	public TomographyParameterAcquisition getAcquisition() {
		if (acquisition == null) {
			acquisition = TomographyParametersAcquisitionController.createNewAcquisition();
		}
		return acquisition;
	}

	public StageDescription getStageDescription() {
		return stageDescription;
	}

	public void setStageDescription(StageDescription stageDescription) {
		this.stageDescription = stageDescription;
	}

	public Set<DevicePosition<Double>> savePosition(Positions position) {
		motorsPosition.put(position, getStageDescription().getMotorsPosition());
		return motorsPosition.get(position);
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
		populateMetadata();
		savePosition(Positions.START);
		StageConfiguration sc = new StageConfiguration(acquisition, getStageDescription(), getMotorsPositions());
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
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(acquisition);
			logger.debug(String.format("Saving acquisition: %s", json));
			return json;
		} catch (JsonProcessingException e) {
			throw new AcquisitionControllerException("Cannot parse json document", e);
		}
	}

	private Map<Positions, Set<DevicePosition<Double>>> getMotorsPositions() {
		return motorsPosition;
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

	private TomographyRunMessage createTomographyRunMessage(StageConfiguration acquisition) throws AcquisitionControllerException {
		return new TomographyRunMessage(dataToJson(acquisition));
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

	public static TomographyParameterAcquisition createNewAcquisition() {
		TomographyParameterAcquisition newConfiguration = new TomographyParameterAcquisition();
		newConfiguration.setAcquisitionConfiguration(new TomographyConfiguration());
		TomographyParameters tp = new TomographyParameters();

		tp.setName("Default name");
		tp.setScanType(ScanType.FLY);
		tp.setStart(new StartAngle(0.0, false, Double.MIN_VALUE));
		tp.setEnd(new EndAngle(RangeType.RANGE_180, 1, 0.0));
		tp.setProjections(new Projections(1, 0));
		tp.setImageCalibration(new ImageCalibration());

		MultipleScans multipleScan = new MultipleScans();
		multipleScan.setMultipleScansType(MultipleScansType.REPEAT_SCAN);
		multipleScan.setNumberRepetitions(1);
		multipleScan.setWaitingTime(0);
		tp.setMultipleScans(multipleScan);

		newConfiguration.getAcquisitionConfiguration().setAcquisitionParameters(tp);
		return newConfiguration;
	}

	private void publishSave(String name, String acquisition, String scriptPath) {
		SpringApplicationContextProxy.publishEvent(new TomographySaveEvent(this, name, acquisition, scriptPath));
	}

	private void publishRun(TomographyRunMessage tomographyRunMessage) {
		SpringApplicationContextProxy.publishEvent(new TomographyRunAcquisitionEvent(this, tomographyRunMessage));
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		TomographyParametersAcquisitionControllerHelper.onApplicationEvent(event, this);
	}

	@Override
	public void deleteAcquisitionConfiguration(String data) throws AcquisitionControllerException {
		throw new AcquisitionControllerException("Delete not implemented");
	}
}