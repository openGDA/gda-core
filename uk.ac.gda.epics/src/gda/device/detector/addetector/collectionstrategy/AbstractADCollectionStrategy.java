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

/**
 * This class is intended as the base class for all Area Detector Collection strategies which are compatible with the new composition system. Currently it just
 * redirects to AbstractADTriggeringStrategy, but eventually it should replace it, with a minimal set of collection strategy functions, allowing
 * AbstractADCollectionStrategyDecorators to add most of the more complex code. Any functions that just delegate to AbstractADTriggeringStrategy should be added
 * to AbstractADCollectionStrategyBase, not to this class.
 */
public abstract class AbstractADCollectionStrategy extends AbstractADCollectionStrategyBase {
}
