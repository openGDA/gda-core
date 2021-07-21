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

package uk.ac.gda.exafs.ui;

import java.util.ArrayList;
import java.util.List;

import gda.factory.FindableBase;
import gda.util.exafs.Element;
import uk.ac.gda.beans.exafs.Region;

public class StandardXanesRegionsProvider extends FindableBase implements DefaultXanesRegions {

	@Override
	public List<Region> getDefaultRegions(Element element, String edge) {
		var edgeEnergy = element.getEdgeEnergy(edge);
		var coreHole = element.getCoreHole(edge);
		return new ArrayList<>(List.of(
				createRegion(edgeEnergy - 100 * coreHole, 5 * coreHole, 1.0),
				createRegion(edgeEnergy - 20 * coreHole, coreHole, 0.5),
				createRegion(edgeEnergy - 10 * coreHole, coreHole / 5, 1.0),
				createRegion(edgeEnergy + 10 * coreHole, coreHole, 1.8),
				createRegion(edgeEnergy + 20 * coreHole, 2 * coreHole, 5.0)
			));
	}

	private Region createRegion(double energy, double step, double time) {
		var region = new Region();
		region.setEnergy(energy);
		region.setStep(step);
		region.setTime(time);
		return region;
	}

	@Override
	public double getFinalEnergy(Element element, String edge) {
		return element.getFinalEnergy(edge);
	}

}
