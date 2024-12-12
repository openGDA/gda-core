/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.core.sampletransfer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientAction extends AbstractStepAction {
	private static final Logger logger = LoggerFactory.getLogger(ClientAction.class);

	private CountDownLatch confirmationLatch;

	public ClientAction(String description) {
		super(description);
	}

	@Override
	public void execute(StepContext context) throws InterruptedException {
		confirmationLatch = new CountDownLatch(1);
		try {
			boolean confirmed = confirmationLatch.await(1, TimeUnit.HOURS);
			if (!confirmed) {
				throw new InterruptedException();
			}
		} catch (InterruptedException e) {
			logger.debug("Thread interrupted during await, stopping execution");
			Thread.currentThread().interrupt();
			throw e;
		}
	}
	@Override
	public void resume() {
		if (confirmationLatch != null) {
			confirmationLatch.countDown();
		}
	}

	@Override
	public void terminate() {
		logger.info("Terminate called on ClientAction.");
		resume();
	}
}
