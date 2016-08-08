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

package uk.ac.gda.client.experimentdefinition.ui.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scanning.api.event.EventException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.commandqueue.Command;
import gda.commandqueue.CommandBean;

public class RunExperimentNewQueueCommandHandler extends RunExperimentCommandHandler {
	private static final Logger logger = LoggerFactory.getLogger(RunExperimentNewQueueCommandHandler.class);

	private CommandBeanSubmitter submitter;

	@Override
	protected void submitCommandToQueue(ExperimentCommandProvider commandProvider) throws ExecutionException {
		final Command command = commandProvider.getCommand();

		if (submitter == null) {
			submitter = new CommandBeanSubmitter();
			submitter.init();
		}

		// create the command bean
		CommandBean bean = new CommandBean();
		bean.setCommand(command);
		String description = "Unknown command";
		try {
			description = command.getDescription();
		} catch (Exception e) {
			logger.error("Error getting description of command", e);
		}
		bean.setName(description);
		bean.setCommand(command);
		// TODO set other fields?

		// submit the bean
		try {
			submitter.submitScan(bean);
		} catch (EventException e) {
			logger.error("Exception adding ExperimentCommandProvider to queue." + e.getMessage());
			throw new ExecutionException("Exception adding ExperimentCommandProvider to queue.", e);
		}
	}

}
