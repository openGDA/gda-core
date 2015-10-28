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

package gda.device.detector.addetector.collectionstrategy;

import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;

/**
 * Configure trigger mode as Internal, usually used to indicate software triggered detectors, i.e. those triggered as soon as
 * startAcquiring is called.
 */
public class InternalTriggerModeDecorator extends TriggerModeDecorator {

	// InitializingBean interface

	@Override
	public void afterPropertiesSet() throws Exception {
		super.setTriggerMode(StandardTriggerMode.INTERNAL.ordinal());
		super.afterPropertiesSet();
	}

	// TriggerModeDecorator

	/**
	 * This is class is implemented as a TriggerModeDecorator with the setTriggerMode property disabled to maintain consistency.
	 */
	@Override @Deprecated
	public void setTriggerMode(int triggerMode) {
		throw new IllegalAccessError("Attempt to set property triggerMode in InternalTriggerDecorator bean!");
	}
}
