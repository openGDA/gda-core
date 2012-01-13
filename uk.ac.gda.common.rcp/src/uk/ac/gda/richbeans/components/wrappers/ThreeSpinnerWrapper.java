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
import org.eclipse.swt.layout.GridLayout;
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
public class ThreeSpinnerWrapper extends ButtonComposite implements IFieldWidget {

    protected Spinner spinner1;
    protected Spinner spinner2;
    protected Spinner spinner3;
	private ModifyListener modifyListener;
	/**
	 * 
	 * @param parent
	 * @param style
	 */
	public ThreeSpinnerWrapper(Composite parent, int style) {
		
		super(parent, SWT.NONE);
		setLayout(new BorderLayout());
		
		final Composite spinnerContainer = new Composite(this, SWT.NONE);
		spinnerContainer.setLayoutData(BorderLayout.CENTER);
		mainControl = spinner1;
		
		this.modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				final ValueEvent evt = new ValueEvent(spinner1,getFieldName());
				evt.setValue(getValue());
				eventDelegate.notifyValueListeners(evt);
			}
		};
		
		GridLayout gridLayout = new GridLayout(3,true);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		spinnerContainer.setLayout(gridLayout);
		this.spinner1 = new Spinner(spinnerContainer, SWT.BORDER|style);
		spinner1.addModifyListener(modifyListener);
		
		this.spinner2 = new Spinner(spinnerContainer, SWT.BORDER|style);
		spinner2.addModifyListener(modifyListener);
		
		this.spinner3 = new Spinner(spinnerContainer, SWT.BORDER|style);
		spinner3.addModifyListener(modifyListener);
	}
	
	@Override
	public void dispose() {
		if (spinner1!=null&&!spinner1.isDisposed()) spinner1.removeModifyListener(modifyListener);
		if (spinner2!=null&&!spinner2.isDisposed()) spinner2.removeModifyListener(modifyListener);
		if (spinner3!=null&&!spinner3.isDisposed()) spinner3.removeModifyListener(modifyListener);
		super.dispose();
	}

	@Override
	public Object getValue() {
		return new int[]{spinner1.getSelection(),spinner2.getSelection(),spinner3.getSelection()}; // Integer
	}
	
	@Override
	public void setValue(final Object value) {
		if (value==null) {
			spinner1.setSelection(spinner1.getMinimum());
			spinner2.setSelection(spinner2.getMinimum());
			spinner3.setSelection(spinner3.getMinimum());
			return;
		}
		
		final int[] ia = (int[])value;
		spinner1.setSelection(ia[0]);
		spinner2.setSelection(ia[1]);
		spinner3.setSelection(ia[2]);
		
		if (this.notifyType!=null&&(notifyType==NOTIFY_TYPE.ALWAYS||notifyType==NOTIFY_TYPE.VALUE_CHANGED)) {
			final ValueEvent evt = new ValueEvent(this,getFieldName());
			evt.setValue(getValue());
			eventDelegate.notifyValueListeners(evt);
		}
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
		spinner1.setVisible(active);
		spinner2.setVisible(active);
		spinner3.setVisible(active);
	}
	
	/**
	 * @param i
	 */
	public void setMaximum1(int i) {
		spinner1.setMaximum(i);
	}
	/**
	 * @param i
	 */
	public void setMinimum1(int i) {
		spinner1.setMinimum(i);
	}
	
	/**
	 * @param i
	 */
	public void setMaximum2(int i) {
		spinner2.setMaximum(i);
	}
	/**
	 * @param i
	 */
	public void setMinimum2(int i) {
		spinner2.setMinimum(i);
	}
	
	/**
	 * @param i
	 */
	public void setMaximum3(int i) {
		spinner3.setMaximum(i);
	}
	/**
	 * @param i
	 */
	public void setMinimum3(int i) {
		spinner3.setMinimum(i);
	}
	/*******************************************************************/

}

	