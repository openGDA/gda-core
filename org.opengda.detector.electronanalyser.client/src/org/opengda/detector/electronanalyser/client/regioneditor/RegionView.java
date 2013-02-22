package org.opengda.detector.electronanalyser.client.regioneditor;

import gda.device.DeviceException;
import gda.device.scannable.ScannableMotor;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.RegionDefinitionResourceUtil;
import org.opengda.detector.electronanalyser.client.RegionStepsTimeEstimation;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.DETECTOR_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.ENERGY_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RUN_MODES;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.RegiondefinitionPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Region Editor View for defining new or editing existing Region Definition
 * for VG Scienta Electron Analyser.
 * 
 * @author fy65
 * 
 */
public class RegionView extends ViewPart {
	private static final Logger logger = LoggerFactory
			.getLogger(RegionView.class);

	public RegionView() {
		setTitleToolTip("Editing a selected region parameters");
		// setContentDescription("A view for editing region parameters");
		setPartName("Region Editor");
	}

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

	@Override
	public void createPartControl(Composite parent) {

		final ScrolledComposite sc2 = new ScrolledComposite(parent,
				SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		sc2.setExpandHorizontal(true);
		sc2.setExpandVertical(true);

		Composite rootComposite = new Composite(sc2, SWT.NONE);
		sc2.setContent(rootComposite);
		GridLayout gl_root = new GridLayout();
		gl_root.horizontalSpacing = 2;
		rootComposite.setLayout(gl_root);

		Group grpName = new Group(rootComposite, SWT.NONE);
		grpName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpName.setText("Name");
		grpName.setLayout(new FillLayout());

		regionName = new Text(grpName, SWT.NONE);
		regionName.setToolTipText("List of available active regions to select");

		Composite bigComposite = new Composite(rootComposite, SWT.None);
		// Contains Lens model, pass energy, run mode, acquisition mode, and
		// energy mode.
		bigComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		bigComposite.setLayout(new GridLayout(2, false));

		Group grpLensMode = new Group(bigComposite, SWT.NONE);
		grpLensMode.setText("Lens Mode");
		grpLensMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpLensMode.setLayout(new FillLayout());

		lensMode = new Combo(grpLensMode, SWT.READ_ONLY);
		lensMode.setToolTipText("List of available modes to select");

		Group grpPassEnergy = new Group(bigComposite, SWT.NONE);
		grpPassEnergy.setLayout(new FillLayout());
		grpPassEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpPassEnergy.setText("Pass Energy");

		passEnergy = new Combo(grpPassEnergy, SWT.READ_ONLY);
		passEnergy.setToolTipText("List opf available pass energy to select");

		Group grpRunMode = new Group(bigComposite, SWT.NONE);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.widthHint = 300;
		;
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

		Group grpAcquisitionMode = new Group(bigComposite, SWT.NONE);
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

		Group grpEnergyMode = new Group(bigComposite, SWT.NONE);
		grpEnergyMode.setText("Energy Mode");
		grpEnergyMode.setLayout(new GridLayout());
		grpEnergyMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		btnKinetic = new Button(grpEnergyMode, SWT.RADIO);
		btnKinetic.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object source = e.getSource();
				onModifyEnergyMode(source);
				if (source.equals(btnKinetic)) {
					updateFeature(region, RegiondefinitionPackage.eINSTANCE
							.getRegion_EnergyMode(), ENERGY_MODE.KINETIC);
				}

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
				if (source.equals(btnBinding)) {
					updateFeature(region, RegiondefinitionPackage.eINSTANCE
							.getRegion_EnergyMode(), ENERGY_MODE.BINDING);
				}
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
		txtLow.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtLow.setToolTipText("start energy");

		Label lblCenter = new Label(grpEnergy, SWT.NONE);
		lblCenter.setText("Center");

		txtCenter = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		txtCenter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtCenter.setToolTipText("Center/Fixed energy");

		Label lblHigh = new Label(grpEnergy, SWT.NONE);
		lblHigh.setText("High");

		txtHigh = new Text(grpEnergy, SWT.BORDER | SWT.SINGLE);
		txtHigh.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtHigh.setToolTipText("Stop enenery");

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

		Label lblFramesPerSecond = new Label(grpStep, SWT.NONE);
		lblFramesPerSecond.setText("Frames/s");

		txtFramesPerSecond = new Text(grpStep, SWT.BORDER);
		txtFramesPerSecond
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtFramesPerSecond.setToolTipText("Camera frame rate");
		txtFramesPerSecond.setEditable(false);

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
		txtTotalSteps.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				Object source = e.getSource();
				if (source.equals(txtTotalSteps)
						&& !txtTotalSteps.isFocusControl()) {
					updateTotalTime();
				}
			}
		});
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
					if (e.getSource().equals(btnHard)) {
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
					if (e.getSource().equals(btnSoft)) {
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

		sc2.setMinSize(rootComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		initaliseValues();

		Object service = getSite().getService(ISelectionService.class);
		if (service instanceof ISelectionService) {
			ISelectionService selService = (ISelectionService) service;
			selService.addSelectionListener(selectionListener);
		}
	}

	private ISelectionListener selectionListener = new ISelectionListener() {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part != RegionView.this
					&& selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				Object firstElement = sel.getFirstElement();
				if (firstElement instanceof Region) {
					if (!firstElement.equals(region)) {
						region = (Region) firstElement;
						initialiseViewWithRegionData(region);
					}
				}
			}
		}
	};

	private void initaliseValues() {
		// TODO replace the following values by sourcing it from detector at
		// initialisation
		lensMode.setItems(new String[] { "Transmission", "Angular45",
				"Angular60" });
		// TODO replace the following values by sourcing it from detector at
		// initialisation
		passEnergy.setItems(new String[] { "5", "10", "50", "75", "100", "200",
				"500" });

		List<Region> regions = Collections.emptyList();
		try {
			regions = regionDefinitionResourceUtil.getRegions(false);
		} catch (Exception e1) {
			logger.error("Cannot get regions from resource: ", e1);
		}
		txtFramesPerSecond.setText(String.format("%d", camera.getFrameRate()));
		txtMinimumTime
				.setText(String.format("%f", 1.0 / camera.getFrameRate()));
		spinnerFrames.setMinimum(1);
		spinnerFrames.setMaximum(1000);

		if (regions.isEmpty()) {
			lensMode.setText(lensMode.getItem(0));
			passEnergy.setText(passEnergy.getItem(1));
			runMode.setText(runMode.getItem(0));
			btnNumberOfIterations.setSelection(true);
			btnSwept.setSelection(true);
			btnKinetic.setSelection(true);
			kineticSelected = true;
			bindingSelected = false;
			txtLow.setText(String.format("%.4f", 8.0));
			txtHigh.setText(String.format("%.4f", 10.0));
			txtCenter.setText(String.format("%.4f", (Double.parseDouble(txtLow
					.getText()) + Double.parseDouble(txtHigh.getText())) / 2));
			txtWidth.setText(String.format("%.4f", (Double.parseDouble(txtHigh
					.getText()) - Double.parseDouble(txtLow.getText()))));
			txtMinimumSize.setText(String.format(
					"%.3f",
					camera.getEnergyResolution()
							* Integer.parseInt(passEnergy.getText())));
			spinnerFrames.setSelection(3);
			txtTime.setText(String.format(
					"%.3f",
					Double.parseDouble(txtMinimumTime.getText())
							* Integer.parseInt(spinnerFrames.getText())));
			txtSize.setText(String.format("%.3f", 200.0));
			txtTotalSteps
					.setText(String.format("%d", RegionStepsTimeEstimation
							.calculateTotalSteps(Double.parseDouble(txtWidth
									.getText()), Double.parseDouble(txtSize
									.getText()),
									(Double.parseDouble(txtMinimumSize
											.getText()) * camera
											.getCameraXSize()))));
			txtTotalTime.setText(String.format(
					"%.3f",
					RegionStepsTimeEstimation.calculateTotalTime(
							Double.parseDouble(txtTime.getText()),
							Integer.parseInt(txtTotalSteps.getText()))));
			// txtTotalSteps.setText(String.format("%d", 0));
			// txtTotalTime.setText(String.format(
			// "%.3f",
			// Double.parseDouble(txtTime.getText())
			// * Double.parseDouble(txtTotalSteps.getText())));
			spinnerEnergyChannelTo.setSelection(camera.getCameraXSize());
			spinnerYChannelTo.setSelection(camera.getCameraYSize());
			spinnerSlices.setSelection(1);
			btnADCMode.setSelection(true);
			if (regionDefinitionResourceUtil.isSourceSelectable()) {
				btnHard.setSelection(true);
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
		} else {
			txtMinimumSize.setText(String.format("%.3f",
					camera.getEnergyResolution()
							* regions.get(0).getPassEnergy()));
			initialiseRegionView(regions.get(0));
		}

		// add listener after initialisation otherwise return 'empty String'
		passEnergy.addSelectionListener(passEnerySelectionAdapter);
		passEnergy.addModifyListener(passEnergyModifyListener);
		btnSwept.addSelectionListener(sweptSelectionListener);
		btnFixed.addSelectionListener(fixedSelectionListener);
		txtLow.addSelectionListener(energySelectionListener);
		txtHigh.addSelectionListener(energySelectionListener);
		txtCenter.addSelectionListener(energySelectionListener);
		txtWidth.addModifyListener(widthModifyListener);
		spinnerFrames.addSelectionListener(framesSelectionListener);
		// spinnerFrames.addModifyListener(framesModifyListener);
		txtTime.addSelectionListener(timeSelectionListener);
		txtTime.addModifyListener(timeModifiedListener);
		txtSize.addSelectionListener(sizeSelectionListener);
		txtMinimumSize.addModifyListener(minimumSizeModifyListener);
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
		try {
			editingDomain = regionDefinitionResourceUtil.getEditingDomain();
		} catch (Exception e1) {
			logger.error("Cannot get Editing Domain object.", e1);
		}

	}

	private ModifyListener widthModifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			updateTotalSteps();
		}
	};
	private ModifyListener timeModifiedListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			Object source = e.getSource();
			if (source.equals(txtTime) && !txtTime.isFocusControl()) {
				long frames = Math.round(Double.parseDouble(txtTime.getText())
						/ Double.parseDouble(txtMinimumTime.getText()));
				spinnerFrames.setSelection((int) frames);
				updateTotalTime();
				updateFeature(region,
						RegiondefinitionPackage.eINSTANCE.getRegion_StepTime(),
						Double.parseDouble(txtTime.getText()));
			}
		}
	};

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
				updateFeature(region,
						RegiondefinitionPackage.eINSTANCE.getRegion_Name(),
						regionName.getText());
			}
		}

		// on selection from list
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(regionName)) {
				// TODO update all other fields in region view
			}

		}
	};
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
								.getRunMode_ConfirmAfterEachInteration(),
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
	private ModifyListener passEnergyModifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
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
		}
	}

	private ModifyListener minimumSizeModifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			Object source = e.getSource();
			onModifyMinimumSize(source);
		}
	};
	private Text txtSize;

	protected void onModifyMinimumSize(Object source) {
		if (source.equals(txtMinimumSize)) {
			if (txtSize.getText().isEmpty()
					|| (Double.parseDouble(txtSize.getText()) < Double
							.parseDouble(txtMinimumSize.getText()))) {
				sweptStepSize = Double.parseDouble(txtMinimumSize.getText());
				txtSize.setText(String.format("%.3f", sweptStepSize));
				updateFeature(region,
						RegiondefinitionPackage.eINSTANCE
								.getRegion_EnergyStep(), sweptStepSize);
				if (btnSwept.getSelection()) {
					updateTotalSteps();
				}
			} else {
				if (btnSwept.getSelection()) {
					updateTotalSteps();
				}
			}
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
		txtTotalTime.setText(String.format(
				"%.3f",
				RegionStepsTimeEstimation.calculateTotalTime(
						Double.parseDouble(txtTime.getText()),
						Integer.parseInt(txtTotalSteps.getText()))));
		updateFeature(region,
				RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(),
				Double.parseDouble(txtTotalTime.getText()));
	}

	private void updateTotalSteps() {
		txtTotalSteps.setText(String.format("%d", RegionStepsTimeEstimation
				.calculateTotalSteps(Double.parseDouble(txtWidth.getText()),
						Double.parseDouble(txtSize.getText()), (Double
								.parseDouble(txtMinimumSize.getText()) * camera
								.getCameraXSize()))));
		updateFeature(region,
				RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(),
				Integer.parseInt(txtTotalSteps.getText()));
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

	private Text txtLow;
	private Text txtHigh;

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
			// update domain features
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
		// update domain features
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
	}

	private double sweptStepSize;

	private Text txtCenter;
	private Text txtWidth;
	SelectionAdapter fixedSelectionListener = new SelectionAdapter() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnFixed)) {
				setToFixedMode();
				txtTotalTime.setText(String.format(
						"%.3f",
						RegionStepsTimeEstimation.calculateTotalTime(
								Double.parseDouble(txtTime.getText()),
								Integer.parseInt(txtTotalSteps.getText()))));
				updateFeature(region,
						RegiondefinitionPackage.eINSTANCE
								.getRegion_AcquisitionMode(),
						ACQUISITION_MODE.FIXED);
				updateFeature(region,
						RegiondefinitionPackage.eINSTANCE
								.getRegion_EnergyStep(), fixedEnergyRange());
				updateFeature(region,
						RegiondefinitionPackage.eINSTANCE
								.getRegion_TotalSteps(),
						Integer.parseInt(txtTotalSteps.getText()));
				updateFeature(
						region,
						RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(),
						Double.parseDouble(txtTotalTime.getText()));
			}
		}
	};

	private void setToFixedMode() {
		txtLow.setEditable(false);
		txtHigh.setEditable(false);
		txtSize.setEditable(false);
		txtSize.setText(String.format("%.3f", fixedEnergyRange()));
		txtTotalSteps.setText("1");
	}

	private double fixedEnergyRange() {
		return Double.parseDouble(txtMinimumSize.getText())
				* (Integer.parseInt(spinnerEnergyChannelTo.getText())
						- Integer.parseInt(spinnerEnergyChannelFrom.getText()) + 1);
	}

	private Text txtTotalSteps;
	private Text txtTotalTime;
	private SelectionAdapter sweptSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnSwept)) {
				setToSweptMode();
				updateFeature(region,
						RegiondefinitionPackage.eINSTANCE
								.getRegion_EnergyStep(), sweptStepSize);

				updateTotalSteps();
				updateFeature(region,
						RegiondefinitionPackage.eINSTANCE
								.getRegion_AcquisitionMode(),
						ACQUISITION_MODE.SWEPT);
			}
		}

	};

	private void setToSweptMode() {
		txtLow.setEditable(true);
		txtHigh.setEditable(true);
		txtSize.setEditable(true);
		// restore the original energy step size for the SWEPT
		txtSize.setText(String.format("%.3f", sweptStepSize));
	}

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

	private SelectionAdapter xRaySourceSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			onSelectEnergySource(source);
		}
	};
	private Button btnSoft;
	private ScannableMotor dcmenergy;
	private ScannableMotor pgmenergy;
	private Button btnFixed;
	private Button btnBinding;
	private Text regionName;
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
	private boolean kineticSelected;
	private boolean bindingSelected;

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
		if (source.equals(btnKinetic) && !kineticSelected) {
			updateEnergyFields();
			kineticSelected = true;
			bindingSelected = false;
		} else if (source.equals(btnBinding) && !bindingSelected) {
			updateEnergyFields();
			kineticSelected = false;
			bindingSelected = true;
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
				RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(), high);
	}

	@Override
	public void dispose() {
		Object service = getSite().getService(ISelectionService.class);
		if (service instanceof ISelectionService) {
			ISelectionService selService = (ISelectionService) service;
			selService.removeSelectionListener(selectionListener);
		}
		super.dispose();
	}

	private void updateExcitationEnergy(Text txt) {
		excitationEnergy = Double.parseDouble(txt.getText());
		updateFeature(region,
				RegiondefinitionPackage.eINSTANCE.getRegion_ExcitationEnergy(),
				excitationEnergy);
	}

	private void initialiseRegionView(final Region region) {
		regionName.setText(region.getName());
		lensMode.setText(region.getLensMode());
		passEnergy.setText(String.valueOf(region.getPassEnergy()));
		runMode.setText(runMode.getItem(region.getRunMode().getMode()
				.getValue()));
		btnNumberOfIterations.setSelection(region.getRunMode()
				.isNumIterationOption());
		btnRepeatuntilStopped.setSelection(region.getRunMode()
				.isRepeatUntilStopped());
		btnConfirmAfterEachInteration.setSelection(region.getRunMode()
				.isConfirmAfterEachInteration());
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
		if (btnKinetic.getSelection()) {
			kineticSelected = true;
			bindingSelected = false;
		}
		if (btnBinding.getSelection()) {
			kineticSelected = false;
			bindingSelected = true;
		}
		txtLow.setText(String.format("%.4f", region.getLowEnergy()));
		txtHigh.setText(String.format("%.4f", region.getHighEnergy()));
		txtCenter.setText(String.format("%.4f",
				(region.getLowEnergy() + region.getHighEnergy()) / 2));
		txtWidth.setText(String.format("%.4f",
				(region.getHighEnergy() - region.getLowEnergy())));
		txtTime.setText(String.format("%.3f", region.getStepTime()));
		if (btnSwept.getSelection()) {
			sweptStepSize = region.getEnergyStep();
			setToSweptMode();
			updateTotalSteps();
		} else {
			setToFixedMode();
		}
		spinnerEnergyChannelFrom.setSelection(region.getFirstXChannel());
		spinnerEnergyChannelTo.setSelection(region.getLastXChannel());
		spinnerYChannelFrom.setSelection(region.getFirstYChannel());
		spinnerYChannelTo.setSelection(region.getLastYChannel());
		spinnerSlices.setSelection(region.getSlices());
		btnADCMode.setSelection(region.getDetectorMode().getLiteral()
				.equalsIgnoreCase("ADC"));
		btnPulseMode.setSelection(region.getDetectorMode().getLiteral()
				.equalsIgnoreCase("PULSE_COUNTING"));
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
	}
}
