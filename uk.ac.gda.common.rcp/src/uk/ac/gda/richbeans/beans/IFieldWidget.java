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

package uk.ac.gda.richbeans.beans;

import uk.ac.gda.richbeans.event.ValueListener;

/**
 * @author fcp94556
 *
 */
public interface IFieldWidget {
    /**
     * @return value
     */
    public Object getValue();
    /**
     * @param value
     */
    public void setValue(Object value);
    
    /** 
     * Defines if the widget is currently 
     * in an on state regarding listeners.
     * @return boolean
     */
    public boolean isOn();
    /**
     * Called to switch the ui bean off from
     * notifying its listeners.
     */
    public void off();
    
    /**
     * Called to switch the ui bean on to
     * notifying its listeners. Most beans 
     * start off and are switched on in the
     * init method.
     */
    public void on();
    
    /**
     * Should return true if the widget is visible and it's value
     * should be put in the bean. Should return false if the 
     * widget is not active and its value should not be stored.
     * @return boolean
     */
    public boolean isActivated();
    
    /**
     * IFieldWidget must have ability to add a listener notified when 
     * their value changes.
     * 
     * @param listener
     */
	public void addValueListener(ValueListener listener);
	
	/**
	 * Remove the listener
	 * @param listener
	 */
	public void removeValueListener(ValueListener listener);

	/**
	 * Fires all values listeners
	 */
	public void fireValueListeners();
	
	/**
	 * Fires all listeners involved in value updates
	 */
	public void fireBoundsUpdaters();
	
	/**
	 * @return field name this widget is linked to.
	 */
	public String getFieldName();
	
	/**
	 * Set field name is called when BeanUI links the widget.
	 * The field name can then be used in lists to update the correct value.
	 * @param fieldName
	 */
	public void setFieldName(String fieldName);
	
	/**
	 * Set the widget enabled or not
	 */
	public void setEnabled(boolean isEnabled);
	
	/**
	 * Called by RichBeanEditorPart when is disposes.
	 */
	public void dispose();
	
}

	