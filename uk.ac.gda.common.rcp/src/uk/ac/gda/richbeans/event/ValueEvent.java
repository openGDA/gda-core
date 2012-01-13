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

package uk.ac.gda.richbeans.event;

import java.util.EventObject;

import uk.ac.gda.richbeans.components.scalebox.ScaleBox;


/**
 * Custom event object used in notification of custom widgets
 * 
 * @see ScaleBox
 * @author fcp94556
 *
 */
public class ValueEvent extends EventObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2193419622660949003L;
	private double doubleValue;

	/**
	 * @return the doubleValue
	 */
	public double getDoubleValue() {
		return doubleValue;
	}
	/**
	 * @param doubleValue the doubleValue to set
	 */
	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
	}
	private Object value;
	private String fieldName;
	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	/**
	 * Constructor
	 * @param source
	 */
	public ValueEvent(final Object source, final String field) {
		super(source);
		this.fieldName = field;
	}
	/**
	 * @return f
	 */
	public String getFieldName() {
		return fieldName;
	}
	/**
	 * @param fieldName
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
}

	