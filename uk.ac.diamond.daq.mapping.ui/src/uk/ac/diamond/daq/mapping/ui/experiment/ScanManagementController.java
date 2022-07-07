/*-
 * Copyright © 2019 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.mapping.ui.experiment;

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.eclipse.scanning.api.scan.ui.MonitorScanUIElement.MonitorScanRole;
import org.eclipse.scanning.device.ui.device.MonitorView;
import org.eclipse.scanning.device.ui.util.PageUtil;
import org.eclipse.scanning.sequencer.ScanRequestBuilder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.application.persistence.service.PersistenceException;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IScanBeanSubmitter;
import uk.ac.diamond.daq.mapping.api.PersistableMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.ScanRequestSavedEvent;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.ui.MappingUIConstants;
import uk.ac.diamond.daq.mapping.ui.experiment.copyscan.CopyScanWizard;
import uk.ac.diamond.daq.mapping.ui.experiment.copyscan.CopyScanWizardDialog;
import uk.ac.diamond.daq.mapping.ui.experiment.file.DescriptiveFilenameFactory;
import uk.ac.diamond.daq.osgi.OsgiService;
import uk.ac.diamond.daq.persistence.manager.PersistenceServiceWrapper;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Controller to handle loading, saving and submission of scans
 *
 * @since GDA 9.13
 */
@OsgiService(ScanManagementController.class)
public class ScanManagementController extends AbstractMappingController {

	private static final Logger logger = LoggerFactory.getLogger(ScanManagementController.class);

	public static final String DEFAULT_SAMPLE_NAME = "Unnamed Sample";
	public static final String DEFAULT_SAMPLE_DESCRIPTION = "No description provided.";

	private MappingStageInfo stage;
	private DescriptiveFilenameFactory filenameFactory = new DescriptiveFilenameFactory();

	private int gridModelIndex = 0;

	public ScanManagementController() {
		logger.debug("Created ScanManagementController");
	}

	@Override
	protected void oneTimeInitialisation() {
		stage = getService(MappingStageInfo.class);
	}

