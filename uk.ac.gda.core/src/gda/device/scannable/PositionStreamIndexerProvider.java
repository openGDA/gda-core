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

package gda.device.scannable;

import gda.device.DeviceException;

/**
 * Standard PositionCallableProviders are useful when a class only ever returns
 * the same single stream of data, but in situations where a class needs to
 * return one of several possible streams, this interface allows the specific
 * stream to be selected. In addition, since this returns a PositionStreamIndexer<T>
 * rather than a Callable directly, it is up to the calling class as to whether
 * to use named or unnamed callable or specify a thread pool size.
 * @param <T>
 */
public interface PositionStreamIndexerProvider<T> {

	public PositionStreamIndexer<T> getPositionSteamIndexer(int index) throws DeviceException;
	// TODO:                                   ^^^^^ Fix mis-spelling

}
