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

package gda.rcp.ncd.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class SaxsWaxsPerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		// String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		{
			IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.RIGHT, 0.95f,
					IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("uk.ac.gda.client.ncd.saxsview");
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder_2", IPageLayout.LEFT, 0.2f, "folder");
			folderLayout.addView("gda.rcp.ncd.views.NCDStatus");
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1", IPageLayout.BOTTOM, 0.5f,
					"gda.rcp.ncd.views.NCDStatus");
			folderLayout.addView("gda.rcp.ncd.views.NcdDataSourceSaxs");
			folderLayout.addView("gda.rcp.ncd.views.NcdDataSourceWaxs");
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder_3", IPageLayout.BOTTOM, 0.85f, "folder");
			folderLayout.addView("uk.ac.gda.client.ncd.NcdButtonPanelView");
			folderLayout.addView("gda.rcp.views.baton.BatonView");
			folderLayout.addView("uk.ac.gda.rcp.views.dashboardView");
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder_4", IPageLayout.RIGHT, 0.5f, "folder");
			folderLayout.addView("uk.ac.gda.client.ncd.waxsview");
		}
	}
}
