package uk.ac.diamond.daq.experiment.driver;

import static uk.ac.diamond.daq.experiment.api.driver.DriverState.IDLE;
import static uk.ac.diamond.daq.experiment.api.driver.DriverState.PAUSED;
import static uk.ac.diamond.daq.experiment.api.driver.DriverState.RUNNING;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import gda.device.Scannable;
import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.driver.DriverState;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;

public abstract class ExperimentDriverBase<T extends DriverModel> implements IExperimentDriver<T> {
	
	private String name;
	
	private Map<String, Scannable> readouts;
	private String mainReadoutName;
	private T model;
	
	// Should this be org.eclipse.scanning.api.event.scan.DeviceState?
	protected DriverState state = IDLE;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setModel(T model) {
		this.model = model;
	}

	@Override
	public T getModel() {
		return model;
	}
	
	public void setReadouts(Map<String, Scannable> readouts) {
		this.readouts = readouts;
	}

	@Override
	public Map<String, Scannable> getReadouts() {
		return readouts;
	}
	
	@Override
	public String getMainReadoutName() {
		if (mainReadoutName == null && readouts.size() == 1) {
			mainReadoutName = readouts.values().iterator().next().getName();
		}
		Objects.requireNonNull(mainReadoutName, "One of the readouts must be nominated as 'main' - use setMainReadoutName()");
		return mainReadoutName;
	}
	
	public void setMainReadoutName(String mainReadoutName) {
		this.mainReadoutName = mainReadoutName;
	}
	
	@Override
	public void zero() {
		checkValidState("zero", IDLE);
		doZero();
	}

	@Override
	public void start() {
		checkValidState("start", IDLE);
		state = RUNNING;
		doStart();
		state = IDLE;
	}

	@Override
	public void pause() {
		checkValidState("pause", RUNNING);
		doPause();
		state = PAUSED;
	}

	@Override
	public void resume() {
		checkValidState("resume", PAUSED);
		doResume();
		state = RUNNING;
	}

	@Override
	public void abort() {
		checkValidState("abort", RUNNING, PAUSED);
		doAbort();
		state = IDLE;
	}

	@Override
	public DriverState getState() {
		return state;
	}
	
	protected abstract void doZero();
	protected abstract void doStart();
	protected abstract void doPause();
	protected abstract void doResume();
	protected abstract void doAbort();
	
	private void checkValidState(String methodName, DriverState... allowedStates) {
		List<DriverState> validStates = Arrays.asList(allowedStates);
		if (validStates.contains(state)) return;
		final String states = validStates.stream().map(Object::toString).collect(Collectors.joining(", "));
		throw new IllegalStateException("Method " + methodName + " can only be called from: " + states);
	}
	
	@Override
	public String getQuantityName() {
		return "Unknown";
	}
	
	@Override
	public String getQuantityUnits() {
		return "a. u.";
	}

}
