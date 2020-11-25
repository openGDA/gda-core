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

package uk.ac.gda.client.live.stream.handlers;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnectionManager;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.LiveStreamView;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * A handler that adds the current live stream in the LiveStream view as a map in the Mapped Data view.
 */
public class CreateLiveMapHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final LiveStreamView liveStreamView = getLiveStreamView(event);
		final CameraConfiguration cameraConfig = liveStreamView.getActiveCameraConfiguration();
		final StreamType streamType = liveStreamView.getActiveStreamType();
		if (cameraConfig == null || streamType == null) {
			MessageDialog.openError(getShell(), "Error", "Cannot get active live stream.");
			return null;
		}

		final LiveStreamConnection liveStreamConnection;
		try {
			liveStreamConnection = (LiveStreamConnection) LiveStreamConnectionManager.getInstance()
					.getIStreamConnection(
							LiveStreamConnectionManager.getInstance().getIStreamConnection(cameraConfig, streamType));
		} catch (LiveStreamException e) {
			throw new ExecutionException("Error creating live stream", e);
		}
		final IMapFileController mapFileController = PlatformUI.getWorkbench().getService(IMapFileController.class);
		mapFileController.addLiveStream(new LiveStreamPlottable(liveStreamConnection));

		return null;
	}

	private LiveStreamView getLiveStreamView(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (!(part instanceof LiveStreamView)) {
			throw new ExecutionException("Active part should be " + LiveStreamView.class.getName());
		}
		return (LiveStreamView) part;
	}

	private Shell getShell() {
		return Display.getDefault().getActiveShell();
	}

}
