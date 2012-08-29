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

package uk.ac.diamond.tomography.reconstruction.views;

import java.util.Collections;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.CommonNavigator;

public class NexusNavigator extends CommonNavigator {

	public static final String ID = "uk.ac.diamond.tomography.reconstruction.NexusNavigator";

	private IPartListener partListener = new IPartListener() {

		@Override
		public void partOpened(IWorkbenchPart part) {
			NexusNavigator.this.selectReveal(new StructuredSelection(Collections.emptyList()));
		}

		@Override
		public void partDeactivated(IWorkbenchPart part) {
			// Do nothing
		}

		@Override
		public void partClosed(IWorkbenchPart part) {
			// Do nothing
		}

		@Override
		public void partBroughtToTop(IWorkbenchPart part) {
			NexusNavigator.this.selectReveal(new StructuredSelection(Collections.emptyList()));
		}

		@Override
		public void partActivated(IWorkbenchPart part) {
			NexusNavigator.this.selectReveal(new StructuredSelection(Collections.emptyList()));
		}
	};

	@Override
	public void createPartControl(org.eclipse.swt.widgets.Composite aParent) {
		super.createPartControl(aParent);

		getSite().getPage().addPartListener(partListener);
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(partListener);
		super.dispose();
	}
}
