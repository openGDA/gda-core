/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import gda.data.nexus.INeXusInfoWriteable;
import gda.device.Detector;
import gda.device.DeviceException;

public interface IEDXDElement extends Detector, INeXusInfoWriteable {

	/**
	 * @return the energy live time
	 * @throws DeviceException
	 */
	double getEnergyLiveTime() throws DeviceException;

	/**
	 * the trigger live time
	 * @return the reported trigger live time
	 * @throws DeviceException
	 */
	double getTriggerLiveTime() throws DeviceException;

	/**
	 * @return the real time
	 * @throws DeviceException
	 */
	double getRealTime() throws DeviceException;

	/**
	 * @return the number of events
	 * @throws DeviceException
	 */
	int getEvents() throws DeviceException;

	/**
	 * @return the input count rate
	 * @throws DeviceException
	 */
	double getInputCountRate() throws DeviceException;

	/**
	 * @return The output count rate
	 * @throws DeviceException
	 */
	double getOutputCountRate() throws DeviceException;

	/**
	 * @return a double array containing the energy value per bin on the outputed data
	 * @throws DeviceException
	 */
	double[] getEnergyBins() throws DeviceException;

	/**
	 * @return the peak time
	 * @throws DeviceException
	 */
	double getPeakTime() throws DeviceException;

	/**
	 * Sets the peak time
	 * @param peakTime
	 * @return the peaktime as it is reported
	 * @throws DeviceException
	 */
	double setPeakTime(double peakTime) throws DeviceException;

	/**
	 * @return the dynamic range
	 * @throws DeviceException
	 */
	double getDynamicRange() throws DeviceException;

	/**
	 * sets the dynamic range
	 * @param dynamicRange
	 * @return the dynamic range as it is reported
	 * @throws DeviceException
	 */
	double setDynamicRange(double dynamicRange) throws DeviceException;

	/**
	 * @return The trigger threshold
	 * @throws DeviceException
	 */
	double getTriggerThreshold() throws DeviceException;

	/**
	 * sets the trigger threshold
	 * @param triggerThreshold
	 * @return the trigger threshold as it is reported
	 * @throws DeviceException
	 */
	double setTriggerThreshold(double triggerThreshold) throws DeviceException;

	/**
	 * @return the base threshold
	 * @throws DeviceException
	 */
	double getBaseThreshold() throws DeviceException;

	/**
	 * sets the base threshold
	 * @param baseThreshold
	 * @return the base threshold as reported
	 * @throws DeviceException
	 */
	double setBaseThreshold(double baseThreshold) throws DeviceException;

	/**
	 * @return The base length
	 * @throws DeviceException
	 */
	int getBaseLength() throws DeviceException;

	/**
	 * sets the Base Length
	 * @param baseLength
	 * @return the base length as it is reported
	 * @throws DeviceException
	 */
	int setBaseLength(int baseLength) throws DeviceException;

	/**
	 * @return the energy threshold
	 * @throws DeviceException
	 */
	double getEnergyThreshold() throws DeviceException;

	/**
	 * sets the energy threshold
	 * @param energyThreshold
	 * @return the energy threshold
	 * @throws DeviceException
	 */
	double setEnergyThreshold(double energyThreshold) throws DeviceException;

	/**
	 * @return the bin width
	 * @throws DeviceException
	 */
	double getBinWidth() throws DeviceException;

	/**
	 * sets the bin width
	 * @param binWidth
	 * @return the bin width as reported
	 * @throws DeviceException
	 */
	double setBinWidth(double binWidth) throws DeviceException;

	/**
	 * @return the preamp gain
	 * @throws DeviceException
	 */
	double getPreampGain() throws DeviceException;

	/**
	 * sets the preamp gain
	 * @param preampGain
	 * @return the preamp gain as reported
	 * @throws DeviceException
	 */
	double setPreampGain(double preampGain) throws DeviceException;

