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

package uk.ac.diamond.daq.mapping.ui.timeseries;

import static java.util.stream.Collectors.toCollection;
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.device.ui.device.MalcolmModelEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanBeanSubmitter;

/**
 * A view to set up a time series scan. This is a malcolm scan of one or more steps (scan points),
 * where no motors are moved, and the detectors controlled by malcolm may take different
 * numbers of frames per step.
 */
public class TimeSeriesScanView {

	private static final Logger logger = LoggerFactory.getLogger(TimeSeriesScanView.class);

	@Inject
	private IEclipseContext eclipseContext;

	private IRunnableDeviceService runnableDeviceService;

	private MalcolmModelEditor malcolmModelEditor;

	private Composite viewComposite;

	private Composite malcolmModelEditorComposite;

	@PostConstruct
	public void createView(Composite parent) {
		parent.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		parent.setBackgroundMode(SWT.INHERIT_DEFAULT);

		viewComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(viewComposite);

		try {
			final List<String> malcolmDeviceNames = getMalcolmDeviceNames();
			if (malcolmDeviceNames.isEmpty()) {
				createErrorLabel(viewComposite, "No malcolm devices found");
			} else {
				createViewControls(viewComposite, malcolmDeviceNames); // normal case
			}
		} catch (ScanningException e) {
			logger.error("Could not get malcolm devices", e);
			createErrorLabel(parent, "Could not get malcolm devices.");
		}
	}

	private List<String> getMalcolmDeviceNames() throws ScanningException {
		final List<String> malcolmDeviceNames = getRunnableDeviceService()
				.getDeviceInformation(DeviceRole.MALCOLM).stream()
				.map(DeviceInformation::getName)
				.collect(toCollection(ArrayList::new));
		Collections.sort(malcolmDeviceNames);
		return malcolmDeviceNames;
	}

	private void createErrorLabel(Composite parent, String errorText) {
		final Label label = new Label(parent, SWT.NONE);
		label.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
		label.setText(errorText);
	}

	private void createViewControls(Composite parent, final List<String> malcolmDeviceNames) {
		createMalcolmDeviceSelectionCombo(parent, malcolmDeviceNames);

		// we need a composite to hold the model editor so that we can change it
		malcolmModelEditorComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(malcolmModelEditorComposite);
		malcolmModelEditor = createMalcolmModelEditor(malcolmModelEditorComposite, malcolmDeviceNames.get(0));

		createButtons(parent);
	}

	private void createMalcolmDeviceSelectionCombo(Composite parent, List<String> malcolmDeviceNames) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

		final Label malcolmDeviceLabel = new Label(composite, SWT.NONE);
		malcolmDeviceLabel.setText("Malcolm device:");
		GridDataFactory.swtDefaults().applyTo(malcolmDeviceLabel);

