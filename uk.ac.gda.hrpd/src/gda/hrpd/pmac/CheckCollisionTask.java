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

package gda.hrpd.pmac;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.hrpd.pmac.UnsafeOperationException;
import gda.device.detector.mythen.tasks.ScanTask;

public class CheckCollisionTask implements ScanTask {

	
	private Scannable checkedScannable;
	private SafePosition safePosition;

	@Override
	public void run() throws DeviceException {
		checkForCollision();
	}
	private void checkForCollision() throws DeviceException {
		//collision avoidance check tth motor position only proceed if tth motor is at MAC Safe Position defined in Spring configuration
		if (getCheckedScannable() != null) {
			if (Math.abs(Double.parseDouble(getCheckedScannable().getPosition().toString()) - getSafePosition().getPosition()) > getSafePosition().getTolerance()){
				throw new UnsafeOperationException(getSafePosition().getPosition(), getSafePosition().getPosition(), "Cannot proceed as MAC detector is not at safe position.");
			}
		}
	}

	public void setCheckedScannable(Scannable checkedScannable) {
		this.checkedScannable = checkedScannable;
	}

	public Scannable getCheckedScannable() {
		return checkedScannable;
	}

	public void setSafePosition(SafePosition macSafePosition) {
		this.safePosition = macSafePosition;
	}

	public SafePosition getSafePosition() {
		return safePosition;
	}

}
