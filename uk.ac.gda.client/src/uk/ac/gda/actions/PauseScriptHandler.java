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

package uk.ac.gda.actions;

import gda.jython.Jython;
import gda.jython.JythonServerFacade;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PauseScriptHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(PauseScriptHandler.class);

	/**
	 * Returns if the button should be checked (ie something was pause), true or
	 * if there was nothing to pause or a resume happened then false.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {

			logger.debug("Pause/Resume Script button pressed");

			final JythonServerFacade facade = JythonServerFacade.getCurrentInstance();

			if (facade.getScriptStatus()==Jython.IDLE) {
				return Boolean.FALSE;
			}
			if (facade.getScriptStatus()!=Jython.PAUSED) {
				facade.pauseCurrentScript();
				return Boolean.TRUE;
			}

			facade.resumeCurrentScript();
			return Boolean.FALSE;

		} catch (Exception ne) {
			throw new ExecutionException("Error pausing/resuming script", ne);
		}
	}

}
