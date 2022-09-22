/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.dawnsci.common.widgets.stepper.IStepperSelectionListener;
import org.dawnsci.common.widgets.stepper.Stepper;
import org.dawnsci.common.widgets.stepper.StepperChangedEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.TIFFImageLoader;
import uk.ac.diamond.tomography.reconstruction.Activator;
import uk.ac.diamond.tomography.reconstruction.ReconUtil;
import uk.ac.diamond.tomography.reconstruction.jobs.ReconSchedulingRule;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType;
import uk.ac.gda.util.io.FileUtils;

public class CenterOfRotationView extends BaseParameterView implements ISelectionListener {
	private static final String JOB_NAME_UPDATE_CENTRE_OF_ROTATION_SEARCH = "Update centre(%s): %.02f";
	private static final String FIND_TOMO_CENTRE_SCRIPT_FILE_NAME = "platform:/plugin/%s/scripts/tomo_centre.sh";
	private static final String FIND_CENTRE_JOB_NAME = "Finding Centre (%.02f) for %s";
	private static final String FIND_CENTRE_COMMAND = "%1$s -s %2$d -c %3$f -t %4$s --ctot=%5$d --cstep=%6$f -n %7$s %8$s";
	private static final String PLOTVIEW_PLOT_1 = "Plot 1";
	private static final String UPDATING_CENTRE_OF_ROTATION = "Updating Centre of Rotation";
	private static final String MSG_ACTIVATE = "Please select a nexus file in the navigator view to activate the view.";
	private static final String DEFAULT_CENTRE_OF_ROTATION = "2012";
	private static final Logger logger = LoggerFactory.getLogger(CenterOfRotationView.class);
	public static final String ID = "uk.ac.diamond.tomography.reconstruction.view.centreOfRotation";
	private static final String USE_THIS = "Use This";
	private static final String CURRENT_CENTER_OF_ROTATION = "Current Center of Rotation";
	private FormToolkit toolkit;
	private Stepper sliceStepper;
	private PageBook pgBook_showSlider;
	private Composite pg_showSlider_plainComposite;
	private Composite pg_showSlider_sliderComposite;
	private PageBook pgBook_mainComposite;
	private Composite pg_mainComposite_plain;
	private Composite pg_mainComposite_root;

	@Override
	public void setFocus() {
		sliceStepper.setFocus();
	}

	private double centreOfCentre;

	private void runCentreOfRotation(CENTRE_OF_ROTATION_MODE detectCentreMode) {

		URL shFileURL = null;
		try {
			shFileURL = new URL(String.format(FIND_TOMO_CENTRE_SCRIPT_FILE_NAME, Activator.PLUGIN_ID));
		} catch (MalformedURLException e) {
			logger.error("Unable to resolve URL", e);
		}
		logger.debug("shFileURL:{}", shFileURL);
		File centreOfRotationScriptFile = null;
		try {
			centreOfRotationScriptFile = new File(FileLocator.toFileURL(shFileURL).toURI());
		} catch (URISyntaxException e) {
			logger.error("Unable to resolve URL", e);
		} catch (IOException e) {
			logger.error("Unable to find script file.", e);
		}
		logger.debug("tomoDoShScript:{}", centreOfRotationScriptFile);
		if (centreOfRotationScriptFile != null) {
			String shScriptName = centreOfRotationScriptFile.getAbsolutePath();

			int sliceNumber = getSliceNumber() / ProjectionsView.SPLITS;

			centreOfCentre = Double.parseDouble(txtCentreOfRotation.getText());
			double stepSize = detectCentreMode.getStepSize();
			int totalSteps = detectCentreMode.getTotalSteps();
			String reducedNxsFileName = getReducedNexusFileName();
			String outDir = getOutDir();

			File file = new File(outDir);
			FileUtils.deleteContents(file);

			String centreOfRotationCommand = String.format(FIND_CENTRE_COMMAND, shScriptName, sliceNumber,
					centreOfCentre, getHmSettingsInProcessingDir(), totalSteps, stepSize, reducedNxsFileName, outDir);

			logger.debug("Centre of rotation command : {}", centreOfRotationCommand);

			runCommand(String.format(FIND_CENTRE_JOB_NAME, centreOfCentre, nexusFile.getName()),
					centreOfRotationCommand, totalSteps, stepSize);

		} else {
			logger.debug("Unable to locate script file");
		}
	}

