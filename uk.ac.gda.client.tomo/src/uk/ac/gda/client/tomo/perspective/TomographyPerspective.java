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

package uk.ac.gda.client.tomo.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.gda.client.tomo.alignment.view.factory.TomoAlignmentViewFactory;
import uk.ac.gda.client.tomo.configuration.view.factory.TomoConfigurationViewFactory;

/**
 * Perspective for the Tomography alignment - contains just one view which is described on <a href
 * ="http://confluence.diamond.ac.uk/pages/viewpage.action?pageId=5013516"> Requirements page</a>
 */
public class TomographyPerspective implements IPerspectiveFactory {
	public static final String ID = "uk.ac.gda.client.tomo.perspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		final String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);

		final IFolderLayout mainFolder = layout.createFolder("MAIN_FOLDER", IPageLayout.TOP, IPageLayout.RATIO_MAX, editorArea);
		mainFolder.addView(TomoAlignmentViewFactory.ID);
		mainFolder.addView(TomoConfigurationViewFactory.ID);
		layout.addShowViewShortcut(TomoAlignmentViewFactory.ID);
		layout.addShowViewShortcut(TomoConfigurationViewFactory.ID);
	}
}
