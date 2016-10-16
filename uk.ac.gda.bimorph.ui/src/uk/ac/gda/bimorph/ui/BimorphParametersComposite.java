/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.bimorph.ui;

import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;

public final class BimorphParametersComposite extends Composite {
	private FieldComposite slitSizeScannable;
	private FieldComposite slitPosScannable;
	private FieldComposite otherSlitSizeScannable;
	private FieldComposite otherSlitPosScannable;
	private FieldComposite detectorName;
	private ScaleBox exposureTime;
	private FieldComposite scanNumberInputs;
	private FieldComposite errorFile;
	private FieldComposite bimorphScannableName;
	private FieldComposite bimorphVoltages;
	private FieldComposite bimorphGroups;
	private BooleanWrapper btnCalculateErrorFile;
	private BooleanWrapper btnAutoOffset;
	private BooleanWrapper btnAutoDist;
	private Button btnRunOptimisation;
	private Button btnRunSlitScan;
	private Group beamOffsetGroup;
	private Group scanDataGroup;
	private Group grpNewMirrorSettings;
	private Group grpPresentMirrorSettings;
	private ComboWrapper mirrorType;
	private ComboWrapper iSign;
	private ComboWrapper inv;
	private ComboWrapper method;
	private Composite composite;
	private ScaleBox voltageIncrement;
	private ScaleBox slitSize;
	private ScaleBox slitStart;
	private ScaleBox slitEnd;
	private ScaleBox slitStep;
	private ScaleBox otherSlitPos;
	private ScaleBox otherSlitSize;
	private ScaleBox settleTime;
	private ScaleBox beamOffset;
	private ScaleBox focusSize;
	private ScaleBox presentSourceMirrorDistance;
	private ScaleBox presentMirrorFocusDistance;
	private ScaleBox presentAngleOfIncidence;
	private ScaleBox newSourceMirrorDistance;
	private ScaleBox newMirrorFocusDistance;
	private ScaleBox newAngleOfIncidence;
	private ScaleBox pixelSize;
	private ScaleBox detectorDistance;
	private ScaleBox presentDetDist;
	private ScaleBox slitScanDetDist;
	private ScaleBox minSlitPos;
	private ScaleBox maxSlitPos;
	private Label lblFocusSize;
	private Label lblScannableName;
	private Label lblBimorphVoltages;
	private Label lblScanNumbers;
	private Label lblErrorFile;
	private Label lblMirrorType;
	private Label lblName;
	private Label lblOffset;
	private Label lblISign;
	private Label lblInv;
	private Label lblMethod;
	private Label lblPresentSourcemirrorDistance;
	private Label lblPresentMirrorfocusDistance;
	private Label lblAngleOfIncidence;
	private Label lblNewSourcemirrorDistance;
	private Label lblNewMirrorfocusDistance;
	private Label lblNewAngleOf;
	private Label label;
	private Label lblPresentDetDist;
	private Label lblSlitScanDetDist;
	private Label lblMinSlitPos;
	private Label lblMaxSlitPos;
	private String user_offset;
	private int noOfElectrodes;
	private Label label_1;
	private Label lblElectrodeGroups;
	private BooleanWrapper btnGroupElectrodesTogether;
	private Label scanDirectory;
	private String selectedDir;
	
