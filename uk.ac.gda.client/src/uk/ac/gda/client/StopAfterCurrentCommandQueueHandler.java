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

package uk.ac.gda.client;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopAfterCurrentCommandQueueHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(StopAfterCurrentCommandQueueHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			logger.debug("Stop after current scan button pressed");
			CommandQueueViewFactory.processor.stopAfterCurrent();
			return Boolean.TRUE;
		} catch (Exception ne) {
			throw new ExecutionException("Error running StopAfterCurrentCommand", ne);
		}
	}

}
