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

package gda.beamline.health;

import static gda.beamline.health.BeamlineHealthState.ERROR;
import static gda.beamline.health.BeamlineHealthState.OK;
import static gda.beamline.health.BeamlineHealthState.WARNING;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import gda.factory.FindableBase;

/**
 * Returns the {@link BeamlineHealthState} of each component that is configured as relevant to the health of the
 * beamline, and the overall beamline state. <br>
 * The most serious state amongst the conditions for individual components determines the overall state of the beamline.
 */
public class BeamlineHealthMonitor extends FindableBase {

	/** The condition of each component that is configured as relevant to the overall health of the beamline. */
	private Collection<ComponentHealthCondition> conditions;

	public BeamlineHealthResult getState() {
		// Convert each condition to a form that can be (de)serialised.
		final List<BeamlineHealthComponentResult> scannableResults = new ArrayList<>(conditions.size());
		for (ComponentHealthCondition condition : conditions) {
			scannableResults.add(new BeamlineHealthComponentResult(condition.getDescription(),
					condition.getCurrentState(), condition.getHealthState(),
					condition.getErrorMessage()));
		}
		// Find the most serious scannable condition
		for (BeamlineHealthState state : Arrays.asList(ERROR, WARNING)) {
			final Optional<BeamlineHealthComponentResult> result = 	scannableResults.stream()
					.filter(r -> r.getComponentHealthState() == state)
					.findFirst();
			if (result.isPresent()) {
				return new BeamlineHealthResult(state, result.get().getErrorMessage(), scannableResults);
			}
		}
		return new BeamlineHealthResult(OK, "Beamline is ready", scannableResults);
	}

	public void setConditions(Collection<ComponentHealthCondition> conditions) {
		this.conditions = conditions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
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
		BeamlineHealthMonitor other = (BeamlineHealthMonitor) obj;
		if (conditions == null) {
			if (other.conditions != null)
				return false;
		} else if (!conditions.equals(other.conditions))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BeamlineHealthMonitor [conditions=" + conditions + "]";
	}
}
