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

package org.eclipse.scanning.api.points.models;

import java.util.List;

/**
 * IScanPointGeneratorModels are any model that can be created through the ScanPointGenerator,
 * as each should extend Generator.py they need to provide Axes names, Units (either 1 or N
 * where N is the number of axes), Alternating, number of points for each Generator.
 *
 * As all Generator are wrapped in CompoundGenerators, Continuousness is also contained
 */
public interface IScanPointGeneratorModel extends IScanPathModel {

	/**
	 * If <code>true</code> the scan is continuous if possible, i.e. the motors continue to move while the detectors are
	 * exposed, if <code>false</code> the motors stop at each point for the detectors are exposed. For a scan to be
	 * continous, generally it must done by malcolm device, i.e. for a GDA point of view the scan contains exactly one
	 * runnable device and that must be a malcolm device. Additionally continuous scanning is only possible for certain
	 * path models, e.g. {@link TwoAxisGridPointsModel}, where even then the scan will only be continuous in the fast
	 * axis, and step in the slow axis, where the x-axis is the fast axis by default.
	 *
	 * @return <code>true</code> if the scan should be continuous, <code>false</code> otherwise.
	 */
	public boolean isContinuous();

	public void setContinuous(boolean continuous);

	/**
	 * This setting only makes sense when there is another model outside of this model within a CompoundModel: e.g. a
	 * StepModel in Energy outside of a Spiral in x, y will run the points of the Spiral backwards every alternate step
	 * of Energy, preventing having to return to starting positions of that scan. In the special case of Grid shaped
	 * scans both axes of the scan alternate, so it runs: bottom left -> bottom right -> top right -> top left, top left
	 * -> top right...
	 *
	 * @return <code>true</code> if the scan is a alternating, <code>false</code> otherwise
	 */
	public boolean isAlternating();

	public void setAlternating(boolean continuous);

	public List<String> getUnits();

	public void setUnits(List<String> units);

}
