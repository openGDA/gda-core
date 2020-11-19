package uk.ac.diamond.daq.mapping.ui.properties;

import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper.AcquisitionPropertyType;
import uk.ac.diamond.daq.mapping.ui.properties.stages.ScannablePropertiesDocument;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument;
import uk.ac.gda.client.properties.CameraProperties;

/**
 * Describes which acquisition engine and detectors are associated to a specific acquisition context. <p>
 * See <a href="https://confluence.diamond.ac.uk/display/DIAD/Configuration+Properties">Configuration Properties</a>
 *
 * @author Maurizio Nagni
 *
 * @see AcquisitionsPropertiesHelper
 */
@JsonDeserialize(builder = AcquisitionPropertiesDocument.Builder.class)
public class AcquisitionPropertiesDocument {

	/**
	 * Identifies an acquisition in its context.
	 */
	private final int index;
	/**
	 * The identifies the acquisition type. Used mostly by the perspective to identify its configuration parameters
	 */
	private final AcquisitionPropertyType type;
	/**
	 * The engine associated with this acquisition
	 */
	private final AcquisitionEngineDocument engine;
	/**
	 * A collection of {@link CameraProperties#getId()} associated with this acquisition type.
	 */
	private final Set<String> cameras;

	/**
	 * A collection of {@link ScannablePropertiesDocument#getScannable()} associated with this acquisition type
	 * defining the out of beam
	 */
	private final Set<String> outOfBeamScannables;

	/**
	 * The name of the primary dataset; typically the name of the detector.
	 * Used to identify a NeXus appender corresponding to this acquisition
	 * e.g. a calibration merger
	 */
	private final String primaryDataset;

	AcquisitionPropertiesDocument(int index, AcquisitionPropertyType type, AcquisitionEngineDocument engine,
			Set<String> cameras, Set<String> outOfBeamScannables, String primaryDataset) {
		super();
		this.index = index;
		this.type = type;
		this.engine = engine;
		this.cameras = cameras;
		this.outOfBeamScannables = outOfBeamScannables;
		this.primaryDataset = primaryDataset;
	}

	/**
	 * Identifies a detector in its context.
	 *
	 * @return the detector index number
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * The acquisition type.
	 *
	 * @return Returns the acquisition type
	 */
	public AcquisitionPropertyType getType() {
		return type;
	}

	/**
	 * The acquisition engine.
	 *
	 * @return the engine used by this acquisition
	 */
	public AcquisitionEngineDocument getEngine() {
		return engine;
	}

	/**
	 * A collection of cameras associated with this acquisition type.
	 *
	 * @return the cameras IDs
	 */
	public Set<String> getCameras() {
		return cameras;
	}

	public Set<String> getOutOfBeamScannables() {
		return outOfBeamScannables;
	}

	public String getPrimaryDataset() {
		return primaryDataset;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private int index;
		private AcquisitionPropertyType type;
		private AcquisitionEngineDocument engine;
		private Set<String> cameras;
		private Set<String> outOfBeamScannables;
		private String primaryDataset;

		public Builder() {
		}

		public Builder(final AcquisitionPropertiesDocument parent) {
			this.index = parent.getIndex();
			this.type = parent.getType();
			this.engine = parent.getEngine();
			this.cameras = parent.getCameras();
			this.outOfBeamScannables = parent.getOutOfBeamScannables();
		}

		public Builder withIndex(int index) {
			this.index = index;
			return this;
		}

		public Builder withType(AcquisitionPropertyType type) {
			this.type = type;
			return this;
		}

		public Builder withEngine(AcquisitionEngineDocument engine) {
			this.engine = engine;
			return this;
		}

		public Builder withCameras(Set<String> cameras) {
			this.cameras = cameras;
			return this;
		}

		public Builder withOutOfBeamScannables(Set<String> outOfBeamScannables) {
			this.outOfBeamScannables = outOfBeamScannables;
			return this;
		}

		public Builder withPrimaryDataset(String primaryDataset) {
			this.primaryDataset = primaryDataset;
			return this;
		}

		public AcquisitionPropertiesDocument build() {
			return new AcquisitionPropertiesDocument(index, type, engine, cameras, outOfBeamScannables, primaryDataset);
		}
	}
}
