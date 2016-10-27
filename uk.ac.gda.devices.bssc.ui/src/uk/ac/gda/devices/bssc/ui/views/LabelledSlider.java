/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.gda.devices.bssc.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;

/**
 * Class for a labelled Slider. The labels are positioned below the slider at its extremities
 */
public class LabelledSlider extends Composite {
	Slider slider = null;
	private Label leftLabel;
	private Label rightLabel;
	private Label currentLabel;

	public LabelledSlider(Composite parent, int style) {
		super(parent, SWT.NONE);

		setLayout(new GridLayout(3, false));
		GridData gd;

		slider = new Slider(this, style);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		slider.setLayoutData(gd);

		leftLabel = new Label(this, SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		leftLabel.setLayoutData(gd);
		leftLabel.setToolTipText("Start value");
		
		currentLabel = new Label(this, SWT.CENTER);
		gd = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		currentLabel.setLayoutData(gd);
		currentLabel.setToolTipText("Current Frame");

		rightLabel = new Label(this, SWT.RIGHT);
		gd = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
		rightLabel.setLayoutData(gd);
		rightLabel.setToolTipText("End value");
		getValue();
	}

	public void setLeftLabel(String label) {
		leftLabel.setText(label);
	}

	public void setRightLabel(String label) {
		rightLabel.setText(label);
	}

	public void addSelectionListener(SelectionListener sListener) {
		slider.addSelectionListener(sListener);
	}

	public void setMinMax(int minimum, int maximum, String minText, String maxText) {
		slider.setMinimum(minimum);
		slider.setMaximum(maximum + slider.getThumb());
		leftLabel.setText(minText);
		rightLabel.setText(maxText);
		this.pack();
	}

	public void setThumb(int i) {
		slider.setThumb(i);
	}

	public void setIncrements(int inc, int pageInc) {
		slider.setIncrement(inc);
		slider.setPageIncrement(pageInc);
	}

	@Override
	public void setEnabled(boolean enabled) {
		slider.setEnabled(enabled);
	}

	public boolean equals(Slider s) {
		return slider.equals(s);
	}

	public void setValue(int value) {
		if (value < 0 || value >= slider.getMaximum())
			value = 0;
		slider.setSelection(value);
		currentLabel.setText(String.valueOf(value));
	}

	public int getValue() {
		int val = slider.getSelection();
		currentLabel.setText(String.valueOf(val));
		return val;
	}
}
