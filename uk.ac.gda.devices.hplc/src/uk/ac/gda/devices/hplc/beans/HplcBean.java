package uk.ac.gda.devices.hplc.beans;

import java.util.Map;

import uk.ac.gda.devices.hatsaxs.beans.ExperimentBean;

public class HplcBean extends ExperimentBean {

	private static final long serialVersionUID = 2999210681645575696L;
	public static final String DEFAULT_HPLC_MODE = "HPLC";

	public static Map<String, Boolean> MODES;
	public static void setModes(Map<String, Boolean> modes) {
		MODES = modes;
	}
	private String sampleName = "Sample";
	private double concentration;
	private double molecularWeight;
	private double timePerFrame;
	private String comment = "";
	private String buffers = "";
	private String mode = DEFAULT_HPLC_MODE;
	private double delay = 0;
	private double totalDuration = 100;
	private String columnType = "";

	public String getSampleName() {
		return sampleName;
	}
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}
	public double getConcentration() {
		return concentration;
	}
	public void setConcentration(double concentration) {
		this.concentration = concentration;
	}
	public double getMolecularWeight() {
		return molecularWeight;
	}
	public void setMolecularWeight(double molecularWeight) {
		this.molecularWeight = molecularWeight;
	}
	public double getTimePerFrame() {
		return timePerFrame;
	}
	public void setTimePerFrame(double timePerFrame) {
		this.timePerFrame = timePerFrame;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getBuffers() {
		return buffers;
	}
	public void setBuffers(String buffers) {
		this.buffers = buffers;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		if (mode == null || !MODES.containsKey(mode = mode.toUpperCase())) {
			throw new IllegalArgumentException("Mode " + mode + " is not valid");
		}
		this.mode = mode;
	}

	public void clear() {

	}
	public double getTotalDuration() {
		return totalDuration;
	}
	public void setTotalDuration(double totalDuration) {
		this.totalDuration = totalDuration;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String value) {
		columnType = value;
	}
	public double getDelay() {
		return delay;
	}
	public void setDelay(double delay) {
		this.delay = delay;
	}
}
