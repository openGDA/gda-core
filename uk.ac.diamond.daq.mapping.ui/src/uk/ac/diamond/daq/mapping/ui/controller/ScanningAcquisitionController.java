package uk.ac.diamond.daq.mapping.ui.controller;

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.diamond.daq.mapping.api.ScanRequestSavedEvent;
import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.mapping.api.document.ScanRequestFactory;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionRunEvent;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionSaveEvent;
import uk.ac.diamond.daq.mapping.api.document.helper.ImageCalibrationHelper;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.AcquisitionReader;
import uk.ac.diamond.daq.mapping.api.document.helper.reader.ImageCalibrationReader;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.service.message.ScanningAcquisitionMessage;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper.AcquisitionPropertyType;
import uk.ac.diamond.daq.mapping.ui.properties.stages.ManagedScannable;
import uk.ac.diamond.daq.mapping.ui.services.ScanningAcquisitionFileService;
import uk.ac.diamond.daq.mapping.ui.stage.BeamSelector;
import uk.ac.diamond.daq.mapping.ui.stage.StageConfiguration;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.Position;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionControllerException;
import uk.ac.gda.api.acquisition.configuration.processing.DiffractionCalibrationMergeRequest;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.api.acquisition.configuration.processing.SavuProcessingRequest;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceSaveEvent;
import uk.ac.gda.api.exception.GDAException;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.core.tool.spring.DiffractionContextFile;
import uk.ac.gda.core.tool.spring.TomographyContextFile;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;

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
		implements AcquisitionController<ScanningAcquisition> {
	private static final Logger logger = LoggerFactory.getLogger(ScanningAcquisitionController.class);

	@Autowired
	private AcquisitionFileContext fileContext;

	@Autowired
	private StageController stageController;

	@Autowired
	private ClientRemoteServices remoteServices;

	private ScanningAcquisition acquisition;

	private ScanningAcquisitionFileService fileService;

	private Supplier<ScanningAcquisition> newAcquisitionSupplier;

	private AcquisitionPropertyType acquisitionType;

	private ImageCalibrationHelper imageCalibrationHelper;
	private ScanningAcquisitionControllerDetectorHelper detectorsHelper;

	private AcquisitionReader acquisitionReader;

	public ScanningAcquisitionController() {
		super();
		this.acquisitionType = AcquisitionPropertyType.DEFAULT;
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
		this.acquisitionType = acquisitionType;
	}

	@Override
	public ScanningAcquisition getAcquisition() {
		return acquisition;
	}

	@Override
	public void saveAcquisitionConfiguration() throws AcquisitionControllerException {
		updateImageCalibration();
		updateProcessingRequest();
		try {
			save(formatConfigurationFileName(getAcquisition().getName()), DocumentMapper.toJSON(getAcquisition()));
		} catch (GDAException e) {
			throw new AcquisitionControllerException(e);
		}
	}

	@Override
	public void runAcquisition() throws AcquisitionControllerException {
		updateImageCalibration();
		updateProcessingRequest();
		updateStartPosition();
		publishRun(createScanningMessage());
	}

	@Override
	public void loadAcquisitionConfiguration(URL url) throws AcquisitionControllerException {
		loadAcquisitionConfiguration(parseAcquisitionConfiguration(url).getResource());
	}

	@Override
	public void loadAcquisitionConfiguration(ScanningAcquisition acquisition) throws AcquisitionControllerException {
		setAcquisition(acquisition);
		publishEvent(new AcquisitionConfigurationResourceLoadEvent(this, acquisition.getAcquisitionLocation()));
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
	public void deleteAcquisitionConfiguration(URL url) throws AcquisitionControllerException {
		throw new AcquisitionControllerException("Delete not implemented");
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

	private ScanningAcquisitionMessage createScanningMessage() throws AcquisitionControllerException {
		try {
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

		return String.format("%s_%s",
				Optional.ofNullable(acquisitionType).orElse(AcquisitionPropertyType.DEFAULT).name(), fn);
	}

	private void save(String fileName, String acquisitionDocument) {
		try {
			Path path = getPath(fileName, acquisitionDocument);
			publishEvent(new AcquisitionConfigurationResourceSaveEvent(this, path.toUri().toURL()));
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
			ScanRequest scanRequest = srf.createScanRequest(remoteServices.getIRunnableDeviceService());
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
		// Note - Uses a read-only acquisition object to avoid nullpointer in ic
		Set<DevicePositionDocument> flatPosition = stageController.getPositionDocuments(Position.OUT_OF_BEAM, detectorsHelper.getOutOfBeamScannables());
		if ((ic.getFlatCalibration().isAfterAcquisition() || ic.getFlatCalibration().isBeforeAcquisition())
				&& flatPosition.isEmpty()) {
			throw new AcquisitionConfigurationException("Save an OutOfBeam position to acquire flat images");
		}
		flatPosition.add(stageController.createShutterOpenRequest());
		updateBeamSelectorPosition(flatPosition);
		getImageCalibrationHelper().updateFlatDetectorPositionDocument(flatPosition);
	}

	private void validateDarkCalibrationParameters(ImageCalibrationReader ic) throws AcquisitionConfigurationException {
		// Note - Uses a read-only acquisition object to avoid nullpointer in ic
		if (ic.getDarkCalibration().isAfterAcquisition() || ic.getDarkCalibration().isBeforeAcquisition()) {
			Set<DevicePositionDocument> darkPosition = new HashSet<>();
			darkPosition.add(stageController.createShutterClosedRequest());
			updateBeamSelectorPosition(darkPosition);
			getImageCalibrationHelper().updateDarkDetectorPositionDocument(darkPosition);
		}
	}

	private void updateProcessingRequest() throws AcquisitionControllerException {
		if (acquisitionType.equals(AcquisitionPropertyType.TOMOGRAPHY)) {
			URL processingFile = fileContext.getTomographyContext().getContextFile(TomographyContextFile.TOMOGRAPHY_DEFAULT_PROCESSING_FILE);
			if (processingFile == null)
				return;
			List<URL> urls = new ArrayList<>();
			urls.add(processingFile);
			SavuProcessingRequest request = new SavuProcessingRequest.Builder()
					.withValue(urls)
					.build();
			List<ProcessingRequestPair<?>> requests = new ArrayList<>();
			requests.add(request);
			getAcquisition().getAcquisitionConfiguration().setProcessingRequest(requests);
		}
		if (acquisitionType.equals(AcquisitionPropertyType.DIFFRACTION)) {
			URL processingFile = fileContext.getDiffractionContext().getContextFile(DiffractionContextFile.DIFFRACTION_DEFAULT_CALIBRATION);
			if (processingFile == null)
				return;
			List<URL> urls = new ArrayList<>();
			urls.add(processingFile);
			DiffractionCalibrationMergeRequest request = new DiffractionCalibrationMergeRequest.Builder()
					.withValue(urls)
					.withDeviceName(Optional.ofNullable(getDatasetName())
											.orElseThrow(() -> new AcquisitionConfigurationException("Dataset name property not configured for diffraction scans")))
					.build();
			List<ProcessingRequestPair<?>> requests = new ArrayList<>();
			requests.add(request);
			getAcquisition().getAcquisitionConfiguration().setProcessingRequest(requests);
		}
	}

	private void updateBeamSelectorPosition(Set<DevicePositionDocument> positions) {
		ManagedScannable<String> beamSelector = BeamSelector.getManagedScannable();
		if (beamSelector == null)
			return;

		if (beamSelector.isAvailable()) {
			DevicePositionDocument beamSelectorPosition = stageController.createDevicePositionDocument(beamSelector);
			positions.add(beamSelectorPosition);
		}
	}

	private String getDatasetName() {
		return AcquisitionsPropertiesHelper.getAcquistionPropertiesDocument(acquisitionType)
				// should only have one document per acquisition type:
				.iterator().next().getPrimaryDataset();
	}

	private void updateStartPosition() {
		if (getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters() == null)
			return;
		// Saves ALL the devices position and mark this set as Position.Start
		// This collection is stored in the stage controller as it is responsible
		// for uk.ac.diamond.daq.mapping.ui.stage.enumeration.Position values
		stageController.savePosition(Position.START);
		// Filters out from the Position.START positions, the position document from AcquisitionPropertiesDocument::getOutOfBeamScannables
		// See AcquisitionPropertiesDocument#outOfBeamScannables
		Set<DevicePositionDocument> startPosition = stageController.getPositionDocuments(Position.START, detectorsHelper.getOutOfBeamScannables());
		startPosition.add(stageController.createShutterOpenRequest());
		updateBeamSelectorPosition(startPosition);

		getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters().setPosition(startPosition);
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
		publishEvent(new ScanningAcquisitionSaveEvent(this, name, acquisition));
	}

	private void publishRun(ScanningAcquisitionMessage tomographyRunMessage) {
		publishEvent(new ScanningAcquisitionRunEvent(this, tomographyRunMessage));
	}

	private void setAcquisition(ScanningAcquisition acquisition) {
		// eventually release already acquired resources, eventually
		releaseResources();
		this.acquisition = acquisition;
		// associate a new helper with the new acquisition
		this.detectorsHelper = new ScanningAcquisitionControllerDetectorHelper(acquisitionType,
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
}