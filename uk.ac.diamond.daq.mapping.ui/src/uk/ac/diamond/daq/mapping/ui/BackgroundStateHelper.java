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

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.diamond.osgi.services.ServiceProvider;
import uk.ac.gda.client.live.stream.LiveStreamConnectionBuilder;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.handlers.LiveStreamPlottable;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * Handles the stream plotting for {@link EnableLiveBackgroundHandler} and {@link LiveStreamBackgroundAction}.
 *
 * @author Maurizio Nagni
 */
public class BackgroundStateHelper {

	private static final Logger logger = LoggerFactory.getLogger(BackgroundStateHelper.class);

	private Optional<LiveStreamMapObject> liveStreamMap = Optional.empty();

	private long connectionRetryInterval = 200L;
	private int numConnectionAttempts = 2;

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
		getIMapFileController().registerUpdates(null);
	}

	/**
	 * @return {@code true} if the stream is plotted, {@code false} if is not plotted or is still not initialised
	 */
	public boolean isPlotted() {
		return liveStreamMap
				.map(LiveStreamMapObject::isPlotted)
				.orElse(false);
	}

	/**
	 * Establishes the Live stream connection and links it to the mapping view. If no suitable default connection has
	 * been defined a dialog is displayed informing the user what should be done to correct this.
	 */
	private void initialiseStream() {
		liveStreamMap = getDefaultStreamSource();
		if (liveStreamMap.isPresent()) {
			getIMapFileController().addLiveStream(liveStreamMap.get());
		} else {
			MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Missing Camera Configuration",
					"No default Camera Configuration is defined.\n"
					+ "Please add the name of a valid CameraConfiguration bean\n"
					+ "to the MappingStageInfo configuration");
		}
	}

	/**
	 * Obtains the mappable stream source identified as the default for the beamline
	 */
	private Optional<LiveStreamMapObject> getDefaultStreamSource() {
		return getDefaultStreamSourceConfiguration().map(this::getLiveStreamObjectRetry).orElse(Optional.empty());
	}

	public Optional<CameraConfiguration> getDefaultStreamSourceConfiguration() {
		return Optional.ofNullable(getIStageScanConfiguration())
				.map(IStageScanConfiguration::getDefaultStreamSourceConfig).map(Finder::find);
	}

	private Optional<LiveStreamMapObject> getLiveStreamObjectRetry(CameraConfiguration cameraConfig) {
		Optional<LiveStreamMapObject> stream = getLiveStreamObject(cameraConfig);
		int numRetries = numConnectionAttempts;
		while (stream.isEmpty() && numRetries-- > 0) {
			logger.warn("Live stream is empty, retrying to connect again}");
			try {
				Thread.sleep(connectionRetryInterval);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			stream =  getLiveStreamObject(cameraConfig);
		}
		return stream;
	}

	private Optional<LiveStreamMapObject> getLiveStreamObject(CameraConfiguration cameraConfig) {
		final StreamType streamType = getStreamType(cameraConfig);
		try {
			var stream = new LiveStreamConnectionBuilder(cameraConfig, streamType).buildAndConnect();
			logger.warn("Stream connected : {}", stream.isConnected());
			LiveStreamPlottable plottable = new LiveStreamPlottable(stream);
			plottable.connect();
			return Optional.of(plottable);
		} catch (LiveStreamException e) {
			logger.error("Error retrieving live stream", e);
			return Optional.empty();
		}
	}

	private StreamType getStreamType(CameraConfiguration cameraConfig) {
		// assume the preference is PVA array, then Epics Array, then MJPEG
		if (cameraConfig.getPvAccessPv() != null) {
			return StreamType.EPICS_PVA;
		} else if (cameraConfig.getArrayPv() != null) {
			return StreamType.EPICS_ARRAY;
		} else if (cameraConfig.getUrl() != null) {
			return StreamType.MJPEG;
		}
		throw new IllegalStateException(String.format("No stream defined in %s '%s'", cameraConfig.getClass().getName(), cameraConfig.getName()));
	}

	private IMapFileController getIMapFileController() {
		return ServiceProvider.getService(IMapFileController.class);

	}

	private IStageScanConfiguration getIStageScanConfiguration() {
		return ServiceProvider.getService(IStageScanConfiguration.class);
	}
}