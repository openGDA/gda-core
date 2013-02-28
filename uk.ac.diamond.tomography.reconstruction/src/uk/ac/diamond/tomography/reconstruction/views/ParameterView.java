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

import gda.analysis.io.ScanFileHolderException;
import gda.util.OSCommandRunner;
import gda.util.OSCommandRunner.LOGOPTION;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.diamond.tomography.reconstruction.Activator;
import uk.ac.diamond.tomography.reconstruction.ReconUtil;
import uk.ac.diamond.tomography.reconstruction.jobs.ReconSchedulingRule;
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
import uk.ac.diamond.tomography.reconstruction.parameters.hm.presentation.IParameterView;
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

public class ParameterView extends ViewPart implements ISelectionListener, IParameterView {

	private static final String JOB_NAME_FULL_RECONSTRUCTION = "Full Reconstruction (%s)";

	public static final String JOB_NAME_QUICK_RECONSTRUCTION = "Quick Reconstruction (%s)";

	public static final String JOB_NAME_CREATING_COMPRESSED_NEXUS = "Creating compressed Nexus(%s)";

	private static final String SHELL_TITLE_RECONSTRUCTION_COMPLETE = "Reconstruction complete";

	private static final String PATH_TO_IMAGE_KEY_IN_DATASET = "/entry1/tomo_entry/instrument/detector/image_key";

	private static final String DATA_PATH_IN_NEXUS = "/entry1/tomo_entry/data/data";
	private static final String FULL_RECONSTRUCTION = "Full Reconstruction";
	private static final String ADVANCED_SETTINGS = "Advanced Settings";
	private static final String FILE_NAME = "File Name";
	private static final String HM_FILE_EXTN = "hm";
	private static final String DARK_FIELDS = "Dark Fields";
	private static final String BLANK = "";
	private static final String RECTANGLE = "Rectangle";
	private static final String STANDARD = "Standard";
	private static final String Y_MAX = "Y max";
	private static final String Y_MIN = "Y min";
	private static final String X_MAX = "X max";
	private static final String X_MIN = "X min";
	private static final String REGION_OF_INTEREST = "Region of Interest";
	private static final String VALUE_AFTER = "Value After";
	private static final String FILE_BEFORE = "File Before";
	private static final String VALUE_BEFORE = "Value Before";
	private static final String TYPE = "Type";
	private static final String ROW = "Row";
	private static final String USER = "User";
	private static final String FLAT_FIELDS = "Flat Fields";
	private static final String PREVIEW_RECONSTRUCTION_SETTINGS = "Preview Recon";
	private static final String SCRIPTS_TOMODO_PY = "scripts/tomodo.py";
	private static final String SCRIPTS_TOMODO_SH = "scripts/tomodo.sh";
	private static final String NUM_SERIES = "Num Series";
	private static final String AML = "AML";
	private static final String AML_COLUMN = "Column";
	private static final String AML_NO = "No";
	private static final String RING_ARTEFACTS = "Ring Artefacts";
	private static final String ROTATION_CENTRE = "Rotation Centre";

	private static final Logger logger = LoggerFactory.getLogger(ParameterView.class);
	private List<String> jobNames = Collections.synchronizedList(new ArrayList<String>());

	private FormToolkit toolkit;
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "uk.ac.diamond.tomography.reconstruction.views.ParameterView";

	private String pathname = "tomoSettings.hm";

	private File fileOnFileSystem;

	private IFile defaultSettingFile;

	private IFile nexusFile;

	private File hmSettingsInProcessingDir;

	private Text txtCenterOfRotation;

	private CCombo cmbAml;

	private Text txtNumSeries;

	private CCombo cmbFlatFieldType;

	private Text txtFlatFieldValueBefore;

	private Text txtFlatFieldValueAfter;

	private Text txtFlatFieldFileBefore;

	private CCombo cmbDarkFieldType;

	private Text txtDarkFieldValueBefore;

	private Text txtDarkFieldFileBefore;

	private Text txtDarkFieldValueAfter;

	private CCombo cmbRoiType;

	private Text txtRoiXMin;

	private Text txtRoiXMax;

	private Text txtRoiYMin;

	private Text txtRoiYMax;
	private Text txtFileName;

	private boolean isPartActive;

	synchronized void setPartActive(boolean isActive) {
		this.isPartActive = isActive;
	}

