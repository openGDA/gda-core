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

package gda.example.viewer;

/**
 * A listener which is notified when a sample alignment viewer
 * has a new motor values
 */
public interface ISampleAlignmentViewerListener {

	/**
	 * Notifies that a new X motor value is available
	 * 
	 * @param val the new motor value
	 */
	public void newXMotorValue(double val);
	
	/**
	 * Notifies that a new Y motor value is available
	 * 
	 * @param val the new motor value
	 */	
	public void newYMotorValue(double val);
}
