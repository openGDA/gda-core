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

package gda.device.detector.areadetector.v17.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.areadetector.v17.NDStats;
import gda.epics.LazyPVFactory;
import gda.epics.connection.EpicsController;
import gda.observable.Observable;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

public class NDStatsImpl extends NDBaseImpl implements InitializingBean, NDStats {

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String basePVName;

	static final Logger logger = LoggerFactory.getLogger(NDStatsImpl.class);

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	/**
	*
	*/
	@Override
	public short getComputeStatistics() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeStatistics));
		} catch (Exception ex) {
			logger.warn("Cannot getComputeStatistics", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setComputeStatistics(int computestatistics) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ComputeStatistics), computestatistics);
		} catch (Exception ex) {
			logger.warn("Cannot setComputeStatistics", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeStatistics_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeStatistics_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getComputeStatistics_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBgdWidth() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(BgdWidth));
		} catch (Exception ex) {
			logger.warn("Cannot getBgdWidth", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setBgdWidth(int bgdwidth) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(BgdWidth), bgdwidth);
		} catch (Exception ex) {
			logger.warn("Cannot setBgdWidth", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBgdWidth_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(BgdWidth_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getBgdWidth_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getMinValue_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(MinValue_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getMinValue_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getMaxValue_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(MaxValue_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getMaxValue_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getMeanValue_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(MeanValue_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getMeanValue_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getSigma_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(Sigma_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSigma_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getTotal_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(Total_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getTotal_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getNet_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(Net_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNet_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeCentroid() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeCentroid));
		} catch (Exception ex) {
			logger.warn("Cannot getComputeCentroid", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setComputeCentroid(int computecentroid) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ComputeCentroid), computecentroid);
		} catch (Exception ex) {
			logger.warn("Cannot setComputeCentroid", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeCentroid_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeCentroid_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getComputeCentroid_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getCentroidThreshold() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(CentroidThreshold));
		} catch (Exception ex) {
			logger.warn("Cannot getCentroidThreshold", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setCentroidThreshold(double centroidthreshold) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(CentroidThreshold), centroidthreshold);
		} catch (Exception ex) {
			logger.warn("Cannot setCentroidThreshold", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getCentroidThreshold_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(CentroidThreshold_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getCentroidThreshold_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getCentroidX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(CentroidX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getCentroidX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getCentroidY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(CentroidY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getCentroidY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getSigmaX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(SigmaX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSigmaX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getSigmaY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(SigmaY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSigmaY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getSigmaXY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(SigmaXY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSigmaXY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeProfiles() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeProfiles));
		} catch (Exception ex) {
			logger.warn("Cannot getComputeProfiles", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setComputeProfiles(int computeprofiles) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ComputeProfiles), computeprofiles);
		} catch (Exception ex) {
			logger.warn("Cannot setComputeProfiles", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeProfiles_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeProfiles_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getComputeProfiles_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getProfileSizeX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ProfileSizeX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getProfileSizeX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getProfileSizeY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ProfileSizeY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getProfileSizeY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getCursorX() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(CursorX));
		} catch (Exception ex) {
			logger.warn("Cannot getCursorX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setCursorX(int cursorx) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(CursorX), cursorx);
		} catch (Exception ex) {
			logger.warn("Cannot setCursorX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getCursorX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(CursorX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getCursorX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getCursorY() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(CursorY));
		} catch (Exception ex) {
			logger.warn("Cannot getCursorY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setCursorY(int cursory) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(CursorY), cursory);
		} catch (Exception ex) {
			logger.warn("Cannot setCursorY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getCursorY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(CursorY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getCursorY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileAverageX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileAverageX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getProfileAverageX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileAverageY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileAverageY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getProfileAverageY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileThresholdX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileThresholdX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getProfileThresholdX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileThresholdY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileThresholdY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getProfileThresholdY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileCentroidX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileCentroidX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getProfileCentroidX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileCentroidY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileCentroidY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getProfileCentroidY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileCursorX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileCursorX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getProfileCursorX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileCursorY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileCursorY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getProfileCursorY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeHistogram() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeHistogram));
		} catch (Exception ex) {
			logger.warn("Cannot getComputeHistogram", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setComputeHistogram(int computehistogram) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ComputeHistogram), computehistogram);
			logger.debug("Set Compute Histogram to {} on {}", computehistogram, ComputeHistogram);
		} catch (Exception ex) {
			logger.warn("Cannot setComputeHistogram", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeHistogram_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeHistogram_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getComputeHistogram_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getHistSize() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(HistSize));
		} catch (Exception ex) {
			logger.warn("Cannot getHistSize", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setHistSize(int histsize) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(HistSize), histsize);
			logger.debug("Set History Size to {} on {}", histsize, HistSize);
		} catch (Exception ex) {
			logger.warn("Cannot setHistSize", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getHistSize_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(HistSize_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getHistSize_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHistMin() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(HistMin));
		} catch (Exception ex) {
			logger.warn("Cannot getHistMin", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setHistMin(double histmin) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(HistMin), histmin);
			logger.debug("Set History Min to {} on {}", histmin, HistMin);
		} catch (Exception ex) {
			logger.warn("Cannot setHistMin", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHistMin_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(HistMin_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getHistMin_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHistMax() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(HistMax));
		} catch (Exception ex) {
			logger.warn("Cannot getHistMax", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setHistMax(double histmax) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(HistMax), histmax);
			logger.debug("Set History Max to {} on {}", histmax, HistMax);
		} catch (Exception ex) {
			logger.warn("Cannot setHistMax", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHistMax_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(HistMax_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getHistMax_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHistEntropy_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(HistEntropy_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getHistEntropy_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getHistogram_RBV() throws Exception {
		Channel channel = getChannel(Histogram_RBV);
		return EPICS_CONTROLLER.cagetDoubleArray(channel);
	}

	@Override
	public double[] getHistogram_RBV(int numberOfElements) throws Exception {
		Channel channel = getChannel(Histogram_RBV);
		return EPICS_CONTROLLER.cagetDoubleArray(channel, numberOfElements);
	}

	/**
	*
	*/
	@Override
	public int getMaxSizeX() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(MaxSizeX));
		} catch (Exception ex) {
			logger.warn("Cannot getMaxSizeX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setMaxSizeX(int maxsizex) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(MaxSizeX), maxsizex);
			logger.debug("Set History Max Size X to {} on {}", maxsizex, MaxSizeX);
		} catch (Exception ex) {
			logger.warn("Cannot setMaxSizeX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getSetXHOPR() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(SetXHOPR));
		} catch (Exception ex) {
			logger.warn("Cannot getSetXHOPR", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSetXHOPR(double setxhopr) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(SetXHOPR), setxhopr);
		} catch (Exception ex) {
			logger.warn("Cannot setSetXHOPR", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getMaxSizeY() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(MaxSizeY));
		} catch (Exception ex) {
			logger.warn("Cannot getMaxSizeY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setMaxSizeY(int maxsizey) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(MaxSizeY), maxsizey);
			logger.debug("Set History Max Y Size to {} on {}", maxsizey, MaxSizeY);
		} catch (Exception ex) {
			logger.warn("Cannot setMaxSizeY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getSetYHOPR() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(SetYHOPR));
		} catch (Exception ex) {
			logger.warn("Cannot getSetYHOPR", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSetYHOPR(double setyhopr) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(SetYHOPR), setyhopr);
		} catch (Exception ex) {
			logger.warn("Cannot setSetYHOPR", ex);
			throw ex;
		}
	}

	/**
	 * @return Returns the basePVName.
	 */
	public String getBasePVName() {
		return basePVName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
	}

	/**
	 * @param basePVName
	 *            The basePVName to set.
	 */
	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	private String getChannelName(String pvElementName, String... args) {
		String pvPostFix = null;
		if (args.length > 0) {
			// PV element name is different from the pvPostFix
			pvPostFix = args[0];
		} else {
			pvPostFix = pvElementName;
		}

		return basePVName + pvPostFix;
	}
	/**
	 * This method allows to toggle between the method in which the PV is acquired.
	 *
	 * @param pvElementName
	 * @param args
	 * @return {@link Channel} to talk to the relevant PV.
	 * @throws Exception
	 */
	private Channel getChannel(String pvElementName, String... args) throws Exception {
		return createChannel(getChannelName(pvElementName, args));
	}

	public Channel createChannel(String fullPvName) throws CAException, TimeoutException {
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			channel = EPICS_CONTROLLER.createChannel(fullPvName);
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	@Override
	public void reset() throws Exception {
		getPluginBase().reset();
	}


	@Override
	public Observable<String> createComputeHistogramObservable() throws Exception {
		return LazyPVFactory.newReadOnlyEnumPV(getChannelName(ComputeHistogram_RBV), String.class);
	}

	@Override
	public Observable<String> createComputeStatisticsObservable() throws Exception {
		return LazyPVFactory.newReadOnlyEnumPV(getChannelName(ComputeStatistics_RBV), String.class);
	}

	@Override
	public Observable<Double> createMinObservable() throws Exception {
		return LazyPVFactory.newReadOnlyDoublePV(getChannelName(MinValue_RBV));
	}

	@Override
	public Observable<Double> createMaxObservable() throws Exception {
		return LazyPVFactory.newReadOnlyDoublePV(getChannelName(MaxValue_RBV));
	}
	@Override
	public Observable<Double> createMeanObservable() throws Exception {
		return LazyPVFactory.newReadOnlyDoublePV(getChannelName(MeanValue_RBV));
	}
	@Override
	public Observable<Double> createTotalObservable() throws Exception {
		return LazyPVFactory.newReadOnlyDoublePV(getChannelName(Total_RBV));
	}
	@Override
	public Observable<Double> createSigmaObservable() throws Exception {
		return LazyPVFactory.newReadOnlyDoublePV(getChannelName(Sigma_RBV));
	}

}
