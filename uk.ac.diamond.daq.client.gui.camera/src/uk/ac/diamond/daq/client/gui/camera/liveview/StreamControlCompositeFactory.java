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

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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

	private Button connect;
	private Button disconnect;
	private ComboViewer detector;
	private ComboViewer streamType;

	private final StreamController streamController;
	private List<CameraConfigurationProperties> cameras;

	public StreamControlCompositeFactory(StreamController streamController, List<CameraConfigurationProperties> cameras) {
		this.streamController = streamController;
		this.cameras = cameras;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		var composite = new Composite(parent, style);
		GridLayoutFactory.swtDefaults().numColumns(6).applyTo(composite);
		GridDataFactory.swtDefaults().indent(5, 0).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(composite);

		new Label(composite, SWT.NONE).setText("Detector");
		detector = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		detector.setContentProvider(ArrayContentProvider.getInstance());
		GridDataFactory.fillDefaults().applyTo(detector.getControl());

		new Label(composite, SWT.NONE).setText("Stream");
		streamType = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		streamType.setContentProvider(ArrayContentProvider.getInstance());
		GridDataFactory.fillDefaults().applyTo(streamType.getControl());

		connect = new Button(composite, SWT.PUSH);
		connect.setText("Connect");
		connect.setImage(ClientSWTElements.getImage(ClientImages.START));
		GridDataFactory.fillDefaults().applyTo(connect);

		disconnect = new Button(composite, SWT.PUSH);
		disconnect.setText("Disconnect");
		disconnect.setEnabled(false);
		disconnect.setImage(ClientSWTElements.getImage(ClientImages.STOP));
		GridDataFactory.fillDefaults().applyTo(disconnect);

		detector.setInput(cameras);
		var camera = streamController.getControlData().getCameraConfigurationProperties();
		detector.setSelection(new StructuredSelection(camera));
		updateStreamType(camera);

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

		streamType.addSelectionChangedListener(selection -> changeStream());
		connect.addListener(SWT.Selection, selection -> connectStream());
		disconnect.addListener(SWT.Selection, selection -> disconnectStream());

		return composite;
	}

	private CameraConfigurationProperties getSelectedDetector() {
		return (CameraConfigurationProperties) detector.getStructuredSelection().getFirstElement();
	}

	private StreamType getSelectedStream() {
		return (StreamType) streamType.getStructuredSelection().getFirstElement();
	}

	private void connectStream() {
		try {
			streamController.listen();
		} catch (LiveStreamException e) {
			String message = "Error connecting to stream";
			logger.error(message, e);
			UIHelper.showError(message, e);
		}
		enableButtons();
	}

	private void disconnectStream() {
		try {
			streamController.idle();
		} catch (LiveStreamException e) {
			String message = "Error disconnecting to stream";
			logger.error(message, e);
			UIHelper.showError(message, e);
		}
		enableButtons();
	}

	private void enableButtons() {
		if (streamController.getState() instanceof ListeningState) {
			connect.setEnabled(false);
			disconnect.setEnabled(true);
		} else {
			connect.setEnabled(true);
			disconnect.setEnabled(false);
		}
	}

	/*
	 * StreamController sets its new stream connection configuration.
	 * If updates the stream state depending on the current state of the live stream it was connected to:
	 * - Idle State -> it will remain idle
	 * - Listening State -> it will stop listening to the previous stream and will
	 * set a new listeningState on the updated streamController
	 */
	private void changeStream() {
		streamController.setControlData(new StreamControlData(getSelectedDetector(), getSelectedStream()));
		try {
			streamController.update();
		} catch(LiveStreamException e) {
			String message = "Error changing stream";
			logger.error(message, e);
			UIHelper.showError(message, e);
		}
		enableButtons();
	}

	private void updateStreamType(CameraConfigurationProperties camera) {
		var configurationOptional = CameraHelper.createICameraConfiguration(camera).getCameraConfiguration();
		if (configurationOptional.isEmpty()) return;
		var streams = configurationOptional.get().cameraStreamTypes();
		streamType.setInput(streams);
		if (streams.isEmpty()) return;
		streamType.setSelection(new StructuredSelection(streams.iterator().next()));
	}
}