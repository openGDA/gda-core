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
import gda.hrpd.sample.api.ScanDescription;

public abstract class ScanSetup implements ScanDescription {
	private Map<Scannable, Object> initialPositions;
	private Collection<String> backgroundScannables;
	private final double collectionTime;
	private final boolean spin;
	private final double spos;

	public ScanSetup(double collectionTime, boolean spin, double spos, Map<Scannable, Object> initialPositions, Collection<String> backgroundScannables) {
		this.collectionTime = collectionTime;
		this.initialPositions = initialPositions;
		this.backgroundScannables = backgroundScannables;
		this.spin = spin;
		this.spos = spos;
	}

	@Override
	public Map<Scannable, Object> getInitalPositions() {
		return initialPositions;
	}

	@Override
	public Collection<String> getBackgroundScannables() {
		return backgroundScannables;
	}

	@Override
	public double getCollectionTime() {
		return collectionTime;
	}
	@Override
	public double getSPos() {
		return spos;
	}

	@Override
	public boolean spinOn() {
		return spin;
	}
}
