/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.device.detector.xmap.edxd;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.epicsdevice.ReturnType;
import gda.device.epicsdevice.XmapEpicsDevice;
import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Quadratic;
import uk.ac.diamond.scisoft.analysis.optimize.LeastSquares;

public class EDXDElement extends DetectorBase implements IEDXDElement {

	private static final String REALTIME = "REALTIME";
	private static final String ELIVETIME = "ELIVETIME";
	private static final String TLIVETIME = "TLIVETIME";
	private static final String GETNBINS = "GETNBINS";
	private static final String DATA = "DATA";
	private static final String EVENTS = "EVENTS";
	private static final String INPUTCOUNTRATE = "INPUTCOUNTRATE";
	private static final String OUTPUTCOUNTRATE = "OUTPUTCOUNTRATE";
	private static final String ENERGYBINS = "ENERGYBINS";
	private static final String PEAKTIME = "PEAKTIME";
	private static final String DYNRANGE = "DYNRANGE";
	private static final String TRIGTHRESH = "TRIGTHRESH";
	private static final String BASETHRESH = "BASETHRESH";
	private static final String BASELENGTH = "BASELENGTH";
	private static final String ENERGYTHRESH = "ENERGYTHRESH";
	private static final String BINWIDTH = "BINWIDTH";
	private static final String PREAMPGAIN = "PREAMPGAIN";
	private static final String RESETDELAY = "RESETDELAY";
	private static final String GAPTIME = "GAPTIME";
	private static final String TRIGPEAKTIME = "TRIGPEAKTIME";
	private static final String TRIGGAPTIME = "TRIGGAPTIME";
	private static final String MAXWIDTH = "MAXWIDTH";

	private double a = 0.0;
	private double b = 1.0;
	private double c = 0.0;
	private double theta = 15.0;
	private double[] energy = null;
	private double[] q = null;

	protected XmapEpicsDevice xmap;
	private Integer number;
	private static final String SCADATA = "SCADATA";
	private static final String SCALOWLIMITS = "SCALOWLIMITS";
	private static final String SCAHIGHLIMITS = "SCAHIGHLIMITS";

	/**
	 * @param xmapDevice the device where the element is connected to
	 * @param elementNumber the number of the element in the xmap
	 */
	public EDXDElement(XmapEpicsDevice xmapDevice, int elementNumber ) {
		number = elementNumber;
		xmap = xmapDevice;
	}

