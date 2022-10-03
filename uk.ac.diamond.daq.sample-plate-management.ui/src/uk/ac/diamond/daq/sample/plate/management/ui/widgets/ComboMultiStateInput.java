/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.sample.plate.management.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ComboMultiStateInput extends AbstractMultiStateInput {

	private Integer defaultValue;

	private String[] stateValues;

	public ComboMultiStateInput(Composite parent, String name, Integer defaultValue, String pvName, String[] stateValues) {
		super(parent, name, pvName);
		this.defaultValue = defaultValue;
		this.stateValues = stateValues;
		initGUI();
	}

	@Override
	public String[] getCurrentStateValues() {
		List<String> values = new ArrayList<>();
		for (Control control: inputStates.get(currentStateIndex).getChildren()) {
			values.add("'" + ((Combo) control).getText() + "'");
		}
		return values.toArray(new String[0]);
	}

	@Override
	public void initInput() {
		Composite composite = new Composite(this, SWT.NONE);
		GridDataFactory.fillDefaults().span(1, 1).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(composite);

		Combo combo = new Combo(composite, SWT.READ_ONLY);
		if (stateValues != null) {
			combo.setItems(stateValues);
		}
		if (defaultValue != null) {
			combo.select(defaultValue);
		}
		GridDataFactory.swtDefaults().span(1, 1).grab(true, false).hint(SWT.DEFAULT, SWT.DEFAULT).applyTo(combo);

		this.stateNames.add("ENUM");
		inputStates.add(composite);
		showState(0, false);
	}
}