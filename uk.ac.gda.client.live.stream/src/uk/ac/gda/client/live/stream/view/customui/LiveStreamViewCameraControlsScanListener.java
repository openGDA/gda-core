/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view.customui;

import java.net.URISyntaxException;
import java.util.Optional;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.ImageMode;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.client.properties.camera.StreamConfiguration;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;

public class LiveStreamViewCameraControlsScanListener extends LiveStreamViewCameraControls {
	private static Logger logger = LoggerFactory.getLogger(LiveStreamViewCameraControlsScanListener.class);

	private ISubscriber<IBeanListener<StatusBean>> scanSubscriber;

	private Optional<ImageMode> imageMode;
	private Optional<Short> triggerMode;

	public LiveStreamViewCameraControlsScanListener(CameraConfigurationProperties camera, CameraControl cameraControl){
		super(cameraControl);
		changeExposureWhileCameraAcquiring = true;
		imageMode = Optional.ofNullable(camera.getStreamingConfiguration()).map(StreamConfiguration::getImageMode);
		triggerMode = Optional.ofNullable(camera.getStreamingConfiguration()).map(StreamConfiguration::getTriggerMode);

		try {
			addListeners();
		} catch (Exception e) {
			logger.error("Could not create subscriber to the status queue", e);
		}
	}

	private void addListeners() throws URISyntaxException, EventException {
		ClientRemoteServices remoteServices = SpringApplicationContextFacade.getBean(ClientRemoteServices.class);
		scanSubscriber = remoteServices.createSubscriber(EventConstants.STATUS_TOPIC);
		scanSubscriber.addListener(this::scanListener);
	}

	private void scanListener(BeanEvent<StatusBean> event) {
		boolean activeScan = event.getBean().getStatus().isActive();
		Display.getDefault().asyncExec(() -> {
			enableControls(!activeScan);
			if (activeScan) {
				setTooltip("A scan is running or pending");
			}
		});
	}
	@Override
	protected void startAcquiring(SelectionEvent e) {
		try {
			if (imageMode.isPresent()) {
				cameraControl.setImageMode(imageMode.get());
			}
			if (triggerMode.isPresent()) {
				cameraControl.setTriggerMode(triggerMode.get());
			}
			super.startAcquiring(e);
		} catch (Exception ex) {
			logger.error("Error starting data acquisition", ex);
		}
	}

	@Override
	public void dispose() {
		scanSubscriber.removeAllListeners();
		try {
			scanSubscriber.disconnect();
		} catch (EventException e) {
			logger.error("Could not disconnect subscriber", e);
		}
		super.dispose();
	}

}
