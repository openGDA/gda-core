package uk.ac.gda.tomography.scan.editor;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gda.jython.JythonServerFacade;
import uk.ac.gda.tomography.controller.TomographyControllerException;
import uk.ac.gda.tomography.model.EndAngle;
import uk.ac.gda.tomography.model.ImageCalibration;
import uk.ac.gda.tomography.model.MultipleScans;
import uk.ac.gda.tomography.model.MultipleScansType;
import uk.ac.gda.tomography.model.Projections;
import uk.ac.gda.tomography.model.RangeType;
import uk.ac.gda.tomography.model.ScanType;
import uk.ac.gda.tomography.model.StartAngle;
import uk.ac.gda.tomography.model.TomographyScanParameters;
import uk.ac.gda.tomography.service.TomographyService;
import uk.ac.gda.tomography.service.TomographyServiceException;
import uk.ac.gda.tomography.service.impl.TomographyServiceImpl;
import uk.ac.gda.tomography.service.message.TomographyRunMessage;

/**
 * The basic controller for Tomography scan.
 *
 * @author Maurizio Nagni
 */
public class TomographyConfigurationController implements ITomographyEditorController<TomographyScanParameters> {
	private static final Logger logger = LoggerFactory.getLogger(TomographyConfigurationController.class);

	private static final String DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL = "tomographyScanModel";

	private TomographyScanParameters data;

	private final TomographyService tomographyService;
	private Object dataConfigurationSource;

	public TomographyConfigurationController() {
		this.tomographyService = new TomographyServiceImpl();
	}

	public TomographyConfigurationController(TomographyService tomographyService) throws TomographyServiceException {
		this.tomographyService = tomographyService;
	}

	/**
	 * Returns the data
	 */
	@Override
	public TomographyScanParameters getData() {
		return data;
	}

	@Override
	public void createNewData() {
		this.data = new TomographyScanParameters();
		this.data.setName("Hello there!");
		this.data.setScanType(ScanType.FLY);
		this.data.setStart(new StartAngle(0.0, false, Double.MIN_VALUE));
		this.data.setEnd(new EndAngle(RangeType.RANGE_180, 1, 0.0));
		this.data.setProjections(new Projections(1, 0));
		this.data.setImageCalibration(new ImageCalibration());
		this.data.setMultipleScans(new MultipleScans(MultipleScansType.REPEAT_SCAN, 1, 0));
		dataConfigurationSource = "";
	}

	@Override
	public void loadData(File file) throws TomographyControllerException {
		String jsonData;
		try {
			jsonData = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			throw new TomographyControllerException("Cannot load the file", e);
		}
		parseJsonData(jsonData, file);
	}

	@Override
	public void loadData(String data) throws TomographyControllerException {
		parseJsonData(data, data);
	}

	@Override
	public void loadData(IDialogSettings dialogSettings) throws TomographyControllerException {
		final String jsonData = dialogSettings.get(DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL);
		parseJsonData(jsonData, dialogSettings);
	}

	@Override
	public void saveData() throws TomographyControllerException {
		String modelJson = dataToJson();
		logger.debug(modelJson);

		if (dataConfigurationSource.getClass().isAssignableFrom(IDialogSettings.class)) {
			IDialogSettings.class.cast(dataConfigurationSource).put(DIALOG_SETTINGS_KEY_TOMOGRAPHY_SCAN_MODEL,
					modelJson);
		}

		if (dataConfigurationSource.getClass().isAssignableFrom(File.class)) {
			Path filePath = File.class.cast(dataConfigurationSource).toPath();
			try {
				Files.delete(filePath);
			} catch (IOException e) {
				throw new TomographyControllerException(e);
			}
			File newFile = new File(filePath.toUri());
			try (FileOutputStream fos = new FileOutputStream(newFile)) {
				DataOutputStream outStream = new DataOutputStream(fos);
				outStream.writeUTF(modelJson);
			} catch (IOException e) {
				throw new TomographyControllerException(e);
			}
		}

		if (dataConfigurationSource.getClass().isAssignableFrom(String.class)) {
			dataConfigurationSource = modelJson;
		}
	}

	@Override
	public void showConfigurationDialog(Display display) {
		new TomographyScanParameterDialog(Display.getDefault().getActiveShell(), this).open();
	}

	@Override
	public void runAcquisition() {
		try {
			tomographyService.runAcquisition(createTomographyRunMessage(), getAcquisitionScript(), null, null);
		} catch (TomographyServiceException e) {
			logger.error("Error submitting tomoscan to queue", e);
		}
	}

	@Override
	public Path takeFlatImage() throws TomographyControllerException {
		try {
			return tomographyService.takeFlatImage(createTomographyRunMessage(), getAcquisitionScript());
		} catch (TomographyServiceException e) {
			throw new TomographyControllerException("Error acquiring Flat Image", e);
		}
	}

	@Override
	public Path takeDarkImage() throws TomographyControllerException {
		try {
			return tomographyService.takeDarkImage(createTomographyRunMessage(), getAcquisitionScript());
		} catch (TomographyServiceException e) {
			throw new TomographyControllerException("Error acquiring Dark Image", e);
		}
	}

	private String dataToJson() {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(data);
	}

	private void parseJsonData(String jsonData, Object origin) throws TomographyControllerException {
		parseJsonData(jsonData);
		dataConfigurationSource = origin;
	}

	private void parseJsonData(String jsonData) throws TomographyControllerException {
		try {
			Gson gson = new GsonBuilder().create();
			data = gson.fromJson(jsonData, TomographyScanParameters.class);
		} catch (Exception e) {
			throw new TomographyControllerException("Cannot parse the jsonDocument", e);
		}
	}

	private TomographyRunMessage createTomographyRunMessage() {
		return new TomographyRunMessage(dataToJson());
	}

	private File getAcquisitionScript() {
		String scriptPath = JythonServerFacade.getInstance().locateScript("tomographyScan.py");
		return new File(scriptPath);
	}

}