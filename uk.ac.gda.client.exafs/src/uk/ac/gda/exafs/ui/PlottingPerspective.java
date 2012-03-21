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

package uk.ac.gda.exafs.ui;

import gda.rcp.views.JythonTerminalView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.gda.client.XYPlotView;
import uk.ac.gda.client.experimentdefinition.components.RunControllerView;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.plot.LnI0ItScanPlotView;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;

/**
 *
 */
public class PlottingPerspective implements IPerspectiveFactory {
	/**
	 * 
	 */
	public static final String ID = "org.diamond.exafs.ui.PlottingPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		IFolderLayout folderLayout_0 = layout.createFolder("folder10", IPageLayout.LEFT, 0.76f, editorArea);
		folderLayout_0.addView(XYPlotView.ID);
		if (!ExafsActivator.getDefault().getPreferenceStore()
				.getBoolean(ExafsPreferenceConstants.HIDE_LnI0ItScanPlotView)) {
			folderLayout_0.addView(LnI0ItScanPlotView.ID);
		}

//		IFolderLayout folderLayout_2 = layout.createFolder("folder1", IPageLayout.RIGHT, 0.55f, LnI0ItScanPlotView.ID);
//		folderLayout_2.addView(DerivativeScanPlotView.ID);
//		folderLayout_2.addView(SubtractedBackgroundScanPlotView.ID);
//		folderLayout_2.addView(FourierScanPlotView.ID);

		IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.BOTTOM, 0.5f, "folder1");
		folderLayout.addView(JythonTerminalView.ID);

//		if (ExafsActivator.getDefault().getPreferenceStore()
//				.getBoolean(ExafsPreferenceConstants.SHOW_B18ScalersMonitorView)) {
//			folderLayout.addView(B18ScalersMonitorView.ID);
//		} else {
//			folderLayout.addView(ScalersMonitorView.ID);
//		}

		IFolderLayout folderLayout_1 = layout.createFolder("folder0", IPageLayout.BOTTOM, 0.85f, XYPlotView.ID);
		folderLayout_1.addView(RunControllerView.ID);
	}

}
