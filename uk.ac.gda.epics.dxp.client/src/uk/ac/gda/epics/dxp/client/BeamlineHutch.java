/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.dxp.client;

public enum BeamlineHutch {

	EH1("EH1"), EH2("EH2"), None("None");

	private final String value;

	private BeamlineHutch(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public enum Collimator {
		C1("C1"), C2("C2"), C3("C3"), C4("C4"), Nil("Nil");

		private final String value;

		private Collimator(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
