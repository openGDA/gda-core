/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class is deprecated and should not be used! See <a href="http://jira.diamond.ac.uk/browse/GDA-6241">GDA-6241</a>
 * </p>
 * <p>
 * A utility class which allows easy sleeping.
 * </p>
 */
@Deprecated
public class Sleep {
	private static final Logger logger = LoggerFactory.getLogger(Sleep.class);

	/**
	 * This method is deprecated and should not be used! See <a href="http://jira.diamond.ac.uk/browse/GDA-6241">GDA-6241</a>. Typically use
	 * {@link Thread#sleep(long)} directly instead.
	 *
	 * @param milliSecs
	 */
	@Deprecated
	public static void sleep(int milliSecs) {
		try {
			Thread.sleep(milliSecs);
		} catch (InterruptedException ex) {
			logger.error("InterruptedException was swallowed. The call to this method should be removed!", ex);
		}
	}
}
