/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.logpanel;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.gda.client.logpanel.view.LogpanelView;

public class Perspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		// view(s) only
		layout.setEditorAreaVisible(false);

		layout.setFixed(true);
		/* achieves following but only before view added
		IViewLayout statusLayout = layout.getViewLayout(LogpanelView.ID);
		statusLayout.setCloseable(false);
		statusLayout.setMoveable(false);
		*/

		/* displays view toolbar icons in top left corner over anything https://bugs.eclipse.org/bugs/show_bug.cgi?id=98883
		layout.addStandaloneView(LogpanelView.ID, false, IPageLayout.BOTTOM, IPageLayout.NULL_RATIO, layout.getEditorArea());
		*/
		layout.addView(LogpanelView.ID, IPageLayout.BOTTOM, 0.5f, layout.getEditorArea());
		// displays view toolbar icons to the right or below of tab
	}
}
