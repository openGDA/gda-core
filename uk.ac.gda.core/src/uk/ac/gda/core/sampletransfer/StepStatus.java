/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.core.sampletransfer;

import java.util.Objects;

import org.eclipse.scanning.api.event.status.StatusBean;

public class StepStatus extends StatusBean {

	private String description;
	private boolean clientAction;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isClientAction() {
		return clientAction;
	}

	public void setClientAction(boolean clientAction) {
		this.clientAction = clientAction;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(clientAction, description);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StepStatus other = (StepStatus) obj;
		return clientAction == other.clientAction && Objects.equals(description, other.description);
	}

	@Override
	public String toString() {
		return "StepStatus [description=" + description + ", clientAction=" + clientAction + "]";
	}

}
