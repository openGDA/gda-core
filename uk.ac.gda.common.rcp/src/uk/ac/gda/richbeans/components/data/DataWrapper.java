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

package uk.ac.gda.richbeans.components.data;

import uk.ac.gda.richbeans.beans.IFieldWidget;
import uk.ac.gda.richbeans.components.EventManagerDelegate;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

/**
 * A not widget wrapper for data only contained in the editor.
 */
public class DataWrapper implements IFieldWidget {
	
	private EventManagerDelegate eventDelegate;
	/**
	 * 
	 */
	public DataWrapper() {
		this.eventDelegate = new EventManagerDelegate(this);
	}
	
	
	private Object value;
    private String fieldName;
	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public void setFieldName(String fieldName) {
		this.fieldName  = fieldName;
	}

	@Override
	public void addValueListener(ValueListener l) {
		eventDelegate.addValueListener(l);
	}
	
	@Override
	public void removeValueListener(ValueListener l) {
		eventDelegate.removeValueListener(l);
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public boolean isActivated() {
		return true;
	}

	private boolean isOn = false;
	@Override
	public boolean isOn() {
		return isOn;
	}

	@Override
	public void off() {
		isOn = false;
	}

	@Override
	public void on() {
		isOn = true;
	}

	@Override
	public void setEnabled(boolean isEnabled) {

	}

	@Override
	public void setValue(Object value) {
		this.value = value;
		eventDelegate.notifyValueListeners(new ValueEvent(this, getFieldName()));
	}

	@Override
	public void fireValueListeners() {
		final ValueEvent evt = new ValueEvent(this, getFieldName());
		evt.setValue(getValue());
		eventDelegate.notifyValueListeners(evt);
	}


	@Override
	public void fireBoundsUpdaters() {
		// There are none
	}

	@Override
	public void dispose() {
		
	}


}
