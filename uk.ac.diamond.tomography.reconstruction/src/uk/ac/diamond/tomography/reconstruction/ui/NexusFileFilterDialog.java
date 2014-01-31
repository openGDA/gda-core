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

package uk.ac.diamond.tomography.reconstruction.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A dialog that allows us to create new nexus file filters
 */
public class NexusFileFilterDialog extends NexusFileSortDialog {


	public NexusFileFilterDialog(Shell parentShell, String initialPath, String[] ruPaths) {
		super(parentShell, initialPath, ruPaths);
		setTitle("Create New NeXus File Filter");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Button filterNormalButton = new Button(composite, SWT.RADIO);
		Button filterReverseButton = new Button(composite, SWT.RADIO);
		Button filterConditionButton = new Button(composite, SWT.RADIO);

		filterNormalButton.setText("Remove NeXus file if it contains the path above");
		filterReverseButton.setText("Remove NeXus file if it DOES NOT contain the path above");
		filterConditionButton.setText("Remove NeXus file according to the condition below");

		filterNormalButton.setSelection(true);
		filterReverseButton.setSelection(false);
		filterConditionButton.setSelection(false);

		filterNormalButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRadioButtonsPressed(e);
			}
		});

		filterReverseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRadioButtonsPressed(e);
			}
		});

		filterConditionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRadioButtonsPressed(e);
			}
		});

		Group group = new Group(composite, SWT.NONE);
		group.setText("Condition");
		group.setLayout(GridLayoutFactory.swtDefaults().numColumns(3).create());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(group);

		Label label = new Label(group, SWT.NULL);
		label.setText("[NeXus Path]");
		Combo opCombo = new Combo(group, SWT.READ_ONLY);
		String[] operations = new String[]{"Equal to (==)", "Not equal to (!=)", "Greater than (>)", "Greater than or equal to (>=)", "Less than (<)", "Less than or equal to (<=)" };
		for (String op : operations) {
			opCombo.add(op);
		}
		opCombo.select(0);
		Text value = new Text(group, SWT.SINGLE | SWT.BORDER);
		return composite;
	}

	private void handleRadioButtonsPressed(SelectionEvent e){
		Object source = e.getSource();
		if (source != null){

		}

	}

	@Override
	protected void okPressed() {
		super.okPressed();
	}

}
