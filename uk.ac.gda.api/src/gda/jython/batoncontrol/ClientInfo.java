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
import java.text.MessageFormat;

/**
 * Private class for holding information within the BatonManager. This information is shared outside this class using
 * the ClientDetails class.
 */
public class ClientInfo implements Serializable {
	
	int index;
	String userID;
	String hostname;
	int authorisationLevel;
	String visitID;

	protected ClientInfo() {

	}

	protected ClientInfo(int index, String userID, String hostname, int authorisationLevel, String visitID) {
		this.index = index;
		this.userID = userID;
		this.hostname = hostname;
		this.authorisationLevel = authorisationLevel;
		this.visitID = visitID;
	}
	
	protected ClientInfo copy() {
		return new ClientInfo(this.index, this.userID, this.hostname, this.authorisationLevel, this.visitID);
	}

	@Override
	public String toString() {
		return MessageFormat.format("{0}@{1} visit:{2} authorisation:{3}", userID, hostname, visitID, authorisationLevel);
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getAuthorisationLevel() {
		return authorisationLevel;
	}

	public void setAuthorisationLevel(int authorisationLevel) {
		this.authorisationLevel = authorisationLevel;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getVisitID() {
		return visitID;
	}

	public void setVisitID(String visitID) {
		this.visitID = visitID;
	}
	
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
}
