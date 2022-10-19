package uk.ac.diamond.daq.experiment.plan;

import java.util.function.DoubleSupplier;

import gda.device.Scannable;
import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;

/**
 * Holds common functionality for the description of general plan components.
 * @param <T> The plan component built by {@link #build(IPlanRegistrar)}
 */
public abstract class PlanComponentFactory<T> {
	
	private final String name;
	
	private SignalSource source;
	private ISampleEnvironmentVariable sev;
	
	PlanComponentFactory(String name) {
		this.name = name;
	}
	
	/**
	 * Called internally to build the component
	 * once it has been fully described.
	 */
	abstract T build(IPlanRegistrar registrar); 
	
	String getName() {
		return name;
	}
	
	SignalSource getSource() {
		return source;
	}

	ISampleEnvironmentVariable getSampleEnvironmentVariable() {
		return sev;
	}
	
	void setScannableSev(Scannable scannable) {
		sev = new SampleEnvironmentVariable(scannable);
		source = SignalSource.POSITION;
	}
	
	void setCustomSev(DoubleSupplier signalSource) {
		sev = new SampleEnvironmentVariable(signalSource);
		source = SignalSource.POSITION;
	}
	
	void setCustomSev(DoubleSupplier signalSource, String name) {
		sev = new SampleEnvironmentVariable(new ExternalSourceWrapper(signalSource, name));
		source = SignalSource.POSITION;
	}
	
	void setTimerSev() {
		sev = PlanFactory.getSystemTimer();
		source = SignalSource.TIME;
	}

}
