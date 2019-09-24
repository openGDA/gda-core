/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.xanes.ui;

import static gda.jython.JythonStatus.RUNNING;
import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_SCAN_REQUEST_JSON;
import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_XANES_EDGE_PARAMS_JSON;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IJobQueue;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;
import gda.rcp.GDAClientActivator;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters;
import uk.ac.diamond.daq.mapping.ui.experiment.MappingExperimentView;
import uk.ac.diamond.daq.mapping.ui.experiment.OuterScannablesSection;
import uk.ac.diamond.daq.mapping.ui.experiment.SubmitScanSection;

/**
 * Submit a XANES scan
 * <p>
 * This combines the standard {@link ScanRequest} with the specific parameters from the
 * {@link XanesEdgeParametersSection} and calls the script <code>{beamline config}/scanning/submit_xanes_scan.py</code>
 * <p>
 * The parameters are passed in JSON format to avoid serialisation problems.
 */
public class XanesSubmitScanSection extends SubmitScanSection {
	private static final Logger logger = LoggerFactory.getLogger(XanesSubmitScanSection.class);

	private static final String SCRIPT_FILE = "scanning/submit_xanes_scan.py";
	private String energyScannable;

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

	@Override
	public void createControls(Composite parent) {
		setButtonColour(new RGB(179, 204, 255));
		super.createControls(parent);
	}

	@Override
	protected void createSubmitSection() {
		final Composite submitComposite = new Composite(getComposite(), SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(submitComposite);
		createSubmitButton(submitComposite);
		createStopButton(submitComposite);
	}

	private void createStopButton(Composite parent) {
		final Button stopButton = new Button(parent, SWT.PUSH);
		stopButton.setText("Stop");
		stopButton.setToolTipText("Stop all scripts and the current scan");
		final ImageDescriptor stopImage = GDAClientActivator.getImageDescriptor("icons/stop.png");
		Objects.requireNonNull(stopImage, "Missing image for stop button");
		stopButton.setImage(stopImage.createImage());
		stopButton.addSelectionListener(widgetSelectedAdapter(e -> stopScan()));
	}

	@Override
	protected void submitScan() {
		final IScriptService scriptService = getService(IScriptService.class);
		final ScanRequest scanRequest = getScanRequest(getMappingBean());
		final XanesEdgeParametersSection paramsSection = getMappingView().getSection(XanesEdgeParametersSection.class);
		final XanesEdgeParameters xanesEdgeParameters = paramsSection.getScanParameters();

		try {
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));
			scriptService.setNamedValue(VAR_NAME_XANES_EDGE_PARAMS_JSON, marshallerService.marshal(xanesEdgeParameters));
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
			return;
		}

		Async.execute(() -> {
			// Run the script, disabling the submit button while it is running
			final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
			try {
				setButtonEnabled(false);
				logger.info("Running XANES scanning script: {}", SCRIPT_FILE);
				jythonServerFacade.runScript(SCRIPT_FILE);
				while (jythonServerFacade.getScriptStatus() == RUNNING) {
					Thread.sleep(500);
				}
				logger.info("Finished running XANES scanning script");
			} catch (Exception e) {
				logger.error("Error running XANES scanning script", e);
			} finally {
				setButtonEnabled(true);
			}
		});
	}

	private void stopScan() {
		logger.info("Stopping XANES script & job");

		// Stop the XANES script
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

	private void setButtonEnabled(boolean enabled) {
		Display.getDefault().syncExec(() -> setSubmitScanButtonEnabled(enabled));
	}

	@Override
	protected void onShow() {
		setParametersVisibility(true);
		selectEnergyScannable(true);
	}

	@Override
	protected void onHide() {
		setParametersVisibility(false);
		selectEnergyScannable(false);
	}

	/**
	 * Show or hide the corresponding parameters section
	 *
	 * @param visible
	 *            <code>true</code> to show the section, <code>false</code> to hide it
	 */
	private void setParametersVisibility(boolean visible) {
		final XanesEdgeParametersSection xanesParams = getMappingView().getSection(XanesEdgeParametersSection.class);

		if (xanesParams == null) {
			logger.error("No XANES parameters section found");
		} else {
			xanesParams.setVisible(visible);
			relayoutMappingView();
		}
	}

	/**
	 * Select or deselect the energy scannable
	 * <p>
	 * If the energy scannable is set, it will be selected when the user switches to the XANES scanning view and
	 * deselected when they switch to another view. This prevents them from (for example) inadvertently running an
	 * energy scan as a standard Mapping scan, which will not correct for drift.
	 *
	 * @param select
	 *            <code>true</code> to select the energy scannable, <code>false</code> to deselect it
	 */
	private void selectEnergyScannable(boolean select) {
		if (energyScannable != null && !energyScannable.equals("")) {
			final List<IScanModelWrapper<IScanPathModel>> outerScannables = getMappingBean().getScanDefinition().getOuterScannables();
			for (IScanModelWrapper<IScanPathModel> scannable : outerScannables) {
				if (scannable.getName().equals(energyScannable)) {
					scannable.setIncludeInScan(select);
					final OuterScannablesSection outerScannablesSection = getMappingView().getSection(OuterScannablesSection.class);
					outerScannablesSection.updateControls();
					break;
				}
			}
		}
	}

	public void setEnergyScannable(String energyScannable) {
		this.energyScannable = energyScannable;
	}
}