	private IPartListener2 partAdapter = new IPartListener2() {

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(ParameterView.this)) {
				setPartActive(true);
			}
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(ParameterView.this)) {
				setPartActive(false);
			}
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {

		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(ParameterView.this)) {
				setPartActive(false);
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false).equals(ParameterView.this)) {
				setPartActive(true);
			}
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {

		}

	};

	private IJobChangeListener jobListener = new JobChangeAdapter() {

		@Override
		public void running(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
			String jobName = event.getJob().getName();
			if (jobName != null && nexusFile != null) {
				String jobNameToSearchFor = String.format(JOB_NAME_CREATING_COMPRESSED_NEXUS, nexusFile.getName());
				if (jobName.equals(jobNameToSearchFor)) {
					jobNames.add(jobNameToSearchFor);
				}
			}

		}

		@Override
		public void done(org.eclipse.core.runtime.jobs.IJobChangeEvent event) {
			String jobName = event.getJob().getName();
			if (jobName != null && nexusFile != null) {
				String jobNameToSearchFor = String.format(JOB_NAME_CREATING_COMPRESSED_NEXUS, nexusFile.getName());
				if (jobName.equals(jobNameToSearchFor)) {
					jobNames.remove(jobNameToSearchFor);
				}
			}
		}
	};

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {

		toolkit = new FormToolkit(parent.getDisplay());
		toolkit.setBorderStyle(SWT.BORDER);

		pgBook = new PageBook(parent, SWT.None);

		emptyCmp = toolkit.createComposite(pgBook);
		emptyCmp.setLayout(new FillLayout());

		toolkit.createLabel(emptyCmp, "Parameters cannot be displayed for the current selection");

		pgBook.showPage(emptyCmp);

		mainForm = toolkit.createForm(pgBook);

		Composite formComposite = mainForm.getBody();
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		formComposite.setLayout(layout);

		Composite subFormContainer = toolkit.createComposite(formComposite);
		subFormContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		subFormContainer.setLayout(new FillLayout());

		scrolledForm = toolkit.createScrolledForm(subFormContainer);
		Composite scrolledCmp = scrolledForm.getBody();
		scrolledCmp.setLayout(new FillLayout());
		scrolledCmp.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite topComposite = toolkit.createComposite(scrolledCmp);

		topComposite.setLayout(new GridLayout());
		Composite rotationCenterCmp = toolkit.createComposite(topComposite);
		rotationCenterCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rotationCenterCmp.setLayout(new GridLayout(2, false));

		Label lblFileName = toolkit.createLabel(rotationCenterCmp, FILE_NAME);
		lblFileName.setLayoutData(new GridData());

		txtFileName = toolkit.createText(rotationCenterCmp, BLANK);
		txtFileName.setEditable(false);
		txtFileName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		//
		Label lblCenterOfRotation = toolkit.createLabel(rotationCenterCmp, ROTATION_CENTRE);
		lblCenterOfRotation.setLayoutData(new GridData());

		txtCenterOfRotation = toolkit.createText(rotationCenterCmp, BLANK);
		txtCenterOfRotation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Ring Artefacts
		Composite ringArtefacts = createRingArtefacts(topComposite);
		ringArtefacts.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Flat fields
		Composite flatFields = createFlatFields(topComposite);
		flatFields.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// Dark Fields
		Composite darkFields = createDarkFields(topComposite);
		darkFields.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// ROI
		Composite roiComp = createRoi(topComposite);
		roiComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// scrolledCmp.setContent(topComposite);

		Composite cmpButtons = toolkit.createComposite(formComposite);
		GridData layoutData2 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData2.horizontalSpan = 2;
		layoutData2.verticalAlignment = SWT.END;
		cmpButtons.setLayoutData(layoutData2);
		GridLayout layout2 = new GridLayout(3, false);
		cmpButtons.setLayout(layout2);

		Button btnPreview = new Button(cmpButtons, SWT.PUSH);
		btnPreview.setText(PREVIEW_RECONSTRUCTION_SETTINGS);
		btnPreview.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnPreview.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e1) {
				runReconScript(true);
			}
		});

		Button btnRunFullRecon = new Button(cmpButtons, SWT.PUSH);
		btnRunFullRecon.setText(FULL_RECONSTRUCTION);
		btnRunFullRecon.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRunFullRecon.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e1) {
				runReconScript(false);
			}
		});
		Button btnAdvanced = new Button(cmpButtons, SWT.PUSH);
		btnAdvanced.setText(ADVANCED_SETTINGS);
		btnAdvanced.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnAdvanced.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openAdvancedSettings();
			}

		});

		// Read settings file from resource and copy to /tmp
		createSettingsFile();
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		getViewSite().getWorkbenchWindow().getPartService().addPartListener(partAdapter);
		Job.getJobManager().addJobChangeListener(jobListener);
	}

	public static class SampleInOutBeamPosition extends Dialog {

		private Text txtInBeamPos;
		private Text txtOutBeamPos;
		private Double inBeamPosition;
		private Double outOfBeamPosition;

		protected SampleInOutBeamPosition(Shell parentShell) {
			super(parentShell);
		}

		@Override
		public void create() {
			super.create();
			getShell().setText("In/Out Beam positions");
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite cmp = new Composite(parent, SWT.None);
			cmp.setLayout(new GridLayout(2, false));

			Label lblErrStatus = new Label(cmp, SWT.None);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			lblErrStatus.setLayoutData(gd);

			Label lblInBeamPos = new Label(cmp, SWT.None);
			lblInBeamPos.setText("In Beam Position");
			lblInBeamPos.setLayoutData(new GridData());

			txtInBeamPos = new Text(cmp, SWT.BORDER);
			txtInBeamPos.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Label lblOutBeamPos = new Label(cmp, SWT.None);
			lblOutBeamPos.setText("Out of Beam Position");
			lblOutBeamPos.setLayoutData(new GridData());

			txtOutBeamPos = new Text(cmp, SWT.BORDER);
			txtOutBeamPos.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			return cmp;
		}

		@Override
		protected void okPressed() {
			if (isValid()) {
				inBeamPosition = Double.parseDouble(txtInBeamPos.getText());
				outOfBeamPosition = Double.parseDouble(txtOutBeamPos.getText());
				super.okPressed();
			}
		}

		private boolean isValid() {
			return true;
		}

		public double getInBeamPosition() {
			if (inBeamPosition != null) {
				return inBeamPosition;
			}
			return Double.NaN;
		}

		public double getOutOfBeamPosition() {
			if (outOfBeamPosition != null) {
				return outOfBeamPosition;
			}
			return Double.NaN;
		}
	}

	private void runReconScript(boolean quick) {
		saveModel();
		if (isHdf) {
			runHdfReconstruction(quick);
		} else {
			runTiffReconstruction(quick);
		}
	}

	private void runHdfReconstruction(boolean quick) {
		File tomoDoShScript = null;
		try {
			URL shFileURL = new URL("platform:/plugin/" + Activator.PLUGIN_ID + "/" + "scripts/hdfrecon.sh");
			logger.debug("shFileURL:{}", shFileURL);
			tomoDoShScript = new File(FileLocator.toFileURL(shFileURL).toURI());
			logger.debug("tomoDoShScript:{}", tomoDoShScript);

			IPath fullPath = nexusFile.getLocation();
			if (quick) {
				fullPath = new Path(reducedNexusFile.getPath());
			}

			String nexusFileLocation = nexusFile.getLocation().toString();
			if (quick) {
				nexusFileLocation = reducedNexusFile.getPath();
			}
			File path = new File(nexusFileLocation);
			String imagePath = path.getName().replace(".nxs", "");
			File pathToImages = null;
			if (quick) {
				pathToImages = new File(ReconUtil.getReconstructedReducedDataDirectoryPath(nexusFileLocation));
			} else {
				imagePath = String.format("%s/%s", ReconUtil.getReconOutDir(nexusFileLocation).toString(), imagePath);
				pathToImages = new File(imagePath);
			}

			if (pathToImages.exists()) {
				final File[] files = pathToImages.listFiles();
				for (File f : files) {
					try {
						f.delete();
					} catch (Exception e) {
						logger.warn("Cannot delete file");
					}
				}
			} else {
				try {
					pathToImages.mkdirs();
				} catch (Exception ex) {
					logger.warn("Cannot create writing directory");
				}
			}

			String fileName = fullPath.toOSString();

			String shScriptName = tomoDoShScript.getAbsolutePath();
			String templateFileName = hmSettingsInProcessingDir.getAbsolutePath();

			int height = hdfShape[1];
			if (quick && reducedDataShape != null) {
				height = reducedDataShape[1];
			}

			StringBuffer command = new StringBuffer(String.format("%s -m 2 -e %d -n -t %s", shScriptName, height,
					templateFileName));
			command.append(" " + fileName);
			command.append(" " + pathToImages.toString());

			logger.debug("Command that will be run:{}", command);
			// OSCommandRunner.runNoWait(command.toString(), LOGOPTION.ALWAYS, null);
			String jobNameToDisplay = null;

			if (quick) {
				jobNameToDisplay = String.format(JOB_NAME_QUICK_RECONSTRUCTION, nexusFile.getName());
				// if (!(jobNames.contains(String.format(JOB_NAME_CREATING_COMPRESSED_NEXUS, nexusFile.getName())))) {
				// runCommand(jobNameToDisplay, command.toString());
				// } else {
				// MessageDialog
				// .openWarning(
				// getViewSite().getShell(),
				// "Preparing for reconstuction",
				// "The system is preparing the Nexus file for reconstrution.\n\nPlease try to run the quick reconstruction after the preparation has completed. ");
				// }
			} else {
				jobNameToDisplay = String.format(JOB_NAME_FULL_RECONSTRUCTION, nexusFile.getName());
				// runCommand(jobNameToDisplay, command.toString());
			}
			runCommand(jobNameToDisplay, command.toString());

		} catch (URISyntaxException e) {
			logger.error("Incorrect URI for script", e);
		} catch (IOException e) {
			logger.error("Cannot find script file", e);
		}
	}

	private void runCommand(final String jobName, final String command) {
		final Shell shell = getViewSite().getShell();
		final Display display = shell.getDisplay();
		Job job = new Job(jobName) {
			@Override
			public IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(jobName, IProgressMonitor.UNKNOWN);
				OSCommandRunner osCommandRunner = new OSCommandRunner(command, true, null, null);
				if (osCommandRunner.exception != null) {
					String msg = "Exception seen trying to run command " + osCommandRunner.getCommandAsString();
					logger.error(msg);
					logger.error(osCommandRunner.exception.toString());
				} else if (osCommandRunner.exitValue != 0) {
					String msg = "Exit code = " + Integer.toString(osCommandRunner.exitValue)
							+ " returned from command " + osCommandRunner.getCommandAsString();
					logger.warn(msg);
					osCommandRunner.logOutput();
				} else {
					osCommandRunner.logOutput();
				}
				monitor.done();
				if (!display.isDisposed()) {
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog.openInformation(shell, SHELL_TITLE_RECONSTRUCTION_COMPLETE,
									String.format("%s is now complete", jobName));
						}
					});
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(new ReconSchedulingRule(nexusFile));
//		job.setUser(true);
		job.schedule();
	}

	private void runTiffReconstruction(boolean quick) {
		boolean isImageKeyAvailable = isImageKeyAvailable();
		SampleInOutBeamPosition sampleInOutBeamPositionDialog = null;

		if (!isImageKeyAvailable) {
			// pop up a dialog and get values from it (in beam and out of beam physical positions
			sampleInOutBeamPositionDialog = new SampleInOutBeamPosition(getSite().getShell());
			int ret = sampleInOutBeamPositionDialog.open();

			logger.debug("Image key is not available");
			if (Window.CANCEL == ret) {
				return;
			}
		}

		File tomoDoPyScript = null;
		File tomoDoShScript = null;
		try {
			URL shFileURL = new URL("platform:/plugin/" + Activator.PLUGIN_ID + "/" + SCRIPTS_TOMODO_SH);
			logger.debug("shFileURL:{}", shFileURL);
			URL pyFileURL = new URL("platform:/plugin/" + Activator.PLUGIN_ID + "/" + SCRIPTS_TOMODO_PY);
			logger.debug("pyFileURL:{}", pyFileURL);
			tomoDoPyScript = new File(FileLocator.toFileURL(pyFileURL).toURI());
			logger.debug("tomoDoPyScript:{}", tomoDoPyScript);
			tomoDoShScript = new File(FileLocator.toFileURL(shFileURL).toURI());
			logger.debug("tomoDoShScript:{}", tomoDoShScript);

			IPath fullPath = nexusFile.getLocation();
			IPath outdir = ReconUtil.getProcessingDir(nexusFile);

			String fileName = fullPath.toOSString();

			String pyScriptName = tomoDoPyScript.getAbsolutePath();
			String shScriptName = tomoDoShScript.getAbsolutePath();
			String templateFileName = hmSettingsInProcessingDir.getAbsolutePath();

			StringBuffer command = new StringBuffer(String.format(
					"%s %s -f %s --outdir %s --sino --recon --template %s", shScriptName, pyScriptName, fileName,
					outdir, templateFileName));
			if (quick) {
				command.append(" --quick");
			}
			// String localTomoUtilFileLocation = LocalTomoUtil.getLocalTomoUtilFileLocation();
			// if (localTomoUtilFileLocation != null) {
			// command.append(" --local " + localTomoUtilFileLocation);
			// }

			double inBeamVal = 0;
			double outOfBeamVal = 0;

			if (!isImageKeyAvailable && sampleInOutBeamPositionDialog != null) {
				inBeamVal = sampleInOutBeamPositionDialog.getInBeamPosition();
				outOfBeamVal = sampleInOutBeamPositionDialog.getOutOfBeamPosition();
			}
			command.append(String.format(" --stageInBeamPhys %s", inBeamVal));
			command.append(String.format(" --stageOutOfBeamPhys %s", outOfBeamVal));

			logger.debug("Command that will be run:{}", command);

			OSCommandRunner.runNoWait(command.toString(), LOGOPTION.ALWAYS, null);
		} catch (URISyntaxException e) {
			logger.error("Unable to find the URI of the scripts file", e);
		} catch (IOException e) {
			logger.error("Problem finding scripts for recon", e);
		}
	}

	private boolean isImageKeyAvailable() {
		String path = nexusFile.getLocation().toOSString();
		HDF5Loader hdf5Loader = new HDF5Loader(path);
		DataHolder loadFile;
		try {
			loadFile = hdf5Loader.loadFile();
			ILazyDataset dataset = loadFile.getLazyDataset(PATH_TO_IMAGE_KEY_IN_DATASET);
			return dataset != null;
		} catch (ScanFileHolderException e1) {
			logger.error("Image key not available", e1);
		} catch (IllegalArgumentException e2) {
			logger.error("Image key not available", e2);
		}
		return false;
	}

	private void openAdvancedSettings() {
		if (defaultSettingFile.exists()) {
			try {

				IProject tomoSettingsProject = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(Activator.TOMOGRAPHY_SETTINGS);
				final IFile tomoSettingsFile = tomoSettingsProject.getFile(hmSettingsInProcessingDir.getName());
				if (!tomoSettingsFile.exists()) {
					new WorkspaceModifyOperation() {

						@Override
						protected void execute(IProgressMonitor monitor) throws CoreException,
								InvocationTargetException, InterruptedException {
							try {
								tomoSettingsFile.createLink(new Path(hmSettingsInProcessingDir.getAbsolutePath()),
										IResource.REPLACE, monitor);
							} catch (IllegalArgumentException ex) {
								logger.debug("Problem identified - eclipse doesn't refresh the right folder");
							}
						}
					}.run(null);
				}
				IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), tomoSettingsFile,
						HmEditor.ID);
			} catch (Exception ex) {
				logger.error("Cannot open HM editor - ", ex);
			}
		}
	}

	private Composite createRoi(Composite formComposite) {
		ExpandableComposite roiExpCmp = toolkit.createExpandableComposite(formComposite, ExpandableComposite.TWISTIE);
		roiExpCmp.setText(REGION_OF_INTEREST);
		roiExpCmp.addExpansionListener(expansionAdapter);
		roiExpCmp.setLayout(new FillLayout());

		Composite cmpRoi = toolkit.createComposite(roiExpCmp);
		cmpRoi.setLayout(new GridLayout(4, false));

		Label lblRoiType = toolkit.createLabel(cmpRoi, TYPE);
		lblRoiType.setLayoutData(new GridData());

		cmbRoiType = new CCombo(cmpRoi, SWT.FLAT | SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		GridData layoutData2 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData2.horizontalSpan = 3;
		cmbRoiType.setLayoutData(layoutData2);
		cmbRoiType.setItems(new String[] { STANDARD, RECTANGLE });

		Label lblXmin = toolkit.createLabel(cmpRoi, X_MIN);
		lblXmin.setLayoutData(new GridData());

		txtRoiXMin = toolkit.createText(cmpRoi, BLANK);
		txtRoiXMin.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblXmax = toolkit.createLabel(cmpRoi, X_MAX);
		lblXmax.setLayoutData(new GridData());

		txtRoiXMax = toolkit.createText(cmpRoi, BLANK);
		txtRoiXMax.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblYmin = toolkit.createLabel(cmpRoi, Y_MIN);
		lblYmin.setLayoutData(new GridData());

		txtRoiYMin = toolkit.createText(cmpRoi, BLANK);
		txtRoiYMin.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblYmax = toolkit.createLabel(cmpRoi, Y_MAX);
		lblYmax.setLayoutData(new GridData());

		txtRoiYMax = toolkit.createText(cmpRoi, BLANK);
		txtRoiYMax.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		roiExpCmp.setClient(cmpRoi);
		return roiExpCmp;
	}

	private Composite createDarkFields(Composite formComposite) {
		ExpandableComposite darkFieldsExpCmp = toolkit.createExpandableComposite(formComposite,
				ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
		darkFieldsExpCmp.setText(DARK_FIELDS);
		darkFieldsExpCmp.addExpansionListener(expansionAdapter);
		darkFieldsExpCmp.setLayout(new FillLayout());

		Composite cmpDarkFields = toolkit.createComposite(darkFieldsExpCmp);
		cmpDarkFields.setLayout(new GridLayout(4, false));

		Label lblDarkType = toolkit.createLabel(cmpDarkFields, TYPE);
		lblDarkType.setLayoutData(new GridData());

		cmbDarkFieldType = new CCombo(cmpDarkFields, SWT.FLAT | SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		cmbDarkFieldType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cmbDarkFieldType.setItems(new String[] { USER, ROW });

		Label lblDarkValueBefore = toolkit.createLabel(cmpDarkFields, VALUE_BEFORE);
		lblDarkValueBefore.setLayoutData(new GridData());

		txtDarkFieldValueBefore = toolkit.createText(cmpDarkFields, BLANK);
		txtDarkFieldValueBefore.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblDarkFileBefore = toolkit.createLabel(cmpDarkFields, FILE_BEFORE);
		lblDarkFileBefore.setLayoutData(new GridData());

		txtDarkFieldFileBefore = toolkit.createText(cmpDarkFields, BLANK);
		txtDarkFieldFileBefore.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblDarkValueAfter = toolkit.createLabel(cmpDarkFields, VALUE_AFTER);
		lblDarkValueAfter.setLayoutData(new GridData());

		txtDarkFieldValueAfter = toolkit.createText(cmpDarkFields, BLANK);
		txtDarkFieldValueAfter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		darkFieldsExpCmp.setClient(cmpDarkFields);
		return darkFieldsExpCmp;
	}

	private Composite createFlatFields(Composite formComposite) {
		ExpandableComposite flatFieldsExpCmp = toolkit.createExpandableComposite(formComposite,
				ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
		flatFieldsExpCmp.setText(FLAT_FIELDS);
		flatFieldsExpCmp.addExpansionListener(expansionAdapter);
		flatFieldsExpCmp.setLayout(new FillLayout());

		Composite cmpFlatFields = toolkit.createComposite(flatFieldsExpCmp);
		cmpFlatFields.setLayout(new GridLayout(4, false));

		Label lblType = toolkit.createLabel(cmpFlatFields, TYPE);
		lblType.setLayoutData(new GridData());

		cmbFlatFieldType = new CCombo(cmpFlatFields, SWT.FLAT | SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		cmbFlatFieldType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cmbFlatFieldType.setItems(new String[] { USER, ROW });

		Label lblValueBefore = toolkit.createLabel(cmpFlatFields, VALUE_BEFORE);
		lblValueBefore.setLayoutData(new GridData());

		txtFlatFieldValueBefore = toolkit.createText(cmpFlatFields, BLANK);
		txtFlatFieldValueBefore.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblFileBefore = toolkit.createLabel(cmpFlatFields, FILE_BEFORE);
		lblFileBefore.setLayoutData(new GridData());

		txtFlatFieldFileBefore = toolkit.createText(cmpFlatFields, BLANK);
		txtFlatFieldFileBefore.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblValueAfter = toolkit.createLabel(cmpFlatFields, VALUE_AFTER);
		lblValueAfter.setLayoutData(new GridData());

		txtFlatFieldValueAfter = toolkit.createText(cmpFlatFields, BLANK);
		txtFlatFieldValueAfter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		flatFieldsExpCmp.setClient(cmpFlatFields);
		return flatFieldsExpCmp;
	}

	private Composite createRingArtefacts(Composite formComposite) {
		ExpandableComposite ringArtefactsExpCmp = toolkit.createExpandableComposite(formComposite,
				ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
		ringArtefactsExpCmp.setText(RING_ARTEFACTS);
		ringArtefactsExpCmp.addExpansionListener(expansionAdapter);
		ringArtefactsExpCmp.setLayout(new FillLayout());

		Composite artefactsCmp = toolkit.createComposite(ringArtefactsExpCmp);
		artefactsCmp.setLayout(new GridLayout(4, false));

		Label lblAML = toolkit.createLabel(artefactsCmp, RING_ARTEFACTS);
		lblAML.setLayoutData(new GridData());

		cmbAml = new CCombo(artefactsCmp, SWT.FLAT | SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		cmbAml.setItems(new String[] { AML_NO, AML_COLUMN, AML });
		cmbAml.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblNumSeries = toolkit.createLabel(artefactsCmp, NUM_SERIES);
		lblNumSeries.setLayoutData(new GridData());

		txtNumSeries = toolkit.createText(artefactsCmp, BLANK);
		txtNumSeries.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		ringArtefactsExpCmp.setClient(artefactsCmp);
		return ringArtefactsExpCmp;
	}

	private ExpansionAdapter expansionAdapter = new ExpansionAdapter() {
		@Override
		public void expansionStateChanged(ExpansionEvent e) {
			scrolledForm.reflow(true);
		}
	};
	private ScrolledForm scrolledForm;
	private Form mainForm;
	private PageBook pgBook;
	private Composite emptyCmp;
	private boolean isHdf = false;
	private int[] hdfShape;
	private File reducedNexusFile;
	private int[] reducedDataShape;

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
			logger.error("Problem saving model ", e);
		}
	}

	private void initializeView() {
		pgBook.showPage(mainForm);
		HMxmlType hmxmlType = getModel();

		FBPType fbp = hmxmlType.getFBP();
		BackprojectionType backprojection = fbp.getBackprojection();

		txtFileName.setText(nexusFile.getLocation().toOSString());

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
		String blueprintFileLoc = null;

		try {
			String urlSpec = String.format("platform:/plugin/%s/resources/settings.xml", Activator.PLUGIN_ID);
			blueprintFileLoc = new URL(urlSpec).toString();
			logger.debug("shFileURL:{}", blueprintFileLoc);
		} catch (MalformedURLException e) {
			logger.error("URL is malformed.", e);
		}

		fileOnFileSystem = null;
		try {
			URL fileURL = new URL(blueprintFileLoc);
			fileOnFileSystem = new File(FileLocator.resolve(fileURL).toURI());
		} catch (URISyntaxException e) {
			logger.error("URI problem", e);
		} catch (IOException e) {
			logger.error("File not found problem", e);
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
				logger.error("Cannot create project for tomo.", e);
			} catch (InterruptedException e) {
				logger.error("Interrupted creating tomo project.", e);
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
				logger.error("Cannot open tomo project", e);
			} catch (InterruptedException e) {
				logger.error("Interrupted opening tomo project", e);
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
						logger.error("Unable to create default Setting File.", e);
					}
				}
			};
			try {
				workspaceModifyOperation.run(new NullProgressMonitor());

			} catch (InvocationTargetException e) {
				logger.error("TODO put description of error here", e);
			} catch (InterruptedException e) {
				logger.error("TODO put description of error here", e);
			}
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		scrolledForm.setFocus();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (isPartActive) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection iss = (IStructuredSelection) selection;
				Object firstElement = iss.getFirstElement();
				// nexusFile = null;
				boolean newSelection = false;
				if (firstElement instanceof IFile
						&& Activator.NXS_FILE_EXTN.equals(((IFile) firstElement).getFileExtension())) {
					nexusFile = (IFile) firstElement;
					newSelection = true;

				} else if (part instanceof IEditorPart) {
					IEditorPart ed = (IEditorPart) part;
					Object adapter = ed.getAdapter(IParameterView.class);
					if (adapter != null) {
						if (adapter instanceof IFile) {
							IFile hmFile = (IFile) adapter;
							if (HM_FILE_EXTN.equals(hmFile.getFileExtension())) {
								nexusFile = getNexusFileFromHmFileLocation(hmFile.getLocationURI().toString());
								newSelection = true;
							}
						}
					}
				}

				if (nexusFile != null && newSelection) {
					hmSettingsInProcessingDir = getHmSettingsInProcessingDir();
					String path = nexusFile.getLocation().toOSString();
					ILazyDataset actualDataset = getDataSetFromFileLocation(path);
					if (actualDataset != null && actualDataset.getRank() == 3) {
						isHdf = true;
						hdfShape = actualDataset.getShape();
						reducedNexusFile = getReducedNexusFile(nexusFile);
						if (reducedNexusFile.exists()) {
							ILazyDataset reducedDataset = getDataSetFromFileLocation(reducedNexusFile.getPath());
							if (reducedDataset != null) {
								reducedDataShape = reducedDataset.getShape();
							} else {
								logger.warn("Reduced data set not ready yet");
							}
						}
					}
					getViewSite().getShell().getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							initializeView();
						}
					});
					return;
				}
			}
		}

	}

	private ILazyDataset getDataSetFromFileLocation(String path) {
		HDF5Loader hdf5Loader = new HDF5Loader(path);
		DataHolder loadFile;
		ILazyDataset dataset = null;
		try {
			loadFile = hdf5Loader.loadFile();
			dataset = loadFile.getLazyDataset(DATA_PATH_IN_NEXUS);

		} catch (Exception ex) {
			logger.error("Error", ex);
		}
		return dataset;
	}

	private File getReducedNexusFile(IFile nexusFile) {
		File reducedNexusFile = ReconUtil.getReducedNexusFile(nexusFile.getLocation().toString());
		if (!reducedNexusFile.exists()) {
			createReducedNexusFile(nexusFile.getLocation().toOSString(), reducedNexusFile.getPath());
		}
		return reducedNexusFile;
	}

	private void createReducedNexusFile(String actualNexusFileLocation, String outputNexusFileLocation) {
		URL compressNxsURL = null;
		try {
			compressNxsURL = new URL("platform:/plugin/" + Activator.PLUGIN_ID + "/" + "scripts/compress_nxs.sh");
		} catch (MalformedURLException e) {
			logger.error("Cant find compress_nxs script", e);
		}
		logger.debug("shFileURL:{}", compressNxsURL);
		File compressNexusScript = null;
		try {
			compressNexusScript = new File(FileLocator.toFileURL(compressNxsURL).toURI());
		} catch (URISyntaxException e) {
			logger.error("Wrong location", e);
		} catch (IOException e) {
			logger.error("Unable to find file compressScript", e);
		}

		File reducedNxsFileHandle = new File(outputNexusFileLocation);

		String reducedNxsFileParentLocation = reducedNxsFileHandle.getParent();
		File reducedNxsFileParentFileHandle = new File(reducedNxsFileParentLocation);

		reducedNxsFileParentFileHandle.mkdirs();

		if (compressNexusScript != null) {
			String compressNxsScriptFullLocation = compressNexusScript.getAbsolutePath();
			String command = String.format("%s %s %s", compressNxsScriptFullLocation, actualNexusFileLocation,
					outputNexusFileLocation);
			runCommand(String.format(JOB_NAME_CREATING_COMPRESSED_NEXUS, nexusFile.getName()), command);
		}
	}

	private IFile getNexusFileFromHmFileLocation(String hmFileLocation) {
		return ReconUtil.getNexusFileFromHmFileLocation(hmFileLocation);
	}

	private File getHmSettingsInProcessingDir() {
		IPath hmSettingsPath = new Path(ReconUtil.getSettingsFileLocation(nexusFile).getAbsolutePath()).append(
				new Path(nexusFile.getName()).removeFileExtension().toString()).addFileExtension(HM_FILE_EXTN);

		File hmSettingsFile = new File(hmSettingsPath.toOSString());
		if (!hmSettingsFile.exists()) {
			logger.debug("hm settings path:{}", hmSettingsPath);
			try {
				hmSettingsFile = new File(hmSettingsPath.toString());
				File file = defaultSettingFile.getLocation().toFile();
				FileUtils.copy(file, hmSettingsFile);
			} catch (IOException e) {
				logger.error("Unable to create hm setting file.", e);
			}
		}

		return hmSettingsFile;
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
		getViewSite().getWorkbenchWindow().getPartService().removePartListener(partAdapter);
		Job.getJobManager().removeJobChangeListener(jobListener);
		super.dispose();
	}

}