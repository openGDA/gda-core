/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.malcolm.event;

import org.eclipse.scanning.api.malcolm.IMalcolmDevice;

public class MalcolmStepsCompletedEvent extends MalcolmEvent {

	private final int stepsCompleted;

	protected MalcolmStepsCompletedEvent(IMalcolmDevice<?> malcolmDevice, int stepsCompleted, String message) {
		super(malcolmDevice, MalcolmEventType.STEPS_COMPLETED, message);
		this.stepsCompleted = stepsCompleted;
	}

	protected MalcolmStepsCompletedEvent(MalcolmStepsCompletedEvent event) {
		super(event.getMalcolmDevice(), MalcolmEventType.STEPS_COMPLETED, event.getMessage());
		this.stepsCompleted = event.stepsCompleted;
	}

	public int getStepsCompleted() {
		return stepsCompleted;
	}

	@Override
	public String toString() {
		return "MalcolmStepsCompletedEvent [stepsCompleted=" + stepsCompleted + ", getMalcolmDeviceName()="
				+ getMalcolmDeviceName() + ", getMessage()=" + getMessage() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + stepsCompleted;
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
		MalcolmStepsCompletedEvent other = (MalcolmStepsCompletedEvent) obj;
		if (stepsCompleted != other.stepsCompleted)
			return false;
		return true;
	}

}
