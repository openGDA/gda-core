/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArpesSlicingPerspective implements IPerspectiveFactory{
	private static final Logger logger = LoggerFactory.getLogger(ArpesSlicingPerspective.class);

	@Override
	public void createInitialLayout(IPageLayout layout) {
		logger.debug("Start building ARPES 3D Slicing perspective");
		layout.setEditorAreaVisible(true);
		{
			IFolderLayout folderLayout = layout.createFolder("folder_2", IPageLayout.BOTTOM, 0.55f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("gda.rcp.jythonterminalview");
			folderLayout.addView("gda.rcp.views.baton.BatonView");
		}
		layout.addView("uk.ac.gda.arpes.ui.view.samplemetadata", IPageLayout.LEFT, 0.41f, "folder_2");
		layout.addView("uk.ac.gda.arpes.ui.analyserprogress", IPageLayout.BOTTOM, 0.62f, "uk.ac.gda.arpes.ui.view.samplemetadata");

		layout.addView("org.eclipse.ui.navigator.ProjectExplorer", IPageLayout.LEFT, 0.15f, IPageLayout.ID_EDITOR_AREA);

		{
			IFolderLayout folderLayout = layout.createFolder("folder_1", IPageLayout.RIGHT, 0.22f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("uk.ac.gda.client.arpes.slicingview");

		}

	}

}
