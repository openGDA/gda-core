package org.opengda.lde.experiments;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.jython.commands.ScannableCommands;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;
import gda.scan.Scan.ScanStatus;
import gda.scan.ScanEvent;
import gda.scan.ScanEvent.EventType;
import gda.scan.ScanInformation;
import gda.util.Sleep;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.opengda.lde.events.ProcessMessage;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataCollection implements IObserver {
	private static final Logger logger = LoggerFactory
			.getLogger(DataCollection.class);
	private ArrayList<SampleStage> stages = new ArrayList<SampleStage>();
	private DetectorArm detectorArm;
	private LDEResourceUtil resUtil;
	private Scriptcontroller eventAdmin;
	private Scannable sampleNameScannable;
	private Scannable calibrantNameScannable;
	private List<Sample> samples = new ArrayList<Sample>();
	private MultiMap<String, List<Sample>> stageSamplesMap = new MultiValueMap<String, List<Sample>>();
	private Scannable datareduction;
	private Detector pixium;

	public void parkAllStages() throws DeviceException {
		String message = "Parking all stages...";
		updateMessage(null, message);
		if (getStages() != null && !getStages().isEmpty()) {
			for (SampleStage stage : getStages()) {
				stage.parkStage();
			}
			// wait for all stages to be parked
			boolean allParked = false;
			while (!allParked) {
				boolean parked = true;
				for (SampleStage stage : getStages()) {
					parked = stage.isParked() && parked;
				}
				allParked = parked;
			}
			message="All stages are parked.";
			updateMessage(null, message);
		}
	}

	public void engageStage(SampleStage stage) throws DeviceException {
		String message = "Engage stage '" + stage.getName()+ "' ...";
		updateMessage(null, message);
		stage.engageStage();
		while (!stage.isEngaged()) {
			Sleep.sleep(100);
		}
		message = "Stage " + stage.getName() + " is engaged.";
		updateMessage(null, message);
	}

	public void prepareProcessSamples(String filename) {
		if (resUtil != null) {
			// load samples definition from .lde file
			try {
				samples = resUtil.getSamples(filename);
			} catch (Exception e) {
				String message = "Cannot load samples from experiment sample definition file "+ filename + ".";
				updateMessage(e, message);
			}
		}
		if (samples != null && !samples.isEmpty()) {
			// group sample into stage using stage name as key
			for (Sample sample : samples) {
				stageSamplesMap.put(sample.getCellID().split("-")[0].toLowerCase(), sample);
			}
		}
		for (SampleStage stage : getStages()) {
			stage.setSamplesProcessed(false);
			stage.setDetectorCalibrated(false);
		}
	}

	public void processStages() {
		String message="Processing sample stages ...";
		for (SampleStage stage : getStages()) {
			@SuppressWarnings("unchecked")
			List<Sample> samples = (List<Sample>) stageSamplesMap.get(stage.getName());
			if (samples == null || samples.isEmpty()) {
				// No samples on this stage
				if (stage == getStages().get(getStages().size() - 1)) {
					// all stages are done now, no need to engage the last stage
					try {
						parkAllStages();
					} catch (DeviceException e) {
						message = "Park all stage failed: "+ e.getMessage();
						updateMessage(e, message);
					}
				} else {
					try {
						engageStage(stage);
					} catch (DeviceException e) {
						message="Engage stage '" + stage.getName()+ "' failed: "+e.getMessage();
						updateMessage(e, message);
					}
				}
			} else {
				processSamples(stage);
			}
		}
	}

	private void processSamples(SampleStage stage) {
			moveDetectorToPosition(stage);
			doDetectorCalibration(stage);
			doSampleDataCollection(stage);
	}

	private void moveDetectorToPosition(SampleStage stage) {
		@SuppressWarnings("unchecked")
		List<Sample> samples = (List<Sample>) stageSamplesMap.get(stage.getName());
		Sample sample1 = samples.get(0);
		double sampleDetectorZPosition=sample1.getDetector_z();
		String message="Move detector to its position "+sampleDetectorZPosition+" relative to the stage '"+stage.getName()+"' ...";
		updateMessage(null, message);
		for (Sample sample : samples) {
			if (sample.getDetector_z() != sampleDetectorZPosition) {
				//TODO current here does not support samples on the stage having different distance from the detector. 
				message= "All samples on one the stage '"+stage.getName()+"' must have the same distance from the detector";
				updateMessage(null, message);
				throw new IllegalStateException(message);
			}
		}
		try {
			if (!detectorArm.isAtXPosition(sample1)) {
				detectorArm.getXMotor().asynchronousMoveTo(sample1.getDetector_x());
				message="Move detector X motor to position "+sample1.getDetector_x()+" ...";
				updateMessage(null, message);
			}
		} catch (DeviceException e) {
			message="Error check or move detector X motor: "+e.getMessage();
			updateMessage(e, message);
		}
		try {
			if (!detectorArm.isAtYPosition(sample1)) {
				detectorArm.getYMotor().asynchronousMoveTo(sample1.getDetector_y());
				message="Move detector Y motor to position "+sample1.getDetector_y()+" ...";
				updateMessage(null, message);
			}
		} catch (DeviceException e) {
			message="Error check or move detector Y motor: "+e.getMessage();
			updateMessage(e, message);
		}
		boolean atZPosition = false;
		try {
			atZPosition = detectorArm.isAtZPosition(sample1, stage.getzPosition());
		} catch (DeviceException e1) {
			message="Failed on checking detector Z position: "+e1.getMessage();
			updateMessage(e1, message);
		}
		if (atZPosition) {
			message="Detector is already at position "+ sample1.getDetector_z()+" relative to the stage '"+stage.getName()+"'";
			updateMessage(null, message);
			return;
		} else {
			try {
				message="Move detector Z motor to position "+(sampleDetectorZPosition+stage.getzPosition())+" ...";
				updateMessage(null, message);
				detectorArm.getZMotor().moveTo(stage.getzPosition()+sampleDetectorZPosition);
				message="Detector is at position "+sampleDetectorZPosition+" relative to the stage '"+stage.getName()+"'";
				updateMessage(null, message);
			} catch (DeviceException e) {
				message = "Failed to move detector to position "+sampleDetectorZPosition+" relative to stage '"+stage.getName()+"'";
				updateMessage(e, message);
			}
		}
	}

	private void doDetectorCalibration(SampleStage stage) {
		@SuppressWarnings("unchecked")
		List<Sample> samples = (List<Sample>) stageSamplesMap.get(stage.getName());
		Sample sample1 = samples.get(0);
		String calibrant = sample1.getCalibrant();
		String message="Starting detector calibration using calibrant '"+calibrant+"' for stage '"+stage.getName()+"' ...";
		updateMessage(null, message);		
		// set calibrant name
		if (getCalibrantNameScannable()!=null) {
			try {
				getCalibrantNameScannable().moveTo(calibrant);
			} catch (DeviceException e) {
				message="Failed to set calibrant name to '"+calibrant+"' for stage '"+stage.getName()+"'.";
				updateMessage(e, message);
			}
		}
		
		//TODO move calibrant into beam
		try {
			stage.getXMotor().asynchronousMoveTo(sample1.getCalibrant_x());
		} catch (DeviceException e) {
			message="Failed to move stage '"+stage.getName()+"x' to "+sample1.getCalibrant_x()+".";
			updateMessage(e, message);
		}
		try {
			stage.getYMotor().asynchronousMoveTo(sample1.getCalibrant_y());
		} catch (DeviceException e) {
			message="Failed to move stage '"+stage.getName()+"y' to "+sample1.getCalibrant_y()+".";
			updateMessage(e, message);
		}
		// collect calibrant diffraction data with data reduction
		InterfaceProvider.getJSFObserver().addIObserver(this);
		try {
			ScannableCommands.scan(getDatareduction(), 1,1,1, getPixium(), sample1.getCalibrant_exposure());
			stage.setDetectorCalibrated(true);
		} catch (Exception e) {
			message="Scan failed during calibrant diffraction collection: "+e.getMessage();
			updateMessage(e, message);
			stage.setDetectorCalibrated(false);
		} finally {
			InterfaceProvider.getJSFObserver().deleteIObserver(this);
		}
	}

	private void doSampleDataCollection(SampleStage stage) {
		String message="Starting diffraction data collection for samples on stage '"+stage.getName()+"'...";
		updateMessage(null, message);
		@SuppressWarnings("unchecked")
		List<Sample> samples = (List<Sample>) stageSamplesMap.get(stage.getName());
		InterfaceProvider.getJSFObserver().addIObserver(this);
		boolean success=true;
		for (Sample sample : samples) {
			String name = sample.getName();
			if (getSampleNameScannable()!=null) {
				try {
					getSampleNameScannable().moveTo(name);
				} catch (DeviceException e) {
					message="Failed to set sample name to '"+name+"' on the stage '"+stage.getName()+"'.";
					updateMessage(e, message);
				}
			}
			Scannable x_motor = stage.getXMotor();
			Double x_start = sample.getSample_x_start();
			Double x_stop = sample.getSample_x_stop();
			Double x_step = sample.getSample_x_step();
			
			if (x_start==Double.NaN || x_step==Double.NaN || x_stop==Double.NaN) {
				message="Missing parameters: Sample '"+name+"' must have X motor positions being provided for start, stop, and step.";
				updateMessage(null, message);
				throw new IllegalArgumentException(message);
			}
			ArrayList<Object> scanparameters=new ArrayList<Object>();
			scanparameters.add(x_motor);
			scanparameters.add(x_start);
			scanparameters.add(x_stop);
			scanparameters.add(x_step);
			Double y_start = sample.getSample_y_start();
			Double y_stop = sample.getSample_y_stop();
			Double y_step = sample.getSample_y_step();
			if (y_start!=Double.NaN) {
				scanparameters.add(stage.getYMotor());
				scanparameters.add(y_start);
				if (y_stop!=Double.NaN) {
					scanparameters.add(y_stop);
				}
				if (y_step!=Double.NaN) {
					scanparameters.add(y_step);
				}
			}
			scanparameters.add(pixium);
			double sample_exposure = sample.getSample_exposure();
			scanparameters.add(sample_exposure);
			scanparameters.add(datareduction);
			try {
				ScannableCommands.scan(scanparameters);
				success = true && success;
			} catch (Exception e) {
				message="Scan failed during sample '"+name+"' diffraction collection: "+e.getMessage();
				updateMessage(e, message);
				success=false && success;
			}
		}
		if (success) {
			stage.setSamplesProcessed(true);
		} else {
			stage.setSamplesProcessed(false);
		}
		InterfaceProvider.getJSFObserver().deleteIObserver(this);
	}

	private void updateMessage(Exception e, String message) {
		if (e != null)
			logger.error(message, e);
		else
			logger.info(message);
		print(message);
		if (eventAdmin != null) {
			((ScriptControllerBase) eventAdmin).update(eventAdmin,
					new ProcessMessage(message));
		}
	}

	public LDEResourceUtil getResUtil() {
		return resUtil;
	}

	public void setResUtil(LDEResourceUtil resUtil) {
		this.resUtil = resUtil;
	}

	public Scriptcontroller getEventAdmin() {
		return eventAdmin;
	}

	public void setEventAdmin(Scriptcontroller eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	public ArrayList<SampleStage> getStages() {
		return stages;
	}

	public void setStages(ArrayList<SampleStage> stages) {
		this.stages = stages;
	}

	private void print(String message) {
		InterfaceProvider.getTerminalPrinter().print(message);
	}

	public DetectorArm getDetectorArm() {
		return detectorArm;
	}

	public void setDetectorArm(DetectorArm detectorArm) {
		this.detectorArm = detectorArm;
	}

	public Scannable getSampleNameScannable() {
		return sampleNameScannable;
	}

	public void setSampleNameScannable(Scannable sampleNameScannable) {
		this.sampleNameScannable = sampleNameScannable;
	}

	public Scannable getCalibrantNameScannable() {
		return calibrantNameScannable;
	}

	public void setCalibrantNameScannable(Scannable calibrantNameScannable) {
		this.calibrantNameScannable = calibrantNameScannable;
	}

	public Scannable getDatareduction() {
		return datareduction;
	}

	public void setDatareduction(Scannable datareduction) {
		this.datareduction = datareduction;
	}

	public Detector getPixium() {
		return pixium;
	}

	public void setPixium(Detector pixium) {
		this.pixium = pixium;
	}

	@Override
	public void update(Object source, Object arg) {
		if (source instanceof JythonServerFacade && arg instanceof ScanEvent) {
			ScanEvent event=((ScanEvent)arg);
			EventType type = event.getType();
			ScanInformation latestInformation = event.getLatestInformation();
			int currentPointNumber = event.getCurrentPointNumber();
			ScanStatus latestStatus = event.getLatestStatus();
			String message = null;
			if (type==EventType.STARTED) {
				message="Start scan "+ latestInformation.getScanNumber();
			} else if (type==EventType.UPDATED) {
				message="Scan point " + currentPointNumber + " is collected.";
			} else if (type==EventType.FINISHED) {
				message="Scan "+latestInformation.getScanNumber()+" completed.";
			}
			updateMessage(null, message);
		}
	}
}
