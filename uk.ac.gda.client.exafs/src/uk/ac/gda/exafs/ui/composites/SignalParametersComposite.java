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

import gda.device.Scannable;

import java.util.regex.Pattern;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.beans.exafs.SignalParameters;
import uk.ac.gda.components.wrappers.FindableNameWrapper;
import uk.ac.gda.richbeans.components.wrappers.PrintfWrapper;
import uk.ac.gda.richbeans.components.wrappers.RegularExpressionTextWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper.TEXT_TYPE;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;

/**
 *
 */
public class SignalParametersComposite extends Composite {

	private FindableNameWrapper scannableName;
	private TextWrapper expression;
	private TextWrapper label;
	private PrintfWrapper dataFormat;
	private TextWrapper name;
	private Label scannableNameLabel;
	private Button setLabelToName;
	/**
	 * @param parent
	 * @param style
	 */
	public SignalParametersComposite(Composite parent, int style) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
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
		
		final Label labelLabel = new Label(this, SWT.NONE);
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

		final Label dataFormatLabel = new Label(this, SWT.NONE);
		dataFormatLabel.setText("Data Format");

		dataFormat = new PrintfWrapper(this, SWT.BORDER);
		dataFormat.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setToolTipText("Optional parameter to say variable name to extract from scannable. If scannable name should be evaluated directly, leave blank.");
		nameLabel.setText("Variable Name*");

		name = new TextWrapper(this, SWT.BORDER);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Label expressionLabel = new Label(this, SWT.NONE);
		expressionLabel.setToolTipText("Optional Parameter to set expression of scannable");
		expressionLabel.setText("Expression*");

		expression = new TextWrapper(this, SWT.BORDER);
		expression.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		expression.setTextType(TEXT_TYPE.EXPRESSION);
	}
	/**
	 * @return n
	 */
	// getName is the same a private getName, suppress warning
	@SuppressWarnings("all")
	public TextWrapper getName() {
		return name;
	}
	/**
	 * @return TextWrapper
	 */
	public PrintfWrapper getDataFormat() {
		return dataFormat;
	}
	/**
	 * 
	 * @return k
	 */
	public TextWrapper getLabel() {
		return label;
	}
	/**
	 * 
	 * @return e
	 */
	public TextWrapper getExpression() {
		return expression;
	}
	/**
	 * 
	 * @return s
	 */
	public TextWrapper getScannableName() {
		return scannableName;
	}
	
	public void selectionChanged(SignalParameters sigParams) {
		String labelValue = (String) label.getValue();
		String scannableNameValue = (String) scannableName.getValue();
		String dataformat = dataFormat.getValue();
		if (labelValue == null || "".equals(labelValue) || labelValue.equals(scannableNameValue)) {
			// ensure here the minimum set of values in the bean to fulfil the xsd
			if (sigParams != null) {
				sigParams.setLabel(scannableNameValue);
				sigParams.setScannableName(scannableNameValue);
				sigParams.setDataFormat(dataformat);
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
