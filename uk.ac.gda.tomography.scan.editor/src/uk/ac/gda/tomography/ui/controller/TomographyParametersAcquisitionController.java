package uk.ac.gda.tomography.ui.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gda.jython.JythonServerFacade;
import uk.ac.diamond.daq.experiment.api.Services;
import uk.ac.diamond.daq.experiment.api.TriggerableScan;
import uk.ac.gda.tomography.base.TomographyConfiguration;
import uk.ac.gda.tomography.base.TomographyMode;
import uk.ac.gda.tomography.base.TomographyParameterAcquisition;
import uk.ac.gda.tomography.base.TomographyParameters;
import uk.ac.gda.tomography.controller.AcquisitionController;
import uk.ac.gda.tomography.controller.AcquisitionControllerException;
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
import uk.ac.gda.tomography.triggerable.TriggerableTomography;
import uk.ac.gda.tomography.ui.StageConfiguration;

/**
 * The basic controller for Tomography scan.
 *
 * @author Maurizio Nagni
 */
public class TomographyParametersAcquisitionController implements AcquisitionController<TomographyParameterAcquisition> {
	private static final Logger logger = LoggerFactory.getLogger(TomographyParametersAcquisitionController.class);

	private static final String CANNOT_SERIALIZE = "Cannot serialize data";
	private static final String TOMOGRAPHY_BASE_DIR = "tomographyBaseDir";
	private static final String TOMOGRAPHY_CONFIGURATIONS_FOLDER = "configurations";
	private static final String TOMOGRAPHY_SCRIPT_FOLDER = "scripts";
	private static final String JSON_EXTENSION = "json";
	private static final String PYTHON_EXTENSION = "py";

	public enum Positions {
		DEFAULT, OUT_OF_BEAM, START, END;
	}

	public enum METADATA {
		EXPOSURE;
	}

	private TomographyMode tomographyMode;
	private TomographyParameterAcquisition acquisition;
	private final Map<Positions, Set<DevicePosition<Double>>> motorsPosition = new EnumMap<>(Positions.class);

	// This parameter should be of type IFilePathService,
	// however this package does not have scanning.api as dependency but only, by change may be, uk.ac.gda.core
	// for now is enough to proof the concept
	private TomographyFileService fileService;

	private final TomographyService tomographyService;
	private Object dataConfigurationSource;

	public TomographyParametersAcquisitionController() {
		this.tomographyService = new TomographyServiceImpl();
	}

	public TomographyParametersAcquisitionController(TomographyService tomographyService) {
		this.tomographyService = tomographyService;
	}

