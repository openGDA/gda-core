package uk.ac.diamond.daq.experiment.plan;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.SEVListener;
import uk.ac.diamond.daq.experiment.api.plan.SEVSignal;

/**
 * This implementation does not poll a {@link SEVSignal}. Methods {@link #broadcast(double)} will
 * set and broadcast the desired signal, and {@link #ramp(double, double)} will call broadcast incrementally
 * until the desired signal is reached.
 */
public class MockSEV implements ISampleEnvironmentVariable {

	private double signal;
	private Set<SEVListener> listeners = new CopyOnWriteArraySet<>();
	private boolean enabled;

	@Override
	public void addListener(SEVListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
			if (!enabled) enabled = true;
		}
	}

	@Override
	public void removeListener(SEVListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
			if (listeners.isEmpty()) enabled = false;
		}
	}

	@Override
	public Set<SEVListener> getListeners() {
		return listeners;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public double read() {
		return signal;
	}

	public void broadcast(double signal) {
		this.signal = signal;
		listeners.forEach(listener -> listener.signalChanged(signal));
	}

	public void ramp(double target, double increment) {
		if (increment == 0) throw new IllegalArgumentException("increment is 0");
		if (increment > 0) {
			while (signal < target) {
				broadcast(signal + increment);
			}
		} else {
			while (signal > target) {
				broadcast(signal + increment);
			}
		}
	}
}
