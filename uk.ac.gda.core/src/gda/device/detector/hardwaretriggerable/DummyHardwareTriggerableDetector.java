/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.hardwaretriggerable;

import gda.device.Detector;
import gda.device.DeviceException;

public class DummyHardwareTriggerableDetector extends HardwareTriggerableDetectorBase {

	private boolean integrating = false;

	public DummyHardwareTriggerableDetector(String name) {
		setName(name);
		setInputNames(new String[] { name });
		setExtraNames(new String[] {});
		setOutputFormat(new String[] { "%s" });
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int[] { 1 };
	}
	
	@Override
	public void arm() throws DeviceException {
	}

	@Override
	public void collectData() throws DeviceException {
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return null;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return null;
	}

	@Override
	public int getStatus() throws DeviceException {
		return Detector.IDLE;
	}

	@Override
	public Object readout() throws DeviceException {
		return getCollectionTime();
	}

	@Override
	public void update(Object source, Object arg) {
	}
	
	@Override
	public boolean integratesBetweenPoints() {
		return integrating;
	}
	
	public void setIntegrating(boolean b) {
		integrating = b;
	}

}
