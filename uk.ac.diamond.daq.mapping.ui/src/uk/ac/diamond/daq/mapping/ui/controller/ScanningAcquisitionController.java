package uk.ac.diamond.daq.mapping.ui.controller;

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;
import static uk.ac.gda.ui.tool.rest.ClientRestServices.getScanningAcquisitionRestServiceClient;

import java.util.Optional;
import java.util.UUID;

import org.dawnsci.mapping.ui.Activator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import gda.data.metadata.SampleMetadataService;
import uk.ac.diamond.daq.mapping.api.document.helper.ImageCalibrationHelper;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.AcquisitionReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.ImageCalibrationReader;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.AcquisitionSubType;
import uk.ac.gda.api.acquisition.TrajectoryShape;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceDeleteEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceSaveEvent;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;
import uk.ac.gda.client.AcquisitionManager;
import uk.ac.gda.client.exception.AcquisitionControllerException;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.ui.tool.controller.AcquisitionController;
import uk.ac.gda.ui.tool.rest.ConfigurationsRestServiceClient;

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
public class ScanningAcquisitionController implements AcquisitionController<ScanningAcquisition> {
	private static final Logger logger = LoggerFactory.getLogger(ScanningAcquisitionController.class);

	public static final String DEFAULT_CONFIGURATION_NAME = "UntitledConfiguration";

	private ConfigurationsRestServiceClient configurationService;
	private PositionManager positionManager;
	private SampleMetadataService sampleMetadataService;

	private AcquisitionManager acquisitionManager;

	public ScanningAcquisitionController(
			@Autowired ConfigurationsRestServiceClient configurationService,
			@Autowired PositionManager positionManager,
			@Autowired SampleMetadataService sampleMetadataService) {

		this.configurationService = configurationService;
		this.positionManager = positionManager;
		this.sampleMetadataService = sampleMetadataService;
	}

	private AcquisitionManager getAcquisitionManager() {
		if (acquisitionManager == null) {
			acquisitionManager = Activator.getService(AcquisitionManager.class);
		}
		return acquisitionManager;
	}

	/**
	 * For tests only.
	 */
	protected void setAcquisitionManager(AcquisitionManager acquisitionManager) {
		this.acquisitionManager = acquisitionManager;
	}

	private ScanningAcquisition acquisition;

	private ImageCalibrationHelper imageCalibrationHelper;

	private AcquisitionReader acquisitionReader;

	@Override
	public ScanningAcquisition getAcquisition() {
		return acquisition;
	}

	@Override
	public void saveAcquisitionConfiguration() throws AcquisitionControllerException {
		finalizeAcquisition();
		removeSampleName();
		save();
	}

	@Override
	public RunAcquisitionResponse runAcquisition() throws AcquisitionControllerException {
		insertSampleName();
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
		String message = response != null ? response.getMessage() : "Unknown error";
		throw new AcquisitionControllerException(message);
	}

	@Override
	public void loadAcquisitionConfiguration(ScanningAcquisition acquisition) throws AcquisitionControllerException {
		getAcquisitionManager().register(acquisition);
		updateAcquisitionConfiguration(acquisition);
	}

	@Override
	public void initialise(AcquisitionKeys keys) throws AcquisitionControllerException {
		updateAcquisitionConfiguration(getAcquisitionManager().getAcquisition(keys));
	}