	private String getOutDir() {
		String reducedNxsFileName = getReducedNexusFileName();
		return ReconUtil.getCentreOfRotationDirectory(reducedNxsFileName) + File.separator + getCentreOfCentreString();
	}

	private String getCentreOfCentreString() {
		return getCentreOfRotationDisplayText(centreOfCentre).replace(".", "");
	}

	private double[] getStepValues(int totalSteps, double centreOfRotStart, double stepSize) {
		double[] values = new double[totalSteps];
		int ts = totalSteps / 2;
		double start = centreOfRotStart - (ts * stepSize);
		for (int i = 0; i < totalSteps; i++) {
			values[i] = start;
			start = start + stepSize;
		}
		return values;
	}

	private String getReducedNexusFileName() {
		if (nexusFile == null) {
			IViewPart findView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.findView(NexusNavigator.ID);
			if (findView != null) {
				ISelection selection = findView.getViewSite().getSelectionProvider().getSelection();

				if (selection instanceof IStructuredSelection) {
					Object firstElement = ((IStructuredSelection) selection).getFirstElement();
					if (firstElement instanceof IFile) {
						nexusFile = (IFile) firstElement;
					}
				}
			}
		}
		if (nexusFile != null) {
			File reducedNexusFile = ReconUtil.getReducedNexusFile(nexusFile.getLocation().toOSString());
			if (reducedNexusFile != null && reducedNexusFile.exists()) {
				return reducedNexusFile.getPath();
			}
		}
		return null;
	}

	private int getSliceNumber() {
		IViewPart findView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(ProjectionsView.ID);
		if (findView != null) {
			ISelection selection = findView.getViewSite().getSelectionProvider().getSelection();
			if (selection instanceof ProjectionSliceSelection) {
				ProjectionSliceSelection projectionsSliceSelection = (ProjectionSliceSelection) selection;
				return projectionsSliceSelection.getSliceNumber();
			}
		}
		return 0;
	}

