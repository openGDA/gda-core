package org.opengda.lde.experiments;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.jython.ScriptBase;
import gda.jython.commands.ScannableCommands;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;
import gda.scan.Scan.ScanStatus;
import gda.scan.ScanEvent;
import gda.scan.ScanEvent.EventType;
import gda.scan.ScanInformation;
import gda.util.Sleep;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.opengda.lde.events.ProcessMessage;
import org.opengda.lde.events.SampleChangedEvent;
import org.opengda.lde.events.SampleProcessingEvent;
import org.opengda.lde.events.SampleStatusEvent;
import org.opengda.lde.events.StageChangedEvent;
import org.opengda.lde.model.ldeexperiment.STATUS;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class DataCollection extends ScriptBase implements IObserver, InitializingBean, Findable, Configurable {
	private static final Logger logger = LoggerFactory
			.getLogger(DataCollection.class);
	private ArrayList<SampleStage> stages = new ArrayList<SampleStage>();
	private MultiMap<String, List<Sample>> stageActiveSamplesMap = new MultiValueMap<String, List<Sample>>();
	private List<Sample> samples = new ArrayList<Sample>();
	
	private DetectorArm detectorArm;
	private LDEResourceUtil resUtil;
	private Scriptcontroller eventAdmin;
	private Scannable sampleNameScannable;
	private Scannable calibrantNameScannable;
	private Scannable datareduction;
	private Detector pixium;
	private String name;
	private boolean configured;
	private int numActiveSamples;
	private int currentSampleNumber;
	private String currentSampleName="";
	private String dataDriver;
	private String dataFolder;
	private String beamlineID;


	public void parkAllStages() throws DeviceException, InterruptedException {
		String message = "Parking all stages...";
		updateMessage(null, message);
		if (getStages() != null && !getStages().isEmpty()) {
			for (SampleStage stage : getStages()) {
				stage.parkStage();
				checkForPauseAndInterruption();
			}
			// wait for all stages to be parked
			waitForAllStagesToBeParked();
		}
	}

	private void waitForAllStagesToBeParked() throws DeviceException, InterruptedException {
		String message;
		boolean allParked = false;
		while (!allParked) {
			boolean parked = true;
			for (SampleStage stage : getStages()) {
				parked = stage.isParked() && parked;
				checkForPauseAndInterruption();
			}
			allParked = parked;
		}
		message="All stages are parked.";
		updateMessage(null, message);
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
	public void collectData(String filename) throws InterruptedException {
		prepareDataCollection(filename);
		processStages();
	}

	public void pause() {
		String message="Pause current data collection.";
		updateMessage(null, message);
		setPaused(true);
	}
	public void resume() {
		String message="Resume current data collection.";
		updateMessage(null, message);
		setPaused(false);
	}
	public void skip() {
		String message;
		int scanStatus = InterfaceProvider.getScanStatusHolder().getScanStatus();
		if (scanStatus==ScanStatus.RUNNING.asJython() || scanStatus == ScanStatus.PAUSED.asJython()) {
			int scanNumber = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getScanNumber();
			message = "Skip current scan "+ scanNumber;
			updateMessage(null, message);
			InterfaceProvider.getCurrentScanController().requestFinishEarly();
		} else {
			message="No active sample scan to skip at the moment.";
			updateMessage(null, message);
		}
	}
	private void checkForPauseAndInterruption() throws InterruptedException {
		try {
			super.checkForPauses();
		} catch (InterruptedException e) {
			throw new InterruptedException("Data Collections are interrupted.");
		}
	}
	/**
	 * load the samples from experiment definition file, group active or selected samples into stages, and ensure all stages are in safe positions. 
	 * @param filename
	 * @throws InterruptedException 
	 */
	public void prepareDataCollection(String filename) throws InterruptedException {
		String message="Prepare data collection ...";
		updateMessage(null, message);
		stageActiveSamplesMap.clear();
		numActiveSamples = 0;
		currentSampleNumber=0;
		currentSampleName="";
		if (resUtil != null) {
			// load samples definition from .lde file
			try {
				samples = resUtil.getSamples(filename);
			} catch (Exception e) {
				message = "Cannot load samples from experiment sample definition file "+ filename + ".";
				updateMessage(e, message);
				//stop when cannot load in sample data
				throw new IllegalStateException(message, e);
			}
		}
		checkForPauseAndInterruption();
		if (samples != null && !samples.isEmpty()) {
			// group sample into stage using stage name as key
			for (Sample sample : samples) {
				if (sample.isActive()) {
					stageActiveSamplesMap.put(sample.getCellID().split("-")[0].toLowerCase(), sample);
					numActiveSamples++;
					if (eventAdmin!=null) {
						((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleStatusEvent(sample.getSampleID(), STATUS.READY));
					}
				}
			}
		}
		if (stageActiveSamplesMap.isEmpty()) {
			message = "No sample is selected in the table for data collection.";
			updateMessage(null, message);
			//stop when no active sample
			throw new IllegalStateException(message);
		}
		for (SampleStage stage : getStages()) {
			//initialize stage cached states
			stage.setSamplesProcessed(false);
			stage.setDetectorCalibrated(false);
		}
		checkForPauseAndInterruption();
		
		if (eventAdmin!=null) {
			((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleProcessingEvent(currentSampleName, currentSampleNumber, numActiveSamples));
		}
		for (SampleStage stage : getStages()) {
			try {
				if (!stage.isParked()) {
					try {
						stage.parkStage();
						checkForPauseAndInterruption();
					} catch (DeviceException e) {
						message="Failed to park stage '"+stage.getName()+"' before data collection";
						updateMessage(e, message);
					}
				}
			} catch (DeviceException e) {
				message="Failed on checking stage '"+stage.getName()+"' is at parkin poistion or not.";
				updateMessage(e, message);
			}
		}
		checkForPauseAndInterruption();
		try {
			if (!detectorArm.isParked()) {
				detectorArm.parkDetector();
			}
		} catch (DeviceException e) {
			message="Failed to park or check detector '"+detectorArm.getName()+"' is at parkin poistion or not.";
			updateMessage(e, message);
		}
		checkForPauseAndInterruption();
		try {
			waitForAllStagesToBeParked();
		} catch (DeviceException e) {
			message = "Failed to park all stages before data collection process start.";
			updateMessage(e, message);
			//stop when any stage cannot be parked for safety reason.
			throw new IllegalStateException(message, e);
		}
		try {
			while (!detectorArm.isParked()) {
				Sleep.sleep(100);
			}
		} catch (DeviceException e) {
			message="Failed on checking detector '"+detectorArm.getName()+"' is at parkin poistion or not.";
			updateMessage(e, message);
			//stop when detector cannot be parked for safety reason.
			throw new IllegalStateException(message, e);
		}
		message="There are "+numActiveSamples+" samples to process.";
		updateMessage(null, message);
	}
	/**
	 * process samples on each stage in order, engage the stage when its samples are processed or it has no samples except the last stage.
	 * @throws InterruptedException 
	 */
	public void processStages() throws InterruptedException {
		checkForPauseAndInterruption();
		String message="Processing sample stages down the beam direction ...";
		for (SampleStage stage : getStages()) {
			@SuppressWarnings("unchecked")
			List<Sample> samples = (List<Sample>) stageActiveSamplesMap.get(stage.getName());
			if (samples != null && !samples.isEmpty()) {
				//stage has samples to process
				processSamples(stage);
			}
			checkForPauseAndInterruption();
			if (stage != getStages().get(getStages().size() - 1)) {
				//engage the stage if it is not the last stage down the beam.
				try {
					engageStage(stage);
				} catch (DeviceException e) {
					message="Engage stage '" + stage.getName()+ "' failed: "+e.getMessage();
					updateMessage(e, message);
				}
			}
		}

		// all stages are done now
		message="All selected samples on all stages are processed.";
		updateMessage(null, message);
		//parking the detector
		try {
			if (!detectorArm.isParked()) {
				detectorArm.parkDetector();
			}
		} catch (DeviceException e) {
			message="Failed to park or check detector '"+detectorArm.getName()+"' is at parkin poistion or not.";
			updateMessage(e, message);
		}
		//parking all stages
		try {
			parkAllStages();
		} catch (DeviceException e) {
			message = "Park all stage failed: "+ e.getMessage();
			updateMessage(e, message);
		}
		try {
			while (!detectorArm.isParked()) {
				Sleep.sleep(100);
			}
		} catch (DeviceException e) {
			message="Failed on checking detector '"+detectorArm.getName()+"' is at parkin poistion or not.";
			updateMessage(e, message);
		}
		message="Data collection completed!";
		updateMessage(null, message);
	}
	/**
	 * process samples a a stage:
	 * 1st move detector in position relative to the satge;
	 * 2nd do detector calibration with the calibrant on the stage;
	 * 3rd do diffraction collection for all samples on the stage.
	 * @param stage
	 * @throws InterruptedException 
	 */
	private void processSamples(SampleStage stage) throws InterruptedException {
		moveDetectorToPosition(stage);
		doDetectorCalibration(stage);
		doSampleDataCollection(stage);
	}
	/**
	 * Only support one position per stage - all samples and calibrant on the stage must have the same distance from the detector.
	 * @param stage
	 * @throws InterruptedException 
	 */
	private void moveDetectorToPosition(SampleStage stage) throws InterruptedException {
		@SuppressWarnings("unchecked")
		List<Sample> samples = (List<Sample>) stageActiveSamplesMap.get(stage.getName());
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
		if (eventAdmin!=null) {
			((ScriptControllerBase)eventAdmin).update(eventAdmin, new StageChangedEvent(stage.getName(), samples.size()));
			((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleChangedEvent(sample1.getSampleID()));
		}
		checkForPauseAndInterruption();

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
				detectorArm.getZMotor().asynchronousMoveTo(stage.getzPosition()+sampleDetectorZPosition);
			} catch (DeviceException e) {
				message = "Failed to move detector to position "+sampleDetectorZPosition+" relative to stage '"+stage.getName()+"'";
				updateMessage(e, message);
			}
		}
		try {
			while (!detectorArm.isAtPosition(sample1, stage.getzPosition())){
				checkForPauseAndInterruption();
				Sleep.sleep(100);
			}
		} catch (DeviceException e) {
			message = "Failed to check detector at position for stage '"+stage.getName()+"'";
			updateMessage(e, message);
		}
		try {
			message="Detector is at position ("+detectorArm.getXMotor().getPosition()+", "+detectorArm.getYMotor().getPosition()+", "+detectorArm.getZMotor().getPosition()+") for stage '"+stage.getName()+"'";
			updateMessage(null, message);
		} catch (DeviceException e) {
			message="Failed to get Detector position for stage '"+stage.getName()+"'";
			updateMessage(e, message);
		}
	}
	/**
	 * 
	 * @param stage
	 * @throws InterruptedException 
	 */
	private void doDetectorCalibration(SampleStage stage) throws InterruptedException {
		@SuppressWarnings("unchecked")
		List<Sample> samples = (List<Sample>) stageActiveSamplesMap.get(stage.getName());
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
		checkForPauseAndInterruption();

		if (eventAdmin!=null) {
			((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleProcessingEvent(calibrant, currentSampleNumber, numActiveSamples));
		}
		
		//move calibrant into beam
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
		try {
			while (!stage.isAtCalibrantPosition(sample1)) {
				checkForPauseAndInterruption();
				Sleep.sleep(100);
			}
		} catch (DeviceException e) {
			message="Failed on checking stage '"+stage.getName()+" in position or not.";
			updateMessage(e, message);
			throw new IllegalStateException(message, e);
		}
		//set data directory
		LocalProperties.set(LocalProperties.GDA_DATAWRITER_DIR, getDataDirectory(sample1));
		// collect calibrant diffraction data with data reduction
		InterfaceProvider.getJSFObserver().addIObserver(this);
		checkForPauseAndInterruption();
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
	private String getDataDirectory(Sample sample) {
		String dataDir=File.separator;
		if (getDataDriver()!=null && !getDataDriver().isEmpty()) {
			dataDir += getDataDriver()+File.separator;
		}
		if (getBeamlineID()!=null && !getBeamlineID().isEmpty()) {
			dataDir += getBeamlineID()+File.separator;
		}
		if (getDataFolder()!=null && !getDataFolder().isEmpty()) {
			dataDir += getDataFolder()+File.separator;
		}
		dataDir += Calendar.getInstance().get(Calendar.YEAR)+File.separator+sample.getVisitID();
		return dataDir;
	}
	public String getDataDriver() {
		return dataDriver;
	}

	public void setDataDriver(String dataDriver) {
		this.dataDriver = dataDriver;
	}

	public String getDataFolder() {
		return dataFolder;
	}

	public void setDataFolder(String dataFolder) {
		this.dataFolder = dataFolder;
	}
	public String getBeamlineID() {
		return beamlineID;
	}

	public void setBeamlineID(String beamlineID) {
		this.beamlineID = beamlineID;
	}


	private void doSampleDataCollection(SampleStage stage) throws InterruptedException {
		String message="Starting diffraction data collection for samples on stage '"+stage.getName()+"'...";
		updateMessage(null, message);
		@SuppressWarnings("unchecked")
		List<Sample> samples = (List<Sample>) stageActiveSamplesMap.get(stage.getName());
		boolean success=true;
		for (Sample sample : samples) {
			checkForPauseAndInterruption();
			currentSampleName = sample.getName();
			currentSampleNumber++;
			if (getSampleNameScannable()!=null) {
				try {
					getSampleNameScannable().moveTo(currentSampleName);
				} catch (DeviceException e) {
					message="Failed to set sample name to '"+currentSampleName+"' on the stage '"+stage.getName()+"'.";
					updateMessage(e, message);
				}
			}
			if (eventAdmin!=null) {
				((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleProcessingEvent(currentSampleName, currentSampleNumber, numActiveSamples));
				((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleChangedEvent(sample.getSampleID()));
			}
			checkForPauseAndInterruption();

			Scannable x_motor = stage.getXMotor();
			Double x_start = sample.getSample_x_start();
			Double x_stop = sample.getSample_x_stop();
			Double x_step = sample.getSample_x_step();
			
			if (x_start==Double.NaN || x_step==Double.NaN || x_stop==Double.NaN) {
				message="Missing parameters: Sample '"+name+"' must have X motor positions being provided for start, stop, and step.";
				updateMessage(null, message);
				// stop as scan will fail
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
			//set data directory
			LocalProperties.set(LocalProperties.GDA_DATAWRITER_DIR, getDataDirectory(sample));
			checkForPauseAndInterruption();

			if (eventAdmin!=null) {
				((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleStatusEvent(sample.getSampleID(), STATUS.RUNNING));
			}
			InterfaceProvider.getJSFObserver().addIObserver(this);
			try {
				ScannableCommands.scan(scanparameters);
				success = true && success;
			} catch (Exception e) {
				message="Scan failed during sample '"+name+"' diffraction collection: "+e.getMessage();
				updateMessage(e, message);
				if (eventAdmin!=null) {
					((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleStatusEvent(sample.getSampleID(), STATUS.ERROR));
				}
				success=false;
			} finally {
				InterfaceProvider.getJSFObserver().deleteIObserver(this);
			}
			
			if (success && eventAdmin!=null) {
				((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleStatusEvent(sample.getSampleID(), STATUS.COMPLETED));
			}
		}
		if (success) {
			stage.setSamplesProcessed(true);
		} else {
			stage.setSamplesProcessed(false);
		}
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
//			ScanStatus latestStatus = event.getLatestStatus();
			String message = null;
			if (type==EventType.STARTED) {
				message="Start scan "+ latestInformation.getScanNumber();
			} else if (type==EventType.UPDATED) {
				message="Collect scan point " + currentPointNumber + ".";
			} else if (type==EventType.FINISHED) {
				message="Scan "+latestInformation.getScanNumber()+" completed.";
			}
			updateMessage(null, message);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (detectorArm==null) {
			throw new IllegalStateException("detector arm scannable group is missing.");
		}
		if (resUtil==null) {
			throw new IllegalStateException("Resource Utility 'resUtil' is missing.");
		}
		if (eventAdmin==null) {
			throw new IllegalStateException("Script controller 'eventAdmin' is missing.");
		}
		if (sampleNameScannable==null) {
			throw new IllegalStateException("Scannable 'sampleNameScannable' is missing.");
		}
		if (calibrantNameScannable==null) {
			throw new IllegalStateException("Scannable 'calibrantNameScannable' is missing.");
		}
		if (datareduction==null) {
			throw new IllegalStateException("Scannable 'datareduction' is missing.");
		}
		if (pixium==null) {
			throw new IllegalStateException("Detector 'pixium' is missing.");
		}
	}

	@Override
	public void setName(String name) {
		this.name=name;		
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			configured=true;
		}
		
	}
}