	/**
	 * Handles the case when a new {@link ScanningAcquisition} has to be created from its identifying {@link AcquisitionKeys}
	 * @param acquisitionKeys
	 * @throws AcquisitionControllerException
	 */
	private void updateAcquisitionConfiguration(ScanningAcquisition acquisition) {
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
		} catch (GDAClientRestException e) {
			logger.error("Cannot delete scanning acquisiton", e);
		}
	}

	@Override
	public void newScanningAcquisition(AcquisitionKeys acquisitionKeys) throws AcquisitionControllerException {
		updateAcquisitionConfiguration(getAcquisitionManager().newAcquisition(acquisitionKeys));
	}

	@Override
	public AcquisitionKeys getAcquisitionKeys() {
		return acquisition.getKey();
	}

	private void save() throws AcquisitionControllerException {
		ScanningAcquisition savedAcquisition = null;
		AcquisitionConfigurationResourceType type = AcquisitionConfigurationResourceType.DEFAULT;
		try {
			if (AcquisitionPropertyType.TOMOGRAPHY.equals(getAcquisitionKeys().getPropertyType())) {
				savedAcquisition = configurationService.insertImaging(getAcquisition());
				type = AcquisitionConfigurationResourceType.TOMO;
			} else if (AcquisitionPropertyType.DIFFRACTION.equals(getAcquisitionKeys().getPropertyType())) {
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
	private void finalizeAcquisition() {
		updateStartPosition();
		updateImageCalibrationStartPosition();
	}
	private void insertSampleName() {
		var name = sampleMetadataService.getSampleName();
		if (name.isBlank()) {
			var dialog = new SampleNameRequiredDialog(Display.getCurrent().getActiveShell());
			dialog.open();
			name = dialog.getName();
			sampleMetadataService.setSampleName(name);
		}
		getAcquisition().setDescription(name);
	}

	/**
	 * Important to remove the sample name before saving to file,
	 * as these acquisitions are commonly repeated across a series of samples
	 * and therefore the saved name would become outdated.
	 */
	private void removeSampleName() {
		getAcquisition().setDescription("");
	}
	/**
	 * When no sample name is found, this dialog can be opened
	 * to prompt the user to correct that.
	 *
	 * This is rather forceful, as the dialog will not close
	 * until the user writes something.
	 */
	private class SampleNameRequiredDialog extends Dialog {

		private String name;

		protected SampleNameRequiredDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Sample name required");
		}

		@Override
		protected Point getInitialSize() {
			return new Point(550,  150);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			var composite = (Composite) super.createDialogArea(parent);
			GridLayoutFactory.swtDefaults().numColumns(2).margins(20, 10).applyTo(composite);

			new Label(composite, SWT.NONE).setText("Sample name:");
			var nameBox = new Text(composite, SWT.BORDER);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(nameBox);

			var warning = new ControlDecoration(nameBox, SWT.TOP | SWT.RIGHT);

			Image image = FieldDecorationRegistry.getDefault()
							.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
							.getImage();

			warning.setDescriptionText("Sample name cannot be blank.");
			warning.setImage(image);
			warning.setMarginWidth(5);

			nameBox.addListener(SWT.Modify, event -> {
				name = nameBox.getText();
				toggleOKButton(nameBox.getText());
				if (validInput(name)) {
					warning.hide();
				} else {
					warning.show();
				}
			});

			return composite;
		}

		private boolean validInput(String text) {
			return text != null && !text.isBlank();
		}

		/** disable OK button if name is empty */
		private void toggleOKButton(String text) {
			getButton(IDialogConstants.OK_ID).setEnabled(validInput(text));
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			// create only the OK button
			createButton(parent, OK, "OK", true);
			toggleOKButton("");
		}

		public String getName() {
			return name;
		}

		@Override
		public boolean close() {
			if (validInput(name)) {
				return super.close();
			}
			// simply don't close with invalid input
			return false;
		}

	}



	private void updateStartPosition() {
		var startPosition = positionManager.getStartPosition(getAcquisitionKeys());
		acquisition.getAcquisitionConfiguration().getAcquisitionParameters().setStartPosition(startPosition);

		// return scannables to start positions after the scan
		acquisition.getAcquisitionConfiguration().setEndPosition(startPosition);
	}

	private void updateImageCalibrationStartPosition() {
		ImageCalibrationReader calibration = getAcquisitionReader().getAcquisitionConfiguration().getImageCalibration();

		if (calibration.getFlatCalibration().getNumberExposures() > 0) {
			var flatFieldKey = new AcquisitionKeys(AcquisitionPropertyType.CALIBRATION, AcquisitionSubType.FLAT, TrajectoryShape.STATIC_POINT);
			var positionForFlats = positionManager.getStartPosition(flatFieldKey);
			getImageCalibrationHelper().updateFlatDetectorPositionDocument(positionForFlats);
		}

		if (calibration.getDarkCalibration().getNumberExposures() > 0) {
			var darkFieldKey = new AcquisitionKeys(AcquisitionPropertyType.CALIBRATION, AcquisitionSubType.DARK, TrajectoryShape.STATIC_POINT);
			var positionForDarks = positionManager.getStartPosition(darkFieldKey);
			getImageCalibrationHelper().updateDarkDetectorPositionDocument(positionForDarks);
		}
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
