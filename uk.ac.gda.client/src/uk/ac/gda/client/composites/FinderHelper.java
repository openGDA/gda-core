/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.client.composites;

import java.util.Optional;

import gda.device.IScannableMotor;
import gda.device.Scannable;
import gda.factory.Findable;
import gda.factory.Finder;

/**
 * Finds devices being aware of the expected type.
 * This class should helps the caller instance to parametrise the required findable object
 *
 * @author Maurizio Nagni
 */
public final class FinderHelper {

	private FinderHelper() {}

	public static synchronized <T extends Findable> Optional<T> getFindableDevice(String beanId) {
		return Finder.getInstance().findOptional(beanId);
	}

	public static synchronized Optional<Scannable> getScannable(String beanId) {
		return Finder.getInstance().findOptional(beanId);
	}

	public static synchronized Optional<IScannableMotor> getIScannableMotor(String beanId) {
		return Finder.getInstance().findOptional(beanId);
	}
}