	/**
	 * Loads the file specified by the supplied fully quAlified filename into a mapping bean which is then returned
	 * within and {@link Optional}. An error dialog is displayed if the file could not be successfully loaded.
	 *
	 * @param filename
	 *            The fully qualified name of the required file
	 * @return An {@link Optional} of a mapping bean constructed from the contents of the file
	 */
	public Optional<IMappingExperimentBean> loadScanMappingBean(final String filename) {
		checkInitialised();
		Optional<IMappingExperimentBean> result = Optional.empty();
		if (filename != null) {
			try {
				byte[] bytes = Files.readAllBytes(Paths.get(filename));
				final String json = new String(bytes, "UTF-8");

				final IMarshallerService marshaller = getService(IMarshallerService.class);
				MappingExperimentBean mappingBean = marshaller.unmarshal(json, MappingExperimentBean.class);
				stage.merge((MappingStageInfo) mappingBean.getStageInfoSnapshot());
				result = Optional.of(mappingBean);
			} catch (Exception e) {
				final String errorMessage = "Could not load a mapping scan from file: " + filename;
				logger.error(errorMessage, e);
				ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Load Scan", errorMessage,
						new Status(IStatus.ERROR, MappingUIConstants.PLUGIN_ID, errorMessage, e));
			}
		}
		return result;
	}

	/**
	 * Loads a {@link ScanRequest} from the NeXus file specified by the supplied fully qualified filename then
	 * returned within an {@link Optional}.
	 * An error dialog is displayed if the {@link ScanRequest} could not be successfully loaded. Not all
	 * NeXus files contain {@link ScanRequest}s.
	 *
	 * @param nxFilename
	 *        The fully qualified name of the required file
	 * @return An {@link Optional} of a scan request extracted from the metadata of the NeXus file
	 */
	public Optional<ScanRequest> loadScanRequest(final String nxFilename) {
		checkInitialised();
		if (nxFilename != null) {
			try {
				return ScanRequestBuilder.buildFromNexusFile(nxFilename);
			} catch (Exception e) {
				final String errorMessage = "Could not load scan request from nexus file: " + nxFilename;
				logger.error(errorMessage, e);
				ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Load Scan", errorMessage,
						new Status(IStatus.ERROR, MappingUIConstants.PLUGIN_ID, errorMessage, e));
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	/**
	 * Loads the Mapping Scan from the Persistence Service.
	 *
	 * @param id
	 *            The Persistence Id for the mapping Bean
	 * @return An {@link Optional} of a mapping bean constructed from the contents of the file
	 */
	public Optional<IMappingExperimentBean> loadScanMappingBean(final long id) {
		checkInitialised();
		Optional<IMappingExperimentBean> result = Optional.empty();
		try {
			final PersistenceServiceWrapper persistenceService = getService(PersistenceServiceWrapper.class);
			PersistableMappingExperimentBean persistedBean = persistenceService.get(id,
					PersistableMappingExperimentBean.class);
			IMappingExperimentBean mappingBean = persistedBean.getMappingBean();
			stage.merge((MappingStageInfo) mappingBean.getStageInfoSnapshot());
			result = Optional.of(mappingBean);
		} catch (PersistenceException e) {
			final String errorMessage = "Could not load a mapping scan with id: " + id;
			logger.error(errorMessage, e);
			ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Load Scan", errorMessage,
					new Status(IStatus.ERROR, MappingUIConstants.PLUGIN_ID, errorMessage, e));
		}
		return result;
	}

	/**
	 * Writes the current contents of the mapping bean to a file specified by the supplied fully qualified filename.
	 * This also includes the the current mapping stage positions. An error dialog is displayed if the file could not be
	 * successfully saved.
	 *
	 * @param filename
	 *            The fully qualified name of the required file to store
	 */
	public void saveScan(final String filename) {
		checkInitialised();
		if (filename != null) {
			captureStageInfoSnapshot();
			final IMarshallerService marshaller = getService(IMarshallerService.class);
			try {
				logger.trace("Serializing the state of the mapping view to json");
				final String json = marshaller.marshal(getMappingBean());
				logger.trace("Writing state of mapping view to file: {}", filename);
				Files.write(Paths.get(filename), json.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE);
			} catch (Exception e) {
				final String errorMessage = "Could not save the mapping scan to file: " + filename;
				logger.error(errorMessage, e);
				ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Save Scan", errorMessage,
						new Status(IStatus.ERROR, MappingUIConstants.PLUGIN_ID, errorMessage, e));
			}
		}

		SpringApplicationContextProxy.publishEvent(
				new ScanRequestSavedEvent(this, getShortName(filename), createScanBean().getScanRequest()));

	}

	/**
	 * Saves the current mapping bean to the Persistence Service.
	 *
	 * @param id
	 *            the id under which to save the scan.<br>
	 *            If a scan with this id already exists, it will be updated; otherwise the scan will be saved under an
	 *            automatically-generated id
	 * @return the id under which the scan has been saved
	 */
	public long saveScanAs(long id) {
		checkInitialised();
		IMappingExperimentBean mappingBean = getMappingBean();
		mappingBean.setId(id);
		PersistableMappingExperimentBean persistableBean = new PersistableMappingExperimentBean();
		persistableBean.setMappingBean(mappingBean);
		saveScan(persistableBean);
		return persistableBean.getId();
	}

	/**
	 * Saves the current mapping bean as a new bean to the Persistence Service.
	 *
	 * @param scanName
	 *            the display name of the scan
	 * @return the id under which the scan is saved, or IMappingExperimentBean.INVALID_ID if the save fails
	 */
	public long saveScanAs(final String scanName) {
		checkInitialised();
		if (scanName != null) {
			IMappingExperimentBean mappingBean = getMappingBean();
			mappingBean.setDisplayName(scanName);
			PersistableMappingExperimentBean persistableBean = new PersistableMappingExperimentBean();
			persistableBean.setMappingBean(mappingBean);
			saveScan(persistableBean);
			return persistableBean.getId();
		}
		return IMappingExperimentBean.INVALID_ID;
	}

	private void saveScan(PersistableMappingExperimentBean persistableBean) {
		try {
			captureStageInfoSnapshot();
			final PersistenceServiceWrapper persistenceService = getService(PersistenceServiceWrapper.class);
			persistenceService.save(persistableBean);

			SpringApplicationContextFacade.publishEvent(new ScanRequestSavedEvent(this, persistableBean.getScanName(),
					createScanBean().getScanRequest()));
		} catch (PersistenceException e) {
			final String errorMessage = "Could not save the mapping scan : " + persistableBean.getScanName();
			logger.error(errorMessage, e);
			ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Save Scan", errorMessage,
					new Status(IStatus.ERROR, MappingUIConstants.PLUGIN_ID, errorMessage, e));
		}
	}

	public void deleteScan(String filename) {
		try {
			Files.deleteIfExists(new File(filename).toPath());
		} catch (IOException e) {
			logger.error("Failed to delete scan at path: {}", filename, e);
		}
	}

	public void deleteScan(long id) {
		final PersistenceServiceWrapper persistenceService = getService(PersistenceServiceWrapper.class);
		try {
			persistenceService.delete(id);
		} catch (PersistenceException e) {
			logger.error("Failed to delete scan with id: {}", id, e);
		}
	}

	/**
	 * ExperimentService expects only the filename, Files.write expects the absolute path. ExperimentService also
	 * doesn't like non-alphanumeric characters, while the auto-generated section of the filename does
	 */

	private String getShortName(String filename) {
		return FilenameUtils.getName(filename).split("\\.")[0];
	}

	/**
	 * Submits the scan described by the current mapping bean to the submission service. An error dialog is displayed if
	 * the scan could not be successfully submitted.
	 */
	public void submitScan() {
		submitScan(Optional.empty(), null);
	}

	public void submitScan(URL acquisitionFile, ScanningAcquisition acquisitionParameters) {
		submitScan(Optional.ofNullable(acquisitionFile.getPath()), acquisitionParameters);
	}

	/**
	 * Submits the scan described by the current mapping bean to the submission service. An error dialog is displayed if
	 * the scan could not be successfully submitted.
	 *
	 * @param filePath
	 *            The filepath of the output NeXus file. If {@code null} it is generated through default properties.
	 */
	public void submitScan(Optional<String> filePath, ScanningAcquisition acquisitionParameters) {
		final IScanBeanSubmitter submitter = getService(IScanBeanSubmitter.class);
		try {
			final ScanBean scanBean = createScanBean(filePath, acquisitionParameters);
			if (LocalProperties.isPersistenceServiceAvailable()) {
				// Save current status of mapping view and cross-reference in ScanBean
				getMappingBean().setDisplayName("");
				// Force the persistence service to allocate a new ID to the saved version
				// of the mapping bean
				final long scanId = saveScanAs(IMappingExperimentBean.INVALID_ID);
				scanBean.setMappingBeanId(scanId);
			}
			submitter.submitScan(scanBean);
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Submitting Scan",
					"The scan could not be submitted. See the error log for more details.");
		}
	}

	/**
	 * Transforms the current mapping bean into a {@link ScanBean} which can be submitted.
	 *
	 * @return The resultant {@link ScanBean}
	 */
	public ScanBean createScanBean() {
		return createScanBean(Optional.empty(), new ScanningAcquisition());
	}

	/**
	 * Transforms the current mapping bean into a a {@link ScanBean} which can be submitted. The given filePath is set
	 * in the intermediate {@link ScanRequest}.
	 */
	public ScanBean createScanBean(Optional<String> filePath) {
		return createScanBean(filePath, null);
	}

	/**
	 * Transforms the current mapping bean into a a {@link ScanBean} which can be submitted. The given filePath is set
	 * in the intermediate {@link ScanRequest}.
	 *
	 * @param filePath
	 *            the output path. This will override any path set in the {@link ScanRequest} created from the mapping
	 *            bean.
	 * @param acquisitionParameters
	 *            the acquisition parameters
	 * @return a scan bean
	 */
	public ScanBean createScanBean(Optional<String> filePath, ScanningAcquisition acquisitionParameters) {
		checkInitialised();
		final IMappingExperimentBean mappingBean = getMappingBean();

		// Ensure the detector named in ScanningAcquisition (if any) is activated
		Optional.ofNullable(acquisitionParameters)
			.map(ScanningAcquisition::getName)
			.ifPresent(dName -> ensureDetectorIncludedInScan(dName, mappingBean));
		addMonitors(mappingBean);

		final String sampleName = getSampleName(mappingBean, acquisitionParameters);
		final String pathName = mappingBean.getScanDefinition().getMappingScanRegion().getScanPath().getName();

		final ScanBean scanBean = new ScanBean();
		scanBean.setName(String.format("%s - %s Scan", sampleName, pathName));
		scanBean.setBeamline(System.getProperty("BEAMLINE"));

		final ScanRequestConverter converter = getService(ScanRequestConverter.class);
		final ScanRequest scanRequest = converter.convertToScanRequest(mappingBean);
		if (acquisitionParameters != null) {
			setSampleMetadata(scanRequest, sampleName);
		}
		if (filePath.isPresent()) {
			scanRequest.setFilePath(filePath.get());
		}
		scanBean.setScanRequest(scanRequest);
		return scanBean;
	}

	private void ensureDetectorIncludedInScan(String detectorName, IMappingExperimentBean mappingBean) {
	    mappingBean.getDetectorParameters().stream()
	    	.filter(d -> d.getModel().getName().contentEquals(detectorName))
	    	.forEach(d -> d.setIncludeInScan(true));
	}

	/**
	 * Open a wizard to copy the ScanRequest representation of the current mapping bean to the clipboard as a Jython
	 * class
	 */
	public void copyScanToClipboard() {
		final CopyScanWizard copyScanWizard = new CopyScanWizard(createScanBean());
		new CopyScanWizardDialog(Display.getCurrent().getActiveShell(), copyScanWizard).open();
	}

	/**
	 * Manages indication of whether use of Ctrl-Click on the Map View should start a scan using the current mapping
	 * bean settings
	 *
	 * @return An {@link IObservableValue} indicating if Ctrl-Click scanning is enabled
	 */
	public IObservableValue<Boolean> getClickToScanArmedObservableValue() {
		final IBeanValueProperty<ScanManagementController, Boolean> property = BeanProperties.value("clickToScanArmed");
		return property.observe(this);
	}

	private void captureStageInfoSnapshot() {
		// capture the current MappingStageInfo in the mapping bean
		((MappingStageInfo) getMappingBean().getStageInfoSnapshot()).merge(stage);
	}

	private void addMonitors(IMappingExperimentBean mappingBean) {
		final IViewReference viewRef = PageUtil.getPage().findViewReference(MonitorView.ID);
		if (viewRef == null) return;

		final MonitorView monitorView = (MonitorView) viewRef.getView(true); // TODO should we restore the view?
		final BiFunction<Map<String, MonitorScanRole>, MonitorScanRole, Set<String>> getMonitorNamesForRole =
				(map, role) -> map.entrySet().stream()
						.filter(entry -> entry.getValue() == role)
						.map(Map.Entry::getKey).collect(toSet());

		final Map<String, MonitorScanRole> monitors = monitorView.getEnabledMonitors();
		mappingBean.setPerPointMonitorNames(getMonitorNamesForRole.apply(monitors, MonitorScanRole.PER_POINT));
		mappingBean.setPerScanMonitorNames(getMonitorNamesForRole.apply(monitors, MonitorScanRole.PER_SCAN));
	}

	/**
	 * Produces a filename using the {@link DescriptiveFilenameFactory} which encodes a description of the scan elements
	 * in it. The result will be of the form myfilename.<scan description>.map
	 *
	 * @param body
	 *            The base name of the file
	 * @return The filename containing the scan descriptor and extension
	 */
	public String buildDescriptiveFilename(final String body) {
		return filenameFactory.getFilename(body, getMappingBean());
	}

	/**
	 * These methods below are a temporary bodge to manage the RandomOffsetGrid selection until it is replaced by a
	 * mutator on the normal models. TODO: delete these methods and the associated field once Random Offset mutator has
	 * been coded.
	 */

	public void updateGridModelIndex() {
		updateGridModelIndex(
				getMappingBean().getScanDefinition().getMappingScanRegion().getScanPath().getClass()
						.equals(TwoAxisGridPointsRandomOffsetModel.class));
	}

	public void updateGridModelIndex(boolean isRandom) {
		gridModelIndex = isRandom ? 1 : 0;
	}

	public int getGridModelIndex() {
		return gridModelIndex;
	}

	private String getSampleName(IMappingExperimentBean mappingBean, ScanningAcquisition acquisitionParameters) {
		final Optional<String> acquisitionSampleName = Optional.ofNullable(acquisitionParameters).map(ScanningAcquisition::getName);
		final String sampleName = acquisitionSampleName.isPresent() ? acquisitionSampleName.get() : mappingBean.getSampleMetadata().getSampleName();
		return (sampleName == null || sampleName.length() == 0) ? DEFAULT_SAMPLE_NAME : sampleName;
	}

	/**
	 * This method clones a similar one present in {@link ScanRequestConverter#convertToScanRequest}.
	 * The reason is to mitigate the replacement of the {@link MetadataController} properties so that
	 * can be again written as {@link ScanMetadata}
	 *
	 * @param scanRequest
	 * @param sampleName
	 */
	private void setSampleMetadata(ScanRequest scanRequest, String sampleName) {
		final ScanMetadata scanMetadata = new ScanMetadata(MetadataType.SAMPLE);
		scanMetadata.addField(ScanRequestConverter.FIELD_NAME_SAMPLE_NAME, sampleName);
		scanRequest.setScanMetadata(Arrays.asList(scanMetadata));
	}
}
