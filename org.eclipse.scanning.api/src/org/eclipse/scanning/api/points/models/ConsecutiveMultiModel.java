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
* A model that runs multiple other modules consecutively. Each model must be in the same axes, with the
* same units for each axis. A Compounded model (any grid) is not a valid model to be run consecutively.
*
* If run continuously, the models validation additionally must ensure that every models final bound (0.5 steps
* beyond the final point) is within DIFF_LIMIT (1e-5) in each axis of the initial bound (0.5 steps before the first
* point) for all axes.
*/
public class ConsecutiveMultiModel extends AbstractMultiModel<IScanPointGeneratorModel> {

	@Override
	public List<String> getScannableNames() {
		return getFirstModel().getScannableNames();
	}

	@Override
	public List<String> getUnits() {
		return getFirstModel().getUnits();
	}

}
