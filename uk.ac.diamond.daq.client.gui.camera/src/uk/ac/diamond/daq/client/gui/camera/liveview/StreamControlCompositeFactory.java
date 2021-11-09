/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.liveview.state.ListeningState;
import uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamController;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;

public class StreamControlCompositeFactory implements CompositeFactory {

	private final Logger logger = LoggerFactory.getLogger(StreamControlCompositeFactory.class);

	private final StreamController streamController;
	private ComboViewer detector;
	private ComboViewer streamType;

	public StreamControlCompositeFactory(StreamController streamController) {
		this.streamController = streamController;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		var composite = new Composite(parent, style);
		GridLayoutFactory.swtDefaults().numColumns(6).applyTo(composite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(composite);

		new Label(composite, SWT.NONE).setText("Detector");
		detector = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		detector.setContentProvider(ArrayContentProvider.getInstance());
		GridDataFactory.fillDefaults().applyTo(detector.getControl());

		new Label(composite, SWT.NONE).setText("Stream");
		streamType = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		streamType.setContentProvider(ArrayContentProvider.getInstance());
		GridDataFactory.fillDefaults().applyTo(streamType.getControl());

		var connect = new Button(composite, SWT.PUSH);
		connect.setText("Connect");
		connect.setImage(ClientSWTElements.getImage(ClientImages.START));
		GridDataFactory.fillDefaults().applyTo(connect);

		var disconnect = new Button(composite, SWT.PUSH);
		disconnect.setText("Disconnect");
		disconnect.setEnabled(false);
		disconnect.setImage(ClientSWTElements.getImage(ClientImages.STOP));
		GridDataFactory.fillDefaults().applyTo(disconnect);

		var cameras = CameraHelper.getAllCameraConfigurationProperties();
		detector.setInput(CameraHelper.getAllCameraConfigurationProperties());
		if (!cameras.isEmpty()) {
			var firstElement = cameras.iterator().next();
			detector.setSelection(new StructuredSelection(firstElement));
			updateStreamType(firstElement);
		}

		detector.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				var selected = (CameraConfigurationProperties) element;
				return selected.getName();
			}
		});

		detector.addSelectionChangedListener(selection -> {
			var selected = (CameraConfigurationProperties) selection.getStructuredSelection().getFirstElement();
			updateStreamType(selected);
		});

		streamType.addSelectionChangedListener(selection -> replaceStream());

		connect.addListener(SWT.Selection, selection -> {
			changeStreamState();
			connect.setEnabled(false);
			disconnect.setEnabled(true);
		});

		disconnect.addListener(SWT.Selection, selection -> {
			changeStreamState();
			disconnect.setEnabled(false);
			connect.setEnabled(true);
		});

		return composite;
	}

	private void updateStreamType(CameraConfigurationProperties camera) {
		var configurationOptional = CameraHelper.createICameraConfiguration(camera).getCameraConfiguration();
		if (configurationOptional.isEmpty()) return;
		var streams = configurationOptional.get().cameraStreamTypes();
		streamType.setInput(streams);
		if (streams.isEmpty()) return;
		streamType.setSelection(new StructuredSelection(streams.iterator().next()));
	}

	private void changeStreamState() {
		try {
			if (streamController.getState() instanceof ListeningState) {
				streamController.idle();
			} else {
				streamController.listen();
			}
		} catch (LiveStreamException e) {
			Status status = new Status(IStatus.ERROR, "PluginID", "Partial camera configuration");
			// Display the dialog
			ErrorDialog.openError(Display.getCurrent().getActiveShell(), "Stream Control Error", e.getMessage(),
					status);
		}
	}

	private void replaceStream() {
		updateStreamController();
		try {
			streamController.update();
		} catch (LiveStreamException e) {
			handleException("Problem replacing stream", e);
		}
	}

	private void updateStreamController() {
		streamController.setControlData(new StreamControlData(getSelectedDetector(), getSelectedStream()));
	}

	private CameraConfigurationProperties getSelectedDetector() {
		var selection = detector.getStructuredSelection().getFirstElement();
		return (CameraConfigurationProperties) selection;
	}

	private void handleException(String message, Exception e) {
		try {
			streamController.idle();
		} catch (LiveStreamException persistedException) {
			logger.error("Error disconnecting stream", persistedException);
		} finally {
			logger.error(message, e);
			UIHelper.showError(message, e);
		}
	}

	private StreamType getSelectedStream() {
		var selection = streamType.getStructuredSelection().getFirstElement();
		return (StreamType) selection;
	}

}
