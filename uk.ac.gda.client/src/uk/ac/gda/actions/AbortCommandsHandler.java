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

package uk.ac.gda.actions;

import gda.jython.JythonServerFacade;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbortCommandsHandler extends AbstractHandler {

	public static String id = "uk.ac.gda.actions.AbortCommandsHandler";
	private static final Logger logger = LoggerFactory.getLogger(AbortCommandsHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			logger.debug("Abort All Commands button pressed");
			Job job = new Job("Abort Commands...") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					JythonServerFacade.getInstance().abortCommands();
					return Status.OK_STATUS;
				}
			};
			job.setUser(false);
			job.schedule();

			return Boolean.TRUE;
		} catch (Exception ne) {
			throw new ExecutionException("Error running abortCommands", ne);
		}
	}

}
