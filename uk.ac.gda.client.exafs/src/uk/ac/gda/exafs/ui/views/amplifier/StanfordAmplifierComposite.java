/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.views.amplifier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class StanfordAmplifierComposite{

	public StanfordAmplifierComposite(Composite parent, int style, String name) {

		
		Group group = new Group(parent, SWT.NONE);
		group.setText(name);
		group.setLayout(new GridLayout(5, false));
		
		Label lblSensitivity = new Label(group, SWT.NONE);
		lblSensitivity.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSensitivity.setText("Sensitivity");
		
		Combo combo = new Combo(group, SWT.NONE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		combo.setItems(new String[] {"1", "2", "5", "10", "20", "50", "100", "200", "500"});
		
		Combo combo_1 = new Combo(group, SWT.NONE);
		combo_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		combo_1.setItems(new String[] {"pA/V", "nA/V", "uA/V", "mA/V"});
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);
		
		Label lblInputOffsetCurrent = new Label(group, SWT.NONE);
		lblInputOffsetCurrent.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblInputOffsetCurrent.setText("Input Offset Current");
		
		Combo combo_2 = new Combo(group, SWT.NONE);
		combo_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		combo_2.setItems(new String[] {"1", "2", "5", "10", "20", "50", "100", "200", "500"});
		
		Combo combo_3 = new Combo(group, SWT.NONE);
		combo_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		combo_3.setItems(new String[] {"pA", "nA", "uA"});
		
		Button btnOn = new Button(group, SWT.NONE);
		btnOn.setText("On");
		
		Button btnOff = new Button(group, SWT.NONE);
		btnOff.setText("Off");
	}
}
