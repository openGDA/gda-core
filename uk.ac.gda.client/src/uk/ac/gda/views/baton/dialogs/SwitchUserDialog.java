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

package uk.ac.gda.views.baton.dialogs;

import gda.jython.InterfaceProvider;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 *
 */
public class SwitchUserDialog extends Dialog {

	private String  username;
	private String  password;
	private boolean authenticated;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public SwitchUserDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.RESIZE | SWT.TITLE);
	}

	/**
	 * @return authenticated
	 */
	public boolean isAuthenticated() {
		return authenticated;
	}
	
	/**
	 * @return user name
	 */
	public String getUserName() {
		return username;
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		
		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		{
			Label lblUsername = new Label(container, SWT.NONE);
			lblUsername.setText("User name");
		}
		{
			final Text uname = new Text(container, SWT.BORDER);
			uname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			uname.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					username = uname.getText();
				}
			});
		}
		{
			Label lblPassword = new Label(container, SWT.NONE);
			lblPassword.setText("Password");
		}
		{
			final Text pswd = new Text(container, SWT.BORDER);
			pswd.setEchoChar('*');
			pswd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			pswd.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					password = pswd.getText();
				}
			});
		}

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				do_button_widgetSelected(e);
			}
		});
		Button button_1 = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				do_button_1_widgetSelected(e);
			}
		});
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(357, 163);
	}

	protected void do_button_widgetSelected(@SuppressWarnings("unused") SelectionEvent e) {
		
		if (username==null||password==null) {
			authenticated = false;
		} else {
		    authenticated = InterfaceProvider.getBatonStateProvider().switchUser(username, password);
		}
		
		if (!authenticated) {
			MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					                     "Authentication Failed",
					                      "Could not switch user to '"+username+"'.\n\nThe user name or password may be incorrect.");
		}
	}

	protected void do_button_1_widgetSelected(@SuppressWarnings("unused") SelectionEvent e) {
		authenticated = false;
	}
}
