package uk.ac.diamond.daq.client.gui.camera.liveview;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraComboItem;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.liveview.state.ListeningState;
import uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamController;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.client.widgets.SmartCombo;
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

	private SmartCombo<StreamType> streamTypeCombo;

	private Button streamActivationButton;

	private final StreamController streamController;

	public StreamControlCompositeFactory(StreamController streamController) {
		super();
		this.streamController = streamController;
	}

	private static final Logger logger = LoggerFactory.getLogger(StreamControlCompositeFactory.class);

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite container = ClientSWTElements.createClientCompositeWithGridLayout(parent, style, 3);
		ClientSWTElements.createClientGridDataFactory().grab(true, false).applyTo(container);

		// -- Headers --
		Label label = ClientSWTElements.createClientLabel(container, SWT.NONE, ClientMessages.CAMERA,
				Optional.empty());
		ClientSWTElements.createClientGridDataFactory().indent(5, 2).align(SWT.BEGINNING, SWT.BEGINNING).applyTo(label);
		
		label = ClientSWTElements.createClientLabel(container, SWT.NONE, ClientMessages.STREAM, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().indent(5, 2).align(SWT.BEGINNING, SWT.BEGINNING).span(2, 1).applyTo(label);
		
		// -- Controls --
		cameraCombo = ClientSWTElements.createCombo(container, SWT.READ_ONLY, getCameras(),
				ClientMessages.STAGE_TP);
		ClientSWTElements.createClientGridDataFactory().indent(5, 0).applyTo(cameraCombo);
		
		streamTypeCombo = new SmartCombo<>(container, style, Optional.of(ClientMessages.STAGE_TP),
				Optional.of(this::changeStreamController));
		ClientSWTElements.createClientGridDataFactory().indent(5, 0).applyTo(streamTypeCombo);
		
		streamActivationButton = ClientSWTElements.createClientButton(container, SWT.NONE,
				ClientMessages.START_STREAM, ClientMessages.START_STREAM, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().indent(5, 0).applyTo(streamActivationButton);
		streamActivationButton.setData(ClientMessages.START_STREAM);
		streamActivationButton.addListener(SWT.Selection, this::changeStreamState);
		// ---------------------------------
		
		// initialise composite
		initialiseComposite(parent);

		// add listeners
		cameraCombo.addListener(SWT.Selection, this::changeStreamController);

		return container;
	}

	private List<ImmutablePair<String, StreamType>> streamTypeItems(int cameraIndex) {
		List<ImmutablePair<String, StreamType>> ip = new ArrayList<>();
		CameraHelper.getCameraStreamTypes(cameraIndex).ifPresent(
				types -> types.forEach(type -> ip.add(new ImmutablePair<String, StreamType>(type.toString(), type))));
		return ip;
	}

	private void initialiseComposite(Composite parent) {
		// initialises the camera combo
		cameraCombo.select(0);
		// initialises the streamType combo
		streamTypeCombo.populateCombo(streamTypeItems(0));
	}

	private String[] getCameras() {
		if (comboItems == null) {
			comboItems = CameraHelper.getCameraComboItems();
		}
		return comboItems.stream().map(CameraComboItem::getName).collect(Collectors.toList()).toArray(new String[0]);
	}

	private void changeStreamController(Event e) {
		// Has to publish ChangeActiveCameraEvent when selection change in cameraCombo
		updateStreamController();
		try {
			streamController.update();
		} catch (LiveStreamException ex) {
			handleException(ex);
		}
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

	private void updateStreamController() {
		// Changes camera
		if (streamController.getControlData().getCamera().getIndex() != cameraCombo.getSelectionIndex()) {
			streamTypeCombo.populateCombo(streamTypeItems(cameraCombo.getSelectionIndex()));
		}
		streamTypeCombo.getSelectedItem().ifPresent(st -> streamController
				.setControlData(new StreamControlData(comboItems.get(cameraCombo.getSelectionIndex()), st.getValue())));
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
