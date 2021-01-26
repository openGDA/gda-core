package uk.ac.diamond.daq.client.gui.camera.beam.state;

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;

import uk.ac.diamond.daq.client.gui.camera.beam.BeamCameraMapping;
import uk.ac.diamond.daq.client.gui.camera.beam.state.BeamMappingStateContext.State;


/**
 * Ready state for the beam mapping process.
 * <p>
 * Possible new state from this state are
 * <ul>
 * <li>
 * {@link #start(BeamMappingStateContext)} --> {@link BeamCameraMappingRunning}
 * </li>
 * <li>
 * {@link #stop(BeamMappingStateContext)} --> {@link BeamCameraMappingTerminated}
 * </li>
 * </ul>
 * </p>
 * @author Maurizio Nagni
 *
 * @see BeamCameraMappingState
 */
class BeamCameraMappingReady implements BeamCameraMappingState {

	private static final BeamMappingStateContext.State state = State.READY;

	@Override
	public void start(BeamMappingStateContext context) {
		BeamCameraMapping ic = getBean(BeamCameraMapping.class);
		// advances the mapping state to start
		context.setMappingThread(new Thread(() -> ic.calibrate(context)));
		// start the mapping thread so the GUI can be updated meanwhile the mapping is running
		context.getMappingThread().start();
		context.setState(BeamMappingStateContext.RUNNING);
	}

	@Override
	public void stop(BeamMappingStateContext context) {
		context.setState(BeamMappingStateContext.TERMINATED);
	}

	@Override
	public BeamMappingStateContext.State getState() {
		return state;
	}

}
