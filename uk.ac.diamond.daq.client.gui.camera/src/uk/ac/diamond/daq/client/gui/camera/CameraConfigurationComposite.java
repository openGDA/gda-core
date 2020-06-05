package uk.ac.diamond.daq.client.gui.camera;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.springframework.context.ApplicationListener;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.binning.BinningCompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.diamond.daq.client.gui.camera.exposure.ExposureDurationComposite;
import uk.ac.diamond.daq.client.gui.camera.liveview.state.ListeningState;
import uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamController;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.composites.MotorCompositeFactory;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Assembles different {@link Composite} as control panel for a camera. Listen
 * to {@link ChangeActiveCameraEvent} events to update the components
 * accordingly.
 * 
 * @author Maurizio Nagni
 */
public class CameraConfigurationComposite implements CompositeFactory {

	private Composite motorCompositeArea;
	private UUID uuidRoot;
	private Optional<Integer> activeCameraIndex;

	private final StreamController streamController;

	public CameraConfigurationComposite(StreamController streamController) {
		super();
		this.streamController = streamController;
	}

	private Button streamActivationButton;

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite container = ClientSWTElements.createComposite(parent, style);
		uuidRoot = ClientSWTElements.findParentUUID(container).orElse(null);
		activeCameraIndex = Optional.ofNullable(CameraHelper.getDefaultCameraProperties().getIndex());

		GridLayoutFactory.swtDefaults().numColumns(4).equalWidth(false).applyTo(container);
		GridDataFactory gdf = GridDataFactory.fillDefaults().grab(true, true);

		// Exposure Component
		Composite exposureLengthComposite = new ExposureDurationComposite().createComposite(container, style);
		gdf.applyTo(exposureLengthComposite);

		// Binning Component
		Composite binningCompositeArea = new BinningCompositeFactory().createComposite(container, style);
		gdf.applyTo(binningCompositeArea);

		// Motors Components
		motorCompositeArea = ClientSWTElements.createComposite(container, style);
		buildMotorsGUI();
		gdf.applyTo(motorCompositeArea);
		try {
			SpringApplicationContextProxy.addDisposableApplicationListener(container,
					getChangeCameraListener(container));
		} catch (GDAClientException e) {
			UIHelper.showError("Cannot add camera change listener to CameraConfiguration", e);
		}

		streamActivationButton = ClientSWTElements.createButton(container, SWT.NONE, ClientMessages.START_STREAM,
				ClientMessages.START_STREAM);
		streamActivationButton.setData(ClientMessages.START_STREAM);

		streamActivationButton.addListener(SWT.Selection, this::changeStreamState);

		return container;
	}

	private void buildMotorsGUI() {
		Arrays.stream(motorCompositeArea.getChildren()).forEach(Widget::dispose);
		getICameraConfiguration()
				.ifPresent(c -> c.getCameraProperties().getMotorProperties().stream().forEach(motor -> {
					MotorCompositeFactory mc = new MotorCompositeFactory(motor);
					mc.createComposite(motorCompositeArea, SWT.HORIZONTAL);
				}));
		motorCompositeArea.layout(true, true);
	}

	private void changeStreamState(Event e) {
		try {
			// Is it listening?
			if (ListeningState.class.isInstance(streamController.getState())) {
				streamController.idle();
			} else {
				// then was idle
				streamController.listen();
			}
		} catch (LiveStreamException ex) {
			// handleException(ex);
		}
		updateStreamActivationButton();
	}

	private void updateStreamActivationButton() {
		if (ListeningState.class.isInstance(streamController.getState())) {
			streamActivationButton.setText(ClientMessagesUtility.getMessage(ClientMessages.STOP_STREAM));
		} else {
			streamActivationButton.setText(ClientMessagesUtility.getMessage(ClientMessages.START_STREAM));
		}
	}

	private Optional<ICameraConfiguration> getICameraConfiguration() {
		return activeCameraIndex.map(CameraHelper::createICameraConfiguration);
	}

	private ApplicationListener<ChangeActiveCameraEvent> getChangeCameraListener(Composite container) {
		return new ApplicationListener<ChangeActiveCameraEvent>() {
			@Override
			public void onApplicationEvent(ChangeActiveCameraEvent event) {
				if (!event.haveSameParent(container)) {
					return;
				}
				activeCameraIndex = Optional.of(event.getActiveCamera().getIndex());
				buildMotorsGUI();
			}
		};
	}
}
