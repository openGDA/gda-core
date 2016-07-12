/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.ionchambers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class VoltageSupplyComposite extends Composite{
	private Text text;

	public VoltageSupplyComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);
		Group grpVoltageSupply = new Group(this, SWT.NONE);
		GridData gd_grpVoltageSupply = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpVoltageSupply.widthHint = 250;
		grpVoltageSupply.setLayoutData(gd_grpVoltageSupply);
		GridLayout gl_grpVoltageSupply = new GridLayout(3, false);
		gl_grpVoltageSupply.verticalSpacing = 3;
		gl_grpVoltageSupply.marginWidth = 3;
		gl_grpVoltageSupply.marginHeight = 3;
		gl_grpVoltageSupply.horizontalSpacing = 3;
		grpVoltageSupply.setLayout(gl_grpVoltageSupply);
		grpVoltageSupply.setText("Voltage Supply");
		Label lblNewLabel = new Label(grpVoltageSupply, SWT.NONE);
		lblNewLabel.setText("Current Voltage");
		Label lblNewLabel_2 = new Label(grpVoltageSupply, SWT.NONE);
		lblNewLabel_2.setText("1200 V");
		Button btnNewButton = new Button(grpVoltageSupply, SWT.NONE);
		btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnNewButton.setText("Get");
		Label lblNewLabel_1 = new Label(grpVoltageSupply, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Demand Voltage");
		text = new Text(grpVoltageSupply, SWT.BORDER);
		GridData gd_text = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text.widthHint = 75;
		text.setLayoutData(gd_text);
		Button btnSet = new Button(grpVoltageSupply, SWT.NONE);
		btnSet.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnSet.setText("Set");
	}

}