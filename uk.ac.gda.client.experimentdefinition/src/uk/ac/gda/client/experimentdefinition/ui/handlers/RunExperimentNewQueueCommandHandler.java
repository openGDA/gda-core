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

import java.util.stream.Collectors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.event.ui.view.StatusQueueView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.commandqueue.Command;
import gda.commandqueue.CommandBean;
import gda.commandqueue.CommandQueue;
import gda.commandqueue.ExperimentCommandBean;
import uk.ac.gda.client.CommandQueueView;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;

/**
 * Extends {@link RunExperimentCommandHandler} to override {@link #submitCommandToQueue(ExperimentCommandProvider)}
 * to place an {@link ExperimentCommandBean} on the new ActiveMQ based queue (so that it is
 * shown in the {@link StatusQueueView}), instead of the old {@link CommandQueue} (shown in the
 * {@link CommandQueueView}).
 */
public class RunExperimentNewQueueCommandHandler extends RunExperimentCommandHandler {
	private static final Logger logger = LoggerFactory.getLogger(RunExperimentNewQueueCommandHandler.class);

	private CommandBeanSubmitter submitter = null;

	@Override
	protected void submitCommandToQueue(ExperimentCommandProvider commandProvider) throws ExecutionException {
		// create the command bean
		final Command command = commandProvider.getCommand();
		CommandBean bean = createCommandBean(command);

		// submit the bean
		try {
			getSubmitter().submitScan(bean);
		} catch (EventException e) {
			logger.error("Exception adding ExperimentCommandProvider to queue", e);
			throw new ExecutionException("Exception adding ExperimentCommandProvider to queue.", e);
		}
	}

	private CommandBeanSubmitter getSubmitter() {
		if (submitter == null) {
			submitter = new CommandBeanSubmitter();
			submitter.init();
		}

		return submitter;
	}

	private CommandBean createCommandBean(final Command command) {
		ExperimentCommandBean bean = new ExperimentCommandBean();
		bean.setCommand(command);
		String description = "Unknown command";
		try {
			description = command.getDescription();
		} catch (Exception e) {
			logger.error("Error getting description of command", e);
		}
		bean.setName(description);
		bean.setCommand(command);

		// The experiment object doesn't get serialized (as it's not easily serialisable to json)
		// so instead we put the important properties into the bean
		IExperimentObject experimentObj = ((ExperimentCommand) command).getExperimentObject();
		bean.setFolderPath(experimentObj.getFolder().getFullPath().toString());
		bean.setMultiScanName(experimentObj.getMultiScanName());
		bean.setRunName(experimentObj.getRunName());
		bean.setFiles(experimentObj.getFiles().stream().map(
				file -> file.getFullPath().toString()).collect(Collectors.toList()));
		return bean;
	}

}
