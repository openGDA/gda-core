/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.IParserService;
import org.eclipse.scanning.api.scan.ui.MonitorScanUIElement.MonitorScanRole;
import org.eclipse.scanning.device.ui.device.MonitorView;
import org.eclipse.scanning.device.ui.util.PageUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.ui.MappingUIConstants;

/**
 * A section containing:<ul>
 * <li>a button to submit a scan to the queue;</li>
 * <li>a button to save a scan to disk;</li>
 * <li>a button to load a scan from disk.</li>
 * </ul>
 */
public class SubmitScanSection extends AbstractMappingSection {

	private static final String[] FILE_FILTER_NAMES = new String[] { "Mapping Scan Files", "All Files (*.*)" };
	private static final String[] FILE_FILTER_EXTENSIONS = new String[] { "*.map", "*.*" };
	private static final Logger logger = LoggerFactory.getLogger(SubmitScanSection.class);

	@Override
	public boolean createSeparator() {
		return false;
	}

	@Override
	public void createControls(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).applyTo(composite);
		GridLayoutFactory.swtDefaults().numColumns(5).applyTo(composite);

		// Button to submit a scan to the queue
		final Button submitScanButton = new Button(composite, SWT.PUSH);
		submitScanButton.setText("Queue Scan");
		GridDataFactory.swtDefaults().applyTo(submitScanButton);
		submitScanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				submitScan();
			}
		});

		// Button to copy a scan to the clipboard
		final Button copyScanCommandButton = new Button(composite, SWT.PUSH);
		copyScanCommandButton.setImage(MappingExperimentUtils.getImage("icons/copy.png"));
		copyScanCommandButton.setToolTipText("Copy the scan command to the system clipboard");
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.RIGHT, SWT.CENTER).applyTo(copyScanCommandButton);
		copyScanCommandButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				copyScanToClipboard();
			}
		});

		// Button to load a scan from disk
		final Button loadButton = new Button(composite, SWT.PUSH);
		loadButton.setImage(MappingExperimentUtils.getImage("icons/open.png"));
		loadButton.setToolTipText("Load a scan from the file system");
		GridDataFactory.swtDefaults().applyTo(loadButton);
		loadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				loadScan();
			}
		});

		// Button to save a scan to disk
		Button saveButton = new Button(composite, SWT.PUSH);
		saveButton.setImage(MappingExperimentUtils.getImage("icons/save.png"));
		saveButton.setToolTipText("Save a scan to the file system");
		GridDataFactory.swtDefaults().applyTo(saveButton);
		saveButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				saveScan();
			}
		});
	}

	private void loadScan() {
		final String fileName = chooseFileName(SWT.OPEN);
		if (fileName == null) return;

		try {
			byte[] bytes = Files.readAllBytes(Paths.get(fileName));
			final String json = new String(bytes, "UTF-8");

			final IMarshallerService marshaller = getService(IMarshallerService.class);
			IMappingExperimentBean mappingBean = marshaller.unmarshal(json, MappingExperimentBean.class);
			getMappingView().setMappingBean(mappingBean);
			loadStageInfoSnapshot();
			getMappingView().updateControls();
		} catch (Exception e) {
			final String errorMessage = "Could not load a mapping scan from file: " + fileName;
			logger.error(errorMessage, e);
			ErrorDialog.openError(getShell(), "Load Scan", errorMessage,
					new Status(IStatus.ERROR, MappingUIConstants.PLUGIN_ID, errorMessage, e));
		}
	}

	private void saveScan() {
		final String fileName = chooseFileName(SWT.SAVE);
		if (fileName == null) return;

		captureStageInfoSnapshot();
		final IMappingExperimentBean mappingBean = getMappingBean();
		final IMarshallerService marshaller = getService(IMarshallerService.class);
		try {
			logger.trace("Serializing the state of the mapping view to json");
			final String json = marshaller.marshal(mappingBean);
			logger.trace("Writing state of mapping view to file: {}", fileName);
			Files.write(Paths.get(fileName), json.getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE);
		} catch (Exception e) {
			final String errorMessage = "Could not save the mapping scan to file: " + fileName;
			logger.error(errorMessage, e);
			ErrorDialog.openError(getShell(), "Save Scan", errorMessage,
					new Status(IStatus.ERROR, MappingUIConstants.PLUGIN_ID, errorMessage, e));
		}
	}

	private String chooseFileName(int fileDialogStyle) {
		final FileDialog dialog = new FileDialog(getShell(), fileDialogStyle);
		dialog.setFilterNames(FILE_FILTER_NAMES);
		dialog.setFilterExtensions(FILE_FILTER_EXTENSIONS);
		final String visitConfigDir = getService(IFilePathService.class).getVisitConfigDir();
		dialog.setFilterPath(visitConfigDir);
		dialog.setOverwrite(true);

		return dialog.open();
	}

	private ScanBean createScanBean() {
		IMappingExperimentBean mappingBean = getMappingBean();
		addMonitors(mappingBean);

		ScanBean scanBean = new ScanBean();
		String sampleName = mappingBean.getSampleMetadata().getSampleName();
		if (sampleName == null || sampleName.length() == 0) {
			sampleName = "unknown sample";
		}
		String pathName = mappingBean.getScanDefinition().getMappingScanRegion().getScanPath().getName();
		scanBean.setName(String.format("%s - %s Scan", sampleName, pathName));
		scanBean.setBeamline(System.getProperty("BEAMLINE"));

		final ScanRequestConverter converter = getService(ScanRequestConverter.class);
		ScanRequest<IROI> scanRequest = converter.convertToScanRequest(mappingBean);
		scanBean.setScanRequest(scanRequest);
		return scanBean;
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

	private void loadStageInfoSnapshot() {
		// push the saved stage info in the mapping bean to the OSGi component
		IMappingExperimentBean bean = getMappingBean();
		MappingStageInfo stage = getService(MappingStageInfo.class);
		stage.merge((MappingStageInfo) bean.getStageInfoSnapshot());
	}

	private void captureStageInfoSnapshot() {
		// capture the current MappingStageInfo in the mapping bean
		IMappingExperimentBean bean = getMappingBean();
		MappingStageInfo stage = getService(MappingStageInfo.class);
		((MappingStageInfo) bean.getStageInfoSnapshot()).merge(stage);
	}

	private void copyScanToClipboard() {
		try {
			ScanBean scanBean = createScanBean();
			IParserService parserService = getEclipseContext().get(IParserService.class);
			String scanCommand = parserService.getCommand(scanBean.getScanRequest(), true);
			Clipboard clipboard = new Clipboard(Display.getDefault());
			clipboard.setContents(new Object[] { scanCommand }, new Transfer[] { TextTransfer.getInstance() });
			clipboard.dispose();
			logger.debug("Copied mapping scan command to clipboard: {}", scanCommand);
		} catch (Exception e) {
			logger.error("Copy to clipboard failed.", e);
			MessageDialog.openError(getShell(), "Error Copying Scan Command",
					"The scan command could not be copied to the clipboard. See the error log for more details.");
		}
	}

	private void submitScan() {
		final ScanBeanSubmitter submitter = getService(ScanBeanSubmitter.class);
		try {
			ScanBean scanBean = createScanBean();
			submitter.submitScan(scanBean);
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
		}
	}

}
