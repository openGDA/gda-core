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

import org.eclipse.richbeans.widgets.FieldBeanComposite;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.exafs.b18.LakeshoreParameters;

/**
 *
 */
public final class LakeshoreComposite extends FieldBeanComposite {

	private BooleanWrapper tempSelect0;
	private BooleanWrapper tempSelect1;
	private BooleanWrapper tempSelect2;
	private BooleanWrapper tempSelect3;
	private ScaleBox setPointSet;
	private ScaleBox tolerance;
	private ScaleBox time;
	private BooleanWrapper controlFlag;

	public LakeshoreComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("Temperature 0");

		this.tempSelect0 = new BooleanWrapper(this, SWT.NONE);
		tempSelect0.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("Temperature 1");

		this.tempSelect1 = new BooleanWrapper(this, SWT.NONE);
		tempSelect1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("Temperature 2");

		this.tempSelect2 = new BooleanWrapper(this, SWT.NONE);
		tempSelect2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("Temperature 3");

		this.tempSelect3 = new BooleanWrapper(this, SWT.NONE);
		tempSelect3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("Setpoint Set");
		this.setPointSet = new ScaleBox(this, SWT.NONE);
		setPointSet.setUnit("C");
		setPointSet.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblTolerance = new Label(this, SWT.NONE);
		lblTolerance.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTolerance.setText("Tolerance");
		tolerance = new ScaleBox(this, SWT.NONE);
		tolerance.setUnit("C");
		tolerance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblTime = new Label(this, SWT.NONE);
		lblTime.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTime.setText("Wait for stability time");
		time = new ScaleBox(this, SWT.NONE);
		time.setUnit("s");
		time.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		this.controlFlag = new BooleanWrapper(this, SWT.NONE);
		controlFlag.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		controlFlag.setText("Only read, no control");
	}


	public LakeshoreParameters getParameterBean() {
		LakeshoreParameters params = new LakeshoreParameters();
		params.setTempSelect0(tempSelect0.getValue());
		params.setTempSelect1(tempSelect1.getValue());
		params.setTempSelect2(tempSelect2.getValue());
		params.setTempSelect3(tempSelect3.getValue());

		params.setSetPointSet(setPointSet.getNumericValue());
		params.setTolerance(tolerance.getNumericValue());
		params.setTime(time.getNumericValue());
		params.setControlFlag(controlFlag.getValue());

		return params;
	}

	public void setupUiFromBean(LakeshoreParameters bean) {
		tempSelect0.setValue(bean.isTempSelect0());
		tempSelect1.setValue(bean.isTempSelect1());
		tempSelect2.setValue(bean.isTempSelect2());
		tempSelect3.setValue(bean.isTempSelect3());

		setPointSet.setValue(bean.getSetPointSet());
		tolerance.setValue(bean.getTolerance());
		time.setValue(bean.getTime());
		controlFlag.setValue(bean.isControlFlag());
	}

	public FieldComposite getTempSelect0() {
		return tempSelect0;
	}

	public FieldComposite getTempSelect1() {
		return tempSelect1;
	}

	public FieldComposite getTempSelect2() {
		return tempSelect2;
	}

	public FieldComposite getTempSelect3() {
		return tempSelect3;
	}

	public FieldComposite getSetPointSet() {
		return setPointSet;
	}

	public FieldComposite getTolerance(){
		return tolerance;
	}

	public FieldComposite getTime(){
		return time;
	}

	public FieldComposite getControlFlag() {
		return controlFlag;
	}
}
