package uk.ac.diamond.daq.experiment.api.structure;

import java.util.Objects;

import org.eclipse.scanning.api.event.IdBean;

public class ExperimentEvent extends IdBean {

	public enum Transition {
		STARTED, STOPPED
	}

	private String experimentName;
	private Transition transition;

	public ExperimentEvent() {}

	public ExperimentEvent(String experimentName, Transition transition) {

		this.experimentName = experimentName;
		this.transition = transition;
	}

	public String getExperimentName() {
		return experimentName;
	}

	public void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}

	/**
	 * The transition which caused this event
	 */
	public Transition getTransition() {
		return transition;
	}

	public void setTransition(Transition transition) {
		this.transition = transition;
	}

	private static final long serialVersionUID = -5323571169497554790L;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(experimentName, transition);
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
		ExperimentEvent other = (ExperimentEvent) obj;
		return Objects.equals(experimentName, other.experimentName) && transition == other.transition;
	}

}
