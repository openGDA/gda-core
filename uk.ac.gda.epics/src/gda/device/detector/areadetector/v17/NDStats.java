/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17;

import gda.observable.Observable;


/**
 * This maps to the stats plugin on the edm screen.
 */
public interface NDStats {

	/**
	 * List all the PVs
	 */

	String ComputeStatistics = "ComputeStatistics";

	String ComputeStatistics_RBV = "ComputeStatistics_RBV";

	String BgdWidth = "BgdWidth";

	String BgdWidth_RBV = "BgdWidth_RBV";

	String MinValue_RBV = "MinValue_RBV";

	String MaxValue_RBV = "MaxValue_RBV";

	String MeanValue_RBV = "MeanValue_RBV";

	String Sigma_RBV = "Sigma_RBV";

	String Total_RBV = "Total_RBV";

	String Net_RBV = "Net_RBV";

	String ComputeCentroid = "ComputeCentroid";

	String ComputeCentroid_RBV = "ComputeCentroid_RBV";

	String CentroidThreshold = "CentroidThreshold";

	String CentroidThreshold_RBV = "CentroidThreshold_RBV";

	String CentroidX_RBV = "CentroidX_RBV";

	String CentroidY_RBV = "CentroidY_RBV";

	String SigmaX_RBV = "SigmaX_RBV";

	String SigmaY_RBV = "SigmaY_RBV";

	String SigmaXY_RBV = "SigmaXY_RBV";

	String ComputeProfiles = "ComputeProfiles";

	String ComputeProfiles_RBV = "ComputeProfiles_RBV";

	String ProfileSizeX_RBV = "ProfileSizeX_RBV";

	String ProfileSizeY_RBV = "ProfileSizeY_RBV";

	String CursorX = "CursorX";

	String CursorX_RBV = "CursorX_RBV";

	String CursorY = "CursorY";

	String CursorY_RBV = "CursorY_RBV";

	String ProfileAverageX_RBV = "ProfileAverageX_RBV";

	String ProfileAverageY_RBV = "ProfileAverageY_RBV";

	String ProfileThresholdX_RBV = "ProfileThresholdX_RBV";

	String ProfileThresholdY_RBV = "ProfileThresholdY_RBV";

	String ProfileCentroidX_RBV = "ProfileCentroidX_RBV";

	String ProfileCentroidY_RBV = "ProfileCentroidY_RBV";

	String ProfileCursorX_RBV = "ProfileCursorX_RBV";

	String ProfileCursorY_RBV = "ProfileCursorY_RBV";

	String ComputeHistogram = "ComputeHistogram";

	String ComputeHistogram_RBV = "ComputeHistogram_RBV";

	String HistSize = "HistSize";

	String HistSize_RBV = "HistSize_RBV";

	String HistMin = "HistMin";

	String HistMin_RBV = "HistMin_RBV";

	String HistMax = "HistMax";

	String HistMax_RBV = "HistMax_RBV";

	String HistEntropy_RBV = "HistEntropy_RBV";

	String Histogram_RBV = "Histogram_RBV";

	String MaxSizeX = "MaxSizeX";

	String SetXHOPR = "SetXHOPR";

	String MaxSizeY = "MaxSizeY";

	String SetYHOPR = "SetYHOPR";

	NDPluginBase getPluginBase();

	/**
	 *
	 */
	short getComputeStatistics() throws Exception;

	/**
	 *
	 */
	void setComputeStatistics(int computestatistics) throws Exception;

	/**
	 *
	 */
	short getComputeStatistics_RBV() throws Exception;

	/**
	 *
	 */
	int getBgdWidth() throws Exception;

	/**
	 *
	 */
	void setBgdWidth(int bgdwidth) throws Exception;

	/**
	 *
	 */
	int getBgdWidth_RBV() throws Exception;

	/**
	 *
	 */
	double getMinValue_RBV() throws Exception;

	/**
	 *
	 */
	double getMaxValue_RBV() throws Exception;

	/**
	 *
	 */
	double getMeanValue_RBV() throws Exception;

	/**
	 *
	 */
	double getSigma_RBV() throws Exception;

	/**
	 *
	 */
	double getTotal_RBV() throws Exception;

	/**
	 *
	 */
	double getNet_RBV() throws Exception;

	/**
	 *
	 */
	short getComputeCentroid() throws Exception;

	/**
	 *
	 */
	void setComputeCentroid(int computecentroid) throws Exception;

	/**
	 *
	 */
	short getComputeCentroid_RBV() throws Exception;

	/**
	 *
	 */
	double getCentroidThreshold() throws Exception;

