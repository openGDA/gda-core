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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import uk.ac.gda.client.live.stream.controls.Activator;
import uk.ac.gda.client.live.stream.controls.ImageConstants;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.LiveStreamView;

/**
 * A handler to stop the camera giving the image in this {@link LiveStreamView}.
 * <p>
 * This implementation stops the camera acquisition.
 * </p>
 */
public class EpicsCameraStopHandler extends AbstractHandler implements IElementUpdater {

	private static final Logger logger =  LoggerFactory.getLogger(EpicsCameraStopHandler.class);
	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private static final String CAM_ACQUIRE=":CAM:Acquire";
	private Channel startCh = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final LiveStreamView liveStreamView = (LiveStreamView) HandlerUtil.getActivePart(event);
		final CameraConfiguration activeCameraConfiguration = liveStreamView.getActiveCameraConfiguration();
		final String arrayPv = activeCameraConfiguration.getArrayPv();
		String pvName = null;
		if (LocalProperties.isDummyModeEnabled() || LocalProperties.get(LocalProperties.GDA_MODE).equals("dummy")) {
			pvName = createPV(arrayPv);
		} else {
			// get from command parameter - support direct setting of PV name in command in live mode
			pvName = event.getParameter("uk.ac.gda.client.live.stream.controls.stop.PVname");
			if (pvName == null) {
				pvName = createPV(arrayPv);
			}
		}
		if (pvName == null) {
			logger.error("{}: PV name for camera stop command is not set!", activeCameraConfiguration.getName());
			throw new ExecutionException(String.format("%s: PV name for camera stop command is not set!", activeCameraConfiguration.getName()));
		}

		try {
			startCh = EPICS_CONTROLLER.createChannel(pvName);
			EPICS_CONTROLLER.caput(startCh,0); // 0 to stop the camera acquisitions
			return null;
		} catch ( CAException | TimeoutException e) {
			throw new ExecutionException(String.format("Stop Camera '%s' failed", pvName), e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ExecutionException(String.format("Stop Camera '%s' failed", pvName), e);
		}
	}

	private String createPV(String arrayPv) {
		if (StringUtils.isBlank(arrayPv))
			return null;
		return arrayPv.split(":")[0] + CAM_ACQUIRE;
	}

	@Override
	public void dispose() {
		if (startCh!= null) {
			startCh.dispose();
			startCh=null;
		}
		super.dispose();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void updateElement(UIElement element, Map parameters) {
		element.setText("Stop");
		element.setTooltip("Stop camera acquisition");
		element.setIcon(Activator.getImageDescriptor(ImageConstants.ICON_CAMERA_STOP));
	}

}
