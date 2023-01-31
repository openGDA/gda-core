/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.CommandQueueViewFactory;

public class QueueAndRunExperimentCommandHandler extends RunExperimentCommandHandler {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(QueueAndRunExperimentCommandHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		setCancelAll(false);
		if (event.getCommand().getId()
				.equals("uk.ac.gda.client.experimentdefinition.QueueAndRunMultiExperimentCommand")) {
			queueMultiScan();

		} else if (event.getCommand().getId()
				.equals("uk.ac.gda.client.experimentdefinition.QueueAndRunSingleExperimentCommand")) {

			queueSingleScan();

		} else if (event.getCommand().getId()
				.equals("uk.ac.gda.client.experimentdefinition.QueueAndRunSingleScanOnlyCommand")) {

			queueSingleScanSingleRepetition();
		}

		startQueue();

		return null;

	}

	private void startQueue() {
		try {
			CommandQueueViewFactory.getProcessor().start(100000);
		} catch (Exception e) {
			logger.error("Exception trying to resume the Command Queue", e);
		}

	}

}