	public BimorphParametersComposite(Composite parent, int style) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout(1, false));
		createMirrorGroup();
		createSlitsGroup();
		createOtherSlitsGroup();
		createDetectorGroup();
		createEllipseGroup();
		createPresentMirrorSettingsGroup();
		createNewMirrorSettingsGroup();
		createReturnVoltagesCheckbox();
		createScanDataGroup();
		createBeamGroup();
		createButtons();
	}

	public void runSlitScanScript() {
		String mirror = bimorphScannableName.getValue().toString();
		String increment = voltageIncrement.getValue().toString();
		String slitToScanSize = slitSizeScannable.getValue().toString();
		String slitToScanPos = slitPosScannable.getValue().toString();
		String slitSizeValue = slitSize.getValue().toString();
		String otherSlitSizeScannableName = otherSlitSizeScannable.getValue().toString();
		String otherSlitPosScannableName = otherSlitPosScannable.getValue().toString();
		String slitStartValue = slitStart.getValue().toString();
		String slitEndValue = slitEnd.getValue().toString();
		String slitStepValue = slitStep.getValue().toString();
		String detector = detectorName.getValue().toString();
		String exposure = exposureTime.getValue().toString();
		String settleTimeValue = settleTime.getValue().toString();
		String otherSlitSizeValue = otherSlitSize.getValue().toString();
		String otherSlitPosValue = otherSlitPos.getValue().toString();
		String autoOptimise = "False";
		String groups_string = bimorphGroups.getValue().toString();
		String grouped = "None";
	
		if(btnGroupElectrodesTogether.getValue())
			grouped = "True";

		String command = "slitscanner.run(globals(),\"" + mirror + "\"," + increment + ",\"" + slitToScanSize + "\",\""
		+ slitToScanPos + "\"," + slitSizeValue + ",\"" + otherSlitSizeScannableName + "\",\""
		+ otherSlitPosScannableName + "\"," + slitStartValue + "," + slitEndValue + "," + slitStepValue
		+ ",\"" + detector + "\"," + exposure + "," + settleTimeValue + "," + otherSlitSizeValue + "," + otherSlitPosValue + ","
		+ autoOptimise + "," + grouped + ",\"" + groups_string + "\")";
		
		JythonServerFacade.getInstance().runCommand(command);

		String elec = JythonServerFacade.getInstance().evaluateCommand(mirror + ".numOfChans");
		noOfElectrodes = Integer.parseInt(elec);

		int currentScanNumber = determineCurrentScanFileNumber();
		errorFile.setValue(currentScanNumber);
		String scanNumbers = "";
		for (int i = currentScanNumber; i <= noOfElectrodes + currentScanNumber; i++) {
			scanNumbers += i;
			if (i != noOfElectrodes + currentScanNumber)
				scanNumbers += ",";
		}
		scanNumberInputs.setValue(scanNumbers);
	}

	public void calculateErrorFile() {
		double pixel_size = (Double) pixelSize.getValue();
		double p_1 = (Double) presentSourceMirrorDistance.getValue();
		double q_1 = (Double) presentMirrorFocusDistance.getValue();
		double theta_1 = (Double) presentAngleOfIncidence.getValue()/1000.0;
		double p_2 = (Double) newSourceMirrorDistance.getValue();
		double q_2 = (Double) newMirrorFocusDistance.getValue();
		double theta_2 = (Double) newAngleOfIncidence.getValue()/1000.0;
		double detector_distance = (Double) detectorDistance.getValue();
		double slit_start = (Double) slitStart.getValue();
		double slit_end = (Double) slitEnd.getValue();
		double slit_step = (Double) slitStep.getValue();

		double i_sign = 1;
		if (iSign.getValue().equals("upstream"))
			i_sign = -1;

		String mirror_type = mirrorType.getItem(mirrorType.getSelectionIndex());
		String column = "peak2d_peaky";
		if (mirror_type.equals("hfm"))
			column = "peak2d_peakx";

		double invVal = 1;
		if (inv.getValue().equals("-1"))
			invVal = -1;
		double methodVal = 0;
		if (method.getValue().equals("1"))
			methodVal = 1;
		else if (method.getValue().equals("2"))
			methodVal = 2;
		if (method.getValue().equals("2"))
			methodVal = 2;
		
		String command = "el = ellipse.EllipseCalculator(" + pixel_size + "," + p_1 + "," + q_1 + "," + theta_1 + ","
				+ p_2 + "," + q_2 + "," + theta_2 + "," + i_sign + "," + detector_distance + "," + slit_start + ","
				+ slit_end + "," + slit_step + ",\"" + column + "\"," + invVal + "," + methodVal + ")";

		JythonServerFacade.getInstance().print(command);
		JythonServerFacade.getInstance().runCommand("import ellipse");
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		JythonServerFacade.getInstance().runCommand(command);
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		JythonServerFacade.getInstance().runCommand("el.calcSlopes()");
	}

	public void runOptimiseScript() {
		String mirror_type = mirrorType.getItem(mirrorType.getSelectionIndex());
		String voltageInc = voltageIncrement.getValue().toString();
		String error_file = errorFile.getValue().toString();
		String bm_voltages = "[" + bimorphVoltages.getValue().toString() + "]";
		String beamOffsetValue = beamOffset.getValue().toString();
		String bimorphScannable = bimorphScannableName.getValue().toString();
		String desiredFocSize = focusSize.getValue().toString();
		String mirror = bimorphScannableName.getValue().toString();
		String files = scanNumberInputs.getValue().toString();
		double presentDetDistVal = Double.parseDouble(presentDetDist.getValue().toString());
		double slitScanDetDistVal = Double.parseDouble(slitScanDetDist.getValue().toString());
		
		// assuming these two lines are validating the positions
		Double.parseDouble(minSlitPos.getValue().toString());
		Double.parseDouble(maxSlitPos.getValue().toString());
		
		boolean autoDistVal = btnAutoDist.getValue();

		if(voltageInc.equals(""))
			System.out.println("Please enter a correct voltage increment");
		if(error_file.equals(""))
			System.out.println("Please enter a correct error file");
		if(bm_voltages.equals(""))
			System.out.println("Please enter correct voltages");
		if(beamOffsetValue.equals(""))
			System.out.println("Please enter a correct beam offset");
		if(bimorphScannable.equals(""))
			System.out.println("Please enter a correct bimorph scannable");
		if(desiredFocSize.equals(""))
			System.out.println("Please enter a correct desired focus size");
		if(mirror.equals(""))
			System.out.println("Please enter a correct mirror");
		if(files.equals(""))
			System.out.println("Please enter correct scan numbers");
		if(presentDetDistVal==0)
			System.out.println("Please enter a correct present detector distance");
		if(slitScanDetDistVal==0)
			System.out.println("Please enter a correct slit scan detector distance");
		if(minSlitPos.getValue().toString().equals(""))
			System.out.println("Please enter a correct min slit pos val");
		if(maxSlitPos.getValue().toString().equals(""))
			System.out.println("Please enter a correct max slit pos val");
		
		String autoDist = "False";
		if(autoDistVal)
			autoDist="True";
		String scalingFactor="None";
		if(presentDetDistVal!=0 && slitScanDetDistVal!=0)
			scalingFactor = String.valueOf(presentDetDistVal/slitScanDetDistVal);
		if (bimorphScannable.equals(""))
			bimorphScannable = "None";
		if (files.contains(":")) {
			int start = Integer.parseInt(files.substring(0, files.indexOf(":")));
			int end = Integer.parseInt(files.substring(files.indexOf(":") + 1));
			files = "[";
			for (int i = 0; i < end - start; i++)
				files = files.concat(String.valueOf(start + i) + ",");
			files = files.concat(String.valueOf(end));
			files = files.concat("]");
		} 
		else
			files = "[" + scanNumberInputs.getValue().toString() + "]";

		String elec = JythonServerFacade.getInstance().evaluateCommand(mirror + ".numOfChans");

		noOfElectrodes = Integer.parseInt(elec);

		if (btnCalculateErrorFile.getValue()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			calculateErrorFile();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			errorFile.setValue(determineCurrentScanFileNumber());
			error_file = errorFile.getValue().toString();
		}

		if(selectedDir==null)
			selectedDir="None";
		
		String slitToScanPos = slitPosScannable.getValue().toString();
		
		String command = "bimorph.runOptimisation(" + bimorphScannable + "," + "\"" + mirror_type + "\"" + ","
				+ String.valueOf(noOfElectrodes) + "," + voltageInc + "," + files + "," + error_file + ","
				+ desiredFocSize + "," + "\"" + user_offset + "\"" + "," + bm_voltages + "," + beamOffsetValue + "," + autoDist + "," + scalingFactor  +  ",'" + selectedDir + "'," + minSlitPos.getValue().toString() + "," +  maxSlitPos.getValue().toString() + ",'" + slitToScanPos + "')";

		JythonServerFacade.getInstance().runCommand(command);
	}

	private int determineCurrentScanFileNumber(){
		int currentScanNumber = InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint().getScanIdentifier();
		return currentScanNumber;
	}
	
	public void setEllipseEnabled(boolean enabled) {
		lblISign.setEnabled(enabled);
		lblPresentSourcemirrorDistance.setEnabled(enabled);
		lblPresentMirrorfocusDistance.setEnabled(enabled);
		lblAngleOfIncidence.setEnabled(enabled);
		lblNewSourcemirrorDistance.setEnabled(enabled);
		lblNewMirrorfocusDistance.setEnabled(enabled);
		lblNewAngleOf.setEnabled(enabled);
		presentSourceMirrorDistance.setEnabled(enabled);
		presentMirrorFocusDistance.setEnabled(enabled);
		presentAngleOfIncidence.setEnabled(enabled);
		newSourceMirrorDistance.setEnabled(enabled);
		newMirrorFocusDistance.setEnabled(enabled);
		newAngleOfIncidence.setEnabled(enabled);
		iSign.setEnabled(enabled);
		pixelSize.setEnabled(enabled);
		detectorDistance.setEnabled(enabled);
	}

	public void createSlitsGroup() {
		Group slitsGroup = new Group(this, SWT.NONE);
		GridData gd_slitsGroup = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_slitsGroup.widthHint = 391;
		slitsGroup.setLayoutData(gd_slitsGroup);
		slitsGroup.setLayout(new GridLayout(6, false));
		slitsGroup.setText("Slits");
		label = new Label(slitsGroup, SWT.NONE);
		label.setText("Size Scannable");
		label = new Label(slitsGroup, SWT.NONE);
		label.setText("Size");
		label = new Label(slitsGroup, SWT.NONE);
		label.setText("Pos Scannable");
		label = new Label(slitsGroup, SWT.NONE);
		label.setText("Start");
		label = new Label(slitsGroup, SWT.NONE);
		label.setText("End");
		label = new Label(slitsGroup, SWT.NONE);
		label.setText("Step");
		slitSizeScannable = new TextWrapper(slitsGroup, SWT.BORDER);
		slitSizeScannable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		slitSize = new ScaleBox(slitsGroup, SWT.NONE);
		slitSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(slitSize, SWT.NONE);
		new Label(slitSize, SWT.NONE);
		slitPosScannable = new TextWrapper(slitsGroup, SWT.BORDER);
		slitPosScannable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		slitStart = new ScaleBox(slitsGroup, SWT.NONE);
		slitStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		slitStart.setMaximum(1000.0);
		slitStart.setMinimum(-1000.0);
		new Label(slitStart, SWT.NONE);
		new Label(slitStart, SWT.NONE);
		slitEnd = new ScaleBox(slitsGroup, SWT.NONE);
		slitEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		slitEnd.setMaximum(1000.0);
		slitEnd.setMinimum(-1000.0);
		new Label(slitEnd, SWT.NONE);
		new Label(slitEnd, SWT.NONE);
		slitStep = new ScaleBox(slitsGroup, SWT.NONE);
		slitStep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		slitStep.setMaximum(1000.0);
		slitStep.setMinimum(-1000.0);
		new Label(slitStep, SWT.NONE);
		new Label(slitStep, SWT.NONE);
	}

	public void createOtherSlitsGroup() {
		Group otherSlitsGroup = new Group(this, SWT.NONE);
		GridData gd_otherSlitsGroup = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_otherSlitsGroup.widthHint = 306;
		otherSlitsGroup.setLayoutData(gd_otherSlitsGroup);
		otherSlitsGroup.setLayout(new GridLayout(4, false));
		otherSlitsGroup.setText("Other Slits");
		label = new Label(otherSlitsGroup, SWT.NONE);
		label.setText("Size Scannable");
		label = new Label(otherSlitsGroup, SWT.NONE);
		label.setText("Size");
		label = new Label(otherSlitsGroup, SWT.NONE);
		label.setText("Pos Scannable");
		label = new Label(otherSlitsGroup, SWT.NONE);
		label.setText("Pos");
		otherSlitSizeScannable = new TextWrapper(otherSlitsGroup, SWT.BORDER);
		otherSlitSizeScannable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		otherSlitSize = new ScaleBox(otherSlitsGroup, SWT.NONE);
		otherSlitSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(otherSlitSize, SWT.NONE);
		new Label(otherSlitSize, SWT.NONE);
		otherSlitPosScannable = new TextWrapper(otherSlitsGroup, SWT.BORDER);
		otherSlitPosScannable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		otherSlitPos = new ScaleBox(otherSlitsGroup, SWT.NONE);
		otherSlitPos.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		otherSlitPos.setMaximum(1000.0);
		otherSlitPos.setMinimum(-1000.0);
		new Label(otherSlitPos, SWT.NONE);
		new Label(otherSlitPos, SWT.NONE);
	}

	public void createDetectorGroup() {
		Group detectorGroup = new Group(this, SWT.NONE);
		GridData gd_detectorGroup = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_detectorGroup.widthHint = 497;
		detectorGroup.setLayoutData(gd_detectorGroup);
		detectorGroup.setLayout(new GridLayout(4, false));
		detectorGroup.setText("Detector");
		lblName = new Label(detectorGroup, SWT.NONE);
		lblName.setText("Name     ");
		label = new Label(detectorGroup, SWT.NONE);
		label.setText("Exposure (Camera Units)");

		Label lblPixelSize = new Label(detectorGroup, SWT.NONE);
		lblPixelSize.setText("Pixel Size (μm)");

		Label lblDetectorDistance = new Label(detectorGroup, SWT.NONE);
		lblDetectorDistance.setText("detector distance (m)");

		detectorName = new TextWrapper(detectorGroup, SWT.BORDER);
		GridData gd_detectorName = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_detectorName.widthHint = 76;
		detectorName.setLayoutData(gd_detectorName);
		exposureTime = new ScaleBox(detectorGroup, SWT.NONE);
		exposureTime.setMaximum(99999);
		exposureTime.setMinimum(-99999);
		exposureTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(exposureTime, SWT.NONE);
		new Label(exposureTime, SWT.NONE);
		pixelSize = new ScaleBox(detectorGroup, SWT.NONE);
		pixelSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		pixelSize.setDecimalPlaces(8);
		new Label(pixelSize, SWT.NONE);
		new Label(pixelSize, SWT.NONE);
		detectorDistance = new ScaleBox(detectorGroup, SWT.NONE);
		detectorDistance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		detectorDistance.setDecimalPlaces(8);
		detectorDistance.setMaximum(Double.MAX_VALUE);
		new Label(detectorDistance, SWT.NONE);
		new Label(detectorDistance, SWT.NONE);
	}

	public void createMirrorGroup() {

		Group mirrorGroup = new Group(this, SWT.NONE);
		GridData gd_mirrorGroup = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_mirrorGroup.widthHint = 610;
		mirrorGroup.setLayoutData(gd_mirrorGroup);
		mirrorGroup.setLayout(new GridLayout(4, false));
		mirrorGroup.setText("Mirror");

		lblScannableName = new Label(mirrorGroup, SWT.NONE);
		lblScannableName.setText("Scannable Name");
		lblMirrorType = new Label(mirrorGroup, SWT.NONE);
		lblMirrorType.setText("Orientation");
		label = new Label(mirrorGroup, SWT.NONE);
		label.setText("Voltage Increment (V)");
		label = new Label(mirrorGroup, SWT.NONE);
		label.setText("Settle (Seconds)");

		bimorphScannableName = new TextWrapper(mirrorGroup, SWT.BORDER);
		bimorphScannableName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		mirrorType = new ComboWrapper(mirrorGroup, SWT.NONE);
		mirrorType.setItems(new String[] { "hfm", "vfm" });
		GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_combo.widthHint = 70;
		mirrorType.setLayoutData(gd_combo);
		voltageIncrement = new ScaleBox(mirrorGroup, SWT.NONE);
		((GridData) voltageIncrement.getControl().getLayoutData()).widthHint = 149;
		voltageIncrement.setMaximum(1000.0);
		voltageIncrement.setMinimum(-1000.0);
		voltageIncrement.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(voltageIncrement, SWT.NONE);
		settleTime = new ScaleBox(mirrorGroup, SWT.NONE);
		((GridData) settleTime.getControl().getLayoutData()).widthHint = 127;
		settleTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(settleTime, SWT.NONE);
		new Label(settleTime, SWT.NONE);

		lblBimorphVoltages = new Label(mirrorGroup, SWT.NONE);
		lblBimorphVoltages.setText("Current Voltages");

		bimorphVoltages = new TextWrapper(mirrorGroup, SWT.BORDER);
		GridData gd_bimorphVoltages = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd_bimorphVoltages.widthHint = 440;
		bimorphVoltages.setLayoutData(gd_bimorphVoltages);
		
		btnGroupElectrodesTogether = new BooleanWrapper(mirrorGroup, SWT.CHECK);
		btnGroupElectrodesTogether.setText("Group Electrodes?");
		
		lblElectrodeGroups = new Label(mirrorGroup, SWT.NONE);
		lblElectrodeGroups.setText("Electrode Groups");
		
		bimorphGroups = new TextWrapper(mirrorGroup, SWT.BORDER);
		GridData gd_bimorphGroups = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd_bimorphGroups.widthHint = 299;
		bimorphGroups.setLayoutData(gd_bimorphGroups);
	}

	public void createEllipseGroup() {
		btnRunSlitScan = new Button(this, SWT.NONE);
		btnRunSlitScan.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnRunSlitScan.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runSlitScanScript();
			}
		});
		btnRunSlitScan.setText("   Run Slit Scan   ");

		label_1 = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_label_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_label_1.widthHint = 601;
		label_1.setLayoutData(gd_label_1);
		Group grpEllipseCalculation = new Group(this, SWT.NONE);
		grpEllipseCalculation.setText("Ellipse Calculation");
		grpEllipseCalculation.setLayout(new GridLayout(1, false));

		Composite comp1 = new Composite(grpEllipseCalculation, SWT.NONE);
		comp1.setLayout(new GridLayout(3, false));
		
		btnCalculateErrorFile = new BooleanWrapper(comp1, SWT.CHECK);
		btnCalculateErrorFile.setText("Calculate Error File    ");

		lblISign = new Label(comp1, SWT.NONE);
		lblISign.setText("Slit Scan Starts");
		iSign = new ComboWrapper(comp1, SWT.NONE);
		iSign.setItems(new String[] { "upstream", "downstream" });

		btnCalculateErrorFile.addValueListener(new ValueListener() {

			@Override
			public String getValueListenerName() {
				return null;
			}

			@Override
			public void valueChangePerformed(ValueEvent e) {
				boolean calcEllipse = btnCalculateErrorFile.getValue();
				setEllipseEnabled(calcEllipse);
			}
		});
		
		Composite comp2 = new Composite(grpEllipseCalculation, SWT.NONE);
		comp2.setLayout(new GridLayout(4, false));
		
		lblInv = new Label(comp2, SWT.NONE);
		lblInv.setText("Inv");
		inv = new ComboWrapper(comp2, SWT.NONE);
		inv.setItems(new String[] { "1", "-1" });
		lblMethod = new Label(comp2, SWT.NONE);
		lblMethod.setText("Method");
		method = new ComboWrapper(comp2, SWT.NONE);
		method.setItems(new String[] { "0", "1", "2" });
	}

	public void createPresentMirrorSettingsGroup() {
		grpPresentMirrorSettings = new Group(this, SWT.NONE);
		grpPresentMirrorSettings.setText("Present Mirror Settings");
		grpPresentMirrorSettings.setLayout(new GridLayout(1, false));
		
		Composite comp = new Composite(grpPresentMirrorSettings, SWT.NONE);
		comp.setLayout(new GridLayout(3, false));
		
		lblPresentSourcemirrorDistance = new Label(comp, SWT.NONE);
		lblPresentSourcemirrorDistance.setText("Source-Mirror Distance");

		lblPresentMirrorfocusDistance = new Label(comp, SWT.NONE);
		lblPresentMirrorfocusDistance.setText("Mirror-Focus Distance");

		lblAngleOfIncidence = new Label(comp, SWT.NONE);
		lblAngleOfIncidence.setText("Angle Of Incidence (mrad)");

		presentSourceMirrorDistance = new ScaleBox(comp, SWT.NONE);
		presentSourceMirrorDistance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		presentSourceMirrorDistance.setDecimalPlaces(8);
		new Label(presentSourceMirrorDistance, SWT.NONE);
		new Label(presentSourceMirrorDistance, SWT.NONE);
		presentMirrorFocusDistance = new ScaleBox(comp, SWT.NONE);
		presentMirrorFocusDistance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		presentMirrorFocusDistance.setDecimalPlaces(8);
		new Label(presentMirrorFocusDistance, SWT.NONE);
		new Label(presentMirrorFocusDistance, SWT.NONE);
		presentAngleOfIncidence = new ScaleBox(comp, SWT.NONE);
		presentAngleOfIncidence.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		presentAngleOfIncidence.setDecimalPlaces(8);
		new Label(presentAngleOfIncidence, SWT.NONE);
		new Label(presentAngleOfIncidence, SWT.NONE);
		
		btnAutoDist = new BooleanWrapper(grpPresentMirrorSettings, SWT.CHECK);
		btnAutoDist.setText("Present detector distance <> slit scan detector distance?");
		
		Composite comp2 = new Composite(grpPresentMirrorSettings, SWT.NONE);
		comp2.setLayout(new GridLayout(2, false));
		
		lblPresentDetDist = new Label(comp2, SWT.NONE);
		lblPresentDetDist.setText("Present Detector Distance");
		lblSlitScanDetDist = new Label(comp2, SWT.NONE);
		lblSlitScanDetDist.setText("Slit Scan Detector Distance");
		
		
		presentDetDist = new ScaleBox(comp2, SWT.NONE);
		presentDetDist.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		presentDetDist.setDecimalPlaces(8);
		new Label(presentDetDist, SWT.NONE);
		new Label(presentDetDist, SWT.NONE);
		
		slitScanDetDist = new ScaleBox(comp2, SWT.NONE);
		slitScanDetDist.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		slitScanDetDist.setDecimalPlaces(8);
		new Label(slitScanDetDist, SWT.NONE);
		new Label(slitScanDetDist, SWT.NONE);

		btnAutoDist.addValueListener(new ValueListener() {

			@Override
			public String getValueListenerName() {
				return null;
			}

			@Override
			public void valueChangePerformed(ValueEvent e) {
				lblPresentDetDist.setEnabled(btnAutoDist.getValue());
				lblSlitScanDetDist.setEnabled(btnAutoDist.getValue());
				presentDetDist.setEnabled(btnAutoDist.getValue());
				slitScanDetDist.setEnabled(btnAutoDist.getValue());
			}
		});
	}

	public void createNewMirrorSettingsGroup() {
		grpNewMirrorSettings = new Group(this, SWT.NONE);
		grpNewMirrorSettings.setText("New Mirror Settings");
		grpNewMirrorSettings.setLayout(new GridLayout(3, false));

		lblNewSourcemirrorDistance = new Label(grpNewMirrorSettings, SWT.NONE);
		lblNewSourcemirrorDistance.setText("Source-Mirror Distance");

		lblNewMirrorfocusDistance = new Label(grpNewMirrorSettings, SWT.NONE);
		lblNewMirrorfocusDistance.setText("Mirror-Focus Distance");

		lblNewAngleOf = new Label(grpNewMirrorSettings, SWT.NONE);
		lblNewAngleOf.setText("Angle Of Incidence (mrad)");

		newSourceMirrorDistance = new ScaleBox(grpNewMirrorSettings, SWT.NONE);
		newSourceMirrorDistance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		newSourceMirrorDistance.setDecimalPlaces(8);
		new Label(newSourceMirrorDistance, SWT.NONE);
		new Label(newSourceMirrorDistance, SWT.NONE);
		newMirrorFocusDistance = new ScaleBox(grpNewMirrorSettings, SWT.NONE);
		newMirrorFocusDistance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		newMirrorFocusDistance.setDecimalPlaces(8);
		new Label(newMirrorFocusDistance, SWT.NONE);
		new Label(newMirrorFocusDistance, SWT.NONE);
		newAngleOfIncidence = new ScaleBox(grpNewMirrorSettings, SWT.NONE);
		newAngleOfIncidence.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		newAngleOfIncidence.setDecimalPlaces(8);
		new Label(newAngleOfIncidence, SWT.NONE);
		new Label(newAngleOfIncidence, SWT.NONE);
	}

	public void createScanDataGroup() {
		
		scanDataGroup = new Group(this, SWT.NONE);
		scanDataGroup.setText("Scan Data");
		scanDataGroup.setLayout(new GridLayout(1, false));

		Composite comp = new Composite(scanDataGroup, SWT.NONE);
		GridData gd_comp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_comp.heightHint = 34;
		comp.setLayoutData(gd_comp);
		comp.setLayout(new GridLayout(4, false));
		
		lblScanNumbers = new Label(comp, SWT.NONE);
		lblScanNumbers.setText("Scan Numbers");
		scanNumberInputs = new TextWrapper(comp, SWT.BORDER);
		
		GridData gd_scanNumberInputs = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_scanNumberInputs.widthHint=353;
		scanNumberInputs.setLayoutData(gd_scanNumberInputs);
		
		lblErrorFile = new Label(comp, SWT.NONE);
		lblErrorFile.setText("Error File");
		errorFile = new TextWrapper(comp, SWT.BORDER);
		GridData gd_errorFile = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_errorFile.widthHint = 59;
		errorFile.setLayoutData(gd_errorFile);

		Composite comp2 = new Composite(scanDataGroup, SWT.NONE);
		comp2.setLayout(new GridLayout(4, false));
		
		lblMinSlitPos = new Label(comp2, SWT.NONE);
		lblMinSlitPos.setText("Min Slit Pos");
		minSlitPos = new ScaleBox(comp2, SWT.NONE);
		minSlitPos.setMaximum(99999);
		minSlitPos.setMinimum(-99999);
		new Label(minSlitPos, SWT.NONE);
		new Label(minSlitPos, SWT.NONE);
		lblMaxSlitPos = new Label(comp2, SWT.NONE);
		lblMaxSlitPos.setText("Max Slit Pos");
		maxSlitPos = new ScaleBox(comp2, SWT.NONE);
		maxSlitPos.setMaximum(99999);
		maxSlitPos.setMinimum(-99999);
		new Label(maxSlitPos, SWT.NONE);
		new Label(maxSlitPos, SWT.NONE);
		new Label(comp, SWT.NONE);
		new Label(comp, SWT.NONE);
		new Label(comp, SWT.NONE);
		new Label(comp, SWT.NONE);

		scanDirectory = new Label(scanDataGroup, SWT.NONE);
		scanDirectory.setText("Scan file directory = current visit");
		scanDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		Button changeDir = new Button(scanDataGroup, SWT.NONE);
		changeDir.setAlignment(SWT.LEFT);
		changeDir.setText("Change scan file directory");
		
		changeDir.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(scanDataGroup.getShell());
		        selectedDir = dlg.open();
		        scanDirectory.setText("Scan file directory = " + selectedDir);
			}
		});
		
	}

	public void createBeamGroup() {
		beamOffsetGroup = new Group(this, SWT.NONE);
		GridData gd_beamOffsetGroup = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_beamOffsetGroup.widthHint = 287;
		beamOffsetGroup.setLayoutData(gd_beamOffsetGroup);
		beamOffsetGroup.setLayout(new GridLayout(3, false));
		beamOffsetGroup.setText("Beam");
		btnAutoOffset = new BooleanWrapper(beamOffsetGroup, SWT.CHECK);
		btnAutoOffset.setText("Auto Offset");
		lblOffset = new Label(beamOffsetGroup, SWT.NONE);
		lblOffset.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblOffset.setText("Offset");
		btnAutoOffset.addValueListener(new ValueListener() {

			@Override
			public String getValueListenerName() {
				return null;
			}

			@Override
			public void valueChangePerformed(ValueEvent e) {
				boolean autoOffset = btnAutoOffset.getValue();
				beamOffset.setEnabled(!autoOffset);
				lblOffset.setEnabled(!autoOffset);
				if (autoOffset) {
					user_offset = "n";
				} else {
					user_offset = "y";
				}
			}
		});
		lblFocusSize = new Label(beamOffsetGroup, SWT.NONE);
		lblFocusSize.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		lblFocusSize.setText("Focus Size (pixels)");
		new Label(beamOffsetGroup, SWT.NONE);
		beamOffset = new ScaleBox(beamOffsetGroup, SWT.NONE);
		beamOffset.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		beamOffset.setEnabled(true);
		new Label(beamOffset, SWT.NONE);
		focusSize = new ScaleBox(beamOffsetGroup, SWT.NONE);
		focusSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		focusSize.setEnabled(true);
		new Label(focusSize, SWT.NONE);
	}

	public void createButtons() {
		composite = new Composite(this, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite.heightHint = 30;
		gd_composite.widthHint = 627;
		composite.setLayoutData(gd_composite);
		btnRunOptimisation = new Button(composite, SWT.NONE);
		btnRunOptimisation.setBounds(482, 0, 131, 29);
		btnRunOptimisation.setAlignment(SWT.LEFT);
		btnRunOptimisation.setText("Run Optimisation");
		btnRunOptimisation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runOptimiseScript();
			}
		});
	}

	public void createReturnVoltagesCheckbox() {
	}

	public FieldComposite getMirrorScannableName() {
		return bimorphScannableName;
	}

	public ComboWrapper getMirrorType() {
		return mirrorType;
	}

	public FieldComposite getVoltageIncrement() {
		return voltageIncrement;
	}

	public FieldComposite getSlitSizeScannable() {
		return slitSizeScannable;
	}

	public FieldComposite getSlitPosScannable() {
		return slitPosScannable;
	}

	public FieldComposite getSlitSize() {
		return slitSize;
	}

	public FieldComposite getSlitStart() {
		return slitStart;
	}

	public FieldComposite getSlitEnd() {
		return slitEnd;
	}

	public FieldComposite getSlitStep() {
		return slitStep;
	}

	public FieldComposite getOtherSlitSizeScannable() {
		return otherSlitSizeScannable;
	}

	public FieldComposite getOtherSlitPosScannable() {
		return otherSlitPosScannable;
	}

	public FieldComposite getOtherSlitPos() {
		return otherSlitPos;
	}

	public FieldComposite getOtherSlitSize() {
		return otherSlitSize;
	}

	public FieldComposite getDetectorName() {
		return detectorName;
	}

	public FieldComposite getExposureTime() {
		return exposureTime;
	}

	public FieldComposite getSettleTime() {
		return settleTime;
	}

	public FieldComposite getScanNumberInputs() {
		return scanNumberInputs;
	}

	public FieldComposite getErrorFile() {
		return errorFile;
	}

	public FieldComposite getBeamOffset() {
		return beamOffset;
	}

	public FieldComposite getBimorphScannableName() {
		return bimorphScannableName;
	}

	public FieldComposite getBimorphVoltages() {
		return bimorphVoltages;
	}

	public ScaleBox getPixelSize() {
		return pixelSize;
	}

	public ScaleBox getPresentSourceMirrorDistance() {
		return presentSourceMirrorDistance;
	}

	public ScaleBox getPresentMirrorFocusDistance() {
		return presentMirrorFocusDistance;
	}

	public ScaleBox getPresentAngleOfIncidence() {
		return presentAngleOfIncidence;
	}

	public ScaleBox getNewSourceMirrorDistance() {
		return newSourceMirrorDistance;
	}

	public ScaleBox getNewMirrorFocusDistance() {
		return newMirrorFocusDistance;
	}

	public ScaleBox getNewAngleOfIncidence() {
		return newAngleOfIncidence;
	}

	public ComboWrapper getISign() {
		return iSign;
	}

	public ScaleBox getDetectorDistance() {
		return detectorDistance;
	}

	public ScaleBox getFocusSize() {
		return focusSize;
	}

	public BooleanWrapper getAutoOffset() {
		return btnAutoOffset;
	}
	
	public BooleanWrapper getAutoDist() {
		return btnAutoDist;
	}

	public BooleanWrapper getCalculateErrorFile() {
		return btnCalculateErrorFile;
	}
	
	public FieldComposite getBimorphGroups() {
		return bimorphGroups;
	}

	public BooleanWrapper getBtnGroupElectrodesTogether() {
		return btnGroupElectrodesTogether;
	}

	public ScaleBox getPresentDetDist() {
		return presentDetDist;
	}

	public ScaleBox getSlitScanDetDist() {
		return slitScanDetDist;
	}

	public ScaleBox getMinSlitPos() {
		return minSlitPos;
	}

	public ScaleBox getMaxSlitPos() {
		return maxSlitPos;
	}

	public ComboWrapper getInv() {
		return inv;
	}

	public ComboWrapper getMethod() {
		return method;
	}
	
}
