package uk.ac.diamond.daq.mapping.ui.properties;

import java.util.Optional;

import uk.ac.gda.client.properties.DetectorProperties;

/**
 * Builder for {@link DetectorProperties} objects
 *
 * @see DetectorHelper
 * @author Maurizio Nagni
 *
 */
public class DetectorPropertiesBuilder {

	private final DetecorPropertiesImpl detectorProperties = new DetecorPropertiesImpl();

	public static DetectorPropertiesBuilder createBuilder() {
		return new DetectorPropertiesBuilder();
	}

	public DetectorProperties build() {
		return detectorProperties;
	}

	public void setIndex(int index) {
		detectorProperties.setIndex(index);
	}

	public void setId(String id) {
		detectorProperties.setId(Optional.ofNullable(id));
	}

	public void setName(String name) {
		detectorProperties.setName(name);
	}

	public void setDetectorBean(String detectorBean) {
		detectorProperties.setDetectorBean(detectorBean);
	}

	private class DetecorPropertiesImpl implements DetectorProperties {

		private int index;
		private Optional<String> id;
		private String name;
		private String detectorBean;

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
		public String getDetectorBean() {
			return detectorBean;
		}

		public void setDetectorBean(String detectorBean) {
			this.detectorBean = detectorBean;
		}
	}
}
