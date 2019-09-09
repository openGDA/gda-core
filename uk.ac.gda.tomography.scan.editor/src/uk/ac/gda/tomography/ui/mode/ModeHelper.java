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

package uk.ac.gda.tomography.ui.mode;

import java.util.Objects;

import gda.device.IScannableMotor;
import gda.factory.Finder;
import uk.ac.gda.tomography.controller.IncompleteModeException;

/**
 * Finds devices being aware of the expected type.
 * This class should helps the caller instance to parametrise the required findable object
 *
 * @author Maurizio Nagni
 */
public final class ModeHelper {

	private ModeHelper() {}

	public static IScannableMotor getMotor(String beanId) throws IncompleteModeException {
		return getFindableDevice(beanId);
	}

	public static synchronized <T> T getFindableDevice(String beanId) throws IncompleteModeException {
		T findable = Finder.getInstance().find(beanId);
		if (Objects.isNull(findable)) {
			throw new IncompleteModeException(String.format("Cannot find any beanId:%s", beanId));
		}
		return findable;
	}
}
