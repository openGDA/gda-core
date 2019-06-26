package uk.ac.diamond.daq.experiment.api.driver;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import gda.factory.FindableBase;

/**
 * This base class is essentially a {@link Serializable} version of FindableBase with {@link PropertyChangeSupport}
 */
public abstract class DriverModelBase extends FindableBase implements DriverModel {

	private static final long serialVersionUID = -5230839380845124567L;

	final PropertyChangeSupport pcs;

	public DriverModelBase() {
		pcs = new PropertyChangeSupport(this);
	}

	@Override
	public String getLabel() {
		return getName();
	}

	@Override
	public void setName(String name) {
		String oldName = getName();
		super.setName(name);
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
