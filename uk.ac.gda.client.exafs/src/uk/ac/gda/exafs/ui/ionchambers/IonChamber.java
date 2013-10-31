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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

public class IonChamber extends Composite{
	private Text log;
	private List ionChamberList;
	private String[] ionChambers = {"I0", "It", "Iref"};
	
	public IonChamber(Composite parent, int style, boolean inEditor, IonChambersBean bean) {
		super(parent, style);
		setLayout(new FormLayout());
		Group grpIonChambers = new Group(this, SWT.NONE);
		grpIonChambers.setLayoutData(new FormData());
		GridLayout gl_grpIonChambers = new GridLayout(1, false);
		gl_grpIonChambers.verticalSpacing = 3;
		gl_grpIonChambers.horizontalSpacing = 3;
		gl_grpIonChambers.marginWidth = 3;
		gl_grpIonChambers.marginHeight = 3;
		grpIonChambers.setLayout(gl_grpIonChambers);
		grpIonChambers.setText("Ion Chambers");
		
		Composite composite_1 = new Composite(grpIonChambers, SWT.NONE);
		GridLayout gl_composite_1 = new GridLayout(2, false);
		gl_composite_1.marginWidth = 0;
		gl_composite_1.marginHeight = 0;
		composite_1.setLayout(gl_composite_1);
		GridData gd_composite_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite_1.heightHint = 72;
		gd_composite_1.widthHint = 489;
		composite_1.setLayoutData(gd_composite_1);
		
		ionChamberList = new List(composite_1, SWT.BORDER);
		ionChamberList.setItems(ionChambers);
		GridData gd_list = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_list.widthHint = 90;
		ionChamberList.setLayoutData(gd_list);
		
		log = new Text(composite_1, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		GridData gd_text = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_text.widthHint = 366;
		log.setLayoutData(gd_text);
		log.append(bean.getLog());
		
		Composite composite = new Composite(grpIonChambers, SWT.NONE);
		GridLayout gl_composite = new GridLayout(2, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginHeight = 0;
		gl_composite.horizontalSpacing = 3;
		gl_composite.marginWidth = 0;
		composite.setLayout(gl_composite);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite.widthHint = 488;
		composite.setLayoutData(gd_composite);
		
		Amplifier amplifier = new Amplifier(composite, SWT.NONE);
		amplifier.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));

		VoltageSupply voltageSupply = new VoltageSupply(composite, SWT.NONE);
		voltageSupply.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		
		GasFilling gasFilling = new GasFilling(grpIonChambers, SWT.NONE, inEditor);
		GridLayout gridLayout = (GridLayout) gasFilling.getLayout();
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		
		ionChamberList.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] selection = ionChamberList.getSelection();
				log.append(selection[0]+" selected\n");
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	public Text getLog() {
		return log;
	}

	public List getList() {
		return ionChamberList;
	}
	
}