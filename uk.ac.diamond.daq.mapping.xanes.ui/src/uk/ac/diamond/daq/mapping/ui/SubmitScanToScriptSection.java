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
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;
import uk.ac.diamond.daq.mapping.ui.experiment.MappingExperimentView;
import uk.ac.diamond.daq.mapping.ui.experiment.SubmitScanSection;

/**
 * Abstract base class for submit sections that submit the scan to a script
 */
public abstract class SubmitScanToScriptSection extends SubmitScanSection {
	private static final Logger logger = LoggerFactory.getLogger(SubmitScanToScriptSection.class);

	private IJobQueue<StatusBean> jobQueueProxy;

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
			jythonServerFacade.runScript(scriptFile);
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
}
