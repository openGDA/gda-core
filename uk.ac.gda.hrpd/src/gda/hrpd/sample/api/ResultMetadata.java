/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.hrpd.sample.api;

import java.time.Instant;
import java.util.Map;

public interface ResultMetadata {
	/** Get the outcome of this collection - true if successful, false otherwise */
	default boolean isSuccess() {
		return getError() == null;
	}
	/** Get the first error raised while running this collection - null if successful */
	public Exception getError();
	/** Get the GDA scan number for this collection */
	public int getScanNumber();
	/** Get the GDA scan file for this scan - usually a header file */
	public String getScanFile();
	/** Get a list of all files collected as part of this collection */
	public String[] getAllFiles();
	/** Get the time that the collection was started */
	public Instant getStartTime();
	/** Get the time that collection ended */
	public Instant getEndTime();
	/** Get a map of any other context data (temps/ring current etc) */
	public Map<String, Object> getContextData();
	/** Get the fedID of the user that ran the collection */
	public String getUser();
//	/** Get the scan type required for this sample */
}
