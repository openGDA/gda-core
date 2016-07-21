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

import gda.jython.JythonServerFacade;
import gda.observable.IObserver;

/**
 * Implementation of {@link IConsumerProcess} for running a {@link Command}. This
 * allows a command for the old command queue to be run on the new GDA9 queue.
 */
public class CommandProcess extends AbstractPausableProcess<CommandBean> {

	private static final Logger logger = LoggerFactory.getLogger(CommandProcess.class);

	private final Command command;

	protected CommandProcess(CommandBean bean, IPublisher<CommandBean> publisher) {
		super(bean, publisher);
		command = bean.getCommand();
		setBeanStatus(Status.QUEUED);
		broadcast(bean);
	}

	private Status getPreRequestStatus() {
		// when doPause, doResume or doTerminate are called the current bean status is expected
		// to be a request status, e.g. REQUEST_PAUSE. In this case, we need to get the
		// previous state of the bean to see if we actually are currently paused or terminated, etc.
		Status status = bean.getStatus();
		if (status.isRequest()) {
			status = bean.getPreviousStatus();
		}

		return status;
	}

	@Override
	protected void doPause() throws Exception {
		Status status = getPreRequestStatus();
		if (status.isFinal() || status.isPaused()) {
			logger.error("Cannot pause scan {}, status is {}", bean.getName(), bean.getStatus());
		} else {
			command.pause();
		}
	}

	@Override
	protected void doResume() throws Exception {
		Status status = getPreRequestStatus();
		if (!status.isPaused()) {
			logger.error("Cannot resume scan {}, status is {}", bean.getName(), bean.getStatus());
		} else {
			command.resume();
		}
	}

	@Override
	protected void doTerminate() throws Exception {
		Status status = getPreRequestStatus();
		if (status.isFinal()) {
			logger.error("Cannot terminate scan {}, status is {}", bean.getName(), bean.getStatus());
		} else {
			JythonServerFacade.getInstance().requestFinishEarly();
		}
	}

	private void setBeanStatus(Status status) {
		bean.setPreviousStatus(bean.getStatus());
		bean.setStatus(status);
	}

	private void updateProgress(CommandProgress progress) {
		bean.setPercentComplete(progress.getPercentDone());
		broadcast(bean);
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		// an IObserver to listen for progress, i.e. percentComplete
		final IObserver progressObserver = new IObserver() {

			@Override
			public void update(Object source, Object arg) {
				if (arg instanceof CommandProgress) {
					updateProgress((CommandProgress) arg);
				}
			}
		};

		try {
			command.addIObserver(progressObserver);

			// broadcast the bean for the start of the scan
			setBeanStatus(Status.RUNNING);
			bean.setPercentComplete(0);
			broadcast(bean);

			// run the command - this is the method that performs the scan
			command.run();

			// broadcast the bean for the end of the scan
			if (!bean.getStatus().isTerminated()) {
				setBeanStatus(Status.COMPLETE);
				bean.setPercentComplete(100);
				broadcast(bean);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Cannot execute command " + command);

			// broadcast the bean for a failed scan
			setBeanStatus(Status.FAILED);
			bean.setPercentComplete(100);
			bean.setMessage(e.getMessage());
			broadcast(bean);

			throw new EventException(e);
		} finally {
			command.deleteIObserver(progressObserver);
		}
	}

}
