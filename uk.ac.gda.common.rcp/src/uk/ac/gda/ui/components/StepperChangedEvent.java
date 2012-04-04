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
package uk.ac.gda.ui.components;

public class StepperChangedEvent {
	private String label;

	private int position;

	private Object source;

	public StepperChangedEvent(Stepper source, String label, int position) {
		this.source = source;
		this.label = label;
		this.position = position;
	}

	public StepperChangedEvent(Stepper stepper, int selection) {
		this(stepper, null, selection);
	}

	public String getLabel() {
		return label;
	}

	public int getPosition() {
		return position;
	}

	public Object getSource() {
		return source;
	}
}