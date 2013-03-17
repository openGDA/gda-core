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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;

import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;

/**
 * This is a non generic (unlike how its name implies) composite used for I18 with hard coded scannable names.
 * TODO rename composite or make configurable. The latter is prefered.
 */
public final class SampleStageParametersComposite extends FieldBeanComposite {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SampleStageParametersComposite.class);
	private ScaleBox x;
	private ScaleBox y;
	private ScaleBox z;

	public SampleStageParametersComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("x");
		
		x = new ScaleBox(this, SWT.NONE);
		GridData gridData_2 = (GridData) x.getControl().getLayoutData();
		gridData_2.widthHint = 100;
		gridData_2.horizontalAlignment = SWT.LEFT;
		x.setDecimalPlaces(4);
		x.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("y");
		
		y = new ScaleBox(this, SWT.NONE);
		GridData gridData = (GridData) y.getControl().getLayoutData();
		gridData.widthHint = 100;
		gridData.horizontalAlignment = SWT.LEFT;
		y.setDecimalPlaces(4);
		y.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("z");
		
		z = new ScaleBox(this, SWT.NONE);
		GridData gridData_1 = (GridData) z.getControl().getLayoutData();
		gridData_1.widthHint = 100;
		gridData_1.horizontalAlignment = SWT.LEFT;
		z.setDecimalPlaces(4);
		z.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		try {
			setMotorLimits("sc_MicroFocusSampleX", (x));
			setMotorLimits("sc_MicroFocusSampleX", y);
			setMotorLimits("sc_sample_z", z);
		} catch (Exception e) {
			logger.warn("exception while setting hardware limits: " + e.getMessage(), e);
		}
	}

	public FieldComposite getX() {
		return x;
	}

	public FieldComposite getY() {
		return y;
	}

	public FieldComposite getZ() {
		return z;
	}

	public void setXValue(String newX) {
		this.x.setValue(newX);
	}

	public void setYValue(String newY) {
		this.y.setValue(newY);
	}

	public void setZValue(String newZ) {
		this.z.setValue(newZ);
	}

	public void setMotorLimits(String motorName, ScaleBox box) throws Exception {
		String lowerLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getLowerInnerLimit()");
		String upperLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getUpperInnerLimit()");
		if (!lowerLimit.equals("None") && !lowerLimit.isEmpty())
			box.setMinimum(Double.parseDouble(lowerLimit));
		if (!upperLimit.equals("None") && !upperLimit.isEmpty())
			box.setMaximum(Double.parseDouble(upperLimit));
	}

}
