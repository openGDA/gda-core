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
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

public class TextMultiStateInput extends AbstractMultiStateInput {

	private String defaultValue;

	private Map<Integer, String> states;

	public TextMultiStateInput(Composite parent, String name, String defaultValue, String pvName, Map<Integer, String> states) {
		super(parent, name, pvName);

		this.defaultValue = defaultValue;
		this.states = states;
		this.stateNames = new ArrayList<>(states.values());

		initGUI();
	}

	@Override
	public String[] getCurrentStateValues() {
		List<String> values = new ArrayList<>();
		for (Control control: inputStates.get(currentStateIndex).getChildren()) {
			if (((Text) control).getText().equals("")) {
				values.add(((Text) control).getMessage());
			} else {
				values.add(((Text) control).getText());
			}
		}

		return values.toArray(new String[0]);
	}

	@Override
	public void initInput() {
		if (states != null) {
			for (var state: states.entrySet()) {
				Composite composite = new Composite(this, SWT.NONE);
				GridDataFactory.fillDefaults().span(1, 1).applyTo(composite);
				GridLayoutFactory.fillDefaults().numColumns(state.getKey()).applyTo(composite);

				for (int j = 0; j < state.getKey(); j++) {
					Text text = new Text(composite, SWT.BORDER);
					if (defaultValue != null) {
						text.setMessage(defaultValue);
					}
					GridDataFactory.swtDefaults().span(1, 1).grab(true, false).hint(SWT.DEFAULT, SWT.DEFAULT).applyTo(text);
				}

				inputStates.add(composite);
				showState(inputStates.size() - 1, false);
			}
		}
	}
}