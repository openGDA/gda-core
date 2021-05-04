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

package uk.ac.diamond.daq.client.gui.camera.liveview;

import static uk.ac.gda.ui.tool.ClientMessages.CAMERA;
import static uk.ac.gda.ui.tool.ClientMessages.STAGE_TP;
import static uk.ac.gda.ui.tool.ClientMessages.START_STREAM;
import static uk.ac.gda.ui.tool.ClientMessages.STOP_STREAM;
import static uk.ac.gda.ui.tool.ClientMessages.STREAM;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;
import static uk.ac.gda.ui.tool.ClientSWTElements.createCombo;

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
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.client.widgets.SmartCombo;


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
		Composite container = createClientCompositeWithGridLayout(parent, style, 3);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(container);

		// -- Headers --
		Label label = createClientLabel(container, SWT.NONE, CAMERA);
		createClientGridDataFactory().indent(5, 2).align(SWT.BEGINNING, SWT.BEGINNING).applyTo(label);

		label = createClientLabel(container, SWT.NONE, STREAM);
		createClientGridDataFactory().indent(5, 2).align(SWT.BEGINNING, SWT.BEGINNING).span(2, 1).applyTo(label);

		// -- Controls --
		cameraCombo = createCombo(container, SWT.READ_ONLY, getCameras(),
				STAGE_TP);
		createClientGridDataFactory().indent(5, 0).applyTo(cameraCombo);

		streamTypeCombo = new SmartCombo<>(container, style, Optional.of(STAGE_TP),
				Optional.of(this::changeStreamController));
		createClientGridDataFactory().indent(5, 0).applyTo(streamTypeCombo);

		streamActivationButton = createClientButton(container, SWT.NONE, START_STREAM, START_STREAM);
		createClientGridDataFactory().indent(5, 0).applyTo(streamActivationButton);
		streamActivationButton.setData(START_STREAM);
		streamActivationButton.addListener(SWT.Selection, this::changeStreamState);
		// ---------------------------------

		// initialise composite
		initialiseComposite(parent);

		// add listeners
		cameraCombo.addListener(SWT.Selection, this::changeStreamController);

		return container;
	}

	private List<ImmutablePair<String, StreamType>> streamTypeItems(CameraComboItem comboItem) {
		List<ImmutablePair<String, StreamType>> ip = new ArrayList<>();
		CameraHelper.createICameraConfiguration(comboItem.getCameraProperties()).getCameraConfiguration()
			.map(CameraConfiguration::cameraStreamTypes)
			.ifPresent(
				types -> types.forEach(type -> ip.add(new ImmutablePair<>(type.toString(), type))));
		return ip;
	}

	private void initialiseComposite(Composite parent) {
		// initialises the camera combo
		cameraCombo.select(0);
		// initialises the streamType combo
		streamTypeCombo.populateCombo(streamTypeItems(getComboByIndex(cameraCombo.getSelectionIndex())));
	}

	private CameraComboItem getComboByIndex(int index) {
		return comboItems.get(index);
	}

	private CameraComboItem getSelectedCombo() {
		return getComboByIndex(cameraCombo.getSelectionIndex());
	}

	private String[] getCameras() {
		if (comboItems == null) {
			comboItems = CameraHelper.getCameraComboItems();
		}
		return comboItems.stream()
				.map(CameraComboItem::getCameraProperties)
				.map(CameraConfigurationProperties::getName)
				.collect(Collectors.toList())
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
			streamActivationButton.setText(getMessage(STOP_STREAM));
		} else {
			streamActivationButton.setText(	getMessage(START_STREAM));
		}
	}

	private void updateStreamController() {
		CameraComboItem selectedItem = getSelectedCombo();
		// Changes camera
		if (streamController.getControlData().getCamera() != selectedItem) {
			streamTypeCombo.populateCombo(streamTypeItems(selectedItem));
		}
		streamTypeCombo.getSelectedItem().ifPresent(st -> streamController
				.setControlData(new StreamControlData(selectedItem, st.getValue())));
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
