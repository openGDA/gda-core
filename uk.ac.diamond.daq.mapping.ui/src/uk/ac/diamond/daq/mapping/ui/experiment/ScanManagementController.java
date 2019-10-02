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
import static uk.ac.diamond.daq.experiment.api.Services.getExperimentService;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.RandomOffsetGridModel;
import org.eclipse.scanning.api.scan.IParserService;
import org.eclipse.scanning.api.scan.ui.MonitorScanUIElement.MonitorScanRole;
import org.eclipse.scanning.device.ui.device.MonitorView;
import org.eclipse.scanning.device.ui.util.PageUtil;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.TriggerableScan;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.triggerable.TriggerableMap;
import uk.ac.diamond.daq.mapping.ui.MappingUIConstants;
import uk.ac.diamond.daq.mapping.ui.experiment.file.DescriptiveFilenameFactory;
import uk.ac.diamond.daq.osgi.OsgiService;

/**
 * Controller to handle loading, saving and submission of scans
 *
 * @since GDA 9.13
 */
@OsgiService(ScanManagementController.class)
public class ScanManagementController extends AbstractMappingController {

	private static final Logger logger = LoggerFactory.getLogger(ScanManagementController.class);

	//FIXME: Currently FileSystemBasedExperimentService is not using the experiment id, but this should be some logical grouping (e.g. visit id) for scans
	private final String EXPERIMENT_ID = null;

	private boolean clickToScanArmed = false;
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
	 * @param filename	The fully qualified name of the required file
	 * @return			An {@link Optional} of a mapping bean constructed from the contents of the file
	 */
	public Optional<IMappingExperimentBean> loadScanMappingBean(final String filename) {
		checkInitialised();
		Optional<IMappingExperimentBean> result = Optional.empty();
		if (filename != null) {
			try {
				byte[] bytes = Files.readAllBytes(Paths.get(filename));
				final String json = new String(bytes, "UTF-8");

				final IMarshallerService marshaller = getService(IMarshallerService.class);
				IMappingExperimentBean mappingBean = marshaller.unmarshal(json, MappingExperimentBean.class);
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
	 * Writes the current contents of the mapping bean to a file specified by the supplied fully qualified filename.
	 * This also includes the the current mapping stage positions. An error dialog is displayed if the file could not be
	 * successfully saved.
	 *
	 * @param filename	The fully qualified name of the required file to store
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
			TriggerableScan scan = new TriggerableMap(createScanBean().getScanRequest(), false);
			getExperimentService().saveScan(scan, getShortName(filename), EXPERIMENT_ID);
		}
	}

	/**
	 *  ExperimentService expects only the filename, Files.write expects the absolute path.
	 *  ExperimentService also doesn't like non-alphanumeric characters, while the auto-generated section of the filename does
	 */

	private String getShortName(String filename) {
		return FilenameUtils.getName(filename).split("\\.")[0];
	}

	/**
	 * Submits the scan described by the current mapping bean to the submission service. An error dialog is displayed if
	 *  the scan could not be successfully submitted.
	 */
	public void submitScan() {
		final ScanBeanSubmitter submitter = getService(ScanBeanSubmitter.class);
		try {
			ScanBean scanBean = createScanBean();
			submitter.submitScan(scanBean);
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
		}
	}

	/**
	 * Produces a textual command that could be submitted to perform the scan descvribed by the current mapping bean.
	 *
	 * @return	The parsed scan command or an error message is parsing fails
	 */
	public String createScanCommand() {
		final ScanBean scanBean = createScanBean();
		final IParserService parserService = getService(IEclipseContext.class).get(IParserService.class);
		try {
			return parserService.getCommand(scanBean.getScanRequest(), true);
		} catch (Exception e) {
			final String message = "Error creating scan commmand";
			logger.error(message, e);
			return message;
		}
	}

	/**
	 * Transforms the current mapping bean into a {@link ScanBean} which can be submitted.
	 *
	 * @return	The resultant {@link ScanBean}
	 */
	public ScanBean createScanBean() {
		checkInitialised();
		final IMappingExperimentBean mappingBean = getMappingBean();
		addMonitors(mappingBean);

		final ScanBean scanBean = new ScanBean();
		String sampleName = mappingBean.getSampleMetadata().getSampleName();
		if (sampleName == null || sampleName.length() == 0) {
			sampleName = "unknown sample";
		}
		final String pathName = mappingBean.getScanDefinition().getMappingScanRegion().getScanPath().getName();
		scanBean.setName(String.format("%s - %s Scan", sampleName, pathName));
		scanBean.setBeamline(System.getProperty("BEAMLINE"));

		final ScanRequestConverter converter = getService(ScanRequestConverter.class);
		final ScanRequest scanRequest = converter.convertToScanRequest(mappingBean);
		scanBean.setScanRequest(scanRequest);
		return scanBean;
	}

	/**
	 * Copies the scan command representation of the current mapping bean to the clipboard
	 */
	public void copyScanToClipboard() {
		try {
			final String scanCommand = createScanCommand();
			Clipboard clipboard = new Clipboard(Display.getDefault());
			clipboard.setContents(new Object[] { scanCommand }, new Transfer[] { TextTransfer.getInstance() });
			clipboard.dispose();
			logger.debug("Copied mapping scan command to clipboard: {}", scanCommand);
		} catch (Exception e) {
			logger.error("Copy to clipboard failed.", e);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Copying Scan Command",
					"The scan command could not be copied to the clipboard. See the error log for more details.");
		}
	}

	/**
	 * Manages indication of whether use of Ctrl-Click on the Map View should start a scan using the current mapping
	 * bean settings
	 *
	 * @return	An {@link IObservableValue} indicating if Ctrl-Click scanning is enabled
	 */
	@SuppressWarnings("unchecked")
	public IObservableValue<Boolean> getClickToScanArmedObservableValue() {
		return BeanProperties.value("clickToScanArmed").observe(this);
	}

	public boolean isClickToScanArmed() {
		return clickToScanArmed;
	}

	public void setClickToScanArmed(boolean clickToScanArmed) {
		this.clickToScanArmed = clickToScanArmed;
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
	 * @param body	The base name of the file
	 * @return		The filename containing the scan descriptor and extension
	 */
	public String buildDescriptiveFilename(final String body) {
		return filenameFactory.getFilename(body, getMappingBean());
	}

	/**
	 * These methods below are a temporary bodge to manage the RandomOffsetGrid selection until it is replaced by a
	 * mutator on the normal models.
	 * TODO: delete these methods and the associated field once Random Offset mutator has been coded.
	 */

	public void updateGridModelIndex() {
		updateGridModelIndex(
				getMappingBean().getScanDefinition().getMappingScanRegion().getScanPath().getClass()
				.equals(RandomOffsetGridModel.class));
	}

	public void updateGridModelIndex(boolean isRandom) {
		gridModelIndex = isRandom ? 1 : 0;
	}

	public int getGridModelIndex() {
		return gridModelIndex;
	}
}
