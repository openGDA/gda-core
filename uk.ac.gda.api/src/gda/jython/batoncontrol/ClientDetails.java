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

/**
 * Public information about clients shared by this package
 */
public class ClientDetails extends ClientInfo {

	private boolean hasBaton;

	public ClientDetails(int index, String userID, String fullName, String hostname, int authorisationLevel, boolean hasBaton, String visitID) {
		super(index, userID, fullName, hostname, authorisationLevel, visitID);
		this.hasBaton = hasBaton;
	}

	public ClientDetails(ClientInfo other, boolean hasBaton) {
		this(other.getIndex(), other.getUserID(), other.getFullName(), other.getHostname(), other.getAuthorisationLevel(), hasBaton, other.getVisitID());
	}

	@Override
	public String toString() {
		return super.toString() + " baton:" + hasBaton;
	}

	public boolean hasBaton() {
		return hasBaton;
	}

	public void setHasBaton(boolean hasBaton) {
		this.hasBaton = hasBaton;
	}

}