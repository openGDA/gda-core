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

package uk.ac.diamond.daq.client.gui.camera.properties;

import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.gda.client.properties.MotorProperties;

/**
 * Builder for {@link MotorProperties} objects
 *
 * @see CameraHelper
 * @author Maurizio Nagni
 *
 */
public class MotorPropertiesBuilder {

	private final MotorPropertiesImpl motorProperties = new MotorPropertiesImpl();

	public static MotorPropertiesBuilder createBuilder() {
		return new MotorPropertiesBuilder();
	}

	public MotorProperties build() {
		return motorProperties;
	}

	public void setName(String name) {
		motorProperties.setName(name);
	}

	public void setController(String controller) {
		motorProperties.setController(controller);
	}

	private class MotorPropertiesImpl implements MotorProperties {

		private String name;
		private String controller;

		@Override
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String getController() {
			return controller;
		}

		public void setController(String controller) {
			this.controller = controller;
		}
	}
}
