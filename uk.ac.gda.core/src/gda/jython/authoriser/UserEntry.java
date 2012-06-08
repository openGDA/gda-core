/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython.authoriser;

/**
 * Object holding information about a user
 */
public class UserEntry {
	private String userName;

	private Integer authorisationLevel;
	
	private Boolean staff;

	/**
	 * Constructor
	 * 
	 * @param userName
	 * @param authorisationLevel
	 * @param staff
	 */
	public UserEntry(String userName, Integer authorisationLevel, boolean staff) {
		setUserName(userName);
		setAuthorisationLevel(authorisationLevel);
		setStaff(staff);
	}

	/**
	 * @return the userName
	 */
	protected String getUserName() {
		return userName;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	protected void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the authorisationLevel
	 */
	protected int getAuthorisationLevel() {
		return authorisationLevel;
	}

	/**
	 * @param authorisationLevel
	 *            the authorisationLevel to set
	 */
	protected void setAuthorisationLevel(int authorisationLevel) {
		this.authorisationLevel = authorisationLevel;
	}
	
	/**
	 * @return Returns the staff.
	 */
	protected Boolean getStaff() {
		return staff;
	}

	/**
	 * @param staff The staff to set.
	 */
	protected void setStaff(Boolean staff) {
		this.staff = staff;
	}


}
