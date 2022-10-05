/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArpesRemoteExperimentPerspective extends ArpesExperimentPerspective {

	private static final Logger logger = LoggerFactory.getLogger(ArpesRemoteExperimentPerspective.class);

	@Override
	public void createInitialLayout(IPageLayout layout) {
		logger.info("Building ARPES experiment perspective");

		layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.TOP, 0.5f, IPageLayout.ID_EDITOR_AREA);

		IFolderLayout detectorPlots = layout.createFolder("detector_plots", IPageLayout.RIGHT, 0.175f, IPageLayout.ID_PROJECT_EXPLORER);
		detectorPlots.addView("uk.ac.gda.client.arpes.sumview");
		detectorPlots.addView("uk.ac.gda.client.arpes.cameraview");
		detectorPlots.addView("uk.ac.gda.client.arpes.sweptview");

		layout.addView("uk.ac.gda.arpes.ui.analysermonitoring", IPageLayout.RIGHT, 0.595959f, "detector_plots");
		layout.addView("uk.ac.gda.client.liveplotview", IPageLayout.BOTTOM, 0.5f, "uk.ac.gda.arpes.ui.analysermonitoring");

		IFolderLayout jythonAndBaton = layout.createFolder("jython_and_baton", IPageLayout.RIGHT, 0.33333333f, IPageLayout.ID_EDITOR_AREA);
		jythonAndBaton.addView("gda.rcp.jythonterminalview");
		jythonAndBaton.addView("gda.rcp.views.baton.BatonView");

		IFolderLayout sampleMetadataAndDashboard = layout.createFolder("sample_data_and_dashboard", IPageLayout.RIGHT, 0.5f, "jython_and_baton");
		sampleMetadataAndDashboard.addView("uk.ac.gda.arpes.ui.view.samplemetadata");
		sampleMetadataAndDashboard.addView("uk.ac.gda.rcp.views.dashboardView");

		layout.addView("uk.ac.gda.arpes.ui.analyserprogress", IPageLayout.BOTTOM, 0.33f, "sample_data_and_dashboard");

		createExampleArpesFileIfRequired();

		logger.info("Finished building ARPES experiment perspective");
	}
}
