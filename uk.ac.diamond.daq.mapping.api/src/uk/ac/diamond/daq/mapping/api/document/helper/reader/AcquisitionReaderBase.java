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

package uk.ac.diamond.daq.mapping.api.document.helper.reader;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utility class to read acquisition documents.
 *
 * @author Maurizio Nagni
 */
public class AcquisitionReaderBase<T> {

	public static final String NOT_AVAILABLE = "Not Available";

	private final Supplier<T> supplier;

	public AcquisitionReaderBase(Supplier<T> supplier) {
		this.supplier = Optional.ofNullable(supplier)
							.orElse(() -> null);
	}

	/**
	 * Get the data from the provider if the supplier is not {@code null}
	 * @return the supplier data, otherwise {@code null}
	 */
	public final T getData() {
		return this.supplier.get();
	}
}
