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

package uk.ac.diamond.daq.client.gui.camera.settings.views;

import java.util.Optional;
import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnectionManager;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.api.ILiveStreamConnectionManager;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.client.properties.CameraProperties;

/**
 * Creates a predefined view for the imaging camera stream. This view defines the {@link #IMAGING_CAMERA_ID} property which has to be equal to one
 * {@code client.cameraConfigurations} available IDs.
 *
 * @see CameraHelper
 *
 * @author Maurizio Nagni
 */
public class ImagingCameraView extends ViewPart {

	public static final String ID = "uk.ac.diamond.daq.client.gui.camera.settings.views.ImagingCameraView";

	public static final String IMAGING_CAMERA_ID = "imagingCameraView.id";
	private static final Logger logger = LoggerFactory.getLogger(ImagingCameraView.class);

	private CameraImageComposite cic;

	@Override
	public void createPartControl(Composite parent) {
		try {
			cic = new CameraImageComposite(parent, SWT.NONE, getLiveStreamConnection());
		} catch (GDAClientException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		} catch (LiveStreamException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		}
	}

	@Override
	public void setFocus() {
		// Do not necessary
	}

	private LiveStreamConnection getLiveStreamConnection() throws LiveStreamException {
		Optional<CameraProperties> cameraProperties = CameraHelper
				.getCameraPropertiesByID(LocalProperties.get(IMAGING_CAMERA_ID, null));
		if (!cameraProperties.isPresent()) {
			throw new LiveStreamException("No Camera Confguration found");
		}
		CameraConfiguration cc = CameraHelper.getCameraConfiguration(cameraProperties.get().getIndex())
				.orElseThrow(() -> new LiveStreamException("No Camera Confguration found"));
		ILiveStreamConnectionManager manager = LiveStreamConnectionManager.getInstance();
		UUID connectionID = manager.getIStreamConnection(cc, StreamType.EPICS_ARRAY);
		return LiveStreamConnection.class.cast(manager.getIStreamConnection(connectionID));
	}
}
