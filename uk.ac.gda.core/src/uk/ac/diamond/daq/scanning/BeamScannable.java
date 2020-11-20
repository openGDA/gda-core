/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.scanning;

import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;

public class BeamScannable extends AbstractScannable<Double> {

	private double beamSize = 0;

	public BeamScannable() {
		super(null, ScannableDeviceConnectorService.getInstance());
	}

	@Override
	public Double getPosition() throws ScanningException {
		return beamSize;
	}

	@Override
	public Double setPosition(Double value, IPosition position) throws ScanningException {
		this.beamSize = value;
		return this.beamSize;
	}

	public void setBeamSize(double beamSize) {
		this.beamSize = beamSize; // setting position from Spring directly doesn't seem to work
	}

	public double getBeamSize() {
		return this.beamSize;
	}

}
