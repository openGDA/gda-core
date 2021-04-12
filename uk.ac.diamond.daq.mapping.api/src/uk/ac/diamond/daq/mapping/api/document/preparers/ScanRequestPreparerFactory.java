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

package uk.ac.diamond.daq.mapping.api.document.preparers;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import uk.ac.gda.api.acquisition.Acquisition;
import uk.ac.gda.api.acquisition.AcquisitionType;

public class ScanRequestPreparerFactory {

	private static final Map<AcquisitionType, ScanRequestPreparer> preparers = new EnumMap<>(AcquisitionType.class);

	private ScanRequestPreparerFactory() {
		throw new IllegalAccessError("For static access only");
	}

	static {
		preparers.put(AcquisitionType.DIFFRACTION, new DiffractionPreparer());
	}

	public static ScanRequestPreparer getPreparer(Acquisition<?> acquisition) {
		return Optional.ofNullable(preparers.get(acquisition.getType())).orElse(request -> {});
	}

}
