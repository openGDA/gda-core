package uk.ac.gda.tomography.model;

import java.util.List;

/**
 * The base class for tomography multiple acquisitions. Likely to be removed in the next iteration.
 *
 *  @author Maurzio Nagni
 */
public class TomographyExperiment implements Experiment<TomographyAcquisition> {
	private String name;
	private List<ActionLog> logs;
	private List<TomographyAcquisition> acquisitions;

	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public List<ActionLog> getLogs() {
		return logs;
	}
	public void setLogs(List<ActionLog> logs) {
		this.logs = logs;
	}

	@Override
	public List<TomographyAcquisition> getAcquisitions() {
		return acquisitions;
	}
	public void setAcquisitions(List<TomographyAcquisition> acquisitions) {
		this.acquisitions = acquisitions;
	}

}
