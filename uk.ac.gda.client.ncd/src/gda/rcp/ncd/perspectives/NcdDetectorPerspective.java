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

/**
 * 
 */
public class NcdDetectorPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
//		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		layout.addView("uk.ac.gda.client.ncd.NcdTfgConfigure", IPageLayout.TOP, 0.95f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("gda.rcp.ncd.views.NCDStatus", IPageLayout.LEFT, 0.26f, "uk.ac.gda.client.ncd.NcdTfgConfigure");
		{
			IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.BOTTOM, 0.8f, "uk.ac.gda.client.ncd.NcdTfgConfigure");
			folderLayout.addView("gda.rcp.views.baton.BatonView");
			folderLayout.addView("uk.ac.gda.client.ncd.NcdButtonPanelView");
		}
		layout.addView("gda.rcp.ncd.views.NcdDetectorView", IPageLayout.BOTTOM, 0.5f, "gda.rcp.ncd.views.NCDStatus");
		layout.addView("uk.ac.gda.client.ncd.calibconf", IPageLayout.BOTTOM, 0.5f, "gda.rcp.ncd.views.NcdDetectorView");
	}
}