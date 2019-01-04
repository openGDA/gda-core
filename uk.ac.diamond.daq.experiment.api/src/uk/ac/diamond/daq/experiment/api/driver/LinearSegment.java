package uk.ac.diamond.daq.experiment.api.driver;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class LinearSegment {
	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private double start;
	private double stop;
	private double duration;
	
	public double getStart() {
		return start;
	}
	public void setStart(double start) {
		double old = this.start;
		this.start = start;
		pcs.firePropertyChange("start", old, start);
	}
	public double getStop() {
		return stop;
	}
	public void setStop(double stop) {
		double old = this.stop;
		this.stop = stop;
		pcs.firePropertyChange("stop", old, stop);
	}
	public double getDuration() {
		return duration;
	}
	public void setDuration(double duration) {
		double old = this.duration;
		this.duration = duration;
		pcs.firePropertyChange("duration", old, duration);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

}
