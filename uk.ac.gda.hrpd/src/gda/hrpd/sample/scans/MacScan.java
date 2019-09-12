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

package gda.hrpd.sample.scans;

import java.util.Collection;
import java.util.Map;

import gda.device.Scannable;

public class MacScan extends ScanSetup {
	private static final double[] NO_REBINNING = new double[] {};

	private final double[] rebinning;

	public MacScan(double collectionTime, boolean spin, double spos, Map<Scannable, Object> initialPositions, Collection<String> backgroundScannables) {
		this(collectionTime, NO_REBINNING, spin, spos, initialPositions, backgroundScannables);
	}

	public MacScan(double collectionTime, double[] rebinning, boolean spin, double spos, Map<Scannable, Object> initialPositions, Collection<String> backgroundScannables) {
		super(collectionTime, spin, spos, initialPositions, backgroundScannables);
		this.rebinning = rebinning;
	}

	public double[] getRebinning() {
		return rebinning;
	}

	@Override
	public Map<String, Object> asMap() {
		Map<String, Object> map = super.asMap();
		map.put("type", "mac");
		map.put("rebinning", getRebinning());
		return map;
	}

	@Override
	public String toString() {
		return "MAC scan[cvscan " + getCollectionTime() + "]";
	}
}