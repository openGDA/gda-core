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

package uk.ac.gda.api.scan;

/**
 * A scan object defined implicitly by start, stop and step positions.
 * Note that any or all of these values may not be specified (i.e. are <code>null</code>).
 */
public interface IImplicitScanObject extends IScanObject {

	public Object getStart();

	public Object getStop();

	public Object getStep();

	public boolean hasStep();

}
