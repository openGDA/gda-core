package uk.ac.gda.tomography.scan.editor;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gda.jython.JythonServerFacade;
import uk.ac.gda.tomography.controller.AcquisitionControllerException;
import uk.ac.gda.tomography.model.ActionLog;
import uk.ac.gda.tomography.model.EndAngle;
import uk.ac.gda.tomography.model.ImageCalibration;
import uk.ac.gda.tomography.model.MementoCaretaker;
import uk.ac.gda.tomography.model.MultipleScans;
import uk.ac.gda.tomography.model.MultipleScansType;
import uk.ac.gda.tomography.model.Projections;
import uk.ac.gda.tomography.model.RangeType;
import uk.ac.gda.tomography.model.ScanType;
import uk.ac.gda.tomography.model.StartAngle;
import uk.ac.gda.tomography.model.TomographyAcquisition;
import uk.ac.gda.tomography.model.TomographyConfiguration;
import uk.ac.gda.tomography.scan.editor.view.ItemViewerController;
import uk.ac.gda.tomography.service.TomographyFileService;
import uk.ac.gda.tomography.service.TomographyService;
import uk.ac.gda.tomography.service.TomographyServiceException;
import uk.ac.gda.tomography.service.impl.TomographyServiceImpl;
import uk.ac.gda.tomography.service.message.TomographyRunMessage;

/**
 * The basic controller for Tomography scan.
 *
 * @author Maurizio Nagni
 */
public class TomographyAcquisitionController implements AcquisitionEditorController<TomographyAcquisition> {
	private static final Logger logger = LoggerFactory.getLogger(TomographyAcquisitionController.class);

	private static final String CANNOT_SERIALIZE = "Cannot serialize data";
	private static final String DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL = "tomographyScanModel";
	private static final String TOMOGRAPHY_BASE_DIR = "tomographyBaseDir";
	private static final String TOMOGRAPHY_CONFIGURATIONS_FOLDER = "configurations";
	private static final String TOMOGRAPHY_SCRIPT_FOLDER = "scripts";
	private static final String JSON_EXTENSION = "json";
	private static final String PYTHON_EXTENSION = "py";

	private MementoCaretaker<TomographyAcquisition> acquisitionMemento = new MementoCaretaker<>();
	private TomographyAcquisition tomographyAcquisition;

	// This parameter should be of type IFilePathService,
	// however this package does not have scanning.api as dependency but only, by change may be, uk.ac.gda.core
	// for now is enough to proof the concept
	private TomographyFileService fileService;

	private final TomographyService tomographyService;
	private Object dataConfigurationSource;

	public TomographyAcquisitionController(TomographyAcquisition tomographyAcquisition) {
		this.tomographyAcquisition = tomographyAcquisition;
		this.tomographyService = new TomographyServiceImpl();
		acquisitionMemento.saveState(getAcquisition().save());
	}

	// public TomographyAcquisitionController() {
	// this.tomographyService = new TomographyServiceImpl();
	// }

	 public TomographyAcquisitionController(TomographyService tomographyService) throws TomographyServiceException {
	 this.tomographyService = tomographyService;
	 }

	/**
	 * Returns the data
	 */
	@Override
	public TomographyAcquisition getAcquisition() {
		return tomographyAcquisition;
	}

	@Override
	public void createNewData() {
		tomographyAcquisition = TomographyAcquisitionController.createNewAcquisition();
		dataConfigurationSource = "";
	}

	@Override
	public void loadData(File file) throws AcquisitionControllerException {
		String jsonData;
		try {
			jsonData = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			throw new AcquisitionControllerException("Cannot load the file", e);
		}
		parseJsonData(jsonData, file);
	}

	@Override
	public void loadData(String data) throws AcquisitionControllerException {
		parseJsonData(data, data);
	}

	@Override
	public void loadData(IDialogSettings dialogSettings) throws AcquisitionControllerException {
		final String jsonData = dialogSettings.get(DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL);
		parseJsonData(jsonData, dialogSettings);
	}

	@Override
	public void saveAcquisition() throws AcquisitionControllerException {
		if (Objects.isNull(dataConfigurationSource)) {
			throw new AcquisitionControllerException("Cannot save an empty configuration.");
		}
		saveAcquisitionState(getAcquisition());
		String modelJson;
		try {
			modelJson = dataToJson();
			logger.debug(String.format("Serialized Acquisition: %s", modelJson));
		} catch (JsonProcessingException e) {
			throw new AcquisitionControllerException(CANNOT_SERIALIZE, e);
		}

		if (dataConfigurationSource.getClass().isAssignableFrom(IDialogSettings.class)) {
			IDialogSettings.class.cast(dataConfigurationSource).put(DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL, modelJson);
		}

		if (dataConfigurationSource.getClass().isAssignableFrom(File.class)) {
			Path filePath = File.class.cast(dataConfigurationSource).toPath();
			try {
				Files.delete(filePath);
			} catch (IOException e) {
				throw new AcquisitionControllerException(e);
			}
			File newFile = new File(filePath.toUri());
			try (FileOutputStream fos = new FileOutputStream(newFile)) {
				DataOutputStream outStream = new DataOutputStream(fos);
				outStream.writeUTF(modelJson);
			} catch (IOException e) {
				throw new AcquisitionControllerException("Canot save acquisition configuration on file", e);
			}
		}

		if (dataConfigurationSource.getClass().isAssignableFrom(String.class)) {
			dataConfigurationSource = modelJson;
		}
	}

