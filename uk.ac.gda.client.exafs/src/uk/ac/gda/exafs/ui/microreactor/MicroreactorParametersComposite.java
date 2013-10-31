/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.microreactor;

import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.RegularExpressionTextWrapper;
import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;

public final class MicroreactorParametersComposite extends FieldBeanComposite {
	private SpinnerWrapper gas0Rate;
	private SpinnerWrapper gas1Rate;
	private SpinnerWrapper gas2Rate;
	private SpinnerWrapper gas3Rate;
	private SpinnerWrapper gas4Rate;
	private SpinnerWrapper gas5Rate;
	private SpinnerWrapper gas6Rate;
	private SpinnerWrapper gas7Rate;
	private RegularExpressionTextWrapper masses;
	private ScaleBox temperature;

	public MicroreactorParametersComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		Composite tempComposite = new Composite(this,SWT.NONE);
		GridLayout temp_gridLayout = new GridLayout(2, false);
		tempComposite.setLayout(temp_gridLayout);

		Label label = new Label(tempComposite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("Furnace temp");
		this.temperature = new ScaleBox(tempComposite, SWT.NONE);
		((GridData) temperature.getControl().getLayoutData()).widthHint = 98;
		temperature.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		temperature.setUnit("C");
		temperature.setMinimum(100);
		temperature.setMaximum(300);
		createEmptyLabel(temperature);
		
		final Group gasPressures = new Group(this, SWT.NONE);
		gasPressures.setText("Gas pressures (ml/min)");
		gasPressures.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout gridLayout = new GridLayout(2, false);
		gasPressures.setLayout(gridLayout);

		label = new Label(gasPressures, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("H");
		this.gas0Rate = new SpinnerWrapper(gasPressures, SWT.NONE);
		gas0Rate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		gas0Rate.setMinimum(0);
		gas0Rate.setMaximum(100);

		label = new Label(gasPressures, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("He");
		this.gas1Rate = new SpinnerWrapper(gasPressures, SWT.NONE);
		gas1Rate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		gas1Rate.setMinimum(0);
		gas1Rate.setMaximum(100);

		label = new Label(gasPressures, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("H/He");
		this.gas2Rate = new SpinnerWrapper(gasPressures, SWT.NONE);
		gas2Rate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		gas2Rate.setMinimum(0);
		gas2Rate.setMaximum(100);

		label = new Label(gasPressures, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("O2");
		this.gas3Rate = new SpinnerWrapper(gasPressures, SWT.NONE);
		gas3Rate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		gas3Rate.setMinimum(0);
		gas3Rate.setMaximum(100);

		label = new Label(gasPressures, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("CO/CO2");
		this.gas4Rate = new SpinnerWrapper(gasPressures, SWT.NONE);
		gas4Rate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		gas4Rate.setMinimum(0);
		gas4Rate.setMaximum(100);

		label = new Label(gasPressures, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("N2O");
		this.gas5Rate = new SpinnerWrapper(gasPressures, SWT.NONE);
		gas5Rate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		gas5Rate.setMinimum(0);
		gas5Rate.setMaximum(100);

		label = new Label(gasPressures, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("NH3");
		this.gas6Rate = new SpinnerWrapper(gasPressures, SWT.NONE);
		gas6Rate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		gas6Rate.setMinimum(0);
		gas6Rate.setMaximum(100);

		label = new Label(gasPressures, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("Alkane");
		this.gas7Rate = new SpinnerWrapper(gasPressures, SWT.NONE);
		gas7Rate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		gas7Rate.setMinimum(0);
		gas7Rate.setMaximum(100);
		
		final Group massGroup = new Group(this, SWT.NONE);
		massGroup.setText("Spectrometer Masses (AMU)");
		massGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		gridLayout = new GridLayout(1, false);
		massGroup.setLayout(gridLayout);

		masses = new RegularExpressionTextWrapper(massGroup, SWT.NONE, Pattern.compile("[0-9,]*"));
		masses.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		masses.setToolTipText("Comma separated integer list of Mass Spec masses");
		masses.setErrorImage(null);
		masses.setNameImage(null);
	}

	public FieldComposite getGas0Rate() {
		return gas0Rate;
	}

	public FieldComposite getGas1Rate() {
		return gas1Rate;
	}

	public FieldComposite getGas2Rate() {
		return gas2Rate;
	}

	public FieldComposite getGas3Rate() {
		return gas3Rate;
	}

	public FieldComposite getGas4Rate() {
		return gas4Rate;
	}

	public FieldComposite getGas5Rate() {
		return gas5Rate;
	}

	public FieldComposite getGas6Rate() {
		return gas6Rate;
	}

	public FieldComposite getGas7Rate() {
		return gas7Rate;
	}

	public FieldComposite getTemperature() {
		return temperature;
	}

	public FieldComposite getMasses() {
		return masses;
	}

	@SuppressWarnings("unused")
	private void createEmptyLabel(Composite composite){
		new Label(composite, SWT.NONE);
	}
	
}