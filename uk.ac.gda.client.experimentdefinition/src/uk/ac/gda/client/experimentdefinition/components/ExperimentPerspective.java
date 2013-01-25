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

package uk.ac.gda.client.experimentdefinition.components;

import gda.gui.scriptcontroller.logging.ScriptControllerLogView;
import gda.rcp.views.JythonTerminalView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.gda.client.CommandQueueViewFactory;

public class ExperimentPerspective implements IPerspectiveFactory {

	public final static String ID = "uk.ac.gda.client.experimentdefinition.experimentperspective";
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		// Get the editor area.
		String editorArea = layout.getEditorArea();

		// Top left: Resource Navigator view and Bookmarks view placeholder

		IFolderLayout belowEditor = layout.createFolder("folder", IPageLayout.BOTTOM, 0.70f, editorArea);

		belowEditor.addView(CommandQueueViewFactory.ID);

		IFolderLayout bottomMiddle = layout.createFolder("bottomMiddle", IPageLayout.RIGHT, 0.333f, "folder");
		bottomMiddle.addView(JythonTerminalView.ID);

		IFolderLayout bottomRight = layout.createFolder("bottomRight", IPageLayout.RIGHT, 0.5f, "bottomMiddle");
		bottomRight.addView(ScriptControllerLogView.ID);

		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.22f, editorArea);
		topLeft.addView(ExperimentExperimentView.ID);
	}
}
