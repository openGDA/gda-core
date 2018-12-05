package uk.ac.diamond.daq.client.gui.camera;

public abstract class DiadConfigurationModel {
	public String sectionName;

	private String version;

	protected DiadConfigurationModel(String sectionName, String version) {
		this.sectionName = sectionName;
		this.version = version;
	}

	public String getSectionName() {
		return sectionName;
	}

	public String getVersion() {
		return version;
	}
}
