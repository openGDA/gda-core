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

package gda.hrpd.sample.api;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import gda.device.Scannable;

public interface ScanDescription {
	/** Get the time (in seconds) for each collection in this scan */
	double getCollectionTime();

	/** Get a list of additional scannables to record data from during the scan */
	Collection<String> getBackgroundScannables();

	/** Get the positions of any scannables that need to be moved prior to a scan */
	Map<Scannable, Object> getInitalPositions();

	/** Whether the spinner needs to be enabled for scan */
	boolean spinOn();

	/** Position of sample during scan */
	double getSPos();

	/** Get data as map - to be serialised */
	default Map<String, Object> asMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("collectionTime", getCollectionTime());
		map.put("backgroundScannables", getBackgroundScannables());
		map.put("initialPositions", getInitalPositions());
		map.put("spin", spinOn());
		map.put("spos", getSPos());
		return map;
	}
}
