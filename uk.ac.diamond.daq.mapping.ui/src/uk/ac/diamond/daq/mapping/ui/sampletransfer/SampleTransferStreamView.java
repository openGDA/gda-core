/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.sampletransfer;

import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.composite;
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.displayError;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnectionBuilder;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;
import uk.ac.gda.client.live.stream.view.StreamType;

public class SampleTransferStreamView {
	private static final Logger logger = LoggerFactory.getLogger(SampleTransferStreamView.class);

	private static final String TITLE = "Sample Transfer System";
	private static final int STREAM_PLOT_HORIZONTAL_SIZE = 400;
	private static final int STREAM_PLOT_VERTICAL_SIZE = 400;

	private List<CameraConfiguration> cameras;
	private List<LiveStreamConnection> streamConnections = new ArrayList<>();
	private List<LivePlottingComposite> plottingComposites = new ArrayList<>();

	public SampleTransferStreamView(Composite parent, List<CameraConfiguration> cameras) {
		this.cameras = cameras;
		createStreamComposite(parent);
	}

	private void createStreamComposite(Composite container) {
		var plottingComposite = composite(container, 2);
		cameras.forEach(camera -> {
			try {
				var streamConnection = new LiveStreamConnectionBuilder(camera, StreamType.MJPEG).buildAndConnect();
				streamConnections.add(streamConnection);
				createPlottingView(plottingComposite, streamConnection);
			} catch (LiveStreamException e) {
				displayError("Live stream connection error", "Error", e, logger);
			}
		});
	}

	private void createPlottingView(Composite parent, LiveStreamConnection streamConnection) {
		try {
			var plottingComposite = new LivePlottingComposite(parent, SWT.NONE, TITLE, streamConnection);
			GridDataFactory.fillDefaults().hint(STREAM_PLOT_HORIZONTAL_SIZE, STREAM_PLOT_VERTICAL_SIZE).applyTo(plottingComposite);
			plottingComposite.setShowAxes(false);
			plottingComposite.setShowTitle(true);
			plottingComposites.add(plottingComposite);
		} catch (GDAClientException e) {
			displayError("Error creating plotting view", "Error", e, logger);
		}
	}

	public List<LiveStreamConnection> getStreamConnections() {
		return streamConnections;
	}

	public List<LivePlottingComposite> getPlottingComposites() {
		return plottingComposites;
	}

}
