package uk.ac.diamond.daq.experiment.api.plan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.diamond.daq.experiment.api.remote.PlanRequest;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;

public class ExperimentPlanBean implements PlanRequest, PropertyChangeListener {

	public static final String DRIVER_PROPERTY = "driver";
	public static final String NAME_PROPERTY = "name";
	public static final String DESCRIPTION_PROPERTY = "description";

	public static final String DRIVER_USED_PROPERTY = "driverUsed";
	public static final String DRIVER_NAME_PROPERTY = "experimentDriverName";
	public static final String DRIVER_PROFILE_PROPERTY = "experimentDriverProfile";

	public static final String SEGMENTS_PROPERTY = "segments";

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
		pcs.firePropertyChange(NAME_PROPERTY, oldName, name);
	}

	@Override
	public String getPlanDescription() {
		return description;
	}

	public void setPlanDescription(String description) {
		String oldDescription = this.description;
		this.description = description;
		pcs.firePropertyChange(DESCRIPTION_PROPERTY, oldDescription, description);
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

	@Override
	public List<SegmentRequest> getSegmentRequests() {
		return segments.stream()
				.map(SegmentRequest.class::cast)
				.collect(Collectors.toList());
	}

	public List<SegmentDescriptor> getSegments() {
		return segments;
	}

	public void setSegments(List<SegmentDescriptor> segments) {
		List<SegmentDescriptor> old = this.segments;
		if (old != null) {
			old.forEach(segment -> segment.removePropertyChangeListener(this));
		}
		segments.forEach(segment -> segment.addPropertyChangeListener(this));
		this.segments = segments;
		pcs.firePropertyChange(SEGMENTS_PROPERTY, old, segments);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		// when a segment changes, I want my listeners to know about it
		pcs.firePropertyChange(event);
	}
}
