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

import gda.jython.IJythonContext;
import gda.jython.JythonServerFacade;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * @author tjs15132
 *
 */
@SuppressWarnings("restriction")
public class OpenLocalFileAction extends Action implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	private String filterPath;
	private IJythonContext jythonContext;

	/**
	 * Creates a new action for opening a local file.
	 */
	public OpenLocalFileAction() {
		setEnabled(true);
	}


	@Override
	public void dispose() {
		window =  null;
		filterPath =  null;
	}


	@Override
	public void init(IWorkbenchWindow window) {
		this.window =  window;
		String filterPath = this.getJythonContext().getDefaultScriptProjectFolder();
		this.filterPath = (filterPath != null) ? filterPath : System.getProperty("user.home");
	}

	@Override
	public void run(IAction action) {
		run();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}


	@Override
	public void run() {
		FileDialog dialog =  new FileDialog(window.getShell(), SWT.OPEN | SWT.MULTI);
		dialog.setText(IDEWorkbenchMessages.OpenLocalFileAction_title);
		dialog.setFilterPath(filterPath);
		dialog.open();
		String[] names =  dialog.getFileNames();

		if (names != null) {
			filterPath =  dialog.getFilterPath();

			int numberOfFilesNotFound =  0;
			StringBuffer notFound =  new StringBuffer();
			for (int i =  0; i < names.length; i++) {
				IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path(filterPath));
				fileStore =  fileStore.getChild(names[i]);
				if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
					IWorkbenchPage page =  window.getActivePage();
					try {
						IDE.openEditorOnFileStore(page, fileStore);
					} catch (PartInitException e) {
						String msg =  NLS.bind(IDEWorkbenchMessages.OpenLocalFileAction_message_errorOnOpen, fileStore.getName());
						IDEWorkbenchPlugin.log(msg,e.getStatus());
						MessageDialog.openError(window.getShell(), IDEWorkbenchMessages.OpenLocalFileAction_title, msg);
					}
				} else {
					if (++numberOfFilesNotFound > 1)
						notFound.append('\n');
					notFound.append(fileStore.getName());
				}
			}

			if (numberOfFilesNotFound > 0) {
				String msgFmt =  numberOfFilesNotFound == 1 ? IDEWorkbenchMessages.OpenLocalFileAction_message_fileNotFound : IDEWorkbenchMessages.OpenLocalFileAction_message_filesNotFound;
				String msg =  NLS.bind(msgFmt, notFound.toString());
				MessageDialog.openError(window.getShell(), IDEWorkbenchMessages.OpenLocalFileAction_title, msg);
			}
		}
	}


	String getFilterPathForTesting() {
		return filterPath;
	}

	private IJythonContext getJythonContext() {
		return (jythonContext != null) ? jythonContext : JythonServerFacade.getInstance();
	}
	
	void setJythonContextForTesting(IJythonContext context) {
		this.jythonContext = context;
	}
}
