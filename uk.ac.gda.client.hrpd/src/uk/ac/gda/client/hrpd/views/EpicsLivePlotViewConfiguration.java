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

package uk.ac.gda.client.hrpd.views;

import java.util.Objects;

import uk.ac.gda.client.hrpd.epicsdatamonitor.EpicsDoubleDataArrayListener;

/**
 * Configuration for {@link LivePlotView}. Note that these objects should be created with unique traceNames.
 *
 * TODO replace with Java Record
 */
public final class EpicsLivePlotViewConfiguration {
	private final String traceName;
	private final EpicsDoubleDataArrayListener x;
	private final EpicsDoubleDataArrayListener y;

	public EpicsLivePlotViewConfiguration(String traceName, EpicsDoubleDataArrayListener x, EpicsDoubleDataArrayListener y) {
		this.traceName = traceName;
		this.x = x;
		this.y = y;

	}

	public String getTraceName() {
		return traceName;
	}

	public EpicsDoubleDataArrayListener getX() {
		return x;
	}

	public EpicsDoubleDataArrayListener getY() {
		return y;
	}

	@Override
	public String toString() {
		return "EpicsLivePlotViewConfiguration [traceName=" + traceName + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(traceName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EpicsLivePlotViewConfiguration other = (EpicsLivePlotViewConfiguration) obj;
		return Objects.equals(traceName, other.traceName);
	}

}