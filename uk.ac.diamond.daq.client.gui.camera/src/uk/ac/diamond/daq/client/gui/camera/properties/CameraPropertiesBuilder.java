package uk.ac.diamond.daq.client.gui.camera.properties;

import java.util.List;
import java.util.Optional;

import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.gda.client.properties.CameraProperties;
import uk.ac.gda.client.properties.MotorProperties;

/**
 * Builder for {@link CameraProperties} objects
 * 
 * @see CameraHelper
 * @author Maurizio Nagni
 *
 */
public class CameraPropertiesBuilder {

	private final CameraPropertiesImpl cameraProperties = new CameraPropertiesImpl();

	public static CameraPropertiesBuilder createBuilder() {
		return new CameraPropertiesBuilder();
	}

	public CameraProperties build() {
		return cameraProperties;
	}

	public void setIndex(int index) {
		cameraProperties.setIndex(index);
	}

	public void setId(String id) {
		cameraProperties.setId(Optional.ofNullable(id));
	}
	
	public void setName(String name) {
		cameraProperties.setName(name);
	}

	public void setCameraControl(String cameraControl) {
		cameraProperties.setCameraControl(cameraControl);
	}

	public void setCameraConfiguration(String cameraConfiguration) {
		cameraProperties.setCameraConfiguration(cameraConfiguration);
	}

	public void setMotorProperties(List<MotorProperties> motorProperties) {
		cameraProperties.setMotorProperties(motorProperties);
	}

	public void setBeamMappingActive(boolean beamMappingActive) {
		cameraProperties.setBeamMappingActive(beamMappingActive);
	}
	
	private class CameraPropertiesImpl implements CameraProperties {

		private int index;
		private Optional<String> id;
		private String name;
		private String cameraControl;
		private String cameraConfiguration;
		private List<MotorProperties> motorProperties;
		private boolean beamMappingActive;

		@Override
		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		@Override
		public Optional<String> getId() {
			return id;
		}

		public void setId(Optional<String> id) {
			this.id = id;
		}

		@Override
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String getCameraControl() {
			return cameraControl;
		}

		public void setCameraControl(String cameraControl) {
			this.cameraControl = cameraControl;
		}

		@Override
		public String getCameraConfiguration() {
			return cameraConfiguration;
		}

		public void setCameraConfiguration(String cameraConfiguration) {
			this.cameraConfiguration = cameraConfiguration;
		}

		@Override
		public List<MotorProperties> getMotorProperties() {
			return motorProperties;
		}

		public void setMotorProperties(List<MotorProperties> motorProperties) {
			this.motorProperties = motorProperties;
		}

		@Override
		public boolean isBeamMappingActive() {
			return beamMappingActive;
		}

		public void setBeamMappingActive(boolean beamMappingActive) {
			this.beamMappingActive = beamMappingActive;
		}
	}
}
