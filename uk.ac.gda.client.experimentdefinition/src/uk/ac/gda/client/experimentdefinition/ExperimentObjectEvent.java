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

package uk.ac.gda.client.experimentdefinition;


import java.util.EventObject;


@SuppressWarnings("serial")
public class ExperimentObjectEvent extends EventObject {

	private String  propertyName;
	private boolean isError = false;
	private boolean isCompleteRefresh = false;

	public ExperimentObjectEvent(Object source) {
		super(source);
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public IExperimentObject getRunObject() {
		if (getSource() instanceof IExperimentObjectManager) {
			return ((IExperimentObjectManager)getSource()).getExperimentList().get(0);
		}
		return (IExperimentObject)getSource();
	}

	public boolean isCompleteRefresh() {
		return isCompleteRefresh;
	}

	public void setCompleteRefresh(boolean isFileSpace) {
		this.isCompleteRefresh = isFileSpace;
	}
}
