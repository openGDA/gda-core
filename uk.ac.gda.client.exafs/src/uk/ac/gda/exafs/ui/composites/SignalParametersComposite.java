/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites;

import java.util.regex.Pattern;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.widgets.wrappers.RegularExpressionTextWrapper;
import org.eclipse.richbeans.widgets.wrappers.SpinnerWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper.TEXT_TYPE;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import gda.device.Scannable;
import uk.ac.gda.beans.exafs.SignalParameters;
import uk.ac.gda.components.wrappers.FindableNameWrapper;

public class SignalParametersComposite extends Composite {
	private FindableNameWrapper scannableName;
	private TextWrapper expression;
	private TextWrapper label;
	private TextWrapper name;
	private Label scannableNameLabel;
	private Button setLabelToName;
	private Label lblDecimalPlaces;
	private SpinnerWrapper decimalPlacesSpinner;

	public SignalParametersComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		setLabelToName = new Button(this, SWT.CHECK);
		setLabelToName.setText("Set Label to Scannable Name");
		GridDataFactory.defaultsFor(setLabelToName).span(2, 1).applyTo(setLabelToName);
		setLabelToName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnables();
			}
		});

		Label labelLabel = new Label(this, SWT.NONE);
		labelLabel.setText("Column Label");

		label = new RegularExpressionTextWrapper(this, SWT.BORDER, Pattern.compile("[a-zA-Z0-9_]+"));
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		scannableNameLabel = new Label(this, SWT.NONE);
		scannableNameLabel.setText("Scannable Name");

		scannableName = new FindableNameWrapper(this, SWT.BORDER, Scannable.class);
		scannableName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		scannableName.addValueListener(new ValueAdapter("Sig Params Listener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateLabelContents();
			}
		});

		lblDecimalPlaces = new Label(this, SWT.NONE);
		lblDecimalPlaces.setText("Decimal Places");

		decimalPlacesSpinner = new SpinnerWrapper(this, SWT.BORDER);

		Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setToolTipText("Optional parameter to say variable name to extract from scannable. If scannable name should be evaluated directly, leave blank.");
		nameLabel.setText("Function Name*");

		name = new TextWrapper(this, SWT.BORDER);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label expressionLabel = new Label(this, SWT.NONE);
		expressionLabel.setToolTipText("Optional Parameter to set expression of scannable");
		expressionLabel.setText("Expression*");

		expression = new TextWrapper(this, SWT.BORDER);
		expression.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		expression.setTextType(TEXT_TYPE.EXPRESSION);
	}

	public TextWrapper getName() {
		return name;
	}

	public SpinnerWrapper getDecimalPlaces() {
		return decimalPlacesSpinner;
	}

	public TextWrapper getLabel() {
		return label;
	}

	public TextWrapper getExpression() {
		return expression;
	}

	public TextWrapper getScannableName() {
		return scannableName;
	}

	public void selectionChanged(SignalParameters sigParams) {
		String labelValue = (String) label.getValue();
		String scannableNameValue = (String) scannableName.getValue();
		if (labelValue == null || "".equals(labelValue) || labelValue.equals(scannableNameValue)) {
			if (sigParams != null) {
				sigParams.setLabel(scannableNameValue);
				sigParams.setScannableName(scannableNameValue);
			}
			setLabelToName.setSelection(true);
		} else {
			setLabelToName.setSelection(false);
		}
		updateEnables();
		scannableName.refresh();
	}

	private void updateEnables() {
		boolean selection = setLabelToName.getSelection();
		label.setEnabled(!selection);
		updateLabelContents();
	}

	private void updateLabelContents() {
		boolean selection = setLabelToName.getSelection();
		if (selection) {
			label.setEnabled(true);
			label.setValue(scannableName.getValue());
			label.setEnabled(false);
		}
	}

}