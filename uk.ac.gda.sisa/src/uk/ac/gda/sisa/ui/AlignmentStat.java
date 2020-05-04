package uk.ac.gda.sisa.ui;

import gda.device.Scannable;

public class AlignmentStat {

	private String label;
	private Scannable scannable;
	
	public AlignmentStat(String label, Scannable scannable) {
		this.label = label;
		this.scannable = scannable;
	}
	
	public String getLabel() {
		return label;
	}
	
	public Scannable getScannable () {
		return scannable;
	}
}
