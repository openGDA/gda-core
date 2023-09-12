/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package org.eclipse.scanning.event.ui.view;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.WatchdogStatusRecord;
import org.eclipse.scanning.api.event.status.WatchdogStatusRecord.WatchdogState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class WatchdogWatcher {
	private static final Logger logger = LoggerFactory.getLogger(WatchdogWatcher.class);

	private Map<String, WatchdogStatusRecord> watchdogs;
	private ISubscriber<IBeanListener<WatchdogStatusRecord>> watchdogTopicSubscriber;

	public WatchdogWatcher(URI uri) {
		var service = ServiceProvider.getService(IEventService.class);
		watchdogs = new HashMap<>();

		try {
			watchdogTopicSubscriber = service.createSubscriber(uri, EventConstants.WATCHDOG_STATUS_TOPIC);
			watchdogTopicSubscriber.addListener(evt -> updateWatchdogs(evt.getBean()));
		} catch (EventException e) {
			logger.error("Cannot listen to topic changes because command server is not there", e);
		}
	}

	public void dispose() {
		try {
			if (watchdogTopicSubscriber!=null) watchdogTopicSubscriber.disconnect();
		} catch (Exception ne) {
			logger.warn("Problem stopping topic listening for");
		}

	}

	private void updateWatchdogs(WatchdogStatusRecord watchdogStatus) {
		watchdogs.put(watchdogStatus.watchdogName(), watchdogStatus);
	}

	private Predicate<WatchdogStatusRecord> watchdogPausingPredicate = w -> w.enabled() && w.state().equals(WatchdogState.PAUSING);

	public boolean isWatchdogPausing() {
		return watchdogs.values().stream().anyMatch(watchdogPausingPredicate);
	}

	public String getWatchdogPausingNames() {
		return watchdogs.values().stream()
				.filter(watchdogPausingPredicate)
				.map(WatchdogStatusRecord::watchdogName)
				.collect(Collectors.joining("\n"));
	}

}
