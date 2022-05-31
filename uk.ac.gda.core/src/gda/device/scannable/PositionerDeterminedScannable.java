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
import gda.device.EnumPositionerStatus;
import gda.device.Scannable;
import gda.observable.IObserver;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * The delegate {@link Scannable} is determined by the position of an {@link EnumPositioner}.
 * An observer is attached to the positioner to update the delegate selection as the positioner moves.
 */
@ServiceInterface(Scannable.class)
public class PositionerDeterminedScannable extends ScannableBase {

	private static final Logger logger = LoggerFactory.getLogger(PositionerDeterminedScannable.class);

	private final EnumPositioner selector;
	private final Map<String, Scannable> delegates;

	private Scannable activeScannable;

	/**
	 * attached to the active scannable to forward its updates to this scannable's observers,
	 * changing the event's source to this scannable instance.
	 */
	private final IObserver updatesForwarding = (source, argument) -> notifyIObservers(this, argument);

	public PositionerDeterminedScannable(EnumPositioner selector, Map<String, Scannable> delegates) throws DeviceException {
		validate(selector, delegates);
		this.selector = selector;
		this.delegates = delegates;

		// select initial delegate
		refreshDelegateScannable();

		// trigger delegate selection when selector completes a move
		this.selector.addIObserver(this::refreshDelegateScannable);
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
		return activeScannable.isBusy();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		activeScannable.asynchronousMoveTo(position);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return activeScannable.getPosition();
	}

	@Override
	public String[] getOutputFormat() {
		return activeScannable.getOutputFormat();
	}

	/**
	 * Observer attached to our selector.
	 * Triggers a delegate refresh when we receive
	 * a {@code EnumPositionerStatus.IDLE} message
	 * (signifying the end of a positioner move)
	 */
	private void refreshDelegateScannable(Object source, Object arg) {
		if (arg == EnumPositionerStatus.IDLE) {
			logger.debug("Updating delegate scannable after receiving event from {}", source);
			refreshDelegateScannable();
		}
	}

	/**
	 * Updates the delegate selection based on selector position,
	 * and handles updates forwarding
	 */
	private void refreshDelegateScannable() {
		notifyIObservers(this, ScannableStatus.BUSY);
		try {
			var scannable = getScannable();

			if (activeScannable != null && scannable != activeScannable) {
				activeScannable.deleteIObserver(updatesForwarding);
			}

			scannable.addIObserver(updatesForwarding);
			activeScannable = scannable;
		} catch (DeviceException e) {
			logger.error("Error reading positioner. Could be delegating to wrong scannable ({})", activeScannable.getName(), e);
		}
		notifyIObservers(this, ScannableStatus.IDLE);
	}

	private Scannable getScannable() throws DeviceException {
		return delegates.get(selector.getPosition());
	}

}
