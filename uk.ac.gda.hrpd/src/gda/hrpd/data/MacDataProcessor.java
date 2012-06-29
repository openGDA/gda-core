/*-
 * Copyright © 2010 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.hrpd.data;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MacDataProcessor extends FileBasedDataProcessor {
	private static final Logger logger = LoggerFactory
			.getLogger(MacDataProcessor.class);
	

	public enum Mode {
		SERIAL, PARALLEL
	}

	public static Mode MODE = Mode.SERIAL;
	private MacDataRebinner rebinner;
	private MacDataPlotter plotter;
	private boolean enabled = true;
	private boolean plotData = true;

	public boolean isPlotData() {
		return plotData;
	}

	public void setPlotData(boolean plotData) {
		this.plotData = plotData;
	}

	public MacDataProcessor() {
		rebinner = new MacDataRebinner();
		plotter = new MacDataPlotter();
		if (isPlotData()) {
			rebinner.addIObserver(plotter);
		}
	}

	@Override
	public File processData(File rawdata) {
		rebinner.setInputDataFile(rawdata);
		Thread rebinThread = new Thread(rebinner, "Rebinning Thread");
		rebinThread.start();
		if (MODE == Mode.SERIAL) {
			// running post collection data processing in sequence - blocking
			while (rebinThread.isAlive()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.warn("rebin processing is interrupted", e);
				}
			}
			return rebinner.getOutputDataFile();
		}
		// running post collection data processing in parallel - non-blocking -
		// no filename return
		return null;
	}


	@Override
	public void completeProcess() {
		completeCollection(this);
	}
	
	public void setMode(Mode mode) {
		MODE = mode;
	}

	/**
	 * check if MAC data processing, i.e. rebinning and plotting enabled or not.
	 * 
	 * @return boolean - true: rebin & plot, false: no rebin & no plot
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * set MAC data processing state during data collection, true: enable;
	 * false: disable.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * enable the MAC data processing - i.e. rebinning and plotting during data
	 * collection
	 */
	public void enable() {
		setEnabled(true);
	}

	/**
	 * disable the MAC data processing - i.e. no rebinning, no plotting during
	 * data collection
	 */
	public void disable() {
		setEnabled(false);
	}
}
