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

/**
 * 
 */
package gda.example.richbean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;

public final class ExampleExptComposite extends Composite {

	private FieldComposite finalEnergy;
	private FieldComposite startEnergy;

	public ExampleExptComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		label.setText("finalEnergy");
		this.finalEnergy = new ScaleBox(this, SWT.NONE);
		finalEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false,
				1, 1));
		label.setText("startEnergy");
		this.startEnergy = new ScaleBox(this, SWT.NONE);
		startEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

	}

	public FieldComposite getFinalEnergy() {
		return finalEnergy;
	}

	public FieldComposite getStartEnergy() {
		return startEnergy;
	}

}
