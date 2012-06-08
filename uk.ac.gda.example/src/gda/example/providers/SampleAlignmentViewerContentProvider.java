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

package gda.example.providers;

import uk.ac.gda.example.device.collection.SampleAlignmentDeviceCollection;
import gda.device.DeviceException;

public class SampleAlignmentViewerContentProvider implements ISampleAlignmentViewerProvider {

	SampleAlignmentDeviceCollection collection;
	
	public SampleAlignmentViewerContentProvider(SampleAlignmentDeviceCollection collection) {
		this.collection = collection;
	}

	@Override
	public Object getXMotorValue() {
		try {
			Object pos = collection.getScannable().getPosition();
			if (pos instanceof double[]) {
				double[] array = (double[]) pos;
				return null;//return new ArrayPropertySource(array, scannable.getExtraNames());
			} else {
				return pos;
			}
		} catch (DeviceException e) {
			return null;
		}
	}

	@Override
	public Object getYMotorValue() {
		try {
			Object pos = collection.getScannablemotorunits().getPosition();
			if (pos instanceof double[]) {
				double[] array = (double[]) pos;
				return null;//return new ArrayPropertySource(array, scannable.getExtraNames());
			} else {
				return pos;
			}
		} catch (DeviceException e) {
			return null;
		}
	}

}
