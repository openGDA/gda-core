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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.tomography.reconstruction.INexusFilterDescriptor;
import uk.ac.diamond.tomography.reconstruction.INexusFilterDescriptor.Operation;
import uk.ac.diamond.tomography.reconstruction.views.NexusFilterDescriptor;

/**
 * A dialog that allows us to create new nexus file filters
 */
public class NexusFileFilterDialog extends BaseNexusPathDialog {
	private static final Logger logger = LoggerFactory.getLogger(NexusFileFilterDialog.class);

	private static final String OP_EXISTS = "Exists";
	private static final String OP_NOT_EXIST = "Does not exist";
	private static final String OP_EQUALS = "Equal to (==)";
	private static final String OP_NOT_EQUAL = "Not equal to (!=)";
	private static final String OP_GREATER = "Greater than (>)";
	private static final String OP_GREATER_EQUAL = "Greater than or equal to (>=)";
	private static final String OP_LESS = "Less than (<)";
	private static final String OP_LESS_EQUAL = "Less than or equal to (<=)";

	private static final Map<String, Operation> SUPPORTED_OPS = new LinkedHashMap<>();
	static {
		SUPPORTED_OPS.put(OP_EXISTS, Operation.CONTAINS);
		SUPPORTED_OPS.put(OP_NOT_EXIST, Operation.DOES_NOT_CONTAIN);
		SUPPORTED_OPS.put(OP_EQUALS, Operation.EQUALS);
		SUPPORTED_OPS.put(OP_NOT_EQUAL, Operation.NOT_EQUALS);
		SUPPORTED_OPS.put(OP_GREATER, Operation.GREATER_THAN);
		SUPPORTED_OPS.put(OP_GREATER_EQUAL, Operation.GREATER_THAN_OR_EQUAL);
		SUPPORTED_OPS.put(OP_LESS, Operation.LESS_THAN);
		SUPPORTED_OPS.put(OP_LESS_EQUAL, Operation.LESS_THAN_OR_EQUAL);
	}

	private Text operandField;
	private Combo opCombo;
	private NexusFilterDescriptor descriptor;

	public NexusFileFilterDialog(Shell parentShell, String initialPath, String[] ruPaths) {
		super(parentShell, initialPath, ruPaths);
		setTitle("Create New NeXus File Filter");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		Composite group = new Composite(composite, SWT.NONE);
		group.setLayout(GridLayoutFactory.swtDefaults().numColumns(2).create());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(group);

		Label label = new Label(group, SWT.NONE);
		label.setText("Condition to apply to path above:");
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(label);

		opCombo = new Combo(group, SWT.READ_ONLY);
		for (String op : SUPPORTED_OPS.keySet()) {
			opCombo.add(op);
		}
		opCombo.select(0);
		opCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateConditionField();
			}
		});
		operandField = new Text(group, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(operandField);

		// initialize defaults
		updateConditionField();
		return composite;
	}

	private void updateConditionField() {
		if (getFilterOperation().NUMBER_OF_OPERANDS == 0) {
			operandField.setEnabled(false);
		} else {
			operandField.setEnabled(true);
		}
	}

	/**
	 * Returns the filter operands
	 *
	 * @return the filter operands
	 */
	protected String[] getFilterOperands() {
		if (operandField.isEnabled()) {
			return new String[] { operandField.getText() };
		}
		return new String[0];
	}

	/**
	 * Returns the filter operation
	 *
	 * @return the filter operation string
	 */
	protected Operation getFilterOperation() {
		String text = opCombo.getText();
		return SUPPORTED_OPS.get(text);
	}

	/**
	 * @return filter or null if no filter
	 */
	public INexusFilterDescriptor getFilterDescriptor() {
		return descriptor;
	}

	@Override
	protected void okPressed() {
		try {
			descriptor = new NexusFilterDescriptor(getNexusPath(), getFilterOperation(), getFilterOperands());
		} catch (NullPointerException | IllegalArgumentException e) {
			logger.error("The GUI should have OK disabled if the settings are illegal,"
					+ " therefore this should be unreachable", e);
			descriptor = null;
		}
		super.okPressed();
	}
}
