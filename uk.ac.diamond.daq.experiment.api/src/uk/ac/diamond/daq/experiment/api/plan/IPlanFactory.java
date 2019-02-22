package uk.ac.diamond.daq.experiment.api.plan;

import org.eclipse.scanning.api.event.scan.ScanRequest;

public interface IPlanFactory {


	/* ~~~~~~~~~~~~  Sample Environment Variables  ~~~~~~~~~~~~ */


	/**
	 * Creates an {@link ISampleEnvironmentVariable} which samples the specified {@link SignalSource}.
	 *
	 * @param signalProvider
	 * @return reference to created sev
	 */
	ISampleEnvironmentVariable addSEV(SignalSource signalProvider);


	/**
	 * Gets a timer to use as an {@link ISampleEnvironmentVariable}.
	 */
	ISampleEnvironmentVariable addTimer();


	/* ~~~~~~~~~~~~~~~~~~~~~~  Segments  ~~~~~~~~~~~~~~~~~~~~~~ */


	/**
	 * Creates the next segment in the experiment (segments are executed in order of creation).
	 *
	 * @param name				The name of this segment.
	 * @param sev 				The {@link ISampleEnvironmentVariable} acting as source for the limiting signal
	 * @param limit 			The {@link LimitCondition} which will terminate this segment once met.
	 * @param triggers			The triggers which should be enabled during this segment
	 *
	 * @return 					Reference to the created segment
	 */
	ISegment addSegment(String name, ISampleEnvironmentVariable sev, LimitCondition limit, ITrigger... triggers);


	/**
	 * Creates the next time-based segment with the given duration
	 *
	 * @param name				The name of this segment.
	 * @param sev 				The {@link ISampleEnvironmentVariable} acting as source for the limiting signal
	 * @param duration			The duration in seconds of this segment
	 * @param triggers			The triggers which should be enabled during this segment
	 *
	 * @return Reference to the created segment
	 */
	ISegment addSegment(String name, ISampleEnvironmentVariable sev, double duration, ITrigger... triggers);


	/* ~~~~~~~~~~~~~~~~~~~~~~  Triggers  ~~~~~~~~~~~~~~~~~~~~~~ */


	/**
	 * Create a trigger that will perform an operation only once,
	 * when signal from {@code sev} = {@code target} ± {@code tolerance}
	 *
	 * @param name 				Name of this trigger
	 * @param triggerable		Triggered job
	 * @param sev 				The {@link ISampleEnvironmentVariable} used as triggering signal source
	 * @param target			The optimal triggering signal
	 * @param tolerance			The acceptable tolerance around the target signal
	 *
	 * @return 					Reference to the created trigger
	 */
	ITrigger addTrigger(String name, Triggerable triggerable, ISampleEnvironmentVariable sev, double target, double tolerance);


	/**
	 * Create a trigger that will submit a scan only once,
	 * when signal from {@code sev} = {@code target} ± {@code tolerance}
	 *
	 * @param name 				Name of this trigger
	 * @param scanRequest		Scan request to submit
	 * @param sev 				The {@link ISampleEnvironmentVariable} used as triggering signal source
	 * @param target			The optimal triggering signal
	 * @param tolerance			The acceptable tolerance around the target signal
	 *
	 * @return 					Reference to the created trigger
	 */
	ITrigger addTrigger(String name, ScanRequest<?> scanRequest, ISampleEnvironmentVariable sev, double target, double tolerance);


	/**
	 * Create a trigger that will submit a (possibly important) scan only once,
	 * when signal from {@code sev} = {@code target} ± {@code tolerance}
	 *
	 * @param name 				Name of this trigger
	 * @param scanRequest		Scan request to submit
	 * @param importantScan 	The scan importance determines the outcome of the trigger submitting this scan while another scan is running.<br>
	 * 							If {@code true}, the currently run is terminated and this one submitted.<br>
	 * 							If {@code false}, the scan submission request will be logged and ignored
	 * 							allowing the uninterrupted execution of the previous scan.<br>
	 * 							Default is {@code false}
	 * @param sev 				The {@link ISampleEnvironmentVariable} used as triggering signal source
	 * @param target			The optimal triggering signal
	 * @param tolerance			The acceptable tolerance around the target signal
	 *
	 * @return 					Reference to the created trigger
	 */
	ITrigger addTrigger(String name, ScanRequest<?> scanRequest, boolean importantScan, ISampleEnvironmentVariable sev, double target, double tolerance);


	/**
	 * Creates a trigger which will execute the given operation in specified intervals of signal from sample environment variable provided
	 *
	 * @param name of this trigger
	 * @param runnable which this trigger will perform
	 * @param sev the {@link ISampleEnvironmentVariable} this trigger will listen to
	 * @param interval or period between triggers
	 *
	 * @return reference to the created trigger
	 */
	ITrigger addTrigger(String name, Triggerable triggerable, ISampleEnvironmentVariable sev, double interval);


	/**
	 * Creates a trigger which will submit the given scan in specified intervals of signal from sample environment variable provided
	 *
	 * @param name of this trigger
	 * @param scanRequest scan request to submit
	 * @param sev the {@link ISampleEnvironmentVariable} this trigger will listen to
	 * @param interval the period between triggers
	 *
	 * @return reference to the created trigger
	 */
	ITrigger addTrigger(String name, ScanRequest<?> scanRequest, ISampleEnvironmentVariable sev, double interval);


	/**
	 * Creates a trigger which will submit a (possibly important) scan in specified intervals of signal from sample environment variable provided
	 *
	 * @param name 				Name of this trigger
	 * @param scanRequest 		Scan request to submit
	 * @param importantScan 	The scan importance determines the outcome of the trigger submitting this scan while another scan is running.<br>
	 * 							If {@code true}, the currently run is terminated and this one submitted.<br>
	 * 							If {@code false}, the scan submission request will be logged and ignored
	 * 							allowing the uninterrupted execution of the previous scan.<br>
	 * 							Default is {@code false}
	 * @param sev 				The {@link ISampleEnvironmentVariable} used as triggering signal source
	 * @param interval 			The period between triggers
	 *
	 * @return 					Reference to the created trigger
	 */
	ITrigger addTrigger(String name, ScanRequest<?> scanRequest, boolean importantScan, ISampleEnvironmentVariable sev, double interval);


	/**
	 * The registrar is used for creating components which need to report their events.
	 * @param registrar
	 */
	void setRegistrar(IPlanRegistrar registrar);

}
