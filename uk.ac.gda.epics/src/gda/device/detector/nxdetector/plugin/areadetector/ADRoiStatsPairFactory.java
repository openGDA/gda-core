/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;

import gda.device.detector.areadetector.v18.NDStatsPVs.BasicStat;
import gda.device.detector.areadetector.v18.NDStatsPVs.CentroidStat;
import gda.device.detector.nxdetector.roi.RectangularROIProvider;
import gda.device.detector.nxdetector.roi.SimpleRectangularROIProvider;
/**
 * create a ROI and STAT pair to support region of interest acquisition using EPICS Time Series plug-in.
 *
 * example Spring configuration:
 * <pre>
 * {@code
 * 	<bean id="medipix_RoiStats1" class="gda.device.detector.nxdetector.plugin.areadetector.ADRoiStatsPairFactory">
		<property name="pluginName" value="medipix_RoiStats1" />
		<property name="baseRoiPVName" value="BL06I-EA-DET-02:ROI1:" />
		<property name="baseStatsPVName" value="BL06I-EA-DET-02:STAT1:" />
		<property name="legacyTSpvs" value="false"/>
		<property name="roiInputNdArrayPort" value="mpx.CAM" />
		<property name="enabledBasicStats" value="MeanValue" />
		<property name="oneTimeSeriesCollectionPerLine" value="false" />
		<property name="roiProvider">
			<bean class="gda.device.detector.nxdetector.roi.LiveStreamRoiIndexer">
				<property name="liveStreamRoiProvider" ref="medipix_roi" />
				<property name="index" value="0" /> <!-- Zero based indexing i.e. Region 1 = index 0 -->
			</bean>
		</property>
	</bean>
 * }
 * </pre>
 *
 * @since 9.14 added property 'legacyTSpvs' to handle EPICS Time Series PVs change, default to true so existing or legacy configuration still works without modification.
 */
public class ADRoiStatsPairFactory implements FactoryBean<ADRoiStatsPair> {

	private String pluginName;

	private String baseRoiPVName;

	private String baseStatsPVName;

	private boolean legacyTSpvs=true;

	private RectangularROIProvider<Integer> roiProvider = new SimpleRectangularROIProvider();

	/**
	 * Defaults to {@link SimpleRectangularROIProvider}.
	 * @param roiProvider
	 */
	public void setRoiProvider(RectangularROIProvider<Integer> roiProvider) {
		this.roiProvider = roiProvider;
	}

	public void setEnabledBasicStats(List<BasicStat> enabledBasicStats) {
		this.enabledBasicStats = enabledBasicStats;
	}

	public void setEnabledCentroidStats(List<CentroidStat> enabledCentroidStats) {
		this.enabledCentroidStats = enabledCentroidStats;
	}

	private String roiInputNdArrayPort;

	private List<BasicStat> enabledBasicStats = Arrays.asList();

	private List<CentroidStat> enabledCentroidStats = Arrays.asList();

	private Boolean oneTimeSeriesCollectionPerLine = null;

	private boolean EnableROIPVPairSupported = true; //this flag is introduced because xmap detector used an old EPICs version

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public void setBaseRoiPVName(String baseRoiPVName) {
		this.baseRoiPVName = baseRoiPVName;
	}

	public void setBaseStatsPVName(String baseStatsPVName) {
		this.baseStatsPVName = baseStatsPVName;
	}

	public void setRoiInputNdArrayPort(String roiInputNdArrayPort) {
		this.roiInputNdArrayPort = roiInputNdArrayPort;
	}

	public boolean isOneTimeSeriesCollectionPerLine() {
		return oneTimeSeriesCollectionPerLine;
	}
	/**
	 * Perform one time series per collection per line, rather than one per scan. Defaults to true.
	 * @param oneTimeSeriesCollectionPerLine
	 */
	public void setOneTimeSeriesCollectionPerLine(boolean oneTimeSeriesCollectionPerLine) {
		this.oneTimeSeriesCollectionPerLine = oneTimeSeriesCollectionPerLine;
	}

	@Override
	public ADRoiStatsPair getObject() throws Exception {
		ADRectangularROIPlugin roiPlugin = ADRectangularROIPlugin.createFromBasePVName(pluginName + "_roi", baseRoiPVName, roiProvider);
		roiPlugin.setEnablePVPairSupported(EnableROIPVPairSupported);
		ADTimeSeriesStatsPlugin statsPlugin = ADTimeSeriesStatsPlugin.createFromBasePVName(pluginName + "_stats", baseStatsPVName, roiProvider, isLegacyTSpvs());
		if (oneTimeSeriesCollectionPerLine != null) { // else use the plugin's default
			statsPlugin.setOneTimeSeriesCollectionPerLine(oneTimeSeriesCollectionPerLine);
		}
		ADRoiStatsPair pair = new ADRoiStatsPair(pluginName, roiPlugin, statsPlugin, roiInputNdArrayPort, roiProvider);
		pair.setEnabledBasicStats(enabledBasicStats);
		pair.setEnabledCentroidStats(enabledCentroidStats);
		return pair;
	}

	public boolean isEnableROIPVPairSupported() {
		return EnableROIPVPairSupported;
	}

	public void setEnableROIPVPairSupported(boolean enableROIPVPairSupported) {
		EnableROIPVPairSupported = enableROIPVPairSupported;
	}

	@Override
	public Class<?> getObjectType() {
		return ADRoiStatsPair.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	public boolean isLegacyTSpvs() {
		return legacyTSpvs;
	}

	public void setLegacyTSpvs(boolean legacyTSpvs) {
		this.legacyTSpvs = legacyTSpvs;
	}

}
