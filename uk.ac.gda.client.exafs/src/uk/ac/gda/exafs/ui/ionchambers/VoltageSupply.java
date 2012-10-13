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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;

public class VoltageSupply extends Composite implements ViewEditor {
	private Text text;

	public VoltageSupply(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());
		
		Group grpVoltageSupply = new Group(this, SWT.NONE);
		grpVoltageSupply.setLayout(new GridLayout(3, false));
		FormData fd_grpVoltageSupply = new FormData();
		fd_grpVoltageSupply.top = new FormAttachment(0);
		fd_grpVoltageSupply.left = new FormAttachment(0);
		fd_grpVoltageSupply.bottom = new FormAttachment(0, 97);
		fd_grpVoltageSupply.right = new FormAttachment(0, 276);
		grpVoltageSupply.setLayoutData(fd_grpVoltageSupply);
		grpVoltageSupply.setText("Voltage Supply");
		
		Label lblNewLabel = new Label(grpVoltageSupply, SWT.NONE);
		lblNewLabel.setText("Current Voltage");
		
		Label lblNewLabel_2 = new Label(grpVoltageSupply, SWT.NONE);
		
		Button btnNewButton = new Button(grpVoltageSupply, SWT.NONE);
		btnNewButton.setText("Get");
		
		Label lblNewLabel_1 = new Label(grpVoltageSupply, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Demand Voltage");
		
		text = new Text(grpVoltageSupply, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnSet = new Button(grpVoltageSupply, SWT.NONE);
		btnSet.setText("Set");
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isInEditor() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setInEditor() {
		// TODO Auto-generated method stub
		
	}
}
