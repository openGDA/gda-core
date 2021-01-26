/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.beam;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;
import static uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy.addDisposableApplicationListener;

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.beam.event.BeamMappingStateEvent;
import uk.ac.diamond.daq.client.gui.camera.beam.state.BeamMappingStateContext;
import uk.ac.diamond.daq.client.gui.camera.beam.state.BeamMappingStateContext.Outcome;
import uk.ac.diamond.daq.client.gui.camera.event.BeamCameraMappingEvent;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.event.ScannableStateEvent;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.event.PlottingSystemUpdateEvent;
import uk.ac.gda.client.liveplot.LivePlotComposite;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.client.properties.camera.CameraToBeamMap;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * GUI to maps camera pixels to the motors driving the beam. This class listen
 * to the following events (published by the container it belongs)
 * <ul>
 * <li>{@link ChangeActiveCameraEvent} to set the active camera to map pixels against the motors</li>
 * <li>{@link PlottingSystemUpdateEvent} to set the active plotting system to capture the new stream</li>
 * <li>{@link BeamCameraMappingEvent} to draw the camera area where mapping exists</li>
 * </ul>
 * <p>
 * The first two events have to be published by the top parent container or any
 * of its children. See <a href=
 * "https://confluence.diamond.ac.uk/display/DIAD/Camera+Configuration+View">Camera
 * Configuration</a> in Confluence.
 * </p>
 *
 * <p>
 * See <a href=
 * "https://confluence.diamond.ac.uk/display/DIAD/BeamCameraMapping">CameraBeamMapping</a>
 * in Confluence
 * </p>
 *
 * @author Maurizio Nagni
 *
 */
public class BeamCameraCalibrationComposite implements CompositeFactory {
	private Button doCalibration;

	private Label motorXPosition;
	private Label motorYPosition;

	private Composite container;
	private ICameraConfiguration cameraConfiguration;
	// Displays the 2x2 matrix representing the transformation
	private CameraMappingTable mappingTable;
	/**
	 * The plotting system associated with the actual camera stream
	 */
	private IPlottingSystem<Composite> plottingSystem;

	private BeamMappingStateContext beamMappingContext = new BeamMappingStateContext(() -> cameraConfiguration, 4, 4);
	private static final String CONTEXT_STATE = "STATE";

	private static final Logger logger = LoggerFactory.getLogger(BeamCameraCalibrationComposite.class);

	private ApplicationListener<ChangeActiveCameraEvent> getChangeActiveCameraListener(Composite parent) {
		return CameraHelper.createChangeCameraListener(parent, changeCameraControl);
	}

	private Consumer<ChangeActiveCameraEvent> changeCameraControl = this::updateCamera;

