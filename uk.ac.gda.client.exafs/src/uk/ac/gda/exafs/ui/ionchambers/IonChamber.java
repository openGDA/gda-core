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
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.RowData;

public class IonChamber extends Composite implements ViewEditor {
	private Text text;

	public IonChamber(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());
		Group grpIonChambers = new Group(this, SWT.NONE);
		grpIonChambers.setLayoutData(new FormData());
		grpIonChambers.setLayout(new GridLayout(1, false));
		grpIonChambers.setText("Ion Chambers");
		
		Composite composite_1 = new Composite(grpIonChambers, SWT.NONE);
		GridData gd_composite_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite_1.widthHint = 557;
		composite_1.setLayoutData(gd_composite_1);
		composite_1.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		List list = new List(composite_1, SWT.BORDER);
		list.setLayoutData(new RowData(91, 102));
		
		text = new Text(composite_1, SWT.BORDER);
		text.setLayoutData(new RowData(446, 97));
		
		Composite composite = new Composite(grpIonChambers, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite.widthHint = 556;
		composite.setLayoutData(gd_composite);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);
		
		Amplifier amplifier = new Amplifier(composite, SWT.NONE);
		GridData gd_amplifier = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_amplifier.widthHint = 273;
		amplifier.setLayoutData(gd_amplifier);
		VoltageSupply voltageSupply = new VoltageSupply(composite, SWT.NONE);
		new GasFilling(grpIonChambers, SWT.NONE);
	}

	@Override
	public boolean isInEditor() {
		return false;
	}

	@Override
	public void setInEditor() {
	}
}
