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

package gda.rcp.ncd.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SaveDataDialog extends Dialog {

	private static final int SAVE_ID = 4096;
	private static final int DISCARD_ID = 4097;
	private static final int CONTINUE_ID = 4098;
	
	private boolean saveRequired = false;
	private boolean clearRequired = false;

	/**
	 * Create the dialog
	 * @param parentShell
	 */
	public SaveDataDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginLeft = 50;
		layout.marginHeight = 15;
		composite.setLayout(layout);
		Text text = new Text(composite, SWT.MULTI | SWT.CENTER);
		text.setText("You have not saved the data\nDo you want to output?");
		text.setBackground(parent.getBackground());
		return parent;		
	}
	
	/**
	 * Create contents of the button bar
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		
		createButton(parent, SAVE_ID, "Save", true);
		createButton(parent, DISCARD_ID, "Discard", false);
		createButton(parent, CONTINUE_ID, "Continue", false);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (SAVE_ID == buttonId) {
			saveRequired = true;
			clearRequired = false;
			setReturnCode(SAVE_ID);
		} else if (DISCARD_ID == buttonId) {
			saveRequired = false;
			clearRequired = true;
			setReturnCode(DISCARD_ID);
		} else {
			saveRequired = false;
			clearRequired = false;
			setReturnCode(CONTINUE_ID);
		}
		close();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Save Data Warning");
	}
	
	/**
	 * @return true if save is required
	 */
	public boolean isSaveRequired() {
		return saveRequired;
	}

	/**
	 * @return true if clear is required
	 */
	public boolean isClearRequired() {
		return clearRequired;
	}
	
}
