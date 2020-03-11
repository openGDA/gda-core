package uk.ac.diamond.daq.experiment.plan;

import java.math.BigDecimal;
import java.math.RoundingMode;

import uk.ac.diamond.daq.experiment.api.plan.IPlanRegistrar;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.event.TriggerEvent;

/**
 * Works like PositionTrigger but without executor service.
 * No triggerable job but triggering signals increment trigger count (getTriggerCount())
 */
class DummySEVTrigger extends TriggerBase {

	public DummySEVTrigger(String name, IPlanRegistrar registrar, double positionInterval, ISampleEnvironmentVariable sev) {
		super(registrar, () -> null, sev);
		setName(name);
		this.thesev = sev;
		this.positionInterval = positionInterval;
		this.registrar = registrar;
	}

	private final IPlanRegistrar registrar;
	private final ISampleEnvironmentVariable thesev;
	private final double positionInterval;

	private double previousTrigger;

	private int triggerCount;

	@Override
	public void signalChanged(double signal) {
		if (!isEnabled()) return;
		if (BigDecimal.valueOf(signal).subtract(BigDecimal.valueOf(previousTrigger)).abs().divide(BigDecimal.valueOf(positionInterval), 5, RoundingMode.HALF_UP).compareTo(BigDecimal.ONE) < 0) {
			return;
		}

		previousTrigger = signal;
		registrar.triggerOccurred(this);
		registrar.triggerComplete(this, new TriggerEvent(signal), thesev.getName());
		triggerCount++;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			previousTrigger = thesev.read();
			thesev.addListener(this);
		} else {
			thesev.removeListener(this);
		}
	}

	@Override
	protected void enable() {
		// nothing else
	}

	@Override
	protected void disable() {
		// nothing else
	}

	public int getTriggerCount() {
		return triggerCount;
	}

	@Override
	protected boolean evaluateTriggerCondition(double signal) {
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(positionInterval);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(previousTrigger);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((registrar == null) ? 0 : registrar.hashCode());
		result = prime * result + ((thesev == null) ? 0 : thesev.hashCode());
		result = prime * result + triggerCount;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DummySEVTrigger other = (DummySEVTrigger) obj;
		if (Double.doubleToLongBits(positionInterval) != Double.doubleToLongBits(other.positionInterval))
			return false;
		if (Double.doubleToLongBits(previousTrigger) != Double.doubleToLongBits(other.previousTrigger))
			return false;
		if (registrar == null) {
			if (other.registrar != null)
				return false;
		} else if (!registrar.equals(other.registrar))
			return false;
		if (thesev == null) {
			if (other.thesev != null)
				return false;
		} else if (!thesev.equals(other.thesev))
			return false;
		if (triggerCount != other.triggerCount)
			return false;
		return true;
	}

}