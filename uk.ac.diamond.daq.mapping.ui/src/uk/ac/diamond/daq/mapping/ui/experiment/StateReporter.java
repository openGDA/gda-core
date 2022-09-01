/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.function.Consumer;

/**
 * Reports to a consumer the state of the system
 */
public interface StateReporter {

	enum State {
		GOOD, BAD
	}

	public class StateReport{

		private State state;
		private String message;

		public StateReport(State state, String message) {
			this.state = state;
			this.message = message;
		}

		public State getState() {
			return state;
		}

		public String getMessage() {
			return message;
		}
	}

	void initialize(Consumer<StateReport> stateConsumer);

	void dispose();


}


