/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.rcp;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.VisitData;
import gda.data.metadata.icat.IcatProvider;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * Watchdog to check whether the user's visit has expired, and close the client if it has
 */
public class CurrentVisitValidWatchdog {
	private static final Logger logger = LoggerFactory.getLogger(CurrentVisitValidWatchdog.class);

	/** Time between checks in minutes */
	private static final long POLLING_INTERVAL_MINS = 10;

	/** Give the client time to start up fully before checking */
	private static final long POLLING_DELAY = 2;

	/** Time (in hours) after the visit expires before client will be closed */
	private static final long GRACE_PERIOD_HOURS = 24;

	/** User's fedid */
	private final String user;

	/** Visit id for which the client is logged in */
	private final String visit;

	public CurrentVisitValidWatchdog(String user, String visit) {
		this.user = user;
		this.visit = visit;
	}

	public void startWatchdog() {
		logger.info("Starting CurrentVisitValidWatchdog at {}: user {}, visit {}, polling interval {} mins", new Date(), user, visit, POLLING_INTERVAL_MINS);

		try {
			if (!IcatProvider.getInstance().icatInUse()) {
				logger.info("Icat database not in use - cannot check whether visit is still valid.");
				return;
			}
			Async.scheduleAtFixedRate(this::checkVisitValid, POLLING_DELAY, POLLING_INTERVAL_MINS, TimeUnit.MINUTES, "Current visit valid watchdog");
			logger.info("Started current visit valid watchdog");
		} catch (Exception e) {
			logger.warn("Error starting CurrentVisitValidWatchdog", e);
		}
	}

	private void checkVisitValid() {
		if (visitExpired()) {
			logger.info("Visit has expired - closing client");
			closeClient();
		}
	}

	/**
	 * Check whether the current visit has expired.<br>
	 * If there is an error getting visit information, the visit is assumed *not* to have expired.
	 *
	 * @return <code>true</code> if the visit has expired, <code>false</code> otherwise
	 */
	private boolean visitExpired() {
		final Optional<VisitData> visitData;
		try {
			// SpyCat can only search for 2-letter prefixes, so we have to filter the result by the full visit id
			final List<VisitData> visits = IcatProvider.getInstance().getVisitDataByPrefix(visit.substring(0, 2));
			visitData = visits.stream().filter(v -> v.getVisitId().equals(visit)).findFirst();
		} catch (Exception e) {
			logger.error("Error getting visits", e);
			return false;
		}

		if (visitData.isEmpty()) {
			logger.debug("Visit {} not found", visit);
			return false;
		}

		final Date visitEnd = visitData.get().getEndTime();
		final Date now = new Date();
		final long timeToEndHours = TimeUnit.MILLISECONDS.toHours(visitEnd.getTime() - now.getTime());
		final boolean expired = timeToEndHours <= -GRACE_PERIOD_HOURS;
		logger.debug("Time now {}, visit ends {} in {} hours, expired {}", now, visitEnd, timeToEndHours, expired);

		return expired;
	}

	private void closeClient() {
		// Need to be in the UI thread to call close.
		// Additionally, we need to ensure that any dirty editors are saved without displaying a pop-up.
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().syncExec(() -> {
			workbench.saveAllEditors(false);
			workbench.close();
		});
	}
}
