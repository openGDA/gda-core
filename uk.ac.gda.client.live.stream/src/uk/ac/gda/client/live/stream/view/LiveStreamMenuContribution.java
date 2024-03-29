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

package uk.ac.gda.client.live.stream.view;

import static uk.ac.gda.client.live.stream.view.StreamViewUtility.getSecondaryId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.diamond.daq.epics.connector.EpicsV3DynamicDatasetConnector;
import uk.ac.diamond.daq.epics.connector.EpicsV4DynamicDatasetConnector;

/**
 * This processes {@link CameraConfiguration}s in the client Spring and generates the menu items to allow them to be
 * opened. If no {@link CameraConfiguration}s are found it does nothing.
 *
 * @author James Mudd
 */
public class LiveStreamMenuContribution extends ExtensionContributionFactory {

	private static final Logger logger = LoggerFactory.getLogger(LiveStreamMenuContribution.class);

	@Override
	public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
		logger.debug("Adding menu items for live streams...");

		// Find all the implemented cameras. This is currently using the finder but could use OSGi instead.
		final List<CameraConfiguration> cameras = Finder.listLocalFindablesOfType(CameraConfiguration.class);
		final Map<String, CameraConfiguration> cameraMap = new HashMap<>();
		for (CameraConfiguration camConfig : cameras) {
			if (camConfig.getDisplayName() != null) {
				cameraMap.put(camConfig.getDisplayName(), camConfig);
			} else {
				logger.warn("No display name was set for camera id: {}. Using id instead", camConfig.getName());
				cameraMap.put(camConfig.getName(), camConfig);
			}
		}
		if (cameraMap.isEmpty()) {
			logger.debug("No camera configurations found");
			return;
		}

		// We have some cameras
		logger.debug("Found {} cameras", cameras.size());

		// Build a map for each stream type
		final Map<String, CameraConfiguration> mjpegCameras = new TreeMap<>();
		final Map<String, CameraConfiguration> epicsCameras = new TreeMap<>();
		final Map<String, CameraConfiguration> pvaCameras = new TreeMap<>();

		for (Entry<String, CameraConfiguration> cam : cameraMap.entrySet()) {
			// If a URL is set add to MJPEG map
			if (cam.getValue().getUrl() != null) {
				mjpegCameras.put(cam.getKey(), cam.getValue());
			}
			// If PV is set add to EPICS map
			if (cam.getValue().getArrayPv() != null) {
				epicsCameras.put(cam.getKey(), cam.getValue());
			}
			// If PVA PV is set add to PVA map
			if (cam.getValue().getPvAccessPv() != null) {
				pvaCameras.put(cam.getKey(), cam.getValue());
			}
		}

		// Now need to build the menus, first MJPEG
		if (!mjpegCameras.isEmpty()) {
			final MenuManager mjpegMenu = new MenuManager("MJPEG Streams");

			for (Entry<String, CameraConfiguration> cam : mjpegCameras.entrySet()) {
				mjpegMenu.add(createMenuAction(cam, StreamType.MJPEG));
			}

			// Add to the window menu always shown
			additions.addContributionItem(mjpegMenu, Expression.TRUE);
		}

		// Now EPICS Array streams
		if (!epicsCameras.isEmpty()) {
			// Check if EPICS streams can work, i.e. id uk.ac.gda.epics bundle is available
			try {
				// Make the class load if not available will throw
				EpicsV3DynamicDatasetConnector.class.getName();

				// Only build the menu if EPICS streams can work
				final MenuManager epicsMenu = new MenuManager("EPICS Streams");

				for (Entry<String, CameraConfiguration> cam : epicsCameras.entrySet()) {
					epicsMenu.add(createMenuAction(cam, StreamType.EPICS_ARRAY));
				}

				// Add to the window menu always shown
				additions.addContributionItem(epicsMenu, Expression.TRUE);
			}
			catch (NoClassDefFoundError e) {
				logger.error("Camera configurations including EPICS PVs were found but EPICS bundle (uk.ac.gda.epics) is not avaliable", e);
			}
		}

		// PV Access streams
		if (!pvaCameras.isEmpty()) {
			// Check if EPICS streams can work, i.e. id uk.ac.gda.epics bundle is available
			try {
				// Make the class load if not available will throw
				EpicsV4DynamicDatasetConnector.class.getName();

				// Only build the menu if EPICS streams can work
				final MenuManager pvaMenu = new MenuManager("PV Access Streams");

				for (Entry<String, CameraConfiguration> cam : pvaCameras.entrySet()) {
					pvaMenu.add(createMenuAction(cam, StreamType.EPICS_PVA));
				}

				// Add to the window menu always shown
				additions.addContributionItem(pvaMenu, Expression.TRUE);
			}
			catch (NoClassDefFoundError e) {
				logger.error("Camera configurations including PVA PVs were found but EPICS bundle (uk.ac.gda.epics) is not avaliable", e);
			}
		}

		logger.debug("Finished creating menu items for live streams");
	}

	/**
	 * Creates an {@link Action} which will open a {@link LiveStreamView} for the given camera and stream type.
	 *
	 * @param cameraConfig The camera to open
	 * @param streamType The stream type to open
	 * @return The created menu action
	 */
	private Action createMenuAction(Entry<String, CameraConfiguration> cameraConfig, StreamType streamType) {
		return new Action(cameraConfig.getKey()) {
			@Override
			public void run() {
				logger.debug("Opening {} {} stream", cameraConfig.getKey(), streamType.displayName);
				String viewId=LiveStreamView.ID;
				String secondaryId = getSecondaryId(cameraConfig.getValue(), streamType);

				if (StringUtils.isNotBlank(cameraConfig.getValue().getViewID())) {
					// if the CameraConfiguration has viewId set in bean definition, use that
					viewId = cameraConfig.getValue().getViewID();
				} else if (getViewID(secondaryId)!=null) {
					//if the view is already registered by a plugin, return its ID, don't create a new one
					viewId=getViewID(secondaryId);
					secondaryId=null; // since the viewId already contains the secondaryId
				}
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().
						showView(viewId, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
				} catch (PartInitException e) {
					logger.error("Error opening Stream view for {} {}", cameraConfig.getKey(), streamType.displayName, e);
				}
			}
		};
	}

	private String getViewID(String key) {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.views");
		for (IConfigurationElement each : elements) {
			if (each.getAttribute("id").contains(key)) {
				return each.getAttribute("id");
			}
		}
		return null;
	}

}
