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

package uk.ac.gda.arpes.perspectives;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonStatus;
import uk.ac.gda.arpes.beans.ARPESScanBean;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;

public class Startup implements IStartup {
	private static final Logger logger = LoggerFactory.getLogger(Startup.class);
	private static final String EXPERIMENT_PERSPECTIVE_ID = "uk.ac.gda.arpes.perspectives.ArpesExperimentPerspective";
	private static final String ALIGNMENT_PERSPECTIVE_ID = "uk.ac.gda.arpes.perspectives.ArpesAlignmentPerspective";
	private static final String SAMPLE_CONFIGURATION_DIR = "gda.analyser.sampleConf.dir";
	private static final String ALIGNMENT_CONFIGURATION_FILE_NAME = "gda.analyser.alignmentConf";

	ARPESScanBean bean = new ARPESScanBean();

	private IVGScientaAnalyserRMI analyser;

	public Startup() {
		analyser = Finder.getInstance().find("analyser");
		if (analyser == null) {
			throw new RuntimeException("No analyser was found!");
		}
	}

	/**
	 * This listener will zero supplies on the analyser if the perspective is switched when no scan is running
	 */
	private final IPerspectiveListener analyserListener = new IPerspectiveListener() {

		@Override
		public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
			// Note: This is not fired when the user changes perspective! It is fired when the perspective itself
			// is changed eg reset.
		}

		@Override
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
			// Check if a scan is running if not stop the analyser
			if (InterfaceProvider.getScanStatusHolder().getScanStatus() == JythonStatus.IDLE) {
				logger.info("Perspective Activated: No scan running: Stop analyser");
				try {
					// Stop the analyser and zero supplies
					analyser.zeroSupplies();
				} catch (Exception e) {
					logger.error("Failed to stop analyser on perspective switch", e);
				}
			}

			if (perspective.getId().equals(EXPERIMENT_PERSPECTIVE_ID)) {
				IEditorPart activeEditor = page.getActiveEditor();
				page.closeEditor(activeEditor, false);
				createAlignmentArpesFile();
			} else if (perspective.getId().equals(ALIGNMENT_PERSPECTIVE_ID)) {
				loadAlignmentConfiguration();
			}
		}

		private void createAlignmentArpesFile() {
			try {
				bean.setLensMode(analyser.getLensMode());
				bean.setPassEnergy(analyser.getPassEnergy());
				bean.setTimePerStep(analyser.getCollectionTime());
				bean.setSweptMode(false);
				bean.setConfigureOnly(true);
				bean.setCentreEnergy(analyser.getCentreEnergy());
				String directoryName = InterfaceProvider.getPathConstructor().createFromProperty(SAMPLE_CONFIGURATION_DIR);
				String alignmentXmlFileName = LocalProperties.get(ALIGNMENT_CONFIGURATION_FILE_NAME);
				File targetFile = new File(directoryName, alignmentXmlFileName);
				logger.debug("Alignment .arpes file target '{}'", targetFile.getAbsolutePath());
				try {
					ARPESScanBean.writeToXML(bean, targetFile.getAbsolutePath());
				} catch (IOException e) {
					logger.debug("Could not create xml file from bean", e);
					MessageBox validationDialog = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_ERROR | SWT.OK);
					validationDialog.setText("Could not set ARPES bean with values from analyser");
					validationDialog.setMessage("You are viewing a default arpes file");
					validationDialog.open();
				}
				if (targetFile.exists()) {
					// Open the example in the editor
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IFileStore fileStore = EFS.getLocalFileSystem().getStore(targetFile.toURI());
					try {
						IDE.openEditorOnFileStore(page, fileStore);
						logger.debug("Opened alignment config file '{}' in editor", targetFile);
					} catch (PartInitException e) {
						logger.error("Could not open alignment config file '{}' in editor", targetFile, e);
					}
				}
			} catch (Exception e) {
				logger.debug("Could not get analyser values to create ARPES bean ", e);
			}
		}

		private void loadAlignmentConfiguration() {
			try {
				analyser.setPassEnergy(bean.getPassEnergy());
				analyser.setCentreEnergy(bean.getCentreEnergy());
				analyser.setCollectionTime(bean.getTimePerStep());
				analyser.setLensMode(bean.getLensMode());
			} catch (Exception e) {
				logger.debug("Could not set analyser with values from ARPES bean", e);
			}

		}
	};

	@Override
	public void earlyStartup() {
		// This is to fix ARPES-253. Create a preference store and then set aspectRatio to false this will make
		// 2D plots fill the available space by default.
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.plotting");
		store.setValue("org.dawb.plotting.system.aspectRatio", false);

		// Need to run the startup in the UI thread
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();

				logger.info("Creating perspectives");
				for (String id : new String[] { EXPERIMENT_PERSPECTIVE_ID, ALIGNMENT_PERSPECTIVE_ID }) {
					try {
						workbench.showPerspective(id, workbench.getActiveWorkbenchWindow());
					} catch (WorkbenchException e) {
						logger.error("Error creating workbench", e);
					}
				}

				logger.info("Adding perspective switch listener");
				workbench.getActiveWorkbenchWindow().addPerspectiveListener(analyserListener);
			}
		});

	}
}
