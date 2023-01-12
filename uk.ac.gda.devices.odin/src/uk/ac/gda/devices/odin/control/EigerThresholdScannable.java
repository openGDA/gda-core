/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.odin.control;

import java.io.IOException;

import gda.device.DeviceException;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableBase;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;
import gda.epics.ReadOnlyPV;
import gda.factory.FactoryException;

/**
 * To allow scanning the energy threshold of the Eiger detector
 */
public class EigerThresholdScannable extends ScannableBase {

	private static final String STALE = "Stale";
	private static final String VALID = "Valid";

	private String basePv;

	private PV<Double> thresholdPv;
	private ReadOnlyPV<String> staleParams;
	private double minimum  = 3000;
	private double maximum = 60000;

	@Override
	public void configure() throws FactoryException {
		if (basePv == null) {
			throw new IllegalStateException("Cannot configure Odin detector without base PV");
		}
		if (!isConfigured()) {
			thresholdPv = new PVWithSeparateReadback<>(
					LazyPVFactory.newDoublePV(basePv + "CAM:ThresholdEnergy"),
					LazyPVFactory.newReadOnlyDoublePV(basePv + "CAM:ThresholdEnergy_RBV"));

			staleParams = LazyPVFactory.newReadOnlyEnumPV(basePv + "CAM:StaleParameters_RBV", String.class);

			setConfigured(true);
		}
	}

	@Override
	public String checkPositionValid(Object position) throws DeviceException {
		double pos;
		try {
			pos = PositionConvertorFunctions.toDouble(position);
		} catch (IllegalArgumentException e) {
			return e.getMessage();
		}
		if (pos <= maximum && pos >= minimum) {
			// valid
			return null;
		}
		return String.format("position %f is out of range for %s", pos, getName());
	}

	@Override
	public boolean isBusy() throws DeviceException {
		try {
			return staleParams.get().equals(STALE);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		try {
			thresholdPv.putNoWait((Double) externalPosition);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}


	@Override
	public Object getPosition() throws DeviceException {
		try {
			if (staleParams.get().equals(VALID)) {
				return thresholdPv.get();
			}
		} catch (IOException e) {
			throw new DeviceException(e);
		}
		throw new DeviceException("Detector parameters are stale");
	}



	public String getBasePv() {
		return basePv;
	}

	public void setBasePv(String basePv) {
		this.basePv = basePv;
	}

}
