/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;

public class MockWorkbenchWindow implements IWorkbenchWindow {

	@Override
	public void addPageListener(IPageListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addPerspectiveListener(IPerspectiveListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePageListener(IPageListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removePerspectiveListener(IPerspectiveListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getService(@SuppressWarnings("rawtypes") Class api) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasService(@SuppressWarnings("rawtypes") Class api) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean close() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IWorkbenchPage getActivePage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWorkbenchPage[] getPages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPartService getPartService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISelectionService getSelectionService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Shell getShell() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWorkbench getWorkbench() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isApplicationMenu(String menuId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IWorkbenchPage openPage(String perspectiveId, IAdaptable input) throws WorkbenchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IWorkbenchPage openPage(IAdaptable input) throws WorkbenchException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setActivePage(IWorkbenchPage page) {
		// TODO Auto-generated method stub

	}

	@Override
	public IExtensionTracker getExtensionTracker() {
		// TODO Auto-generated method stub
		return null;
	}

}
