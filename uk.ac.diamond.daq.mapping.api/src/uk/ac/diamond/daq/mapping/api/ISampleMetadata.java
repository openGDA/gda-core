/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

/**
 * This interface should be implemented by any class which can represent sample metadata.
 * It contains only minimal methods which should be extended.
 */
public interface ISampleMetadata {

	/**
	 * Gets the sample name
	 *
	 * @return sampleName
	 */
	public String getSampleName();

	/**
	 * Sets the sample name
	 *
	 * @param sampleName
	 */
	public void setSampleName(String sampleName);

}
