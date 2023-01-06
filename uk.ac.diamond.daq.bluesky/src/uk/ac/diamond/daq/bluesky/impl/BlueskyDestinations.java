/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.bluesky.impl;

import javax.jms.Queue;
import javax.jms.Topic;

/**
 * Queues and topics for interacting with Bluesky
 */
public final class BlueskyDestinations {
	public static final Queue WORKER_RUN = () -> "worker.run";
	public static final Queue WORKER_PLANS = () -> "worker.plans";
	public static final Queue WORKER_DEVICES = () -> "worker.devices";
	public static final Topic WORKER_EVENT_TASK = () -> "worker.event.task";
}
