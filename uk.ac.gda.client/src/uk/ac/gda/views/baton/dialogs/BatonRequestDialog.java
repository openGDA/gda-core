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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.BatonRequested;
import gda.jython.batoncontrol.ClientDetails;
import gda.rcp.GDAClientActivator;
import uk.ac.gda.preferences.PreferenceConstants;
import uk.ac.gda.views.baton.BatonView;

public class BatonRequestDialog extends Dialog {
	private static final Logger logger = LoggerFactory.getLogger(BatonRequestDialog.class);
	private Label lblARequestFor;
	private ClientDetails request;
	private boolean open = true;

	public static void doPassBaton(Shell shell, final BatonRequested request) {
		final boolean keepBaton = GDAClientActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.KEEP_BATON);
		BatonRequestDialog.openPassBatonForm(shell, request.getRequester(), keepBaton);
	}

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	private BatonRequestDialog(Shell parentShell, final ClientDetails request) {
		super(parentShell);
		this.request = request;
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		getShell().setText(String.format("%s has requested the baton", request.getUserID()));
		Composite container = (Composite) super.createDialogArea(parent);
		{
			lblARequestFor = new Label(container, SWT.NONE);
			String message = String.format("The user '%s' (%s) has requested the baton.  %n%n" +
					"Would you like to pass control to them?", request.getFullName(), request.getUserID());
			lblARequestFor.setText(message);
		}

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Yes", true);
		createButton(parent, IDialogConstants.CANCEL_ID, "No", false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 177);
	}

	public static void openPassBatonForm(final Shell shell, final ClientDetails request, boolean batonAlwaysHeld) {

		final BatonRequestDialog currentOpenDialog = new BatonRequestDialog(shell, request);
		logger.debug("Baton request from {}, KEEP_BATON set to {}", request.getUserID(), batonAlwaysHeld);

		if (!batonAlwaysHeld) { // We close it in 2 minutes.
			final int minutes = GDAClientActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.BATON_REQUEST_TIMEOUT);
			logger.debug("Waiting {} minutes before automatically releasing baton", minutes);

			Job job = new Job("Automatically accepting baton change.") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {

					if (!currentOpenDialog.isOpen()) {
						logger.trace("Baton request dialog had already been closed at end of timeout");
						return new Status(IStatus.OK, BatonView.ID, "Choosing ok for the user.");
					}
					try {
						final Shell shell = currentOpenDialog.getShell();
						if (shell == null) {
							return new Status(IStatus.OK, BatonView.ID, "Choosing ok for the user.");
						}
						logger.debug("Baton request timed out. Automatically releasing baton");
						shell.getDisplay().asyncExec(shell::dispose);
						return new Status(IStatus.OK, BatonView.ID, "Choosing ok for the user.");
					} catch (Exception ne) {
						logger.error("Cannot process dialog kill", ne);
						return new Status(IStatus.ERROR, BatonView.ID, "Choosing ok for the user.");
					}
				}
			};
			job.setUser(false);
			job.setSystem(true);
			job.setThread(Display.getDefault().getThread());

			job.schedule(minutes*60*1000L);// 2 minutes.
		}

		final int returnCode = currentOpenDialog.open();
		currentOpenDialog.setOpen(false);
		logger.info("Baton request from {}@{} was {}",
				request.getUserID(),
				request.getHostname(),
				returnCode == OK ? "accepted" : "denied"); // NOSONAR
		doPass(request, returnCode == OK);

	}

	private static void doPass(final ClientDetails request, boolean ok) {
		if (ok) {
			int batonHolderIndex = InterfaceProvider.getBatonStateProvider().getBatonHolder().getIndex();
			InterfaceProvider.getBatonStateProvider().assignBaton(request.getIndex(), batonHolderIndex);
		} else {
			InterfaceProvider.getBatonStateProvider().sendMessage("Baton request denied.");
		}
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
}
