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

package uk.ac.diamond.tomography.reconstruction.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public abstract class BaseTomoReconPart extends ViewPart {

	private boolean isPartActive;

	protected IFile nexusFile;

	synchronized void setPartActive(boolean isActive) {
		this.isPartActive = isActive;

		if (isActive) {
			IFile newNexusFile = null;
			IViewPart nexusNavigatorView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.findView(NexusNavigator.ID);
			if (nexusNavigatorView != null) {
				ISelection selection = nexusNavigatorView.getViewSite().getSelectionProvider().getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) selection;
					Object firstElement = sel.getFirstElement();
					if (firstElement instanceof IFile) {
						newNexusFile = (IFile) firstElement;
					}
				}

			}
			if (newNexusFile != null && (nexusFile == null || !(nexusFile.equals(newNexusFile)))) {
				nexusFile = newNexusFile;
				processNewNexusFile();
			}
		}
	}

	protected abstract void processNewNexusFile();

	private IPartListener2 partAdapter = new IPartListener2() {

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(BaseTomoReconPart.this)) {
				setPartActive(true);
			}
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(BaseTomoReconPart.this)) {
				setPartActive(false);
			}
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(BaseTomoReconPart.this)) {
				setPartActive(false);
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(BaseTomoReconPart.this)) {
				setPartActive(true);
			}
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {

		}

	};

	@Override
	public void createPartControl(org.eclipse.swt.widgets.Composite parent) {
		getViewSite().getWorkbenchWindow().getPartService().addPartListener(partAdapter);
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getPartService().removePartListener(partAdapter);
		super.dispose();
	}

	public boolean isPartActive() {
		return isPartActive;
	}
}