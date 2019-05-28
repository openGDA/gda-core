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

package uk.ac.diamond.daq.mapping.standards.ui;

import static gda.jython.JythonStatus.RUNNING;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;
import gda.rcp.GDAClientActivator;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.impl.ScanPathModelWrapper;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanPathEditor;

public class StandardsScanView {
	private static final Logger logger = LoggerFactory.getLogger(StandardsScanView.class);

	private static final String SCRIPT_FILE = "scanning/submit_standards_scan.py";

	@Inject
	private IEclipseContext injectionContext;

	private ScanPathEditor scanPathEditor;
	private Button submitButton;

	private IConsumer<StatusBean> consumerProxy;

	@PostConstruct
	public void createView(Composite parent) {
		GridDataFactory.swtDefaults().applyTo(parent);
		GridLayoutFactory.swtDefaults().applyTo(parent);

		createScannableEditor(parent);
		createSubmitSection(parent);

		final IEventService eventService = injectionContext.get(IEventService.class);
		try {
			final URI activeMQUri = new URI(LocalProperties.getActiveMQBrokerURI());
			consumerProxy = eventService.createConsumerProxy(activeMQUri, EventConstants.SUBMISSION_QUEUE,
					EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		} catch (Exception e) {
			logger.error("Error creating consumer proxy", e);
		}
	}

	private void createScannableEditor(Composite parent) {
		final Composite editorComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(editorComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(editorComposite);

		final StandardsScanConfig standardsScanConfig = PlatformUI.getWorkbench().getService(StandardsScanConfig.class);
		if (standardsScanConfig == null) {
			logger.error("Missing configuration for standards scan");
			return;
		}

		// Scannable name
		final String scannableName = standardsScanConfig.getScannableName();
		final Label nameLabel = new Label(editorComposite, SWT.NONE);
		nameLabel.setText(scannableName);

		// Display and edit scannable values
		final IScanModelWrapper<IScanPathModel> scannableWrapper = new ScanPathModelWrapper(scannableName, null, false);
		scanPathEditor = new ScanPathEditor(editorComposite, SWT.NONE, scannableWrapper);
	}

	private void createSubmitSection(Composite parent) {
		final Composite submitComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(submitComposite);

		submitButton = new Button(submitComposite, SWT.PUSH);
		submitButton.setText("Submit standards scan");
		submitButton.setBackground(new Color(Display.getDefault(), new RGB(255, 191, 0)));
		submitButton.addSelectionListener(widgetSelectedAdapter(e -> submitScan()));

		final Button stopButton = new Button(submitComposite, SWT.PUSH);
		stopButton.setText("Stop");
		stopButton.setToolTipText("Stop all scripts and pause queue");
		final ImageDescriptor stopImage = GDAClientActivator.getImageDescriptor("icons/stop.png");
		Objects.requireNonNull(stopImage, "Missing image for stop button");
		stopButton.setImage(stopImage.createImage());
		stopButton.addSelectionListener(widgetSelectedAdapter(e -> stopScan()));
	}

	private void submitScan() {
		final String scanPath = scanPathEditor.getAxisText();
		if (scanPath == null || scanPath.isEmpty()) {
			displayError("Scan path empty", "No scan path has been defined");
			return;
		}

		final IScriptService scriptService = injectionContext.get(IScriptService.class);
		scriptService.setNamedValue(IScriptService.VAR_NAME_SCAN_PATH, scanPathEditor.getAxisText());

		Async.execute(() -> {
			// Run the script, disabling the submit button while it is running
			final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
			try {
				setButtonEnabled(false);
				logger.info("Running standards scan script: {}", SCRIPT_FILE);
				jythonServerFacade.runScript(SCRIPT_FILE);
				while (jythonServerFacade.getScriptStatus() == RUNNING) {
					Thread.sleep(500);
				}
				logger.info("Finished running standards scan script");
			} catch (Exception e) {
				logger.error("Error running standards scan script", e);
			} finally {
				setButtonEnabled(true);
			}
		});
	}

	private void setButtonEnabled(boolean enabled) {
		Display.getDefault().syncExec(() -> submitButton.setEnabled(enabled));
	}
	private void stopScan() {
		logger.info("Stopping standards scan script & job");

		// Stop the script
		final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
		jythonServerFacade.abortCommands();

		try {
			// Stop the currently-running job
			final List<StatusBean> currentJobs = consumerProxy.getRunningAndCompleted();
			for (StatusBean job : currentJobs) {
				if (job.getStatus() == Status.RUNNING) {
					consumerProxy.terminateJob(job);
				}
			}
		} catch (EventException e) {
			logger.error("Error accessing queue", e);
		}
	}

	private void displayError(String title, String message) {
		final Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		MessageDialog.openError(activeShell, title, message);
	}

	@PreDestroy
	public void onDispose() {
		scanPathEditor.dispose();
	}
}
