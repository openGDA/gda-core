package uk.ac.diamond.daq.experiment.api.plan;

import java.util.List;
import java.util.stream.Collectors;

import uk.ac.diamond.daq.experiment.api.remote.PlanRequest;
import uk.ac.diamond.daq.experiment.api.remote.SegmentRequest;

public class ExperimentPlanBean implements PlanRequest {

	private static final long serialVersionUID = 2836310522704078875L;
	private String name;
	private String description;

	private String experimentDriverName;
	private List<SegmentDescriptor> segments;
	private String experimentDriverProfile;


	@Override
	public String getPlanName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getPlanDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getExperimentDriverName() {
		return experimentDriverName;
	}

	public void setExperimentDriverName(String experimentDriverName) {
		this.experimentDriverName = experimentDriverName;
	}

	public List<SegmentDescriptor> getSegments() {
		return segments;
	}

	public void setSegments(List<SegmentDescriptor> segments) {
		this.segments = segments;
	}

	@Override
	public List<SegmentRequest> getSegmentRequests() {
		return segments.stream()
				.map(SegmentRequest.class::cast)
				.collect(Collectors.toList());
	}

	@Override
	public String getExperimentDriverProfile() {
		return experimentDriverProfile;
	}

	public void setExperimentDriverProfile(String experimentDriverProfile) {
		this.experimentDriverProfile = experimentDriverProfile;
	}
}
