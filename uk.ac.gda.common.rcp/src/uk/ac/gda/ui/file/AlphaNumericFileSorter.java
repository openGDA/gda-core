/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.file;

import org.eclipse.jface.viewers.ViewerComparator;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;


@Deprecated(since="GDA 9.7")
public class AlphaNumericFileSorter extends ViewerComparator {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(AlphaNumericFileSorter.class);

	/**
	 *
	 */
	public AlphaNumericFileSorter() {
		super(String.CASE_INSENSITIVE_ORDER);
		logger.deprecatedClass();
	}
}
