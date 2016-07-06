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

import gda.scan.AxisSpec;

import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DAppearance;
import org.eclipse.january.dataset.Dataset;

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

}
