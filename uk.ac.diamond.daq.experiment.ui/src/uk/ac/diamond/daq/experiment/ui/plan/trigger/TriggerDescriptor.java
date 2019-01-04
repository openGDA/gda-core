package uk.ac.diamond.daq.experiment.ui.plan.trigger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import uk.ac.diamond.daq.experiment.ui.widget.EditableWithListWidget;

public class TriggerDescriptor implements EditableWithListWidget {
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public enum Source {SEV, TIME}
	public enum Mode {SINGLE, PERIODIC}
	
	private String name;
	
	private String executable;

	private Source source = Source.TIME;
	private Mode mode = Mode.SINGLE;
	
	private String sevName;
	private double target;
	private double tolerance;
	private double interval;
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		String oldName = this.name;
		this.name = name;
		pcs.firePropertyChange("name", oldName, this.name);
	}
	public String getExecutable() {
		return executable;
	}
	public void setExecutable(String executable) {
		this.executable = executable;
	}
	public String getSevName() {
		return sevName;
	}
	public void setSevName(String sevName) {
		this.sevName = sevName;
	}
	public double getTarget() {
		return target;
	}
	public void setTarget(double target) {
		this.target = target;
	}
	public double getTolerance() {
		return tolerance;
	}
	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}
	public double getInterval() {
		return interval;
	}
	public void setInterval(double interval) {
		this.interval = interval;
	}
	
	public Source getSource() {
		return source;
	}
	public void setSource(Source source) {
		this.source = source;
	}
	public Mode getMode() {
		return mode;
	}
	public void setMode(Mode mode) {
		this.mode = mode;
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
