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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;

/**
 *
 */
public final class AttenuatorParametersComposite extends FieldBeanComposite {

	private FieldComposite name;
	private Combo position;
	private FieldComposite selectedPosition;

	public AttenuatorParametersComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("name");
		this.name = new TextWrapper(this, SWT.NONE);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("position");
		this.position = new Combo(this, SWT.READ_ONLY);
		{
			GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
			gridData.heightHint = 188;
			position.setLayoutData(gridData);
		}
		this.selectedPosition = new TextWrapper(this, SWT.NONE);
		//selectedPosition.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		this.selectedPosition.setVisible(false);

	}

	@SuppressWarnings("all")
	public FieldComposite getName() {
		return name;
	}

	public FieldComposite getSelectedPosition() {
		return selectedPosition;
	}

	public Combo getPosition() {
		return position;
	}

}
