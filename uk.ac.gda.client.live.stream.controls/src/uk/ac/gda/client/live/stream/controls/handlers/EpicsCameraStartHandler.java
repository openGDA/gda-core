/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.controls.handlers;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import uk.ac.gda.client.live.stream.controls.Activator;
import uk.ac.gda.client.live.stream.controls.ImageConstants;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.LiveStreamView;

/**
 * A handler to start the camera giving the image in this {@link LiveStreamView}.
 * <p>
 * This implementation starts the camera acquisition.
 * </p>
 */
public class EpicsCameraStartHandler extends AbstractHandler implements IElementUpdater {

	public static final String commandID = "uk.ac.gda.client.live.stream.startcommand";
	private static final Logger logger = LoggerFactory.getLogger(EpicsCameraStartHandler.class);
	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private static final String CAM_ACQUIRE = ":CAM:Acquire";
	private Channel startCh = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final LiveStreamView liveStreamView = (LiveStreamView) HandlerUtil.getActivePart(event);
		final CameraConfiguration activeCameraConfiguration = liveStreamView.getActiveCameraConfiguration();
		final String arrayPv = activeCameraConfiguration.getArrayPv();
		if (arrayPv == null || arrayPv.isEmpty()) {
			return null;
		}
		try {
			startCh = EPICS_CONTROLLER.createChannel(arrayPv.split(":")[0] + CAM_ACQUIRE);
			EPICS_CONTROLLER.caput(startCh, 1); // 1 to start the camera acquisitions
			return null;
		} catch (CAException | TimeoutException | InterruptedException e) {
			logger.error("Start Camera '{}' failed", arrayPv.split(":")[0] + CAM_ACQUIRE, e);
			throw new ExecutionException(String.format("Start Camera '%s' failed", arrayPv.split(":")[0] + CAM_ACQUIRE), e);
		}
	}

	@Override
	public void dispose() {
		if (startCh != null) {
			startCh.dispose();
			startCh = null;
		}
		super.dispose();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void updateElement(UIElement element, Map parameters) {
		element.setText("Start");
		element.setTooltip("Start camera acquisition");
		element.setIcon(Activator.getImageDescriptor(ImageConstants.ICON_CAMERA_START));
	}

}