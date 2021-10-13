/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * The delegate {@link Scannable} is determined by the position of an {@link EnumPositioner}
 * at the time the scannable function is called
 */
@ServiceInterface(Scannable.class)
public class PositionerDeterminedScannable extends ScannableBase {

	private static final Logger logger = LoggerFactory.getLogger(PositionerDeterminedScannable.class);

	private final EnumPositioner selector;
	private final Map<String, Scannable> delegates;

	public PositionerDeterminedScannable(EnumPositioner selector, Map<String, Scannable> delegates) throws DeviceException {
		validate(selector, delegates);
		this.selector = selector;
		this.delegates = delegates;
	}

	private void validate(EnumPositioner selector, Map<String, Scannable> delegates) throws DeviceException {
		Objects.requireNonNull(selector);
		Objects.requireNonNull(delegates);
		for (var position : selector.getPositions()) {
			if (!delegates.containsKey(position)) {
				throw new DeviceException("Missing delegate scannable for position '{}'", position);
			}
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return getScannable().isBusy();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		getScannable().asynchronousMoveTo(position);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return getScannable().getPosition();
	}

	@Override
	public String[] getOutputFormat() {
		try {
			return getScannable().getOutputFormat();
		} catch (DeviceException e) {
			logger.error("Could not read position of {}", selector, e);
			return super.getOutputFormat();
		}
	}

	private Scannable getScannable() throws DeviceException {
		return delegates.get(selector.getPosition());
	}

}
