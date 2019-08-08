package uk.ac.diamond.daq.experiment.api.plan;

import java.io.Serializable;

public class DriverBean implements Serializable {

	private static final long serialVersionUID = 8840080167686307649L;

	public static final String DRIVER_PROPERTY = "driver";
	public static final String PROFILE_PROPERTY = "profile";

	private String driver;
	private String profile;

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((driver == null) ? 0 : driver.hashCode());
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DriverBean other = (DriverBean) obj;
		if (driver == null) {
			if (other.driver != null)
				return false;
		} else if (!driver.equals(other.driver)) {
			return false;
		}
		if (profile == null) {
			if (other.profile != null)
				return false;
		} else if (!profile.equals(other.profile)) {
			return false;
		}
		return true;
	}

}
