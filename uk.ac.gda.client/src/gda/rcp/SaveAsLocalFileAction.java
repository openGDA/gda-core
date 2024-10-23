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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.python.pydev.core.IPyEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.JythonServerFacade;


@SuppressWarnings("restriction")
public class SaveAsLocalFileAction extends Action implements IWorkbenchWindowActionDelegate {

	private static final Logger logger = LoggerFactory.getLogger(SaveAsLocalFileAction.class);
	private IWorkbenchWindow window;
	private String filterPath;

	/**
	 * Creates a new action for opening a local file.
	 */
	public SaveAsLocalFileAction() {
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
		String filterpath = JythonServerFacade.getInstance().getDefaultScriptProjectFolder();
		this.filterPath = (filterpath != null) ? filterpath : System.getProperty("user.home");
	}

	@Override
	public void run(IAction action) {
		run();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		//no-op
	}

	@Override
	public void run() {
		FileDialog dialog =  new FileDialog(window.getShell(), SWT.SAVE);
		dialog.setText(IDEWorkbenchMessages.SaveAsDialog_title);
		dialog.setFilterPath(filterPath);
		dialog.setFilterNames(new String[] { "Python Scripts", "Text File", "All Files (*.*)" });
		dialog.setFilterExtensions(new String[] { "*.py", "*.txt", "*.*" });
		dialog.setFileName("");
		String filename = dialog.open();
		if (StringUtils.isBlank(filename)) {
			MessageDialog.openError(window.getShell(), IDEWorkbenchMessages.SaveAsDialog_title, "Please specify file name to save to");
			return;
		}
		File destination = new File(filename);
		if (destination.exists()) {
			boolean answer = MessageDialog.openQuestion(window.getShell(), "File already exists", filename + " is already existed. Do you want to overwrite it ?");
			if (!answer) return;
		}
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart activeEditor = activePage.getActiveEditor();
		IDocument editorText;
		// get the content of the active editor - including unsaved changes
		switch (activeEditor) {
			case IPyEdit pyeditor -> editorText = pyeditor.getDocument();
			case ITextEditor texteditor -> editorText = texteditor.getDocumentProvider().getDocument(texteditor.getEditorInput());
			default -> throw new IllegalArgumentException("File selected to save as must be a python or txt file.");
		}
		// save the modified active editor contents to a new file
		try {
			FileUtils.writeStringToFile(destination, editorText.get(), Charset.defaultCharset());
		} catch (IOException e) {
			logger.error("Exception throws while doing file SaveAs", e);
		}
		// open the new file
		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(new Path(filterPath));
		fileStore =  fileStore.getChild(destination.getName());
		if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
			try {
				IDE.openEditorOnFileStore(activePage, fileStore);
				// close active editor without saving changes
				activePage.closeEditor(activeEditor, false);
			} catch (PartInitException e) {
				String msg =  NLS.bind(IDEWorkbenchMessages.OpenLocalFileAction_message_errorOnOpen, fileStore.getName());
				IDEWorkbenchPlugin.log(msg,e.getStatus());
				MessageDialog.openError(window.getShell(), IDEWorkbenchMessages.OpenLocalFileAction_title, msg);
			}
		} else {
			StringBuilder notFound =  new StringBuilder();
			notFound.append('\n');
			notFound.append(fileStore.getName());
			String msg =  NLS.bind(IDEWorkbenchMessages.OpenLocalFileAction_message_filesNotFound, notFound.toString());
			MessageDialog.openError(window.getShell(), IDEWorkbenchMessages.OpenLocalFileAction_title, msg);
		}
	}
}
