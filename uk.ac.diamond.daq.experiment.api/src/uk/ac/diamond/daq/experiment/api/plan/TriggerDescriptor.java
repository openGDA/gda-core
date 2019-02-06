package uk.ac.diamond.daq.experiment.api.plan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;

public class TriggerDescriptor implements EditableWithListWidget, TriggerRequest {


	private static final long serialVersionUID = 1545993638702697236L;

	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private String name;

	private String scanName;

	private SignalSource source = SignalSource.TIME;
	private ExecutionPolicy policy = ExecutionPolicy.SINGLE;

	private String sevName;
	private double target;
	private double tolerance;
	private double interval;


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
	public String getScanName() {
		return scanName;
	}
	public void setScanName(String executable) {
		this.scanName = executable;
	}
	@Override
	public String getSampleEnvironmentVariableName() {
		return sevName;
	}
	public void setSampleEnvironmentVariableName(String sevName) {
		this.sevName = sevName;
	}
	@Override
	public double getTarget() {
		return target;
	}
	public void setTarget(double target) {
		this.target = target;
	}
	@Override
	public double getTolerance() {
		return tolerance;
	}
	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}
	@Override
	public double getInterval() {
		return interval;
	}
	public void setInterval(double interval) {
		this.interval = interval;
	}
	@Override
	public SignalSource getSignalSource() {
		return source;
	}
	public void setSignalSource(SignalSource source) {
		this.source = source;
	}
	@Override
	public ExecutionPolicy getExecutionPolicy() {
		return policy;
	}
	public void setExecutionPolicy(ExecutionPolicy policy) {
		this.policy = policy;
	}
	@Override
	public String getLabel() {
		return getName();
	}
	@Override
	public EditableWithListWidget createDefault() {
		TriggerDescriptor model = new TriggerDescriptor();
		model.name = "New trigger";
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
