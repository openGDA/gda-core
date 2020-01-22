/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.client;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import uk.ac.gda.views.baton.dialogs.PassBatonDialog;

public class PassBatonCommandQueueHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(PassBatonCommandQueueHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (InterfaceProvider.getBatonStateProvider().amIBatonHolder()) {
			try {
				logger.debug("Pass baton to another client");
				PassBatonDialog dlg = new PassBatonDialog(Display.getCurrent().getActiveShell());
				dlg.open();
				if (dlg.getBatonReceiverUsername() != null) {
					CommandQueueViewFactory.getProcessor().passBaton(
							dlg.getBatonReceiverUsername(),
							dlg.getBatonReceiverIndex()
							);
					return Boolean.TRUE;
				} else {
					logger.info("User cancelled queuing baton pass action");
					return Boolean.TRUE;
				}
			} catch (Exception ne) {
				throw new ExecutionException("Error running passBaton", ne);
			}
		} else {
			logger.warn("You cannot insert a baton pass in the queue as you do not hold the baton");
			return Boolean.FALSE;
		}
	}

}
