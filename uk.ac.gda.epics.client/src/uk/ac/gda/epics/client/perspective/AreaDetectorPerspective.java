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

package uk.ac.gda.epics.client.perspective;

import gda.rcp.views.JythonTerminalView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.gda.pydev.extension.ui.perspective.JythonPerspective;

/**
 * Area detector perspective
 */
public abstract class AreaDetectorPerspective implements IPerspectiveFactory {

	private static final String JYTHON_TERMINAL = "jythonTerminal";
	private static final String PROJ_EXP = "projExp";
	private static final String PLOT_FOLDER = "plotFolder";
	private static final String IMAGE_VIEWER = "imageViewer";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setFixed(false);
		String editorArea = layout.getEditorArea();
		IFolderLayout imageViewerFolder = layout.createFolder(IMAGE_VIEWER, IPageLayout.TOP, 0.7f, editorArea);
		imageViewerFolder.addView(getPlotViewId());
		imageViewerFolder.addView(getCameraViewId());

		IFolderLayout plotFolder = layout.createFolder(PLOT_FOLDER, IPageLayout.RIGHT, 0.5f, IMAGE_VIEWER);
		plotFolder.addPlaceholder(getSidePlotViewId());
		plotFolder.addPlaceholder(getHistogramViewPlotId());
		plotFolder.addPlaceholder(getSubSamplePlotViewId());

		IFolderLayout sideStatusFolder = layout.createFolder("statusFolder", IPageLayout.RIGHT, 0.73f, PLOT_FOLDER);
		sideStatusFolder.addView(getStatusViewId());
		sideStatusFolder.addPlaceholder(getStatusViewId());

		IFolderLayout projExpFolder = layout.createFolder(PROJ_EXP, IPageLayout.LEFT, 0.17f, editorArea);
		projExpFolder.addView(IPageLayout.ID_PROJECT_EXPLORER);

		IFolderLayout jythonTerminalFolder = layout.createFolder(JYTHON_TERMINAL, IPageLayout.RIGHT, 0.50f, editorArea);
		jythonTerminalFolder.addView(JythonTerminalView.ID);

		layout.addPerspectiveShortcut(JythonPerspective.ID);

		layout.addShowViewShortcut(getPlotViewId());
		layout.addShowViewShortcut(getStatusViewId());
		layout.addShowViewShortcut(JythonTerminalView.ID);
		layout.addShowViewShortcut(getCameraViewId());
		layout.addShowViewShortcut(getSidePlotViewId());
		layout.addShowViewShortcut(getHistogramViewPlotId());
		layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);

	}

	protected abstract String getSubSamplePlotViewId();

	protected abstract String getStatusViewId();

	protected abstract String getCameraViewId();

	abstract protected String getPlotViewId();

	abstract protected String getSidePlotViewId();

	abstract protected String getHistogramViewPlotId();

}
