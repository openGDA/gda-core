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

package uk.ac.gda.exafs.ui.dialogs;

import gda.exafs.scan.RepetitionsProperties;
import gda.jython.InterfaceProvider;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

public class ChangeNumberRepetitionsDialog extends TitleAreaDialog {

	private Spinner spinner;
	private Label message;

	public ChangeNumberRepetitionsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Change number of repetitions");
		setMessage("Applies to the current running scan only.");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout(layout);

		message = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(message);
		message.setText("Number of repetitions:");

		spinner = new Spinner(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().applyTo(spinner);
		spinner.setValues(getLiveValue(), 0, 10000, 0, 1, 10);

		return parent;
	}

	private int getLiveValue() {
		String numRepsString = InterfaceProvider.getCommandRunner().evaluateCommand(
				"LocalProperties.get(\"" + RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY + "\")");
		if (numRepsString != null && !numRepsString.isEmpty()) {
			return Integer.parseInt(numRepsString);
		}
		return 0;
	}

	@Override
	protected void okPressed() {
		setLiveValue(spinner.getSelection());
		super.okPressed();
	}

	private void setLiveValue(int newValue) {
		InterfaceProvider.getCommandRunner().runCommand(
				"LocalProperties.set(\"" + RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY + "\",str(" + newValue + "))");

	}
}
