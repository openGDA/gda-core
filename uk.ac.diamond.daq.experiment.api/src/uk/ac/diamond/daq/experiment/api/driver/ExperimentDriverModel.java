package uk.ac.diamond.daq.experiment.api.driver;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This model contains:
 * <ol>
 * <li>the driver profile, as a {@link MultiSegmentModel}</li>
 * <li>software limits</li>
 * <li>abort conditions</li>
 * </ol>
 * @author Douglas Winter
 *
 */
public class ExperimentDriverModel implements Serializable {

	private static final long serialVersionUID = -4072720382672143028L;
	private List<DriverProfileSection> profile;
	private Double maxLimit;
	private Double minLimit;
	private Set<AbortCondition> abortConditions;
	
	public List<DriverProfileSection> getProfile() {
		return profile;
	}
	
	public void setProfile(List<DriverProfileSection> profile) {
		this.profile = profile;
	}
	
	public Double getMaxLimit() {
		return maxLimit;
	}
	
	public void setMaxLimit(Double maxLimit) {
		this.maxLimit = maxLimit;
	}
	
	public Double getMinLimit() {
		return minLimit;
	}
	
	public void setMinLimit(Double minLimit) {
		this.minLimit = minLimit;
	}
	
	public Set<AbortCondition> getAbortConditions() {
		return abortConditions != null ? abortConditions : Collections.emptySet();
	}
	
	public void setAbortConditions(Set<AbortCondition> abortConditions) {
		this.abortConditions = abortConditions;
	}
	
	public void addAbortCondition(AbortCondition condition) {
		if (abortConditions == null) abortConditions = new HashSet<>();
		abortConditions.add(condition);
	}

}
