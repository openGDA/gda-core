package uk.ac.diamond.daq.experiment.api.plan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.diamond.daq.experiment.api.remote.PlanRequest;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;

public class ExperimentPlanBean implements PlanRequest {

	public static final String DRIVER_NAME_PROPERTY = "experimentDriverName";

	private static final long serialVersionUID = 2836310522704078875L;
	private String name;
	private String description;

	private boolean driverUsed;
	private String experimentDriverName;
	private String experimentDriverProfile;

	private List<SegmentDescriptor> segments;

	private final PropertyChangeSupport pcs;

	public ExperimentPlanBean() {
		this.pcs = new PropertyChangeSupport(this);
	}

	@Override
	public String getPlanName() {
		return name;
	}

	public void setPlanName(String name) {
		String oldName = this.name;
		this.name = name;
		pcs.firePropertyChange("name", oldName, name);
	}

	@Override
	public String getPlanDescription() {
		return description;
	}

	public void setPlanDescription(String description) {
		String oldDescription = this.description;
		this.description = description;
		pcs.firePropertyChange("description", oldDescription, description);
	}

	@Override
	public boolean isDriverUsed() {
		return driverUsed;
	}

	public void setDriverUsed(boolean driverUsed) {
		boolean old = this.driverUsed;
		this.driverUsed = driverUsed;
		pcs.firePropertyChange("driverUsed", old, driverUsed);
	}

	@Override
	public String getExperimentDriverName() {
		return experimentDriverName;
	}

	public void setExperimentDriverName(String experimentDriverName) {
		String old = this.experimentDriverName;
		this.experimentDriverName = experimentDriverName;
		pcs.firePropertyChange(DRIVER_NAME_PROPERTY, old, experimentDriverName);
	}

	@Override
	public String getExperimentDriverProfile() {
		return experimentDriverProfile;
	}

	public void setExperimentDriverProfile(String experimentDriverProfile) {
		String old = this.experimentDriverProfile;
		this.experimentDriverProfile = experimentDriverProfile;
		pcs.firePropertyChange("experimentDriverProfile", old, experimentDriverProfile);
	}
	public List<SegmentDescriptor> getSegments() {
		return segments;
	}

	@Override
	public List<SegmentRequest> getSegmentRequests() {
		return segments.stream()
				.map(SegmentRequest.class::cast)
				.collect(Collectors.toList());
	}

	public void setSegments(List<SegmentDescriptor> segments) {
		List<SegmentDescriptor> old = this.segments;
		this.segments = segments;
		pcs.firePropertyChange("segments", old, segments);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}
}
