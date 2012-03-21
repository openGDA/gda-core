/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector.xspress;

import gda.data.nexus.extractor.NexusGroupData;

import java.util.HashMap;
import java.util.Map;

/**
 * Class which is sent at ScanDataPoint data. Must be public so that serializes.
 */
public class MultiDetectorData extends HashMap<String, NexusGroupData > {

	@Override
	public String toString() {
		String s="";
		for(Map.Entry<String, NexusGroupData> entry : this.entrySet() ){
			s += "<detector name=" + entry.getKey() + ">";
			s += entry.toString() + "</detector>";
		}
		return s;
	}
}