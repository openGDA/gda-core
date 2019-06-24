package org.opengda.lde.scannables;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.FilenameUtils;
import org.opengda.lde.events.DataReductionFailedEvent;
import org.opengda.lde.events.DataReductionWarnEvent;
import org.opengda.lde.events.NewDataFileEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.fileregistrar.FileRegistrarHelper;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.DummyScannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.device.scannable.SimpleUDPServerScannable;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;
import gda.util.OSCommandRunner;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(Scannable.class)
public class DataReductionScannable extends DummyScannable implements IObserver {
	private static final Logger logger=LoggerFactory.getLogger(DataReductionScannable.class);
	private Scriptcontroller eventAdmin;
	private boolean calibrant=false;
	private String Current_Calibrant_Data_Filename=null;
	private String command;
	private String filename=null;
	private double timeout=30000; //30 seconds
	private StringValueScannable currentCalibrationScannable;
	private SimpleUDPServerScannable simpleUDPServer;
	private String sampleID=null;
	private Map<String, String> map=new HashedMap<>();


	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (simpleUDPServer!=null){
				simpleUDPServer.addIObserver(this);
			}
			super.configure();
			setConfigured(true);
		}
	}
	@Override
	public void close() throws DeviceException {
		if (isConfigured()) {
			if (simpleUDPServer!=null){
				simpleUDPServer.deleteIObserver(this);
			}
			super.close();
			setConfigured(false);
		}
	}
	@Override
	public void atScanEnd() {
		if (InterfaceProvider.getCurrentScanController().isFinishEarlyRequested()) {
			return;
		}
		Thread resultThread=new Thread(new Runnable() {

			@Override
			public void run() {
				long starttimer=System.currentTimeMillis();
				long elapsedtimer=System.currentTimeMillis();
				try {
					while (InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint()==null) {
						Thread.sleep(100);
						elapsedtimer=System.currentTimeMillis()-starttimer;
						if (elapsedtimer>timeout) {
							InterfaceProvider.getTerminalPrinter().print("Timeout while waiting for filename from last scan data point");
							logger.error("Timeout while waiting for filename from last scan data point");
							break;
						}
					}
				} catch (InterruptedException e) {
					logger.error("Thread interrupted while waiting for data point", e);
					Thread.currentThread().interrupt();
					return;
				}
				filename=InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint().getCurrentFilename();
				if (!isCalibrant()) {
					logger.info("Starting data reduction processing on the cluster...");
					InterfaceProvider.getTerminalPrinter().print("Starting data reduction processing on the cluster for file "+filename+" ...");
				} else {
					logger.info("Starting detector calibration processing on the cluster...");
					InterfaceProvider.getTerminalPrinter().print("Starting detector calibration processing on the cluster using file "+filename+" ...");
				}
				if (isCalibrant()) {
					if (filename == null || filename.isEmpty()) {
						InterfaceProvider.getTerminalPrinter().print("No calibrant data filename provided, so cannot start data reduction.");
						logger.warn("No calibrant data filename provided, so cannot start data reduction.");
						return;
					}

					command=LocalProperties.get("gda.lde.datareduction.software","/dls_sw/apps/i11-scripts/bin/LDE-RunFromGDAAtEndOfScan.sh")+" "+filename;
					setCurrentCalibrantDataFilename(filename);
				} else {
					if (getCurrentCalibrantDataFilename()==null || getCurrentCalibrantDataFilename().isEmpty() ) {
						InterfaceProvider.getTerminalPrinter().print("No calibrant data filename provided, so cannot start data reduction. Please collect a Calibrant diffraction first.");
						logger.warn("No calibrant data filename provided, so cannot start data reduction. Please collect a Calibrant diffraction first.");
						return;
					}
					if (filename == null || filename.isEmpty()) {
						InterfaceProvider.getTerminalPrinter().print("No data filename provided, so cannot start data reduction.");
						logger.warn("No data filename provided, so cannot start data reduction.");
						return;
					}
					command=LocalProperties.get("gda.lde.datareduction.software","/dls_sw/apps/i11-scripts/bin/LDE-RunFromGDAAtEndOfScan.sh")+" "+getCurrentCalibrantDataFilename()+" "+filename;
					if (getSampleID()!=null) {
						map.put(FilenameUtils.getBaseName(filename), getSampleID());
					}
				}
				Callable<String> r = new Callable<String>() {
					@Override
					public String call() throws Exception {
						String msg;
						OSCommandRunner osCommandRunner = new OSCommandRunner(command, true, null, null);
						if (osCommandRunner.exception != null) {
							msg = "Exception seen trying to run command " + osCommandRunner.getCommandAsString();
							logger.error(msg);
							logger.error(osCommandRunner.exception.toString());
						} else if (osCommandRunner.exitValue != 0) {
							msg = "Exit code = " + Integer.toString(osCommandRunner.exitValue)
									+ " returned from command " + osCommandRunner.getCommandAsString();
							logger.warn(msg);
							osCommandRunner.logOutput();
						} else {
							if (!isCalibrant()) {
								msg="Data reduction processing is completed on the cluster.";
							} else {
								msg="Detector calibration processing is completed on the cluster.";
							}
							osCommandRunner.logOutput();
						}
						return msg;
					}
				};
				final ExecutorService executor = Executors.newFixedThreadPool(1);
				final Future<String> submit = executor.submit(r);
				String result;
				try {
					result=submit.get(600, TimeUnit.SECONDS);
					logger.info(result);
					InterfaceProvider.getTerminalPrinter().print(result);
				} catch (InterruptedException e) {
					logger.error("Data reduction process is interrupted.", e);
					InterfaceProvider.getTerminalPrinter().print("Data reduction process is interrupted.");
				} catch (ExecutionException e) {
					logger.error("Data reduction process is aborted.", e);
					InterfaceProvider.getTerminalPrinter().print("Data reduction process is aborted.");
				} catch (TimeoutException e) {
					logger.error("Data reduction process takes too long, more than 600 seconds.", e);
					InterfaceProvider.getTerminalPrinter().print("Data reduction process does not return results in 600 seconds.");
				}
				executor.shutdown();
			}
		});
		resultThread.start();
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
		if (!calibrant) return;
		try {
			currentCalibrationScannable.moveTo("Undefined");
		} catch (DeviceException e) {
			logger.error("Failed to reset the data filename of current calibration scannable");
		}
	}

	public String getCurrentCalibrantDataFilename() {
		return Current_Calibrant_Data_Filename;
	}

	public void setCurrentCalibrantDataFilename(String calibrantFilename) {
		Current_Calibrant_Data_Filename = calibrantFilename;
		try {
			if (isCalibrant()) {
				currentCalibrationScannable.moveTo(calibrantFilename);
			}
		} catch (DeviceException e) {
			logger.error("failed to set the data file name for the current calibration scannable to {}", calibrantFilename);
		}
	}

	public StringValueScannable getCurrentCalibrationScannable() {
		return currentCalibrationScannable;
	}

	public void setCurrentCalibrationScannable(
			StringValueScannable currentCalibrationScannable) {
		this.currentCalibrationScannable = currentCalibrationScannable;
	}

	public SimpleUDPServerScannable getSimpleUDPServer() {
		return simpleUDPServer;
	}

	public void setSimpleUDPServer(SimpleUDPServerScannable simpleUDPServer) {
		this.simpleUDPServer = simpleUDPServer;
	}

	@Override
	public void update(Object source, Object arg) {
		if(arg instanceof ScannablePositionChangeEvent){
			Object pos = ((ScannablePositionChangeEvent)arg).newPosition;
			if( pos instanceof String){
				String messages=(String)pos;
				String [] fields = messages.split(",");
				String baseName = FilenameUtils.getBaseName(fields[1]);
				String sampleid = map.get(baseName);
				if (fields[0].equalsIgnoreCase("OK")) {
					InterfaceProvider.getTerminalPrinter().print("Plotting reduced data from file "+fields[1]);
					logger.info("Plotting reduced data from file {}",fields[1]);
					String reducedFilename = fields[1];
					FileRegistrarHelper.registerFile(reducedFilename);
					if (getEventAdmin() != null) {
						eventAdmin.update(getEventAdmin(), new NewDataFileEvent(sampleid, reducedFilename));
					}
				} else if (fields[0].equalsIgnoreCase("FAIL")) {
					InterfaceProvider.getTerminalPrinter().print("Data reduction failed: "+fields[1]);
					logger.warn("Data reduction failed: {}",fields[1]);
					if (getEventAdmin() != null) {
						eventAdmin.update(getEventAdmin(), new DataReductionFailedEvent(sampleid, fields[1]));
					}
				} else if(fields[0].equalsIgnoreCase("WARN")) {
					InterfaceProvider.getTerminalPrinter().print("Data reduction returns WARN on file: "+fields[1]+"; Cause: "+fields[2]);
					logger.warn("Data reduction returns on file: {}; Cause: {}",fields[1], fields[2]);
					if (getEventAdmin() != null) {
						eventAdmin.update(getEventAdmin(), new DataReductionWarnEvent(sampleid, fields[1], fields[2]));
					}
				}
				if (map.containsKey(baseName)) {
					map.remove(baseName);
				}
			}
		}
	}
	public String getSampleID() {
		return sampleID;
	}
	public void setSampleID(String sampleID) {
		this.sampleID = sampleID;
	}
}
