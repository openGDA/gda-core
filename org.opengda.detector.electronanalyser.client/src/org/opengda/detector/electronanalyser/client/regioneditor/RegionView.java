package org.opengda.detector.electronanalyser.client.regioneditor;

import gda.device.DeviceException;
import gda.device.scannable.ScannableMotor;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.model.regiondefinition.util.RegionDefinitionResourceUtil;
/**
 * A Region Editor View for defining new or editing existing Region Definition for VG Scienta Electron Analyser.
 * @author fy65
 *
 */
public class RegionView extends ViewPart {
	public RegionView() {
		setTitleToolTip("Create a new or editing an existing region");
		setContentDescription("A view for editing region parameters");
		setPartName("Region Editor");
	}

	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private int framerate = 70;
	private double energyresolution = 0.0877;
	private int cameraXSize=1024;
	private int cameraYSize=1024;

	private Text txtMinimumSize;
	private Combo passEnergy;
	private Text txtTime;
	private Text txtMinimumTime;
	private Spinner spinnerFrames;
	private Spinner spinnerEnergyChannelTo;
	private Spinner spinnerYChannelTo;
	private Button btnHard;
	
	@Override
	public void createPartControl(Composite parent) {
		GridLayout gl_root = new GridLayout();
		gl_root.horizontalSpacing = 2;
		Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(gl_root);

		Group grpName = new Group(rootComposite, SWT.NONE);
		grpName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpName.setText("Name");
		grpName.setLayout(new FillLayout());

		Combo regionName = new Combo(grpName, SWT.NONE);
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
		// TODO replace the following values by sourcing it from detector
		lensMode.setItems(new String[] { "Transmission", "Angular45", "Angular60"});
		lensMode.setToolTipText("List of available modes to select");

		Group grpPassEnergy = new Group(bigComposite, SWT.NONE);
		grpPassEnergy.setLayout(new FillLayout());
		grpPassEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpPassEnergy.setText("Pass Energy");

		passEnergy = new Combo(grpPassEnergy, SWT.READ_ONLY);
		// TODO replace the following values by sourcing it from detector
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
		runMode.setItems(new String[] { "Normal", "Add Dimension"});
		runMode.setToolTipText("List of available run modes");
		runMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		new Label(grpRunMode, SWT.NONE);

		btnNumberOfIterations = new Button(grpRunMode, SWT.RADIO);
		btnNumberOfIterations.setText("Number of iterations");

		Spinner spinner = new Spinner(grpRunMode, SWT.BORDER);
		spinner.setMinimum(1);
		spinner.setToolTipText("Set number of iterations required");

		Button btnRepeatuntilStopped = new Button(grpRunMode, SWT.RADIO);
		btnRepeatuntilStopped.setText("Repeat until stopped");

		new Label(grpRunMode, SWT.NONE);

		Button btnConfirmAfterEachInteration = new Button(grpRunMode, SWT.CHECK);
		btnConfirmAfterEachInteration.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		btnConfirmAfterEachInteration.setText("Confirm after each iteration");
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
				Object source=e.getSource();
				onModifyEnergyMode(source);
			}
		});
		btnKinetic.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnKinetic.setText("Kinetic");

		btnBinding = new Button(grpEnergyMode, SWT.RADIO);
		btnBinding.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object source=e.getSource();
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

		Spinner spinnerEnergyChannelFrom = new Spinner(grpDetector, SWT.BORDER);
		spinnerEnergyChannelFrom.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		spinnerEnergyChannelFrom.setToolTipText("Low bound");
		spinnerEnergyChannelFrom.setMinimum(1);
		spinnerEnergyChannelFrom.setMaximum(getCameraXSize());
		
		Label lblEnergyChannelTo = new Label(grpDetector, SWT.NONE);
		lblEnergyChannelTo.setText("To");

		spinnerEnergyChannelTo = new Spinner(grpDetector, SWT.BORDER);
		spinnerEnergyChannelTo.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		spinnerEnergyChannelTo.setToolTipText("High bound");
		spinnerEnergyChannelTo.setMinimum(1);
		spinnerEnergyChannelTo.setMaximum(getCameraXSize());

		Label lblYChannel = new Label(grpDetector, SWT.NONE);
		lblYChannel.setText("Y Channels:");

		Label lblYChannelFrom = new Label(grpDetector, SWT.NONE);
		lblYChannelFrom.setText("From");

		Spinner spinnerYChannelFrom = new Spinner(grpDetector, SWT.BORDER);
		spinnerYChannelFrom
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerYChannelFrom.setToolTipText("Low bound");
		spinnerYChannelFrom.setMinimum(1);
		spinnerYChannelFrom.setMaximum(getCameraYSize());

		Label lblYChannelTo = new Label(grpDetector, SWT.NONE);
		lblYChannelTo.setText("To");

		spinnerYChannelTo = new Spinner(grpDetector, SWT.BORDER);
		spinnerYChannelTo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerYChannelTo.setToolTipText("High bound");
		spinnerYChannelTo.setMinimum(1);
		spinnerYChannelTo.setMaximum(getCameraYSize());

		Label lblSclies = new Label(grpDetector, SWT.NONE);
		lblSclies.setText("Slices:");
		new Label(grpDetector, SWT.NONE);

		spinnerSlices = new Spinner(grpDetector, SWT.BORDER);
		spinnerSlices.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		spinnerSlices.setToolTipText("Number of slices");
		spinnerSlices.setMinimum(1);

		new Label(grpDetector, SWT.NONE);
		new Label(grpDetector, SWT.NONE);

		Label lblDetectorMode = new Label(grpDetector, SWT.NONE);
		lblDetectorMode.setText("Mode:");

		new Label(grpDetector, SWT.NONE);

		btnADCMode = new Button(grpDetector, SWT.RADIO);
		btnADCMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnADCMode.setText("ADC");

		new Label(grpDetector, SWT.NONE);

		Button btnPulseMode = new Button(grpDetector, SWT.RADIO);
		btnPulseMode.setText("Pulse Counting");

		Group grpExcitationEnergy = new Group(rootComposite, SWT.NONE);
		grpExcitationEnergy.setText("Excitation Energy [eV]");
		grpExcitationEnergy
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		grpExcitationEnergy.setLayout(new GridLayout(3, true));

		Label lblXRaySource=new Label(grpExcitationEnergy, SWT.None);
		lblXRaySource.setText("X-Ray Source:");
		
		btnHard = new Button(grpExcitationEnergy, SWT.RADIO);
		btnHard.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnHard.setText("Hard");

		btnSoft = new Button(grpExcitationEnergy, SWT.RADIO);
		btnSoft.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSoft.setText("Soft");

		btnMoveMonochromator = new Button(grpExcitationEnergy, SWT.CHECK);
		btnMoveMonochromator.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));

		txtExcitationEnergy = new Text(grpExcitationEnergy, SWT.BORDER);
		txtExcitationEnergy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				txtExcitationEnergy.setText(String.format("%.4f",Double.parseDouble(txtExcitationEnergy.getText())));
			}
		});
		txtExcitationEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtExcitationEnergy.setToolTipText("Photon energy value");

		Button btnGetEnergy = new Button(grpExcitationEnergy, SWT.NONE);
		btnGetEnergy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//TODO fetch X-ray beam energy from beamline energy object
				try {
					txtExcitationEnergy.setText(xrayenergy.getPosition().toString());
				} catch (DeviceException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnGetEnergy.setText("Get Excitation Energy");
		
		initaliseValues();
	}

	private void initaliseValues() {
		lensMode.setText(lensMode.getItem(0));
		passEnergy.setText(passEnergy.getItem(1));
		runMode.setText(runMode.getItem(0));
		btnNumberOfIterations.setSelection(true);
		btnSwept.setSelection(true);
		btnKinetic.setSelection(true);
		txtLow.setText(String.format("%.4f",8.0));
		txtHigh.setText(String.format("%.4f", 10.0));
		txtCenter.setText(String.format("%.4f", (Double.parseDouble(txtLow.getText())+Double.parseDouble(txtHigh.getText()))/2));
		txtWidth.setText(String.format("%.4f", (Double.parseDouble(txtHigh.getText())-Double.parseDouble(txtLow.getText()))));
		txtFramesPerSecond.setText(String.format("%d",getCameraFrameRate()));
		txtMinimumTime.setText(String.format("%f",1.0 / getCameraFrameRate()));
		txtMinimumSize.setText(String.format("%.3f",getCameraEnergyResolution()
					* Integer.parseInt(passEnergy.getText())));
		spinnerFrames.setMinimum(1);
		spinnerFrames.setMaximum(1000);
		spinnerFrames.setSelection(3);
		txtTime.setText(String.format("%.3f",Double.parseDouble(txtMinimumTime.getText())
				* Integer.parseInt(spinnerFrames.getText())));
		txtSize.setText(String.format("%.3f", 200.0));
		txtTotalSteps.setText(String.format("%d",0));
		txtTotalTime.setText(String.format("%.3f", Double.parseDouble(txtTime.getText())*Double.parseDouble(txtTotalSteps.getText())));
		spinnerEnergyChannelTo.setSelection(getCameraXSize());
		spinnerYChannelTo.setSelection(getCameraYSize());
		spinnerSlices.setSelection(1);
		btnADCMode.setSelection(true);
		btnMoveMonochromator.setText("Move Mono");
		btnMoveMonochromator.setSelection(false);
		btnHard.setSelection(true);
		txtExcitationEnergy.setText(String.format("%.4f", 0.0));
		// add listener after initialisation otherwise return 'empty String'
		passEnergy.addSelectionListener(passEnerySelectionAdapter);
		passEnergy.addModifyListener(passEnergyModifyListener);
		btnSwept.addSelectionListener(sweptSelectionListener);
		btnFixed.addSelectionListener(fixedSelectionListener);
		txtLow.addSelectionListener(energySelectionListener);
		txtHigh.addSelectionListener(energySelectionListener);
		txtCenter.addSelectionListener(energySelectionListener);
		spinnerFrames.addSelectionListener(framesSelectionListener);
		//spinnerFrames.addModifyListener(framesModifyListener);
		txtTime.addSelectionListener(timeSelectionListener);
		txtSize.addSelectionListener(sizeSelectionListener);
		txtMinimumSize.addModifyListener(minimumSizeModifyListener);
		txtMinimumSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnHard.addSelectionListener(xRaySourceSelectionListener);
		btnSoft.addSelectionListener(xRaySourceSelectionListener);

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
			txtMinimumSize.setText(String.format("%.3f",getCameraEnergyResolution()
					* Integer.parseInt(passEnergy.getText())));
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
			txtTime.setText(String.format("%.3f",Double.parseDouble(txtMinimumTime.getText())
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
			if (txtSize.getText().isEmpty() || (Double.parseDouble(txtSize.getText()) < Double.parseDouble(txtMinimumSize.getText()))) {
				txtSize.setText(txtMinimumSize.getText());
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
			if (Double.parseDouble(txtSize.getText()) < Double.parseDouble(txtMinimumSize.getText())) {
				txtSize.setText(txtMinimumSize.getText());
			} else {
				txtSize.setText(String.format("%.3f", Double.parseDouble(txtSize.getText())));
			}
		}		
	}
	private SelectionAdapter timeSelectionListener = new SelectionAdapter() {
		public void widgetDefaultSelected(SelectionEvent e) {
			Object source = e.getSource();
			onModifyTime(source);
		}
	};
	protected void onModifyTime(Object source) {
		if (source.equals(txtTime) && !txtTime.getText().isEmpty()) {
			long frames=Math.round(Double.parseDouble(txtTime.getText())/Double.parseDouble(txtMinimumTime.getText()));
			spinnerFrames.setSelection((int) frames);
			txtTime.setText(String.format("%.3f",Double.parseDouble(txtTime.getText())));
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
		} else if ( source.equals(txtHigh) && txtHigh.isFocusControl()) {
			updateEnergyFields(txtHigh);
		} else if (source.equals(txtCenter) && txtCenter.isFocusControl()) {
			double low=Double.parseDouble(txtCenter.getText())-Double.parseDouble(txtWidth.getText())/2;
			txtLow.setText(String.format("%.4f",low));
			double high=Double.parseDouble(txtCenter.getText())+Double.parseDouble(txtWidth.getText())/2;
			txtHigh.setText(String.format("%.4f",high));
			txtCenter.setText(String.format("%.4f",Double.parseDouble(txtCenter.getText())));
		}
	}

	private void updateEnergyFields(Text txt) {
		if (Double.parseDouble(txtLow.getText()) > Double.parseDouble(txtHigh.getText())) {
			String low=txtHigh.getText();
			txtHigh.setText(String.format("%.4f",Double.parseDouble(txtLow.getText())));
			txtLow.setText(String.format("%.4f",Double.parseDouble(low)));
		} else {
			txt.setText(String.format("%.4f",Double.parseDouble(txt.getText())));
		}
		double center=(Double.parseDouble(txtLow.getText())+Double.parseDouble(txtHigh.getText()))/2;
		txtCenter.setText(String.format("%.4f",center));
		double width=Double.parseDouble(txtHigh.getText())-Double.parseDouble(txtLow.getText());
		txtWidth.setText(String.format("%.4f",width));
	}
	private Text txtCenter;
	private Text txtWidth;
	SelectionAdapter fixedSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			txtLow.setEditable(false);
			txtHigh.setEditable(false);
			txtSize.setEditable(false);
			txtTotalSteps.setText("1");
			txtSize.setText(txtMinimumSize.getText());
			txtTotalTime.setText(String.format("%.3f",Integer.parseInt(txtTotalSteps.getText())*Double.parseDouble(txtTime.getText())));
		}
	};
	private Text txtTotalSteps;
	private Text txtTotalTime;
	private SelectionAdapter sweptSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			txtLow.setEditable(true);
			txtHigh.setEditable(true);
			txtSize.setEditable(true);
			//TODO implement total steps calculation algorithm.
			int totalsteps=100;
			txtTotalSteps.setText(String.format("%d",totalsteps));
			txtTotalTime.setText(String.format("%.3f",totalsteps*Double.parseDouble(txtTime.getText())));
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
	private Text txtExcitationEnergy;
	private ScannableMotor xrayenergy;
	private Button btnMoveMonochromator;

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
	protected void onSelectEnergySource(Object source) {
		if (source.equals(btnHard)) {
			btnMoveMonochromator.setText("Move Mono");
			this.xrayenergy=getDcmEnergy();
		} else if (source.equals(btnSoft)) {
			btnMoveMonochromator.setText("Move PGM");
			this.xrayenergy=getPgmEnergy();
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
		this.dcmenergy=energy;
	}
	public ScannableMotor getDcmEnergy() {
		return this.dcmenergy;
	}
	public void setPgmEnergy(ScannableMotor energy) {
		this.pgmenergy=energy;
	}
	public ScannableMotor getPgmEnergy() {
		return this.pgmenergy;
	}

	private void onModifyEnergyMode(Object source) {
		if (source.equals(btnKinetic) && btnKinetic.isFocusControl()) {
			txtLow.setText(String.format("%.4f",(Double.parseDouble(txtExcitationEnergy.getText())-Double.parseDouble(txtLow.getText()))));
			txtHigh.setText(String.format("%.4f",(Double.parseDouble(txtExcitationEnergy.getText())-Double.parseDouble(txtHigh.getText()))));
			txtCenter.setText(String.format("%.4f",(Double.parseDouble(txtExcitationEnergy.getText())-Double.parseDouble(txtCenter.getText()))));
		} else if (source.equals(btnBinding) && btnBinding.isFocusControl()) {
			txtLow.setText(String.format("%.4f",(Double.parseDouble(txtExcitationEnergy.getText())-Double.parseDouble(txtLow.getText()))));
			txtHigh.setText(String.format("%.4f",(Double.parseDouble(txtExcitationEnergy.getText())-Double.parseDouble(txtHigh.getText()))));
			txtCenter.setText(String.format("%.4f",(Double.parseDouble(txtExcitationEnergy.getText())-Double.parseDouble(txtCenter.getText()))));
		}
	}

}
