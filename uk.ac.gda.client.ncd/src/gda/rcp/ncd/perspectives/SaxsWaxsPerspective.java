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
		layout.addView("gda.rcp.ncd.views.NCDStatus", IPageLayout.TOP, 1f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.gda.client.live.stream.view.LiveStreamView:Pilatus_SAXS#EPICS_PVA", IPageLayout.RIGHT, 0.3f, "gda.rcp.ncd.views.NCDStatus");
		layout.addView("uk.ac.gda.client.live.stream.view.LiveStreamView:Pilatus_WAXS#EPICS_PVA", IPageLayout.RIGHT, 0.33f, "uk.ac.gda.client.live.stream.view.LiveStreamView:Pilatus_SAXS#EPICS_PVA");
		layout.addView("gda.rcp.jythonterminalview", IPageLayout.RIGHT, 0.5f, "uk.ac.gda.client.live.stream.view.LiveStreamView:Pilatus_WAXS#EPICS_PVA");
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1", IPageLayout.BOTTOM, 0.87f, "gda.rcp.ncd.views.NCDStatus");
			folderLayout.addView("gda.rcp.views.baton.BatonView");
			folderLayout.addView("uk.ac.gda.rcp.views.dashboardView");
		}
	}
}
