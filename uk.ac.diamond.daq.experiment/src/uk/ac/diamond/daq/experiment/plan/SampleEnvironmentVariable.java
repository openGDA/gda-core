package uk.ac.diamond.daq.experiment.plan;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.SEVListener;
import uk.ac.diamond.daq.experiment.api.plan.SEVSignal;

/**
 * Instances of this class will sample a {@link SEVSignal} at a specified frequency, and
 * when the signal changes significantly (i.e. change >= tolerance), will notify its {@link SEVListener}s.
 * It will stop sampling if there are no listeners registered.
 *
 */
public class SampleEnvironmentVariable implements ISampleEnvironmentVariable {
	
	private static final Logger logger = LoggerFactory.getLogger(SampleEnvironmentVariable.class);
	
	private SEVSignal signalProvider;
	private Set<SEVListener> listeners;
	private boolean enabled;
	private double lastPosition;
	private double tolerance;
	
	/**
	 * @param signalProvider
	 */
	SampleEnvironmentVariable(SEVSignal signalProvider) {
		this(signalProvider, 0.01);
	}
	
	public SampleEnvironmentVariable(SEVSignal signalProvider, double tolerance) {
		this.signalProvider = signalProvider;
		this.lastPosition = signalProvider.read();
		this.tolerance = tolerance;
		clear();
	}
	
	private void begin() {
		if (listeners.isEmpty()) {
			enabled = false;
			logger.info("No listeners registered to this SEV. Disabling.");
			return;
		}
		final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
			Thread thread = new Thread(runnable);
			thread.setName("Sample Environment Variable");
			thread.setDaemon(true);
			return thread;
		});
		
		executorService.scheduleAtFixedRate(()->{
			double newPosition = signalProvider.read();
			if (Math.abs(lastPosition - newPosition) >= tolerance) {
				listeners.forEach(listener -> listener.signalChanged(newPosition));
				lastPosition = newPosition;
			}
		}, 0, 1, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void addListener(SEVListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
			if (!enabled) setEnabled(true);
		}
	}
	
	@Override
	public void removeListener(SEVListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
			if (listeners.isEmpty()) setEnabled(false);
		}
	}
	
	private void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (enabled) begin();
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public double read() {
		return signalProvider.read();
	}
	
	@Override
	public Set<SEVListener> getListeners() {
		return listeners;
	}
	
	private void clear() {
		listeners = new CopyOnWriteArraySet<>();
		setEnabled(false);
	}

	@Override
	public String toString() {
		return "SampleEnvironmentVariable [signalProvider=" + signalProvider + ", enabled="
				+ enabled + "]";
	}
	
}
