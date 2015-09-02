/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.triggering;

import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;



public class SimpleTriggerModeProvider implements TriggerModeProvider {

	private TriggerMode triggerMode = new TriggerMode(StandardTriggerMode.SOFTWARE.toString(),StandardTriggerMode.SOFTWARE.ordinal());

	@Override
	public TriggerMode getTriggerMode() {
		return triggerMode;
	}

	public SimpleTriggerModeProvider(TriggerMode triggerMode) {
		super();
		this.triggerMode = triggerMode;
	}

	public void setTriggerMode(TriggerMode triggerMode) {
		this.triggerMode = triggerMode;
	}

	

}
