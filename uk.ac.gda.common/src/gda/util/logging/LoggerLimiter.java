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

package gda.util.logging;

import java.time.Duration;
import java.time.Instant;

/** Simple class to manage rate limiting of log messages
 * <BR><BR>
 * For example:
 * <PRE><BR>{@code
 * var logLimiter = new LoggerLimiter(Duration.ofSeconds(10), true);
 * ...
 * if (logLimiter.isLogDue()) {
 * 		logger.warn("... blocked for {} seconds ...logLimiter.getTimeSinceStart().getSeconds());
 * }
 * }</PRE>
 * <BR>
 */
public final class LoggerLimiter {
	public LoggerLimiter(Duration toNextReport, boolean backOff) {
		super();
		this.toNextReport = toNextReport;
		this.betweenReports = toNextReport;
		this.backOff = backOff;
	}

	final boolean backOff;
	final Instant startTime = Instant.now();
	final Duration betweenReports;
	Duration toNextReport;

	public boolean isLogDue() {
		if (Duration.between(startTime, Instant.now()).minus(toNextReport).isNegative()) {
			return false;
		}
		toNextReport = backOff ? toNextReport.multipliedBy(2) : toNextReport.plus(betweenReports);
		return true;
	}

	public Duration getTimeSinceStart() {
		return Duration.between(startTime, Instant.now());
	}
}