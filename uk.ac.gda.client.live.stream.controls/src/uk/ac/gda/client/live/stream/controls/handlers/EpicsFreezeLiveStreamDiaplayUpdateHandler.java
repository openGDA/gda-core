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
 * A handler to toggle (enable/disable) the image update in the {@link LiveStreamView}.
 *<p>
 * This implementation uses enable and disable EPICS Array Plugin to achieve this, so it does not stop the actual camera acquisitions.
 * </p>
 */
public class EpicsFreezeLiveStreamDiaplayUpdateHandler extends AbstractHandler implements IElementUpdater {

	public static final String commandID="uk.ac.gda.client.live.stream.freezecommand";
	private static final Logger logger = LoggerFactory.getLogger(EpicsFreezeLiveStreamDiaplayUpdateHandler.class);
	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private static final String ENABLE_CALLBACKS=":EnableCallbacks";
	private Channel enableCh = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final LiveStreamView liveStreamView = (LiveStreamView) HandlerUtil.getActivePart(event);
		final CameraConfiguration activeCameraConfiguration = liveStreamView.getActiveCameraConfiguration();
		final String arrayPv = activeCameraConfiguration.getArrayPv();
		if (arrayPv == null || arrayPv.isEmpty()) {
			return null;
		}

		try {
			enableCh = EPICS_CONTROLLER.createChannel(arrayPv+ENABLE_CALLBACKS);
			short posi = EPICS_CONTROLLER.cagetEnum(enableCh);
			if (posi == 0) {
				EPICS_CONTROLLER.caput(enableCh,1); // 1 to enable the camera Array Plugin
			} else if (posi==1) {
				EPICS_CONTROLLER.caput(enableCh,0); // 0 to disable the camera Array Plugin
			}
			return null;
		} catch ( CAException | TimeoutException | InterruptedException e) {
			logger.error("Freeze Camera '{}' failed", arrayPv+ENABLE_CALLBACKS, e);
			throw new ExecutionException(String.format("Freeze Camera '%s' failed", arrayPv+ENABLE_CALLBACKS), e);
		}
	}

	@Override
	public void dispose() {
		if (enableCh!= null) {
			enableCh.dispose();
			enableCh=null;
		}
		super.dispose();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void updateElement(UIElement element, Map parameters) {
		element.setText("Freeze");
		element.setTooltip("Enable/disable image update");
		element.setIcon(Activator.getImageDescriptor(ImageConstants.ICON_FREEZE_UPDATE_IMAGE));
	}


}
