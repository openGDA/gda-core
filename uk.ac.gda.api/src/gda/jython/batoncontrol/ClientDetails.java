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

package gda.jython.batoncontrol;


import java.io.Serializable;

/**
 * Public information about clients shared by this package 
 */
public class ClientDetails extends ClientInfo implements Serializable {
	boolean hasBaton;

	/**
	 * Constructor.
	 * 
	 * @param index
	 * @param userID
	 * @param authorisationLevel
	 * @param hasBaton
	 * @param visitID 
	 */
	public ClientDetails(int index, String userID, String hostname, int authorisationLevel, boolean hasBaton, String visitID) {
		this.index = index;
		this.userID = userID;
		this.hostname = hostname;
		this.authorisationLevel = authorisationLevel;
		this.hasBaton = hasBaton;
		this.visitID = visitID;
	}

	/**
	 * @param other
	 * @param hasBaton
	 */
	public ClientDetails(ClientInfo other, boolean hasBaton) {
		this.index = other.index;
		this.userID = other.userID;
		this.hostname = other.hostname;
		this.authorisationLevel = other.authorisationLevel;
		this.hasBaton = hasBaton;
		this.visitID = other.visitID;
		
	}

	@Override
	public String toString() {
		return super.toString() + " baton:" + hasBaton;
	}
	/**
	 * @return Returns the hasBaton.
	 */
	public boolean isHasBaton() {
		return hasBaton;
	}

	/**
	 * @param hasBaton
	 *            The hasBaton to set.
	 */
	public void setHasBaton(boolean hasBaton) {
		this.hasBaton = hasBaton;
	}

}