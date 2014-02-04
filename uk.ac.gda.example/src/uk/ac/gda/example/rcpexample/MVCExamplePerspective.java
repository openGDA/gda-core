/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.example.rcpexample;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class MVCExamplePerspective implements IPerspectiveFactory {

	/**
	 * Creates and adds a new folder with the given id to this page layout. 
	 * The position and relative size of the folder is expressed relative to a reference part. 
	 * Parameters: folderId the id for the new folder. 
	 * This must be unique within the layout to avoid collision with other parts. 
	 * relationship the position relative to the reference part;
	 * one of TOP, BOTTOM, LEFT, or RIGHT ratio a ratio specifying how to divide the space currently occupied by the
	 * reference part, in the range 0.05f to 0.95f. Values outside this range will be clipped to facilitate direct
	 * manipulation. For a vertical split, the part on top gets the specified ratio of the current space and the part on
	 * bottom gets the rest. Likewise, for a horizontal split, the part at left gets the specified ratio of the current
	 * space and the part at right gets the rest. refId the id of the reference part; either a view id, a folder id, or
	 * the special editor area id returned by getEditorArea 
	 * Returns:the new folder
	 **/

	@Override
	public void createInitialLayout(IPageLayout layout) {

		IFolderLayout folderLayout = layout
				.createFolder("folder", IPageLayout.BOTTOM, 0.7f, IPageLayout.ID_EDITOR_AREA);
		folderLayout.addView("uk.ac.gda.example.rcptrainingview");
	}
}
