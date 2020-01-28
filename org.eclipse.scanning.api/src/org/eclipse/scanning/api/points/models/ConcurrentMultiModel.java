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

import java.util.ArrayList;
import java.util.List;
/**
* A model that runs multiple other modules concurrently. Each model must have the same number of points
* and must be in mutually exclusive axes. A Compounded model (any grid) is not a valid model to be
* run concurrently.
*/
public class ConcurrentMultiModel extends AbstractMultiModel<IScanPointGeneratorModel> {

	@Override
	public List<String> getScannableNames() {
		List<String> scannableNames = new ArrayList<>();
		for (IScanPointGeneratorModel model : getModels()) {
			scannableNames.addAll(model.getScannableNames());
		}
		return scannableNames;
	}

	@Override
	public List<String> getUnits() {
		List<String> units = new ArrayList<>();
		for (IScanPointGeneratorModel model : getModels()) {
			units.addAll(model.getUnits());
		}
		return units;
	}

}
