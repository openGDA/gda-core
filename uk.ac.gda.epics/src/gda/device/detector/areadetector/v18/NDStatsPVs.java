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

package gda.device.detector.areadetector.v18;

import gda.epics.PV;
import gda.epics.ReadOnlyPV;

import java.util.Map;



//PV<TSControlCommands> tsControlPV = LazyPVFactory.newEnumPV(basePVName + "Control", TSControlCommands.class);
//PV<Integer> tsNumPointsPV = LazyPVFactory.newIntegerPV(basePVName + "NumPoints");
//ReadOnlyPV<Integer> tsCurrentPointPV = LazyPVFactory.newReadOnlyIntegerPV(basePVName + "CurrentPoint");
//Map<Stat, ReadOnlyPV<Double[]>> tsArrayPVMap = new HashMap<ADTimeSeriesStats.Stat, ReadOnlyPV<Double[]>>();
//BasicStat[] values = BasicStat.values();
//tsArrayPVMap.putAll(createTsArrayMap(values, basePVName));

//private Map<Stat, ReadOnlyPV<Double[]>> createTsArrayMap(Stat[] statList, String basePVName) {
//	Map<Stat, ReadOnlyPV<Double[]>>  pvMap = new HashMap<ADTimeSeriesStats.Stat, ReadOnlyPV<Double[]>>();
//	for (Stat stat : statList) {
//		String pvName = basePVName + stat.name();
//		pvMap.put(stat, LazyPVFactory.newReadOnlyDoubleArrayPV(basePVName + stat.))
//	}
//	return pvMap;
//}

///**
// * 
// * @param basePVName base PV name ending in TS. E.g. BLRWI-DI-CAM-01:STAT1TS
// */
//public ADTimeSeriesStatsPlugin(String basePVName) {
//	
//}

public interface NDStatsPVs {

	/**
	 * From http://cars9.uchicago.edu/software/epics/NDPluginStats.html.
	 */
	public enum TSControlCommands {

		/**
		 * Erase/Start: Clears all time-series arrays, sets TS_CURRENT_POINT=0, and starts time-series data collection
		 */
		ERASE_AND_START,

		/**
		 * Start: Starts time-series data collection without clearing arrays or modifying TS_CURRENT_POINT. Used to
		 * restart collection after a Stop operation.
		 */
		START,

		/**
		 * Stop: Stops times-series data collection. Performs callbacks on all time-series waveform records.
		 */
		STOP,

		/**
		 * Read: Performs callbacks on all time-series waveform records, updating the values.
		 */
		READ
	}

	public interface Stat {
	}

	/**
	 * Note: these must map to PV Names
	 */
	public enum BasicStat implements Stat {
		MinValue, MinX, MinY, MaxValue, MaxX, MaxY, MeanValue, Sigma, Total, Net,
	}

	/**
	 * Note: these must map to PV Names
	 */
	public enum CentroidStat implements Stat {
		CentroidX, CentroidY, SigmaX, SigmaY, SigmaXY
	}

	public PV<Boolean> getEnableCallbacksPV();

	public PV<TSControlCommands> getTSControlPV();

	public PV<Integer> getTSNumPointsPV();

	public ReadOnlyPV<Integer> getTSCurrentPointPV();

	public Map<Stat, ReadOnlyPV<Double[]>> getTSArrayPVMap();

}
