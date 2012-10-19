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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.GridData;

public class Amplifier extends Composite{
	public Amplifier(Composite parent, int style, boolean inEditor) {
		super(parent, style);
		setLayout(new FormLayout());

		Group grpAmplifier = new Group(this, SWT.NONE);
		grpAmplifier.setLayout(new GridLayout(1, false));
		FormData fd_grpAmplifier = new FormData();
		fd_grpAmplifier.top = new FormAttachment(0);
		fd_grpAmplifier.left = new FormAttachment(0);
		fd_grpAmplifier.bottom = new FormAttachment(0, 95);
		fd_grpAmplifier.right = new FormAttachment(0, 239);
		grpAmplifier.setLayoutData(fd_grpAmplifier);
		grpAmplifier.setText("Amplifier");


		Composite composite = new Composite(grpAmplifier, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_composite.widthHint = 230;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(3, false));



		Label lblNewLabel = new Label(composite, SWT.NONE);
		lblNewLabel.setText("Sensitivity");

		Combo combo = new Combo(composite, SWT.NONE);
		GridData gd_combo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_combo.widthHint = 107;
		combo.setLayoutData(gd_combo);

		if (inEditor) {
			Button btnCheckButton = new Button(composite, SWT.CHECK);
			btnCheckButton.setText("Change Before Scan");
			fd_grpAmplifier.bottom = new FormAttachment(0, 65);
			fd_grpAmplifier.right = new FormAttachment(0, 370);
			gd_composite.widthHint = 360;
		}
		
		else {
			Button btnSet = new Button(composite, SWT.NONE);
			btnSet.setText("Set");
		}
	}
}
