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

import gda.util.OSCommandRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.tomography.reconstruction.Activator;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view shows data obtained from the model. The
 * sample creates a dummy model on the fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be presented in the view. Each view can present the
 * same model objects using different labels and icons, if needed. Alternatively, a single label provider can be shared
 * between views in order to ensure that objects of the same type are presented in the same way everywhere.
 * <p>
 */

public class ParameterView extends ViewPart {
	private static final Logger logger = LoggerFactory.getLogger(ParameterView.class);

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "uk.ac.diamond.tomography.reconstruction.views.ParameterView";
	private Composite composite;

	private IEditorPart editorPart;

	private String pathname = "tomoSettings.xml";

	private File fileOnFileSystem;

	/**
	 * The constructor.
	 */
	public ParameterView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new org.eclipse.swt.layout.GridLayout());
		Button button = new Button(composite, SWT.PUSH);
		GridDataFactory.fillDefaults().applyTo(button);
		button.setText("Go");
		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				editorPart.doSave(null);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// do nothing
				}
				OSCommandRunner runner = new OSCommandRunner("cat " + pathname, true, null, null);
				runner.logOutput();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Read settings file from resource and copy to /tmp
		createSettingsFile();

	}

	private void createSettingsFile() {
		Bundle bundle = Platform.getBundle("uk.ac.diamond.tomography.reconstruction");
		URL fileURL = bundle.getEntry("resources/settings.xml");
		fileOnFileSystem = null;
		try {
			fileOnFileSystem = new File(FileLocator.resolve(fileURL).toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		}
		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(Activator.TOMOGRAPHY_SETTINGS);
		if (!project.exists()) {
			WorkspaceModifyOperation workspaceModifyOperation = new WorkspaceModifyOperation() {

				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
						InterruptedException {
					project.create(monitor);
					project.open(monitor);
				}
			};
			try {
				workspaceModifyOperation.run(null);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			}
		} else if (!project.isAccessible()) {

			WorkspaceModifyOperation workspaceModifyOperation = new WorkspaceModifyOperation() {

				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
						InterruptedException {
					project.open(monitor);
				}
			};
			try {
				workspaceModifyOperation.run(null);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			}

		}

		final IFile textFile = project.getFile(pathname);
		if (!textFile.exists()) {
			WorkspaceModifyOperation workspaceModifyOperation = new WorkspaceModifyOperation() {

				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
						InterruptedException {
					try {
						textFile.create(new FileInputStream(fileOnFileSystem), true, null);
					} catch (FileNotFoundException e) {
						logger.error("TODO put description of error here", e);
					}
				}
			};
			try {
				workspaceModifyOperation.run(null);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			}
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		composite.setFocus();
	}
}