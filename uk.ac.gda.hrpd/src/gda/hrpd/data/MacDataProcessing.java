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

import gda.analysis.Plotter;
import gda.analysis.ScanFileHolder;
import gda.analysis.io.MACLoader;
import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class provides function and controls for post MAC data collection processing, covering data rebin and plotting.
 * It only supports plotting of rebinned data.
 */
public class MacDataProcessing implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(MacDataProcessing.class);
	private static MacDataProcessing instance;
	private boolean rebinCompleted = false;
	
	/**
	 * check if rebinning completed successfully or not
	 * @return boolean
	 */
	public boolean isRebinCompleted() {
		return rebinCompleted;
	}

	/**
	 * set rebinning processing state.
	 * @param rebinCompleted
	 */
	public void setRebinCompleted(boolean rebinCompleted) {
		this.rebinCompleted = rebinCompleted;
	}

	private boolean enabled=true;

	/**
	 * check if MAC data processing, i.e. rebinning and plotting enabled or not.
	 * @return boolean - true: rebin & plot, false: no rebin & no plot
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * set MAC data processing state during data collection, true: enable; false: disable.
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * enable the MAC data processing - i.e. rebinning and plotting during data collection
	 */
	public void enable() {
		setEnabled(true);
	}

	/**
	 * disable the MAC data processing - i.e. no rebinning, no plotting during data collection
	 */
	public void disable() {
		setEnabled(false);
	}
	/**
	 * Rebin the raw MAC data to normalise, combine all detectors data into a single profile 
	 * using external python script.
	 * 
	 * @param filename - the raw data file name
	 * @return the rebinned data file name
	 */
	public String rebinning(String filename) {
		String rebinnedFileName=null;
		if (InterfaceProvider.getCurrentScanController().isFinishEarlyRequested()) {
			return null;
		}
		JythonServerFacade.getInstance().print("Post-Scan data reduction - rebinning, Please Wait...");

		String[] cmdArray = new String[4];
		cmdArray[0] = LocalProperties.get("gda.cvscan.python");
		cmdArray[1] = LocalProperties.get("gda.cvscan.rebin.program");
		cmdArray[2] = filename;
		cmdArray[3] = LocalProperties.get("gda.cvscan.rebin.step");

		setRebinCompleted(false);
		Runtime rt = Runtime.getRuntime();

		int exitValue = Integer.MAX_VALUE;
		try {
			exitValue = rt.exec(cmdArray).waitFor();
		} catch (IOException e1) {
			JythonServerFacade.getInstance().print("Post process data rebin failed.");
			logger.error("Post process data rebin failed.", e1);
			setRebinCompleted(false);
		} catch (InterruptedException e) {
			JythonServerFacade.getInstance().print("Post process data rebin is interrupted.");
			logger.error("Post process data rebin is interrupted.", e);
			setRebinCompleted(false);
		} finally {
			if (exitValue == 0) {
				JythonServerFacade.getInstance().print("Data Rebin Completed Successfully.");
				File file =new File(filename);
				String path = file.getParent();
				String rebinnedfilename=file.getName().replace(".dat","_red.dat");
				rebinnedFileName = path + File.separator + "processing" + File.separator + rebinnedfilename;
				setRebinCompleted(true);
			} else {
				JythonServerFacade.getInstance().print("Data Rebin Failed.");
				setRebinCompleted(false);
			}
		}
		return rebinnedFileName;
	}
	/**
	 * plot rebinned data on DataVector panel
	 * @param filename - the rebinned data file name
	 * @throws IllegalArgumentException
	 */
	public void plotData(String filename) throws IllegalArgumentException {
		if (InterfaceProvider.getCurrentScanController().isFinishEarlyRequested()) {
			return;
		}
		if (!rebinCompleted)
			return;

		ScanFileHolder sfh = new ScanFileHolder();
		try {
			sfh.load(new MACLoader(filename));
		} catch (ScanFileHolderException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		// sfh.loadMACData(inputFileName);
		JythonServerFacade.getInstance().print("Data plotting, please wait ...");
		Plotter.plot("MAC", sfh.getAxis(0), sfh.getAxis(1));
		JythonServerFacade.getInstance().print("Post processing completed");
	}

	/**
	 * return the singleton instance
	 * @return the instance
	 */
	public static MacDataProcessing getInstance() {
		if( instance == null){
			instance = new MacDataProcessing();
		}
		return instance;
	}

}
