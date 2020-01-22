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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;

public class StopCommandQueueHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(StopCommandQueueHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (InterfaceProvider.getBatonStateProvider().amIBatonHolder()) {
			try {
				logger.debug("Stop queue button pressed");
				CommandQueueViewFactory.getProcessor().stop(1000);
				return Boolean.TRUE;
			} catch (Exception ne) {
				throw new ExecutionException("Error running StopCommand", ne);
			}
		} else {
			logger.warn("You cannot stop the queue as you do not hold the baton");
			return Boolean.FALSE;
		}
	}

}
