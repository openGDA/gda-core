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

package gda.rcp.ncd.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 *
 */
public class NcdDetectorPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
//		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		layout.addView("gda.rcp.ncd.views.NCDStatus", IPageLayout.TOP, 1f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.gda.client.ncd.NcdTfgConfigure", IPageLayout.RIGHT, 0.3f, "gda.rcp.ncd.views.NCDStatus");
		layout.addView("gda.rcp.jythonterminalview", IPageLayout.BOTTOM, 0.5f, "uk.ac.gda.client.ncd.NcdTfgConfigure");
		layout.addView("gda.rcp.ncd.views.NcdDetectorView", IPageLayout.BOTTOM, 0.87f, "gda.rcp.ncd.views.NCDStatus");
		layout.addView("uk.ac.gda.client.ncd.calibconf", IPageLayout.RIGHT, 0.5f, "gda.rcp.ncd.views.NcdDetectorView");
	}
}