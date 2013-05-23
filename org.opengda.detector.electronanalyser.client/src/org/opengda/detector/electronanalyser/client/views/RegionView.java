package org.opengda.detector.electronanalyser.client.views;

import gda.device.DeviceException;
import gda.device.scannable.ScannableMotor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.selection.FileSelection;
import org.opengda.detector.electronanalyser.client.selection.RegionActivationSelection;
import org.opengda.detector.electronanalyser.client.selection.TotalTimeSelection;
import org.opengda.detector.electronanalyser.client.viewextensionfactories.SequenceViewExtensionFactory;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.opengda.detector.electronanalyser.utils.RegionStepsTimeEstimation;
import org.opengda.detector.electronanalyser.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Region Editor View for defining new or editing existing Region Definition
 * for VG Scienta Electron Analyser.
 * 
 * @author fy65
 * 
 */
public class RegionView extends ViewPart implements ISelectionProvider {
	private static final Logger logger = LoggerFactory
			.getLogger(RegionView.class);

	public RegionView() {
		setTitleToolTip("Editing a selected region parameters");
		// setContentDescription("A view for editing region parameters");
		setPartName("Region Editor");
		this.selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
	}

	private List<ISelectionChangedListener> selectionChangedListeners;

	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private Camera camera;
	private Text txtMinimumSize;
	private Combo passEnergy;
	private Text txtTime;
	private Text txtMinimumTime;
	private Spinner spinnerFrames;
	private Spinner spinnerEnergyChannelTo;
	private Spinner spinnerYChannelTo;
	private Button btnHard;
	private Region region;
	private EditingDomain editingDomain = null;
	private Button btnSoft;
	private ScannableMotor dcmenergy;
	private ScannableMotor pgmenergy;
	private Button btnFixed;
	private Button btnBinding;
	private Combo regionName;
	private Spinner numberOfIterationSpinner;
	private Button btnRepeatuntilStopped;
	private Button btnConfirmAfterEachInteration;
	private Spinner spinnerEnergyChannelFrom;
	private Spinner spinnerYChannelFrom;
	private Button btnPulseMode;
	private Text txtSoftEnergy;
	private double excitationEnergy = 0.0;
	private double hardXRayEnergy = 5000.0; // eV
	private double softXRayEnergy = 500.0; // eV
	private Text txtLow;
	private Text txtHigh;
	private Text txtSize;
	private double sweptStepSize;
	private Text txtCenter;
	private Text txtWidth;
	private Text txtTotalSteps;
	private Text txtTotalTime;
	private Button btnSwept;
	private Combo lensMode;
	private Combo runMode;
	private Button btnNumberOfIterations;
	private Button btnKinetic;
	private Text txtFramesPerSecond;
	private Spinner spinnerSlices;
	private Button btnADCMode;
	private Text txtHardEnergy;
	private ScannableMotor xrayenergy;
	private List<Region> regions;
	private PageBook regionPageBook;
	private Composite plainComposite;
	private ScrolledComposite regionComposite;

