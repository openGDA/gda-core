/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.hatsaxs.ui;

import gda.rcp.DataProject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.devices.hatsaxs.HatsaxsUtils;
import uk.ac.gda.devices.hatsaxs.beans.ManualBean;
import uk.ac.gda.devices.hatsaxs.beans.ManualSessionBean;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public final class ManualSessionBeanEditor extends RichBeanMultiPageEditorPart {
	private static final Logger logger = LoggerFactory.getLogger(ManualSessionBeanEditor.class);
	private ManualSessionBean sessionBean;
	private ArrayList<ManualBean> measurements;

	public ManualSessionBeanEditor() {
		super();
		setPartProperty("RichBeanEditorPart", null);
	}

	@Override
	public Class<?> getBeanClass() {
		return ManualSessionBean.class;
	}

	@Override
	public URL getMappingUrl() {
		return ManualSessionBean.mappingURL; // Please make sure this field is present and the mapping
	}

	@Override
	public RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		return new ManualSessionBeanUIEditor(path, getMappingUrl(), this, editingBean);
	}

	@Override
	public URL getSchemaUrl() {
		return ManualSessionBean.schemaURL; // Please make sure this field is present and the schema
	}

	@Override
	public void setInput(final IEditorInput input) {
		try {
			assignInput(input);
		} catch (Exception e) {
		}
		try {
			createBean();
		} catch (Exception e) {
			try {
				editingBean = getBeanClass().getConstructor().newInstance();
				setDirty(true);
				logger.debug("Failed to read beans from file, using empty bean", e);
			} catch (Exception e2) {
				logger.error("Failed to read beans from file and could not create empty bean");
			}

		}
		try {
			linkUI();
		} catch (Exception e) {
			logger.error("Error setting input for editor from input " + input.getName(), e);
		}
	}

	/**
	 * NOTE Can save both to this project, in which case add as IFile or to any
	 * other location, in which case add as external resource.
	 */
	@Override
	public void doSaveAs() {

		final IFile currentiFile = EclipseUtils.getIFile(getEditorInput());
		final IFolder folder = (currentiFile != null) ? (IFolder) currentiFile.getParent() : null;

		final FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
		dialog.setText("Save as Experiment");
		dialog.setFilterExtensions(new String[] { "*.manual", "*.xml" });
		final File currentFile = new File(this.path);
		dialog.setFilterPath(HatsaxsUtils.getXmlDirectory());

		String newFile = dialog.open();
		if (newFile != null) {
			if (!newFile.endsWith(".manual") && (!newFile.endsWith(".xml")))
				newFile = newFile + ".manual";
			newFile = validateFileName(newFile);
			if (newFile == null)
				return;

			final File file = new File(newFile);
			if (file.exists()) {
				final boolean ovr = MessageDialog.openQuestion(getSite().getShell(), "Confirm File Overwrite",
						"The file '" + file.getName() + "' exists in '" + file.getParentFile().getName() + "'.\n\n"
								+ "Would you like to overwrite it?");
				if (!ovr)
					return;
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				MessageDialog.openError(getSite().getShell(), "Cannot save file.", "The file '" + file.getName()
						+ "' cannot be created in '" + file.getParentFile().getName() + "'.\n\n" + e.getMessage());
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

	public void openEditorWithDefaultSamples() {
		IProject dataProject = DataProject.getDataProjectIfExists();

		if (dataProject != null) {
			File nativeFile = HatsaxsUtils.getDefaultManualFile();

			if (!nativeFile.exists()) {
				sessionBean = new ManualSessionBean();
				measurements = new ArrayList<>();

				try {
					ManualBean tibi1 = new ManualBean();
					initialiseManualBean(tibi1, "Sample 1", 1, 10, 22, 0, 0, false);
					ManualBean tibi2 = new ManualBean();
					initialiseManualBean(tibi2, "Sample 2", 1, 10, 22, 0, 0, false);
					ManualBean tibi3 = new ManualBean();
					initialiseManualBean(tibi3, "Sample 3", 1, 10, 22, 0, 0, false);
					ManualBean tibi4 = new ManualBean();
					initialiseManualBean(tibi4, "Sample 4", 1, 10, 22, 0, 0, false);
					measurements.add(tibi1);
					measurements.add(tibi2);
					measurements.add(tibi3);
					measurements.add(tibi4);
				} catch (Exception e) {
					logger.error("Exception ", e);
				}

				sessionBean.setMeasurements(measurements);
				try {
					XMLHelpers.writeToXML(ManualSessionBean.mappingURL, sessionBean, nativeFile);
				} catch (Exception e) {
					logger.error("Exception writing bean to XML", e);
				}
				HatsaxsUiUtils.refreshXmlDirectory();
			}

			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(nativeFile.toURI());
			try {
				if (page != null) {
					IDE.openEditorOnFileStore(page, fileStore);
				}
			} catch (PartInitException e) {
				logger.error("PartInitException opening editor", e);
			}
		}
	}

	private void initialiseManualBean(ManualBean bean, String name, double timePerFrame, int noOfFrames,
			double exposureTemp, double delay, double illumination, boolean light) {
		bean.setSampleName(name);
		bean.setTimePerFrame(timePerFrame);
		bean.setTemperature(exposureTemp);
		bean.setDelay(delay);
		bean.setIllumination(illumination);
		bean.setLightExpose(light);
	}
}
