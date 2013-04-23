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

package uk.ac.gda.exafs.ui.composites;

import gda.jython.JythonServerFacade;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.b18.B18SampleParameters;
import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.NumberBox;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

/**
 *
 */
public class LN2CryoStageComposite extends FieldBeanComposite {

	private static final Logger logger = LoggerFactory.getLogger(XYThetaStageComposite.class);

	private BooleanWrapper manual;
	boolean showManual;
	private BooleanWrapper editCalibration;
	private ScaleBox height;
	private ScaleBox angle;
	private ScaleBox calibAngle;
	private ScaleBox calibHeight;
	private ComboWrapper sampleNumberA;
	private ComboWrapper sampleNumberB;
	private ComboWrapper cylinderType;
	private Image fluoCylinder;
	private Image transCylinder;
	private int sel;
	private Group grpCalibrationOfSample;
	private Group sampleNumbers;
	private boolean editCal;
	private String strVal;
	private B18SampleParameters bean;
	private Label transparentIdeaLabel;
	private int xoffset = 24;
	private int xgap = 21;
	private int yoffset = 22;
	private int ygap = 18;
	private Display disp;

	@SuppressWarnings("unused")
	public LN2CryoStageComposite(Composite parent, int style, B18SampleParameters abean) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		bean = abean;

		this.manual = new BooleanWrapper(this, SWT.NONE);
		manual.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		manual.setText("Manual Control");
		showManual = bean.getLN2CryoStageParameters().isManual();

		new Label(this, SWT.NONE);

		final Label lblCylinderType = new Label(this, SWT.NONE);
		lblCylinderType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCylinderType.setText("Cylinder Type");

		cylinderType = new ComboWrapper(this, SWT.NONE);
		cylinderType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cylinderType.setItems(new String[] { "fluo", "trans" });

		final Label angleLabel = new Label(this, SWT.NONE);
		angleLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		angleLabel.setText("Angle (rot)");

		this.angle = new ScaleBox(this, SWT.NONE);
		angle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		angle.setUnit("deg");

		final Label heightLabel = new Label(this, SWT.NONE);
		heightLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		heightLabel.setText("Height (y)");

		this.height = new ScaleBox(this, SWT.NONE);
		height.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		height.setUnit("mm");

		new Label(this, SWT.NONE);

		try {
			setMotorLimits("cryox", height);
			setMotorLimits("cryorot", angle);
		} catch (Exception e) {
			logger.warn("exception while fetching hardware limits: " + e.getMessage(), e);
		}

		final Button btnSet = new Button(this, SWT.NONE);
		btnSet.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String xval = JythonServerFacade.getInstance().evaluateCommand("cryox()");

				if (xval.substring(xval.indexOf(".") + 1).length() > 2)
					height.setValue(xval.substring(0, xval.indexOf(".") + 3));
				else
					height.setValue(xval);

