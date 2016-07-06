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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.EventTracker;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.PageBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
import uk.ac.diamond.scisoft.analysis.io.TIFFImageLoader;
import uk.ac.diamond.tomography.reconstruction.Activator;
import uk.ac.diamond.tomography.reconstruction.ReconUtil;
import uk.ac.diamond.tomography.reconstruction.ServiceLoader;
import uk.ac.diamond.tomography.reconstruction.dialogs.DefineHeightRoiDialog;
import uk.ac.diamond.tomography.reconstruction.dialogs.DefineRoiDialog;
import uk.ac.diamond.tomography.reconstruction.jobs.ReconSchedulingRule;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.BackprojectionType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.DarkFieldType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FBPType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatDarkFieldsType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.FlatFieldType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.HmPackage;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.ROIType;
import uk.ac.diamond.tomography.reconstruction.parameters.hm.RingArtefactsType;
import uk.ac.diamond.tomography.reconstruction.properties.PropertyDescriptor;
import uk.ac.diamond.tomography.reconstruction.properties.TextPropertyDescriptor;
import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconResults;
import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsFactory;
import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconresultsPackage;
import uk.ac.diamond.tomography.reconstruction.results.reconresults.ReconstructionDetail;
import uk.ac.diamond.tomography.reconstruction.results.reconresults.util.ReconresultsResourceImpl;

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

public class ParameterView extends BaseParameterView implements ISelectionListener, ISelectionProvider {

	private static final String LBL_INCORRECTION_STRENGTH = "	Incorrection Strength";

	private static final String ID_INCORRECTION_STRENGTH = "IncorrectionStrength";

	private static final String LBL_DARK_FIELD_TYPE = "Dark field type";

	private static final String LBL_VALUE_AFTER = "	Value After";

	private static final String LBL_VALUE_BEFORE = "	Value Before";

	private static final String LBL_FLAT_FIELD_TYPE = "Flat field type";

	private static final String LBL_HIGH_ASPECT_RATIO_COMPENSATION = "	High Aspect Ratio Compensation";

	private static final String LBL_RING_ARTEFACTS = "Ring Artefacts";

	private static final String LBL_Y_MAX = "	Y Max";

	private static final String LBL_Y_MIN = "	Y Min";

	private static final String LBL_X_MAX = "	X Max";

	private static final String LBL_X_MIN = "	X Min";

	private static final String LBL_ROI = "Region of Interest (ROI)";

	private static final String ID_DARK_FIELD_VALUE_AFTER = "DarkFieldValueAfter";

	private static final String ID_DARK_FIELD_VALUE_BEFORE = "DarkFieldValueBefore";

	private static final String ID_DARK_FIELD_TYPE = "DarkFieldType";

	private static final String ID_FLAT_FIELD_VALUE_AFTER = "FlatFieldValueAfter";

	private static final String ID_FLAT_FIELD_VALUE_BEFORE = "FlatFieldValueBefore";

	private static final String ID_FLAT_FIELD_TYPE = "FlatFieldType";

	private static final String ID_HIGH_ASPECT_RATIO_COMPENSATION = "HighAspectRatioCompensation";

	private static final String ID_RING_ARTEFACTS = "RingArtefacts";

	private static final String ID_ROI_Y_MAX = "RoiYMax";

	private static final String ID_ROI_Y_MIN = "RoiYMin";

	private static final String ID_ROI_X_MAX = "RoiXMax";

	private static final String ID_ROI_X_MIN = "RoiXMin";

	private static final String ID_ROI_TYPE = "RoiType";

	private static final String TOMO_QUICK_RECON_COMMAND = "%1$s -m 4 -p -e %2$d -n -t %3$s %4$s %5$s";

	private static final String TOMO_FULL_RECON_COMMAND = "%1$s -m 20 -b %2$d -e %3$d -t %4$s %5$s %6$s";

	private static final String ROI_ERROR_MESSAGE = "Unable to set ROI. Please run a 'Preview Recon' and try again";

	private static final String DEFINE_ROI = "Define ROI";

	private static final String NEXUS_EXTN = ".nxs";

	private static final String HDF_RECON_SCRIPT_LOCATION = "platform:/plugin/%s/scripts/hdfrecon.sh";

	private static final String FIND_CENTRE = "Find Centre";

	private static final String JOB_NAME_FULL_RECONSTRUCTION = "Full Reconstruction (%s)";

	public static final String JOB_NAME_QUICK_RECONSTRUCTION = "Quick Reconstruction (%s)";

	private static final String PATH_TO_IMAGE_KEY_IN_DATASET = "/entry1/tomo_entry/instrument/detector/image_key";

	private static final String FULL_RECONSTRUCTION = "Full Reconstruction";
	private static final String ADVANCED_SETTINGS = "Advanced Settings";
	private static final String FILE_NAME = "File Name";

	private static final String RECTANGLE = "Rectangle";
	private static final String STANDARD = "Standard";
	private static final String PREVIEW_RECONSTRUCTION_SETTINGS = "Preview Recon";
	private static final String SCRIPTS_TOMODO_PY = "scripts/tomodo.py";
	private static final String SCRIPTS_TOMODO_SH = "scripts/tomodo.sh";
	private static final String ROTATION_CENTRE = "Rotation Centre";

	private static final Logger logger = LoggerFactory.getLogger(ParameterView.class);

	private FormToolkit toolkit;
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "uk.ac.diamond.tomography.reconstruction.views.ParameterView";

	private LinkedHashMap<String, PropertyDescriptor> propertyDescriptors;

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		getViewSite().setSelectionProvider(this);
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
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		formComposite.setLayout(layout);

		Composite subFormContainer = toolkit.createComposite(formComposite);
		subFormContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
		subFormContainer.setLayout(new FillLayout());

