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

package uk.ac.gda.epics.dxp.client;

import gda.rcp.views.JythonTerminalView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.gda.epics.dxp.client.viewfactories.StatusViewFactory;


public class EDXDPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setFixed(false);
		String editorArea = layout.getEditorArea();
		IFolderLayout imageViewerFolder = layout.createFolder("EDXDPlot", IPageLayout.TOP, 0.7f, editorArea);
		imageViewerFolder.addView("uk.ac.gda.epics.dxpplotview");

		IFolderLayout plot1Folder = layout.createFolder("Plot1Folder", IPageLayout.LEFT, 0.3f, "EDXDPlot");
		plot1Folder.addView("uk.ac.diamond.scisoft.analysis.rcp.plotView1");

		IFolderLayout plot2Folder = layout.createFolder("Plot2Folder", IPageLayout.BOTTOM, 0.5f, "Plot1Folder");
		plot2Folder.addView("uk.ac.diamond.scisoft.analysis.rcp.plotView2");
		
		IFolderLayout sideStatusFolder = layout.createFolder("statusFolder", IPageLayout.RIGHT, 0.8f, "EDXDPlot");
		sideStatusFolder.addView(StatusViewFactory.ID);

		IFolderLayout projExpFolder = layout.createFolder("PROJ_EXP", IPageLayout.LEFT, 0.17f, editorArea);
		projExpFolder.addView(IPageLayout.ID_PROJECT_EXPLORER);

		IFolderLayout jythonTerminalFolder = layout.createFolder("JYTHON_TERMINAL", IPageLayout.RIGHT, 0.50f, editorArea);
		jythonTerminalFolder.addView(JythonTerminalView.ID);

	}

}
