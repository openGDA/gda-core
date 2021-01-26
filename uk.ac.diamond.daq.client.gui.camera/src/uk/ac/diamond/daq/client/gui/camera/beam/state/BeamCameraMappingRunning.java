package uk.ac.diamond.daq.client.gui.camera.beam.state;

import java.util.Optional;

import uk.ac.diamond.daq.client.gui.camera.beam.state.BeamMappingStateContext.Outcome;
import uk.ac.diamond.daq.client.gui.camera.beam.state.BeamMappingStateContext.State;

/**
 * Start state for the beam mapping process
 *
 * @author Maurizio Nagni
 *
 */
class BeamCameraMappingRunning implements BeamCameraMappingState {

	private static final BeamMappingStateContext.State state = State.RUNNING;

	@Override
	public void start(BeamMappingStateContext context) {
		// Does nothing - already started
	}

	@Override
	public void stop(BeamMappingStateContext context) {
		if (Outcome.UNAVAILABLE.equals(context.getOutcome())) {
			Optional.ofNullable(context.getMappingThread())
			.ifPresent(t -> {
				if (t.isAlive()) {
					t.interrupt();
					context.setOutcome(Outcome.ABORTED);
				}
			});
		}
		context.setState(BeamMappingStateContext.TERMINATED);
	}

	@Override
	public BeamMappingStateContext.State getState() {
		return state;
	}
}
