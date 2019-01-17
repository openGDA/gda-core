package uk.ac.diamond.daq.experiment.api.plan;

import java.util.Set;

public interface ISampleEnvironmentVariable {

	
	/**
	 * Register either an {@link ITrigger} or an {@link ISegment} to this SEV. If it is the first SEVListener, the SEV will begin.
	 * @param listener
	 */
	void addListener(SEVListener listener);

	/**
	 * Deregister an {@link ITrigger}/{@link ISegment} from this SEV. If it is the last SEVListener, the SEV will stop.
	 * @param listener
	 */
	void removeListener(SEVListener listener);
	
	Set<SEVListener> getListeners();

	boolean isEnabled();

	double read();

}