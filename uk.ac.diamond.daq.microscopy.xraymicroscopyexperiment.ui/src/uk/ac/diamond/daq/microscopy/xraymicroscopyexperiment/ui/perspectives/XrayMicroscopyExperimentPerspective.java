/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.microscopy.xraymicroscopyexperiment.ui.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import gda.rcp.views.JythonTerminalView;
import gda.rcp.views.dashboard.DashboardView;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.client.liveplot.LivePlotView;
import uk.ac.gda.epics.adviewer.views.MJPegView;

/**
 * This defines the X-ray Microscopy Experiment perspective<br>
 * To make this perspective available in your client, include
 * uk.ac.diamond.daq.microscopy.xraymicroscopyexperiment.ui
 * <p>
 * In addition, you should ensure that you have a view with id
 * uk.ac.diamond.daq.microscopy.xraymicroscopyexperiment.ui.StatusView.
 * <p>
 * The details of this are view are beamline-specific, but it is intended to
 * show the status of the Ring and beamline (e.g. shutters)
 */

public class XrayMicroscopyExperimentPerspective implements IPerspectiveFactory {

	private static final String STATUS_VIEW_ID = "uk.ac.diamond.daq.microscopy.xraymicroscopyexperiment.ui.StatusView";

	private IPageLayout layout;

	@Override
	public void createInitialLayout(IPageLayout layout) {
		this.layout = layout;
		addViews();
	}

	private void addViews() {
		// Status view at the top
		final String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		final IFolderLayout statusLayout = layout.createFolder("statusView", IPageLayout.TOP, 0.15f, editorArea);
		statusLayout.addView(STATUS_VIEW_ID);

		final IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.30f, editorArea);
		left.addView(IPageLayout.ID_PROJECT_EXPLORER);

		final IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, 0.0f, editorArea);
		right.addView(LivePlotView.ID);

		final IFolderLayout rightBottom = layout.createFolder("rightBottom", IPageLayout.BOTTOM, 0.65f, "right");
		rightBottom.addView(MJPegView.Id);
		rightBottom.addPlaceholder("org.eclipse.ui.browser.view");
		rightBottom.addPlaceholder("data.dispenser.browser");
		rightBottom.addPlaceholder("org.eclipse.ui.browser.view:data.dispenser.browser");

		final IFolderLayout leftBottom = layout.createFolder("leftBottom", IPageLayout.BOTTOM, 0.30f, "left");
		leftBottom.addView(JythonTerminalView.ID);
		leftBottom.addView(DashboardView.ID);
		leftBottom.addPlaceholder(CommandQueueViewFactory.ID);
	}
}