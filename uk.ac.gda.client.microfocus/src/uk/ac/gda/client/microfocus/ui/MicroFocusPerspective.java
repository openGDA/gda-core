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

package uk.ac.gda.client.microfocus.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.gda.client.CommandQueueViewFactory;

public class MicroFocusPerspective implements IPerspectiveFactory {

	public static final String ID = "uk.ac.gda.microfocus.ui.MicroFocusPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		defineLayout(layout);
	}

	private void defineLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		
		IFolderLayout rightfolder = layout.createFolder("right", IPageLayout.RIGHT, 0.6f, editorArea);
		IFolderLayout leftFolder = layout.createFolder("left", IPageLayout.LEFT, 0.16f, editorArea);
		IFolderLayout outputfolder = layout.createFolder("top", IPageLayout.TOP, 0.76f, editorArea);
		
		IFolderLayout detfolder = layout.createFolder("middle", IPageLayout.BOTTOM, 0.05f, IPageLayout.ID_EDITOR_AREA);
		
		outputfolder.addView("uk.ac.gda.beamline.i18.MapView");
		
		leftFolder.addView("uk.ac.gda.client.microfocus.SelectExafsView");
		leftFolder.addView("uk.ac.gda.client.microfocus.XspressElementListView");
		leftFolder.addView("uk.ac.gda.client.microfocus.VortexElementListView");
		
		detfolder.addView(CommandQueueViewFactory.ID);
		detfolder.addView("gda.rcp.jythonterminalview");

		detfolder.addView(CommandQueueViewFactory.ID);
		rightfolder.addView("uk.ac.gda.beamline.i18.McaView");
		
		layout.setEditorAreaVisible(false);
	}
	

}
