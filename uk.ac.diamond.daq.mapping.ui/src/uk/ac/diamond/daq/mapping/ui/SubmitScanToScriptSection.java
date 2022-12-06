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

package uk.ac.diamond.daq.mapping.ui;

import static gda.jython.JythonStatus.RUNNING;
import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_SCAN_REQUEST_JSON;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.ui.experiment.DetectorsSection;
import uk.ac.diamond.daq.mapping.ui.experiment.MappingExperimentView;
import uk.ac.diamond.daq.mapping.ui.experiment.OuterScannablesSection;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanRequestConverter;
import uk.ac.diamond.daq.mapping.ui.experiment.SubmitScanSection;

/**
 * Abstract base class for submit sections that submit the scan to a script
 */
public abstract class SubmitScanToScriptSection extends SubmitScanSection {
	private static final Logger logger = LoggerFactory.getLogger(SubmitScanToScriptSection.class);

	private static final int NUM_COLUMNS = 2;

	private IJobQueue<StatusBean> jobQueueProxy;

	private String scriptFilePath;
	private String detectorName;
	private String outerScannableName;
	private Colour submitButtonColour;

	@Override
	public void initialize(MappingExperimentView mappingView) {
		super.initialize(mappingView);

		final IEventService eventService = getService(IEventService.class);
		try {
			final URI activeMQUri = new URI(LocalProperties.getActiveMQBrokerURI());
			jobQueueProxy = eventService.createJobQueueProxy(activeMQUri, EventConstants.SUBMISSION_QUEUE,
					EventConstants.CMD_TOPIC, EventConstants.ACK_TOPIC);
		} catch (Exception e) {
			logger.error("Error creating consumer proxy", e);
		}
	}

	protected ScanRequest getScanRequest(final IMappingExperimentBean mappingBean) {
		final ScanRequestConverter converter = getService(ScanRequestConverter.class);
		return converter.convertToScanRequest(mappingBean);
	}

	@Override
	protected void createSubmitSection() {
		final Composite submitComposite = new Composite(getComposite(), SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(submitComposite);

		createSubmitButton(submitComposite);
		createStopButton(submitComposite);
	}

	@Override
	public void createControls(Composite parent) {
		setButtonColour(submitButtonColour.getRGB());
		super.createControls(parent);
	}

	/**
	 * Create a button that stops the current scan and all running scripts
	 *
	 * @param parent
	 *            composite on which to create the button
	 * @return the {@link Button} that has been created
	 */
	protected Button createStopButton(Composite parent) {
		final Button stopButton = new Button(parent, SWT.PUSH);
		stopButton.setText("Stop");
		stopButton.setToolTipText("Stop all scripts and the current scan");
		final Image stopImage = new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/stop.png"));
		Objects.requireNonNull(stopImage, "Missing image for stop button");
		stopButton.setImage(stopImage);
		stopButton.addSelectionListener(widgetSelectedAdapter(e -> stopScan()));
		return stopButton;
	}

	private void stopScan() {
		logger.info("Stopping script & job");

		// Stop the script
		final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
		jythonServerFacade.abortCommands();

		try {
			// Stop the currently-running job
			final List<StatusBean> currentJobs = jobQueueProxy.getRunningAndCompleted();
			for (StatusBean job : currentJobs) {
				if (job.getStatus() == Status.RUNNING) {
					jobQueueProxy.terminateJob(job);
				}
			}
		} catch (EventException e) {
			logger.error("Error accessing queue", e);
		}
	}

	/**
	 * Run the script, disabling the submit button while it is running
	 *
	 * @param scriptFile
	 *            name of the script file to run
	 * @param description
	 *            description of the script to identify it in logging messages
	 */
	protected void runScript(String scriptFile, String description) {
		final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
		try {
			setSubmitButtonEnabledState(false);
			logger.info("Running {}: {}", description, scriptFile);
			jythonServerFacade.runScript(new File(jythonServerFacade.locateScript(scriptFile)));
			while (jythonServerFacade.getScriptStatus() == RUNNING) {
				Thread.sleep(500);
			}
			logger.info("Finished running {}", description);
		} catch (Exception e) {
			logger.error("Error running {}", description, e);
		} finally {
			setSubmitButtonEnabledState(true);
		}
	}

	private void setSubmitButtonEnabledState(boolean enabled) {
		Display.getDefault().syncExec(() -> setSubmitScanButtonEnabled(enabled));
	}

	@Override
	protected void submitScan() {
		final IScriptService scriptService = getService(IScriptService.class);
		final ScanRequest scanRequest = getScanRequest(getBean());

		try {
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
			return;
		}

		Async.execute(() -> runScript(scriptFilePath, getDescription()));
	}

	/**
	 * Select or deselect a scannable in OuterScannablesSection
	 * <p>
	 * The relevant scannable will be made visible in the outer scannable section if it is not already visible.
	 *
	 * @param scannableName
	 *            name of the scannable to select/deselect
	 * @param select
	 *            <code>true</code> to select the motor, <code>false</code> to deselect it
	 */
	protected void selectOuterScannable(String scannableName, boolean select) {
		final MappingExperimentView mappingView = getView();
		final OuterScannablesSection outerScannablesSection = mappingView.getSection(OuterScannablesSection.class);
		if (outerScannablesSection == null) {
			logger.error("OuterScannablesSection not found");
			return;
		}
		outerScannablesSection.showScannable(scannableName, select);
		mappingView.updateControls();
	}

	/**
	 * Select or deselect a detector in DetectorsSection
	 * <p>
	 * The relevant detector will be made visible in the detectors section if it is not already visible.
	 *
	 * @param detectorName
	 *            name of the detector to select/deselect
	 * @param select
	 *            <code>true</code> to select the detector, <code>false</code> to deselect it
	 */
	protected void selectDetector(String detectorName, boolean select) {
		final MappingExperimentView mappingView = getView();
		final DetectorsSection detectorsSection = mappingView.getSection(DetectorsSection.class);
		if (detectorsSection == null) {
			logger.error("DetectorsSection not found");
			return;
		}
		detectorsSection.selectDetector(detectorName, select);
	}

	public void setScriptFilePath(String scriptFilePath) {
		this.scriptFilePath = scriptFilePath;
	}

	protected String getScriptFilePath() {
		return scriptFilePath;
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	protected String getDetectorName() {
		return detectorName;
	}

	public void setOuterScannableName(String scannableName) {
		this.outerScannableName = scannableName;
	}

	protected String getOuterScannableName() {
		return outerScannableName;
	}

	public void setSubmitScanButtonColour(Colour buttonColour) {
		this.submitButtonColour = buttonColour;
	}

}
