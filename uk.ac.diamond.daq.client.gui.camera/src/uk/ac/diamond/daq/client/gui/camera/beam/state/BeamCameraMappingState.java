package uk.ac.diamond.daq.client.gui.camera.beam.state;

/**
 * Base class for the beam mapping state pattern
 * 
 * @author Maurizio Nagni
 *
 */
public interface BeamCameraMappingState {
	/**
	 * Starts the mapping
	 * @param context the actual mapping configuration and state
	 */
	void start(BeamMappingStateContext context);

	/**
	 * Stops the mapping
	 * @param context the actual mapping configuration and state
	 */
	void stop(BeamMappingStateContext context);
	
	BeamMappingStateContext.State getState();
}
