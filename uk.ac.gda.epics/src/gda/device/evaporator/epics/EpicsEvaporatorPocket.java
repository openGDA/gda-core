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

package gda.device.evaporator.epics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.enumpositioner.EpicsSimpleMbbinary;
import gda.device.evaporator.EvaporatorPocket;
import gda.device.scannable.MultiPVScannable;
import gda.device.scannable.PVStringScannable;
import gda.factory.FactoryException;

/**
 * EPICS Controller for an individual {@link EvaporatorPocket}.
 * <br/>
 * Instances should not be created directly. They should instead be accessed
 * from an instance of {@link EpicsEvaporatorPocket}.
 */
class EpicsEvaporatorPocket implements EvaporatorPocket {

	private static final Logger logger = LoggerFactory.getLogger(EpicsEvaporatorPocket.class);

	private final String pvBase;
	private final int index;

	private final PVStringScannable label;
	private final MultiPVScannable current;
	private final MultiPVScannable emission;
	private final MultiPVScannable flux;

	private final EpicsSimpleMbbinary regulation;

	EpicsEvaporatorPocket(String name, String pvBase, int i) {
		pvBase = pvBase + ":" + i + ":";
		this.pvBase = pvBase;
		this.index = i;

		try {
			label = new PVStringScannable(name + "_pocket" + i, pvBase + "LABEL");
			label.configure();

			current = createScannable(name, i, "CURRENT");
			emission = createScannable(name, i, "EMISSION");
			flux = createScannable(name, i, "FLUX");

			regulation = new EpicsSimpleMbbinary();
			regulation.setName(name + "_regulation");
			regulation.setRecordName(pvBase + "FILAMENT_REG_TYPE");
			regulation.setReadOnly(false);
			regulation.configure();
		} catch (FactoryException fe) {
			// Wrap FactoryExceptions in RuntimeException so constructor can be used in a stream
			throw new RuntimeException("Couldn't configure pocket", fe);
		}
	}

	private MultiPVScannable createScannable(String base, int pocket, String target) throws FactoryException {
		MultiPVScannable multiPV = new MultiPVScannable();
		multiPV.setName(base + "_pocket_" + pocket + "_" + target.toLowerCase());
		multiPV.setReadPV(pvBase + target + "_RBV");
		multiPV.setWritePV(pvBase + target);
		multiPV.configure();
		return multiPV;
	}

	@Override
	public String getLabel() throws DeviceException {
		return label.getPosition().toString();
	}

	@Override
	public void setLabel(String label) throws DeviceException {
		this.label.moveTo(label);
	}

	@Override
	public void setRegulation(String mode) throws DeviceException {
		regulation.moveTo(mode);
	}

	@Override
	public String getRegulation() throws DeviceException {
		return regulation.getPosition().toString();
	}

	@Override
	public void setCurrent(double current) throws DeviceException {
		this.current.moveTo(current);
	}

	@Override
	public double getCurrent() throws DeviceException {
		return (double) current.getPosition();
	}

	@Override
	public void setEmission(double emission) throws DeviceException {
		this.emission.moveTo(emission);
	}

	@Override
	public double getEmission() throws DeviceException {
		return (double) emission.getPosition();
	}

	@Override
	public void setFlux(double flux) throws DeviceException {
		this.flux.moveTo(flux);
	}

	@Override
	public double getFlux() throws DeviceException {
		return (double) flux.getPosition();
	}

	@Override
	public String toString() {
		try {
			return String.format("Pocket %d (name=%s, regulation=%s (current=%f, emission=%f, flux=%f)",
					index,
					getLabel(),
					getRegulation(),
					getCurrent(),
					getEmission(),
					getFlux());
		} catch (DeviceException e) {
			logger.error("Could not make pocket string", e);
			return String.format("Pocket %d: UNKNOWN", index);
		}
	}
}
