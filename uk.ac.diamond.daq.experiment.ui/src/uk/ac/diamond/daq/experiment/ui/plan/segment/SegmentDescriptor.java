package uk.ac.diamond.daq.experiment.ui.plan.segment;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.plan.trigger.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.ui.plan.trigger.TriggerDescriptor.Source;

public class SegmentDescriptor implements EditableWithListWidget {
	
	private String name = "";
	private Source source = Source.SEV;
	
	private String sevName = "";
	private Inequality ineq = Inequality.LESS_THAN;
	private double ineqRef;
	
	private double duration;
	
	private List<TriggerDescriptor> triggers = new ArrayList<>();
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldName = this.name;
		this.name = name;
		pcs.firePropertyChange("name", oldName, this.name);
	}

	public String getSevName() {
		return sevName;
	}

	public void setSevName(String sevName) {
		this.sevName = sevName;
	}

	public Inequality getIneq() {
		return ineq;
	}

	public void setIneq(Inequality ineq) {
		this.ineq = ineq;
	}

	public double getIneqRef() {
		return ineqRef;
	}

	public void setIneqRef(double ineqRef) {
		this.ineqRef = ineqRef;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public List<TriggerDescriptor> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<TriggerDescriptor> triggers) {
		this.triggers = triggers;
	}

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public EditableWithListWidget createDefault() {
		SegmentDescriptor model = new SegmentDescriptor();
		model.setName("New segment");
		model.setSource(Source.TIME);
		model.setDuration(5);
		return model;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
}
