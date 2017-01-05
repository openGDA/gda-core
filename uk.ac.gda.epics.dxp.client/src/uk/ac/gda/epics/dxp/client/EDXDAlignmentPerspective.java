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

package uk.ac.gda.epics.dxp.client;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import uk.ac.gda.epics.dxp.client.viewfactories.StatusViewFactory;
import uk.ac.gda.epics.dxp.client.views.FrontEndCameraView;
import uk.ac.gda.epics.dxp.client.views.EDXDAlignmentPlotView;
import uk.ac.gda.epics.dxp.client.views.EDXDChecklistView;
import uk.ac.gda.epics.dxp.client.views.EDXDDetectorSetupView;

public class EDXDAlignmentPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setFixed(false);

		IFolderLayout plotImageViewFolder = layout.createFolder("plotImageFolder", IPageLayout.RIGHT, (float) 0.2,
				layout.getEditorArea());
		plotImageViewFolder.addView(FrontEndCameraView.ID);
		plotImageViewFolder.addView(EDXDAlignmentPlotView.ID);

		layout.addView(EDXDDetectorSetupView.ID, IPageLayout.LEFT, (float) 0.2, layout.getEditorArea());

		layout.addView(EDXDChecklistView.ID, IPageLayout.BOTTOM, (float) 0.25, EDXDDetectorSetupView.ID);

		layout.addView(StatusViewFactory.ID, IPageLayout.BOTTOM, (float) 0.7, EDXDChecklistView.ID);

		layout.addShowViewShortcut(EDXDDetectorSetupView.ID);
		layout.addShowViewShortcut(EDXDChecklistView.ID);
		layout.addShowViewShortcut(FrontEndCameraView.ID);
		layout.addShowViewShortcut(StatusViewFactory.ID);
		layout.addShowViewShortcut(EDXDAlignmentPlotView.ID);

		layout.setEditorAreaVisible(false);
	}

}
