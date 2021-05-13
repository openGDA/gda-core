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

package uk.ac.gda.client.properties.controller;

import uk.ac.gda.client.properties.MotorProperties;

/**
 * Represents a simple {@code name:controller} object
 *
 * @author Maurizio Nagni
 */
public class ControllerConfiguration implements MotorProperties {

	private String controller;
	private String name;
	@Override
	public String getController() {
		return controller;
	}
	public void setController(String controller) {
		this.controller = controller;
	}
	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
