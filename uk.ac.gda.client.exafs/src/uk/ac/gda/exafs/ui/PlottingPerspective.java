/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import gda.configuration.properties.LocalProperties;
import gda.gui.scriptcontroller.logging.ScriptControllerLogView;
import gda.rcp.views.JythonTerminalView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.client.liveplot.LivePlotView;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.plot.DerivativeScanPlotView;
import uk.ac.gda.exafs.ui.plot.FourierScanPlotView;
import uk.ac.gda.exafs.ui.plot.LnI0ItScanPlotView;
import uk.ac.gda.exafs.ui.plot.SubtractedBackgroundScanPlotView;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.exafs.ui.views.scalersmonitor.B18ScalersMonitorView;
import uk.ac.gda.exafs.ui.views.scalersmonitor.ScalersMonitorView;
import uk.ac.gda.views.baton.BatonView;

public class PlottingPerspective implements IPerspectiveFactory {

	public static final String ID = "org.diamond.exafs.ui.PlottingPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		// yuck, but will do for now.
		String nameFrag = LocalProperties.get("gda.instrument");
		if (nameFrag.equals("i20")) {
			IFolderLayout flTop = layout.createFolder("flTop", IPageLayout.LEFT, 1.0f, editorArea);
			flTop.addView(LivePlotView.ID);
			
			IFolderLayout flBottomLeft = layout.createFolder("flBottomLeft", IPageLayout.BOTTOM, 0.7f, "flTop");
			flBottomLeft.addView(CommandQueueViewFactory.ID);
			flBottomLeft.addView(ScriptControllerLogView.ID);

			IFolderLayout flBottom = layout.createFolder("flBottom", IPageLayout.RIGHT, 0.33f, "flBottomLeft");
			flBottom.addView(JythonTerminalView.ID);
			
			IFolderLayout flBottomRight = layout.createFolder("flBottomRight", IPageLayout.RIGHT, 0.5f, "flBottom");
			flBottomRight.addView(ScalersMonitorView.ID);
			flBottomRight.addView(BatonView.ID);
			
			IFolderLayout flTopRight = layout.createFolder("flTopRight", IPageLayout.RIGHT, 0.5f, "flTop");
			flTopRight.addView(LnI0ItScanPlotView.ID);
			flTopRight.addView(SubtractedBackgroundScanPlotView.ID);
			flTopRight.addView(FourierScanPlotView.ID);
			flTopRight.addView(DerivativeScanPlotView.ID);
//			layout.addView(LnI0ItScanPlotView.ID, IPageLayout.RIGHT, 0.5f, "flTop");
//			layout.addView(SubtractedBackgroundScanPlotView.ID, IPageLayout.BOTTOM, 0.5f, LnI0ItScanPlotView.ID);
//			layout.addView(FourierScanPlotView.ID, IPageLayout.RIGHT, 0.5f, SubtractedBackgroundScanPlotView.ID);
//			layout.addView(DerivativeScanPlotView.ID, IPageLayout.RIGHT, 0.5f, LnI0ItScanPlotView.ID);

			return;
		}
		
		IFolderLayout folderLayout_0 = layout.createFolder("folder10", IPageLayout.LEFT, 0.7f, editorArea);
		folderLayout_0.addView(LivePlotView.ID);
		if (!ExafsActivator.getDefault().getPreferenceStore()
				.getBoolean(ExafsPreferenceConstants.HIDE_LnI0ItScanPlotView)) {
			folderLayout_0.addView(LnI0ItScanPlotView.ID);
		}

		IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.BOTTOM, 0.5f, "folder1");
		folderLayout.addView(JythonTerminalView.ID);

		if (ExafsActivator.getDefault().getPreferenceStore()
				.getBoolean(ExafsPreferenceConstants.SHOW_B18ScalersMonitorView)) {
			folderLayout.addView(B18ScalersMonitorView.ID);
		} else {
			folderLayout.addView(ScalersMonitorView.ID);
		}

		IFolderLayout folderLayout_1 = layout.createFolder("folder0", IPageLayout.BOTTOM, 0.7f, LivePlotView.ID);

		folderLayout_1.addView(CommandQueueViewFactory.ID);
		
		IFolderLayout folderLayout_2 = layout.createFolder("folder2", IPageLayout.RIGHT, 0.5f, "folder0");
		folderLayout_2.addView(ScriptControllerLogView.ID);
	}

}
