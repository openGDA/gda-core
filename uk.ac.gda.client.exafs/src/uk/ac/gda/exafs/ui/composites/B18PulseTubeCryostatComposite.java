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

import org.eclipse.richbeans.widgets.FieldBeanComposite;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.exafs.b18.PulseTubeCryostatParameters;

public class B18PulseTubeCryostatComposite extends FieldBeanComposite {
	private ScaleBox setPoint;
	private ScaleBox tolerance;
	private ScaleBox time;
	private BooleanWrapper controlFlag;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	@SuppressWarnings("unused")
	public B18PulseTubeCryostatComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label lblSetPoint = new Label(this, SWT.NONE);
		lblSetPoint.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1));
		lblSetPoint.setText("Set Point");
		setPoint = new ScaleBox(this, SWT.NONE);
		setPoint.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1));
		setPoint.setMinimum(-200);

		Label lblTolerance = new Label(this, SWT.NONE);
		lblTolerance.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1));
		lblTolerance.setText("Tolerance");
		tolerance = new ScaleBox(this, SWT.NONE);
		tolerance.setUnit("K");
		tolerance.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1));


		Label lblTime = new Label(this, SWT.NONE);
		lblTime.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1));
		lblTime.setText("Waiting Time");
		time = new ScaleBox(this, SWT.NONE);
		time.setUnit("s");
		time.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1));


		this.controlFlag = new BooleanWrapper(this, SWT.NONE);
		controlFlag.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1));
		controlFlag.setText("Only read, no control");
	}

	public PulseTubeCryostatParameters getParameterBean() {
		PulseTubeCryostatParameters pulseTubeParams = new PulseTubeCryostatParameters();
		pulseTubeParams.setSetPoint(setPoint.getNumericValue());
		pulseTubeParams.setTolerance(tolerance.getNumericValue());
		pulseTubeParams.setTime(time.getNumericValue());
		pulseTubeParams.setControlFlag(controlFlag.getValue());
		return pulseTubeParams;
	}

	public void setupUiFromBean(PulseTubeCryostatParameters bean) {
		setPoint.setValue(bean.getSetPoint());
		tolerance.setValue(bean.getTolerance());
		time.setValue(bean.getTime());
		controlFlag.setValue(bean.isControlFlag());
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public ScaleBox getSetPoint() {
		return setPoint;
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
