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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.b18.B18SampleParameters;
import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

/**
 *
 */
public class SXCryoStageComposite extends FieldBeanComposite {

	private static final Logger logger = LoggerFactory.getLogger(XYThetaStageComposite.class);
	boolean showManual;
	private BooleanWrapper manual;
	private ScaleBox height;
	private ScaleBox rotation;
	private ScaleBox calibHeight;
	private ScaleBox sampleNumber;
	Group grpCalibrationOfSample;

	@SuppressWarnings("unused")
	public SXCryoStageComposite(Composite parent, int style, B18SampleParameters bean) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		this.manual = new BooleanWrapper(this, SWT.NONE);
		manual.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		manual.setText("Manual Control");
		showManual = bean.getSXCryoStageParameters().isManual();
		new Label(this, SWT.NONE);

		final Label heightLabel = new Label(this, SWT.NONE);
		heightLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		heightLabel.setText("Height (y)");

		this.height = new ScaleBox(this, SWT.NONE);
		height.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		height.setUnit("mm");

		final Label rotationLabel = new Label(this, SWT.NONE);
		rotationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		rotationLabel.setText("Rot");

		this.rotation = new ScaleBox(this, SWT.NONE);
		rotation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		rotation.setUnit("deg");

		new Label(this, SWT.NONE);


		final Button btnSet = new Button(this, SWT.NONE);
		btnSet.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String yval = JythonServerFacade.getInstance().evaluateCommand("sam1y()");

				if (yval.substring(yval.indexOf(".") + 1).length() > 2)
					height.setValue(yval.substring(0, yval.indexOf(".") + 3));
				else
					height.setValue(yval);

				String rotval = JythonServerFacade.getInstance().evaluateCommand("sam1rot()");

				if (rotval.substring(rotval.indexOf(".") + 1).length() > 2)
					rotation.setValue(rotval.substring(0, rotval.indexOf(".") + 3));
				else
					rotation.setValue(rotval);
			}
		});

		btnSet.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnSet.setText("Get current values");

		final Label lblSampleNumber = new Label(this, SWT.NONE);
		lblSampleNumber.setToolTipText("Enter numbers and/or ranges separated by commas");
		lblSampleNumber.setText("Sample Number(s)");

		sampleNumber = new ScaleBox(this, SWT.NONE);
		sampleNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sampleNumber.setMaximum(10);
		sampleNumber.setMinimum(1);
		
		
		grpCalibrationOfSample = new Group(this, SWT.NONE);
		grpCalibrationOfSample.setText("Calibration of sample cassette");
		grpCalibrationOfSample.setLayout(new GridLayout(2, false));
		grpCalibrationOfSample.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		final Label lblHeight = new Label(grpCalibrationOfSample, SWT.NONE);
		lblHeight.setToolTipText("Motor position when sample 1 centred");
		lblHeight.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblHeight.setText("Height (y)");

		calibHeight = new ScaleBox(grpCalibrationOfSample, SWT.NONE);
		calibHeight.setUnit("mm");
		calibHeight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		try {
			setMotorLimits("sam1y", height);
			setMotorLimits("sam1rot", rotation);
			setMotorLimits("sam1y", calibHeight);
			
		} catch (Exception e) {
			logger.warn("exception while fetching hardware limits: " + e.getMessage(), e);
		}

		heightLabel.setEnabled(showManual);
		height.setEnabled(showManual);
		//btnSet.setEnabled(showManual);
		lblSampleNumber.setEnabled(!showManual);
		sampleNumber.setEnabled(!showManual);
		grpCalibrationOfSample.setEnabled(!showManual);
		lblHeight.setEnabled(!showManual);
		calibHeight.setEnabled(!showManual);

		manual.addValueListener(new ValueListener() {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				showManual = !showManual;
				heightLabel.setEnabled(showManual);
				height.setEnabled(showManual);
				//btnSet.setEnabled(showManual);
				lblSampleNumber.setEnabled(!showManual);
				sampleNumber.setEnabled(!showManual);
				grpCalibrationOfSample.setEnabled(!showManual);
				lblHeight.setEnabled(!showManual);
				calibHeight.setEnabled(!showManual);
			}

			@Override
			public String getValueListenerName() {
				return null;
			}
		});
	}

	public void setMotorLimits(String motorName, ScaleBox box) throws Exception {
		String lowerLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getLowerMotorLimit()");
		String upperLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getUpperMotorLimit()");
		if (!lowerLimit.equals("None") && lowerLimit != null && !lowerLimit.isEmpty())
			box.setMinimum(Double.parseDouble(lowerLimit));
		if (!upperLimit.equals("None") && upperLimit != null && !upperLimit.isEmpty())
			box.setMaximum(Double.parseDouble(upperLimit));
	}

	public FieldComposite getHeight() {
		return height;
	}

	public FieldComposite getRot() {
		return rotation;
	}

	public FieldComposite getSampleNumber() {
		return sampleNumber;
	}

	public FieldComposite getCalibHeight() {
		return calibHeight;
	}

	public FieldComposite getManual() {
		return manual;
	}
}
