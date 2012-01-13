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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

import swing2swt.layout.BorderLayout;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;

/**
 * This field edits a choice and an optional number. It is
 * for field introduced to the XML which are of the form:
 *   string <number>
 *   
 *   Where string is a choice and number is an optional 
 *   extra piece of information. Currently the values
 *   are separated by a space. 
 */
public class ComboAndNumberWrapper extends ComboWrapper {
	
	private final ScaleBox     secondValue;
	private final List<String> visibleChoices;
	private SelectionAdapter selectionListener;

	/**
	 * @param parent
	 * @param style
	 * @param visibleChoices 
	 */
	public ComboAndNumberWrapper(Composite parent, int style, final List<String> visibleChoices) {
		super(parent, style);
		
		this.visibleChoices = visibleChoices;
		this.secondValue    = new ScaleBox(this, SWT.NONE);
		secondValue.on();
		secondValue.setActive(false);
		
		secondValue.setLayoutData(BorderLayout.SOUTH);
		this.selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (visibleChoices!=null) {
					if (visibleChoices.contains(combo.getItem(combo.getSelectionIndex()))) {
						secondValue.setActive(true);
					} else if (itemMap!=null && visibleChoices.contains(itemMap.get(combo.getItem(combo.getSelectionIndex())))){
						secondValue.setActive(true);
					} else {
						secondValue.setActive(false);
				    }
				} else {
					secondValue.setActive(true);
				}
			}
		};
		combo.addSelectionListener(selectionListener);
	}
	
	@Override
	public void dispose() {
		combo.removeSelectionListener(selectionListener);
		super.dispose();
	}
	
	@Override
	public Object getValue() {
		final String t = (String)super.getValue();
		
		if (visibleChoices.contains(t)) {
			return t+" "+secondValue.getValue();
		} 
		return t;
	}
	
	@Override
	public void setValue(final Object value) {
		if (value==null) {
			super.setValue(value);
			secondValue.setVisible(false);
			return;
		}
		
		final String txt = (String)value;
		if (txt.indexOf(' ')>-1) {
			final String[] vals = txt.split(" ");
			super.setValue(vals[0]);
			secondValue.setVisible(true);
			secondValue.setValue(vals[1]);
		} else {
			secondValue.setVisible(false);
			super.setValue(value);
		}
	}
	
	/**
	 * @return the ScaleBox editing the second value.
	 */
	public ScaleBox getValueField() {
		return this.secondValue;
	}
	

}
