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

package uk.ac.gda.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView;
import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
import uk.ac.diamond.scisoft.analysis.rcp.views.SidePlotView;

/**
 * A copy of the perspective defined in the scisoft plugin. Copied here as we do not want to be dependent on the main
 * scisoft plugin as it has too many unwanted dependencies, but all the views used in this perspective are defined in
 * plugins lower down the hierarchy tree which are already accessible by the gda.client plugin!
 */
public class DataExplorationPerspective implements IPerspectiveFactory {

	public static final String ID = "uk.ac.gda.exafs.ui.dataexplorationperspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		String explorer = "org.eclipse.ui.navigator.ProjectExplorer";
		layout.addView(explorer, IPageLayout.LEFT, 0.15f, editorArea);
		if (layout.getViewLayout(explorer) != null) {
			layout.getViewLayout(explorer).setCloseable(false);
		}

		IFolderLayout rightPane = layout.createFolder("rightEditors", IPageLayout.RIGHT, 0.5f, editorArea);

		String plot = PlotView.ID + "DP";
		rightPane.addView(plot);
		if (layout.getViewLayout(plot) != null) {
			layout.getViewLayout(plot).setCloseable(false);
		}
		String sidePlot = SidePlotView.ID + ":Dataset Plot";
		rightPane.addView(sidePlot);
		if (layout.getViewLayout(sidePlot) != null) {
			layout.getViewLayout(sidePlot).setCloseable(false);
		}
		
		IFolderLayout viewManagers = layout.createFolder("underEditors", IPageLayout.BOTTOM, 0.6f, editorArea);

		String inspector = DatasetInspectorView.ID;
		viewManagers.addView(inspector);
		if (layout.getViewLayout(inspector) != null) {
			layout.getViewLayout(inspector).setCloseable(false);
		}



	}

}
