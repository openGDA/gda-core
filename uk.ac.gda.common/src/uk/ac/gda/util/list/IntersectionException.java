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

package uk.ac.gda.util.list;

/**
 *
 */
public class IntersectionException extends Exception {

	private String firstName, secondName;
	/**
	 * 
	 */
	public IntersectionException() {
	}

	/**
	 * @param message
	 */
	public IntersectionException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public IntersectionException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IntersectionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @return Returns the firstName.
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName The firstName to set.
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return Returns the secondName.
	 */
	public String getSecondName() {
		return secondName;
	}

	/**
	 * @param secondName The secondName to set.
	 */
	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}

}
