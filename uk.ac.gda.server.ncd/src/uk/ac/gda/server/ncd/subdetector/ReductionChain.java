/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.subdetector;

import java.util.Vector;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

public class ReductionChain extends ReductionDetectorBase {

	private Vector<INcdSubDetector> chain = new Vector<INcdSubDetector>();

	public ReductionChain(String name, String key) {
		super(name, key);
	}

	public Vector<INcdSubDetector> getChain() {
		return chain ;
	}
	
	@Override
	public void writeout(int frames, NXDetectorData nxdata)
			throws DeviceException {

		String detToLookAt = key;
		
		for(INcdSubDetector element: chain) {
			if (element instanceof ReductionDetectorBase) {
				((ReductionDetectorBase) element).key = detToLookAt;
			}
			element.writeout(frames, nxdata);
			detToLookAt = element.getName();
		}
	}
}