	@Override
	public Composite createComposite(final Composite parent, int style) {
		container = createClientCompositeWithGridLayout(parent, style, 3);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(container);

		doCalibration = createClientButton(container, style, ClientMessages.START,
				ClientMessages.BEAM_CAMERA_MAPPING_TP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.BOTTOM).grab(false, false).span(3, 1)
				.applyTo(doCalibration);
		doCalibration.setData(CONTEXT_STATE, beamMappingContext.getState());

		updateCamera(CameraHelper.getDefaultCameraConfigurationProperties());

		Composite transformTableContainer = createClientCompositeWithGridLayout(container, style, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(transformTableContainer);
		mappingTable = new CameraMappingTable();
		mappingTable.createComposite(transformTableContainer, SWT.NONE);

		Composite mappingReportContainer = createClientCompositeWithGridLayout(container, style, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).span(1, 3)
				.applyTo(mappingReportContainer);

		motorXPosition = createClientLabel(mappingReportContainer, style, ClientMessages.EMPTY_MESSAGE);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(motorXPosition);
		motorXPosition.setText("motorX: ");

		motorYPosition = createClientLabel(mappingReportContainer, style, ClientMessages.EMPTY_MESSAGE);
		motorYPosition.setText("motorY: ");
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(motorYPosition);

		Label estimatedTime = createClientLabel(mappingReportContainer, style, ClientMessages.EMPTY_MESSAGE);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(estimatedTime);
		estimatedTime.setText("estimatedTime: ");
		doCalibration.addSelectionListener(widgetSelectedAdapter(this::toggleCalibrationListener));

		addDisposableApplicationListeners(parent);

		return container;
	}

	private void addDisposableApplicationListeners(Composite parent) {
		try {
			// Updates the active camera
			addDisposableApplicationListener(container, getChangeActiveCameraListener(parent));

			// Updates the active plottingSystem
			addDisposableApplicationListener(container, changePlottingSystem);

			// Updates the cameraMapping table
			addDisposableApplicationListener(container, cameraMappingEventListener);

			// Updates the mapping start/stop button following the mapping state
			addDisposableApplicationListener(container, listenMappingState);

			// Updates the beam driving motor position monitor
			addDisposableApplicationListener(container, scannableStateEventListener);
		} catch (GDAClientException e) {
			logger.error("Cannot add DisposableApplicationListener as the container is null");
		}
	}

	private void toggleCalibrationListener(SelectionEvent event) {
		if (BeamMappingStateContext.State.RUNNING.equals(beamMappingContext.getState())) {
			beamMappingContext.stop();
		} else {
			beamMappingContext.start();
		}
	}

	/**
	 * Enables the calibration button if the camera is beam mapping active
	 *
	 * @param cameraIndex the camera to be mapped against the beam
	 */
	private void updateCamera(CameraConfigurationProperties cameraProperties) {
		cameraConfiguration = CameraHelper.createICameraConfiguration(cameraProperties);
		boolean enable = Optional.ofNullable(cameraProperties)
				.map(cp -> cp.getCameraToBeamMap().isActive())
				.orElseGet(() -> false);
		doCalibration.setEnabled(enable);
	}

	/**
	 * Enables the calibration button if the camera is beam mapping active
	 *
	 * @param cameraIndex the camera to be mapped against the beam
	 */
	private void updateCamera(ChangeActiveCameraEvent event) {
		updateCamera(CameraHelper.getCameraConfigurationProperties(event.getActiveCamera().getIndex()));
	}

	/**
	 * A {@link BeamCameraMappingEvent} is published any time a new motor to camera
	 * mapping is calculated. This listener updates the GUI to reflect the new
	 * mapping.
	 */
	private ApplicationListener<BeamCameraMappingEvent> cameraMappingEventListener = new ApplicationListener<BeamCameraMappingEvent>() {
		@Override
		public void onApplicationEvent(BeamCameraMappingEvent event) {
			if (event.getCameraIndex() != cameraConfiguration.getCameraIndex()) {
				return;
			}
			Optional.ofNullable(cameraConfiguration.getBeamCameraMap()).ifPresent(c -> displayAsynch(() -> {
				updateComposite(c);
				try {
					DrawCameraMappingArea.drawBeamBoundaries(plottingSystem, cameraConfiguration);
				} catch (GDAClientException e) {
					UIHelper.showError("Cannot draw camera beam boundaries", e, logger);
				}
			}));
		}

		private void updateComposite(CameraToBeamMap beamCameraMap) {
			// Displays the matrix
			mappingTable.displayMatrix(beamCameraMap);
		}
	};

	/**
	 * Listens to {@link PlottingSystemUpdateEvent} published by the
	 * {@link LivePlotComposite} when a stream is started.
	 */
	private ApplicationListener<PlottingSystemUpdateEvent> changePlottingSystem = new ApplicationListener<PlottingSystemUpdateEvent>() {
		@Override
		public void onApplicationEvent(PlottingSystemUpdateEvent event) {
			if (event.haveSameParent(container)) {
				plottingSystem = event.getPlottingSystem();
			}
		}
	};

	/**
	 * Listener to {@link BeamMappingStateEvent} published through the mapping
	 * process
	 */
	private ApplicationListener<BeamMappingStateEvent> listenMappingState = new ApplicationListener<BeamMappingStateEvent>() {
		@Override
		public void onApplicationEvent(BeamMappingStateEvent event) {
			switch (event.getState()) {
			case RUNNING:
				// then keeps the button ready for abort the mapping process
				displayAsynch(() -> {
					doCalibration.setText("Abort Calibration");
					doCalibration.getShell().layout(true, true);
				});
				break;
			case TERMINATED:
				// then keeps the button ready to start another mapping process
				displayAsynch(() -> ClientSWTElements.updateButton(doCalibration, ClientMessages.START,
						ClientMessages.BEAM_CAMERA_MAPPING_TP, null));
				handleOutcome(event.getOutcome());
				break;
			default:
				break;
			}
		}

		private void handleOutcome(Outcome outcome) {
			switch (outcome) {
			case ABORTED:
				UIHelper.showWarning("Mapping aborted", "User request");
				break;
			case FAILED:
				UIHelper.showWarning("Mapping Failed", "Mapping terminated normally");
				break;
			case SUCCESS:
				UIHelper.showWarning("Mapping Succeeded", "Mapping terminated normally");
				break;
			case UNAVAILABLE:
				UIHelper.showWarning("Mapping Unavailable", "Unavailable");
				break;
			}
			beamMappingContext = new BeamMappingStateContext(() -> cameraConfiguration, 4, 4);
		}
	};

	private ApplicationListener<ScannableStateEvent> scannableStateEventListener = new ApplicationListener<ScannableStateEvent>() {
		@Override
		public void onApplicationEvent(ScannableStateEvent event) {
			updateXMotorPosition(event);
			updateYMotorPosition(event);
		}

		private void updateXMotorPosition(ScannableStateEvent event) {
			getBean(BeamCameraMapping.class).getMotorXName()
				.ifPresent(n -> {
					if (n.equals(event.getScannableName())) {
						updateMotorPosition(motorXPosition, "xPos", event.getScannablePosition());
					}
				});
		}

		private void updateYMotorPosition(ScannableStateEvent event) {
			getBean(BeamCameraMapping.class).getMotorYName()
				.ifPresent(n -> {
					if (n.equals(event.getScannableName())) {
						updateMotorPosition(motorYPosition, "yPos", event.getScannablePosition());
					}
				});
		}

		private void updateMotorPosition(Label label, String motorName, double position) {
			displayAsynch(() -> label.setText(String.format("Beam %s: %.2f", motorName, position)));
		}
	};

	private void displayAsynch(Runnable runnable) {
		Display.getDefault().asyncExec(runnable);
	}
}