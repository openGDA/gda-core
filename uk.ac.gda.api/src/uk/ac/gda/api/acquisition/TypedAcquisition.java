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

package uk.ac.gda.api.acquisition;

import uk.ac.gda.common.entity.Document;

/**
 * Describe the type of {@link Acquisition} {@link Document}
 *
 * <p>
 * The uk.ac.gda.common.entity.Document identifies and describes, at the highest level, any kind of document,
 * while the uk.ac.gda.api.acquisition.Acquisition.getType() is a generic way to identify an acquisition document type.
 * </p>
 * <p>
 * Promoting the getType to the Document interface would reduce the generic role of the Document,
 * however keeping the getType in the Acquisition would make it related to AcquisitionConfiguration.
 * </p>
 * @author Maurizio Nagni
 */
public interface TypedAcquisition {
	/**
	 * An identifier of the type of acquisition.
	 * Default is {@code AcquisitionType.GENERIC}
	 */
	AcquisitionType getType();
}
