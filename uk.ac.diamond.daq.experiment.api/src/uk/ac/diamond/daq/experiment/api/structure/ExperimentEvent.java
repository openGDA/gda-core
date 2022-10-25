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

package uk.ac.diamond.daq.experiment.api.structure;

import java.util.Objects;

import org.eclipse.scanning.api.event.IdBean;

public class ExperimentEvent extends IdBean {

	public enum Transition {
		STARTED, STOPPED
	}

	private String experimentName;
	private Transition transition;

	public ExperimentEvent() {}

	public ExperimentEvent(String experimentName, Transition transition) {

		this.experimentName = experimentName;
		this.transition = transition;
	}

	public String getExperimentName() {
		return experimentName;
	}

	public void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}

	/**
	 * The transition which caused this event
	 */
	public Transition getTransition() {
		return transition;
	}

	public void setTransition(Transition transition) {
		this.transition = transition;
	}

	private static final long serialVersionUID = -5323571169497554790L;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(experimentName, transition);
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
		ExperimentEvent other = (ExperimentEvent) obj;
		return Objects.equals(experimentName, other.experimentName) && transition == other.transition;
	}

}
