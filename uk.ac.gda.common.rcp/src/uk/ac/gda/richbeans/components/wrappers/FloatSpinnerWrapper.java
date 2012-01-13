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

package uk.ac.gda.richbeans.components.wrappers;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import uk.ac.gda.richbeans.event.ValueEvent;

/**
 * A spinner class that supports floating numbers of fixed precision
 * 
 */
public class FloatSpinnerWrapper extends SpinnerWrapper {
	
	private int width;
	private int precision;
	private int maximumValue;
	private double factor;

	/**
	 * Create a fixed float spinner
	 * @param parent
	 * @param style
	 */
	public FloatSpinnerWrapper(Composite parent, int style) {
		this(parent, style, 3, 1);
	}

	/**
	 * Create a fixed float spinner
	 * @param parent
	 * @param style
	 * @param width 
	 * @param precision 
	 */
	public FloatSpinnerWrapper(Composite parent, int style, int width, int precision) {
		super(parent, style);
		setFormat(width, precision);
	}


	/**
	 * Set the format and automatically set minimum and maximum allowed values
	 * @param width of displayed value as total number of digits
	 * @param precision of value in decimal places
	 */
	public void setFormat(int width, int precision) {
		this.precision = precision;
		this.setWidth(width);
		maximumValue = (int) Math.pow(10, width);
		factor = Math.pow(10, precision);

		spinner.setDigits(precision);
		spinner.setMinimum(-maximumValue);
		spinner.setMaximum(maximumValue);
		spinner.setIncrement(1);
		spinner.setPageIncrement(5);
		spinner.setSelection(0);
	}

	/**
	 * @return Returns the precision.
	 */
	public int getPrecision() {
		return precision;
	}

	/**
	 * @param width The width to set.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return Returns the width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param value
	 */
	@Override
	public void setValue(Object value) {
		
		if (value==null) {
			spinner.setSelection(spinner.getMinimum());
			return;
		}

		spinner.setSelection((int) (((Number)value).doubleValue() * factor));
		
		if (this.notifyType!=null&&(notifyType==NOTIFY_TYPE.ALWAYS||notifyType==NOTIFY_TYPE.VALUE_CHANGED)) {
			final ValueEvent evt = new ValueEvent(spinner,getFieldName());
			evt.setValue(getValue());
			eventDelegate.notifyValueListeners(evt);
		}
	}

	/**
	 * @return value
	 */
	@Override
	public Object getValue() {
		return spinner.getSelection() / factor;
	}
	
	public void setFloat(double value) {
		setValue(value);
	}
	
	public double getFloat() {
		return ((Number)getValue()).doubleValue();
	}

	/**
	 * @param listener
	 */
	public void addSelectionListener(SelectionListener listener) {
		spinner.addSelectionListener(listener);
	}

	/**
	 * @param listener
	 */
	public void removeSelectionListener(SelectionListener listener) {
		if (!spinner.isDisposed())
			spinner.removeSelectionListener(listener);
	}
	
	/**
	 * @see Spinner#addModifyListener(ModifyListener)
	 */
	public void addModifyListener(ModifyListener listener) {
		spinner.addModifyListener(listener);
	}

	/**
	 * @see Spinner#removeModifyListener(ModifyListener)
	 */
	public void removeModifyListener(ModifyListener listener) {
		if (!spinner.isDisposed())
			spinner.removeModifyListener(listener);
	}

	/**
	 * @param minimum
	 */
	public void setMinimum(double minimum) {
		spinner.setMinimum((int) (minimum * factor));
	}

	/**
	 * @param maximum
	 */
	public void setMaximum(double maximum) {
		spinner.setMaximum((int) (maximum * factor));
	}

}