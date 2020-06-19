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
package uk.ac.diamond.daq.mapping.ui;
import java.util.Optional;
import java.util.UUID;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import gda.factory.Finder;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnectionManager;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.handlers.LiveStreamPlottable;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
/**
 * Handles the stream plotting for {@link EnableLiveBackgroundHandler} and {@link EnableMappingLiveBackgroundAction}.
 *
 * @author Maurizio Nagni
 */
class BackgroundStateHelper {
	private Optional<LiveStreamMapObject> liveStreamMap = Optional.empty();
	/**
	 * Sets visible, or not, the livestream view associated with the mapping view and updates the
	 * {@link IMapFileController}. If necessary initialises the livestream.
	 *
	 * @param plotStream
	 *            {@code true} to display the stream, {@code false} otherwise.
	 */
	public void show(boolean plotStream) {
		if (!liveStreamMap.isPresent()) {
			initialiseStream();
		}
		liveStreamMap.ifPresent(ls -> ls.setPlotted(plotStream));
		PlatformUI.getWorkbench().getService(IMapFileController.class).registerUpdates(null);
	}
	/**
	 * @return {@code true} if the stream is plotted, {@code false} if is not plotted or is still not initialised
	 */
	public boolean isPlotted() {
		return liveStreamMap.map(LiveStreamMapObject::isPlotted).orElse(false);
	}
	/**
	 * Establishes the Live stream connection and links it to the mapping view. If no suitable default connection has
	 * been defined a dialog is displayed informing the user what should be done to correct this.
	 */
	private void initialiseStream() {
		getDefaultStreamSource().ifPresent(ls -> {
			liveStreamMap = Optional.ofNullable(ls);
			IWorkbench workbench = PlatformUI.getWorkbench();
			IMapFileController mapFileController = workbench.getService(IMapFileController.class);
			mapFileController.addLiveStream(ls);
		});
		if (!liveStreamMap.isPresent()) {
			MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Missing Camera Configuration",
					"No default Camera Configuration is set,\n"
							+ "Please add the name of a valid CameraConfiguration bean\n"
							+ "to the mapping_stage_info bean in your mapping.xml file");
		}
	}
	/**
	 * Obtains the packaged stream source identified as the default for the beamline
	 *
	 * @return An {@link Optional} of the mappable version of the default stream source, empty if none has been set.
	 * @throws LiveStreamException
	 *             If the connection to the source is unsuccessful
	 */
	private Optional<LiveStreamMapObject> getDefaultStreamSource() {
		String defaultConfigName = null;
		IStageScanConfiguration stageConfig = PlatformUI.getWorkbench().getService(IStageScanConfiguration.class);
		if (stageConfig != null) {
			defaultConfigName = stageConfig.getDefaultStreamSourceConfig();
		}
		if (defaultConfigName == null) {
			return Optional.empty();
		}
		Optional<CameraConfiguration> config = Finder.findOptional(defaultConfigName);
		return config.map(this::getLiveStreamObject).orElse(Optional.empty());
	}
	private Optional<LiveStreamMapObject> getLiveStreamObject(CameraConfiguration config) {
		StreamType streamType = config.getArrayPv() != null ? StreamType.EPICS_ARRAY : StreamType.MJPEG;
		UUID uuid;
		try {
			uuid = LiveStreamConnectionManager.getInstance().getIStreamConnection(config, streamType);
		} catch (LiveStreamException e) {
			return Optional.empty();
		}
		LiveStreamConnection connection = LiveStreamConnection.class
				.cast(LiveStreamConnectionManager.getInstance().getIStreamConnection(uuid));
		return Optional.of(new LiveStreamPlottable(connection));
	}
}