		final Combo malcolmDevicesCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridDataFactory.swtDefaults().applyTo(malcolmDevicesCombo);
		malcolmDevicesCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter(
				evt -> malcolmDeviceSelected(malcolmDevicesCombo.getItem(malcolmDevicesCombo.getSelectionIndex()))));

		malcolmDevicesCombo.setItems(malcolmDeviceNames.toArray(new String[malcolmDeviceNames.size()]));
		malcolmDevicesCombo.select(0);
	}

	private MalcolmModelEditor createMalcolmModelEditor(Composite parent, String malcolmDeviceName) {
		try {
			final IRunnableDevice<?> malcolmDevice = getRunnableDeviceService().getRunnableDevice(malcolmDeviceName);
			final IMalcolmModel malcolmModel = (IMalcolmModel) malcolmDevice.getModel();
			final MalcolmModelEditor editor = new MalcolmModelEditor(getRunnableDeviceService(), malcolmModel);
			editor.createEditorPart(parent);
			runValidationJob(true);
			return editor;
		} catch (ScanningException e) {
			logger.error("Could not get malcolm device: ", malcolmDeviceName, e);
			return null;
		}
	}

	private void malcolmDeviceSelected(String malcolmDeviceName) {
		if (malcolmModelEditor != null) {
			malcolmModelEditor.dispose();
		}

		malcolmModelEditor = createMalcolmModelEditor(malcolmModelEditorComposite, malcolmDeviceName);
		viewComposite.layout(true, true);
	}

	private void createButtons(Composite parent) {
		final Composite buttonBar = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(buttonBar);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(buttonBar);

		final Button validateButton = new Button(buttonBar, SWT.PUSH);
		validateButton.setText("Validate");
		validateButton.addSelectionListener(widgetSelectedAdapter(event -> runValidationJob(false)));
		GridDataFactory.swtDefaults().applyTo(validateButton);

		final Button submitButton = new Button(buttonBar, SWT.PUSH);
		submitButton.setText("Submit");
		submitButton.addSelectionListener(widgetSelectedAdapter(event -> submitScan()));
		GridDataFactory.swtDefaults().applyTo(submitButton);
	}

	private void runValidationJob(boolean initialValidation) {
		// TOD this method is copied from EditDetectorModelDialog, how can the we refactor to remove the duplication
		final Job validateJob = Job.create("Validate malcolm model", (ICoreRunnable) monitor -> {
			Object result;
			try {
				result = validate(malcolmModelEditor.getModel());
			} catch (Exception e) {
				result = e;
			}
			displayValidationResult(result, initialValidation);
		});

		validateJob.schedule();
	}

	private void displayValidationResult(Object result, boolean initialValidation) {
		final IMalcolmModel malcolmModel = malcolmModelEditor.getModel();
		Display.getDefault().asyncExec(() -> { // note getShell().getDisplay() can throw NPE initially
			if (result instanceof ValidationException) {
				MessageDialog.openError(getShell(), "Validation Error",
						"The given configuration is invalid: " + ((Exception) result).getMessage());
			} else if (result instanceof Exception) {
				logger.error("Error getting malcolm device '{}'", malcolmModel.getName(), result);
				MessageDialog.openError(getShell(), "Error", "Could not get malcolm device " + malcolmModel.getName());
			} else if (!initialValidation) {
				// only show message for ok if button pressed
				MessageDialog.openInformation(getShell(), "Validation Successful", "The given configuration is valid.");
			}
			malcolmModelEditor.updateValidatedModel(result instanceof IMalcolmModel ? (IMalcolmModel) result : null);
		});
	}

	private void submitScan() {
		final ScanBeanSubmitter submitter = eclipseContext.get(ScanBeanSubmitter.class);
		try {
			submitter.submitScan(createScanBean());
		} catch (EventException e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See error log for details.");
		}
	}

	private ScanBean createScanBean() {
		final IMalcolmModel malcolmModel = malcolmModelEditor.getModel();
		final String malcolmDeviceName = malcolmModel.getName();
		final ScanRequest scanRequest = new ScanRequest();

		final Map<String, Object> detectors = new HashMap<>();
		detectors.put(malcolmDeviceName, malcolmModel);
		scanRequest.setDetectors(detectors);
		scanRequest.setCompoundModel(new CompoundModel(new StaticModel()));

		final ScanBean scanBean = new ScanBean(scanRequest);
		scanBean.setName(String.format("%s - Time Series", malcolmDeviceName));
		return scanBean;
	}

	private Shell getShell() {
		return (Shell) eclipseContext.get(IServiceConstants.ACTIVE_SHELL);
	}

	private IDetectorModel validate(IDetectorModel model) throws ScanningException {
		final IRunnableDevice<IDetectorModel> detector = getRunnableDeviceService().getRunnableDevice(model.getName());
		return detector.validateWithReturn(model);
	}

	private IRunnableDeviceService getRunnableDeviceService() throws ScanningException {
		if (runnableDeviceService == null) {
			try {
				final IEventService eventService = eclipseContext.get(IEventService.class);
				final URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
				return eventService.createRemoteService(jmsURI, IRunnableDeviceService.class);
			} catch (EventException | URISyntaxException e) {
				throw new ScanningException(e);
			}
		}
		return runnableDeviceService;
	}


}
