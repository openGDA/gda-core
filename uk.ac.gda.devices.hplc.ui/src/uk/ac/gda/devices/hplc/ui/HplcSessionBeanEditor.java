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

package uk.ac.gda.devices.hplc.ui;

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
import uk.ac.gda.devices.hatsaxs.ui.HatsaxsUiUtils;
import uk.ac.gda.devices.hplc.beans.HplcBean;
import uk.ac.gda.devices.hplc.beans.HplcSessionBean;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public final class HplcSessionBeanEditor extends RichBeanMultiPageEditorPart {
	private static final Logger logger = LoggerFactory.getLogger(HplcSessionBeanEditor.class);
	private HplcSessionBean sessionBean;
	private ArrayList<HplcBean> measurements;

	public HplcSessionBeanEditor() {
		super();
		setPartProperty("RichBeanEditorPart", null);
	}

	@Override
	public Class<?> getBeanClass() {
		return HplcSessionBean.class;
	}

	@Override
	public URL getMappingUrl() {
		return HplcSessionBean.mappingURL; // Please make sure this field is present and the mapping
	}

	@Override
	public RichBeanEditorPart getRichBeanEditorPart(String path, Object editingBean) {
		return new HplcSessionBeanUIEditor(path, getMappingUrl(), this, editingBean);
	}

	@Override
	public URL getSchemaUrl() {
		return HplcSessionBean.schemaURL; // Please make sure this field is present and the schema
	}

	@Override
	public void setInput(final IEditorInput input) {
		try {
			assignInput(input);
		} catch (Exception e) {
		}
		try {
			createBean();
		} catch (Throwable th) {
			try {
				editingBean = getBeanClass().newInstance();
				setDirty(true);
				logger.debug("Failed to read beans from file, using empty bean");
			} catch (InstantiationException | IllegalAccessException e) {
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
	 * NOTE Can save both to this project, in which case add as IFile or to any other location, in which case add as
	 * external resource.
	 */
	@Override
	public void doSaveAs() {

		final IFile currentiFile = EclipseUtils.getIFile(getEditorInput());
		final IFolder folder = (currentiFile != null) ? (IFolder) currentiFile.getParent() : null;

		final FileDialog dialog = new FileDialog(getSite().getShell(), SWT.SAVE);
		dialog.setText("Save as HPLC Experiment");
		dialog.setFilterExtensions(new String[] { "*.hplc", "*.xml" });
		final File currentFile = new File(this.path);
		dialog.setFilterPath(HatsaxsUtils.getXmlDirectory());

		String newFile = dialog.open();
		if (newFile != null) {
			if (!newFile.endsWith(".hplc") && (!newFile.endsWith(".xml")))
				newFile = newFile + ".hplc";
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
			File nativeFile = HatsaxsUtils.getDefaultHplcFile();

			if (!nativeFile.exists()) {
				sessionBean = new HplcSessionBean();
				measurements = new ArrayList<HplcBean>();

				try {
					HplcBean tibi1 = new HplcBean();
					initialiseHplcBean(tibi1, "Sample A1", "low", (short) 1, 'A', (short) 3, (short) 1, 'A',
							(short) 1, 10, 560, 0.5, 120, (float) 22.0);
					HplcBean tibi2 = new HplcBean();
					initialiseHplcBean(tibi2, "Sample B1", "medium", (short) 1, 'B', (short) 3, (short) 1, 'B',
							(short) 1, 30, 78, 0.5, 120, (float) 22.0);
					HplcBean tibi3 = new HplcBean();
					initialiseHplcBean(tibi3, "Sample C1", "medium", (short) 1, 'C', (short) 3, (short) 1, 'C',
							(short) 1, 300, 340, 2.0, 30, (float) 22.0);
					HplcBean tibi4 = new HplcBean();
					initialiseHplcBean(tibi4, "Sample C2", "medium", (short) 1, 'C', (short) 3, (short) 2, 'C',
							(short) 1, 150, 340, 2.0, 30, (float) 22.0);
					measurements.add(tibi1);
					measurements.add(tibi2);
					measurements.add(tibi3);
					measurements.add(tibi4);
				} catch (Exception e) {
					logger.error("Exception ", e);
				}

				sessionBean.setMeasurements(measurements);
				try {
					XMLHelpers.writeToXML(HplcSessionBean.mappingURL, sessionBean, nativeFile);
				} catch (Exception e) {
					logger.error("Exception writing bean to XML", e);
				}
				HatsaxsUiUtils.refreshXmlDirectory();
			}

			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IFileStore hplcFileStore = EFS.getLocalFileSystem().getStore(nativeFile.toURI());
			try {
				if (page != null) {
					IDE.openEditorOnFileStore(page, hplcFileStore);
				}
			} catch (PartInitException e) {
				logger.error("PartInitException opening editor", e);
			}
		}
	}

	private void initialiseHplcBean(HplcBean titrationBean, String name, String viscosity, short bufferCol,
			char bufferRow, short bufferPlate, short col, char row, short plate, double concentration,
			double molecularWeight, double timePerFrame, int noOfFrames, float exposureTemp) throws Exception {
		titrationBean.setSampleName(name);
		String location = String.format("%c%d", col, row);
		titrationBean.setLocation(location);
		titrationBean.setConcentration(concentration);
		titrationBean.setMolecularWeight(molecularWeight);
		titrationBean.setTimePerFrame(timePerFrame);
	}
}
