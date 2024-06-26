/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;

import gda.rcp.views.JythonTerminalView;
import gda.rcp.views.dashboard.DashboardView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.gda.client.liveplot.LivePlotView;

public class AlignmentPerspective implements IPerspectiveFactory {

	public static final String ID = "org.diamond.exafs.ui.AlignmentPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		IFolderLayout folderLayout_0 = layout.createFolder("folder10", IPageLayout.LEFT, 0.55f, editorArea);
		folderLayout_0.addView(LivePlotView.ID);

		IFolderLayout folderLayout_2 = layout.createFolder("folder1", IPageLayout.RIGHT, 0.55f, "folder2");
		folderLayout_2.addView(DashboardView.ID);

		IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.BOTTOM, 0.5f, "folder1");
		folderLayout.addView(JythonTerminalView.ID);
	}

}
