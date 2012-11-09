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

package gda.rcp.ncd.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;

public class SetupPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);

		layout.addView("uk.ac.gda.client.liveplotview", IPageLayout.TOP, 0.95f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("gda.rcp.ncd.views.NCDStatus", IPageLayout.LEFT, 0.31f, "uk.ac.gda.client.liveplotview");
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1", IPageLayout.BOTTOM, 0.87f, "uk.ac.gda.client.liveplotview");
			folderLayout.addView("uk.ac.gda.client.ncd.NcdButtonPanelView");
			folderLayout.addView("gda.rcp.views.baton.BatonView");
		}
		layout.addView("uk.ac.gda.client.ncd.plot.peak", IPageLayout.TOP, 0.43f, "uk.ac.gda.client.liveplotview");
		layout.addView("uk.ac.gda.client.ncd.plot.edge", IPageLayout.RIGHT, 0.5f, "uk.ac.gda.client.ncd.plot.peak");
		layout.addView("gda.rcp.jythonterminalview", IPageLayout.BOTTOM, 0.42f, "gda.rcp.ncd.views.NCDStatus");
		{
			IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.BOTTOM, 0.67f, "gda.rcp.jythonterminalview");
			folderLayout.addView("gda.rcp.ncd.views.WatchView");
//			folderLayout.addView("uk.ac.gda.exafs.ui.dashboardView");
		}
	}
}