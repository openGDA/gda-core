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
		{
			IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.RIGHT, 0.6f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("uk.ac.gda.client.ncd.gridcameraview");
			folderLayout.addView("uk.ac.gda.client.ncd.mappingthumb");
		}
<<<<<<< Updated upstream
		layout.addView("uk.ac.gda.rcp.views.dashboardView", IPageLayout.TOP, 0.15f, "uk.ac.gda.client.ncd.gridcameraview");
		layout.addView("gda.rcp.jythonterminalview", IPageLayout.LEFT, 0.8f, IPageLayout.ID_EDITOR_AREA);
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1", IPageLayout.BOTTOM, 0.6f, "gda.rcp.jythonterminalview");
		//	folderLayout.addView("org.dawb.workbench.plotting.views.toolPageView.2D");//uk.ac.gda.client.ncd.gridDetails");//, IPageLayout.BOTTOM, 0.4f, IPageLayout.ID_EDITOR_AREA);//"gda.rcp.jythonterminalview");
			folderLayout.addView("org.dawb.workbench.plotting.views.toolPageView.fixed:org.dawb.workbench.plotting.tools.gridTool");//uk.ac.gda.client.ncd.gridDetails");//, IPageLayout.BOTTOM, 0.4f, IPageLayout.ID_EDITOR_AREA);//"gda.rcp.jythonterminalview");
=======
		{
			IFolderLayout folderLayout = layout.createFolder("folder_2", IPageLayout.TOP, 0.3f, "uk.ac.gda.client.ncd.gridcameraview");
			folderLayout.addView("uk.ac.gda.arpes.ui.view.samplemetadata");
			folderLayout.addView("uk.ac.gda.rcp.views.dashboardView");
			//		layout.addView("uk.ac.gda.rcp.views.dashboardView", IPageLayout.TOP, 0.15f, "uk.ac.gda.client.ncd.gridcameraview");
		}
		layout.addView("gda.rcp.jythonterminalview", IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1", IPageLayout.BOTTOM, 0.5f, "gda.rcp.jythonterminalview");
			folderLayout.addView("org.dawb.workbench.plotting.views.toolPageView.fixed:org.dawb.workbench.plotting.tools.gridTool");
>>>>>>> Stashed changes
			folderLayout.addView("uk.ac.gda.client.ncd.NcdButtonPanelView");
			folderLayout.addView("gda.rcp.views.baton.BatonView");
		}
	}
}