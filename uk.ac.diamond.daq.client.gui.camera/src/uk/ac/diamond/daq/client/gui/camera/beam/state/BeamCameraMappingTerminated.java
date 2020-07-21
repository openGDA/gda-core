package uk.ac.diamond.daq.client.gui.camera.beam.state;

import uk.ac.diamond.daq.client.gui.camera.beam.state.BeamMappingStateContext.State;


/**
 * Terminated state for the beam mapping process.
 * <p>
 * Possible new state from this state are
 * <ul>
 * <li>
 * {@link #start(BeamMappingStateContext)} --> stays here and does nothing
 * </li>
 * <li>
 * {@link #stop(BeamMappingStateContext)} --> stays here and does nothing
 * </li>
 * </ul>
 * </p>
 * @author Maurizio Nagni
 *
 * @see BeamCameraMappingState
 */
class BeamCameraMappingTerminated implements BeamCameraMappingState {

	private final BeamMappingStateContext.State state = State.TERMINATED;

	@Override
	public void start(BeamMappingStateContext context) {
		// TODO Does nothing - terminated
	}

	@Override
	public void stop(BeamMappingStateContext context) {
		// TODO Does nothing - terminated
	}

	@Override
	public BeamMappingStateContext.State getState() {
		return state;
	}

}
