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

public class ArpesAlignmentPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);

		{
			IFolderLayout folderLayout = layout.createFolder("folder_3", IPageLayout.RIGHT, 0.62f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("uk.ac.gda.client.arpes.sumview");
		}
		layout.addView("uk.ac.gda.rcp.views.dashboardView", IPageLayout.TOP, 0.28f, "folder_3");
		layout.addView("uk.ac.gda.arpes.ContinuousModeControllerView", IPageLayout.TOP, 0.37f, "folder_3");
		{
			IFolderLayout folderLayout = layout.createFolder("folder_2", IPageLayout.BOTTOM, 0.5f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("gda.rcp.views.baton.BatonView");
			folderLayout.addView("gda.rcp.jythonterminalview");
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1", IPageLayout.TOP, 0.79f, "folder_2");
			folderLayout.addView("uk.ac.gda.client.arpes.cameraview");
		}
	}
}