	@Override
	public void loadData(URL data) throws AcquisitionControllerException {
		String jsonData;
		try {
			StringBuilder sb = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(data.openStream()))) {
				String line;
				while ((line = reader.readLine()) != null)
					sb.append(line);
			}
			jsonData = sb.toString();
		} catch (IOException e) {
			throw new AcquisitionControllerException("Cannot load the file", e);
		}
		parseJsonData(jsonData, data);
	}

	@Override
	public void loadData(String data) throws AcquisitionControllerException {
		parseJsonData(data, data);
	}

	@Override
	public void loadData(IDialogSettings dialogSettings, String key) throws AcquisitionControllerException {
		final String jsonData = dialogSettings.get(key);
		parseJsonData(jsonData, dialogSettings);
	}

	@Override
	public void loadData(TomographyParameterAcquisition data) throws AcquisitionControllerException {
		this.acquisition = data;
	}

	@Override
	public void saveAcquisitionAsFile(final TomographyParameterAcquisition acquisition, URL destination) throws AcquisitionControllerException {
		StageConfiguration sc = generateStageConfiguration(acquisition);
		if (requiresOutOfBeamPosition(sc)) {
			throw new AcquisitionControllerException("Acquisition needs a OutOfBeam position to acquire flat images");
		}
		TriggerableScan triggerableTomo = new TriggerableTomography(dataToJson(generateStageConfiguration(acquisition)),
				getAcquisitionScript().getAbsolutePath());
		Services.getExperimentService().saveScan(triggerableTomo, acquisition.getAcquisitionConfiguration().getAcquisitionParameters().getName(), null); // FIXME visit?)
//		Path filePath = File.class.cast(dataConfigurationSource).toPath();
//		try {
//			Files.delete(filePath);
//		} catch (IOException e) {
//			throw new AcquisitionControllerException(e);
//		}
//		File newFile;
//		try {
//			newFile = new File(destination.toURI());
//		} catch (URISyntaxException e) {
//			throw new AcquisitionControllerException("Canot save acquisition configuration on file", e);
//		}
//		try (FileOutputStream fos = new FileOutputStream(newFile)) {
//			DataOutputStream outStream = new DataOutputStream(fos);
//			outStream.writeUTF(dataToJson(generateStageConfiguration(acquisition)));
//		} catch (IOException e) {
//			throw new AcquisitionControllerException("Canot save acquisition configuration on file", e);
//		}
	}

	private void populateMetadata() {
		getTomographyMode().getMetadata().put(METADATA.EXPOSURE.name(), Double.toString(0.1));
	}

	@Override
	public void saveAcquisitionAsIDialogSettings(TomographyParameterAcquisition acquisition, IDialogSettings destination, String key)
			throws AcquisitionControllerException {
		destination.put(key, dataToJson(acquisition));
	}

	@Override
	public void runAcquisition(TomographyParameterAcquisition acquisition) throws AcquisitionControllerException {
		try {
			StageConfiguration sc = generateStageConfiguration(acquisition);
			if (requiresOutOfBeamPosition(sc)) {
				throw new AcquisitionControllerException("Acquisition needs a OutOfBeam position to acquire flat images");
			}
			tomographyService.runAcquisition(createTomographyRunMessage(generateStageConfiguration(acquisition)), getAcquisitionScript(), null, null);
		} catch (TomographyServiceException e) {
			logger.error("Error submitting tomoscan to queue", e);
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
		return ((ic.getNumberFlat() > 0 && (ic.isAfterAcquisition() || ic.isBeforeAcquisition())) && !sc.getMotorPositions().containsKey(Positions.OUT_OF_BEAM));
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
	public void deleteAcquisition(TomographyParameterAcquisition acquisition) throws AcquisitionControllerException {
		logger.info("Acquisition deleted (--TBD--)");
	}

	@Override
	public TomographyParameterAcquisition getAcquisition() {
		return acquisition;
	}

	public TomographyMode getTomographyMode() {
		return tomographyMode;
	}

	public void setTomographyMode(TomographyMode tomographyMode) {
		this.tomographyMode = tomographyMode;
	}

	public Set<DevicePosition<Double>> savePosition(Positions position) {
		motorsPosition.put(position, getTomographyMode().getMotorsPosition());
		return motorsPosition.get(position);
	}

	private StageConfiguration generateStageConfiguration(TomographyParameterAcquisition acquisition) {
		populateMetadata();
		savePosition(Positions.START);
		return new StageConfiguration(acquisition, getTomographyMode(), getMotorsPositions());
	}

	private String dataToJson(TomographyParameterAcquisition acquisition) throws AcquisitionControllerException {
		return intDataToJson(acquisition);
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

	private void parseJsonData(String jsonData, Object origin) throws AcquisitionControllerException {
		parseJsonData(jsonData);
		dataConfigurationSource = origin;
	}

	protected TomographyParameterAcquisition parseJsonData(String jsonData) throws AcquisitionControllerException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(jsonData, TomographyParameterAcquisition.class);
		} catch (JsonParseException e) {
			logger.error("TODO put description of error here", e);
		} catch (JsonMappingException e) {
			logger.error("TODO put description of error here", e);
		} catch (IOException e) {
			logger.error("TODO put description of error here", e);
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

	private Set<Path> getConfigurations() {
		return getPaths(TOMOGRAPHY_SCRIPT_FOLDER);
	}

	private Set<Path> getScripts() {
		return getPaths(TOMOGRAPHY_SCRIPT_FOLDER);
	}

	private Set<Path> getPaths(String subfolder) {
		return fileService.getPaths(subfolder).collect(Collectors.toSet());
	}

	private TomographyFileService getFileService() {
		if (Objects.isNull(fileService)) {
			fileService = new TomographyFileService(TOMOGRAPHY_BASE_DIR);
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
}