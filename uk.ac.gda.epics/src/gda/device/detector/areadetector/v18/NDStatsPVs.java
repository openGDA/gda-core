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

import gda.device.detector.areadetector.v17.NDPluginBasePVs;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;

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

	/**
	 * Enable or disable the *basic* statistics.
	 */
	public PV<Boolean> getComputeStatistsicsPV();

	public PV<Boolean> getComputeCentroidPV();
	
	public NDPluginBasePVs getPluginBasePVs();
	
	public PV<TSControlCommands> getTSControlPV();

	public PV<Integer> getTSNumPointsPV();

	public ReadOnlyPV<Integer> getTSCurrentPointPV();

	public ReadOnlyPV<Double[]> getTSArrayPV(Stat stat);

}