	@Override
	public void createPartControl(Composite parent) {
		regionPageBook = new PageBook(parent, SWT.None);
		plainComposite = new Composite(regionPageBook, SWT.None);
		plainComposite.setLayout(new FillLayout());

		// regionPageBook.showPage(plainComposite);
		regionComposite = new ScrolledComposite(regionPageBook, SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);

		regionComposite.setExpandHorizontal(true);
		regionComposite.setExpandVertical(true);

		Composite rootComposite = new Composite(regionComposite, SWT.NONE);
		regionComposite.setContent(rootComposite);
		GridLayout gl_root = new GridLayout();
		gl_root.horizontalSpacing = 2;
		rootComposite.setLayout(gl_root);

		Group grpName = new Group(rootComposite, SWT.NONE);
		grpName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpName.setText("Name");
		grpName.setLayout(new GridLayout());

		regionName = new Combo(grpName, SWT.NONE);
		GridData namelayoutData = new GridData(GridData.FILL_HORIZONTAL);
		regionName.setLayoutData(namelayoutData);
		regionName.setToolTipText("List of available active regions to select");
		final ControlDecoration regionNameControlDecorator = new ControlDecoration(
				regionName, SWT.TOP | SWT.LEFT);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION);
		regionNameControlDecorator.setImage(fieldDecoration.getImage());
		FieldDecoration dec = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(
						FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		namelayoutData.horizontalIndent = dec.getImage().getBounds().width;
		regionName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (regionName.getText().length() > 0) {
					regionNameControlDecorator.hide();
				} else {
					regionNameControlDecorator.show();
					if (region != null) {
						regionNameControlDecorator.setDescriptionText(region
								.getName() + " is not enabled");
					}
					regionNameControlDecorator.setShowHover(true);
				}
			}
		});

		Composite modeComposite = new Composite(rootComposite, SWT.None);
		// Contains Lens model, pass energy, run mode, acquisition mode, and
		// energy mode.
		modeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		modeComposite.setLayout(new GridLayout(2, false));

		Group grpLensMode = new Group(modeComposite, SWT.NONE);
		grpLensMode.setText("Lens Mode");
		grpLensMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpLensMode.setLayout(new GridLayout());

		lensMode = new Combo(grpLensMode, SWT.READ_ONLY);
		lensMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lensMode.setToolTipText("List of available modes to select");

		Group grpPassEnergy = new Group(modeComposite, SWT.NONE);
		grpPassEnergy.setLayout(new GridLayout());
		grpPassEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpPassEnergy.setText("Pass Energy");

		passEnergy = new Combo(grpPassEnergy, SWT.READ_ONLY);
		passEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		passEnergy.setToolTipText("List opf available pass energy to select");

		Group grpRunMode = new Group(modeComposite, SWT.NONE);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 300;
		layoutData.verticalSpan = 2;
		grpRunMode.setLayoutData(layoutData);
		grpRunMode.setLayout(new GridLayout(2, false));
		grpRunMode.setText("Run Mode");

		runMode = new Combo(grpRunMode, SWT.READ_ONLY);
		runMode.setItems(new String[] { "Normal", "Add Dimension" });
		runMode.setToolTipText("List of available run modes");
		runMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(grpRunMode, SWT.NONE);

		btnNumberOfIterations = new Button(grpRunMode, SWT.RADIO);
		btnNumberOfIterations
				.setToolTipText("Enable a number of iterations option");
		btnNumberOfIterations.setText("Number of iterations");

		numberOfIterationSpinner = new Spinner(grpRunMode, SWT.BORDER);
		numberOfIterationSpinner.setMinimum(1);
		numberOfIterationSpinner
				.setToolTipText("Set number of iterations required here");

		btnRepeatuntilStopped = new Button(grpRunMode, SWT.RADIO);
		btnRepeatuntilStopped
				.setToolTipText("Enable repeat until stopped option");
		btnRepeatuntilStopped.setText("Repeat until stopped");

		new Label(grpRunMode, SWT.NONE);

		btnConfirmAfterEachInteration = new Button(grpRunMode, SWT.CHECK);
		btnConfirmAfterEachInteration.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		btnConfirmAfterEachInteration.setText("Confirm after each iteration");
		btnConfirmAfterEachInteration
				.setToolTipText("Enable confirm after each iteration");

		new Label(grpRunMode, SWT.NONE);

		Group grpAcquisitionMode = new Group(modeComposite, SWT.NONE);
		grpAcquisitionMode.setText("Acquisition Mode");
		GridLayout gl_grpAcquisitionMode = new GridLayout();
		grpAcquisitionMode.setLayout(gl_grpAcquisitionMode);
		grpAcquisitionMode
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnSwept = new Button(grpAcquisitionMode, SWT.RADIO);
		btnSwept.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				sweptStepSize = Double.parseDouble(txtSize.getText());
			}
		});
		btnSwept.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSwept.setText("Swept");

		btnFixed = new Button(grpAcquisitionMode, SWT.RADIO);
		btnFixed.setText("Fixed");

		Group grpEnergyMode = new Group(modeComposite, SWT.NONE);
		grpEnergyMode.setText("Energy Mode");
		grpEnergyMode.setLayout(new GridLayout());
		grpEnergyMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnKinetic = new Button(grpEnergyMode, SWT.RADIO);
		btnKinetic.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object source = e.getSource();
				onModifyEnergyMode(source);
			}
		});
		btnKinetic.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnKinetic.setText("Kinetic");

		btnBinding = new Button(grpEnergyMode, SWT.RADIO);
		btnBinding.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object source = e.getSource();
				onModifyEnergyMode(source);
			}
		});
		btnBinding.setText("Binding");

		Group grpEnergy = new Group(rootComposite, SWT.NONE);
		grpEnergy.setText("Energy [eV]");
		grpEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpEnergy.setLayout(new GridLayout(4, false));

		Label lblLow = new Label(grpEnergy, SWT.NONE);
		lblLow.setText("Low");

		txtLow = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		GridData lowLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		txtLow.setLayoutData(lowLayoutData);
		txtLow.setToolTipText("start energy");
		// final ControlDecoration textLowControlDecorator = new
		// ControlDecoration(
		// txtLow, SWT.TOP | SWT.LEFT);
		// FieldDecoration textLowFieldDecoration = FieldDecorationRegistry
		// .getDefault().getFieldDecoration(
		// FieldDecorationRegistry.DEC_ERROR);
		// textLowControlDecorator.setImage(textLowFieldDecoration.getImage());
		// FieldDecoration dec1 = FieldDecorationRegistry.getDefault()
		// .getFieldDecoration(
		// FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		// lowLayoutData.horizontalIndent = dec1.getImage().getBounds().width;
		// txtLow.addModifyListener(new ModifyListener() {
		//
		// @Override
		// public void modifyText(ModifyEvent e) {
		// if (Double.parseDouble(txtLow.getText()) < excitationEnergy) {
		// textLowControlDecorator.hide();
		// txtLow.setForeground(ColorConstants.black);
		// } else {
		// textLowControlDecorator.show();
		// textLowControlDecorator
		// .setDescriptionText("This value cannot be greater than current excitation energy "
		// + excitationEnergy);
		// textLowControlDecorator.setShowHover(true);
		// txtLow.setForeground(ColorConstants.red);
		// }
		// }
		// });

		Label lblCenter = new Label(grpEnergy, SWT.NONE);
		lblCenter.setText("Center");

		txtCenter = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		GridData centerLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		txtCenter.setLayoutData(centerLayoutData);
		txtCenter.setToolTipText("Center/Fixed energy");
		// final ControlDecoration textCenterControlDecorator = new
		// ControlDecoration(
		// txtCenter, SWT.TOP | SWT.LEFT);
		// FieldDecoration textCenterFieldDecoration = FieldDecorationRegistry
		// .getDefault().getFieldDecoration(
		// FieldDecorationRegistry.DEC_ERROR);
		// textCenterControlDecorator.setImage(textCenterFieldDecoration
		// .getImage());
		// FieldDecoration dec2 = FieldDecorationRegistry.getDefault()
		// .getFieldDecoration(
		// FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		// centerLayoutData.horizontalIndent =
		// dec2.getImage().getBounds().width;
		// txtCenter.addModifyListener(new ModifyListener() {
		//
		// @Override
		// public void modifyText(ModifyEvent e) {
		// if (Double.parseDouble(txtCenter.getText()) < excitationEnergy) {
		// textCenterControlDecorator.hide();
		// txtCenter.setForeground(ColorConstants.black);
		// } else {
		// textCenterControlDecorator.show();
		// textCenterControlDecorator
		// .setDescriptionText("This value cannot be greater than current excitation energy "
		// + excitationEnergy);
		// textCenterControlDecorator.setShowHover(true);
		// txtCenter.setForeground(ColorConstants.red);
		// }
		// }
		// });

		Label lblHigh = new Label(grpEnergy, SWT.NONE);
		lblHigh.setText("High");

		txtHigh = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		GridData highLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		txtHigh.setLayoutData(highLayoutData);
		txtHigh.setToolTipText("Stop enenery");
		// final ControlDecoration textHighControlDecorator = new
		// ControlDecoration(
		// txtHigh, SWT.TOP | SWT.LEFT);
		// FieldDecoration textHighFieldDecoration = FieldDecorationRegistry
		// .getDefault().getFieldDecoration(
		// FieldDecorationRegistry.DEC_ERROR);
		// textHighControlDecorator.setImage(textHighFieldDecoration.getImage());
		// FieldDecoration dec3 = FieldDecorationRegistry.getDefault()
		// .getFieldDecoration(
		// FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		// highLayoutData.horizontalIndent = dec3.getImage().getBounds().width;
		// txtHigh.addModifyListener(new ModifyListener() {
		// @Override
		// public void modifyText(ModifyEvent e) {
		// if (Double.parseDouble(txtHigh.getText()) < excitationEnergy) {
		// textHighControlDecorator.hide();
		// txtHigh.setForeground(ColorConstants.black);
		// } else {
		// textHighControlDecorator.show();
		// textHighControlDecorator
		// .setDescriptionText("This value cannot be greater than current excitation energy "
		// + excitationEnergy);
		// textHighControlDecorator.setShowHover(true);
		// txtHigh.setForeground(ColorConstants.red);
		// }
		// }
		// });

		Label lblWidth = new Label(grpEnergy, SWT.NONE);
		lblWidth.setText("Width");

		txtWidth = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		txtWidth.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtWidth.setToolTipText("Enery width");
		txtWidth.setEditable(false);

		Group grpStep = new Group(rootComposite, SWT.NONE);
		grpStep.setText("Step");
		grpStep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpStep.setLayout(new GridLayout(4, false));

		Label lblFrames = new Label(grpStep, SWT.NONE);
		lblFrames.setText("Frames");

		spinnerFrames = new Spinner(grpStep, SWT.BORDER);
		spinnerFrames.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerFrames.setToolTipText("Number of frames per step");
		spinnerFrames.setMinimum(1);
		spinnerFrames.setMaximum(1000);

		Label lblFramesPerSecond = new Label(grpStep, SWT.NONE);
		lblFramesPerSecond.setText("Frames/s");

		txtFramesPerSecond = new Text(grpStep, SWT.BORDER);
		txtFramesPerSecond
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtFramesPerSecond.setToolTipText("Camera frame rate");
		txtFramesPerSecond.setEditable(false);
		txtFramesPerSecond.setText(String.format("%d", camera.getFrameRate()));

		Label lblTime = new Label(grpStep, SWT.NONE);
		lblTime.setText("Time [s]");

		txtTime = new Text(grpStep, SWT.BORDER | SWT.SINGLE);
		txtTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtTime.setToolTipText("Time per step");

		Label lblMinimumTime = new Label(grpStep, SWT.NONE);
		lblMinimumTime.setText("Min. Time [s]");

		txtMinimumTime = new Text(grpStep, SWT.BORDER);
		txtMinimumTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtMinimumTime.setToolTipText("Minimum time per step allowed");
		txtMinimumTime.setEditable(false);
		txtMinimumTime
				.setText(String.format("%f", 1.0 / camera.getFrameRate()));

		Label lblSize = new Label(grpStep, SWT.NONE);
		lblSize.setText("Size [meV]");

		txtSize = new Text(grpStep, SWT.BORDER | SWT.SINGLE);
		txtSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtSize.setToolTipText("Energy size per step");

		Label lblMinimumSize = new Label(grpStep, SWT.NONE);
		lblMinimumSize.setText("Min. Size [meV]");

		txtMinimumSize = new Text(grpStep, SWT.BORDER);
		txtMinimumSize.setToolTipText("Minimum energy size per step allowed");
		txtMinimumSize.setEditable(false);

		Label lblTotalTime = new Label(grpStep, SWT.NONE);
		lblTotalTime.setText("Total Time [s]");

		txtTotalTime = new Text(grpStep, SWT.BORDER);
		txtTotalTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtTotalTime
				.setToolTipText("Anticipated total time for this collection");
		txtTotalTime.setEditable(false);

		Label lblTotalSteps = new Label(grpStep, SWT.NONE);
		lblTotalSteps.setText("Total Steps");

		txtTotalSteps = new Text(grpStep, SWT.BORDER);
		txtTotalSteps.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtTotalSteps
				.setToolTipText("Total number of steps for this collection");
		txtTotalSteps.setEditable(false);

		Group grpDetector = new Group(rootComposite, SWT.NONE);
		grpDetector.setText("Detector");
		grpDetector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpDetector.setLayout(new GridLayout(5, false));

		Label lblXChannel = new Label(grpDetector, SWT.NONE);
		lblXChannel.setText("Energy Channels:");

		Label lblEnergyChannelFrom = new Label(grpDetector, SWT.NONE);
		lblEnergyChannelFrom.setText("From");

		spinnerEnergyChannelFrom = new Spinner(grpDetector, SWT.BORDER);
		spinnerEnergyChannelFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerEnergyChannelFrom)) {
					updateFeature(region, RegiondefinitionPackage.eINSTANCE
							.getRegion_FirstXChannel(),
							spinnerEnergyChannelFrom.getSelection());
					if (btnFixed.getSelection()) {
						txtSize.setText(String.format("%.3f",
								fixedEnergyRange()));
					}
				}
			}
		});
		spinnerEnergyChannelFrom.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		spinnerEnergyChannelFrom.setToolTipText("Low bound");
		spinnerEnergyChannelFrom.setMinimum(1);
		spinnerEnergyChannelFrom.setMaximum(camera.getCameraXSize());

		Label lblEnergyChannelTo = new Label(grpDetector, SWT.NONE);
		lblEnergyChannelTo.setText("To");

		spinnerEnergyChannelTo = new Spinner(grpDetector, SWT.BORDER);
		spinnerEnergyChannelTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerEnergyChannelTo)) {
					updateFeature(region, RegiondefinitionPackage.eINSTANCE
							.getRegion_LastXChannel(), spinnerEnergyChannelTo
							.getSelection());
					if (btnFixed.getSelection()) {
						txtSize.setText(String.format("%.3f",
								fixedEnergyRange()));
					}
				}
			}
		});
		spinnerEnergyChannelTo.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		spinnerEnergyChannelTo.setToolTipText("High bound");
		spinnerEnergyChannelTo.setMinimum(1);
		spinnerEnergyChannelTo.setMaximum(camera.getCameraXSize());

		Label lblYChannel = new Label(grpDetector, SWT.NONE);
		lblYChannel.setText("Y Channels:");

		Label lblYChannelFrom = new Label(grpDetector, SWT.NONE);
		lblYChannelFrom.setText("From");

		spinnerYChannelFrom = new Spinner(grpDetector, SWT.BORDER);
		spinnerYChannelFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerYChannelFrom)) {
					updateFeature(region, RegiondefinitionPackage.eINSTANCE
							.getRegion_FirstYChannel(), spinnerYChannelFrom
							.getSelection());
				}
			}
		});
		spinnerYChannelFrom
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerYChannelFrom.setToolTipText("Low bound");
		spinnerYChannelFrom.setMinimum(1);
		spinnerYChannelFrom.setMaximum(camera.getCameraYSize());

		Label lblYChannelTo = new Label(grpDetector, SWT.NONE);
		lblYChannelTo.setText("To");

		spinnerYChannelTo = new Spinner(grpDetector, SWT.BORDER);
		spinnerYChannelTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerYChannelTo)) {
					updateFeature(region, RegiondefinitionPackage.eINSTANCE
							.getRegion_LastYChannel(), spinnerYChannelTo
							.getSelection());
				}
			}
		});
		spinnerYChannelTo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerYChannelTo.setToolTipText("High bound");
		spinnerYChannelTo.setMinimum(1);
		spinnerYChannelTo.setMaximum(camera.getCameraYSize());

		Label lblSclies = new Label(grpDetector, SWT.NONE);
		lblSclies.setText("Slices:");
		new Label(grpDetector, SWT.NONE);

		spinnerSlices = new Spinner(grpDetector, SWT.BORDER);
		spinnerSlices.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerSlices)) {
					updateFeature(region, RegiondefinitionPackage.eINSTANCE
							.getRegion_Slices(), spinnerSlices.getSelection());
				}
			}
		});
		spinnerSlices.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerSlices.setToolTipText("Number of slices");
		spinnerSlices.setMinimum(1);

		new Label(grpDetector, SWT.NONE);
		new Label(grpDetector, SWT.NONE);

		Label lblDetectorMode = new Label(grpDetector, SWT.NONE);
		lblDetectorMode.setText("Mode:");

		new Label(grpDetector, SWT.NONE);

		btnADCMode = new Button(grpDetector, SWT.RADIO);
		btnADCMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(btnADCMode)) {
					updateFeature(region, RegiondefinitionPackage.eINSTANCE
							.getRegion_DetectorMode(), DETECTOR_MODE.ADC);
				}
			}
		});
		btnADCMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnADCMode.setText("ADC");

		new Label(grpDetector, SWT.NONE);

		btnPulseMode = new Button(grpDetector, SWT.RADIO);
		btnPulseMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(btnPulseMode)) {
					updateFeature(region, RegiondefinitionPackage.eINSTANCE
							.getRegion_DetectorMode(),
							DETECTOR_MODE.PULSE_COUNTING);
				}
			}
		});
		btnPulseMode.setText("Pulse Counting");

		Group grpExcitationEnergy = new Group(rootComposite, SWT.NONE);
		grpExcitationEnergy.setText("Excitation Energy [eV]");
		grpExcitationEnergy
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			grpExcitationEnergy.setLayout(new GridLayout(3, true));

			Label lblXRaySource = new Label(grpExcitationEnergy, SWT.None);
			lblXRaySource.setText("X-Ray Source:");

			btnHard = new Button(grpExcitationEnergy, SWT.RADIO);
			btnHard.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			btnHard.setText("Hard");
			btnHard.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (e.getSource().equals(btnHard) && btnHard.getSelection()) {
						updateExcitationEnergy(txtHardEnergy);
					}
				}
			});

			btnSoft = new Button(grpExcitationEnergy, SWT.RADIO);
			btnSoft.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			btnSoft.setText("Soft");
			btnSoft.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (e.getSource().equals(btnSoft) && btnSoft.getSelection()) {
						updateExcitationEnergy(txtSoftEnergy);
					}
				}
			});

			Label lblCurrentValue = new Label(grpExcitationEnergy, SWT.NONE);
			lblCurrentValue.setText("Beam energy:");

			txtHardEnergy = new Text(grpExcitationEnergy, SWT.BORDER
					| SWT.READ_ONLY);
			txtHardEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			txtHardEnergy.setToolTipText("Current hard X-ray beam energy");

			txtSoftEnergy = new Text(grpExcitationEnergy, SWT.BORDER
					| SWT.READ_ONLY);
			txtSoftEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			txtSoftEnergy.setToolTipText("Current soft X-ray beam energy");
		} else {
			grpExcitationEnergy.setLayout(new GridLayout(2, true));

			Label lblCurrentValue = new Label(grpExcitationEnergy, SWT.NONE);
			lblCurrentValue.setText("Beam energy:");

			txtHardEnergy = new Text(grpExcitationEnergy, SWT.BORDER
					| SWT.READ_ONLY);
			txtHardEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			txtHardEnergy.setToolTipText("Current X-ray beam energy");
		}

		regionComposite.setMinSize(rootComposite.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

		initialisation();
		getViewSite().setSelectionProvider(this);
		getViewSite()
				.getWorkbenchWindow()
				.getSelectionService()
				.addSelectionListener(SequenceViewExtensionFactory.ID,
						selectionListener);
	}

	private ISelectionListener selectionListener = new INullSelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof FileSelection) {
				// sequence file changed
				try {
					regions = regionDefinitionResourceUtil.getRegions();
					populateRegionNameCombo(regions);
					if (regions.isEmpty()) {
						region=null;
						regionPageBook.showPage(plainComposite);
					} else {
						regionPageBook.showPage(regionComposite);
						if (regionName.getItemCount() > 0) {
							// if there is enabled regions
							region = (Region) regionName.getData("0");
							initialiseViewWithRegionData(region);
							fireSelectionChanged(region);
						} else {
							// no enabled region
							region = regions.get(0);
							initialiseViewWithRegionData(region);
							fireSelectionChanged(region);
						}
					}
				} catch (Exception e) {
					logger.error("Cannot get regions list from {}",
							regionDefinitionResourceUtil.getFileName(), e);
				}
			} else if (selection instanceof RegionActivationSelection) {
				populateRegionNameCombo(regions);
				if (region.isEnabled()) {
					regionName.setText(region.getName());
				} else {
					regionName.setText("");
				}
			} else if (selection instanceof IStructuredSelection) {
				if (StructuredSelection.EMPTY.equals(selection)) {
					region=null;
					regionPageBook.showPage(plainComposite);
				} else {
					IStructuredSelection sel = (IStructuredSelection) selection;
					Object firstElement = sel.getFirstElement();
					if (firstElement instanceof Region) {
						if (!firstElement.equals(region)) {
							region = (Region) firstElement;
							initialiseViewWithRegionData(region);
						}
					}
					regionPageBook.showPage(regionComposite);
				}
			}
		}
	};

	private Region getSelectedRegionInSequenceView() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow activeWorkbenchWindow = workbench
				.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		IViewPart findView = null;
		if (activePage != null) {
			findView = activePage.findView(SequenceViewExtensionFactory.ID);
		}
		if (findView != null) {
			ISelection selection = findView.getViewSite()
					.getSelectionProvider().getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSel = (IStructuredSelection) selection;
				Object firstElement = structuredSel.getFirstElement();
				if (firstElement instanceof Region) {
					region = (Region) firstElement;
					return region;
				}
			}
		}
		return null;
	}

	private void initialisation() {
		// TODO replace the following values by sourcing it from detector at
		// initialisation
		lensMode.setItems(new String[] { "Transmission", "Angular45",
				"Angular60" });
		// TODO replace the following values by sourcing it from detector at
		// initialisation
		passEnergy.setItems(new String[] { "5", "10", "50", "75", "100", "200",
				"500" });

		try {
			editingDomain = regionDefinitionResourceUtil.getEditingDomain();
		} catch (Exception e1) {
			logger.error("Cannot get Editing Domain object.", e1);
		}
		regions = Collections.emptyList();
		try {
			regions = regionDefinitionResourceUtil.getRegions();
		} catch (Exception e1) {
			logger.error("Cannot get regions from resource: ", e1);
		}

		if (regions.isEmpty()) {
			// open an empty sequence - no region
			region = null;
			new Label(plainComposite, SWT.None)
					.setText("There is no regions to be displayed in this sequence.");
			regionPageBook.showPage(plainComposite);
		} else {
			new Label(plainComposite, SWT.None)
					.setText("There is no region selected in this sequence.");
			regionPageBook.showPage(regionComposite);
			populateRegionNameCombo(regions);
			Region selectedRegionInSequenceView = getSelectedRegionInSequenceView();
			if (selectedRegionInSequenceView != null) {
				// open Region editor when sequence editor is already available
				initialiseRegionView(selectedRegionInSequenceView);
			} else if (regionName.getItemCount() > 0) {
				// if there is enabled regions
				region = (Region) regionName.getData("0");
				initialiseRegionView(region);
				fireSelectionChanged(region);
			} else {
				// no enabled region
				region = regions.get(0);
				initialiseRegionView(region);
				fireSelectionChanged(region);
			}
		}

		// add listener after initialisation otherwise return 'empty String'
		passEnergy.addSelectionListener(passEnerySelectionAdapter);
		btnSwept.addSelectionListener(sweptSelectionListener);
		btnFixed.addSelectionListener(fixedSelectionListener);
		txtLow.addSelectionListener(energySelectionListener);
		txtHigh.addSelectionListener(energySelectionListener);
		txtCenter.addSelectionListener(energySelectionListener);
		spinnerFrames.addSelectionListener(framesSelectionListener);
		txtTime.addSelectionListener(timeSelectionListener);
		txtSize.addSelectionListener(sizeSelectionListener);
		txtMinimumSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			btnHard.addSelectionListener(xRaySourceSelectionListener);
			btnSoft.addSelectionListener(xRaySourceSelectionListener);
		}
		regionName.addSelectionListener(regionNameSelAdapter);
		lensMode.addSelectionListener(lensModeSelAdaptor);
		runMode.addSelectionListener(runModeSelAdaptor);
		btnNumberOfIterations
				.addSelectionListener(btnNumberOfIterationSelAdaptor);
		btnRepeatuntilStopped
				.addSelectionListener(repeatUntilStoopedSelAdaptor);
		btnConfirmAfterEachInteration
				.addSelectionListener(confirmAfterEachIterationSelAdaptor);
		numberOfIterationSpinner
				.addSelectionListener(numIterationSpinnerSelAdaptor);

		// TODO add monitor to dcmenergy in EPICS
		// TODO add monitor to pgmenergy in EPICS
		// TODO add monitor to total steps in EPICS
	}

	private void populateRegionNameCombo(List<Region> regions) {
		// file regionName combo with active regions from region list
		int index = 0;
		regionName.removeAll();
		if (!regions.isEmpty()) {
			for (Region region : regions) {
				if (region.isEnabled()) {
					regionName.add(region.getName());
					regionName.setData(String.valueOf(index), region);
					index++;
				}
			}
		}
	}

	// Update features when it changes in Region Editor
	private void updateFeature(EObject region, Object feature, Object value) {
		if (region != null) {
			if (editingDomain != null) {
				Command setNameCmd = SetCommand.create(editingDomain, region,
						feature, value);
				editingDomain.getCommandStack().execute(setNameCmd);
			}
		}
	}

	private SelectionAdapter regionNameSelAdapter = new SelectionAdapter() {
		public void widgetDefaultSelected(SelectionEvent e) {
			// on enter - change region name
			if (e.getSource().equals(regionName)) {
				String regionNamePrefix = StringUtils
						.prefixBeforeInt(regionName.getText());
				if (!regionNamePrefix.isEmpty()) {
					int largestIntInNames = StringUtils
							.largestIntAtEndStringsWithPrefix(getRegionNames(),
									regionNamePrefix);
					if (largestIntInNames != -1) {
						largestIntInNames++;
						regionName
								.setText(regionNamePrefix + largestIntInNames);
					}
				}
				updateFeature(region,
						RegiondefinitionPackage.eINSTANCE.getRegion_Name(),
						regionName.getText());
			}
		}

		// on selection from list
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(regionName)) {
				Object data = regionName.getData(String.valueOf(regionName
						.getSelectionIndex()));
				if (data instanceof Region) {
					initialiseViewWithRegionData((Region) data);
					fireSelectionChanged((Region) data);
				}
			}

		}
	};

	protected List<String> getRegionNames() {
		List<String> regionNames = new ArrayList<String>();
		for (Region region : regions) {
			regionNames.add(region.getName());
		}
		return regionNames;
	}

	private SelectionAdapter lensModeSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(lensMode)) {
				updateFeature(region,
						RegiondefinitionPackage.eINSTANCE.getRegion_LensMode(),
						lensMode.getText());
			}
		}
	};
	private SelectionAdapter runModeSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(runMode)) {
				// need to use index because string in display is different from
				// those defined in ENUM literal
				int index = runMode.getSelectionIndex();
				updateFeature(region.getRunMode(),
						RegiondefinitionPackage.eINSTANCE.getRunMode_Mode(),
						RUN_MODES.get(index));
				updateFeature(region.getRunMode(),
						RegiondefinitionPackage.eINSTANCE
								.getRunMode_RunModeIndex(), index);
			}
		}
	};
	SelectionAdapter btnNumberOfIterationSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnNumberOfIterations)) {
				updateFeature(region.getRunMode(),
						RegiondefinitionPackage.eINSTANCE
								.getRunMode_NumIterationOption(), true);
				updateFeature(region.getRunMode(),
						RegiondefinitionPackage.eINSTANCE
								.getRunMode_RepeatUntilStopped(), false);
			}
		}
	};
	private SelectionAdapter repeatUntilStoopedSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnRepeatuntilStopped)) {
				updateFeature(region.getRunMode(),
						RegiondefinitionPackage.eINSTANCE
								.getRunMode_RepeatUntilStopped(), true);
				updateFeature(region.getRunMode(),
						RegiondefinitionPackage.eINSTANCE
								.getRunMode_NumIterationOption(), false);
			}
		}
	};
	SelectionAdapter confirmAfterEachIterationSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnConfirmAfterEachInteration)) {
				updateFeature(region.getRunMode(),
						RegiondefinitionPackage.eINSTANCE
								.getRunMode_ConfirmAfterEachIteration(),
						btnConfirmAfterEachInteration.getSelection());
			}
		}
	};
	SelectionAdapter numIterationSpinnerSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			if (e.getSource().equals(numberOfIterationSpinner)) {
				updateFeature(region.getRunMode(),
						RegiondefinitionPackage.eINSTANCE
								.getRunMode_NumIterations(),
						numberOfIterationSpinner.getSelection());
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(numberOfIterationSpinner)) {
				updateFeature(region.getRunMode(),
						RegiondefinitionPackage.eINSTANCE
								.getRunMode_NumIterations(),
						numberOfIterationSpinner.getSelection());
			}
		}
	};

	protected void initialiseViewWithRegionData(final Region region) {
		Display display = getViewSite().getShell().getDisplay();
		if (!display.isDisposed()) {
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					initialiseRegionView(region);
				}
			});

		}
	}

	private SelectionAdapter passEnerySelectionAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifyPassEnergy(source);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifyPassEnergy(source);
		}
	};

	private void onModifyPassEnergy(Object source) {
		if (source.equals(passEnergy)) {
			String passEnergyFromCombo = passEnergy.getText();
			int passEnergyIntValue = Integer.parseInt(passEnergyFromCombo);
			txtMinimumSize.setText(String.format("%.3f",
					camera.getEnergyResolution() * passEnergyIntValue));
			updateFeature(region,
					RegiondefinitionPackage.eINSTANCE.getRegion_PassEnergy(),
					passEnergyIntValue);
			updateEnergyStep();
		}
	}

	private SelectionAdapter framesSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifyFrames(source);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifyFrames(source);
		}
	};

	private void onModifyFrames(Object source) {
		if (source.equals(spinnerFrames)) {
			txtTime.setText(String.format(
					"%.3f",
					Double.parseDouble(txtMinimumTime.getText())
							* Integer.parseInt(spinnerFrames.getText())));
			updateFeature(region,
					RegiondefinitionPackage.eINSTANCE.getRegion_StepTime(),
					Double.parseDouble(txtTime.getText()));
			updateTotalTime();
		}
	}

	private SelectionAdapter sizeSelectionListener = new SelectionAdapter() {
		public void widgetDefaultSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source.equals(txtSize)) {
				if (Double.parseDouble(txtSize.getText()) < Double
						.parseDouble(txtMinimumSize.getText())) {
					txtSize.setText(txtMinimumSize.getText());
				} else {
					txtSize.setText(String.format("%.3f",
							Double.parseDouble(txtSize.getText())));
				}
				updateFeature(region,
						RegiondefinitionPackage.eINSTANCE
								.getRegion_EnergyStep(),
						Double.parseDouble(txtSize.getText()));
				// set Total steps
				// TODO set to EPICS size PV to get total size update
				updateTotalSteps();
				if (btnSwept.getSelection()) {
					sweptStepSize = Double.parseDouble(txtSize.getText());
				}
			}
		}
	};

	private void updateTotalTime() {
		double calculateTotalTime = RegionStepsTimeEstimation
				.calculateTotalTime(Double.parseDouble(txtTime.getText()),
						Integer.parseInt(txtTotalSteps.getText()));
		txtTotalTime.setText(String.format("%.3f", calculateTotalTime));
		updateFeature(region,
				RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(),
				calculateTotalTime);
		fireSelectionChanged(new TotalTimeSelection());
	}

	private void updateTotalSteps() {
		txtTotalSteps
				.setText(String.format(
						"%d",
						RegionStepsTimeEstimation.calculateTotalSteps(
								Double.parseDouble(txtWidth.getText()),
								Double.parseDouble(txtSize.getText()),
								(Double.parseDouble(txtMinimumSize.getText()) * (Integer
										.parseInt(spinnerEnergyChannelTo
												.getText())
										- Integer
												.parseInt(spinnerEnergyChannelFrom
														.getText()) + 1)))));
		updateFeature(region,
				RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(),
				Integer.parseInt(txtTotalSteps.getText()));
		updateTotalTime();
	}

	private SelectionAdapter timeSelectionListener = new SelectionAdapter() {
		public void widgetDefaultSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifyTime(source);
		}
	};

	protected void onModifyTime(Object source) {
		if (source.equals(txtTime) && !txtTime.getText().isEmpty()) {
			long frames = Math.round(Double.parseDouble(txtTime.getText())
					/ Double.parseDouble(txtMinimumTime.getText()));
			spinnerFrames.setSelection((int) frames);
			txtTime.setText(String.format("%.3f",
					Double.parseDouble(txtTime.getText())));
			updateTotalTime();
			updateFeature(region,
					RegiondefinitionPackage.eINSTANCE.getRegion_StepTime(),
					Double.parseDouble(txtTime.getText()));
		}
	}

	private SelectionAdapter energySelectionListener = new SelectionAdapter() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifyEnergy(source);
		}
	};

	protected void onModifyEnergy(Object source) {
		if (source.equals(txtLow) && txtLow.isFocusControl()) {
			updateEnergyFields(txtLow);
		} else if (source.equals(txtHigh) && txtHigh.isFocusControl()) {
			updateEnergyFields(txtHigh);
		} else if (source.equals(txtCenter) && txtCenter.isFocusControl()) {
			double low = Double.parseDouble(txtCenter.getText())
					- Double.parseDouble(txtWidth.getText()) / 2;
			txtLow.setText(String.format("%.4f", low));
			double high = Double.parseDouble(txtCenter.getText())
					+ Double.parseDouble(txtWidth.getText()) / 2;
			txtHigh.setText(String.format("%.4f", high));
			txtCenter.setText(String.format("%.4f",
					Double.parseDouble(txtCenter.getText())));
			updateFeature(region,
					RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(),
					Double.parseDouble(txtLow.getText()));
			updateFeature(region,
					RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(),
					Double.parseDouble(txtHigh.getText()));
		}
	}

	private void updateEnergyFields(Text txt) {
		if (Double.parseDouble(txtLow.getText()) > Double.parseDouble(txtHigh
				.getText())) {
			String low = txtHigh.getText();
			txtHigh.setText(String.format("%.4f",
					Double.parseDouble(txtLow.getText())));
			txtLow.setText(String.format("%.4f", Double.parseDouble(low)));
			// TODO set lowEnergy, highEnergy to EPICS to get updated total
			// steps.
		} else {
			txt.setText(String.format("%.4f", Double.parseDouble(txt.getText())));
		}
		updateFeature(region,
				RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(),
				Double.parseDouble(txtLow.getText()));
		updateFeature(region,
				RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(),
				Double.parseDouble(txtHigh.getText()));

		double center = (Double.parseDouble(txtLow.getText()) + Double
				.parseDouble(txtHigh.getText())) / 2;
		txtCenter.setText(String.format("%.4f", center));
		double width = Double.parseDouble(txtHigh.getText())
				- Double.parseDouble(txtLow.getText());
		txtWidth.setText(String.format("%.4f", width));
		updateTotalSteps();
	}

	SelectionAdapter fixedSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnFixed) && btnFixed.getSelection()) {
				onModifyAcquisitionMode(e.getSource());
				updateFeature(region,
						RegiondefinitionPackage.eINSTANCE
								.getRegion_AcquisitionMode(),
						ACQUISITION_MODE.FIXED);
				fireSelectionChanged(new TotalTimeSelection());
			}
		}
	};

	private void setToFixedMode() {
		txtLow.setEditable(false);
		txtHigh.setEditable(false);
		txtSize.setEditable(false);
		txtSize.setText(String.format("%.3f", fixedEnergyRange()));
		txtTotalSteps.setText("1");
		txtTotalTime.setText(String.format(
				"%.3f",
				RegionStepsTimeEstimation.calculateTotalTime(
						Double.parseDouble(txtTime.getText()),
						Integer.parseInt(txtTotalSteps.getText()))));
	}

	private double fixedEnergyRange() {
		return Double.parseDouble(txtMinimumSize.getText())
				* (Integer.parseInt(spinnerEnergyChannelTo.getText())
						- Integer.parseInt(spinnerEnergyChannelFrom.getText()) + 1);
	}

	private SelectionAdapter sweptSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnSwept) && btnSwept.getSelection()) {
				onModifyAcquisitionMode(e.getSource());
				updateFeature(region,
						RegiondefinitionPackage.eINSTANCE
								.getRegion_AcquisitionMode(),
						ACQUISITION_MODE.SWEPT);
				fireSelectionChanged(new TotalTimeSelection());
			}
		}
	};

	private void onModifyAcquisitionMode(Object source) {
		if (source.equals(btnSwept)) {
			setToSweptMode();
			updateFeature(region,
					RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(),
					sweptStepSize);
			updateFeature(region,
					RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(),
					Integer.parseInt(txtTotalSteps.getText()));
			updateFeature(
					region,
					RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(),
					RegionStepsTimeEstimation.calculateTotalTime(
							Double.parseDouble(txtTime.getText()),
							Integer.parseInt(txtTotalSteps.getText())));
		} else if (source.equals(btnFixed)) {
			setToFixedMode();
			updateFeature(region,
					RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(),
					fixedEnergyRange());
			updateFeature(region,
					RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(),
					Integer.parseInt(txtTotalSteps.getText()));
			updateFeature(
					region,
					RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(),
					RegionStepsTimeEstimation.calculateTotalTime(
							Double.parseDouble(txtTime.getText()),
							Integer.parseInt(txtTotalSteps.getText())));
			fireSelectionChanged(new TotalTimeSelection());
			fireSelectionChanged(new TotalTimeSelection());
			fireSelectionChanged(new TotalTimeSelection());
			fireSelectionChanged(new TotalTimeSelection());
			fireSelectionChanged(new TotalTimeSelection());
			fireSelectionChanged(new TotalTimeSelection());

		}
	}

	private void setToSweptMode() {
		txtLow.setEditable(true);
		txtHigh.setEditable(true);
		txtSize.setEditable(true);
		// restore the original energy step size for the SWEPT
		txtSize.setText(String.format("%.3f", sweptStepSize));
		if (txtSize.getText().isEmpty()
				|| (Double.parseDouble(txtSize.getText()) < Double
						.parseDouble(txtMinimumSize.getText()))) {
			sweptStepSize = Double.parseDouble(txtMinimumSize.getText());
			txtSize.setText(String.format("%.3f", sweptStepSize));
		}
		if (btnSwept.getSelection()) {
			txtTotalSteps
					.setText(String.format(
							"%d",
							RegionStepsTimeEstimation.calculateTotalSteps(
									Double.parseDouble(txtWidth.getText()),
									Double.parseDouble(txtSize.getText()),
									(Double.parseDouble(txtMinimumSize
											.getText()) * (Integer
											.parseInt(spinnerEnergyChannelTo
													.getText())
											- Integer
													.parseInt(spinnerEnergyChannelFrom
															.getText()) + 1)))));
			double calculateTotalTime = RegionStepsTimeEstimation
					.calculateTotalTime(Double.parseDouble(txtTime.getText()),
							Integer.parseInt(txtTotalSteps.getText()));
			txtTotalTime.setText(String.format("%.3f", calculateTotalTime));
		}
	}

	private void updateEnergyStep() {
		if (txtSize.getText().isEmpty()
				|| (Double.parseDouble(txtSize.getText()) < Double
						.parseDouble(txtMinimumSize.getText()))) {
			sweptStepSize = Double.parseDouble(txtMinimumSize.getText());
			txtSize.setText(String.format("%.3f", sweptStepSize));
			updateFeature(region,
					RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(),
					sweptStepSize);
		}
		if (btnSwept.getSelection()) {
			updateTotalSteps();
		}
	}

	private SelectionAdapter xRaySourceSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			onSelectEnergySource(source);
		}
	};

	protected void onSelectEnergySource(Object source) {
		if (source.equals(btnHard)) {
			this.xrayenergy = getDcmEnergy();
		} else if (source.equals(btnSoft)) {
			this.xrayenergy = getPgmEnergy();
		}
		if (xrayenergy != null) {
			try {
				excitationEnergy = (double) xrayenergy.getPosition();
			} catch (DeviceException e) {
				logger.error("Cannot set excitation energy", e);
			}
		}
	}

	@Override
	public void setFocus() {

	}

	public void setViewPartName(String viewPartName) {
		setPartName(viewPartName);
	}

	public void setRegionDefinitionResourceUtil(
			RegionDefinitionResourceUtil regionDefinition) {
		this.regionDefinitionResourceUtil = regionDefinition;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public void setDcmEnergy(ScannableMotor energy) {
		this.dcmenergy = energy;
	}

	public ScannableMotor getDcmEnergy() {
		return this.dcmenergy;
	}

	public void setPgmEnergy(ScannableMotor energy) {
		this.pgmenergy = energy;
	}

	public ScannableMotor getPgmEnergy() {
		return this.pgmenergy;
	}

	private void onModifyEnergyMode(Object source) {
		if (source.equals(btnKinetic) && btnKinetic.getSelection()) {
			updateEnergyFields();
			updateFeature(region,
					RegiondefinitionPackage.eINSTANCE.getRegion_EnergyMode(),
					ENERGY_MODE.KINETIC);
		} else if (source.equals(btnBinding) && btnBinding.getSelection()) {
			updateEnergyFields();
			updateFeature(region,
					RegiondefinitionPackage.eINSTANCE.getRegion_EnergyMode(),
					ENERGY_MODE.BINDING);
		}
	}

	private void updateEnergyFields() {
		double low = Double.parseDouble(txtLow.getText());
		double high = Double.parseDouble(txtHigh.getText());
		double center = Double.parseDouble(txtCenter.getText());
		txtLow.setText(String.format("%.4f", excitationEnergy - high));
		txtHigh.setText(String.format("%.4f", (excitationEnergy - low)));
		txtCenter.setText(String.format("%.4f", (excitationEnergy - center)));
		updateFeature(region,
				RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(),
				Double.parseDouble(txtLow.getText()));
		updateFeature(region,
				RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(),
				Double.parseDouble(txtHigh.getText()));
	}

	@Override
	public void dispose() {
		getViewSite()
				.getWorkbenchWindow()
				.getSelectionService()
				.removeSelectionListener(SequenceViewExtensionFactory.ID,
						selectionListener);
		super.dispose();
	}

	private void updateExcitationEnergy(Text txt) {
		excitationEnergy = Double.parseDouble(txt.getText());
		updateFeature(region,
				RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(),
				excitationEnergy);
	}

	private void initialiseRegionView(final Region region) {
		if (regionDefinitionResourceUtil.isSourceSelectable()) {
			if (region.getExcitationEnergy() > regionDefinitionResourceUtil
					.getXRaySourceEnergyLimit()) {
				btnHard.setSelection(true);
				btnSoft.setSelection(false);
				if (dcmenergy != null) {
					try {
						hardXRayEnergy = (double) dcmenergy.getPosition() * 1000; // eV
					} catch (DeviceException e) {
						logger.error("Cannot get X-ray energy from DCM.", e);
					}
				}
				excitationEnergy = hardXRayEnergy;
				txtHardEnergy.setText(String.format("%.4f", hardXRayEnergy));
				if (pgmenergy != null) {
					try {
						softXRayEnergy = (double) pgmenergy.getPosition();
					} catch (DeviceException e) {
						logger.error("Cannot get X-ray energy from PGM.", e);
					}
				}
				txtSoftEnergy.setText(String.format("%.4f", softXRayEnergy));
			} else {
				btnHard.setSelection(false);
				btnSoft.setSelection(true);
				if (dcmenergy != null) {
					try {
						hardXRayEnergy = (double) dcmenergy.getPosition() * 1000; // eV
					} catch (DeviceException e) {
						logger.error("Cannot get X-ray energy from DCM.", e);
					}
				}
				txtHardEnergy.setText(String.format("%.4f", hardXRayEnergy));
				if (pgmenergy != null) {
					try {
						softXRayEnergy = (double) pgmenergy.getPosition();
					} catch (DeviceException e) {
						logger.error("Cannot get X-ray energy from PGM.", e);
					}
				}
				excitationEnergy = softXRayEnergy;
				txtSoftEnergy.setText(String.format("%.4f", softXRayEnergy));
			}
		} else {
			if (dcmenergy != null) {
				try {
					hardXRayEnergy = (double) dcmenergy.getPosition();
				} catch (DeviceException e) {
					logger.error("Cannot get X-ray energy from DCM.", e);
				}
			}
			excitationEnergy = hardXRayEnergy;
			txtHardEnergy.setText(String.format("%.4f", hardXRayEnergy));
		}

		if (region.isEnabled()) {
			regionName.setText(region.getName());
			// regionName.setEnabled(true);
		} else {
			regionName.setText("");
			// regionName.setEnabled(false);
		}
		lensMode.setText(region.getLensMode());
		passEnergy.setText(String.valueOf(region.getPassEnergy()));
		txtMinimumSize.setText(String.format(
				"%.3f",
				camera.getEnergyResolution()
						* Integer.parseInt(passEnergy.getText())));
		runMode.setText(runMode.getItem(region.getRunMode().getMode()
				.getValue()));
		btnNumberOfIterations.setSelection(!region.getRunMode()
				.isRepeatUntilStopped());
		btnRepeatuntilStopped.setSelection(region.getRunMode()
				.isRepeatUntilStopped());
		btnConfirmAfterEachInteration.setSelection(region.getRunMode()
				.isConfirmAfterEachIteration());
		numberOfIterationSpinner.setSelection(region.getRunMode()
				.getNumIterations());
		btnSwept.setSelection(region.getAcquisitionMode().getLiteral()
				.equalsIgnoreCase("SWEPT"));
		btnFixed.setSelection(region.getAcquisitionMode().getLiteral()
				.equalsIgnoreCase("FIXED"));
		btnKinetic.setSelection(region.getEnergyMode().getLiteral()
				.equalsIgnoreCase("KINETIC"));
		btnBinding.setSelection(region.getEnergyMode().getLiteral()
				.equalsIgnoreCase("BINDING"));
		txtLow.setText(String.format("%.4f", region.getLowEnergy()));
		txtHigh.setText(String.format("%.4f", region.getHighEnergy()));
		txtCenter.setText(String.format("%.4f",
				(region.getLowEnergy() + region.getHighEnergy()) / 2));
		txtWidth.setText(String.format("%.4f",
				(region.getHighEnergy() - region.getLowEnergy())));
		txtTime.setText(String.format("%.3f", region.getStepTime()));
		long frames = Math.round(Double.parseDouble(txtTime.getText())
				/ Double.parseDouble(txtMinimumTime.getText()));
		spinnerFrames.setSelection((int) frames);
		spinnerEnergyChannelFrom.setSelection(region.getFirstXChannel());
		spinnerEnergyChannelTo.setSelection(region.getLastXChannel());
		spinnerYChannelFrom.setSelection(region.getFirstYChannel());
		spinnerYChannelTo.setSelection(region.getLastYChannel());
		spinnerSlices.setSelection(region.getSlices());
		btnADCMode.setSelection(region.getDetectorMode().getLiteral()
				.equalsIgnoreCase("ADC"));
		btnPulseMode.setSelection(region.getDetectorMode().getLiteral()
				.equalsIgnoreCase("PULSE_COUNTING"));
		if (btnSwept.getSelection()) {
			sweptStepSize = region.getEnergyStep();
			setToSweptMode();
		} else {
			setToFixedMode();
		}
		fireSelectionChanged(new TotalTimeSelection());
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		try {
			Region selectedRegionInSequenceView = getSelectedRegionInSequenceView();
			if (selectedRegionInSequenceView != null) {
				return new StructuredSelection(selectedRegionInSequenceView);
			} else {
				List<Region> regions = regionDefinitionResourceUtil
						.getRegions();
				if (!regions.isEmpty()) {
					for (Region region : regions) {
						if (region.isEnabled()) {
							return new StructuredSelection(region);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return StructuredSelection.EMPTY;
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {

	}

	private void fireSelectionChanged(Region region) {
		ISelection sel = StructuredSelection.EMPTY;
		if (region != null) {
			sel = new StructuredSelection(region);
		}
		fireSelectionChanged(sel);

	}

	private void fireSelectionChanged(ISelection sel) {
		SelectionChangedEvent event = new SelectionChangedEvent(this, sel);
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			listener.selectionChanged(event);
		}
	}

}
