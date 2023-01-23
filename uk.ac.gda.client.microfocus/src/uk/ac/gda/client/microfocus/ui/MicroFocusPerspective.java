/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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
import uk.ac.gda.client.microfocus.views.scan.MapPlotView;
import uk.ac.gda.client.microfocus.views.scan.MicroFocusElementListView;

public class MicroFocusPerspective implements IPerspectiveFactory {

	public static final String ID = "uk.ac.gda.microfocus.ui.MicroFocusPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		defineLayout(layout);
	}

	private void defineLayout(IPageLayout layout) {
		layout.getEditorArea();

		IFolderLayout elementsFolder = layout.createFolder("elements", IPageLayout.TOP, 0.6f, IPageLayout.ID_EDITOR_AREA);
		elementsFolder.addView(MicroFocusElementListView.ID);

		IFolderLayout mapplotFolder = layout.createFolder("mapplot", IPageLayout.RIGHT, 0.15f, "elements");
		mapplotFolder.addView(MapPlotView.ID);

		IFolderLayout mcafolder = layout.createFolder("mca", IPageLayout.RIGHT, 0.5f, "mapplot");
		mcafolder.addView("uk.ac.gda.beamline.i18.McaView");

		IFolderLayout leftFolder = layout.createFolder("bottomleft", IPageLayout.BOTTOM, 0.4f, IPageLayout.ID_EDITOR_AREA);
		leftFolder.addView("uk.ac.gda.client.microfocus.SelectExafsView");
		leftFolder.addView(CommandQueueViewFactory.ID);

		IFolderLayout bottomrightFolder = layout.createFolder("bottomright", IPageLayout.RIGHT, 0.3f, "bottomleft");
		bottomrightFolder.addView("gda.rcp.jythonterminalview");
		bottomrightFolder.addView("org.dawb.workbench.plotting.views.toolPageView.fixed:org.dawnsci.rcp.histogram.histogram_tool_page");

		layout.setEditorAreaVisible(false);
	}
}
