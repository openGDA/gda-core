/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class ParameterSetterDialog extends Dialog {
	private int maxRow = 100;
	private int startRow;
	private int endRow;
	private Spinner endSpinner;
	private Spinner startSpinner;

	protected ParameterSetterDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		createComposite(container);
		return parent;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Apply value to range of rows");
	}

	private void createComposite(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		parent.setLayout(layout);

		Label infoLabel = new Label(parent, SWT.NONE);
		infoLabel.setText("Select the range of rows the value should be applied to : ");
		GridDataFactory.fillDefaults().span(4, 1).applyTo(infoLabel);

		Text startTextLabel = new Text(parent, SWT.NONE);
		startTextLabel.setText("Start row : ");
		startSpinner = new Spinner(parent, SWT.BORDER);
		startSpinner.setValues(initialRow, 0, maxRow, 0, 1, 10);

		Text endTextLabel = new Text(parent, SWT.NONE);
		endTextLabel.setText("End row : ");
		endSpinner = new Spinner(parent, SWT.BORDER);
		endSpinner.setValues(initialRow+1, 0, maxRow, 0, 1, 10);
	}

	@Override
	public boolean close() {
		//store start, end row values from widgets before they are disposed
		startRow = Math.min(startSpinner.getSelection(), endSpinner.getSelection());
		endRow = Math.max(startSpinner.getSelection(), endSpinner.getSelection());
		return super.close();
	}

	public void setMaxRow(int maxRow) {
		this.maxRow = maxRow;
	}

	public int getStartRow() {
		return startRow;
	}
	public int getEndRow() {
		return endRow;
	}

	private int initialRow = 0;
	public void setInitialRow(int initialRow) {
		this.initialRow = initialRow;

	}
}
