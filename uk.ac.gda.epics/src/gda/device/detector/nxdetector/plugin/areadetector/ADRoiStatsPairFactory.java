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

import gda.device.detector.areadetector.v18.NDStatsPVs.BasicStat;
import gda.device.detector.areadetector.v18.NDStatsPVs.CentroidStat;
import gda.device.detector.nxdetector.roi.RectangularROIProvider;
import gda.device.detector.nxdetector.roi.SimpleRectangularROIProvider;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;

public class ADRoiStatsPairFactory implements FactoryBean<ADRoiStatsPair> {

	private String pluginName;

	private String baseRoiPVName;

	private String baseStatsPVName;
	
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

	@Override
	public ADRoiStatsPair getObject() throws Exception {
		ADRectangularROIPlugin roiPlugin = ADRectangularROIPlugin.createFromBasePVName(pluginName + "_roi", baseRoiPVName, roiProvider);
		ADTimeSeriesStatsPlugin statsPlugin = ADTimeSeriesStatsPlugin.createFromBasePVName(pluginName + "_stats", baseStatsPVName, roiProvider);
		ADRoiStatsPair pair = new ADRoiStatsPair(pluginName, roiPlugin, statsPlugin, roiInputNdArrayPort, roiProvider);
		pair.setEnabledBasicStats(enabledBasicStats);
		pair.setEnabledCentroidStats(enabledCentroidStats);
		return pair;
	}

	@Override
	public Class<?> getObjectType() {
		return ADRoiStatsPair.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}
