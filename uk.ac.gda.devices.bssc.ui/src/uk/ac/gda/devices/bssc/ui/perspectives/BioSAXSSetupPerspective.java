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

package uk.ac.gda.devices.bssc.ui.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.gda.client.CommandQueueViewFactory;

public class BioSAXSSetupPerspective implements IPerspectiveFactory {
	public static String ID = "uk.ac.gda.devices.bssc.biosaxssetupperspective";
	protected String activePerspectiveID;

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.addView(CommandQueueViewFactory.ID, IPageLayout.BOTTOM, 0.6f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("uk.ac.gda.rcp.views.dashboardView", IPageLayout.RIGHT, 0.5f, CommandQueueViewFactory.ID);
		layout.addView("gda.rcp.ncd.views.NCDStatus", IPageLayout.BOTTOM, 0.5f, CommandQueueViewFactory.ID);
		layout.addView("gda.rcp.jythonterminalview", IPageLayout.BOTTOM, 0.55f, "uk.ac.gda.rcp.views.dashboardView");
	}
}
