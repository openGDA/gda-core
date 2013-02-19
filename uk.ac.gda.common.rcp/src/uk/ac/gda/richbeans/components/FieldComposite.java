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

package uk.ac.gda.richbeans.components;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import uk.ac.gda.richbeans.beans.IFieldWidget;
import uk.ac.gda.richbeans.event.BoundsListener;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

/**
 *
 */
public abstract class FieldComposite extends Composite implements IFieldWidget {

	/**
	 * Can be null!
	 */
	protected Control              mainControl;
	
	/**
	 * Events managed here.
	 */
	protected EventManagerDelegate eventDelegate;

	/**
	 *
	 */
	public enum NOTIFY_TYPE {
	/**
	 * Default
	 */
	DEFAULT, 
	/**
	 * Whenever new value received even if from internal event. Does not
	 * ignore the off flag though.
	 */
	VALUE_CHANGED,
	
	/**
	 * Will tell listeners that value changed even if off.
	 */
	ALWAYS}

	protected NOTIFY_TYPE notifyType;
	
	/**
	 * @param type
	 */
	public void setNotifyType(NOTIFY_TYPE type) {
		this.notifyType = type;
	}
	
	/**
	 * @param parent
	 * @param style
	 */
	public FieldComposite(Composite parent, int style) {
		super(parent, style);
		this.eventDelegate = new EventManagerDelegate(this);
	}

	protected String fieldName;
	/**
	 * @return b
	 */
	@Override
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName
	 */
	@Override
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	

	/** DO not set this, use off() and on() **/
	private volatile boolean on = false;
	
	/**
	 * @return the on
	 */
	@Override
	public boolean isOn() {
		return on;
	}

	/**
	 * off listeners, and notifications.
	 */
	@Override
	public void off() {	
		this.on = false;
	}
	/**
	 * on listeners, and notifications.
	 */
	@Override
	public void on() {
		this.on = true;
	}

	/**
	 * @return Returns the notifyType.
	 */
	public NOTIFY_TYPE getNotifyType() {
		return notifyType;
	}
	
	@Override
	public void setEnabled(final boolean isEnabled) {
		if (mainControl!=null) mainControl.setEnabled(isEnabled);
	}

	@Override
	public void addValueListener(ValueListener l) {
		eventDelegate.addValueListener(l);
	}
	
	@Override
	public void fireValueListeners() {
		final ValueEvent evt = new ValueEvent(this, getFieldName());
		evt.setValue(getValue());
		eventDelegate.notifyValueListeners(evt);
	}

	@Override
	public void fireBoundsUpdaters() {
		final ValueEvent evt = new ValueEvent(this, getFieldName());
		evt.setValue(getValue());
		eventDelegate.notifyBoundsProviderListeners(evt);
	}
	
	@Override
	public void dispose() {
		if (eventDelegate!=null) eventDelegate.dispose();
		if (mainControl!=null)   mainControl.dispose();
		super.dispose();
	}

	/**
	 * Remove a certain listener - can also use the clearListeners(...) method.
	 * @param l
	 */
	@Override
	public void removeValueListener(final ValueListener l) {
		eventDelegate.removeValueListener(l);
	}
	
	/**
	 * Add a listener to be notified of the user entering new values
	 * into the widget.
	 * @param l
	 */
	public void addBoundsListener(final BoundsListener l) {
		eventDelegate.addBoundsListener(l);
	}
	
	/**
	 * Remove a certain listener - can also use the clearListeners(...) method.
	 * @param l
	 */
	public void removeBoundsListener(final BoundsListener l) {
		eventDelegate.removeBoundsListener(l);
	}

	// Important to start with true!
	protected boolean active = true;

	@Override
	public boolean isActivated() {
		return active;
	}
	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * @return string including fieldName
	 * 
	 */
	@Override
	public String toString() {
		return fieldName+" ["+getClass().getName()+"]";
	}

	/**
	 *
	 * @return the underlying control used in the widget for entering data.
	 *         NOTE this can be null depending on implementation.
	 */
	public Control getControl() {
		return mainControl;
	}

	/**
	 * If the widget opens a dialog this method should be implemented to close it.
	 */
	public void closeDialog() {
		
	}
}