				String rotval = JythonServerFacade.getInstance().evaluateCommand("cryorot()");
				if (rotval.substring(rotval.indexOf(".") + 1).length() > 2)
					angle.setValue(rotval.substring(0, rotval.indexOf(".") + 3));
				else
					angle.setValue(rotval);
			}
		});

		btnSet.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnSet.setText("Get current values");

		sampleNumbers = new Group(this, SWT.NONE);
		sampleNumbers.setText("Sample Number(s)");
		sampleNumbers.setLayout(new GridLayout(2, false));
		sampleNumbers.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		sampleNumbers.setEnabled(!showManual);

		Composite sampleComposite = new Composite(sampleNumbers, 0);
		sampleComposite.setLayout(new GridLayout(4, false));
		sampleComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		sampleComposite.setVisible(true);
		final Label sampleNumberALabel = new Label(sampleComposite, SWT.NONE);
		sampleNumberALabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		sampleNumberALabel.setText("Up");

		sampleNumberA = new ComboWrapper(sampleComposite, SWT.NONE);
		GridData gd_sampleNumberA = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_sampleNumberA.widthHint = 60;
		sampleNumberA.setLayoutData(gd_sampleNumberA);

		sampleNumberA.setItems(new String[] { "0", "1", "2", "3", "4", "5" });

		final Label sampleNumberBLabel = new Label(sampleComposite, SWT.NONE);
		sampleNumberBLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		sampleNumberBLabel.setText("Around");

		sampleNumberB = new ComboWrapper(sampleComposite, SWT.NONE);
		GridData gd_sampleNumberB = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_sampleNumberB.widthHint = 60;
		sampleNumberB.setLayoutData(gd_sampleNumberB);

		if (bean.getLN2CryoStageParameters().getCylinderType().equals("fluo")) {
			sampleNumberB.setItems(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8" });
		} else {
			sampleNumberB.setItems(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" });
		}

		transparentIdeaLabel = new Label(sampleNumbers, SWT.NONE);

		disp = this.getDisplay();

		InputStream fluoCylinderStream = getClass().getResourceAsStream("sample_select.bmp");
		fluoCylinder = new Image(disp, fluoCylinderStream);
		final ImageData fluoImg = fluoCylinder.getImageData();

		InputStream transCylinderStream = getClass().getResourceAsStream("sample_select_trans.bmp");
		transCylinder = new Image(disp, transCylinderStream);
		final ImageData transImg = transCylinder.getImageData();

		strVal = bean.getLN2CryoStageParameters().getCylinderType();

		transparentIdeaLabel.addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				int x = e.x / 21;
				int y = e.y / 18;
				
				int fluo_offset=0;
				
				if (x >= 5 && bean.getLN2CryoStageParameters().getCylinderType().equals("fluo"))
					fluo_offset=1;
				if(x<=(sampleNumberB.getItems().length+fluo_offset))
					sampleNumberB.select(x-1-fluo_offset);
				
				if(y<=sampleNumberA.getItems().length)
					sampleNumberA.select(y-1);
				
				setSample(x, y);
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});

		if (strVal.equals("fluo"))
			sel = 0;
		else if (strVal.equals("trans"))
			sel = 1;

		if (sel == 0) {
			transparentIdeaLabel.setImage(fluoCylinder);
			transparentIdeaLabel.setSize(fluoCylinder.getImageData().width, fluoCylinder.getImageData().height);
		} else {
			transparentIdeaLabel.setImage(transCylinder);
			transparentIdeaLabel.setSize(transCylinder.getImageData().width, transCylinder.getImageData().height);
		}

		cylinderType.addValueListener(new ValueAdapter("cylinderType") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				if (bean.getLN2CryoStageParameters().getCylinderType().equals("fluo")) {
					transparentIdeaLabel.setImage(fluoCylinder);
					transparentIdeaLabel.setSize(fluoCylinder.getImageData().width, fluoCylinder.getImageData().height);
					sampleNumberB.setItems(new String[] { "1", "2", "3", "4", "5", "6", "7", "8" });

				} else {
					transparentIdeaLabel.setImage(transCylinder);
					transparentIdeaLabel.setSize(transCylinder.getImageData().width,
							transCylinder.getImageData().height);
					sampleNumberB.setItems(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" });
				}
			}
		});

		grpCalibrationOfSample = new Group(this, SWT.NONE);
		grpCalibrationOfSample.setText("Calibration of sample cassette");
		grpCalibrationOfSample.setLayout(new GridLayout(2, false));
		grpCalibrationOfSample.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		grpCalibrationOfSample.setEnabled(!showManual);

		editCalibration = new BooleanWrapper(grpCalibrationOfSample, SWT.NONE);
		editCalibration.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		editCalibration.setText("Edit Calibration");

		new Label(grpCalibrationOfSample, SWT.NONE);

		final Label lblAngle = new Label(grpCalibrationOfSample, SWT.NONE);
		lblAngle.setToolTipText("Motor positon when sample 1 aligned");
		lblAngle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAngle.setText("Angle (rot)");

		calibAngle = new ScaleBox(grpCalibrationOfSample, SWT.NONE);
		calibAngle.setUnit("deg");
		calibAngle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Label lblHeight = new Label(grpCalibrationOfSample, SWT.NONE);
		lblHeight.setToolTipText("Motor position when sample 1 centred");
		lblHeight.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblHeight.setText("Height (y)");

		calibHeight = new ScaleBox(grpCalibrationOfSample, SWT.NONE);
		calibHeight.setUnit("mm");
		calibHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		editCal = bean.getLN2CryoStageParameters().isEditCalibration();

		grpCalibrationOfSample.setEnabled(!showManual);
		editCalibration.setEnabled(!showManual);
		angleLabel.setEnabled(showManual);
		angle.setEnabled(showManual);
		heightLabel.setEnabled(showManual);
		height.setEnabled(showManual);
		btnSet.setEnabled(showManual);
		lblCylinderType.setEnabled(!showManual);
		cylinderType.setEnabled(!showManual);
		sampleNumberA.setEnabled(!showManual);
		sampleNumberB.setEnabled(!showManual);
		lblAngle.setEnabled(!showManual);
		calibAngle.setEnabled(!showManual);
		lblHeight.setEnabled(!showManual);
		calibHeight.setEnabled(!showManual);

		calibAngle.setEnabled(editCal);
		calibHeight.setEnabled(editCal);
		lblAngle.setEnabled(editCal);
		lblHeight.setEnabled(editCal);

		try {
			setMotorLimits("cryorot", calibAngle);
			setMotorLimits("cryox", calibHeight);
		} catch (Exception e) {
			logger.warn("exception while fetching hardware limits: " + e.getMessage(), e);
		}
		
		sampleNumberA.addValueListener(new ValueAdapter("sampleNumberA") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				int a = Integer.parseInt(sampleNumberB.getValue().toString());
				int b = Integer.parseInt(e.getValue().toString());
					
				if (a >= 5 && bean.getLN2CryoStageParameters().getCylinderType().equals("fluo"))
					a++;
				setSample(a, b);
			}
		});

		sampleNumberB.addValueListener(new ValueAdapter("sampleNumberB") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				int a = Integer.parseInt(e.getValue().toString());
				int b = Integer.parseInt(sampleNumberA.getValue().toString());
					
				if (a >= 5 && bean.getLN2CryoStageParameters().getCylinderType().equals("fluo"))
					a++;
				setSample(a, b);
			}
		});

		manual.addValueListener(new ValueListener() {

			@Override
			public void valueChangePerformed(ValueEvent e) {
				showManual = !showManual;
				grpCalibrationOfSample.setEnabled(!showManual);
				sampleNumbers.setEnabled(!showManual);
				editCalibration.setEnabled(!showManual);
				angleLabel.setEnabled(showManual);
				angle.setEnabled(showManual);
				heightLabel.setEnabled(showManual);
				height.setEnabled(showManual);
				btnSet.setEnabled(showManual);
				lblCylinderType.setEnabled(!showManual);
				cylinderType.setEnabled(!showManual);
				sampleNumberA.setEnabled(!showManual);
				sampleNumberB.setEnabled(!showManual);
				lblAngle.setEnabled(!showManual && editCalibration.getValue());
				calibAngle.setEnabled(!showManual && editCalibration.getValue());
				lblHeight.setEnabled(!showManual && editCalibration.getValue());
				calibHeight.setEnabled(!showManual && editCalibration.getValue());
			}

			@Override
			public String getValueListenerName() {
				return null;
			}
		});

		editCalibration.addValueListener(new ValueListener() {

			@Override
			public void valueChangePerformed(ValueEvent e) {
				calibAngle.setEnabled(editCalibration.getValue());
				calibHeight.setEnabled(editCalibration.getValue());
				lblAngle.setEnabled(editCalibration.getValue());
				lblHeight.setEnabled(editCalibration.getValue());
			}

			@Override
			public String getValueListenerName() {
				return null;
			}
		});
	}

	public void setMotorLimits(String motorName, NumberBox box) throws Exception {
		String lowerLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getLowerMotorLimit()");
		String upperLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getUpperMotorLimit()");
		if (!lowerLimit.equals("None") && !lowerLimit.isEmpty())
			box.setMinimum(Double.parseDouble(lowerLimit));
		if (!upperLimit.equals("None") && !upperLimit.isEmpty())
			box.setMaximum(Double.parseDouble(upperLimit));
	}

	public void setSample(int x, int y) {

		if (bean.getLN2CryoStageParameters().getCylinderType().equals("fluo")) {

			transparentIdeaLabel.setImage(fluoCylinder);

			ImageData tmpFluo = fluoCylinder.getImageData();
			
			if (x != 5) {
				for (int i = -4; i <= 2; i++) {
					tmpFluo.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap)
							+ (ygap / 2) - 1, 255);
					tmpFluo.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap)
							+ (ygap / 2), 255);
					tmpFluo.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap)
							+ (ygap / 2) + 1, 255);
				}

				for (int i = -3; i <= 1; i++) {
					tmpFluo.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap)
							+ (ygap / 2) - 2, 255);
					tmpFluo.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap)
							+ (ygap / 2) - 3, 255);
					tmpFluo.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap)
							+ (ygap / 2) + 2, 255);
					tmpFluo.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap)
							+ (ygap / 2) + 3, 255);
				}

				transparentIdeaLabel.setImage(new Image(disp, tmpFluo));
			}
		}

		else if (bean.getLN2CryoStageParameters().getCylinderType().equals("trans")) {

			transparentIdeaLabel.setImage(transCylinder);

			ImageData tmpTrans = transCylinder.getImageData();

			for (int i = -4; i <= 2; i++) {
				tmpTrans.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap) + (ygap / 2)
						- 1, 255);
				tmpTrans.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap) + (ygap / 2),
						255);
				tmpTrans.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap) + (ygap / 2)
						+ 1, 255);
			}

			for (int i = -3; i <= 1; i++) {
				tmpTrans.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap) + (ygap / 2)
						- 2, 255);
				tmpTrans.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap) + (ygap / 2)
						- 3, 255);
				tmpTrans.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap) + (ygap / 2)
						+ 2, 255);
				tmpTrans.setPixel(xoffset + ((x - 1) * xgap) + (xgap / 2) + i, yoffset + ((y - 1) * ygap) + (ygap / 2)
						+ 3, 255);
			}

			transparentIdeaLabel.setImage(new Image(disp, tmpTrans));
		}
	}

	public FieldComposite getHeight() {
		return height;
	}

	public FieldComposite getAngle() {
		return angle;
	}

	public FieldComposite getCalibAngle() {
		return calibAngle;
	}

	public FieldComposite getCalibHeight() {
		return calibHeight;
	}

	public ComboWrapper getCylinderType() {
		return cylinderType;
	}

	public FieldComposite getManual() {
		return manual;
	}

	public ComboWrapper getSampleNumberA() {
		return sampleNumberA;
	}

	public ComboWrapper getSampleNumberB() {
		return sampleNumberB;
	}

	public BooleanWrapper getEditCalibration() {
		return editCalibration;
	}
}
