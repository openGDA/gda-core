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

package uk.ac.gda.arpes.perspectives;

import java.util.Optional;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.apres.ui.config.HRMonitoringPerspectiveConfiguration;

public class HRMonitoringPerspective implements IPerspectiveFactory {

	private static final Logger logger = LoggerFactory.getLogger(HRMonitoringPerspective.class);

	private final String LIVE_STREAM_VIEW_ID = "uk.ac.gda.client.live.stream.view.LiveStreamView:%s#MJPEG";
	private final String UNCONFIGURED_CAMERA_VIEW_ID = "uk.ac.gda.arpes.ui.unconfiguredcamera";
	private final String MONITORING_PANEL_ID = "uk.ac.gda.arpes.ui.monitoringpanel";
	private final String ANALYSER_MONITORING_VIEW_ID = "uk.ac.gda.arpes.ui.analysermonitoring";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);

		Optional<HRMonitoringPerspectiveConfiguration> config;
		try {
			config = Finder.findOptionalLocalSingleton(HRMonitoringPerspectiveConfiguration.class);
		} catch (IllegalArgumentException exception) {
			logger.error("HRMonitoringPerspectiveConfiguration is not a singleton", exception);
			return;
		}

		if (config.isPresent()) {
			createLayout(layout, config.get());
		} else {
			logger.error("No HRMonitoringPerspectiveConfiguration found.");
		}
	}

	private void createLayout(IPageLayout layout, HRMonitoringPerspectiveConfiguration config) {
		String camera1ViewId = makeLiveStreamViewOrUnconfiguredCameraViewId(config.getCamera1Name());
		String camera2ViewId = makeLiveStreamViewOrUnconfiguredCameraViewId(config.getCamera2Name());
		String camera3ViewId = makeLiveStreamViewOrUnconfiguredCameraViewId(config.getCamera3Name());

		layout.addView(camera1ViewId, IPageLayout.RIGHT, 0.05f, IPageLayout.ID_EDITOR_AREA);
		layout.addView(MONITORING_PANEL_ID, IPageLayout.RIGHT, 0.3333f, camera1ViewId);
		layout.addView(camera2ViewId, IPageLayout.TOP, 0.70f, MONITORING_PANEL_ID);
		layout.addView(camera3ViewId, IPageLayout.RIGHT, 0.5f, camera2ViewId);
		layout.addView(ANALYSER_MONITORING_VIEW_ID, IPageLayout.BOTTOM, 0.70f, camera3ViewId);

	}

	private String makeLiveStreamViewOrUnconfiguredCameraViewId(String cameraName) {
		if (cameraName != null && !cameraName.isEmpty()) {
			return String.format(LIVE_STREAM_VIEW_ID , cameraName);
		}

		return UNCONFIGURED_CAMERA_VIEW_ID;
	}
}
