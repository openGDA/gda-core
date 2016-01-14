package uk.ac.gda.devices.excalibur;

import gda.device.Scannable;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.impl.NDBaseImpl;
import gda.device.scannable.ScannableUtils;

import java.util.List;
import java.util.Random;

import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;


public class ExcaliburNDArraySimulator extends NDBaseImpl implements NDArray {

	double height=0.;
	double centre=0.;
	double width=1.;
	boolean reportSCurve=false;

	/**
	 * @return Returns the reportSCurve.
	 * If false the data for each pixel is the value of a Gaussian
	 * If true the data for each pixel is the cumulativeProbability of a Gaussian
	 */
	public boolean isReportSCurve() {
		return reportSCurve;
	}
	/**
	 * @param reportSCurve The reportSCurve to set.
	 */
	public void setReportSCurve(boolean reportSCurve) {
		this.reportSCurve = reportSCurve;
	}

	/*
	 * Back door to threshold scannable  being scanned - to allow the simulation to know the position in the scan
	 */
	Scannable threshold;

	/**
	 * @return Returns the threshold.
	 */
	public Scannable getThreshold() {
		return threshold;
	}
	/**
	 * @param threshold The threshold to set.
	 */
	public void setThreshold(Scannable threshold) {
		this.threshold = threshold;
	}


	List<ExcaliburReadoutNodeFem> fems;



	/**
	 * @return Returns the fems.
	 */
	public List<ExcaliburReadoutNodeFem> getFems() {
		return fems;
	}
	/**
	 * @param fems The fems to set.
	 */
	public void setFems(List<ExcaliburReadoutNodeFem> fems) {
		this.fems = fems;
	}
	public double getHeight() {
		return height;
	}
	public void setHeight(double height) {
		this.height = height;
		heights = null;
	}
	public double getCentre() {
		return centre;
	}
	public void setCentre(double centre) {
		this.centre = centre;
		heights = null;
	}
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
		heights = null;
	}

	@Override
	public short[] getShortArrayData(int numberOfElements) throws Exception {
		int w = getPluginBase().getArraySize0_RBV();
		int h = getPluginBase().getArraySize1_RBV();

		if( widthUsed != w || heightUsed != h || heights==null){
			handleWidthHeight(w, h);
		}

		short [] ldata = new short[w * h];
		double xVal = ScannableUtils.getCurrentPositionArray(threshold)[0];
		short adjustment = fems.get(0).getMpxiiiChipReg1().getPixel().getThresholdA()[0];
		for (int index = 0; index < ldata.length; index++) {
			if(reportSCurve){
				ldata[index] = (short)( heights[index]* distributions[index].cumulativeProbability(xVal-adjustment));
			} else {
				ldata[index] = (short) (heights[index] * Math.exp(-0.5 * Math.pow(((xVal - centres[index]-adjustment) / widths[index]), 2.0)));
			}
		}
		if( deadRows != null){
			for( Integer i : deadRows){
				if( i < w && i>=0){
					for (int j = 0; j < h; j++) {
						ldata[i*h +j]=-1;
					}
				}
			}
		}

		return ldata;
	}

	@Override
	public byte[] getByteArrayData(int numberOfElements) throws Exception {
		throw new UnsupportedOperationException("Only getIntArrayData is supported");
	}

	int widthUsed=-1;
	int heightUsed=-1;
	List<Integer> deadRows;

	public List<Integer> getDeadRows() {
		return deadRows;
	}
	public void setDeadRows(List<Integer> deadRows) {
		this.deadRows = deadRows;
	}
	@Override
	public int[] getIntArrayData(int numberOfElements) throws Exception {
/*		if(getPluginBase().getDataType_RBV() != NDPluginBase.UInt32)
			throw new UnsupportedOperationException("Only getIntArrayData for  NDPluginBase.UInt32 is supported");
*/
		int w = getPluginBase().getArraySize0_RBV();
		int h = getPluginBase().getArraySize1_RBV();

		if( widthUsed != w || heightUsed != h || heights==null){
			handleWidthHeight(w, h);
		}

		int [] ldata = new int[w * h];
		double xVal = ScannableUtils.getCurrentPositionArray(threshold)[0];
		short adjustment = fems.get(0).getMpxiiiChipReg1().getPixel().getThresholdA()[0];
		for (int index = 0; index < ldata.length; index++) {
			if(reportSCurve){
				ldata[index] = (int)( heights[index]* distributions[index].cumulativeProbability(xVal-adjustment));
			} else {
				ldata[index] = (int) (heights[index] * Math.exp(-0.5 * Math.pow(((xVal - centres[index]-adjustment) / widths[index]), 2.0)));
			}
		}
		if( deadRows != null){
			for( Integer i : deadRows){
				if( i < w && i>=0){
					for (int j = 0; j < h; j++) {
						ldata[i*h +j]=-1;
					}
				}
			}
		}

		return ldata;
	}

	double[] heights; //if null force rebuild
	double[] widths;
	double[] centres;
	NormalDistribution[] distributions;

	private double heightRandomFactor=0.1;
	private double widthRandomFactor=0.1;
	private double centreRandomFactor=0.1;

	public double getHeightRandomFactor() {
		return heightRandomFactor;
	}
	public void setHeightRandomFactor(double heightRandomFactor) {
		this.heightRandomFactor = heightRandomFactor;
		heights = null;
	}
	public double getWidthRandomFactor() {
		return widthRandomFactor;
	}
	public void setWidthRandomFactor(double widthRandomFactor) {
		this.widthRandomFactor = widthRandomFactor;
		heights = null;
	}
	public double getCentreRandomFactor() {
		return centreRandomFactor;
	}
	public void setCentreRandomFactor(double centreRandomFactor) {
		this.centreRandomFactor = centreRandomFactor;
		heights = null;
	}
	private void handleWidthHeight(int w, int h) {
		Random heightRandomizer = new Random();
		Random centreRandomizer = new Random();
		Random widthRandomizer = new Random();
		heights = new double[w*h];
		widths = new double[w*h];
		centres = new double[w*h];
		distributions = new NormalDistribution[w*h];
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				heights[i*h +j] = height*Math.max(0.001,(1 + heightRandomFactor*heightRandomizer.nextGaussian()));
				widths[i*h +j] = width*Math.max(0.001,(1 + widthRandomFactor*widthRandomizer.nextGaussian()));
				centres[i*h +j] = centre*Math.max(0.001,(1 + centreRandomFactor*centreRandomizer.nextGaussian()));
				distributions[i*h+j] = new NormalDistributionImpl(centres[i*h +j], widths[i*h +j]);
			}
		}
		widthUsed = w;
		heightUsed = h;

	}
	@Override
	public float[] getFloatArrayData(int numberOfElements) throws Exception {
		throw new UnsupportedOperationException("Only getIntArrayData is supported");
	}

	@Override
	public float[] getFloatArrayData() throws Exception {
		throw new UnsupportedOperationException("Only getIntArrayData is supported");
	}

	@Override
	public void reset() throws Exception {
		//do nothing
	}

	@Override
	public byte[] getByteArrayData() throws Exception {
		throw new UnsupportedOperationException("Only getIntArrayData is supported");
	}
	@Override
	public Object getImageData(int expectedNumPixels) throws Exception {
		throw new UnsupportedOperationException("getImageData is supported");
	}

	@Override
	public double[] getDoubleArrayData(int numberOfElements) throws Exception {
		throw new UnsupportedOperationException("Only getIntArrayData is supported");
	}

	@Override
	public double[] getDoubleArrayData() throws Exception {
		throw new UnsupportedOperationException("Only getIntArrayData is supported");
	}


}
