/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.hatsaxs.beans;

import java.io.Serializable;

import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;

public abstract class ExperimentBean implements Serializable {

	private String visit = "";
	private String username = "";
	private boolean isStaff;
	private double delay = 0;
	private String datafilename;
	
	protected ExperimentBean() {
		ClientDetails myDetails = InterfaceProvider.getBatonStateProvider().getMyDetails();
		this.visit = myDetails.getVisitID();
		this.username = myDetails.getUserID();
		this.isStaff = myDetails.getAuthorisationLevel() >= 3;
	}

	public String getVisit() {
		return visit;
	}

	public void setVisit(String visit) {
		if (!(isStaff || this.visit.equals(visit))) {
			throw new UnsupportedOperationException("User does not have permission to change username/visit");
		}
		this.visit = visit;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		if (!(isStaff || this.username.equals(username))) {
			throw new UnsupportedOperationException("User does not have permission to change username/visit");
		}
		this.username = username;
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}

	public String getDatafilename() {
		return datafilename;
	}

	public void setDatafilename(String datafilename) {
		this.datafilename = datafilename;
	}

}