	/**
	 * @return the reset delay
	 * @throws DeviceException
	 */
	double getResetDelay() throws DeviceException;

	/**
	 * Sets the reset delay
	 * @param resetDelay
	 * @return the reset delay as reported
	 * @throws DeviceException
	 */
	double setResetDelay(double resetDelay) throws DeviceException;

	/**
	 * @return The gap time
	 * @throws DeviceException
	 */
	double getGapTime() throws DeviceException;

	/**
	 * Sets the gap time
	 * @param gapTime
	 * @return the gap time as reported
	 * @throws DeviceException
	 */
	double setGapTime(double gapTime) throws DeviceException;

	/**
	 * @return the trigger peak time
	 * @throws DeviceException
	 */
	double getTriggerPeakTime() throws DeviceException;

	/**
	 * sets teh trigger peak time
	 * @param triggerPeakTime
	 * @return the trigger peak time as reported
	 * @throws DeviceException
	 */
	double setTriggerPeakTime(double triggerPeakTime) throws DeviceException;

	/**
	 * @return the trigger gap time
	 * @throws DeviceException
	 */
	double getTriggerGapTime() throws DeviceException;

	/**
	 * sets the trigger gap time
	 * @param triggerGapTime
	 * @return the reported trigger gap time
	 * @throws DeviceException
	 */
	double setTriggerGapTime(double triggerGapTime) throws DeviceException;

	/**
	 * @return the max width
	 * @throws DeviceException
	 */
	double getMaxWidth() throws DeviceException;

	/**
	 * sets the max width
	 * @param maxWidth
	 * @return the reported max width
	 * @throws DeviceException
	 */
	double setMaxWidth(double maxWidth) throws DeviceException;

	/**
	 * @return the double array of data from the xmap
	 * @throws DeviceException
	 */
	double[] readoutDoubles() throws DeviceException;

	/**
	 * @return the double array of data from the xmap
	 * @throws DeviceException
	 */
	int[] readoutInts() throws DeviceException;

	/**
	 *
	 * @return the energy mapping
	 * @throws DeviceException
	 */
	double[] getEnergyMapping() throws DeviceException;

	/**
	 * @return the q mapping for the detector
	 * @throws DeviceException
	 */
	double[] getQMapping() throws DeviceException;

	/**
	 * The point of this function is to fit a curve to the data collected from a calibration Sample
	 * @param actual
	 * @param reported
	 * @throws Exception
	 */
	void fitPolynomialToEnergyData(double[] actual, double[] reported) throws Exception;

	/**
	 * @return the configuration of this element in a bean
	 * @throws DeviceException
	 */
	EDXDElementBean saveConfiguration() throws DeviceException;

	/**
	 * Sets all the xmap values from the values in the provided bean
	 * @param bean
	 * @throws DeviceException
	 */
	void loadConfiguration(EDXDElementBean bean) throws DeviceException;

	/**
	 * Set rois the array must be of size [maximum number rois][2]
	 * @param rois
	 * @throws DeviceException
	 */
	void setROIs(double[][] rois) throws DeviceException;

	/**
	 * Sets the ROI low limit
	 * @param roiLow
	 * @throws DeviceException
	 */
	void setLowROIs(double[] roiLow) throws DeviceException;

	/**
	 * get the roi low limit
	 * @return roi low limit array
	 * @throws DeviceException
	 */
	double[] getLowROIs() throws DeviceException;

	/**
	 * Set the roi high limit
	 * @param roiHigh
	 * @throws DeviceException
	 */
	void setHighROIs(double[] roiHigh) throws DeviceException;

	/**
	 * get the roi High limit
	 * @return high limit array
	 * @throws DeviceException
	 */
	double[] getHighROIs() throws DeviceException;

	/**
	 * get the counts for all rois set
	 * @return counts
	 * @throws DeviceException
	 */
	double[] getROICounts() throws DeviceException;

}