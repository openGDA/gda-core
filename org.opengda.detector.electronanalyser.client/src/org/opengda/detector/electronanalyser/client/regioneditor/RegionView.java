package org.opengda.detector.electronanalyser.client.regioneditor;

import java.util.Collections;
import java.util.List;

import gda.device.DeviceException;
import gda.device.scannable.ScannableMotor;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
import org.opengda.detector.electronanalyser.client.RegionDefinitionResourceUtil;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.LENS_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.PASS_ENERGY;
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
	private int framerate = 70;
	private double energyresolution = 0.0877;
	private int cameraXSize = 1024;
	private int cameraYSize = 1024;

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

		regionName = new Combo(grpName, SWT.NONE);
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
		// TODO replace the following values by sourcing it from detector at
		// initialisation
		lensMode.setItems(new String[] { "Transmission", "Angular45",
				"Angular60" });
		lensMode.setToolTipText("List of available modes to select");

		Group grpPassEnergy = new Group(bigComposite, SWT.NONE);
		grpPassEnergy.setLayout(new FillLayout());
		grpPassEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpPassEnergy.setText("Pass Energy");

		passEnergy = new Combo(grpPassEnergy, SWT.READ_ONLY);
		// TODO replace the following values by sourcing it from detector at
		// initialisation
		passEnergy.setItems(new String[] { "5", "10", "50", "75", "100", "200",
				"500" });
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
					updateFeature(RegiondefinitionPackage.eINSTANCE
							.getRegion_EnergyMode(), btnKinetic.getSelection());
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
					updateFeature(RegiondefinitionPackage.eINSTANCE
							.getRegion_EnergyMode(), btnBinding.getSelection());
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
					updateFeature(RegiondefinitionPackage.eINSTANCE
							.getRegion_FirstXChannel(),
							spinnerEnergyChannelFrom.getSelection());
				}
			}
		});
		spinnerEnergyChannelFrom.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		spinnerEnergyChannelFrom.setToolTipText("Low bound");
		spinnerEnergyChannelFrom.setMinimum(1);
		spinnerEnergyChannelFrom.setMaximum(getCameraXSize());

		Label lblEnergyChannelTo = new Label(grpDetector, SWT.NONE);
		lblEnergyChannelTo.setText("To");

		spinnerEnergyChannelTo = new Spinner(grpDetector, SWT.BORDER);
		spinnerEnergyChannelTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerEnergyChannelTo)) {
					updateFeature(RegiondefinitionPackage.eINSTANCE
							.getRegion_LastXChannel(), spinnerEnergyChannelTo
							.getSelection());
				}
			}
		});
		spinnerEnergyChannelTo.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		spinnerEnergyChannelTo.setToolTipText("High bound");
		spinnerEnergyChannelTo.setMinimum(1);
		spinnerEnergyChannelTo.setMaximum(getCameraXSize());

		Label lblYChannel = new Label(grpDetector, SWT.NONE);
		lblYChannel.setText("Y Channels:");

		Label lblYChannelFrom = new Label(grpDetector, SWT.NONE);
		lblYChannelFrom.setText("From");

		spinnerYChannelFrom = new Spinner(grpDetector, SWT.BORDER);
		spinnerYChannelFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerYChannelFrom)) {
					updateFeature(RegiondefinitionPackage.eINSTANCE
							.getRegion_FirstYChannel(), spinnerYChannelFrom
							.getSelection());
				}
			}
		});
		spinnerYChannelFrom
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerYChannelFrom.setToolTipText("Low bound");
		spinnerYChannelFrom.setMinimum(1);
		spinnerYChannelFrom.setMaximum(getCameraYSize());

		Label lblYChannelTo = new Label(grpDetector, SWT.NONE);
		lblYChannelTo.setText("To");

		spinnerYChannelTo = new Spinner(grpDetector, SWT.BORDER);
		spinnerYChannelTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerYChannelTo)) {
					updateFeature(RegiondefinitionPackage.eINSTANCE
							.getRegion_LastYChannel(), spinnerYChannelTo
							.getSelection());
				}
			}
		});
		spinnerYChannelTo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerYChannelTo.setToolTipText("High bound");
		spinnerYChannelTo.setMinimum(1);
		spinnerYChannelTo.setMaximum(getCameraYSize());

		Label lblSclies = new Label(grpDetector, SWT.NONE);
		lblSclies.setText("Slices:");
		new Label(grpDetector, SWT.NONE);

		spinnerSlices = new Spinner(grpDetector, SWT.BORDER);
		spinnerSlices.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource().equals(spinnerSlices)) {
					updateFeature(RegiondefinitionPackage.eINSTANCE
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
					updateFeature(RegiondefinitionPackage.eINSTANCE
							.getRegion_DetectorMode(), btnADCMode
							.getSelection());
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
					updateFeature(RegiondefinitionPackage.eINSTANCE
							.getRegion_DetectorMode(), btnPulseMode
							.getSelection());
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
						updateFeature(RegiondefinitionPackage.eINSTANCE
								.getRegion_ExcitationEnergy(), txtHardEnergy
								.getText());
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
						updateFeature(RegiondefinitionPackage.eINSTANCE
								.getRegion_ExcitationEnergy(), txtSoftEnergy
								.getText());
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
					region = (Region) firstElement;
					initialiseViewWithRegionData(region);
				}
			}
		}
	};

	@SuppressWarnings("unchecked")
	private void initaliseValues() {
		lensMode.setText(lensMode.getItem(0));
		passEnergy.setText(passEnergy.getItem(1));
		runMode.setText(runMode.getItem(0));
		btnNumberOfIterations.setSelection(true);
		btnSwept.setSelection(true);
		btnKinetic.setSelection(true);
		txtLow.setText(String.format("%.4f", 8.0));
		txtHigh.setText(String.format("%.4f", 10.0));
		txtCenter.setText(String.format("%.4f", (Double.parseDouble(txtLow
				.getText()) + Double.parseDouble(txtHigh.getText())) / 2));
		txtWidth.setText(String.format("%.4f", (Double.parseDouble(txtHigh
				.getText()) - Double.parseDouble(txtLow.getText()))));
		txtFramesPerSecond.setText(String.format("%d", getCameraFrameRate()));
		txtMinimumTime.setText(String.format("%f", 1.0 / getCameraFrameRate()));
		txtMinimumSize.setText(String.format(
				"%.3f",
				getCameraEnergyResolution()
						* Integer.parseInt(passEnergy.getText())));
		spinnerFrames.setMinimum(1);
		spinnerFrames.setMaximum(1000);
		spinnerFrames.setSelection(3);
		txtTime.setText(String.format(
				"%.3f",
				Double.parseDouble(txtMinimumTime.getText())
						* Integer.parseInt(spinnerFrames.getText())));
		txtSize.setText(String.format("%.3f", 200.0));
		txtTotalSteps.setText(String.format("%d", 0));
		txtTotalTime.setText(String.format(
				"%.3f",
				Double.parseDouble(txtTime.getText())
						* Double.parseDouble(txtTotalSteps.getText())));
		spinnerEnergyChannelTo.setSelection(getCameraXSize());
		spinnerYChannelTo.setSelection(getCameraYSize());
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
			txtHardEnergy.setText(String.format("%.4f", hardXRayEnergy));
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
		txtSize.addModifyListener(sizeModifyListener);
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

		// populate txtRegionName combo with active (enabled) region names.
		List<Region> regions = Collections.emptyList();
		try {
			regions = regionDefinitionResourceUtil.getRegions(false);
		} catch (Exception e1) {
			logger.error("Cannot get regions from resource: ", e1);
		}
		if (regions.isEmpty()) {
			logger.debug("Sequence is empty. create new sequence in the resource");
		} else {
			for (Region region : regions) {
				if (region.isEnabled()) {
					regionName.add(region.getName());
				}
			}
		}
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
	private ModifyListener sizeModifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			Object source = e.getSource();
			if (source.equals(txtSize) && !txtSize.isFocusControl()) {
				// TODO set to EPICS Size PV in order to get updated total
				// steps value
				// only update if txtSize is changed by others, not itself.
				updateFeature(
						RegiondefinitionPackage.eINSTANCE.getRegion_StepTime(),
						txtSize.getText());
				updateTotalSteps();
			}
		}
	};
	private ModifyListener timeModifiedListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			Object source = e.getSource();
			if (source.equals(txtTime) && !txtTime.isFocusControl()) {
				updateTotalTime();
				updateFeature(
						RegiondefinitionPackage.eINSTANCE.getRegion_StepTime(),
						txtTime.getText());
			}
		}
	};

	// Update features when it changes in Region Editor
	private void updateFeature(Object feature, Object value) {
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
				updateFeature(
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
				updateFeature(
						RegiondefinitionPackage.eINSTANCE.getRegion_LensMode(),
						LENS_MODE.getByName(lensMode.getText()));
			}
		}
	};
	private SelectionAdapter runModeSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(runMode)) {
				updateFeature(
						RegiondefinitionPackage.eINSTANCE.getRegion_RunMode(),
						runMode.getText());
			}
		}
	};
	SelectionAdapter btnNumberOfIterationSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnNumberOfIterations)) {
				updateFeature(
						RegiondefinitionPackage.eINSTANCE
								.getRunMode_NumIterationOption(),
						btnNumberOfIterations.getSelection());
			}
		}
	};
	private SelectionAdapter repeatUntilStoopedSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnRepeatuntilStopped)) {
				updateFeature(
						RegiondefinitionPackage.eINSTANCE
								.getRunMode_RepeatUntilStopped(),
						btnRepeatuntilStopped.getSelection());
			}
		}
	};
	SelectionAdapter confirmAfterEachIterationSelAdaptor = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnConfirmAfterEachInteration)) {
				updateFeature(
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
				updateFeature(
						RegiondefinitionPackage.eINSTANCE
								.getRunMode_NumIterations(),
						numberOfIterationSpinner.getSelection());
			}
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(numberOfIterationSpinner)) {
				updateFeature(
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
					regionName.setText(region.getName());
					lensMode.setText(region.getLensMode().getLiteral());
					passEnergy.setText(region.getPassEnergy().getLiteral());
					runMode.setText(region.getRunMode().getMode().getLiteral());
					btnNumberOfIterations.setSelection(!region.getRunMode()
							.isRepeatUntilStopped());
					btnRepeatuntilStopped.setSelection(region.getRunMode()
							.isRepeatUntilStopped());
					btnConfirmAfterEachInteration.setSelection(region
							.getRunMode().isConfirmAfterEachInteration());
					numberOfIterationSpinner.setSelection(region.getRunMode()
							.getNumIterations());
					btnSwept.setSelection(region.getAcquisitionMode()
							.getLiteral().equalsIgnoreCase("SWEPT"));
					btnFixed.setSelection(region.getAcquisitionMode()
							.getLiteral().equalsIgnoreCase("FIXED"));
					btnKinetic.setSelection(region.getEnergyMode().getLiteral()
							.equalsIgnoreCase("KINETIC"));
					btnBinding.setSelection(region.getEnergyMode().getLiteral()
							.equalsIgnoreCase("BINDING"));
					txtLow.setText(String.format("%.4f", region.getLowEnergy()));
					txtHigh.setText(String.format("%.4f",
							region.getHighEnergy()));
					txtCenter.setText(String.format("%.4f",
							region.getFixEnergy()));
					txtLow.setText(String.format("%.4f", region.getLowEnergy()));
					txtTime.setText(String.format("%.3f", region.getStepTime()));
					txtSize.setText(String.format("%.3f",
							region.getEnergyStep()));
					spinnerEnergyChannelFrom.setSelection(region
							.getFirstXChannel());
					spinnerEnergyChannelTo.setSelection(region
							.getLastXChannel());
					spinnerYChannelFrom.setSelection(region.getFirstYChannel());
					spinnerYChannelTo.setSelection(region.getLastYChannel());
					spinnerSlices.setSelection(region.getSlices());
					btnADCMode.setSelection(region.getDetectorMode()
							.getLiteral().equalsIgnoreCase("ADC"));
					btnPulseMode.setSelection(region.getDetectorMode()
							.getLiteral().equalsIgnoreCase("PULSE_COUNTING"));
				}
			});

		}
	}

	private ModifyListener passEnergyModifyListener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			Object source = e.getSource();
			onModifyPassEnergy(source);
		}
	};

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
					getCameraEnergyResolution() * passEnergyIntValue));
			updateFeature(
					RegiondefinitionPackage.eINSTANCE.getRegion_PassEnergy(),
					PASS_ENERGY.getByName(passEnergyFromCombo));
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
				txtSize.setText(txtMinimumSize.getText());
				updateFeature(
						RegiondefinitionPackage.eINSTANCE
								.getRegion_EnergyStep(),
						txtSize.getText());
			}
		}
	}

	private SelectionAdapter sizeSelectionListener = new SelectionAdapter() {
		public void widgetDefaultSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifySize(source);
		}
	};

	protected void onModifySize(Object source) {
		if (source.equals(txtSize)) {
			if (Double.parseDouble(txtSize.getText()) < Double
					.parseDouble(txtMinimumSize.getText())) {
				txtSize.setText(txtMinimumSize.getText());
			} else {
				txtSize.setText(String.format("%.3f",
						Double.parseDouble(txtSize.getText())));
			}
			updateFeature(
					RegiondefinitionPackage.eINSTANCE.getRegion_EnergyStep(),
					txtSize.getText());
			// set Total steps
			// TODO set to EPICS size PV to get total size update
			updateTotalSteps();
		}
	}

	private void updateTotalTime() {
		txtTotalTime.setText(String.format(
				"%.3f",
				Double.parseDouble(txtTime.getText())
						* Integer.parseInt(txtTotalSteps.getText())));
		updateFeature(RegiondefinitionPackage.eINSTANCE.getRegion_TotalTime(),
				txtTotalTime.getText());
	}

	private void updateTotalSteps() {
		// get number of steps required for the scan
		long M = (long) Math.ceil(Double.parseDouble(txtWidth.getText()) * 1000
				/ Double.parseDouble(txtSize.getText()));
		// calculate image overlapping number per data point
		long N = (long) (Math
				.ceil((Double.parseDouble(txtMinimumSize.getText()) * getCameraXSize())
						/ Double.parseDouble(txtSize.getText())));
		txtTotalSteps.setText(String.format("%d", M + N));
		updateFeature(RegiondefinitionPackage.eINSTANCE.getRegion_TotalSteps(),
				txtTotalSteps.getText());
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
			updateFeature(
					RegiondefinitionPackage.eINSTANCE.getRegion_StepTime(),
					txtTime.getText());
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
			updateFeature(
					RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(),
					txtLow.getText());
			updateFeature(
					RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(),
					txtHigh.getText());
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
		updateFeature(RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(),
				txtLow.getText());
		updateFeature(RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(),
				txtHigh.getText());

		double center = (Double.parseDouble(txtLow.getText()) + Double
				.parseDouble(txtHigh.getText())) / 2;
		txtCenter.setText(String.format("%.4f", center));
		double width = Double.parseDouble(txtHigh.getText())
				- Double.parseDouble(txtLow.getText());
		txtWidth.setText(String.format("%.4f", width));
	}

	private Text txtCenter;
	private Text txtWidth;
	SelectionAdapter fixedSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnFixed)) {
				txtLow.setEditable(false);
				txtHigh.setEditable(false);
				txtSize.setEditable(false);
				txtTotalSteps.setText("1");
				txtSize.setText(txtMinimumSize.getText());
				txtTotalTime.setText(String.format(
						"%.3f",
						Integer.parseInt(txtTotalSteps.getText())
								* Double.parseDouble(txtTime.getText())));
				updateFeature(
						RegiondefinitionPackage.eINSTANCE
								.getRegion_AcquisitionMode(),
						btnFixed.getSelection());
			}
		}
	};
	private Text txtTotalSteps;
	private Text txtTotalTime;
	private SelectionAdapter sweptSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(btnSwept)) {
				txtLow.setEditable(true);
				txtHigh.setEditable(true);
				txtSize.setEditable(true);
				updateTotalSteps();
				updateFeature(
						RegiondefinitionPackage.eINSTANCE
								.getRegion_AcquisitionMode(),
						btnSwept.getSelection());
			}

		}
	};
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
	private Combo regionName;
	private Spinner numberOfIterationSpinner;
	private Button btnRepeatuntilStopped;
	private Button btnConfirmAfterEachInteration;
	private Spinner spinnerEnergyChannelFrom;
	private Spinner spinnerYChannelFrom;
	private Button btnPulseMode;
	private Text txtSoftEnergy;
	private double excitationEnergy = 0.0;
	private double hardXRayEnergy = 0.0; // keV
	private double softXRayEnergy = 0.0; // eV

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

	public void setCameraFrameRate(int rate) {
		if (rate < 1) {
			throw new IllegalArgumentException(
					"Camera frame rate must be great than and equal to 1.");
		}
		this.framerate = rate;
	}

	public int getCameraFrameRate() {
		return this.framerate;
	}

	public void setCameraEnergyResolution(double resolution) {
		this.energyresolution = resolution;
	}

	public double getCameraEnergyResolution() {
		return this.energyresolution;
	}

	public int getCameraXSize() {
		return cameraXSize;
	}

	public void setCameraXSize(int detecterXSize) {
		this.cameraXSize = detecterXSize;
	}

	public int getCameraYSize() {
		return cameraYSize;
	}

	public void setCameraYSize(int detecterYSize) {
		this.cameraYSize = detecterYSize;
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
		if (source.equals(btnKinetic) && btnKinetic.isFocusControl()) {
			updateEnergyFields();
		} else if (source.equals(btnBinding) && btnBinding.isFocusControl()) {
			updateEnergyFields();
		}
	}

	private void updateEnergyFields() {
		txtLow.setText(String.format("%.4f",
				(excitationEnergy - Double.parseDouble(txtLow.getText()))));
		txtHigh.setText(String.format("%.4f",
				(excitationEnergy - Double.parseDouble(txtHigh.getText()))));
		txtCenter.setText(String.format("%.4f",
				(excitationEnergy - Double.parseDouble(txtCenter.getText()))));
		updateFeature(RegiondefinitionPackage.eINSTANCE.getRegion_LowEnergy(),
				txtLow.getText());
		updateFeature(RegiondefinitionPackage.eINSTANCE.getRegion_HighEnergy(),
				txtHigh.getText());
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
}
