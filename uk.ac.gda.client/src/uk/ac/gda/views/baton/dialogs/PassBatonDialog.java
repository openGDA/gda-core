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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;
import gda.jython.batoncontrol.ClientInfo;

public class PassBatonDialog extends Dialog {

	private int batonReceiverIndex;
	private String batonReceiverUsername;

	/**
	 * Create the dialog.
	 *
	 * @param parentShell
	 */
	public PassBatonDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.RESIZE | SWT.TITLE);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Select a client to receive the baton");
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		return container;
	}

	/**
	 * Create contents of the button bar.
	 *
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button cancelButton = createButton(parent, IDialogConstants.OK_ID, "Cancel", true);
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// leave null if cancelling operation.
			}
		});
		final ClientDetails[] clients = InterfaceProvider.getBatonStateProvider().getOtherClientInformation();
		for (ClientInfo client : clients) {
			String label = client.getUserID() + " #" + client.getIndex();
			Button okButton = createButton(parent, IDialogConstants.OK_ID, label, true);
			okButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setBatonReceiverIndex(client.getIndex());
					setBatonReceiverUsername(client.getUserID());
				}
			});
		}
	}

	public int getBatonReceiverIndex() {
		return batonReceiverIndex;
	}

	public void setBatonReceiverIndex(int batonReceiverIndex) {
		this.batonReceiverIndex = batonReceiverIndex;
	}

	public String getBatonReceiverUsername() {
		return batonReceiverUsername;
	}

	public void setBatonReceiverUsername(String batonReceiverUsername) {
		this.batonReceiverUsername = batonReceiverUsername;
	}

}
