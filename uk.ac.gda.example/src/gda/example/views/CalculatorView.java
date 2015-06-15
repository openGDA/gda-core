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

package gda.example.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class CalculatorView extends ViewPart {

	public CalculatorView() {
	}

	private Text firstNumberTextBox;

	@SuppressWarnings("unused")
	private Text secondNumberTextBox;

	@SuppressWarnings("unused")
	private Text totalTextBox;

	@Override
	public void createPartControl(Composite parent) {

		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(parent);

		Label firstNumberLabel = new Label(parent, SWT.NONE);
		firstNumberLabel.setText("First number:");

		firstNumberTextBox = new Text(parent, SWT.BORDER);

		Label secondNumberLabel = new Label(parent, SWT.NONE);
		secondNumberLabel.setText("Second number:");

		secondNumberTextBox = new Text(parent, SWT.BORDER);

		Button addNumbersButton = new Button(parent, SWT.PUSH);
		addNumbersButton.setText("Add numbers");
		GridDataFactory.fillDefaults().span(2, 1).grab(false, false).applyTo(addNumbersButton);

		Label totalLabel = new Label(parent, SWT.NONE);
		totalLabel.setText("Total:");

		totalTextBox = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
	}

	@Override
	public void setFocus() {
		firstNumberTextBox.forceFocus();
	}

}
