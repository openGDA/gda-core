/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.RangeBox;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;

public class B18LargeLNCryostatComposite extends FieldBeanComposite {
	private ScaleBox temperature;
	private ScaleBox tolerance;
	private ScaleBox time;
	private ScaleBox calibAngle;
	private ScaleBox calibHeight;
	private RangeBox sampleNumber;
	private ComboWrapper cylinderType;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	@SuppressWarnings("unused")
	public B18LargeLNCryostatComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(3, false));
		new Label(this, SWT.NONE);

		Label lblTemperature = new Label(this, SWT.NONE);
		lblTemperature.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTemperature.setText("Temperature");

		temperature = new ScaleBox(this, SWT.NONE);
		temperature.setUnit("C");
		temperature.setMinimum(-200);
		temperature.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(this, SWT.NONE);

		Label lblTolerance = new Label(this, SWT.NONE);
		lblTolerance.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTolerance.setText("Tolerance");

		tolerance = new ScaleBox(this, SWT.NONE);
		tolerance.setUnit("C");
		tolerance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(this, SWT.NONE);

		Label lblTime = new Label(this, SWT.NONE);
		lblTime.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTime.setText("Waiting Time");

		time = new ScaleBox(this, SWT.NONE);
		time.setUnit("s");
		time.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(this, SWT.NONE);

		Label lblSampleNumber = new Label(this, SWT.NONE);
		lblSampleNumber.setToolTipText("Enter numbers and/or ranges separated by commas");
		lblSampleNumber.setText("Sample Number(s)");

		sampleNumber = new RangeBox(this, SWT.NONE);
		sampleNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(this, SWT.NONE);

		Group grpCalibrationOfSample = new Group(this, SWT.NONE);
		grpCalibrationOfSample.setText("Calibration of sample cassette");
		grpCalibrationOfSample.setLayout(new GridLayout(2, false));
		grpCalibrationOfSample.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		Label lblAngle = new Label(grpCalibrationOfSample, SWT.NONE);
		lblAngle.setToolTipText("Motor positon when sample 1 aligned");
		lblAngle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAngle.setText("Angle");

		calibAngle = new ScaleBox(grpCalibrationOfSample, SWT.NONE);
		calibAngle.setUnit("deg");
		calibAngle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblHeight = new Label(grpCalibrationOfSample, SWT.NONE);
		lblHeight.setToolTipText("Motor position when sample 1 centred");
		lblHeight.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblHeight.setText("Height");

		calibHeight = new ScaleBox(grpCalibrationOfSample, SWT.NONE);
		calibHeight.setUnit("deg");
		calibHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(this, SWT.NONE);

		Label lblCylinderType = new Label(this, SWT.NONE);
		lblCylinderType.setText("Cylinder Type");

		cylinderType = new ComboWrapper(this, SWT.NONE);
		cylinderType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cylinderType.setItems(new String[] { "fluo", "trans" });
		cylinderType.select(0);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public FieldComposite getTemperature() {
		return temperature;
	}

	public FieldComposite getTolerance() {
		return tolerance;
	}

	public FieldComposite getTime() {
		return time;
	}

	public FieldComposite getSampleNumber() {
		return sampleNumber;
	}

	public FieldComposite getFirstSampleAngle() {
		return calibAngle;
	}

	public FieldComposite getFirstSampleHeight() {
		return calibHeight;
	}

	public FieldComposite getCylinderType() {
		return cylinderType;
	}
}
