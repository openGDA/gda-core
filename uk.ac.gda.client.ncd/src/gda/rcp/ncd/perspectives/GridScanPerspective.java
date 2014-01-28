/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.rcp.ncd.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;

public class GridScanPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		{//image and thumbnail grid
			IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.RIGHT, 0.525f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("uk.ac.gda.client.ncd.gridcameraview");
			folderLayout.addView("uk.ac.gda.client.ncd.mappingthumb");
		}

		layout.addView("gda.rcp.jythonterminalview", IPageLayout.BOTTOM	, 0.52f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.gda.arpes.ui.view.samplemetadata", IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.gda.rcp.views.dashboardView", IPageLayout.BOTTOM, 0.65f, "uk.ac.gda.arpes.ui.view.samplemetadata");
		layout.addView("org.dawb.workbench.plotting.views.toolPageView.fixed:org.dawb.workbench.plotting.tools.gridTool", IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);

		{//Acquisition Buttons and Baton Manager
			IFolderLayout folderLayout = layout.createFolder("folder_1", IPageLayout.BOTTOM, 0.81f, "uk.ac.gda.client.ncd.gridcameraview");
			folderLayout.addView("uk.ac.gda.client.ncd.NcdButtonPanelView");
			folderLayout.addView("gda.rcp.views.baton.BatonView");
		}

	}
}