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

package uk.ac.gda.views.baton.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;

import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * Take the Baton
 */
public class TakeAction extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (!InterfaceProvider.getBatonStateProvider().requestBaton()) {
			final boolean batonHolderIsAutomatedClient =
					InterfaceProvider.getBatonStateProvider().getBatonHolder().isAutomatedUser();

			MessageBox messageBox = new MessageBox(
					HandlerUtil.getActiveShellChecked(event),
					SWT.OK | SWT.ICON_WARNING);

			if (batonHolderIsAutomatedClient) {
				messageBox.setMessage("You have requested the baton from an automated client.\n\n"
						+ "The automated client is finishing the current instruction, after which "
						+ "you will be assigned the baton automatically. "
						+ "Thank you for your patience.");
				messageBox.setText("Baton requested by human user");
				messageBox.open();
			} else {
				messageBox.setMessage("You do not have enough authorisation to take the baton from"
						+ " the current holder.\n\nThe current holder is aware of your request and "
						+ "will normally release within two minutes.");
				messageBox.setText("Baton requested");
				messageBox.open();
			}
		}
		Async.submit(RefreshBatonAction::refresh);
		return null;
	}

}
