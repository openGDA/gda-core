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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import uk.ac.gda.richbeans.beans.IFieldWidget;
import uk.ac.gda.richbeans.components.EventManagerDelegate;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

/**
 * Designed to wrap Text objects to allow then to work with BeanUI
 * @author fcp94556
 *
 */
public class RadioWrapper extends Group implements IFieldWidget {
	
	protected final EventManagerDelegate eventDelegate;
	protected final Map<String,Button>   buttonMap;
	protected final Map<String, SelectionListener> listenerMap;
	
	/**
	 * Simply calls super and adds some listeners.
	 * @param parent
	 * @param style
	 * @param items 
	 */
	public RadioWrapper(Composite parent, int style, String[] items) {

		super(parent, SWT.NONE);	
		setLayout(new RowLayout());
		
		this.eventDelegate = new EventManagerDelegate(this);
		this.buttonMap     = new HashMap<String,Button>(7);
		this.listenerMap   = new HashMap<String,SelectionListener>(7);
		
		for (int i = 0; i < items.length; i++) {
			final Button button = new Button(this, SWT.RADIO | style);
			button.setText(items[i]);
			buttonMap.put(items[i],button);
			final SelectionListener selectionListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					final ValueEvent evt = new ValueEvent(button,getFieldName());
					evt.setValue(getValue());
					eventDelegate.notifyValueListeners(evt);
				}
			};
			listenerMap.put(items[i],selectionListener);
			button.addSelectionListener(selectionListener);
		}
	}
	
	@Override
	public void dispose() {
		for (String key : buttonMap.keySet()) {
			buttonMap.get(key).removeSelectionListener(listenerMap.get(key));
		}
		super.dispose();
	}

	
	@Override
	public Object getValue() {
		for (String  label : buttonMap.keySet()) {
			if (buttonMap.get(label).getSelection()) return label;
		}
		return null;
	}
	
	@Override
	public void setValue(Object value) {
		for (String  label : buttonMap.keySet()) {
			if (label.equals(value)) {
				buttonMap.get(label).setSelection(true);
			} else {
				buttonMap.get(label).setSelection(false);
			}
		}
	}

	/**
	 * Clears all buttons of value.
	 */
	public void clear() {
		for (String  label : buttonMap.keySet()) {
			buttonMap.get(label).setSelection(false);
		}
	}
	/*******************************************************************/
	/**        This section will be the same for many wrappers.       **/
	/*******************************************************************/
	@Override
	protected void checkSubclass () {
	}
	private boolean isOn = false;
	@Override
	public boolean isOn() {return isOn;}
	@Override
	public void off() {isOn = false;}
	@Override
	public void on() {isOn = true;}
	
	// Important to start with true!
	private boolean active = true;

	@Override
	public boolean isActivated() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
		setVisible(active);
	}
	
	@Override
	public void addValueListener(ValueListener l) {
		eventDelegate.addValueListener(l);
	}

	/*******************************************************************/
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
	
	@Override
	public void fireValueListeners() {
		final ValueEvent evt = new ValueEvent(this, getFieldName());
		evt.setValue(getValue());
		eventDelegate.notifyValueListeners(evt);
	}
	/**
	 * 
	 */
	@Override
	public void fireBoundsUpdaters() {
		final ValueEvent evt = new ValueEvent(this, getFieldName());
		evt.setValue(getValue());
		eventDelegate.notifyBoundsProviderListeners(evt);
	}

	@Override
	public void removeValueListener(ValueListener listener) {
		eventDelegate.removeValueListener(listener);
	}

}

	