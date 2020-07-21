package uk.ac.diamond.daq.client.gui.camera.beam.state;

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;

import java.util.Optional;
import java.util.function.Supplier;

import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.beam.BeamCameraCalibrationComposite;
import uk.ac.diamond.daq.client.gui.camera.beam.event.BeamMappingStateEvent;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;

/**
 * Keep track of the actual beam mapping process and provide a common context to
 * the all the components involved in the mapping
 *
 * The class provides a public {@link State} of the mapping while internally
 * relies on package restricted state pattern classes.
 *
 * <p>
 * The instance is created in a {@link #READY} state.
 * </p>
 *
 * @author Maurizio Nagni
 *
 * @see BeamCameraCalibrationComposite
 * @see BeamCameraMappingState
 * @see BeamMappingStateEvent
 */
public class BeamMappingStateContext {

	/**
	 * The state of the mapping process. This enumeration provides the externally
	 * visible state of the process while this class
	 *
	 */
	public enum State {
		READY, RUNNING, TERMINATED
	}

	public enum Outcome {
		UNAVAILABLE, SUCCESS, FAILED, ABORTED
	}

	/**
	 * The ready state class
	 */
	static final BeamCameraMappingState READY = new BeamCameraMappingReady();
	/**
	 * The start state class
	 */
	static final BeamCameraMappingState RUNNING = new BeamCameraMappingRunning();
	/**
	 * The stop state class
	 */
	static final BeamCameraMappingState TERMINATED = new BeamCameraMappingTerminated();

	private Outcome outcome = Outcome.UNAVAILABLE;

	/**
	 * Supplies the camera configuration associated with the mapping
	 */
	private final Supplier<ICameraConfiguration> cameraConfiguration;
	private final int xSamplePoints;
	private final int ySamplePoints;

	/**
	 * The thread where the mapping is executed
	 */
	private Thread mappingThread;

	/**
	 * The actual internal state of the mapping thread. Starts as {@link #STOP}
	 */
	private BeamCameraMappingState state = READY;

	/**
	 * Creates a context for a beam to a camera mapping
	 *
	 * @param cameraConfiguration the camera to map
	 * @param xSamplePoints grid resolution on x axis
	 * @param ySamplePoints grid resolution on y axis
	 */
	public BeamMappingStateContext(Supplier<ICameraConfiguration> cameraConfiguration, int xSamplePoints, int ySamplePoints) {
		this.cameraConfiguration = cameraConfiguration;
		this.xSamplePoints = xSamplePoints;
		this.ySamplePoints = ySamplePoints;
	}

	/**
	 * Starts the mapping thread
	 */
	public void start() {
		// As the start state is STOP, this call really calls STOP.start(this)
		state.start(this);
	}

	/**
	 * Starts the mapping thread
	 */
	public void stop() {
		state.stop(this);
	}

	/**
	 * Returns the actual mapping state
	 * @return the mapping state
	 */
	public State getState() {
		return state.getState();
	}

	/**
	 * The {@link CameraConfiguration} related to the mapping process
	 * @return the relevant camera configuration
	 */
	public Optional<ICameraConfiguration> getCameraConfiguration() {
		return Optional.ofNullable(cameraConfiguration).map(Supplier::get);
	}

	public int getxSamplePoints() {
		return xSamplePoints;
	}

	public int getySamplePoints() {
		return ySamplePoints;
	}

	/**
	 * Set the current state. Normally only called by classes implementing the State
	 * interface.
	 *
	 * @param newState the new state of this context
	 */
	void setState(BeamCameraMappingState newState) {
		state = newState;
		publishEvent(new BeamMappingStateEvent(this, state.getState(), getOutcome()));
	}

	Thread getMappingThread() {
		return mappingThread;
	}

	/**
	 * Sets the thread running the mapping process. The method is restricted to
	 * {@code package} classes so that only classes implementing
	 * {@code BeamCameraMappingState} may set it
	 *
	 * @param mappingThread
	 */
	void setMappingThread(Thread mappingThread) {
		this.mappingThread = mappingThread;
	}

	public Outcome getOutcome() {
		return outcome;
	}

	public void setOutcome(Outcome outcome) {
		this.outcome = outcome;
	}
}
