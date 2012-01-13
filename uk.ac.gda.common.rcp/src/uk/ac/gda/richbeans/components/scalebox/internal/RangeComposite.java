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

package uk.ac.gda.richbeans.components.scalebox.internal;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.doe.DOEUtils;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.scalebox.NumberBox;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.LabelWrapper;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;


/**
 * Used by the RangeBox, not intended for exterior use.
 */
public class RangeComposite extends Composite {

	private ScaleBox initialValue;
	private ScaleBox finalValue;
	private ScaleBox increment;
	private LabelWrapper size;

	public RangeComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		Label lblInitialValue = new Label(this, SWT.NONE);
		lblInitialValue.setText("Initial Value");
		
		this.initialValue = new ScaleBox(this, SWT.NONE);
		initialValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		initialValue.setValue(0d);
		
		Label lblFinalValue = new Label(this, SWT.NONE);
		lblFinalValue.setText("Final Value");
		
		this.finalValue = new ScaleBox(this, SWT.NONE);
		finalValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		finalValue.setValue(10d);
	
		Label lblIncrement = new Label(this, SWT.NONE);
		lblIncrement.setText("Increment");
		
		this.increment = new ScaleBox(this, SWT.NONE);
		increment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		increment.setMinimum(0);
		increment.setMaximum(1000);
		increment.setToolTipText("If the initial value is larger than the final value the loop will be constructed using increment to go backwards from the start.");
		increment.setValue(1d);
		
		final Label lblSize = new Label(this, SWT.NONE);
		lblSize.setText("Size");
		
		size = new LabelWrapper(this, SWT.NONE);
		size.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		final ValueListener sizeUpdate = new ValueAdapter() {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				try {
					final List<? extends Number> vals = DOEUtils.expand(getValue());
					size.setValue(vals.size());
				} catch (Exception ne) {
					size.setValue(0);
				}
			}
		};
		initialValue.addValueListener(sizeUpdate);
		finalValue.addValueListener(sizeUpdate);
		increment.addValueListener(sizeUpdate);
		
		try {
			BeanUI.switchState(this, true);
		} catch (Exception ignored) {
			// We can carry on if this happens.
		}
	}

	public String getValue() {
		final StringBuilder buf = new StringBuilder();
		buf.append(initialValue.getValue());
		buf.append("; ");
		buf.append(finalValue.getValue());
		buf.append("; ");
		buf.append(increment.getValue());
		return buf.toString();
	}
	
	public void setUnit(final String unit) {
		initialValue.setUnit(unit);
		finalValue.setUnit(unit);
		increment.setUnit(unit);
	}

	public void setValue(String range) {
		final String[] vals = range.split(";");
		final Double start = vals!=null&&vals.length>0 ? Double.parseDouble(vals[0].trim()) : 0;
		final Double end   = vals!=null&&vals.length>1 ? Double.parseDouble(vals[1].trim()) : start+1;
		final Double inc   = vals!=null&&vals.length>2 ? Double.parseDouble(vals[2].trim()) : 1;
		setValue(start, end, inc);
	}
	
	public void setValue(Number initialV, Number finalV, Number inc) {
		initialValue.setValue(initialV);
		finalValue.setValue(finalV);
		increment.setValue(inc);
	}

	/**
	 * Sets up the bounds from box passed in.
	 * @param numBox
	 */
	public void setBounds(final NumberBox numBox) {
		initialValue.copySettings(numBox);
		finalValue.copySettings(numBox);
		increment.setIntegerBox(numBox.isIntegerBox());
		increment.setDecimalPlaces(numBox.getDecimalPlaces());
	}

	/**
	 * @return Returns the initialValue.
	 */
	public ScaleBox getInitialValue() {
		return initialValue;
	}

	/**
	 * @return Returns the finalValue.
	 */
	public ScaleBox getFinalValue() {
		return finalValue;
	}

	/**
	 * @return Returns the increment.
	 */
	public ScaleBox getIncrement() {
		return increment;
	}

	public boolean isForwards() {
		final Double s = (Double)getInitialValue().getValue();
		final Double f = (Double)getFinalValue().getValue();
		return f>s;
	}


}
