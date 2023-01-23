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

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import gda.configuration.properties.LocalProperties;
import gda.gui.scriptcontroller.logging.ScriptControllerLogView;
import gda.rcp.views.JythonTerminalView;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.client.liveplot.LivePlotView;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.plot.DerivativeScanPlotView;
import uk.ac.gda.exafs.ui.plot.FourierScanPlotView;
import uk.ac.gda.exafs.ui.plot.LnI0ItScanPlotView;
import uk.ac.gda.exafs.ui.plot.SubtractedBackgroundScanPlotView;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;

public class PlottingPerspective implements IPerspectiveFactory {

	public static final String ID = "org.diamond.exafs.ui.PlottingPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		String nameFrag = LocalProperties.get("gda.instrument");

		if (nameFrag.equals("i20")) {
			IFolderLayout flTop = layout.createFolder("flTop", IPageLayout.LEFT, 1.0f, editorArea);
			flTop.addView(LivePlotView.ID);
			IFolderLayout flBottomLeft = layout.createFolder("flBottomLeft", IPageLayout.BOTTOM, 0.7f, "flTop");
			flBottomLeft.addView(CommandQueueViewFactory.ID);
			IFolderLayout flBottom = layout.createFolder("flBottom", IPageLayout.RIGHT, 0.333f, "flBottomLeft");
			flBottom.addView(JythonTerminalView.ID);
			IFolderLayout flBottomRight = layout.createFolder("flBottomRight", IPageLayout.RIGHT, 0.5f, "flBottom");
			flBottomRight.addView(ScriptControllerLogView.ID);
			IFolderLayout flTopRight = layout.createFolder("flTopRight", IPageLayout.RIGHT, 0.5f, "flTop");
			flTopRight.addView(LnI0ItScanPlotView.ID);
			flTopRight.addView(SubtractedBackgroundScanPlotView.ID);
			flTopRight.addView(FourierScanPlotView.ID);
			flTopRight.addView(DerivativeScanPlotView.ID);
			return;
		}

		IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.70f, editorArea);
		bottomLeft.addView(CommandQueueViewFactory.ID);
		bottomLeft.addView(ScriptControllerLogView.ID);

		IFolderLayout bottomRight = layout.createFolder("bottomRight", IPageLayout.RIGHT, 0.5f, "bottomLeft");
		bottomRight.addView(JythonTerminalView.ID);

		IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.7f, editorArea);
		topLeft.addView(LivePlotView.ID);
		if (!ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.HIDE_LnI0ItScanPlotView))
			topLeft.addView(LnI0ItScanPlotView.ID);

		IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, 0.7f, "topLeft");
		topRight.addView("uk.ac.gda.exafs.ui.views.detectors.ionchamberRates");
		topRight.addView("uk.ac.gda.client.simplescanview");

	}

}
