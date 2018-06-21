/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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
import gda.epics.IAccessControl;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

/**
 * Scannable which holds two scannables, and uses one or the other depending on
 * whether ID Gap control is enabled or disabled.
 * Should be used to switch between energy controllers during a Run vs during a
 * Shutdown.
 */
public class GapControlCheckingEnergyController extends ScannableMotionUnitsBase implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(GapControlCheckingEnergyController.class);

	private ScannableMotionUnits gapControlDisabledEnergyController;
	private ScannableMotionUnits gapControlEnabledEnergyController;

	private IAccessControl gapControl;

	private ScannableMotionUnits chosenScannable;

	@Override
	public void configure() throws FactoryException {
		if (gapControlDisabledEnergyController == null || gapControlEnabledEnergyController == null) {
			throw new FactoryException(String.format("Energy Controllers not set for %s.", this.getName()));
		}

		try {
			checkControlSelectScannable();
		} catch (TimeoutException | CAException | InterruptedException e) {
			throw new FactoryException(String.format("%s couldn't get position of ID Gap Control PV during configuration.", this.getName()), e);
		}
		gapControlDisabledEnergyController.addIObserver(this);
		gapControlEnabledEnergyController.addIObserver(this);

		setConfigured(true);
	}

	@Override
	public String getUserUnits() {
		return chosenScannable.getUserUnits();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		try {
			checkControlSelectScannable();
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.warn(String.format("Couldn't get position of ID Gap Control PV. Moving most recently used energy controller: %s", chosenScannable.getName()), e);
		}
		chosenScannable.asynchronousMoveTo(position);
	}

	private void checkControlSelectScannable() throws TimeoutException, CAException, InterruptedException {
		chosenScannable = gapControl.getAccessControlState() == IAccessControl.Status.ENABLED ? gapControlEnabledEnergyController : gapControlDisabledEnergyController;
		logger.debug(String.format("ID Gap checked: %s using energy controller %s", this.getName(), chosenScannable.getName()));
	}

	@Override
	public Object getPosition() throws DeviceException {
		return chosenScannable.getPosition();
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return getPosition();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		for (Scannable s : new Scannable[] {gapControlDisabledEnergyController, gapControlEnabledEnergyController}) {
			try {
				if (s.isBusy()) {
					return true;
				}
			} catch (DeviceException e) {
				throw new DeviceException(String.format("%s could not get busy state of %s", this.getName(), s.getName()), e);
			}
		}
		return false;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if ( changeCode instanceof ScannableStatus) {
			ScannableStatus scanStatus = ((ScannableStatus) changeCode);

			if (scanStatus == ScannableStatus.BUSY ||  scanStatus== ScannableStatus.FAULT) {
				notifyIObservers(this, scanStatus);
			} else if (scanStatus == ScannableStatus.IDLE) {
				try {
					if (!isBusy()) {
						notifyIObservers(this, scanStatus);
					}
				} catch (DeviceException e) {
					String err = String.format("%s failed to get controller busy status.", this.getName());
					logger.error(err, e);
				}
			}
		}
	}

	@Override
	public void stop() throws DeviceException {
		gapControlDisabledEnergyController.stop();
		gapControlEnabledEnergyController.stop();
	}

	@Override
	public String toFormattedString() {
		return chosenScannable.toFormattedString();
	}

	public ScannableMotionUnits getGapControlDisabledEnergyController() {
		return gapControlDisabledEnergyController;
	}

	public void setGapControlDisabledEnergyController(ScannableMotionUnits gapControlDisabledEnergyController) {
		this.gapControlDisabledEnergyController = gapControlDisabledEnergyController;
	}

	public ScannableMotionUnits getGapControlEnabledEnergyController() {
		return gapControlEnabledEnergyController;
	}

	public void setGapControlEnabledEnergyController(ScannableMotionUnits gapControlEnabledEnergyController) {
		this.gapControlEnabledEnergyController = gapControlEnabledEnergyController;
	}

	public IAccessControl getGapControl() {
		return gapControl;
	}

	public void setGapControl(IAccessControl gapControl) {
		this.gapControl = gapControl;
	}
}
