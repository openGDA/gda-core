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
