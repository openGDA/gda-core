/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.scannable.ScannablePositionChangeEvent;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(EnumPositioner.class)
public class StringMapperBasedEnumPositioner extends MapperBasedEnumPositionerBase<String> {

	private static final Logger logger = LoggerFactory.getLogger(StringMapperBasedEnumPositioner.class);

	@Override
	protected String getExternalValueFromMonitor() throws DeviceException {
		return getExternalValue((String) getMonitor().getPosition());
	}

	@Override
	protected ScannablePositionChangeEvent getScannablePositionChangeEvent(Object source, Object arg) {
		if (source == getMonitor()) {
			try {
				String latestPosition = (String) getMonitor().getPosition();
				String externalValue = getExternalValue(latestPosition);
				return new ScannablePositionChangeEvent(externalValue);
			} catch (DeviceException e) {
				logger.error("Problem getting position from {}", getMonitor().getName());
			}
		}
		return null;
	}
}
