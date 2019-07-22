package uk.ac.diamond.daq.experiment.api.plan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;

public class TriggerDescriptor implements EditableWithListWidget, TriggerRequest {

	public static final String NAME_PROPERTY = "name";
	public static final String SCAN_PROPERTY = "scanName";
	public static final String SOURCE_PROPERTY = "signalSource";
	public static final String EXECUTION_POLICY_PROPERTY = "executionPolicy";
	public static final String SEV_PROPERTY = "sampleEnvironmentVariableName";
	public static final String TARGET_PROPERTY = "target";
	public static final String TOLERANCE_PROPERTY = "tolerance";
	public static final String INTERVAL_PROPERTY = "interval";


	private static final long serialVersionUID = 1545993638702697236L;

	private final PropertyChangeSupport pcs;

	public TriggerDescriptor() {
		pcs = new PropertyChangeSupport(this);
	}

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
		pcs.firePropertyChange(NAME_PROPERTY, oldName, this.name);
		pcs.firePropertyChange(REFRESH_PROPERTY, oldName, name);
	}

	@Override
	public String getScanName() {
		return scanName;
	}

	public void setScanName(String executable) {
		String old = this.scanName;
		this.scanName = executable;
		pcs.firePropertyChange(SCAN_PROPERTY, old, executable);
	}

	@Override
	public String getSampleEnvironmentVariableName() {
		return sevName;
	}

	public void setSampleEnvironmentVariableName(String sevName) {
		String old = this.sevName;
		this.sevName = sevName;
		pcs.firePropertyChange(SEV_PROPERTY, old, sevName);
	}

	@Override
	public double getTarget() {
		return target;
	}

	public void setTarget(double target) {
		double old = this.target;
		this.target = target;
		pcs.firePropertyChange(TARGET_PROPERTY, old, target);
	}

	@Override
	public double getTolerance() {
		return tolerance;
	}

	public void setTolerance(double tolerance) {
		double old = this.tolerance;
		this.tolerance = tolerance;
		pcs.firePropertyChange(TOLERANCE_PROPERTY, old, tolerance);
	}

	@Override
	public double getInterval() {
		return interval;
	}

	public void setInterval(double interval) {
		double old = this.interval;
		this.interval = interval;
		pcs.firePropertyChange(INTERVAL_PROPERTY, old, interval);
	}

	@Override
	public SignalSource getSignalSource() {
		return source;
	}

	public void setSignalSource(SignalSource source) {
		SignalSource old = this.source;
		this.source = source;
		pcs.firePropertyChange(SOURCE_PROPERTY, old, source);
	}

	@Override
	public ExecutionPolicy getExecutionPolicy() {
		return policy;
	}

	public void setExecutionPolicy(ExecutionPolicy policy) {
		ExecutionPolicy old = this.policy;
		this.policy = policy;
		pcs.firePropertyChange(EXECUTION_POLICY_PROPERTY, old, policy);
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
