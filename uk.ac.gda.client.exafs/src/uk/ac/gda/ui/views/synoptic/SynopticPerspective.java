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

package uk.ac.gda.ui.views.synoptic;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.client.livecontrol.LiveControlsView;

public class SynopticPerspective implements IPerspectiveFactory, IExecutableExtension {

	private static final Logger logger = LoggerFactory.getLogger(SynopticPerspective.class);

	private static final String POSITIONS_FOLDER_NAME = "XES_POSITIONS_FOLDER";
	private static final String SIMULATED_POSITIONS_FOLDER_NAME = "XES_SIMULATED_POSITIONS_FOLDER";

	private SynopticPerspectiveConfiguration configuration;

	@Override
	public void createInitialLayout(IPageLayout layout) {

		if (configuration == null) {
			logger.warn("No configuration object has been set - cannot create SynopticPerspective");
			return;
		}

		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		// Simulated positions view on the right
		IFolderLayout xesOffsetFolder = layout.createFolder(POSITIONS_FOLDER_NAME, IPageLayout.RIGHT, 0.6f, editorArea);
		IFolderLayout simulatedPositionsFolder = layout.createFolder(SIMULATED_POSITIONS_FOLDER_NAME, IPageLayout.BOTTOM, 0.5f, POSITIONS_FOLDER_NAME);
		simulatedPositionsFolder.addView(LiveControlsView.ID+":"+configuration.getSimulatedPositionsView());

		// Crystal analysers views on the left
		IFolderLayout xesCalibrationFolder = layout.createFolder("XES_ANALYSERS", IPageLayout.LEFT, 0.6f, editorArea);
		IFolderLayout xesCrystalsFolder = layout.createFolder("XES_CRYSTALS", IPageLayout.TOP, 0.78f, "XES_ANALYSERS");
		xesCrystalsFolder.addView(SynopticView.ID+":"+configuration.getSpectrometerPicture());
		xesCrystalsFolder.addView(LiveControlsView.ID+":"+configuration.getAllCrystalControls());
		xesCrystalsFolder.addView(LiveControlsView.ID+":"+configuration.getMaterialControls());
		xesCrystalsFolder.addView(LiveControlsView.ID+":"+configuration.getDetectorControls());

		// Add the calibration controls below the simulation positions
		IFolderLayout xesCalibrationControlsFolder = layout.createFolder("XES_CALIB_CONTROLS", IPageLayout.BOTTOM, 0.8f, SIMULATED_POSITIONS_FOLDER_NAME);
		xesCalibrationControlsFolder.addView(LiveControlsView.ID+":"+configuration.getCalibrationControls());

		// Add offsets view to the right of simulated positions
		IFolderLayout offsetsFolder = layout.createFolder("XES_OFFSETS", IPageLayout.RIGHT, 0.5f, SIMULATED_POSITIONS_FOLDER_NAME);
		offsetsFolder.addView(LiveControlsView.ID+":"+configuration.getOffsetView());
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {

		if (propertyName.equals("class") && data instanceof String str) {
			Finder.findOptionalOfType(str, SynopticPerspectiveConfiguration.class)
				.ifPresentOrElse(configObject -> {
					logger.info("Creating SynopticPerspective using configuration object {}", str);
					configuration = configObject;
				},
				() -> logger.warn("Could not find configuration object {} to use for SynopticPerspective", str));
		}

	}
}


