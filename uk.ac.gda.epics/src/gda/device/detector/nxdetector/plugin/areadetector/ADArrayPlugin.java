/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.detector.addetector.ArrayData;
import gda.device.detector.areadetector.v17.NDArray;

public class ADArrayPlugin extends ADDirectReadBase {

	private final NDArray ndArray;

	public ADArrayPlugin(NDArray ndArray) {
		super(ndArray);
		this.ndArray = ndArray;
	}

	@Override
	public String getName() {
		return "array";
	}

	@Override
	protected NexusGroupData getData() throws Exception {
		return ArrayData.readArrayData(ndArray);
	}

	public NDArray getNdArray() {
		return ndArray;
	}

}
