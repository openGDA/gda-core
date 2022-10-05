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

package org.eclipse.scanning.points;

import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.points.IPointGeneratorService;

public class ServiceHolder {

	private static IValidatorService validatorService;
	private static IPointGeneratorService pointGeneratorService;

	public static IValidatorService getValidatorService() {
		return validatorService;
	}

	public void setValidatorService(IValidatorService validatorService) {
		ServiceHolder.validatorService = validatorService;
	}

	public static IPointGeneratorService getPointGeneratorService() {
		return pointGeneratorService;
	}

	public void setPointGeneratorService(IPointGeneratorService pointGeneratorService) {
		ServiceHolder.pointGeneratorService = pointGeneratorService;
	}
}