	private void runCommand(final String jobName, final String command, final int totalSteps, final double stepSize) {
		UIJob job2 = new UIJob(getViewSite().getShell().getDisplay(), "") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				pgBook_showSlider.showPage(pg_showSlider_plainComposite);
				return Status.OK_STATUS;
			}
		};
		job2.setRule(new ReconSchedulingRule(nexusFile.getLocation().toOSString()));
		job2.setSystem(true);
		job2.schedule();

		Job job = new Job(jobName) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
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
				return Status.OK_STATUS;
			}
		};
		job.setRule(new ReconSchedulingRule(nexusFile.getLocation().toOSString()));
		job.schedule();

		job2 = new UIJob(getViewSite().getShell().getDisplay(), "") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				pgBook_showSlider.showPage(pg_showSlider_sliderComposite);
				double[] values = getStepValues(totalSteps, centreOfCentre, stepSize);
				sliceStepper.setSteps(totalSteps, values);
				sliceStepper.setSelection(totalSteps / 2);
				return Status.OK_STATUS;
			}
		};
		job2.setSystem(true);
		job2.setRule(new ReconSchedulingRule(nexusFile.getLocation().toOSString()));
		job2.schedule();

		scheduleUpdatePlotterJob(centreOfCentre, totalSteps / 2);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		getViewSite().setSelectionProvider(this);
		toolkit = new FormToolkit(parent.getDisplay());
		toolkit.setBorderStyle(SWT.BORDER);

		pgBook_mainComposite = new PageBook(parent, SWT.None);

		pg_mainComposite_plain = toolkit.createComposite(pgBook_mainComposite);
		pg_mainComposite_plain.setLayout(new FillLayout());

		toolkit.createLabel(pg_mainComposite_plain, MSG_ACTIVATE);

		pg_mainComposite_root = toolkit.createComposite(pgBook_mainComposite);
		pg_mainComposite_root.setLayout(new GridLayout());

		Composite labelComposite = toolkit.createComposite(pg_mainComposite_root);
		labelComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		labelComposite.setLayout(new GridLayout(3, false));

		toolkit.createLabel(labelComposite, CURRENT_CENTER_OF_ROTATION);
		txtCentreOfRotation = toolkit.createText(labelComposite, DEFAULT_CENTRE_OF_ROTATION);
		txtCentreOfRotation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button btnUseThis = toolkit.createButton(labelComposite, USE_THIS, SWT.PUSH);
		btnUseThis.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					IViewPart parameterView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(ParameterView.ID);
					if (parameterView != null) {
						parameterView
								.getViewSite()
								.getSelectionProvider()
								.setSelection(
										new ParametersSelection(nexusFile.getLocation().toOSString(), Double
												.parseDouble(txtCentreOfRotation.getText())));
					}
				} catch (PartInitException e1) {
					logger.error("Unable to find center of rotation view", e1);
				}
			}
		});

		Composite buttonsComposite = toolkit.createComposite(pg_mainComposite_root);
		buttonsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonsComposite.setLayout(new GridLayout(3, true));

		Button btnCoarse = toolkit.createButton(buttonsComposite,
				CENTRE_OF_ROTATION_MODE.COARSE_MODE.getDisplayString(), SWT.PUSH);
		btnCoarse.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnCoarse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runCentreOfRotation(CENTRE_OF_ROTATION_MODE.COARSE_MODE);
			}
		});

		Button btnFine = toolkit.createButton(buttonsComposite, CENTRE_OF_ROTATION_MODE.FINE_MODE.getDisplayString(),
				SWT.PUSH);
		btnFine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnFine.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runCentreOfRotation(CENTRE_OF_ROTATION_MODE.FINE_MODE);
			}
		});

		Button btnVeryFine = toolkit.createButton(buttonsComposite,
				CENTRE_OF_ROTATION_MODE.VERY_FINE_MODE.getDisplayString(), SWT.PUSH);
		btnVeryFine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnVeryFine.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runCentreOfRotation(CENTRE_OF_ROTATION_MODE.VERY_FINE_MODE);
			}
		});

		pgBook_showSlider = new PageBook(pg_mainComposite_root, SWT.None);
		pgBook_showSlider.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		pg_showSlider_plainComposite = toolkit.createComposite(pgBook_showSlider);

		pg_showSlider_sliderComposite = toolkit.createComposite(pgBook_showSlider);
		pg_showSlider_sliderComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		pg_showSlider_sliderComposite.setLayout(new FillLayout());
		sliceStepper = new Stepper(pg_showSlider_sliderComposite, SWT.None);
		sliceStepper.addStepperSelectionListener(stepperSelectionListener);

		double[] arr = new double[20];
		for (int i = 0; i < 20; i++) {
			arr[i] = 0.25 + i;
		}
		sliceStepper.setSteps(20, arr);

		sliceStepper.setSelection(10);
		pgBook_showSlider.showPage(pg_showSlider_plainComposite);

		Composite fileNameComposite = toolkit.createComposite(pg_mainComposite_root);
		fileNameComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		fileNameComposite.setLayout(new GridLayout());

		txtFileName = createTextFileName(toolkit, fileNameComposite);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.verticalAlignment = SWT.BOTTOM;
		txtFileName.setLayoutData(layoutData);

		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(NexusNavigator.ID, this);
		pgBook_mainComposite.showPage(pg_mainComposite_plain);
		initialiseView();
	}

	private void initialiseView() {
		if (getHmXmlModel() != null) {
			HMxmlType hmxmlType = getHmXmlModel();

			FBPType fbp = hmxmlType.getFBP();
			BackprojectionType backprojection = fbp.getBackprojection();

			float floatValue = backprojection.getImageCentre().floatValue();
			txtCentreOfRotation.setText(getCentreOfRotationDisplayText(floatValue));

			txtFileName.setText(nexusFile.getLocation().toOSString());
		}
	}

	private String getCentreOfRotationDisplayText(double value) {
		return String.format("%.02f", value);
	}

	private class UpdatePlotViewJob extends Job {

		private int position;

		public UpdatePlotViewJob() {
			super("");
		}

		public synchronized void setStepperPosition(int position) {
			this.position = position;

		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("", IProgressMonitor.UNKNOWN);
			String outDirStr = getOutDir();
			File outDir = new File(outDirStr);
			if (outDir.exists()) {
				File parentLocation = outDir.listFiles()[0].listFiles()[0];
				String[] fileNames = parentLocation.list();
				List<String> fileNameList = Arrays.asList(fileNames);
				Collections.sort(fileNameList);
				fileNames = fileNameList.toArray(new String[0]);

				File fileName = new File(parentLocation, fileNames[position]);
				logger.debug("File trying to display:{}", fileNames[position]);
				if (fileName.exists()) {
					// update monitor
					try {
						DataHolder data = new TIFFImageLoader(fileName.getAbsolutePath()).loadFile();
						// update monitor
						Dataset image = data.getDataset(0);
						image.isubtract(image.min());
						image.imultiply(1000.0);
						// update monitor
						SDAPlotter.imagePlot(PLOTVIEW_PLOT_1, image);
						// update monitor
					} catch (Exception ex) {
						logger.error("Cannot load recon image for display", ex);
					}
				} else {
					logger.error("Unable to load imagefile :{}", fileName.getPath());
				}
			}
			monitor.done();
			return Status.OK_STATUS;
		}
	}

	private IStepperSelectionListener stepperSelectionListener = new IStepperSelectionListener() {

		@Override
		public void stepperChanged(final StepperChangedEvent e) {
			double value = sliceStepper.getIndexValues()[e.getPosition()];
			txtCentreOfRotation.setText(getCentreOfRotationDisplayText(value));
			int position = e.getPosition();
			scheduleUpdatePlotterJob(value, position);
		}
	};

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(NexusNavigator.ID, this);
		super.dispose();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (isPartActive()) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection iss = (IStructuredSelection) selection;
				Object firstElement = iss.getFirstElement();
				if (firstElement instanceof IFile) {
					IFile fileElement = (IFile) firstElement;
					if (Activator.NXS_FILE_EXTN.equals(fileElement.getFileExtension())) {
						if (!fileElement.equals(nexusFile)) {
							nexusFile = fileElement;
							processSelectionChangeToNexus();
						}
					}
				}
			}
		}

	}

	private void processSelectionChangeToNexus() {
		ILazyDataset datasetFromNexusFile = null;
		try {
			datasetFromNexusFile = getDatasetFromNexusFile();
		} catch (ScanFileHolderException e) {
			logger.error("Unable to load dataset", e);
		}

		if (datasetFromNexusFile != null) {
			super.processNewNexusFile();
			pgBook_showSlider.showPage(pg_showSlider_plainComposite);
			pgBook_mainComposite.showPage(pg_mainComposite_root);

			final float rotCentre = getHmXmlModel().getFBP().getBackprojection().getImageCentre().floatValue();
			UIJob updateCentreOfRotation = new UIJob(getViewSite().getShell().getDisplay(), UPDATING_CENTRE_OF_ROTATION) {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (nexusFile != null) {
						txtCentreOfRotation.setText(getCentreOfRotationDisplayText(rotCentre));
						txtFileName.setText(nexusFile.getLocation().toOSString());
					}
					return Status.OK_STATUS;
				}
			};
			updateCentreOfRotation.schedule();
		} else {
			pgBook_mainComposite.showPage(pg_mainComposite_plain);
		}
	}

	@Override
	protected void processNewNexusFile() {
		super.processNewNexusFile();
		pgBook_showSlider.showPage(pg_showSlider_plainComposite);
		pgBook_mainComposite.showPage(pg_mainComposite_root);

		IViewPart parameterView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(ParameterView.ID);
		if (parameterView != null) {
			ISelection selection = parameterView.getViewSite().getSelectionProvider().getSelection();
			if (selection instanceof ParametersSelection) {
				final ParametersSelection parametersSel = (ParametersSelection) selection;
				UIJob updateCentreOfRotation = new UIJob(getViewSite().getShell().getDisplay(),
						UPDATING_CENTRE_OF_ROTATION) {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (nexusFile != null
								&& parametersSel.getNexusFileFullPath().equals(nexusFile.getLocation().toOSString())) {
							double centreOfRotation = parametersSel.getCentreOfRotation();
							txtCentreOfRotation.setText(getCentreOfRotationDisplayText(centreOfRotation));
							txtFileName.setText(nexusFile.getLocation().toOSString());
						}
						return Status.OK_STATUS;
					}
				};
				updateCentreOfRotation.schedule();
			}
		}

	}

	protected void scheduleUpdatePlotterJob(double value, int position) {
		UpdatePlotViewJob plotViewJob = new UpdatePlotViewJob();
		plotViewJob.setName(String.format(JOB_NAME_UPDATE_CENTRE_OF_ROTATION_SEARCH, nexusFile.getName(), value));
		plotViewJob.setStepperPosition(position);
		plotViewJob.setRule(new ReconSchedulingRule(nexusFile.getLocation().toOSString()));
		plotViewJob.setUser(true);
		plotViewJob.schedule();
	}
}
