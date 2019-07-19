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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import gda.jython.InterfaceProvider;

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

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Switch User");
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public String getUserName() {
		return username;
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));

		final Label lblUsername = new Label(container, SWT.NONE);
		lblUsername.setText("User name");

		final Text uname = new Text(container, SWT.BORDER);
		uname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		uname.addModifyListener(e -> username = uname.getText());

		final Label lblPassword = new Label(container, SWT.NONE);
		lblPassword.setText("Password");

		final Text pswd = new Text(container, SWT.BORDER);
		pswd.setEchoChar('*');
		pswd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		pswd.addModifyListener(e -> password = pswd.getText());

		return container;
	}

	/**
	 * Create contents of the button bar.
	 *
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleOkSelected(e);
			}
		});
		Button cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleCancelSelected(e);
			}
		});
	}

	protected void handleOkSelected(@SuppressWarnings("unused") SelectionEvent e) {

		if (username == null || password == null) {
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

	protected void handleCancelSelected(@SuppressWarnings("unused") SelectionEvent e) {
		authenticated = false;
	}
}
