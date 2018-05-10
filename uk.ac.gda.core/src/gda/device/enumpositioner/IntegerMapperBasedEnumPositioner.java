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

package gda.device.enumpositioner;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.scannable.ScannablePositionChangeEvent;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(EnumPositioner.class)
public class IntegerMapperBasedEnumPositioner extends MapperBasedEnumPositionerBase<Integer> {

	@Override
	protected String getExternalValueFromMonitor() throws IllegalArgumentException, DeviceException {
		return getExternalValue((Integer) monitor.getPosition());
	}

	@Override
	protected ScannablePositionChangeEvent getScannablePositionChangeEvent(Object source, Object arg) {
		if (arg instanceof Integer) {
			String externalValue = getExternalValue((Integer) arg);
			return new ScannablePositionChangeEvent(externalValue);
		}
		return null;
	}
}
