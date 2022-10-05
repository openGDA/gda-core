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

package uk.ac.gda.arpes.ui;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.arpes.beans.ARPESScanBean;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

public final class ARPESScanBeanEditor extends RichBeanMultiPageEditorPart {
	private static final Logger logger = LoggerFactory.getLogger(ARPESScanBeanEditor.class);

	public ARPESScanBeanEditor() {
		super();
		setPartProperty("RichBeanEditorPart", null);
	}

	@Override
	public Class<?> getBeanClass() {
		return ARPESScanBean.class;
	}

	@Override
	public URL getMappingUrl() {
		return ARPESScanBean.mappingURL; // Please make sure this field is present and the mapping
	}

	@Override
	public RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		return new ARPESScanBeanUIEditor(path, getMappingUrl(), this, editingBean);
	}

	@Override
	public URL getSchemaUrl() {
		return ARPESScanBean.schemaURL; // Please make sure this field is present and the schema
	}

	@Override
	public void setInput(final IEditorInput input) {
		try{
			assignInput(input);
			createBean();
			linkUI();
		} catch (Throwable th){
			logger.error("Error setting input for editor from input " + input.getName(), th);
		}
	}

	public void replaceBean(Object object) {
		this.editingBean = object;
		linkUI();
	}

	/**
	 * Override super class's doSaveAs() to remove switch hard coded .xml extension to .arpes in the filename.
	 */
	@Override
	public void doSaveAs() {

		final IFile currentiFile = EclipseUtils.getIFile(getEditorInput());
		final IFolder folder = (currentiFile != null) ? (IFolder) currentiFile.getParent() : null;

		final FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
		dialog.setText("Save as ARPES file");
		dialog.setFilterExtensions(new String[] { "*.arpes" });
		final File currentFile = new File(this.path);
		dialog.setFilterPath(currentFile.getParentFile().getAbsolutePath());
		dialog.setFileName(currentFile.getName());

		String newFile = dialog.open();
		if (newFile != null) {

			newFile = validateFileName(newFile);
			if (newFile == null)
				return;

			final File file = new File(newFile);
			if (file.exists()) {
				final boolean ovr = MessageDialog.openQuestion(getSite().getShell(), "Confirm File Overwrite",
						"The file '" + file.getName() + "' exists in '" + file.getParentFile().getName() + "'.\n\n" + "Would you like to overwrite it?");
				if (!ovr)
					return;
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				MessageDialog.openError(getSite().getShell(), "Cannot save file.",
						"The file '" + file.getName() + "' cannot be created in '" + file.getParentFile().getName() + "'.\n\n" + e.getMessage());
				return;
			}
			try {
				if (!confirmFileNameChange(currentFile, file)) {
					file.delete();
					return;
				}
			} catch (Exception ne) {
				logger.error("Cannot confirm name change", ne);
				return;
			}

			IEditorInput input;
			if (folder != null && folder.getLocation().toFile().equals(file.getParentFile())) {
				final IFile ifile = folder.getFile(file.getName());
				try {
					ifile.refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (CoreException e) {
					logger.error("Cannot refresh " + ifile, e);
				}
				input = new FileEditorInput(ifile);
			} else {
				input = new FileStoreEditorInput(EFS.getLocalFileSystem().fromLocalFile(file));
			}

			assignInput(input);
			doSave(new NullProgressMonitor());
			setDirty(false);
		}
	}
}