	/**
	 *
	 */
	void setCentroidThreshold(double centroidthreshold) throws Exception;

	/**
	 *
	 */
	double getCentroidThreshold_RBV() throws Exception;

	/**
	 *
	 */
	double getCentroidX_RBV() throws Exception;

	/**
	 *
	 */
	double getCentroidY_RBV() throws Exception;

	/**
	 *
	 */
	double getSigmaX_RBV() throws Exception;

	/**
	 *
	 */
	double getSigmaY_RBV() throws Exception;

	/**
	 *
	 */
	double getSigmaXY_RBV() throws Exception;

	/**
	 *
	 */
	short getComputeProfiles() throws Exception;

	/**
	 *
	 */
	void setComputeProfiles(int computeprofiles) throws Exception;

	/**
	 *
	 */
	short getComputeProfiles_RBV() throws Exception;

	/**
	 *
	 */
	int getProfileSizeX_RBV() throws Exception;

	/**
	 *
	 */
	int getProfileSizeY_RBV() throws Exception;

	/**
	 *
	 */
	int getCursorX() throws Exception;

	/**
	 *
	 */
	void setCursorX(int cursorx) throws Exception;

	/**
	 *
	 */
	int getCursorX_RBV() throws Exception;

	/**
	 *
	 */
	int getCursorY() throws Exception;

	/**
	 *
	 */
	void setCursorY(int cursory) throws Exception;

	/**
	 *
	 */
	int getCursorY_RBV() throws Exception;

	/**
	 *
	 */
	double[] getProfileAverageX_RBV() throws Exception;

	/**
	 *
	 */
	double[] getProfileAverageY_RBV() throws Exception;

	/**
	 *
	 */
	double[] getProfileThresholdX_RBV() throws Exception;

	/**
	 *
	 */
	double[] getProfileThresholdY_RBV() throws Exception;

	/**
	 *
	 */
	double[] getProfileCentroidX_RBV() throws Exception;

	/**
	 *
	 */
	double[] getProfileCentroidY_RBV() throws Exception;

	/**
	 *
	 */
	double[] getProfileCursorX_RBV() throws Exception;

	/**
	 *
	 */
	double[] getProfileCursorY_RBV() throws Exception;

	/**
	 *
	 */
	short getComputeHistogram() throws Exception;

	/**
	 *
	 */
	void setComputeHistogram(int computehistogram) throws Exception;

	/**
	 *
	 */
	short getComputeHistogram_RBV() throws Exception;

	/**
	 *
	 */
	int getHistSize() throws Exception;

	/**
	 *
	 */
	void setHistSize(int histsize) throws Exception;

	/**
	 *
	 */
	int getHistSize_RBV() throws Exception;

	/**
	 *
	 */
	double getHistMin() throws Exception;

	/**
	 *
	 */
	void setHistMin(double histmin) throws Exception;

	/**
	 *
	 */
	double getHistMin_RBV() throws Exception;

	/**
	 *
	 */
	double getHistMax() throws Exception;

	/**
	 *
	 */
	void setHistMax(double histmax) throws Exception;

	/**
	 *
	 */
	double getHistMax_RBV() throws Exception;

	/**
	 *
	 */
	double getHistEntropy_RBV() throws Exception;

	/**
	 *
	 */
	double[] getHistogram_RBV() throws Exception;
	
	/**
	 * 
	 */
	double[] getHistogram_RBV(int numberOfElements) throws Exception;

	/**
	 *
	 */
	int getMaxSizeX() throws Exception;

	/**
	 *
	 */
	void setMaxSizeX(int maxsizex) throws Exception;

	/**
	 *
	 */
	double getSetXHOPR() throws Exception;

	/**
	 *
	 */
	void setSetXHOPR(double setxhopr) throws Exception;

	/**
	 *
	 */
	int getMaxSizeY() throws Exception;

	/**
	 *
	 */
	void setMaxSizeY(int maxsizey) throws Exception;

	/**
	 *
	 */
	double getSetYHOPR() throws Exception;

	/**
	 *
	 */
	void setSetYHOPR(double setyhopr) throws Exception;

	/**
	 * @throws Exception
	 */
	void reset() throws Exception;

	Observable<String> createComputeHistogramObservable() throws Exception;

	Observable<String> createComputeStatisticsObservable() throws Exception;

	Observable<Double> createMinObservable() throws Exception;

	Observable<Double> createMaxObservable() throws Exception;
	Observable<Double> createMeanObservable() throws Exception;
	Observable<Double> createTotalObservable() throws Exception;
	Observable<Double> createSigmaObservable() throws Exception;
}