/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.collectionstrategy;

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.nxdetector.AbstractCollectionStrategyDecorator;

import java.util.List;

/**
 * This class is intended as the base class for all Area Detector Collection Strategy Decorators. It adds the ability to get the
 * ADBase of the object being decorated, so requires it to be an Area Detector Collection Strategy.
 */
public class AbstractADCollectionStrategyDecorator extends AbstractCollectionStrategyDecorator {

	private ADBase adBaseCache;

	final protected ADBase getAdBase() {
		if (adBaseCache != null) return adBaseCache;
		List<AbstractADCollectionStrategy> collectionStrategies = getDecorateesOfType(AbstractADCollectionStrategy.class);
		if (collectionStrategies.size() !=1) { throw new RuntimeException("Expected single AD collection strategy for Decorator " + getName() + " got " + collectionStrategies.size()); }
		return collectionStrategies.get(0).getAdBase();
	}
}
