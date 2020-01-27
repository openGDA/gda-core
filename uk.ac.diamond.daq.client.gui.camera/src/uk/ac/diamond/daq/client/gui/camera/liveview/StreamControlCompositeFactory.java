package uk.ac.diamond.daq.client.gui.camera.liveview;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraComboItem;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.liveview.state.ListeningState;
import uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamController;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * 
 * @author Maurizio Nagni
 */
public class StreamControlCompositeFactory implements CompositeFactory {

	private Combo cameraCombo;
	private List<CameraComboItem> comboItems;

	private Combo streamTypeCombo;

	private Button streamActivationButton;

	private StreamController streamController;

	private static final Logger logger = LoggerFactory.getLogger(LiveViewCompositeFactory.class);

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite streamControlArea = ClientSWTElements.createComposite(parent, SWT.NONE, 3);

		Composite cameraComboArea = ClientSWTElements.createComposite(streamControlArea, SWT.NONE);
		ClientSWTElements.createLabel(cameraComboArea, SWT.NONE, ClientMessages.CAMERA, new Point(2, 1));
		cameraCombo = ClientSWTElements.createCombo(cameraComboArea, SWT.READ_ONLY, getCameras(),
				ClientMessages.STAGE_TP);

		Composite streamTypeComboArea = ClientSWTElements.createComposite(streamControlArea, SWT.NONE);
		ClientSWTElements.createLabel(streamTypeComboArea, SWT.NONE, ClientMessages.STREAM, new Point(2, 1));
		streamTypeCombo = ClientSWTElements.createCombo(streamTypeComboArea, SWT.READ_ONLY, getStreamTypes(),
				ClientMessages.STAGE_TP);

		Composite startStreamButtonArea = ClientSWTElements.createComposite(streamControlArea, SWT.NONE);
		ClientSWTElements.createLabel(startStreamButtonArea, SWT.NONE, ClientMessages.STREAM, new Point(2, 1));
		streamActivationButton = ClientSWTElements.createButton(startStreamButtonArea, SWT.NONE,
				ClientMessages.START_STREAM, ClientMessages.START_STREAM);
		streamActivationButton.setData(ClientMessages.START_STREAM);

		// initialise composite
		initialiseComposite(parent);

		// add listeners
		cameraCombo.addListener(SWT.Selection, this::changeStreamController);
		streamTypeCombo.addListener(SWT.Selection, this::changeStreamController);
		streamActivationButton.addListener(SWT.Selection, this::changeStreamState);

		return streamControlArea;
	}

	private void initialiseComposite(Composite parent) {
		// initialises the state
		UUID rootUUID = ClientSWTElements.findParentUUID(parent);
		streamController = new StreamController(new StreamControlData(comboItems.get(0), StreamType.EPICS_ARRAY),
				rootUUID);
		// initialises the camera combo
		cameraCombo.select(0);
		// initialises the streamType combo
		initCameraCombo(0);
	}

	private String[] getCameras() {
		if (comboItems == null) {
			comboItems = CameraHelper.getCameraComboItems();
		}
		return comboItems.stream().map(CameraComboItem::getName).collect(Collectors.toList()).toArray(new String[0]);
	}

	private String[] getStreamTypes() {
		return Arrays.stream(StreamType.values()).map(StreamType::toString).collect(Collectors.toList())
				.toArray(new String[0]);
	}

	private void changeStreamController(Event e) {
		// Has to publish ChangeActiveCameraEvent when selection change in cameraCombo
		updateStreamController();
		try {
			streamController.update();
		} catch (LiveStreamException ex) {
			handleException(ex);
		}
		updateStreamActivationButton();
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
			handleException(ex);
		}
		updateStreamActivationButton();
	}

	private void updateStreamController() {
		// Changes camera
		if (streamController.getControlData().getCamera().getIndex() != cameraCombo.getSelectionIndex()) {
			initCameraCombo(cameraCombo.getSelectionIndex());
		}
		streamController.setControlData(new StreamControlData(comboItems.get(cameraCombo.getSelectionIndex()),
				StreamType.values()[streamTypeCombo.getSelectionIndex()]));
	}

	private void initCameraCombo(int cameraIndex) {
		streamTypeCombo.removeAll();
		CameraHelper.getCameraStreamTypes(cameraIndex).forEach(type -> streamTypeCombo.add(type.toString()));
		if (streamTypeCombo.getItemCount() > 0) {
			streamTypeCombo.select(0);
		}
	}

	private void updateStreamActivationButton() {
		if (ListeningState.class.isInstance(streamController.getState())) {
			streamActivationButton.setText(ClientMessagesUtility.getMessage(ClientMessages.STOP_STREAM));
		} else {
			streamActivationButton.setText(ClientMessagesUtility.getMessage(ClientMessages.START_STREAM));
		}
	}

	private void handleException(LiveStreamException ex) {
		try {
			streamController.idle();
		} catch (LiveStreamException e1) {
			logger.error("Exception persists", e1);
		} finally {
			Status status = new Status(IStatus.ERROR, "PluginID", "Partial camera configuration");
			// Display the dialog
			ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Stream Control Error", ex.getMessage(),
					status);
		}
	}
}
