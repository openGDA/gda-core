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

package uk.ac.gda.devices.bssc.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class BioSAXSProgressPerspective implements IPerspectiveFactory {
	public static String ID = "uk.ac.gda.devices.bssc.biosaxsprogressperspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addView("uk.ac.gda.devices.bssc.views.CapillaryView", IPageLayout.LEFT, 0.25f,
				IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.gda.client.ncd.saxsview", IPageLayout.BOTTOM, 0.25f,
				"uk.ac.gda.devices.bssc.views.CapillaryView");
		layout.addView("org.dawb.workbench.plotting.views.toolPageView.fixed:org.dawb.workbench.plotting.tools.radialProfileTool", IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.gda.devices.bssc.biosaxsprogressview", IPageLayout.RIGHT, 0.60f,
				IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.gda.client.CommandQueueViewFactory", IPageLayout.BOTTOM, 0.75f, "uk.ac.gda.devices.bssc.biosaxsprogressview");
		layout.setEditorAreaVisible(false);
	}
}