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
import gda.util.OSCommandRunner.LOGOPTION;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.tomography.reconstruction.Activator;
import uk.ac.diamond.tomography.reconstruction.ReconUtil;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.presentation.HmEditor;
import uk.ac.gda.util.io.FileUtils;

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

public class ParameterView extends ViewPart implements ISelectionListener {
	private static final Logger logger = LoggerFactory.getLogger(ParameterView.class);

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "uk.ac.diamond.tomography.reconstruction.views.ParameterView";
	private Composite composite;

	private String pathname = "tomoSettings.hm";

	private File fileOnFileSystem;

	private IFile defaultSettingFile;

	private IFile nexusFile;

	private File hmSettingsInProcessingDir;

	private Text txtCenterOfRotation;

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
		composite.setLayout(new org.eclipse.swt.layout.GridLayout(3, false));

		Composite formComposite = new Composite(composite, SWT.None);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 3;
		formComposite.setLayoutData(layoutData);
		formComposite.setLayout(new GridLayout(2, false));

		Label lblCenterOfRotation = new Label(formComposite, SWT.None);
		lblCenterOfRotation.setText("Centre of Rotation");
		lblCenterOfRotation.setLayoutData(new GridData());

		txtCenterOfRotation = new Text(formComposite, SWT.BORDER);
		txtCenterOfRotation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button btnPreview = new Button(composite, SWT.PUSH);
		btnPreview.setText("Preview Reconstruction Settings");
		btnPreview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e1) {

				saveModel();

				Bundle bundle = Platform.getBundle("uk.ac.diamond.tomography.reconstruction");
				URL shFileURL = bundle.getEntry("scripts/tomodo.sh");
				URL pyFileURL = bundle.getEntry("scripts/tomodo.py");
				File tomoDoPyScript = null;
				File tomoDoShScript = null;
				try {
					tomoDoPyScript = new File(FileLocator.resolve(pyFileURL).toURI());
					tomoDoShScript = new File(FileLocator.resolve(shFileURL).toURI());

					IPath fullPath = nexusFile.getLocation();
					Path fullPathCp = new Path(fullPath.toOSString());
					IPath pathWithoutLastSegment = fullPathCp.removeLastSegments(1);
					IPath outdir = pathWithoutLastSegment.append("processing");

					String fileName = fullPath.toOSString();

					String pyScriptName = tomoDoPyScript.getAbsolutePath();
					String shScriptName = tomoDoShScript.getAbsolutePath();
					String templateFileName = hmSettingsInProcessingDir.getAbsolutePath();
					String command = String
							.format("%s %s -f %s --stageInBeamPhys -2.27 --stageOutOfBeamPhys -1.17 --outdir %s --sino --recon --quick --template %s",
									shScriptName, pyScriptName, fileName, outdir, templateFileName);
					logger.debug("Command that will be run:{}", command);
					OSCommandRunner.runNoWait(command, LOGOPTION.ALWAYS, null);
				} catch (URISyntaxException e) {
					logger.error("TODO put description of error here", e);
				} catch (IOException e) {
					logger.error("TODO put description of error here", e);
				}

			}
		});

		Button btnRunFullRecon = new Button(composite, SWT.PUSH);
		btnRunFullRecon.setText("Run Full Reconstruction");

		Button btnAdvanced = new Button(composite, SWT.PUSH);
		btnAdvanced.setText("Open Advanced Settings");
		btnAdvanced.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (defaultSettingFile.exists()) {
					try {
						URI fileUri = URI.create("file:" + hmSettingsInProcessingDir.getAbsolutePath());
						IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), fileUri,
								HmEditor.ID, true);
					} catch (PartInitException e1) {
						logger.error("TODO put description of error here", e1);
					}
				}
			}
		});

		// Read settings file from resource and copy to /tmp
		createSettingsFile();
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);

	}

	protected void saveModel() {
		HMxmlType model = getModel();
		try {
			model.getFBP().getBackprojection()
					.setImageCentre(BigDecimal.valueOf(Double.parseDouble(txtCenterOfRotation.getText())));
			model.eResource().save(Collections.emptyMap());
		} catch (IOException e) {
			logger.error("TODO put description of error here", e);
		}
	}

	private void setInitialText() {
		HMxmlType hmxmlType = getModel();

		float floatValue = hmxmlType.getFBP().getBackprojection().getImageCentre().floatValue();
		txtCenterOfRotation.setText(Float.toString(floatValue));

	}

	private HMxmlType getModel() {
		if (hmSettingsInProcessingDir != null && hmSettingsInProcessingDir.exists()) {
			ResourceSet rset = new ResourceSetImpl();
			Resource hmRes = rset.getResource(
					org.eclipse.emf.common.util.URI.createFileURI(hmSettingsInProcessingDir.getAbsolutePath()), true);

			EObject eObject = hmRes.getContents().get(0);
			if (eObject != null) {

				if (eObject instanceof DocumentRoot) {
					DocumentRoot dr = (DocumentRoot) eObject;
					return dr.getHMxml();
				}
			}
		}

		return null;
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

		defaultSettingFile = project.getFile(pathname);
		if (!defaultSettingFile.exists()) {
			WorkspaceModifyOperation workspaceModifyOperation = new WorkspaceModifyOperation() {

				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
						InterruptedException {
					try {
						defaultSettingFile.create(new FileInputStream(fileOnFileSystem), true, null);
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

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) selection;
			Object firstElement = iss.getFirstElement();
			if (firstElement instanceof IFile
					&& Activator.NXS_FILE_EXTN.equals(((IFile) firstElement).getFileExtension())) {
				nexusFile = (IFile) firstElement;

				getHmSettingsInProcessingDir();

				getViewSite().getShell().getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						setInitialText();
					}
				});
			}
		}
	}

	private void getHmSettingsInProcessingDir() {
		IPath hmSettingsPath = new Path(ReconUtil.getPathToWriteTo(nexusFile).getAbsolutePath()).append(
				new Path(nexusFile.getName()).removeFileExtension().toString()).addFileExtension("hm");
		if (hmSettingsInProcessingDir == null || !hmSettingsInProcessingDir.exists()) {
			logger.debug("hm settings path:{}", hmSettingsPath);
			try {
				hmSettingsInProcessingDir = new File(hmSettingsPath.toString());
				File file = defaultSettingFile.getLocation().toFile();
				FileUtils.copy(file, hmSettingsInProcessingDir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			}
		}
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		super.dispose();
	}

}