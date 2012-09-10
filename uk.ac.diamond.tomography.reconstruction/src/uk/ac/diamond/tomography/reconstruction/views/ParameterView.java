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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
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
import uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType;
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

	private Combo cmbAml;

	private Text txtNumSeries;

	private Text txtDarkField;

	private Combo cmbFlatFieldType;

	private Text txtFlatFieldValueBefore;

	private Text txtFlatFieldValueAfter;

	private Text txtFlatFieldFileBefore;

	private Combo cmbDarkFieldType;

	private Text txtDarkFieldValueBefore;

	private Text txtDarkFieldFileBefore;

	private Text txtDarkFieldValueAfter;

	private Combo cmbRoiType;

	private Text txtRoiXMin;

	private Text txtRoiXMax;

	private Text txtRoiYMin;

	private Text txtRoiYMax;

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
		lblCenterOfRotation.setText("Rotation Centre");
		lblCenterOfRotation.setLayoutData(new GridData());

		txtCenterOfRotation = new Text(formComposite, SWT.BORDER);
		txtCenterOfRotation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Ring Artefacts
		createRingArtefacts(formComposite);

		// Flat fields
		createFlatFields(formComposite);

		// Dark Fields
		createDarkFields(formComposite);

		// ROI
		createRoi(formComposite);

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

	private void createRoi(Composite formComposite) {
		GridData gd;
		Group grpRoi = new Group(formComposite, SWT.None);
		grpRoi.setLayout(new GridLayout(4, false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		grpRoi.setLayoutData(gd);
		grpRoi.setText("Region of Interest");

		Label lblRoiType = new Label(grpRoi, SWT.None);
		lblRoiType.setText("Type");
		lblRoiType.setLayoutData(new GridData());

		cmbRoiType = new Combo(grpRoi, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData layoutData2 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData2.horizontalSpan = 3;
		cmbRoiType.setLayoutData(layoutData2);
		cmbRoiType.setItems(new String[] { "Standard", "Rectangle" });

		Label lblXmin = new Label(grpRoi, SWT.None);
		lblXmin.setText("X min");
		lblXmin.setLayoutData(new GridData());

		txtRoiXMin = new Text(grpRoi, SWT.BORDER);
		txtRoiXMin.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblXmax = new Label(grpRoi, SWT.None);
		lblXmax.setText("X max");
		lblXmax.setLayoutData(new GridData());

		txtRoiXMax = new Text(grpRoi, SWT.BORDER);
		txtRoiXMax.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblYmin = new Label(grpRoi, SWT.None);
		lblYmin.setText("Y min");
		lblYmin.setLayoutData(new GridData());

		txtRoiYMin = new Text(grpRoi, SWT.BORDER);
		txtRoiYMin.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblYmax = new Label(grpRoi, SWT.None);
		lblYmax.setText("Y max");
		lblYmax.setLayoutData(new GridData());

		txtRoiYMax = new Text(grpRoi, SWT.BORDER);
		txtRoiYMax.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createDarkFields(Composite formComposite) {
		GridData gd;
		Group grpDark = new Group(formComposite, SWT.None);
		grpDark.setLayout(new GridLayout(4, false));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		grpDark.setLayoutData(gd);
		grpDark.setText("Dark Fields");

		Label lblDarkType = new Label(grpDark, SWT.None);
		lblDarkType.setText("Type");
		lblDarkType.setLayoutData(new GridData());

		cmbDarkFieldType = new Combo(grpDark, SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbDarkFieldType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cmbDarkFieldType.setItems(new String[] { "User", "Row" });

		Label lblDarkValueBefore = new Label(grpDark, SWT.None);
		lblDarkValueBefore.setText("Value Before");
		lblDarkValueBefore.setLayoutData(new GridData());

		txtDarkFieldValueBefore = new Text(grpDark, SWT.BORDER);
		txtDarkFieldValueBefore.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblDarkFileBefore = new Label(grpDark, SWT.None);
		lblDarkFileBefore.setText("File Before");
		lblDarkFileBefore.setLayoutData(new GridData());

		txtDarkFieldFileBefore = new Text(grpDark, SWT.BORDER);
		txtDarkFieldFileBefore.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblDarkValueAfter = new Label(grpDark, SWT.None);
		lblDarkValueAfter.setText("Value After");
		lblDarkValueAfter.setLayoutData(new GridData());

		txtDarkFieldValueAfter = new Text(grpDark, SWT.BORDER);
		txtDarkFieldValueAfter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createFlatFields(Composite formComposite) {
		Group grpFlat = new Group(formComposite, SWT.None);
		grpFlat.setLayout(new GridLayout(4, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		grpFlat.setLayoutData(gd);
		grpFlat.setText("Flat Fields");

		Label lblType = new Label(grpFlat, SWT.None);
		lblType.setText("Type");
		lblType.setLayoutData(new GridData());

		cmbFlatFieldType = new Combo(grpFlat, SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbFlatFieldType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cmbFlatFieldType.setItems(new String[] { "User", "Row" });

		Label lblValueBefore = new Label(grpFlat, SWT.None);
		lblValueBefore.setText("Value Before");
		lblValueBefore.setLayoutData(new GridData());

		txtFlatFieldValueBefore = new Text(grpFlat, SWT.BORDER);
		txtFlatFieldValueBefore.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblFileBefore = new Label(grpFlat, SWT.None);
		lblFileBefore.setText("File Before");
		lblFileBefore.setLayoutData(new GridData());

		txtFlatFieldFileBefore = new Text(grpFlat, SWT.BORDER);
		txtFlatFieldFileBefore.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblValueAfter = new Label(grpFlat, SWT.None);
		lblValueAfter.setText("Value After");
		lblValueAfter.setLayoutData(new GridData());

		txtFlatFieldValueAfter = new Text(grpFlat, SWT.BORDER);
		txtFlatFieldValueAfter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createRingArtefacts(Composite formComposite) {
		Group grpRingArtefacts = new Group(formComposite, SWT.None);
		grpRingArtefacts.setLayout(new GridLayout(4, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		grpRingArtefacts.setLayoutData(gd);
		grpRingArtefacts.setText("Ring Artefacts");

		Label lblAML = new Label(grpRingArtefacts, SWT.None);
		lblAML.setText("Ring Artefacts");
		lblAML.setLayoutData(new GridData());

		cmbAml = new Combo(grpRingArtefacts, SWT.DROP_DOWN | SWT.READ_ONLY);
		cmbAml.setItems(new String[] { "No", "Column", "AML" });
		cmbAml.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblNumSeries = new Label(grpRingArtefacts, SWT.None);
		lblNumSeries.setText("Num Series");
		lblNumSeries.setLayoutData(new GridData());

		txtNumSeries = new Text(grpRingArtefacts, SWT.BORDER);
		txtNumSeries.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	protected void saveModel() {
		HMxmlType model = getModel();
		try {
			FBPType fbp = model.getFBP();
			BackprojectionType backprojection = fbp.getBackprojection();
			//
			backprojection.setImageCentre(BigDecimal.valueOf(Double.parseDouble(txtCenterOfRotation.getText())));
			RingArtefactsType ringArtefacts = fbp.getPreprocessing().getRingArtefacts();

			ringArtefacts.getType().setValue(cmbAml.getText());
			ringArtefacts.getNumSeries().setValue(BigDecimal.valueOf(Double.parseDouble(txtNumSeries.getText())));

			FlatDarkFieldsType flatDarkFields = fbp.getFlatDarkFields();

			FlatFieldType flatField = flatDarkFields.getFlatField();

			flatField.getType().setValue(cmbFlatFieldType.getText());

			flatField.setValueBefore(Double.parseDouble(txtFlatFieldValueBefore.getText()));
			flatField.setValueAfter(Double.parseDouble(txtFlatFieldValueAfter.getText()));
			flatField.setFileBefore(txtFlatFieldFileBefore.getText());

			DarkFieldType darkField = flatDarkFields.getDarkField();

			darkField.getType().setValue(cmbDarkFieldType.getText());

			darkField.setValueBefore(Double.parseDouble(txtDarkFieldValueBefore.getText()));
			darkField.setValueAfter(Double.parseDouble(txtDarkFieldValueAfter.getText()));
			darkField.setFileBefore(txtDarkFieldFileBefore.getText());

			//
			ROIType roi = backprojection.getROI();
			roi.getType().setValue(cmbRoiType.getText());
			roi.setXmin(Integer.parseInt(txtRoiXMin.getText()));
			roi.setXmax(Integer.parseInt(txtRoiXMax.getText()));
			roi.setYmin(Integer.parseInt(txtRoiYMin.getText()));
			roi.setYmax(Integer.parseInt(txtRoiYMax.getText()));

			model.eResource().save(Collections.emptyMap());
		} catch (IOException e) {
			logger.error("TODO put description of error here", e);
		}
	}

	private void initializeView() {
		HMxmlType hmxmlType = getModel();

		FBPType fbp = hmxmlType.getFBP();
		BackprojectionType backprojection = fbp.getBackprojection();

		float floatValue = backprojection.getImageCentre().floatValue();
		txtCenterOfRotation.setText(Float.toString(floatValue));

		RingArtefactsType ringArtefacts = fbp.getPreprocessing().getRingArtefacts();
		String amlValue = ringArtefacts.getType().getValue();
		cmbAml.setText(amlValue);

		float numSeriesVal = ringArtefacts.getNumSeries().getValue().floatValue();
		txtNumSeries.setText(Float.toString(numSeriesVal));

		FlatDarkFieldsType flatDarkFields = fbp.getFlatDarkFields();

		FlatFieldType flatField = flatDarkFields.getFlatField();

		cmbFlatFieldType.setText(flatField.getType().getValue());

		txtFlatFieldValueBefore.setText(Double.toString(flatField.getValueBefore()));

		txtFlatFieldValueAfter.setText(Double.toString(flatField.getValueAfter()));
		txtFlatFieldFileBefore.setText(flatField.getFileBefore());

		DarkFieldType darkField = flatDarkFields.getDarkField();

		cmbDarkFieldType.setText(darkField.getType().getValue());

		txtDarkFieldValueBefore.setText(Double.toString(darkField.getValueBefore()));

		txtDarkFieldValueAfter.setText(Double.toString(darkField.getValueAfter()));
		txtDarkFieldFileBefore.setText(darkField.getFileBefore());

		//
		ROIType roi = backprojection.getROI();
		cmbRoiType.setText(roi.getType().getValue());
		txtRoiXMin.setText(Integer.toString(roi.getXmin()));
		txtRoiXMax.setText(Integer.toString(roi.getXmax()));
		txtRoiYMin.setText(Integer.toString(roi.getYmin()));
		txtRoiYMax.setText(Integer.toString(roi.getYmax()));

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
						initializeView();
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