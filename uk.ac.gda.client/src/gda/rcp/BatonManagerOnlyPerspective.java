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

package gda.rcp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.views.baton.BatonView;
import uk.ac.gda.views.baton.MessageView;
import uk.ac.gda.views.baton.ReducedGUIWarningView;

public class BatonManagerOnlyPerspective implements IPerspectiveFactory {

	/**
	 *
	 */
	public static final String ID = "gda.rcp.batonmanageronlyperspective"; //$NON-NLS-1$

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.addStandaloneView(ReducedGUIWarningView.ID, false, IPageLayout.TOP, 0.05f, editorArea);

		IFolderLayout batonManager = layout.createFolder("Left", IPageLayout.BOTTOM, (float) 0.6, editorArea);
		batonManager.addView(BatonView.ID);

		IFolderLayout messages = layout.createFolder("Right", IPageLayout.RIGHT, (float) 0.4, "Left");
		messages.addView(MessageView.ID);

		PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(new IPerspectiveListener() {
			@Override
			public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
				if (perspective.getId().equals(BatonManagerOnlyPerspective.ID)
						&& changeId.equals(IWorkbenchPage.CHANGE_VIEW_SHOW)) {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().resetPerspective();
				}
			}

			@Override
			public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			}
		});

	}
}
