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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

import swing2swt.layout.BorderLayout;
import uk.ac.gda.richbeans.beans.IFieldWidget;
import uk.ac.gda.richbeans.components.ButtonComposite;
import uk.ac.gda.richbeans.event.ValueEvent;

/**
 * 
 * @author fcp94556
 *
 */
public class SpinnerWrapper extends ButtonComposite implements IFieldWidget {

    protected Spinner spinner;
	private ModifyListener modifyListener;
	/**
	 * 
	 * @param parent
	 * @param style
	 */
	public SpinnerWrapper(Composite parent, int style) {
		
		super(parent, SWT.NONE);
		setLayout(new BorderLayout());
		
		this.spinner = new Spinner(this, style);
		spinner.setLayoutData(BorderLayout.CENTER);
		mainControl = spinner;
		
		this.modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				final ValueEvent evt = new ValueEvent(spinner,getFieldName());
				evt.setValue(getValue());
				eventDelegate.notifyValueListeners(evt);
			}
		};
		spinner.addModifyListener(modifyListener);
	}
	
	@Override
	public void dispose() {
		if (spinner!=null&&!spinner.isDisposed()) spinner.removeModifyListener(modifyListener);
		super.dispose();
	}

	@Override
	public Object getValue() {
		return spinner.getSelection(); // Integer
	}
	
	@Override
	public void setValue(Object value) {
		if (value==null) {
			spinner.setSelection(spinner.getMinimum());
			return;
		}
		
		if (value instanceof String) {
			double dblValue = Double.valueOf((String)value);
			long rounded = Math.round(dblValue);
			value = Integer.parseInt(new Long(rounded).toString());
		}
		
		spinner.setSelection(((Number)value).intValue());
		
		if (this.notifyType!=null&&(notifyType==NOTIFY_TYPE.ALWAYS||notifyType==NOTIFY_TYPE.VALUE_CHANGED)) {
			final ValueEvent evt = new ValueEvent(spinner,getFieldName());
			evt.setValue(getValue());
			eventDelegate.notifyValueListeners(evt);
		}
	}
	
	@Override
	public void setToolTipText(String text) {
		this.spinner.setToolTipText(text);
	}
	
	/*******************************************************************/
	/**        This section will be the same for many wrappers.       **/
	/*******************************************************************/
	@Override
	protected void checkSubclass () {
	}
	/**
	 * @param active the active to set
	 */
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		spinner.setVisible(active);
	}
	
	/**
	 * @param i
	 */
	public void setMaximum(int i) {
		spinner.setMaximum(i);
	}
	/**
	 * @param i
	 */
	public void setMinimum(int i) {
		spinner.setMinimum(i);
	}

	public void setDigits(int i) {
		spinner.setDigits(i);
	}

	public void setIncrement(int i) {
		spinner.setIncrement(i);
	}

	public int getMaximum() {
		return spinner.getMaximum();
	}
	public int getMinimum() {
		return spinner.getMinimum();
	}
	
	/*******************************************************************/

}

	