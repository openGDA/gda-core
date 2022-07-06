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

package uk.ac.gda.ui.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * You can use this class for low level dialogs in the GDA.
 * Alternative - use the JFace Dialog class.
 */
public abstract class OKCancelDialog extends Dialog {
	
	protected boolean ok = false;
	protected Object  currentSelection;
	private SelectionAdapter okListener;
	private Button okButton;
	private Button cancelButton;
	private SelectionAdapter cancelListener;
	/**
	 * @param parent
	 * @param style
	 */
	public OKCancelDialog (Shell parent, int style) {
		super (parent, style);
	}
	
	/**
	 * Opens the dialog and returns the value, null if the user did not press ok.
	 * @return the object chosen.
	 */
	public Object open() {
		return open(null);
	}
	/**
	 * @param userData (may be null) 
	 * @return the selected file
	 */
	public Object open(final Object userData) {
	
	
		final Shell parent = getParent();
		final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		shell.setText(getText());

		createUserUI(shell, userData);

		final Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new RowLayout());
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		this.okButton = new Button(composite, SWT.NONE);
		okButton.setText("OK");
		this.okListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				OKCancelDialog.this.ok = true;
				shell.dispose();
			}			
		};
		okButton.addSelectionListener(okListener);

		this.cancelButton = new Button(composite, SWT.NONE);
		cancelButton.setText("Cancel");
		this.cancelListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				OKCancelDialog.this.ok = false;
				shell.dispose();
			}			
		};
		cancelButton.addSelectionListener(cancelListener);
		
		OKCancelDialog.this.ok = false;
		shell.setSize(400, 300);
		shell.open();
		
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		
		if (OKCancelDialog.this.ok) {
			return currentSelection;
		}
			
		return null;

	}
	
	/**
	 * Please implement to create the GUI by adding UI to the shell. The shell
	 * already has a grid layout.
	 * @param shell 
	 * @param userObject
	 */
    public abstract void createUserUI(final Shell shell, final Object userObject);
    
    public void dispose() {
    	okButton.removeSelectionListener(okListener);
    	cancelButton.removeSelectionListener(cancelListener);
    }
}
