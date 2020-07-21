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
import org.eclipse.ui.IPerspectiveFactory;

public class ArpesRemoteAlignmentPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);

		IFolderLayout detectorPlots = layout.createFolder("detector_plots", IPageLayout.RIGHT, 0.05f, IPageLayout.ID_EDITOR_AREA);
		detectorPlots.addView("uk.ac.gda.client.arpes.cameraview");
		detectorPlots.addView("uk.ac.gda.client.arpes.sumview");

		layout.addView("uk.ac.gda.arpes.ContinuousModeControllerView", IPageLayout.RIGHT, 0.65f, "detector_plots");

		IFolderLayout jythonDashBaton = layout.createFolder("jython_dash_baton", IPageLayout.BOTTOM, 0.75f, "detector_plots");
		jythonDashBaton.addView("gda.rcp.views.baton.BatonView");
		jythonDashBaton.addView("gda.rcp.jythonterminalview");
		jythonDashBaton.addView("uk.ac.gda.rcp.views.dashboardView");

		layout.addView("uk.ac.gda.client.live.stream.view.LiveStreamView:es_i1_live_stream#MJPEG", IPageLayout.TOP, 0.6f, "uk.ac.gda.arpes.ContinuousModeControllerView");
		layout.addView("uk.ac.gda.arpes.ui.analysermonitoring", IPageLayout.TOP, 0.5f, "uk.ac.gda.client.live.stream.view.LiveStreamView:es_i1_live_stream#MJPEG");
		layout.addView("uk.ac.gda.client.live.stream.view.LiveStreamView:es_i2_live_stream#MJPEG", IPageLayout.RIGHT, 0.5f, "uk.ac.gda.client.live.stream.view.LiveStreamView:es_i1_live_stream#MJPEG");
	}
}