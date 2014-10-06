package org.opengda.lde.scannables;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.scan.ScanInformation;
import gda.util.OSCommandRunner;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataReductionScannable extends ScannableBase implements Scannable {
	private static final Logger logger=LoggerFactory.getLogger(DataReductionScannable.class);
	private Scriptcontroller eventAdmin;
	private boolean calibrant=false;
	private String Current_Calibrant_Data_Filename=null;
	private String command;
	@Override
	public void atScanEnd() {
		ScanInformation scaninfo=InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		final String filename = scaninfo.getFilename();
		logger.info("Starting data reduction processing on the cluster...");
		if (isCalibrant()) {
			command=LocalProperties.get("gda.lde.datareduction.software","/dls_sw/apps/i11-scripts/bin/LDE-RunFromGDAAtEndOfScan.sh")+" "+filename;
			Current_Calibrant_Data_Filename=filename;
			setCalibrant(false);
		} else {
			if (Current_Calibrant_Data_Filename==null || Current_Calibrant_Data_Filename.isEmpty() ) {
				InterfaceProvider.getTerminalPrinter().print("No calibrant data filename provided, so cannot start data reduction. Please collect a Calibrant diffraction first.");
				logger.warn("No calibrant data filename provided, so cannot start data reduction. Please collect a Calibrant diffraction first.");
				return;
			}
			command=LocalProperties.get("gda.lde.datareduction.software","/dls_sw/apps/i11-scripts/bin/LDE-RunFromGDAAtEndOfScan.sh")+" "+Current_Calibrant_Data_Filename+" "+filename;
			
		}
		Thread resultThread=new Thread(new Runnable() {
			
			@Override
			public void run() {
				Callable<String> r = new Callable<String>() {
					@Override
					public String call() throws Exception {
						OSCommandRunner osCommandRunner = new OSCommandRunner(command, true, null, null);
						if (osCommandRunner.exception != null) {
							String msg = "Exception seen trying to run command " + osCommandRunner.getCommandAsString();
							logger.error(msg);
							logger.error(osCommandRunner.exception.toString());
						} else if (osCommandRunner.exitValue != 0) {
							String msg = "Exit code = " + Integer.toString(osCommandRunner.exitValue)
									+ " returned from command " + osCommandRunner.getCommandAsString();
							logger.warn(msg);
							osCommandRunner.logOutput();
						} else {
							osCommandRunner.logOutput();
						}
						return FilenameUtils.getPath(filename)+"processed/"+FilenameUtils.getBaseName(filename)+".xy";
					}
				};
				final ExecutorService executor = Executors.newFixedThreadPool(1);		
				final Future<String> submit = executor.submit(r);
				String result;
				try {
					result=submit.get();
					if (getEventAdmin() != null) {
						((ScriptControllerBase)eventAdmin).update(this, result);
					}
					logger.info("Data reduction processing is completed on the cluster.");
				} catch (InterruptedException e) {
					logger.error("Data reduction process is interrupted.", e);
				} catch (ExecutionException e) {
					logger.error("Data reduction process is aborted.", e);
				}
				executor.shutdown();
			}
		});
		resultThread.start();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return "";
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	public Scriptcontroller getEventAdmin() {
		return eventAdmin;
	}

	public void setEventAdmin(Scriptcontroller eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	public boolean isCalibrant() {
		return calibrant;
	}

	public void setCalibrant(boolean calibrant) {
		this.calibrant = calibrant;
	}

}
