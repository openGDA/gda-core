/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.api.plan.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Keep track of when and why a particular ITrigger is triggered during an experiment
 */
public class TriggerRecord implements Serializable {

	private static final long serialVersionUID = -1263340531173097425L;

	private String triggerName;
	private String sampleEnvironmentName;
	private List<TriggerEvent> events;

	public TriggerRecord(String triggerName, String sampleEnvironmentName) {
		this.triggerName = triggerName;
		this.sampleEnvironmentName = sampleEnvironmentName;
		events = new ArrayList<>();
	}

	public TriggerRecord() {}

	public String getTriggerName() {
		return triggerName;
	}

	public String getSampleEnvironmentName() {
		return sampleEnvironmentName;
	}

	public void setSampleEnvironmentName(String sampleEnvironmentName) {
		this.sampleEnvironmentName = sampleEnvironmentName;
	}

	public List<TriggerEvent> getEvents() {
		return events;
	}

	public void setEvents(List<TriggerEvent> events) {
		this.events = events;
	}

	public void addEvent(TriggerEvent event) {
		events.add(event);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sampleEnvironmentName == null) ? 0 : sampleEnvironmentName.hashCode());
		result = prime * result + ((triggerName == null) ? 0 : triggerName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TriggerRecord other = (TriggerRecord) obj;
		if (!events.containsAll(other.events)) {
			return false;
		}
		if (sampleEnvironmentName == null) {
			if (other.sampleEnvironmentName != null)
				return false;
		} else if (!sampleEnvironmentName.equals(other.sampleEnvironmentName))
			return false;
		if (triggerName == null) {
			if (other.triggerName != null)
				return false;
		} else if (!triggerName.equals(other.triggerName))
			return false;
		return true;
	}

}
