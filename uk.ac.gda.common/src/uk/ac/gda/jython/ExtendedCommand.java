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

package uk.ac.gda.jython;

public interface ExtendedCommand {

	public static final String EXTENDED_QUICK_FIX = "pydevExtendedResolution";
	
	/**
	 * 
	 * @param line
	 * @return true if the line matches this command
	 */
	public boolean matches(String line);

	/**
	 * @return Returns the suggested correction.
	 */
	public String getCorrectionMessage();

	/**
	 * Returns the replacement string for the line if the problem was resolved.
	 * @return string
	 */
	public String getResolution();
}
