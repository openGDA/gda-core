package uk.ac.gda.tomography.scan.editor;

public enum CorrectionCaptureMode {
	none ("None"), start ("Start"), end("End"), both("Both");
	
	private String name;
	
	private CorrectionCaptureMode(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
