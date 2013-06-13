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

import gda.jython.JythonServerFacade;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class StanfordAmplifierComposite{

	private Combo sensitivity;
	private Combo offset;
	private Combo offsetUnit;
	private Combo unit;
	private Button on;
	private Button off;
	private String scannableName;

	public StanfordAmplifierComposite(Composite parent, int style, String name, String scannable) {
		scannableName=scannable;
		
		Group group = new Group(parent, SWT.NONE);
		group.setText(name);
		group.setLayout(new GridLayout(5, false));
		
		Label lblSensitivity = new Label(group, SWT.NONE);
		lblSensitivity.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSensitivity.setText("Sensitivity");
		
		sensitivity = new Combo(group, SWT.NONE);
		sensitivity.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JythonServerFacade.getInstance().runCommand(scannableName+".setSensitivity("+ sensitivity.getSelectionIndex() + ")");
			}
		});
		sensitivity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sensitivity.setItems(new String[] {"1", "2", "5", "10", "20", "50", "100", "200", "500"});
		
		unit = new Combo(group, SWT.NONE);
		unit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JythonServerFacade.getInstance().runCommand(scannableName+".setUnit("+ unit.getSelectionIndex() + ")");
			}
		});
		unit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		unit.setItems(new String[] {"pA/V", "nA/V", "uA/V", "mA/V"});
		
		Button update = new Button(group, SWT.NONE);
		update.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		update.setText("Update Values");
		
		Label lblInputOffsetCurrent = new Label(group, SWT.NONE);
		lblInputOffsetCurrent.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblInputOffsetCurrent.setText("Input Offset");
		
		offset = new Combo(group, SWT.NONE);
		offset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JythonServerFacade.getInstance().runCommand(scannableName+".setOffset("+ offset.getSelectionIndex() + ")");
			}
		});
		offset.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		offset.setItems(new String[] {"1", "2", "5", "10", "20", "50", "100", "200", "500"});
		
		offsetUnit = new Combo(group, SWT.NONE);
		offsetUnit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JythonServerFacade.getInstance().runCommand(scannableName+".setOffsetUnit("+ offsetUnit.getSelectionIndex() + ")");
			}
		});
		offsetUnit.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		offsetUnit.setItems(new String[] {"pA", "nA", "uA"});
		
		on = new Button(group, SWT.NONE);
		on.setSelection(true);
		on.setText("On");
		
		off = new Button(group, SWT.NONE);
		off.setText("Off");
		
		on.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JythonServerFacade.getInstance().runCommand(scannableName+".setOn(1)");
				boolean offsetOn = false;
				if(JythonServerFacade.getInstance().evaluateCommand(scannableName+".isOn()").equals("1"))
					offsetOn=true;
			
				on.setEnabled(!offsetOn);
				off.setEnabled(offsetOn);
			}
		});

		off.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JythonServerFacade.getInstance().runCommand(scannableName+".setOn(0)");
				boolean offsetOn = false;
				if(JythonServerFacade.getInstance().evaluateCommand(scannableName+".isOn()").equals("1"))
					offsetOn=true;
			
				on.setEnabled(!offsetOn);
				off.setEnabled(offsetOn);
			}
		});

		update.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateFields();
			}
		});
		
		updateFields();
	}
	
	private void updateFields(){
		sensitivity.select(Integer.parseInt(JythonServerFacade.getInstance().evaluateCommand(scannableName+".getSensitivity()")));
		offset.select(Integer.parseInt(JythonServerFacade.getInstance().evaluateCommand(scannableName+".getOffset()")));
		offsetUnit.select(Integer.parseInt(JythonServerFacade.getInstance().evaluateCommand(scannableName+".getOffsetUnit()")));
		unit.select(Integer.parseInt(JythonServerFacade.getInstance().evaluateCommand(scannableName+".getUnit()")));
		
		boolean offsetOn = false;
		if(JythonServerFacade.getInstance().evaluateCommand(scannableName+".isOn()").equals("1"))
			offsetOn=true;
	
		on.setEnabled(!offsetOn);
		off.setEnabled(offsetOn);
	}
}
