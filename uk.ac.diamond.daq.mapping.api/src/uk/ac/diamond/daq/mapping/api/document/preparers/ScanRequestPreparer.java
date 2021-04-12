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

import org.eclipse.scanning.api.event.scan.ScanRequest;

/**
 * Prepares a scan request given knowledge of a particular acquisition type,
 * e.g. moving a motor out of the way (through {@code ScanRequest.startPosition})
 * of a detector used for acquisition type X.
 */
public interface ScanRequestPreparer {

	/**
	 * Prepares the given scan request in place according to some defined behaviour
	 */
	void prepare(ScanRequest scanRequest);

}
