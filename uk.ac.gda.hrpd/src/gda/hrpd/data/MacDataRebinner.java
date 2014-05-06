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
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.scan.ScanBase;

public class MacDataRebinner implements IObservable, Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(MacDataRebinner.class);
	private ObservableComponent observers = new ObservableComponent();
	private boolean rebinCompleted = false;
	private File inputDataFile;
	private File outputDataFile;

	@Override
	public void addIObserver(IObserver anIObserver) {
		observers.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observers.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observers.deleteIObservers();
	}

	@Override
	public void run() {
		if (inputDataFile == null) {
			throw new IllegalArgumentException("You must provide input data file of the raw MAC data.");
		}
		outputDataFile = rebinning(inputDataFile);
		if (outputDataFile != null) {
			observers.notifyIObservers(this, outputDataFile);
		}
	}

	public File getInputDataFile() {
		return inputDataFile;
	}

	public void setInputDataFile(File inputDataFile) {
		this.inputDataFile = inputDataFile;
	}

	public File getOutputDataFile() {
		return outputDataFile;
	}

	public void setOutputDataFile(File outputDataFile) {
		this.outputDataFile = outputDataFile;
	}

	/**
	 * Rebin the raw MAC data to normalise, combine all detectors data into a single profile 
	 * using external python script.
	 * 
	 * @param file - the raw data file
	 * @return the rebinned data file
	 */
	private File rebinning(File file) {
		File rebinnedDataFile=null;
		if (InterfaceProvider.getCurrentScanController().isFinishEarlyRequested()) {
			return null;
		}
		JythonServerFacade.getInstance().print("Data" + file.getPath() +" rebinning started.");

		String[] cmdArray = new String[4];
		cmdArray[0] = LocalProperties.get("gda.cvscan.python");
		cmdArray[1] = LocalProperties.get("gda.cvscan.rebin.program");
		try {
			cmdArray[2] = file.getCanonicalPath();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		cmdArray[3] = LocalProperties.get("gda.cvscan.rebin.step");

		Runtime rt = Runtime.getRuntime();

		int exitValue = Integer.MAX_VALUE;
		try {
			exitValue = rt.exec(cmdArray).waitFor();
		} catch (IOException e1) {
			JythonServerFacade.getInstance().print("Data rebin failed.");
			logger.error("Data rebin failed.", e1);
		} catch (InterruptedException e) {
			JythonServerFacade.getInstance().print("Data rebin is interrupted.");
			logger.error("Data rebin is interrupted.", e);
		} finally {
			if (exitValue == 0) {
				JythonServerFacade.getInstance().print("Data Rebin Completed Successfully.");
				String path = file.getParent();
				String rebinnedfilename=file.getName().replace(".dat","_red.dat");
				String rebinnedFileName = path + File.separator + "processing" + File.separator + rebinnedfilename;
				rebinnedDataFile = new File(rebinnedFileName);
			} else {
				JythonServerFacade.getInstance().print("Data Rebin Failed.");
			}
		}
		return rebinnedDataFile;
	}
}
