package uk.ac.diamond.daq.experiment.api.driver;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

/**
 * This base class is essentially a {@link Serializable} version of FindableBase with {@link PropertyChangeSupport}
 */
public abstract class DriverModelBase implements DriverModel {

	private static final long serialVersionUID = -5230839380845124567L;
	private String name;

	final PropertyChangeSupport pcs;

	public DriverModelBase() {
		pcs = new PropertyChangeSupport(this);
	}

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		String oldName = getName();
		this.name = name;
		pcs.firePropertyChange("name", oldName, name);
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
