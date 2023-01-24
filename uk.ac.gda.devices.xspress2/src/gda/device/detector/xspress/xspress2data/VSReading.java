package gda.device.detector.xspress.xspress2data;

public class VSReading implements Reading {
	protected String roiName; // name of the roi, so Readings from different elements can be asscoiated with each other
	protected int elementNumber;
	protected double[] counts;
	// maybe used in a note in the future
	protected String name; // unique name for this reading within this frame
	protected boolean contributesToFF = false; // if OUT of window, then should not be used to calculate FF

	protected VSReading(String roiName, int elementNumber, double[] counts, String name, boolean contributesToFF) {
		this.elementNumber = elementNumber;
		this.counts = counts;
		this.name = name;
		this.roiName = roiName;
		this.contributesToFF = contributesToFF;
	}

	@Override
	public int getElementNumber() {
		return elementNumber;
	}

	@Override
	public String getRoiName() {
		return roiName;
	}
}