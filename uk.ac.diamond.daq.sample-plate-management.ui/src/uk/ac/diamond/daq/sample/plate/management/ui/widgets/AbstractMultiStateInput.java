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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public abstract class AbstractMultiStateInput extends Composite {

	private Composite parent;

	private String name;

	private Button stateButton;

	private Label label;

	private String pvName;

	protected List<Composite> inputStates = new ArrayList<>();

	protected List<String> stateNames = new ArrayList<>();

	protected int currentStateIndex = 0;

	protected AbstractMultiStateInput(Composite parent, String name, String pvName) {
		super(parent, SWT.NONE);
		this.parent = parent;
		this.name = name;
		this.pvName = pvName;
	}

	protected void initGUI() {
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(this);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(this);

		stateButton = new Button(this, SWT.PUSH);
		GridDataFactory.swtDefaults().span(1, 1).applyTo(stateButton);

		label = new Label(this, SWT.NONE);
		label.setText(name);
		GridDataFactory.swtDefaults().span(1, 1).applyTo(label);

		initInput();

		Composite disabledComposite = new Composite(this, SWT.NONE);
		GridDataFactory.swtDefaults().span(1, 1).grab(true, true).applyTo(disabledComposite);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(disabledComposite);

		inputStates.add(disabledComposite);
		this.stateNames.add("OFF");
		showState(inputStates.size() - 1, false);
		showState(0, true);

		stateButton.setText(this.stateNames.get(0));
		stateButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			showNextState();
			stateButton.setText(this.stateNames.get(currentStateIndex));
			parent.layout(true, true);
		}));
	}

	protected void showNextState() {
		showState(currentStateIndex, false);
		currentStateIndex = (++currentStateIndex) % inputStates.size();
		showState(currentStateIndex, true);
	}

	protected void showState(int state, boolean show) {
		GridData data = (GridData) inputStates.get(state).getLayoutData();
		data.exclude = !show;
		inputStates.get(state).setVisible(show);
		inputStates.get(state).getParent().layout(true, true);
		this.layout(true, true);
	}

	public String getPVName() {
		return pvName;
	}

	public boolean isOff() {
		return (currentStateIndex == inputStates.size() - 1);
	}

	public abstract String[] getCurrentStateValues();

	public abstract void initInput();
}