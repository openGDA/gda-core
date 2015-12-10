/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.scannable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;

import static com.google.common.base.Preconditions.checkState;

public class BeforeCheckScannable extends ScannableMotionUnitsWrapper {
	
	private static final Logger logger = LoggerFactory.getLogger(BeforeCheckScannable.class);
	
	Scannable beforeCheck;
	Object before;
	
	public BeforeCheckScannable(ScannableMotionUnits delegate, 
			Scannable beforeCheck, Object before) {
		setDelegate(delegate);
		this.beforeCheck = beforeCheck;
		this.before = before;
	}

	@Override
	// overriding asynchronousMoveTo did not get called!
	public void moveTo(Object position) throws DeviceException {
		checkState(beforeCheck.isAt(before), 
				"%s position should be %s", beforeCheck.getName(), before);
		getDelegate().moveTo(position);
	}

}
