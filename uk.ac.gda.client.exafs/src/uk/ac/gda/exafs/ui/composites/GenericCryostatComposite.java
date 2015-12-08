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
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;

public class GenericCryostatComposite extends FieldBeanComposite {
	private ScaleBox temperature;
	private ScaleBox accuracy;
	private ScaleBox time;
	private BooleanWrapper controlFlag;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public GenericCryostatComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label lblSetPoint = new Label(this, SWT.NONE);
		lblSetPoint.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1));
		lblSetPoint.setText("Temperature");
		temperature = new ScaleBox(this, SWT.NONE);
		temperature.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1));
		temperature.setMinimum(-200);

		Label lblTolerance = new Label(this, SWT.NONE);
		lblTolerance.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1));
		lblTolerance.setText("Accuracy");
		accuracy = new ScaleBox(this, SWT.NONE);
		accuracy.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1));

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

	public ScaleBox getTemperature() {
		return temperature;
	}

	public FieldComposite getAccuracy(){
		return accuracy;
	}

	public FieldComposite getTime(){
		return time;
	}

	public FieldComposite getControlFlag() {
		return controlFlag;
	}
}
