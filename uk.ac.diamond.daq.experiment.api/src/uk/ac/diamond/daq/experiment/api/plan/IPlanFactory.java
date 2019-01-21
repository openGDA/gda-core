package uk.ac.diamond.daq.experiment.api.plan;

public interface IPlanFactory {


	/**
	 * Creates an {@link ISampleEnvironmentVariable} which samples the specified {@link SEVSignal}
	 *
	 * @param signalProvider
	 * @return reference to created sev
	 */
	ISampleEnvironmentVariable addSEV(SEVSignal signalProvider);


	/**
	 * Creates the next segment in the experiment (segments are executed in order of creation).
	 *
	 * @param sev The {@link ISampleEnvironmentVariable} whose signal will be tested for the LimitCondition
	 * @param limit The {@link LimitCondition} which will terminate this segment once met.
	 * @return reference to the created segment
	 */
	ISegment addSegment(String name, ISampleEnvironmentVariable sev, LimitCondition limit, ITrigger... triggers);


	/**
	 * Creates a time-based segment with a given duration
	 *
	 * @param name
	 * @param duration in seconds
	 * @param triggers
	 * @return reference to the created segment
	 */
	ISegment addSegment(String name, ISampleEnvironmentVariable sev, double duration, ITrigger... triggers);


	/**
	 * Creates a trigger which will execute the given operation in specified intervals of signal from given sev
	 *
	 * @param sev the {@link ISampleEnvironmentVariable} this trigger will listen to
	 * @param runnable which this trigger will perform
	 * @param triggerInterval
	 * @return reference to the created trigger
	 */
	ITrigger addTrigger(String name, ISampleEnvironmentVariable sev, Triggerable triggerable, double triggerInterval);


	/**
	 * Create an {@link ITrigger} that will trigger an {@code operation} only once,
	 * when signal from {@code sev} = {@code triggerSignal} ± {@code tolerance}
	 *
	 * @param sev
	 * @param runnable
	 * @param triggerSignal
	 * @param tolerance
	 * @return reference to the created trigger
	 */
	ITrigger addTrigger(String name, ISampleEnvironmentVariable sev, Triggerable triggerable, double triggerSignal, double tolerance);


	/**
	 * The registrar is used for creating components which need to report their events.
	 * @param registrar
	 */
	void setRegistrar(IPlanRegistrar registrar);

}
