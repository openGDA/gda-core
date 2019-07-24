package uk.ac.diamond.daq.experiment.api.plan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.diamond.daq.experiment.api.remote.PlanRequest;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;

public class ExperimentPlanBean implements PlanRequest {

	public static final String DRIVER_PROPERTY = "driver";

	private static final long serialVersionUID = 2836310522704078875L;
	private String name;
	private String description;

	private DriverBean driverBean;

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
	public DriverBean getDriverBean() {
		return driverBean;
	}

	public void setDriverBean(DriverBean driverBean) {
		DriverBean old = this.driverBean;
		this.driverBean = driverBean;
		pcs.firePropertyChange(DRIVER_PROPERTY, old, driverBean);
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
