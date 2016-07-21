/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.logpanel.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;

import uk.ac.gda.client.logpanel.view.Logpanel;
import uk.ac.gda.client.logpanel.view.LogpanelView;

public class ClearHandler extends AbstractHandler implements IHandler {

	private static final boolean askForConfirmation = true; //TODO set from user preference

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Logpanel logpanel = ((LogpanelView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().findView(LogpanelView.ID)).getLogpanel();
		boolean clear = true;
		if (askForConfirmation) {
			MessageBox dialog = new MessageBox(logpanel.getShell(), SWT.ICON_QUESTION | SWT.OK| SWT.CANCEL);
			dialog.setText("Confirm Clear Logpanel");
			dialog.setMessage("This will remove all log messages from this panel \nand cannot be undone.");
			int returnCode = dialog.open();
			// wait for response; one of {OK, Cancel, Close}
			clear = (returnCode == SWT.OK);
		}
		if (clear) {
			logpanel.clear();
		}
		return null;
	}

}