		scrolledForm = toolkit.createScrolledForm(subFormContainer);
		Composite scrolledCmp = scrolledForm.getBody();
		scrolledCmp.setLayout(new FillLayout());
		scrolledCmp.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite topComposite = toolkit.createComposite(scrolledCmp);

		GridLayout gl = new GridLayout();
		gl.marginHeight = 0;
		gl.marginWidth = 0;

		topComposite.setLayout(gl);
		Composite rotationCenterCmp = toolkit.createComposite(topComposite);
		rotationCenterCmp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		rotationCenterCmp.setLayout(new GridLayout(3, false));

		Label lblFileName = toolkit.createLabel(rotationCenterCmp, FILE_NAME);
		lblFileName.setLayoutData(new GridData());

		txtFileName = createTextFileName(toolkit, rotationCenterCmp);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		txtFileName.setLayoutData(layoutData);

		//
		Label lblCenterOfRotation = toolkit.createLabel(rotationCenterCmp, ROTATION_CENTRE);
		lblCenterOfRotation.setLayoutData(new GridData());

		txtCentreOfRotation = toolkit.createText(rotationCenterCmp, BLANK);
		txtCentreOfRotation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button btnFindCentre = toolkit.createButton(rotationCenterCmp, FIND_CENTRE, SWT.None);
		btnFindCentre.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {

					IViewPart centreOfRotationView = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().showView(CenterOfRotationView.ID);
					if (centreOfRotationView != null) {
						centreOfRotationView
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
		btnFindCentre.setLayoutData(new GridData());

		Composite cmpButtons = toolkit.createComposite(topComposite);
		GridData layoutData2 = new GridData(GridData.FILL_HORIZONTAL);
		cmpButtons.setLayoutData(layoutData2);
		GridLayout gridLayout = new GridLayout(3, false);
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		GridLayout layout2 = gridLayout;
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
		Composite cmpRoi = toolkit.createComposite(topComposite);
		cmpRoi.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gl2 = new GridLayout(2, true);
		gl2.marginHeight = 0;
		gl2.marginWidth = 0;

		cmpRoi.setLayout(gl2);

		Button defineRoi = toolkit.createButton(cmpRoi, DEFINE_ROI, SWT.None);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		defineRoi.setLayoutData(gd);

		defineRoi.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final Dataset[] images = new Dataset[1];
				BusyIndicator.showWhile(getViewSite().getShell().getDisplay(), new Runnable() {

					@Override
					public void run() {

						String pathToImages = ReconUtil.getReconstructedReducedDataDirectoryPath(nexusFile
								.getLocation().toOSString());
						int sliceNumber = getSliceSelectionFromProjectionsView();

						if (sliceNumber >= 0) {
							File imageFile = new File(pathToImages, String.format(
									ReconUtil.RECONSTRUCTED_IMAGE_FILE_FORMAT, sliceNumber / ProjectionsView.SPLITS));
							logger.debug("Looking for image file {}", imageFile.getPath());
							if (imageFile.exists()) {
								DataHolder data = null;
								try {
									data = new TIFFImageLoader(imageFile.getAbsolutePath()).loadFile();
								} catch (ScanFileHolderException e1) {
									logger.error("Problem loading data", e1);
								}

								// update monitor
								if (data != null) {
									Dataset image = data.getDataset(0);
									image.isubtract(image.min());
									image.imultiply(1000.0);
									images[0] = image;
								}
							}
						}

					}
				});

				if (images[0] != null) {

					DefineRoiDialog defineRoiDialog = new DefineRoiDialog(getViewSite().getShell(), images[0]);
					defineRoiDialog.open();
					int[] roi = defineRoiDialog.getRoi();
					logger.debug("Roi values:{}", roi);
					if (roi != null) {
						String roiState = getPropertyDescriptor(ID_ROI_TYPE).getValue();
						if (STANDARD.equals(roiState)) {
							getPropertyDescriptor(ID_ROI_TYPE).setValue(RECTANGLE);
							getPropertyDescriptor(ID_ROI_X_MIN).setValue(roi[0]);
							getPropertyDescriptor(ID_ROI_Y_MIN).setValue(roi[1]);
							getPropertyDescriptor(ID_ROI_X_MAX).setValue(roi[2]);
							getPropertyDescriptor(ID_ROI_Y_MAX).setValue(roi[3]);
						} else {
							getPropertyDescriptor(ID_ROI_X_MIN).setValue(
									roi[0] + Integer.parseInt(getPropertyDescriptor(ID_ROI_X_MIN).getValue()));
							getPropertyDescriptor(ID_ROI_Y_MIN).setValue(
									roi[1] + Integer.parseInt(getPropertyDescriptor(ID_ROI_Y_MIN).getValue()));
							getPropertyDescriptor(ID_ROI_X_MAX).setValue(
									roi[2] + Integer.parseInt(getPropertyDescriptor(ID_ROI_X_MAX).getValue()));
							getPropertyDescriptor(ID_ROI_Y_MAX).setValue(
									roi[3] + Integer.parseInt(getPropertyDescriptor(ID_ROI_Y_MAX).getValue()));
						}
						propertyTableViewer.refresh();
						runReconScript(true);
					}
				} else {
					getViewSite().getActionBars().getStatusLineManager().setErrorMessage(ROI_ERROR_MESSAGE);

					getViewSite().getShell().getDisplay().timerExec(5000, new Runnable() {

						@Override
						public void run() {
							getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
						}
					});
				}

			}
		});

		Button resetRoi = toolkit.createButton(cmpRoi, "Reset ROI", SWT.None);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		resetRoi.setLayoutData(gd);

		resetRoi.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getPropertyDescriptor(ID_ROI_TYPE).setValue(STANDARD);
				propertyTableViewer.refresh();
				runReconScript(true);
			}
		});

		propertyTableViewer = new TableViewer(topComposite);
		propertyTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		// propertyTableViewer.getTable().setHeaderVisible(true);
		propertyTableViewer.getTable().setLinesVisible(true);

		createColumns(propertyTableViewer);

		propertyTableViewer.setContentProvider(new IStructuredContentProvider() {

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

			}

			@Override
			public void dispose() {

			}

			@SuppressWarnings("rawtypes")
			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof HashMap) {
					return ((HashMap) inputElement).values().toArray();
				}
				return null;
			}
		});

		// Read settings file from resource and copy to /tmp
		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(NexusNavigator.ID, this);
	}

	private void createColumns(TableViewer tableViewer) {
		TableViewerColumn lblColumn = new TableViewerColumn(tableViewer, SWT.None);
		lblColumn.getColumn().setWidth(320);
		lblColumn.getColumn().setText("Property");
		lblColumn.setLabelProvider(new CellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				cell.setText(((PropertyDescriptor) cell.getElement()).getLabel());
			}
		});

		TableViewerColumn valueColumn = new TableViewerColumn(tableViewer, SWT.None);
		valueColumn.getColumn().setWidth(200);
		valueColumn.getColumn().setText("Value");
		valueColumn.setLabelProvider(new CellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				cell.setText(((PropertyDescriptor) cell.getElement()).getValue());
			}
		});
		EditingSupport valueColumnEditingSupport = new EditingSupport(tableViewer) {

			@Override
			protected void setValue(Object element, Object value) {
				((PropertyDescriptor) element).setValue(value);
				propertyTableViewer.refresh();
			}

			@Override
			protected Object getValue(Object element) {
				return ((PropertyDescriptor) element).getSelectedValue();
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return ((PropertyDescriptor) element).createCellEditor();
			}

			@Override
			protected boolean canEdit(Object element) {
				return ((PropertyDescriptor) element).canEdit();
			}
		};
		valueColumn.setEditingSupport(valueColumnEditingSupport);

	}

	public static class SampleInOutBeamPosition extends Dialog {

		private static final String OUT_OF_BEAM_POSITION = "Out of Beam Position";
		private static final String IN_BEAM_POSITION = "In Beam Position";
		private static final String IN_OUT_BEAM_POSITIONS = "In/Out Beam positions";
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
			getShell().setText(IN_OUT_BEAM_POSITIONS);
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
			lblInBeamPos.setText(IN_BEAM_POSITION);
			lblInBeamPos.setLayoutData(new GridData());

			txtInBeamPos = new Text(cmp, SWT.BORDER);
			txtInBeamPos.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Label lblOutBeamPos = new Label(cmp, SWT.None);
			lblOutBeamPos.setText(OUT_OF_BEAM_POSITION);
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
			try {
				runHdfReconstruction(quick);
			} catch (Exception ex) {
				logger.error("Unable to run reconstruction script", ex);
			}
		} else {
			runTiffReconstruction(quick);
		}
	}

	private void runHdfReconstruction(boolean quick) {
		File tomoDoShScript = null;

		int[] startEnd = null;
		try {
			if (!quick) {
				DefineHeightRoiDialog defineHeightRoiDialog = new DefineHeightRoiDialog(getViewSite().getShell(),
						getImageDataFromProjectionsView());
				int returnCode = defineHeightRoiDialog.open();
				if (returnCode == Window.CANCEL) {
					return;
				}
				startEnd = defineHeightRoiDialog.getStartEnd();
			}
			// track event
			try {
				EventTracker tracker = ServiceLoader.getEventTracker();
				if (tracker != null)
					if (!quick) {
						tracker.trackActionEvent("Tomo_Full_reconstruction");
					} else {
						tracker.trackActionEvent("Tomo_Preview_reconstruction");
					}
			} catch (Exception e1) {
				logger.debug("Could not track event");
			}

			URL shFileURL = new URL(String.format(HDF_RECON_SCRIPT_LOCATION, Activator.PLUGIN_ID));
			logger.debug("shFileURL:{}", shFileURL);
			tomoDoShScript = new File(FileLocator.toFileURL(shFileURL).toURI());
			logger.debug("tomoDoShScript:{}", tomoDoShScript);
			String shScriptName = tomoDoShScript.getAbsolutePath();

			IPath fullPath = nexusFile.getLocation();
			if (quick) {
				fullPath = new Path(reducedNexusFile.getPath());
			}
			String nexusFileLocation = nexusFile.getLocation().toString();
			if (quick) {
				nexusFileLocation = reducedNexusFile.getPath();
			}
			File path = new File(nexusFileLocation);
			String imagePath = path.getName().replace(NEXUS_EXTN, "");
			File pathToImages = null;
			if (quick) {
				pathToImages = new File(ReconUtil.getReconstructedReducedDataDirectoryPath(nexusFileLocation));
			} else {
				imagePath = String.format("%s%s%s", ReconUtil.getReconOutDir(nexusFileLocation).toString(),
						File.separator, imagePath);
				pathToImages = new File(imagePath);
			}

			final File pathImgs = pathToImages;

			Job deleteOldReconJob = new Job(String.format("Deleting old reconstruction files(%s)", nexusFile.getName())) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					if (pathImgs.exists()) {
						final File[] files = pathImgs.listFiles();
						for (File f : files) {
							try {
								f.delete();
							} catch (Exception e) {
								logger.warn("Cannot delete file");
							}
						}
					} else {
						try {
							pathImgs.mkdirs();
						} catch (Exception ex) {
							logger.warn("Cannot create writing directory");
						}
					}
					return Status.OK_STATUS;
				}
			};
			deleteOldReconJob.setRule(new ReconSchedulingRule(nexusFile.getLocation().toOSString()));
			deleteOldReconJob.schedule();

			String fileName = fullPath.toOSString();

			String templateFileName = getHmSettingsInProcessingDir().getAbsolutePath();

			int height = hdfShape[1];
			if (quick) {
				if (reducedDataShape == null) {
					reducedDataShape = getReducedDataShape();
				}
				if (reducedDataShape != null) {
					height = reducedDataShape[1];
				}
			}
			String jobNameToDisplay = null;
			String command = null;
			if (quick) {
				// to add -p
				command = String.format(TOMO_QUICK_RECON_COMMAND, shScriptName, height, templateFileName, fileName,
						pathToImages.toString());

				logger.debug("Command that will be run:{}", command);
				jobNameToDisplay = String.format(JOB_NAME_QUICK_RECONSTRUCTION, nexusFile.getName());
				runCommand(jobNameToDisplay, command);
				updatePlotAfterQuickRecon(nexusFileLocation);
			} else {
				int startHeight = 0;
				int endHeight = hdfShape[1];
				if (startEnd != null) {
					startHeight = startEnd[0];
					endHeight = startEnd[1];
				}
				command = String.format(TOMO_FULL_RECON_COMMAND, shScriptName, startHeight, endHeight,
						templateFileName, fileName, pathToImages.toString());
				logger.debug("Command that will be run:{}", command);

				jobNameToDisplay = String.format(JOB_NAME_FULL_RECONSTRUCTION, nexusFile.getName());

				Resource reconResultsResource = Activator.getDefault().getReconResultsResource();
				if (reconResultsResource != null) {
					EObject eObject = reconResultsResource.getContents().get(0);
					String formattedDate = DateFormat.getTimeInstance().format(new Date());
					if (eObject instanceof ReconResults) {
						ReconResults results = (ReconResults) eObject;
						ReconstructionDetail reconstructionDetail = results.getReconstructionDetail(nexusFileLocation);
						EditingDomain reconResultsEditingDomain = Activator.getDefault().getReconResultsEditingDomain();
						Command commandToExecute = null;
						if (reconstructionDetail == null) {
							ReconstructionDetail detail = ReconresultsFactory.eINSTANCE.createReconstructionDetail();
							detail.setNexusFileLocation(nexusFileLocation);
							detail.setNexusFileName(nexusFile.getName());
							detail.setReconstructedLocation(pathToImages.toString());

							detail.setTimeReconStarted(formattedDate);

							commandToExecute = AddCommand.create(reconResultsEditingDomain, results,
									ReconresultsPackage.eINSTANCE.getReconResults_Reconresult(), detail);

						} else {
							CompoundCommand cmd = new CompoundCommand();
							cmd.append(SetCommand.create(reconResultsEditingDomain, reconstructionDetail,
									ReconresultsPackage.eINSTANCE.getReconstructionDetail_TimeReconStarted(),
									formattedDate));
							cmd.append(SetCommand.create(reconResultsEditingDomain, reconstructionDetail,
									ReconresultsPackage.eINSTANCE.getReconstructionDetail_ReconstructedLocation(),
									pathToImages.toString()));
							commandToExecute = cmd;
						}
						try {
							reconResultsEditingDomain.getCommandStack().execute(commandToExecute);
							reconResultsResource.save(((ReconresultsResourceImpl) reconResultsResource)
									.getDefaultSaveOptions());
						} catch (Exception ex) {
							logger.debug("Unable to add entry for recon results:", ex);
						}
					}
				}
				runCommand(jobNameToDisplay, command);
			}
		} catch (URISyntaxException e) {
			logger.error("Incorrect URI for script", e);
		} catch (IOException e) {
			logger.error("Cannot find script file", e);
		}
	}

	private Dataset getImageDataFromProjectionsView() {
		IViewPart projectionsView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(ProjectionsView.ID);
		ISelection selection = projectionsView.getViewSite().getSelectionProvider().getSelection();
		if (selection instanceof ProjectionSliceSelection) {
			ProjectionSliceSelection sliceSelection = (ProjectionSliceSelection) selection;
			return sliceSelection.getDataSetPlotted();
		}
		return null;
	}

	private int getSliceSelectionFromProjectionsView() {
		IViewPart projectionsView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(ProjectionsView.ID);
		ISelection selection = projectionsView.getViewSite().getSelectionProvider().getSelection();
		if (selection instanceof ProjectionSliceSelection) {
			ProjectionSliceSelection sliceSelection = (ProjectionSliceSelection) selection;
			return sliceSelection.getSliceNumber();
		}
		return -1;
	}

	protected void updatePlotAfterQuickRecon(String nexusFileLocation) {
		int sliceNumber = getSliceSelectionFromProjectionsView();
		if (sliceNumber >= 0) {
			UpdatePlotJob updatePlotJob = new UpdatePlotJob(this);
			updatePlotJob.setRule(new ReconSchedulingRule(nexusFile.getLocation().toOSString()));
			updatePlotJob.setPixelPosition(sliceNumber);
			updatePlotJob.setName(String.format("Update plot after reconstruction:%s", nexusFile.getName()));
			updatePlotJob.setNexusFileLocation(nexusFileLocation);
			updatePlotJob.schedule();
		}
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
			URL shFileURL = new URL(String.format("platform:/plugin/%s/%s", Activator.PLUGIN_ID, SCRIPTS_TOMODO_SH));
			logger.debug("shFileURL:{}", shFileURL);
			URL pyFileURL = new URL(String.format("platform:/plugin/%s/%s", Activator.PLUGIN_ID, SCRIPTS_TOMODO_PY));
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
			String templateFileName = getHmSettingsInProcessingDir().getAbsolutePath();

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
		if (getDefaultSettingFile().exists()) {
			try {

				IProject tomoSettingsProject = Activator.getDefault().getTomoFilesProject();
				final IFile tomoSettingsFile = tomoSettingsProject.getFile(getHmSettingsInProcessingDir().getName());
				if (!tomoSettingsFile.exists()) {
					new WorkspaceModifyOperation() {

						@Override
						protected void execute(IProgressMonitor monitor) throws CoreException,
								InvocationTargetException, InterruptedException {
							try {
								tomoSettingsFile.createLink(new Path(getHmSettingsInProcessingDir().getAbsolutePath()),
										IResource.REPLACE, monitor);
							} catch (IllegalArgumentException ex) {
								logger.debug("Problem identified - eclipse doesn't refresh the right folder");
							}
						}
					}.run(null);
				}
				IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), tomoSettingsFile);
			} catch (Exception ex) {
				logger.error("Cannot open HM editor - ", ex);
			}
		}
	}

	private ScrolledForm scrolledForm;
	private Form mainForm;
	private PageBook pgBook;
	private Composite emptyCmp;
	private boolean isHdf = false;
	private int[] hdfShape;

	private TableViewer propertyTableViewer;

	private PropertyDescriptor getPropertyDescriptor(String id) {
		return propertyDescriptors.get(id);
	}

	protected void saveModel() {
		HMxmlType model = getHmXmlModel();
		try {
			FBPType fbp = model.getFBP();
			BackprojectionType backprojection = fbp.getBackprojection();

			backprojection.setImageCentre(BigDecimal.valueOf(Double.parseDouble(txtCentreOfRotation.getText())));

			model.eResource().save(Collections.emptyMap());
		} catch (IOException e) {
			logger.error("Problem saving model ", e);
		}
	}

	private void initializeView() {
		pgBook.showPage(mainForm);

		if (getHmXmlModel() != null) {
			HMxmlType hmxmlType = getHmXmlModel();

			FBPType fbp = hmxmlType.getFBP();
			BackprojectionType backprojection = fbp.getBackprojection();

			txtFileName.setText(nexusFile.getLocation().toOSString());

			float floatValue = backprojection.getImageCentre().floatValue();
			txtCentreOfRotation.setText(Float.toString(floatValue));

			propertyDescriptors = createPropertyDescriptors(hmxmlType);
			propertyTableViewer.setInput(propertyDescriptors);

			String centreOfRot = getCentreOfRotationFromCentreOfRotationView();
			if (centreOfRot != null) {
				txtCentreOfRotation.setText(centreOfRot);
			}
		}

	}

	private LinkedHashMap<String, PropertyDescriptor> createPropertyDescriptors(HMxmlType hmXmlType) {

		FBPType fbp = hmXmlType.getFBP();

		BackprojectionType backprojection = fbp.getBackprojection();

		//
		final ROIType roi = backprojection.getROI();

		// Roi Type property descriptor
		PropertyDescriptor roiType = createRoiTypePropertyDescriptor(roi);

		// Roi X min property descriptor
		PropertyDescriptor roiXMin = createRoiXMinPropertyDescriptor(roi);

		// Roi X max property descriptor
		PropertyDescriptor roiXMax = createRoiXMaxPropertyDescriptor(roi);

		// Roi Y min property descriptor
		PropertyDescriptor roiYMin = createRoiYMinPropertyDescriptor(roi);

		// Roi X max property descriptor
		PropertyDescriptor roiYMax = createRoiYMaxPropertyDescriptor(roi);

		final RingArtefactsType ringArtefacts = fbp.getPreprocessing().getRingArtefacts();
		final Table propViewTable = propertyTableViewer.getTable();

		// Ring Artefacts
		PropertyDescriptor amlProp = createRingArtefactsPropertyDescriptor(ringArtefacts, propViewTable);

		// High Aspect Ratio compensation
		PropertyDescriptor highAspectRatioComp = createHighAspectRatioCompensationPropertyDescriptor(ringArtefacts,
				propViewTable);
		// Incorrection Strength
		PropertyDescriptor incorrectionStrength = createIncorrectionStrengthPropertyDescriptor(ringArtefacts,
				propViewTable);

		FlatDarkFieldsType flatDarkFields = fbp.getFlatDarkFields();

		final FlatFieldType flatField = flatDarkFields.getFlatField();

		// Flat field type
		PropertyDescriptor flatFieldType = createFlatFieldTypePropertyDescriptor(propViewTable, flatField);

		// Flat field value before
		PropertyDescriptor flatFieldValueBefore = createFlatFieldValueBeforePropertyDescriptor(propViewTable, flatField);

		// flat field value after
		PropertyDescriptor flatFieldValueAfter = createFlatFieldValueAfterPropertyDescriptor(propViewTable, flatField);

		final DarkFieldType darkField = flatDarkFields.getDarkField();

		// dark field type
		PropertyDescriptor darkFieldType = createDarkFieldTypePropertyDescriptor(propViewTable, darkField);

		// dark field value before
		PropertyDescriptor darkFieldValueBefore = createDarkFieldValueBeforePropertyDescriptor(propViewTable, darkField);

		// Dark field value after
		PropertyDescriptor darkFieldValueAfter = createDarkFieldValueAfterPropertyDescriptor(propViewTable, darkField);

		propertyDescriptors = new LinkedHashMap<String, PropertyDescriptor>();

		propertyDescriptors.put(ID_ROI_TYPE, roiType);
		propertyDescriptors.put(ID_ROI_X_MIN, roiXMin);
		propertyDescriptors.put(ID_ROI_X_MAX, roiXMax);
		propertyDescriptors.put(ID_ROI_Y_MIN, roiYMin);
		propertyDescriptors.put(ID_ROI_Y_MAX, roiYMax);
		propertyDescriptors.put(ID_RING_ARTEFACTS, amlProp);
		propertyDescriptors.put(ID_HIGH_ASPECT_RATIO_COMPENSATION, highAspectRatioComp);
		propertyDescriptors.put(ID_INCORRECTION_STRENGTH, incorrectionStrength);
		propertyDescriptors.put(ID_FLAT_FIELD_TYPE, flatFieldType);
		propertyDescriptors.put(ID_FLAT_FIELD_VALUE_BEFORE, flatFieldValueBefore);
		propertyDescriptors.put(ID_FLAT_FIELD_VALUE_AFTER, flatFieldValueAfter);
		propertyDescriptors.put(ID_DARK_FIELD_TYPE, darkFieldType);
		propertyDescriptors.put(ID_DARK_FIELD_VALUE_BEFORE, darkFieldValueBefore);
		propertyDescriptors.put(ID_DARK_FIELD_VALUE_AFTER, darkFieldValueAfter);

		return propertyDescriptors;

	}

	private PropertyDescriptor createIncorrectionStrengthPropertyDescriptor(final RingArtefactsType ringArtefacts,
			Table propViewTable) {
		PropertyDescriptor incorrectionStrengthPropDescriptor = new TextPropertyDescriptor(ID_INCORRECTION_STRENGTH,
				LBL_INCORRECTION_STRENGTH, propViewTable) {
			@Override
			public String getValue() {
				return Float.toString(ringArtefacts.getParameterR().floatValue());
			}

			@Override
			public void setValue(Object value) {
				try {
					BigDecimal floatVal = BigDecimal.valueOf(Float.parseFloat((String) value));
					getEditingDomain().getCommandStack().execute(
							SetCommand.create(getEditingDomain(), ringArtefacts,
									HmPackage.eINSTANCE.getRingArtefactsType_ParameterR(), floatVal));

				} catch (NumberFormatException e) {
					getViewSite().getActionBars().getStatusLineManager()
							.setErrorMessage("Invalid value for incorrection strength");
					getViewSite().getShell().getDisplay().timerExec(5000, new Runnable() {

						@Override
						public void run() {
							getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
						}
					});
				}

			}
		};
		return incorrectionStrengthPropDescriptor;
	}

	private PropertyDescriptor createRoiYMaxPropertyDescriptor(final ROIType roi) {
		PropertyDescriptor roiYMax = new PropertyDescriptor(ID_ROI_Y_MAX, LBL_Y_MAX) {
			@Override
			public void setValue(Object value) {
				if (value instanceof Integer) {

					getEditingDomain().getCommandStack().execute(
							SetCommand.create(getEditingDomain(), roi, HmPackage.eINSTANCE.getROIType_Ymax(), value));
				}
			}

			@Override
			public String getValue() {
				return Integer.toString(roi.getYmax());
			}
		};
		return roiYMax;
	}

	private PropertyDescriptor createRoiYMinPropertyDescriptor(final ROIType roi) {
		PropertyDescriptor roiYMin = new PropertyDescriptor(ID_ROI_Y_MIN, LBL_Y_MIN) {
			@Override
			public void setValue(Object value) {
				getEditingDomain().getCommandStack().execute(
						SetCommand.create(getEditingDomain(), roi, HmPackage.eINSTANCE.getROIType_Ymin(), value));
			}

			@Override
			public String getValue() {
				return Integer.toString(roi.getYmin());
			}
		};
		return roiYMin;
	}

	private PropertyDescriptor createRoiXMaxPropertyDescriptor(final ROIType roi) {
		PropertyDescriptor roiXMax = new PropertyDescriptor(ID_ROI_X_MAX, LBL_X_MAX) {
			@Override
			public void setValue(Object value) {
				getEditingDomain().getCommandStack().execute(
						SetCommand.create(getEditingDomain(), roi, HmPackage.eINSTANCE.getROIType_Xmax(), value));
			}

			@Override
			public String getValue() {
				return Integer.toString(roi.getXmax());
			}
		};
		return roiXMax;
	}

	private PropertyDescriptor createRoiXMinPropertyDescriptor(final ROIType roi) {
		PropertyDescriptor roiXMin = new PropertyDescriptor(ID_ROI_X_MIN, LBL_X_MIN) {
			@Override
			public void setValue(Object value) {
				getEditingDomain().getCommandStack().execute(
						SetCommand.create(getEditingDomain(), roi, HmPackage.eINSTANCE.getROIType_Xmin(), value));
			}

			@Override
			public String getValue() {
				return Integer.toString(roi.getXmin());
			}
		};
		return roiXMin;
	}

	private PropertyDescriptor createRoiTypePropertyDescriptor(final ROIType roi) {
		PropertyDescriptor roiType = new PropertyDescriptor(ID_ROI_TYPE, LBL_ROI) {

			@Override
			public void setValue(Object value) {
				getEditingDomain().getCommandStack().execute(
						SetCommand.create(getEditingDomain(), roi.getType(), HmPackage.eINSTANCE.getTypeType3_Value(),
								value));
			}

			@Override
			public String getValue() {
				return roi.getType().getValue();
			}
		};
		return roiType;
	}

	private PropertyDescriptor createDarkFieldValueAfterPropertyDescriptor(final Table propViewTable,
			final DarkFieldType darkField) {
		PropertyDescriptor darkFieldValueAfter = new TextPropertyDescriptor(ID_DARK_FIELD_VALUE_AFTER, LBL_VALUE_AFTER,
				propViewTable) {
			@Override
			public String getValue() {
				return Double.toString(darkField.getValueAfter());
			}

			@Override
			public void setValue(Object value) {
				try {
					Double doubleValue = Double.parseDouble((String) value);
					getEditingDomain().getCommandStack().execute(
							SetCommand.create(getEditingDomain(), darkField,
									HmPackage.eINSTANCE.getDarkFieldType_ValueAfter(), doubleValue));

				} catch (NumberFormatException e) {
					getViewSite().getActionBars().getStatusLineManager()
							.setErrorMessage("Invalid value for flat field after");
					getViewSite().getShell().getDisplay().timerExec(5000, new Runnable() {

						@Override
						public void run() {
							getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
						}
					});
				}

			}
		};
		return darkFieldValueAfter;
	}

	private PropertyDescriptor createDarkFieldValueBeforePropertyDescriptor(final Table propViewTable,
			final DarkFieldType darkField) {
		PropertyDescriptor darkFieldValueBefore = new TextPropertyDescriptor(ID_DARK_FIELD_VALUE_BEFORE,
				LBL_VALUE_BEFORE, propViewTable) {
			@Override
			public String getValue() {
				return Double.toString(darkField.getValueBefore());
			}

			@Override
			public void setValue(Object value) {
				try {
					Double doubleValue = Double.parseDouble((String) value);
					getEditingDomain().getCommandStack().execute(
							SetCommand.create(getEditingDomain(), darkField,
									HmPackage.eINSTANCE.getDarkFieldType_ValueBefore(), doubleValue));

				} catch (NumberFormatException e) {
					getViewSite().getActionBars().getStatusLineManager()
							.setErrorMessage("Invalid value for dark field before");
					getViewSite().getShell().getDisplay().timerExec(5000, new Runnable() {

						@Override
						public void run() {
							getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
						}
					});
				}

			}
		};
		return darkFieldValueBefore;
	}

	private PropertyDescriptor createDarkFieldTypePropertyDescriptor(final Table propViewTable,
			final DarkFieldType darkField) {
		PropertyDescriptor darkFieldType = new PropertyDescriptor(ID_DARK_FIELD_TYPE, LBL_DARK_FIELD_TYPE, darkField
				.getType().getValue()) {

			@Override
			public String getValue() {

				String value = darkField.getType().getValue();
				if (value.equals("Row")) {
					return "Use Image";
				}
				return "Use Constant";
			}

			@Override
			public Object getSelectedValue() {
				String value = getValue();
				if (value.equals("Use Image")) {
					return 0;
				}
				return 1;
			}

			@Override
			public boolean canEdit() {
				return true;
			}

			@Override
			public CellEditor createCellEditor() {
				return new ComboBoxCellEditor(propViewTable, new String[] { "Use Image", "Use Constant" },
						SWT.READ_ONLY);
			}

			@Override
			public void setValue(Object value) {
				if (value instanceof Integer) {
					Integer intVal = (Integer) value;
					String valueToSet = "Row";
					switch (intVal) {
					case 0:
						valueToSet = "Row";
						break;
					case 1:
						valueToSet = "User";
						break;
					}

					getEditingDomain().getCommandStack().execute(
							SetCommand.create(getEditingDomain(), darkField.getType(),
									HmPackage.eINSTANCE.getTypeType13_Value(), valueToSet));

				}
			}
		};
		return darkFieldType;
	}

	private PropertyDescriptor createFlatFieldValueAfterPropertyDescriptor(final Table propViewTable,
			final FlatFieldType flatField) {
		PropertyDescriptor flatFieldValueAfter = new TextPropertyDescriptor(ID_FLAT_FIELD_VALUE_AFTER, LBL_VALUE_AFTER,
				propViewTable) {
			@Override
			public String getValue() {
				return Double.toString(flatField.getValueAfter());
			}

			@Override
			public void setValue(Object value) {
				try {
					Double floatVal = Double.parseDouble((String) value);
					getEditingDomain().getCommandStack().execute(
							SetCommand.create(getEditingDomain(), flatField,
									HmPackage.eINSTANCE.getFlatFieldType_ValueAfter(), floatVal));

				} catch (NumberFormatException e) {
					getViewSite().getActionBars().getStatusLineManager()
							.setErrorMessage("Invalid value for flat field after");
					getViewSite().getShell().getDisplay().timerExec(5000, new Runnable() {

						@Override
						public void run() {
							getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
						}
					});
				}

			}
		};
		return flatFieldValueAfter;
	}

	private PropertyDescriptor createFlatFieldValueBeforePropertyDescriptor(final Table propViewTable,
			final FlatFieldType flatField) {
		PropertyDescriptor flatFieldValueBefore = new TextPropertyDescriptor(ID_FLAT_FIELD_VALUE_BEFORE,
				LBL_VALUE_BEFORE, propViewTable) {
			@Override
			public String getValue() {
				return Double.toString(flatField.getValueBefore());
			}

			@Override
			public void setValue(Object value) {
				try {
					Double floatVal = Double.parseDouble((String) value);
					getEditingDomain().getCommandStack().execute(
							SetCommand.create(getEditingDomain(), flatField,
									HmPackage.eINSTANCE.getFlatFieldType_ValueBefore(), floatVal));

				} catch (NumberFormatException e) {
					getViewSite().getActionBars().getStatusLineManager()
							.setErrorMessage("Invalid value for flat field before");
					getViewSite().getShell().getDisplay().timerExec(5000, new Runnable() {

						@Override
						public void run() {
							getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
						}
					});
				}

			}
		};
		return flatFieldValueBefore;
	}

	private PropertyDescriptor createFlatFieldTypePropertyDescriptor(final Table propViewTable,
			final FlatFieldType flatField) {
		PropertyDescriptor flatFieldType = new PropertyDescriptor(ID_FLAT_FIELD_TYPE, LBL_FLAT_FIELD_TYPE) {
			@Override
			public Object getSelectedValue() {
				String value = getValue();
				if (value.equals("Use Image")) {
					return 0;
				}
				return 1;
			}

			@Override
			public boolean canEdit() {
				return true;
			}

			@Override
			public CellEditor createCellEditor() {
				return new ComboBoxCellEditor(propViewTable, new String[] { "Use Image", "Use Constant" },
						SWT.READ_ONLY);
			}

			@Override
			public String getValue() {
				String value = flatField.getType().getValue();
				if (value.equals("Row")) {
					return "Use Image";
				}
				return "Use Constant";
			}

			@Override
			public void setValue(Object value) {
				if (value instanceof Integer) {
					Integer intVal = (Integer) value;
					String valueToSet = "Row";
					switch (intVal) {
					case 0:
						valueToSet = "Row";
						break;
					case 1:
						valueToSet = "User";
						break;
					}

					getEditingDomain().getCommandStack().execute(
							SetCommand.create(getEditingDomain(), flatField.getType(),
									HmPackage.eINSTANCE.getTypeType15_Value(), valueToSet));

				}
			}
		};
		return flatFieldType;
	}

	private PropertyDescriptor createHighAspectRatioCompensationPropertyDescriptor(
			final RingArtefactsType ringArtefacts, final Table propViewTable) {
		PropertyDescriptor highAspectRatioComp = new TextPropertyDescriptor(ID_HIGH_ASPECT_RATIO_COMPENSATION,
				LBL_HIGH_ASPECT_RATIO_COMPENSATION, propViewTable) {
			@Override
			public String getValue() {
				return Float.toString(ringArtefacts.getNumSeries().getValue().floatValue());
			}

			@Override
			public void setValue(Object value) {
				try {
					BigDecimal floatVal = BigDecimal.valueOf(Float.parseFloat((String) value));
					getEditingDomain().getCommandStack().execute(
							SetCommand.create(getEditingDomain(), ringArtefacts.getNumSeries(),
									HmPackage.eINSTANCE.getNumSeriesType_Value(), floatVal));

				} catch (NumberFormatException e) {
					getViewSite().getActionBars().getStatusLineManager()
							.setErrorMessage("Invalid value for high aspect ratio compensation");
					getViewSite().getShell().getDisplay().timerExec(5000, new Runnable() {

						@Override
						public void run() {
							getViewSite().getActionBars().getStatusLineManager().setErrorMessage(null);
						}
					});
				}

			}
		};
		return highAspectRatioComp;
	}

	private PropertyDescriptor createRingArtefactsPropertyDescriptor(final RingArtefactsType ringArtefacts,
			final Table propViewTable) {
		PropertyDescriptor amlProp = new PropertyDescriptor(ID_RING_ARTEFACTS, LBL_RING_ARTEFACTS) {

			@Override
			public String getValue() {
				return ringArtefacts.getType().getValue();
			}

			@Override
			public Object getSelectedValue() {
				String value = getValue();
				if (value.equals("No")) {
					return 0;
				}
				if (value.equals("Column")) {
					return 1;
				}

				return 2;
			}

			@Override
			public boolean canEdit() {
				return true;
			}

			@Override
			public CellEditor createCellEditor() {
				return new ComboBoxCellEditor(propViewTable, new String[] { "No", "Column", "AML" }, SWT.READ_ONLY);
			}

			@Override
			public void setValue(Object value) {
				if (value instanceof Integer) {
					Integer intVal = (Integer) value;
					String valueToSet = "AML";
					switch (intVal) {
					case 0:
						valueToSet = "No";
						break;
					case 1:
						valueToSet = "Column";
						break;
					case 2:
						valueToSet = "AML";
						break;
					}
					getEditingDomain().getCommandStack().execute(
							SetCommand.create(getEditingDomain(), ringArtefacts.getType(),
									HmPackage.eINSTANCE.getTypeType5_Value(), valueToSet));
				}
			}
		};
		return amlProp;
	}

	private String getCentreOfRotationFromCentreOfRotationView() {
		IViewPart centreOfRotView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(CenterOfRotationView.ID);
		if (centreOfRotView != null) {
			ISelection selection = centreOfRotView.getViewSite().getSelectionProvider().getSelection();
			if (selection instanceof ParametersSelection && nexusFile != null) {
				ParametersSelection ps = (ParametersSelection) selection;
				if (ps.getNexusFileFullPath().equals(nexusFile.getLocation().toOSString())) {
					return Double.toString(ps.getCentreOfRotation());
				}
			}
		}

		return null;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		txtCentreOfRotation.setFocus();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (isPartActive()) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection iss = (IStructuredSelection) selection;
				Object firstElement = iss.getFirstElement();
				boolean newSelection = false;
				if (firstElement instanceof IFile
						&& Activator.NXS_FILE_EXTN.equals(((IFile) firstElement).getFileExtension())) {
					nexusFile = (IFile) firstElement;
					newSelection = true;

				}

				if (nexusFile != null && newSelection) {
					processNewNexusFile();
					return;
				}
			}
		}

	}

	@Override
	protected void processNewNexusFile() {
		ILazyDataset datasetFromNexusFile = null;
		try {
			datasetFromNexusFile = getDatasetFromNexusFile();
		} catch (ScanFileHolderException e) {
			logger.error("Unable to load dataset", e);
		}

		if (datasetFromNexusFile != null) {
			super.processNewNexusFile();
			pgBook.showPage(mainForm);
			String path = nexusFile.getLocation().toOSString();
			ILazyDataset actualDataset = getDataSetFromFileLocation(path);
			if (actualDataset != null && actualDataset.getRank() == 3) {
				isHdf = true;
				hdfShape = actualDataset.getShape();
			}
			getViewSite().getShell().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					initializeView();
				}
			});
		} else {
			pgBook.showPage(emptyCmp);
		}
	}

	@Override
	public void dispose() {
		getViewSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(NexusNavigator.ID, this);
		super.dispose();
	}

	@Override
	public void setSelection(ISelection selection) {
		super.setSelection(selection);
		if (selection instanceof ParametersSelection) {
			runReconScript(true);
		}

	}

}