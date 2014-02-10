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

package uk.ac.gda.client.hrpd.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class HrpdScriptingPerspective implements IPerspectiveFactory {

	static final String ID = "uk.ac.gda.client.hrpd.ScriptingPerspective";
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		// Get the editor area.
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		// Top left: Resource Navigator view and Bookmarks view placeholder
//		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f,	editorArea);
//		topLeft.addView(IPageLayout.ID_RES_NAV);
//		topLeft.addPlaceholder(IPageLayout.ID_BOOKMARKS);

		// Bottom left: Outline view and Property Sheet view
//		IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.50f,	"topLeft");
//		bottomLeft.addView(IPageLayout.ID_OUTLINE);
//		bottomLeft.addView(IPageLayout.ID_PROP_SHEET);

		// Bottom right: Task List view
//		layout.addView(IPageLayout.ID_TASK_LIST, IPageLayout.BOTTOM, 0.66f, editorArea);
	}

}
