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

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;

import java.util.Optional;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.LiveStreamMapObject;
import org.eclipse.january.dataset.IDynamicShape;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.ui.PlatformUI;

import gda.factory.Finder;
import uk.ac.diamond.daq.client.gui.camera.CameraStreamsManager;
import uk.ac.diamond.daq.mapping.ui.services.MappingRemoteServices;
import uk.ac.gda.client.live.stream.calibration.PixelCalibration;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;

/**
 * Handles the stream plotting for {@link EnableLiveBackgroundHandler} and {@link LiveStreamBackgroundAction}.
 *
 * @author Maurizio Nagni
 */
public class BackgroundStateHelper {
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
		getMappingRemoteServices().getIMapFileController().registerUpdates(null);
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
		liveStreamMap.ifPresent(ls -> getMappingRemoteServices().getIMapFileController().addLiveStream(ls));
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
	 * @return A mappable version of the default stream source, {@code null} otherwise.
	 */
	private Optional<LiveStreamMapObject> getDefaultStreamSource() {
		return Optional.ofNullable(getAssociatedCameraConfiguration())
				.map(this::getLiveStreamObject)
				.orElse(Optional.empty());
	}

	public CameraConfiguration getAssociatedCameraConfiguration() {
		return (CameraConfiguration) Optional
				.ofNullable(getClientRemoteServices().getIStageScanConfiguration())
				.map(IStageScanConfiguration::getDefaultStreamSourceConfig).map(Finder::find).orElse(null);
	}

	private Optional<LiveStreamMapObject> getLiveStreamObject(CameraConfiguration cameraConfig) {
		final StreamType streamType = cameraConfig.getArrayPv() != null ? StreamType.EPICS_ARRAY : StreamType.MJPEG;
		CameraStreamsManager manager = getBean(CameraStreamsManager.class);
		IDynamicShape dataset = manager.getDynamicShape(cameraConfig, streamType);
		cameraConfig.setCalibratedAxesProvider(new PixelCalibration(dataset::getDataset));
		return Optional.ofNullable(manager.getLiveStreamPlottable(cameraConfig, streamType));
	}

	private MappingRemoteServices getMappingRemoteServices() {
		return getBean(MappingRemoteServices.class);
	}

	private ClientRemoteServices getClientRemoteServices() {
		return getBean(ClientRemoteServices.class);
	}
}
