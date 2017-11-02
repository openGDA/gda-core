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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.JythonServerFacade;
/**
 * @deprecated replaced by {@link StopAllHandler}
 * kept only for backward compatibility only, will be removed in future release.
 */
@Deprecated
public class BeamlineHaltHandler extends AbstractHandler {

	public static final String id = "uk.ac.gda.client.StopAll";
	private static final Logger logger = LoggerFactory.getLogger(BeamlineHaltHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			logger.debug("Stop All button pressed");
			Job job = new Job("Stop All Commands and Hardware...") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					JythonServerFacade.getInstance().beamlineHalt();
					return Status.OK_STATUS;
				}
			};
			job.setUser(false);
			job.schedule();

			return Boolean.TRUE;
		} catch (Exception ne) {
			throw new ExecutionException("Error running beamlineHalt", ne);
		}
	}

}
