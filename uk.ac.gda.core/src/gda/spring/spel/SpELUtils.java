/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.spring.spel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import gda.device.DeviceException;
import gda.device.Scannable;

/**
 * Utility class defines function to be used to extend Spring SpEL expression. The function is registered with the Spring {@link StandardEvaluationContext}
 * using the method that can be called within the expression string. This provides a way to handle complex algorithm!
 *
 * @since 9.16
 */
public class SpELUtils {

	private static final Logger logger = LoggerFactory.getLogger(SpELUtils.class);

	private SpELUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * keep detector Idle time same when change exposure time
	 *
	 * @param acquireTime
	 * @param acquirePeriod
	 * @param time
	 */
	public static void updateAcquirePeriodWhenAcquireTimeChanges(Scannable acquireTime, Scannable acquirePeriod, Scannable idleTime, Double time) {
		try {
			double idle = Double.parseDouble(idleTime.getPosition().toString());
			acquireTime.asynchronousMoveTo(time);
			acquirePeriod.asynchronousMoveTo(time + idle);
		} catch (DeviceException e) {
			logger.error("updateAcquirePeriodWhenAcquireTimeChanges({}, {}, {}, {})", acquireTime.getName(), acquirePeriod.getName(), idleTime.getName(), time, e);
		}
	}

	public static void updateNumFilterInProc(Scannable enableFilter, Scannable resetFilter, Scannable numFilter, Integer num) {
		// when detector exposure time is very small, proc plugin will not have time to response to further request.
		try {
			// disable filtering
			enableFilter.asynchronousMoveTo(0);
			// change number of images to filter
			numFilter.asynchronousMoveTo(num);
			// reset filter action
			resetFilter.asynchronousMoveTo(1);
			// enable filtering
			enableFilter.asynchronousMoveTo(1);
		} catch (DeviceException e) {
			logger.error("updateNumFilterInProc({}, {}, {}, {})", enableFilter.getName(), resetFilter.getName(),numFilter.getName(), num, e);
		}
	}

}
