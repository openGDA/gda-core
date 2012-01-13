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
 *
 * There is a jface Dialog which should be used instead
 * as it does almost the same thing as this Dialog.
 */
public abstract class GenericDialog extends Dialog {
	
	protected boolean ok = false;
	protected Object  currentSelection;
	protected PasswordChecker checker;
    /**
     * @param checker
     */
    public void setChecker(PasswordChecker checker) {
		this.checker = checker;
	}

	/**
	 * @param parent
	 * @param style
	 */
	public GenericDialog (Shell parent, int style) {
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

		createContents(shell, userData);

		final Composite buttons = new Composite(shell, SWT.NONE);
		buttons.setLayout(new RowLayout());
		buttons.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, true));

		final Button ok = new Button(buttons, SWT.NONE);
		ok.setText("OK");
		ok.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				GenericDialog.this.ok = true;
				if (checker==null) {
				    shell.dispose();
				} else if (checker.isValid()) {
				    shell.dispose();
				}
			}			
		});
		ok.setFocus();

		final Button cancel = new Button(buttons, SWT.NONE);
		cancel.setText("Cancel");
		cancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				GenericDialog.this.ok = false;
				shell.dispose();
			}			
		});
		
		GenericDialog.this.ok = false;
		if (shouldPack()) shell.pack();
		shell.open();
		
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		
		if (GenericDialog.this.ok) {
			return currentSelection;
		}
		
		return null;

	}	
	
	/**
	 * You may override this method, default is false.
	 * @return if should pack
	 */
	public boolean shouldPack() {
		return false;
	}

	/**
	 * Please implement to create the GUI by adding UI to the shell. The shell
	 * already has a grid layout.
	 * @param shell currentSelection
	 * @param userObject
	 */
    public abstract void createContents(final Shell shell, final Object userObject);
	
	/**
	 * Please implment this interface for checking of user name and
	 * password prior to closing the form.
	 */
    public interface PasswordChecker {
		boolean isValid();
	}

}
