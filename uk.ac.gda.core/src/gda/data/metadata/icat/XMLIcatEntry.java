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

package gda.data.metadata.icat;


/**
 * Bean which holds the information for a single experiment. Used by the XMLIcat class.
 */
public class XMLIcatEntry {
	
	private String experimentalTitle;
	private String visitID;
	private String usernames;
	private String experimentStart;
	private String experimentStop;
	private String beamline;
	
	public XMLIcatEntry(){
	}

	public String getExperimentalTitle() {
		return experimentalTitle;
	}

	public String getVisitID() {
		return visitID;
	}

	public String getUsernames() {
		return usernames;
	}

	public String getExperimentStart() {
		return experimentStart;
	}

	public String getExperimentStop() {
		return experimentStop;
	}

	public String getBeamline() {
		return beamline;
	}

	public void setExperimentalTitle(String experimentalTitle) {
		this.experimentalTitle = experimentalTitle;
	}

	public void setVisitID(String visitID) {
		this.visitID = visitID;
	}

	public void setUsernames(String usernames) {
		this.usernames = usernames;
	}

	public void setExperimentStart(String experimentStart) {
		this.experimentStart = experimentStart;
	}

	public void setExperimentStop(String experimentStop) {
		this.experimentStop = experimentStop;
	}

	public void setBeamline(String beamline) {
		this.beamline = beamline;
	}
	
	

}
