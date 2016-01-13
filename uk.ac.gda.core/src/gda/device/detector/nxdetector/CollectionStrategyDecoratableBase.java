/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector;

/**
 * This can be used as a base class for collection strategies that can be decorated and their decorators (which can also themselves be decorated). It provides
 * support for a strategy/decorator to record whether it is called by a decorator (as opposed to being called directly by a scan runner) and also provides dummy
 * implementations of saving/restoring state. Derived classes should in general save/restore their state if called directly, but not if called by a decorator,
 * as the decorator will save state for each of its decoratees.
 */
public abstract class CollectionStrategyDecoratableBase implements CollectionStrategyDecoratableInterface {

	protected boolean suppressSave;
	protected boolean suppressRestore;
}
