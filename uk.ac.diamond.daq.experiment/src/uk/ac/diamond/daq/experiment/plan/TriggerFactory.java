package uk.ac.diamond.daq.experiment.plan;

import static java.util.Objects.requireNonNull;

import java.util.function.DoubleSupplier;

import gda.device.Scannable;
import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ITrigger;
import uk.ac.diamond.daq.experiment.api.plan.Payload;
import uk.ac.diamond.daq.experiment.api.plan.PayloadService;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.plan.trigger.RepeatingTrigger;
import uk.ac.diamond.daq.experiment.plan.trigger.SingleTimeBasedTrigger;
import uk.ac.diamond.daq.experiment.plan.trigger.SingleTrigger;


/**
 * For simple and descriptive definitions of triggers. <p>
 * 
 * Examples:
 * 
 * <ol>
 * <li><pre>creator.createTrigger("diffraction_probe").tracking(temperature).executing(diffraction_scan).every(5)</pre>
 * <li><pre>creator.createTrigger("stop heating").tracking(temperature).executing(heater.stop).at(target_temp).plusOrMinus(0.2)</pre>
 * <li><pre>creator.createTrigger("say hi").timed().executing(print_greeting).every(15)</pre>
 * 
 * @see PlanCreator#createTrigger(String)
 * @see SegmentFactory#activating(TriggerFactory...)
 */
public class TriggerFactory extends PlanComponentFactory<ITrigger> {
	
	private Payload payload;
	private ExecutionPolicy policy;
	private PayloadService payloadService;

	private double interval;
	private double offset;
	
	private double target;
	private double tolerance;
	
	public TriggerFactory(String triggerName, PayloadService payloadService) {
		super(triggerName);
		this.payloadService = payloadService;
	}
	
	/**
	 * The payload this trigger carries. Usually a scan.
	 */
	public TriggerFactory executing(Object payload) {
		this.payload = payloadService.wrap(payload);
		return this;
	}
	
	/**
	 * Specify the scannable whose position
	 * governs the payload delivery.
	 */
	public TriggerFactory tracking(Scannable scannable) {
		setScannableSev(scannable);
		return this;
	}
	
	/**
	 * Specify a custom signal source
	 * to govern the payload delivery
	 */
	public TriggerFactory tracking(DoubleSupplier signalSource) {
		setCustomSev(signalSource);
		return this;
	}
	
	/**
	 * Specify a named custom signal source
	 * to govern the payload delivery
	 */
	public TriggerFactory tracking(DoubleSupplier signalSource, String name) {
		setCustomSev(signalSource, name);
		return this;
	}
	
	/**
	 * Declare this trigger time-based.
	 */
	public TriggerFactory timed() {
		setTimerSev();
		return this;
	}
	
	/**
	 * The execution period for this trigger
	 */
	public TriggerFactory every(double interval) {
		this.interval = interval;
		policy = ExecutionPolicy.REPEATING;
		return this;
	}
	
	/**
	 * An offset to add to this repeating trigger
	 */
	public TriggerFactory withOffset(double offset) {
		this.offset = offset;
		return this;
	}
	
	/**
	 * The absolute target at which to deliver this trigger's payload.
	 * 
	 * @see #plusOrMinus(double)
	 */
	public TriggerFactory at(double target) {
		this.target = target;
		policy = ExecutionPolicy.SINGLE;
		return this;
	}
	
	/**
	 * A tolerance around the specified target.
	 * 
	 * @see #at(double)
	 */
	public TriggerFactory plusOrMinus(double tolerance) {
		this.tolerance = tolerance;
		return this;
	}
	
	@Override
	ITrigger build(IPlanRegistrar registrar) {
		requireNonNull(payload, "A payload must be specified for trigger: " + getName());
		requireNonNull(getSampleEnvironmentVariable(), "A signal source must be specified for trigger: " + getName());
		
		ITrigger trigger = createTrigger(registrar);
		trigger.setName(getName());
		return trigger;
	}
	
	private ITrigger createTrigger(IPlanRegistrar registrar) {
		switch (policy) {
		case REPEATING:
			return new RepeatingTrigger(registrar, getSampleEnvironmentVariable(), payload, interval, offset);
		case SINGLE:
			switch (getSource()) {
			case POSITION:
				return new SingleTrigger(registrar, getSampleEnvironmentVariable(), payload, target, tolerance);
			case TIME:
				return new SingleTimeBasedTrigger(registrar, getSampleEnvironmentVariable(), payload, target, tolerance);
			default:
				throw new IllegalStateException("Unsupported signal source: '" + getSource());
			}
		default:
			throw new IllegalStateException("Unsupported execution policy: " + policy);
		}
	}

}
