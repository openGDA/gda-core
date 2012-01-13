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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import swing2swt.layout.BorderLayout;

/**
 *
 */
public abstract class ButtonComposite extends FieldComposite {


	protected SelectionListener buttonSelection;
	protected Button button;

	/**
	 * @param parent
	 * @param style
	 */
	public ButtonComposite(Composite parent, int style) {
		super(parent, style);
	}
	
	
	/** NOTE Just used to make RCP Developer recognise bean **/
	private boolean buttonVisible = false;
	
	/**
	 * Called to show scale button.
	 * @param vis
	 */
	public void setButtonVisible(final boolean vis) {
		buttonVisible = vis;
		if (button!=null) button.setVisible(vis);
		
		if (vis) {
			createButton();
			button.addSelectionListener(buttonSelection);
		} else {
			if (buttonSelection!=null) button.removeSelectionListener(buttonSelection);
			buttonSelection = null;
		}
	}
	
	/**
	 * Returns true is scale button showing.
	 * @return true/false
	 */
	public boolean isButtonVisible() {
		return buttonVisible;
	}
	
	/**
	 * Called to add a scale button listener which will be run when the scale button is pressed.
	 * @param l 
	 */
	public void addButtonListener(final SelectionListener l) {
		if (buttonSelection!=null) button.removeSelectionListener(buttonSelection);
		buttonSelection = l;
		if (!buttonVisible) {
			createButton();
			buttonVisible = true;
			button.setVisible(true);
		}
		if (button != null) button.addSelectionListener(buttonSelection);
	}
	
	@Override
	public void dispose() {
		if (button!=null&&buttonSelection!=null) {
			button.removeSelectionListener(buttonSelection);
			button.dispose();
		}
		super.dispose();
	}
	
	protected void createButton() {
		
		if(this.button!=null) return;
		
		this.button = new Button(this, SWT.PUSH) {
			@Override
			protected void checkSubclass () {
			}
			@Override
			public boolean forceFocus(){
				return false;
			}
			@Override
			public boolean isFocusControl() {
				return false;
			}
			@Override
			public boolean setFocus() {
				return false;
			}
		};
		
		// Button not used normally.
		button.setText("..");
		if (getLayout() instanceof BorderLayout) {
		    button.setLayoutData(BorderLayout.EAST);
		} else if (getLayout() instanceof GridLayout){
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		}
		layout();
	}

}
