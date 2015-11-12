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

public class BeforeAfterScannable extends ScannableMotionUnitsWrapper {
	
	private static final Logger logger = LoggerFactory.getLogger(BeforeAfterScannable.class);
	
	Scannable beforeAfter;
	Object before;
	Object after;
	
	public BeforeAfterScannable(ScannableMotionUnits delegate, 
			Scannable beforeAfter, Object before, Object after) {
		setDelegate(delegate);
		this.beforeAfter = beforeAfter;
		this.before = before;
		this.after = after;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		
		logger.debug("before: {}({})", beforeAfter.getName(), before);
		beforeAfter.moveTo(before);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			logger.error("sleep before move interrupted", e);
		}
		
		logger.debug("move: {}({})", getName(), position);
		getDelegate().moveTo(position);
		
		logger.debug("after: {}({})", beforeAfter.getName(), after);
		beforeAfter.moveTo(after);
	}

}