	@Override
	public int showConfigurationDialog(Display display) {
		return new TomographyScanParameterDialog(display.getActiveShell(), this.getAcquisition().getConfiguration()).open();
	}

	@Override
	public void runAcquisition() {
		try {
			tomographyService.runAcquisition(createTomographyRunMessage(), getAcquisitionScript(), null, null);
		} catch (TomographyServiceException e) {
			logger.error("Error submitting tomoscan to queue", e);
		} catch (JsonProcessingException e) {
			logger.error(CANNOT_SERIALIZE, e);
		}
	}

	@Override
	public Path takeFlatImage() throws AcquisitionControllerException {
		try {
			return tomographyService.takeFlatImage(createTomographyRunMessage(), getAcquisitionScript());
		} catch (TomographyServiceException e) {
			throw new AcquisitionControllerException("Error acquiring Flat Image", e);
		} catch (JsonProcessingException e) {
			throw new AcquisitionControllerException(CANNOT_SERIALIZE, e);
		}
	}

	@Override
	public Path takeDarkImage() throws AcquisitionControllerException {
		try {
			return tomographyService.takeDarkImage(createTomographyRunMessage(), getAcquisitionScript());
		} catch (TomographyServiceException e) {
			throw new AcquisitionControllerException("Error acquiring Dark Image", e);
		} catch (JsonProcessingException e) {
			throw new AcquisitionControllerException(CANNOT_SERIALIZE, e);
		}
	}

	@Override
	public void deleteAcquisition() throws AcquisitionControllerException {
		logger.info("Acquisition deleted (--TBD--)");
	}

	private String dataToJson() throws JsonProcessingException {
		// --- all this should be externalised to a service
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(tomographyAcquisition);
	}

	private void parseJsonData(String jsonData, Object origin) throws AcquisitionControllerException {
		parseJsonData(jsonData);
		dataConfigurationSource = origin;
	}

	private void parseJsonData(String jsonData) throws AcquisitionControllerException {
		ObjectMapper mapper = new ObjectMapper();
			try {
				TomographyConfiguration conf = mapper.readValue(jsonData, TomographyConfiguration.class);
				tomographyAcquisition.setConfiguration(conf);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			}
	}

	private TomographyRunMessage createTomographyRunMessage() throws JsonProcessingException {
		return new TomographyRunMessage(dataToJson());
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

	public static TomographyAcquisition createNewAcquisition() {
		TomographyAcquisition newAcquisition = new TomographyAcquisition();

		TomographyConfiguration newConfiguration = new TomographyConfiguration();
		newConfiguration.setName("Default name");
		newConfiguration.setScanType(ScanType.FLY);
		newConfiguration.setStart(new StartAngle(0.0, false, Double.MIN_VALUE));
		newConfiguration.setEnd(new EndAngle(RangeType.RANGE_180, 1, 0.0));
		newConfiguration.setProjections(new Projections(1, 0));
		newConfiguration.setImageCalibration(new ImageCalibration());

		MultipleScans multipleScan = new MultipleScans();
		multipleScan.setMultipleScansType(MultipleScansType.REPEAT_SCAN);
		multipleScan.setNumberRepetitions(1);
		multipleScan.setWaitingTime(0);
		newConfiguration.setMultipleScans(multipleScan);
		newAcquisition.setConfiguration(newConfiguration);

		newAcquisition.setName(null);
		newAcquisition.setScript(null);
		newAcquisition.setLogs(new ArrayList<>());

		return newAcquisition;
	}

	//--- memento block ---//
	public void saveAcquisitionState(TomographyAcquisition tomoAcquisition) {
		acquisitionMemento.saveState(tomoAcquisition.save());
	}
	public void undoAcquisitionState() {
		this.getAcquisition().undo(acquisitionMemento.undoState());
	}

	public ItemViewerController<ActionLog> getAcquistionLogsController() {
		return new ItemViewerController<ActionLog>(){

			@Override
			public ActionLog createItem() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ActionLog editItem(ActionLog item) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ActionLog deleteItem(ActionLog item) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getItemName(ActionLog item) {
				// TODO Auto-generated method stub
				return null;
			}};
	}
}