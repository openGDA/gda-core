/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.commandqueue;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.AbstractPausableProcess;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link IConsumerProcess} for running a {@link Command}. This
 * allows a command for the old command queue to be run on the new GDA9 queue.
 */
public class CommandProcess extends AbstractPausableProcess<CommandBean> {

	private static final Logger logger = LoggerFactory.getLogger(CommandProcess.class);

	protected CommandProcess(CommandBean bean, IPublisher<CommandBean> publisher) {
		super(bean, publisher);
	}

	@Override
	protected void doPause() throws Exception {
		bean.getCommand().pause();
	}

	@Override
	protected void doResume() throws Exception {
		bean.getCommand().resume();
	}

	@Override
	protected void doTerminate() throws Exception {
		bean.getCommand().abort();
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		final Command command = bean.getCommand();
		try {
			command.run();
			bean.setPreviousStatus(Status.RUNNING);
			bean.setStatus(Status.COMPLETE);
			bean.setPercentComplete(100);
			broadcast(bean);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Cannot execute command " + command);
			bean.setPreviousStatus(Status.RUNNING);
			bean.setStatus(Status.FAILED);
			bean.setMessage(e.getMessage());
			broadcast(bean);

			throw new EventException(e);
		}
	}

}
