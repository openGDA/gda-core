package gda.device.detector.xspress.xspress2data;


public class MCAReading implements Reading {
	public String roiName;
	public int elementNumber;
	public String name;
	public double[][] mcacounts;
	public int[][] mcacounts_uncorrected;
	public int roiStart;
	public int roiEnd;
	public double peakArea;
	public double peakArea_bad;


	
	public MCAReading(String roiName, int elementNumber, double[][] counts, int[][] counts_raw, String name, int roiStart, int roiEnd, double peakArea, double peakArea_bad) {
		this.elementNumber = elementNumber;
		this.mcacounts = counts;
		this.mcacounts_uncorrected = counts_raw;
		this.name = name;
		this.roiStart = roiStart;
		this.roiEnd = roiEnd;
		this.peakArea = peakArea;
		this.peakArea_bad = peakArea_bad;
		this.roiName = roiName;
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