	/**
	 * This detector dose not create its own files
	 * @return false
	 * @throws DeviceException
	 */
	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int [] {(Integer) xmap.getValue(ReturnType.DBR_NATIVE,GETNBINS,"")};
	}

	/**
	 * @return the energy live time
	 * @throws DeviceException
	 */
	@Override
	public double getEnergyLiveTime() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,ELIVETIME+number.toString(),"");
	}

	/**
	 * the trigger live time
	 * @return the reported trigger live time
	 * @throws DeviceException
	 */
	@Override
	public double getTriggerLiveTime() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,TLIVETIME+number.toString(),"");
	}

	/**
	 * @return the real time
	 * @throws DeviceException
	 */
	@Override
	public double getRealTime() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,REALTIME+number.toString(),"");
	}

	/**
	 * @return the number of events
	 * @throws DeviceException
	 */
	@Override
	public int getEvents() throws DeviceException {
		return (Integer) xmap.getValue(ReturnType.DBR_NATIVE,EVENTS+number.toString(),"");
	}

	/**
	 * @return the input count rate
	 * @throws DeviceException
	 */
	@Override
	public double getInputCountRate() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,INPUTCOUNTRATE+number.toString(),"");
	}

	/**
	 * @return The output count rate
	 * @throws DeviceException
	 */
	@Override
	public double getOutputCountRate() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,OUTPUTCOUNTRATE+number.toString(),"");
	}

	/**
	 * @return a double array containing the energy value per bin on the outputed data
	 * @throws DeviceException
	 */
	@Override
	public double[] getEnergyBins() throws DeviceException {
		final double[] data = (double[]) xmap.getValue(ReturnType.DBR_NATIVE, ENERGYBINS + number.toString(), "");
		final double[] result = new double[getDataDimensions()[0]];
		for(int i = 0; i < result.length; i++)
			result[i] = data[i];
		return result;
	}

	/**
	 * @return the peak time
	 * @throws DeviceException
	 */
	@Override
	public double getPeakTime() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GET"+PEAKTIME+number.toString(),"");
	}

	/**
	 * Sets the peak time
	 * @param peakTime
	 * @return the peaktime as it is reported
	 * @throws DeviceException
	 */
	@Override
	public double setPeakTime(double peakTime) throws DeviceException {
		xmap.setValue("SET"+PEAKTIME+number.toString(),"",peakTime);
		return getPeakTime();
	}

	/**
	 * @return the dynamic range
	 * @throws DeviceException
	 */
	@Override
	public double getDynamicRange() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GET"+DYNRANGE+number.toString(),"");
	}

	/**
	 * sets the dynamic range
	 * @param dynamicRange
	 * @return the dynamic range as it is reported
	 * @throws DeviceException
	 */
	@Override
	public double setDynamicRange(double dynamicRange) throws DeviceException {
		xmap.setValue("SET"+DYNRANGE+number.toString(),"",dynamicRange);
		return getDynamicRange();
	}

	/**
	 * @return The trigger threshold
	 * @throws DeviceException
	 */
	@Override
	public double getTriggerThreshold() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GET"+TRIGTHRESH+number.toString(),"");
	}

	/**
	 * sets the trigger threshold
	 * @param triggerThreshold
	 * @return the trigger threshold as it is reported
	 * @throws DeviceException
	 */
	@Override
	public double setTriggerThreshold(double triggerThreshold) throws DeviceException {
		xmap.setValue("SET"+TRIGTHRESH+number.toString(),"",triggerThreshold);
		return getTriggerThreshold();
	}

	/**
	 * @return the base threshold
	 * @throws DeviceException
	 */
	@Override
	public double getBaseThreshold() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GET"+BASETHRESH+number.toString(),"");
	}

	/**
	 * sets the base threshold
	 * @param baseThreshold
	 * @return the base threshold as reported
	 * @throws DeviceException
	 */
	@Override
	public double setBaseThreshold(double baseThreshold) throws DeviceException {
		xmap.setValue("SET"+BASETHRESH+number.toString(),"",baseThreshold);
		return getBaseThreshold();
	}

	/**
	 * @return The base length
	 * @throws DeviceException
	 */
	@Override
	public int getBaseLength() throws DeviceException {
		// using set base length here, as it returns the position of the dropdown menu, which is what needs to be set.
		return (Short) xmap.getValue(ReturnType.DBR_NATIVE,"SET"+BASELENGTH+number.toString(),"");
	}

	/**
	 * sets the Base Length
	 * @param baseLength
	 * @return the base length as it is reported
	 * @throws DeviceException
	 */
	@Override
	public int setBaseLength(int baseLength) throws DeviceException {
		xmap.setValue("SET"+BASELENGTH+number.toString(),"",baseLength);
		return getBaseLength();
	}

	/**
	 * @return the energy threshold
	 * @throws DeviceException
	 */
	@Override
	public double getEnergyThreshold() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GET"+ENERGYTHRESH+number.toString(),"");
	}

	/**
	 * sets the energy threshold
	 * @param energyThreshold
	 * @return the energy threshold
	 * @throws DeviceException
	 */
	@Override
	public double setEnergyThreshold(double energyThreshold) throws DeviceException {
		xmap.setValue("SET"+ENERGYTHRESH+number.toString(),"",energyThreshold);
		return getEnergyThreshold();
	}

	/**
	 * @return the bin width
	 * @throws DeviceException
	 */
	@Override
	public double getBinWidth() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GET"+BINWIDTH+number.toString(),"");
	}

	/**
	 * sets the bin width
	 * @param binWidth
	 * @return the bin width as reported
	 * @throws DeviceException
	 */
	@Override
	public double setBinWidth(double binWidth) throws DeviceException {
		xmap.setValue("SET"+BINWIDTH+number.toString(),"",binWidth);
		return getBinWidth();
	}

	/**
	 * @return the preamp gain
	 * @throws DeviceException
	 */
	@Override
	public double getPreampGain() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GET"+PREAMPGAIN+number.toString(),"");
	}

	/**
	 * sets the preamp gain
	 * @param preampGain
	 * @return the preamp gain as reported
	 * @throws DeviceException
	 */
	@Override
	public double setPreampGain(double preampGain) throws DeviceException {
		xmap.setValue("SET"+PREAMPGAIN+number.toString(),"",preampGain);
		return getPreampGain();
	}

	/**
	 * @return the reset delay
	 * @throws DeviceException
	 */
	@Override
	public double getResetDelay() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GET"+RESETDELAY+number.toString(),"");
	}

	/**
	 * Sets the reset delay
	 * @param resetDelay
	 * @return the reset delay as reported
	 * @throws DeviceException
	 */
	@Override
	public double setResetDelay(double resetDelay) throws DeviceException {
		xmap.setValue("SET"+RESETDELAY+number.toString(),"",resetDelay);
		return getResetDelay();
	}

	/**
	 * @return The gap time
	 * @throws DeviceException
	 */
	@Override
	public double getGapTime() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GET"+GAPTIME+number.toString(),"");
	}

	/**
	 * Sets the gap time
	 * @param gapTime
	 * @return the gap time as reported
	 * @throws DeviceException
	 */
	@Override
	public double setGapTime(double gapTime) throws DeviceException {
		xmap.setValue("SET"+GAPTIME+number.toString(),"",gapTime);
		return getGapTime();
	}

	/**
	 * @return the trigger peak time
	 * @throws DeviceException
	 */
	@Override
	public double getTriggerPeakTime() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GET"+TRIGPEAKTIME+number.toString(),"");
	}

	/**
	 * sets teh trigger peak time
	 * @param triggerPeakTime
	 * @return the trigger peak time as reported
	 * @throws DeviceException
	 */
	@Override
	public double setTriggerPeakTime(double triggerPeakTime) throws DeviceException {
		xmap.setValue("SET"+TRIGPEAKTIME+number.toString(),"",triggerPeakTime);
		return getTriggerPeakTime();
	}

	/**
	 * @return the trigger gap time
	 * @throws DeviceException
	 */
	@Override
	public double getTriggerGapTime() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GET"+TRIGGAPTIME+number.toString(),"");
	}

	/**
	 * sets the trigger gap time
	 * @param triggerGapTime
	 * @return the reported trigger gap time
	 * @throws DeviceException
	 */
	@Override
	public double setTriggerGapTime(double triggerGapTime) throws DeviceException {
		xmap.setValue("SET"+TRIGGAPTIME+number.toString(),"",triggerGapTime);
		return getTriggerGapTime();
	}

	/**
	 * @return the max width
	 * @throws DeviceException
	 */
	@Override
	public double getMaxWidth() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GET"+MAXWIDTH+number.toString(),"");
	}

	/**
	 * sets the max width
	 * @param maxWidth
	 * @return the reported max width
	 * @throws DeviceException
	 */
	@Override
	public double setMaxWidth(double maxWidth) throws DeviceException {
		xmap.setValue("SET"+MAXWIDTH+number.toString(),"",maxWidth);
		return getMaxWidth();
	}

	@Override
	public String getName() {
		return "EDXD_Element_"+String.format("%02d", number);
	}

	@Override
	public NexusGroupData readout() throws DeviceException {
		final double[] data = (double[]) xmap.getValue(ReturnType.DBR_NATIVE, DATA + number.toString(), "");
		final double[] result = new double[getDataDimensions()[0]];
		for(int i = 0; i < result.length; i++) {
			result[i] = data[i];
		}
		return new NexusGroupData(getDataDimensions(), result);
	}

	/**
	 * @return the double array of data from the xmap
	 * @throws DeviceException
	 */
	@Override
	public double[] readoutDoubles() throws DeviceException {
		final Object obj = xmap.getValue(ReturnType.DBR_NATIVE, DATA + number.toString(), "");
		if(obj instanceof double[])
			return (double[])obj ;
		else if( obj instanceof int[]){
			final int[] returnObj = (int[]) obj;
			final double[] toreturnObj = new double[returnObj.length];
			for(int i =0 ; i< returnObj.length; i++)
				toreturnObj[i] = returnObj[i];
			return toreturnObj;
		}
		return null;
	}


	/**
	 * @return the double array of data from the xmap
	 * @throws DeviceException
	 */
	@Override
	public int[] readoutInts() throws DeviceException {
		final Object obj = xmap.getValue(ReturnType.DBR_NATIVE, DATA + number.toString(), "");
		if(obj instanceof int[])
			return (int[])obj ;
		else if( obj instanceof double[]){
			final double[] returnObj = (double[]) obj;
			final int[] toreturnObj = new int[returnObj.length];
			for(int i =0 ; i< returnObj.length; i++)
				toreturnObj[i] = (int)returnObj[i];
			return toreturnObj;
		}
		return null;
	}


	private double[] createEnergyMapping() throws DeviceException {
		final double[] energyMapping = new double[getDataDimensions()[0]];
		for (int i = 0; i < energyMapping.length; i++) {
			energyMapping[i] = createEnergyValue(i);
		}
		return energyMapping;
	}

	/**
	 *
	 * @return the energy mapping
	 * @throws DeviceException
	 */
	@Override
	public double[] getEnergyMapping() throws DeviceException {
		if (energy == null)
			energy = createEnergyMapping();
		return energy;
	}

	/**
	 * get an energy for a single bin
	 * @param value
	 * @return the energy in eV
	 */
	protected double createEnergyValue(double value) {
		return (a*value*value)+(b*value)+(c);
	}

	private double[] createQMapping() throws DeviceException {
		final double pre = Math.sin(theta) * ((4.0 * Math.PI) / 12.39);
		final double[] q = new double[getDataDimensions()[0]];
		final double[] energyMapping = getEnergyMapping();
		for(int i = 0; i < q.length; i++)
			q[i] = pre*energyMapping[i];
		return q;
	}

	/**
	 * @return the q mapping for the detector
	 * @throws DeviceException
	 */
	@Override
	public double[] getQMapping() throws DeviceException {
		if (q == null)
			q = createQMapping();
		return q;
	}

	/**
	 * The point of this function is to fit a curve to the data collected from a calibration Sample
	 * @param actual
	 * @param reported
	 * @throws Exception
	 */
	@Override
	public void fitPolynomialToEnergyData(double[] actual, double[] reported) throws Exception {
		final Dataset act = DatasetFactory.createFromObject(actual);
		final Dataset rep = DatasetFactory.createFromObject(reported);

		final double[] initial = { 0.0, 1.0, 0.0 };
		final CompositeFunction out = Fitter.fit(rep, act, new LeastSquares(0.0), new Quadratic(initial));
		a = out.getFunction(0).getParameter(0).getValue();
		b = out.getFunction(0).getParameter(1).getValue();
		c = out.getFunction(0).getParameter(2).getValue();
		energy = null;
		q = null;
	}

	@Override
	public void collectData() throws DeviceException {
	}

	@Override
	public String getDescription() throws DeviceException {
		return null;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return null;
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	@Override
	public void writeNeXusInformation(NexusFile file, Node node)
			throws NexusException {
	}

	/**
	 * @return the configuration of this element in a bean
	 * @throws DeviceException
	 */
	@Override
	public EDXDElementBean saveConfiguration() throws DeviceException {
		final EDXDElementBean bean = new EDXDElementBean();
		bean.setBaseLength(getBaseLength());
		bean.setBaseThreshold(getBaseThreshold());
		bean.setBinWidth(getBinWidth());
		bean.setDynamicRange(getDynamicRange());
		bean.setEnergyThreshold(getEnergyThreshold());
		bean.setGapTime(getGapTime());
		bean.setMaxWidth(getMaxWidth());
		bean.setPeakTime(getPeakTime());
		bean.setPreampGain(getPreampGain());
		bean.setResetDelay(getResetDelay());
		bean.setTriggerGapTime(getTriggerGapTime());
		bean.setTriggerPeakTime(getTriggerPeakTime());
		bean.setTriggerThreshold(getTriggerThreshold());
		return bean;
	}

	/**
	 * Sets all the xmap values from the values in the provided bean
	 * @param bean
	 * @throws DeviceException
	 */
	@Override
	public void loadConfiguration(EDXDElementBean bean) throws DeviceException {
		setBaseLength(bean.getBaseLength());
		setBaseThreshold(bean.getBaseThreshold());
		setBinWidth(bean.getBinWidth());
		setDynamicRange(bean.getDynamicRange());
		setEnergyThreshold(bean.getEnergyThreshold());
		setGapTime(bean.getGapTime());
		setMaxWidth(bean.getMaxWidth());
		setPeakTime(bean.getPeakTime());
		setPreampGain(bean.getPreampGain());
		setResetDelay(bean.getResetDelay());
		setTriggerGapTime(bean.getTriggerGapTime());
		setTriggerPeakTime(bean.getTriggerPeakTime());
		setTriggerThreshold(bean.getTriggerThreshold());
	}

	/**
	 * Set rois the array must be of size [maximum number rois][2]
	 * @param rois
	 * @throws DeviceException
	 */
	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		final double [] roiLow  = getLowROIs();
		mergeRois(roiLow, rois, 0);
		setLowROIs(roiLow);
		final double [] roiHigh = getHighROIs();
		mergeRois(roiHigh, rois, 1);
		setHighROIs(roiHigh);
		final double[] curLow = getLowROIs();
		if (!Arrays.equals(roiLow, curLow)) throw new DeviceException("Did not set low rois!");
		final double[] curHi = getHighROIs();
		if (!Arrays.equals(curHi, roiHigh)) throw new DeviceException("Did not set high rois!");
	}

	protected void mergeRois(double[] curRois, double[][] rois, int i) {
		for (int j = 0; j < curRois.length; j++)
			curRois[j] = rois[j][i];
	}

	/**
	 * Sets the ROI low limit
	 * @param roiLow
	 * @throws DeviceException
	 */
	@Override
	public void setLowROIs(double[] roiLow) throws DeviceException{
		xmap.setValue((SCALOWLIMITS+number.toString()), "", roiLow);
	}

	/**
	 * get the roi low limit
	 * @return roi low limit array
	 * @throws DeviceException
	 */
	@Override
	public double[] getLowROIs() throws DeviceException{
		return (double[])xmap.getValue(ReturnType.DBR_NATIVE, (SCALOWLIMITS+number.toString()), "");
	}

	/**
	 * Set the roi high limit
	 * @param roiHigh
	 * @throws DeviceException
	 */
	@Override
	public void setHighROIs(double[] roiHigh) throws DeviceException{
		xmap.setValue((SCAHIGHLIMITS+number.toString()), "", roiHigh);
	}

	/**
	 * get the roi High limit
	 * @return high limit array
	 * @throws DeviceException
	 */
	@Override
	public double[] getHighROIs() throws DeviceException{
		return (double[])xmap.getValue(ReturnType.DBR_NATIVE, (SCAHIGHLIMITS+number.toString()), "");
	}

	/**
	 * get the counts for all rois set
	 * @return counts
	 * @throws DeviceException
	 */
	@Override
	public double[] getROICounts() throws DeviceException {
		return (double[])xmap.getValue(ReturnType.DBR_NATIVE, SCADATA+number.toString(), "");
	}

}