package uk.ac.diamond.daq.experiment.api.plan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.diamond.daq.experiment.api.remote.Inequality;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;

public class SegmentDescriptor implements EditableWithListWidget, SegmentRequest {
	
	private static final long serialVersionUID = 4022241468104721756L;
	private String name;
	private SignalSource source;
	
	private String sevName;
	private Inequality ineq;
	private double ineqRef;
	
	private double duration;
	
	private List<TriggerDescriptor> triggers = new ArrayList<>();
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldName = this.name;
		this.name = name;
		pcs.firePropertyChange("name", oldName, this.name);
	}

	@Override
	public String getSampleEnvironmentVariableName() {
		return sevName;
	}

	public void setSevName(String sevName) {
		this.sevName = sevName;
	}

	@Override
	public Inequality getInequality() {
		return ineq;
	}

	public void setIneq(Inequality ineq) {
		this.ineq = ineq;
	}

	@Override
	public double getInequalityArgument() {
		return ineqRef;
	}

	public void setIneqRef(double ineqRef) {
		this.ineqRef = ineqRef;
	}

	@Override
	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	@Override
	public SignalSource getSignalSource() {
		return source;
	}

	public void setSignalSource(SignalSource source) {
		this.source = source;
	}

	public List<TriggerDescriptor> getTriggers() {
		return triggers;
	}
	
	@Override
	public List<TriggerRequest> getTriggerRequests() {
		return triggers.stream()
					.map(TriggerRequest.class::cast)
					.collect(Collectors.toList());
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
		model.setName("Unnamed segment");
		model.setSignalSource(SignalSource.TIME);
		model.setIneq(Inequality.LESS_THAN);
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
