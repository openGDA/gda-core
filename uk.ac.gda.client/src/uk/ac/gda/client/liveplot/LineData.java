/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.liveplot;

import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DAppearance;
import org.eclipse.january.dataset.Dataset;

import gda.scan.AxisSpec;

public class LineData {
	private Plot1DAppearance appearance;
	private Dataset x;
	private Dataset y;
	private AxisSpec yAxisSpec;

	public Dataset getX() {
		return x;
	}
	public Dataset getY() {
		return y;
	}
	public Plot1DAppearance getAppearance() {
		return appearance;
	}

	public AxisSpec getyAxisSpec() {
		return yAxisSpec;
	}

	public LineData(Plot1DAppearance appearance, Dataset x, Dataset y, AxisSpec yAxisSpec) {
		super();
		this.appearance = appearance;
		this.x = x;
		this.y = y;
		this.yAxisSpec = yAxisSpec;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((appearance == null) ? 0 : appearance.hashCode());
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		result = prime * result + ((yAxisSpec == null) ? 0 : yAxisSpec.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LineData other = (LineData) obj;
		if (!checkAppearance(other)) {
			return false;
		}
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		if (yAxisSpec == null) {
			if (other.yAxisSpec != null)
				return false;
		} else if (!yAxisSpec.equals(other.yAxisSpec))
			return false;
		return true;
	}

	/**
	 *
	 * @param other
	 * @return true if colour, visibility, style, linewidth of 'other' appearance matches current one
	 */
	private boolean checkAppearance(LineData other) {
		if (appearance == null) {
			if (other.appearance != null)
				return false;
		} else if ( !appearance.getColour().equals(other.appearance.getColour()) ) {
			return false;
		} else if (!appearance.isVisible() != other.appearance.isVisible()) {
			return false;
		} else if (appearance.getStyle() != other.appearance.getStyle()) {
			return false;
		} else if (appearance.getLineWidth() != other.appearance.getLineWidth()) {
			return false;
		}
		return true;
	}
}
