package org.opengda.lde.experiments;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.opengda.lde.events.CellChangedEvent;
import org.opengda.lde.events.ProcessMessage;
import org.opengda.lde.events.SampleChangedEvent;
import org.opengda.lde.events.SampleProcessingEvent;
import org.opengda.lde.events.SampleStatusEvent;
import org.opengda.lde.events.StageChangedEvent;
import org.opengda.lde.model.ldeexperiment.Cell;
import org.opengda.lde.model.ldeexperiment.STATUS;
import org.opengda.lde.model.ldeexperiment.Sample;
import org.opengda.lde.model.ldeexperiment.Stage;
import org.opengda.lde.scannables.DataReductionScannable;
import org.opengda.lde.utils.LDEResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.DummyScannable;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
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

public class DataCollection extends ScriptBase implements IObserver, InitializingBean, Findable, Configurable {
	private static final Logger logger = LoggerFactory.getLogger(DataCollection.class);
	private List<SampleStage> sampleStages = new ArrayList<SampleStage>();
	private Map<String, Stage> stages=new HashMap<String, Stage>();
	private Map<String, Cell> cells= new HashMap<String, Cell>();
	private Map<String, Sample> samples = new HashMap<String, Sample>();
	private ListMultimap<Cell, Sample> cellActiveSamplesMap = ArrayListMultimap.create();
	
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
	private int numCalibrations;
	private int currentCalibrationNumber;
	private Sample currentSample;
	private Cell currentCell;
	/**
	 * blocking move to park all sampleStages in their safe positions. It only returns after all sampleStages are parked.
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
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
	/**
	 * blocking move to engage vacuum pipe in the X-ray beam for the specified stage.
	 * @param stage
	 * @throws DeviceException
	 */
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
	/**
	 * start LDE data collection.
	 * Data collection starts with all stages in parking positions, proceed from 1st stage and move down 
	 * the X-ray beam direction, collect diffraction data from all active samples on these stages along with 
	 * detector calibration for each of cells having active samples. 
	 * @param filename - the file containing a list of samples to be collected.
	 * @throws InterruptedException
	 */
	public void collectData(String filename) throws InterruptedException {
		prepareDataCollection(filename);
		processStages();
		completeDataCollection();
	}
	
	private void completeDataCollection() {
		String message="Automated data collections for all active samples in all cells on all stages are completed.";
		updateMessage(null, message);
		InterfaceProvider.getTerminalPrinter().print("Data collection completed !");
		cellActiveSamplesMap.clear();
		stages.clear();
		cells.clear();
		samples.clear();
		currentCell=null;
		currentSample=null;
	}

	/**
	 * pause data collection after current sample.
	 * This does NOT pause current sample collection if it already started. 
	 */
	public void pause() {
		String message="Pause current data collection.";
		updateMessage(null, message);
		setPaused(true);
		//pause the current scan used to collect data for current sample.
		InterfaceProvider.getCurrentScanController().pauseCurrentScan();
//		InterfaceProvider.getScriptController().pauseCurrentScript();
		if (eventAdmin!=null && (currentSample!=null || currentCell!=null)) {
			((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleStatusEvent(currentSample.getSampleID(), STATUS.PAUSED));
		}
	}
	/**
	 * resume data collection for next sample.
	 */
	public void resume() {
		String message="Resume current data collection.";
		updateMessage(null, message);
		setPaused(false);
		//resume the current scan used to collect data for current sample.
		InterfaceProvider.getCurrentScanController().resumeCurrentScan();
//		InterfaceProvider.getScriptController().resumeCurrentScript();
		if (eventAdmin!=null && (currentSample!=null || currentCell!=null)) {
			((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleStatusEvent(currentSample.getSampleID(), STATUS.RUNNING));
		}
	}
	/**
	 * skip data collection for current sample or detector calibration, i.e. request early finish.
	 */
	public void skip() {
		String message;
		int scanStatus = InterfaceProvider.getScanStatusHolder().getScanStatus();
		if (scanStatus==ScanStatus.RUNNING.asJython() || scanStatus == ScanStatus.PAUSED.asJython()) {
			int scanNumber = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getScanNumber();
			message = "Skip current scan "+ scanNumber;
			updateMessage(null, message);
			InterfaceProvider.getCurrentScanController().requestFinishEarly();
		} else {
			message="No active sample or calibrant scan to skip at the moment.";
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
	 * prepare for data collection, load the samples from experiment definition file, 
	 * group active or selected samples into cells, and ensure all stages are in safe positions. 
	 * @param filename
	 * @throws InterruptedException 
	 */
	private void prepareDataCollection(String filename) throws InterruptedException {
		String message="Prepare data collection run...";
		updateMessage(null, message);
		cellActiveSamplesMap.clear();
		numActiveSamples = 0;
		numCalibrations=0;
		currentSampleNumber=0;
		currentCalibrationNumber=0;
		currentSampleName="";
		currentSample=null;
		currentCell=null;
		if (resUtil != null) {
			// load samples definition from .lde file
			try {
				samples = resUtil.getSamples(filename);
				cells = resUtil.getCells(filename);
				stages = resUtil.getStages(filename);
			} catch (Exception e) {
				message = "Cannot load samples from experiment sample definition file "+ filename + ".";
				updateMessage(e, message);
				//stop when cannot load in sample data
				throw new IllegalStateException(message, e);
			}
		}
		checkForPauseAndInterruption();
		if (samples != null && !samples.isEmpty()) {
			// group sample into cell using cell as key, reset status
			for (Sample sample : samples.values()) {
				if (sample.isActive()) {
					cellActiveSamplesMap.put(sample.getCell(), sample);
					numActiveSamples++;
					if (eventAdmin!=null) {
						((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleStatusEvent(sample.getSampleID(), STATUS.READY));
					}
				}
			}
			numCalibrations=cellActiveSamplesMap.size(); // one calibrant per sample cell.
		}
		if (cellActiveSamplesMap.isEmpty()) {
			message = "No sample is selected for data collection.";
			updateMessage(null, message);
			//stop when no active sample
			throw new IllegalStateException(message);
		}
		for (SampleStage stage : getStages()) {
			//initialize stage cached states
			stage.setProcessed(false);
			for (Cell cell : stages.get(stage.getName()).getCell()) {
				//initialise cell states
				cell.setProcessed(false);
				cell.setCalibrated(false);
			}
		}
		checkForPauseAndInterruption();
		
		if (eventAdmin!=null) {
			((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleProcessingEvent(currentSampleName, currentSampleNumber, currentCalibrationNumber, numActiveSamples, numCalibrations));
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
				message="Failed on checking stage '"+stage.getName()+"' is at parking poistion or not.";
				updateMessage(e, message);
			}
		}
		checkForPauseAndInterruption();
		try {
			if (!detectorArm.isParked()) {
				detectorArm.parkDetector();
			}
		} catch (DeviceException e) {
			message="Failed to park or check detector '"+detectorArm.getName()+"' is at parking poistion or not.";
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
			message="Failed on checking detector '"+detectorArm.getName()+"' is at parking poistion or not.";
			updateMessage(e, message);
			//stop when detector cannot be parked for safety reason.
			throw new IllegalStateException(message, e);
		}
		message="There are "+numActiveSamples+" samples to process.";
		updateMessage(null, message);
	}

	/**
	 * process samples on each stage in order, engage the stage when its samples are processed or it has no samples 
	 * except the last stage.
	 * @throws InterruptedException 
	 */
	private void processStages() throws InterruptedException {
		checkForPauseAndInterruption();
		String message="Processing each of the sample stages down the X-ray beam direction ...";
		for (SampleStage stage : getStages()) {
			EList<Cell> cells2 = stages.get(stage.getName()).getCell();
			if (eventAdmin!=null) {
				((ScriptControllerBase)eventAdmin).update(eventAdmin, new StageChangedEvent(stage.getName(), cells2.size()));
			}
			if (!cells2.isEmpty()) {
				processStage(stage);
				checkForPauseAndInterruption();
				for (Cell cell : cells2) {
					List<Sample> samples2 = cellActiveSamplesMap.get(cell);
					if (!samples2.isEmpty()) {
						if (eventAdmin != null) {
							((ScriptControllerBase) eventAdmin).update(eventAdmin,new CellChangedEvent(cell.getName(), samples2.size()));
						}
						processCell(cell);
						checkForPauseAndInterruption();
						for (Sample sample : samples2) {
							if (eventAdmin != null) {
								((ScriptControllerBase) eventAdmin).update(eventAdmin,new SampleChangedEvent(sample.getSampleID()));
								((ScriptControllerBase) eventAdmin).update(eventAdmin,new SampleStatusEvent(sample.getSampleID(), STATUS.RUNNING));
							}
							processSample(sample);
							checkForPauseAndInterruption();
						}
					}
					cell.setProcessed(true);
				}
			}
			stage.setProcessed(true);
			
			if (stage != getStages().get(getStages().size() - 1)) {
				// do not need to engage the last and first stages.
				try {
					engageStage(stage);
				} catch (DeviceException e) {
					message = "Engage stage '" + stage.getName() + "' failed: " + e.getMessage();
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
			message="Failed to park or check detector '"+detectorArm.getName()+"' is at parking poistion or not.";
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
			message="Failed on checking detector '"+detectorArm.getName()+"' is at parking poistion or not.";
			updateMessage(e, message);
		}
		message="Data collection completed!";
		updateMessage(null, message);
	}
	
	private void processSample(Sample sample) throws InterruptedException {
		doSampleDataCollection(sample);
	}
	/**
	 * process the specified stage. Move detector to position for the specified stage;
	 * @param stage
	 * @throws InterruptedException
	 */
	private void processStage(SampleStage stage) throws InterruptedException {
		moveDetectorToPosition(stage);
	}
	/**
	 * process the specified cell:
	 * 1st take a photo of the sample cell;
	 * 2nd do detector calibration with the calibrant for the specified cell;
	 * @param cell
	 * @throws InterruptedException 
	 */
	private void processCell(Cell cell) throws InterruptedException {
		currentCell=cell;
		takePhoto(cell);
		doDetectorCalibration(cell);
	}

	private void takePhoto(Cell cell) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Only support one detector position per stage - all samples and calibrant in the same cell on the stage
	 *  <b>must</b> have the same distance from the detector.
	 * @param sampleStage
	 * @throws InterruptedException 
	 */
	private void moveDetectorToPosition(SampleStage sampleStage) throws InterruptedException {
		Stage stage = stages.get(sampleStage.getName());
		double sampleDetectorZPosition=stage.getDetector_z();
		String message="Move detector to its position "+sampleDetectorZPosition+" relative to the stage '"+sampleStage.getName()+"' ...";
		updateMessage(null, message);

		checkForPauseAndInterruption();
		message="";
		try {
			if (!detectorArm.isAtXPosition(stage)) {
				detectorArm.getXMotor().asynchronousMoveTo(stage.getDetector_x());
				message="Move detector X to position "+stage.getDetector_x()+", ";
			}
		} catch (DeviceException e) {
			message="Error check or move detector X motor: "+e.getMessage();
			updateMessage(e, message);
		}
		try {
			if (!detectorArm.isAtYPosition(stage)) {
				detectorArm.getYMotor().asynchronousMoveTo(stage.getDetector_y());
				message +="Move Detector Y to position "+stage.getDetector_y()+", ";
			}
		} catch (DeviceException e) {
			message="Error check or move detector Y motor: "+e.getMessage();
			updateMessage(e, message);
		}

		try {
			if (!detectorArm.isAtZPosition(stage, sampleStage.getzPosition())) {
				detectorArm.getZMotor().asynchronousMoveTo(sampleStage.getzPosition() + sampleDetectorZPosition);
				message += "Move detector Z to position " + (sampleDetectorZPosition + sampleStage.getzPosition())+ " ...";
				updateMessage(null, message);
			}
		} catch (DeviceException e) {
			message = "Failed to check or move detector z to position " + sampleDetectorZPosition + " relative to stage '"
					+ sampleStage.getName() + "'";
			updateMessage(e, message);
		}

		try {
			while (!detectorArm.isAtPosition(stage, sampleStage.getzPosition())){
				checkForPauseAndInterruption();
				Sleep.sleep(100);
			}
		} catch (DeviceException e) {
			message = "Failed to check detector at position for stage '"+sampleStage.getName()+"'";
			updateMessage(e, message);
		}
		try {
			message="Detector is at position ("+detectorArm.getXMotor().getPosition()+", "+detectorArm.getYMotor().getPosition()+", "+detectorArm.getZMotor().getPosition()+") for stage '"+sampleStage.getName()+"'";
			updateMessage(null, message);
		} catch (DeviceException e) {
			message="Failed to get Detector positions for stage '"+sampleStage.getName()+"'";
			updateMessage(e, message);
		}
	}
	/**
	 * 
	 * @param stage
	 * @throws InterruptedException 
	 */
	private void doDetectorCalibration(Cell cell) throws InterruptedException {
		if (cell.isCalibrated()) {
			if (getDatareduction()!=null && getDatareduction() instanceof DataReductionScannable) {
				logger.info("Cell {} is already calibrated. Calibration data file is {}.", cell.getName(), ((DataReductionScannable)getDatareduction()).getCurrentCalibrantDataFilename());
			}
			return;
		}
		String calibrant = cell.getCalibrant();
		String message="Starting detector calibration using calibrant '"+calibrant+"' for cell '"+cell.getCellID()+"' ...";
		updateMessage(null, message);		
		// set calibrant name
		if (getCalibrantNameScannable()!=null) {
			try {
				getCalibrantNameScannable().moveTo(calibrant);
			} catch (DeviceException e) {
				message="Failed to set calibrant name to '"+calibrant+"' for cell '"+cell.getName()+"'.";
				updateMessage(e, message);
			}
		} else {
			throw new IllegalStateException("Missing calibrant name scannable.");
		}
		checkForPauseAndInterruption();
		currentCalibrationNumber++;
		if (eventAdmin!=null) {
			((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleProcessingEvent(calibrant, currentSampleNumber, currentCalibrationNumber, numActiveSamples, numCalibrations));
		}
		
		//move calibrant into beam
		SampleStage stage=Finder.getInstance().find(cell.getStage().getStageID());
		try {
			stage.getXMotor().asynchronousMoveTo(cell.getCalibrant_x());
		} catch (DeviceException e) {
			message="Failed to move stage '"+stage.getName()+"x' to "+cell.getCalibrant_x()+".";
			updateMessage(e, message);
		}
		try {
			stage.getYMotor().asynchronousMoveTo(cell.getCalibrant_y());
		} catch (DeviceException e) {
			message="Failed to move stage '"+stage.getName()+"y' to "+cell.getCalibrant_y()+".";
			updateMessage(e, message);
		}
		try {
			while (!stage.isAtCalibrantPosition(cell)) {
				checkForPauseAndInterruption();
				Sleep.sleep(100);
			}
		} catch (DeviceException e) {
			message="Failed on checking stage '"+stage.getName()+" in position or not.";
			updateMessage(e, message);
			throw new IllegalStateException(message, e);
		}
		//set data directory
		LocalProperties.set(LocalProperties.GDA_DATAWRITER_DIR, getDataDirectory(cell));
		// collect calibrant diffraction data with data reduction
		InterfaceProvider.getJSFObserver().addIObserver(this);
		checkForPauseAndInterruption();
		try {
			if (getDatareduction()!= null){
				if (getDatareduction() instanceof DataReductionScannable) {
					((DataReductionScannable)getDatareduction()).setCalibrant(true);
				}
				logger.info("collect diffraction data from {} with request for post collection data reduction on cluster.", getCalibrantNameScannable().getName());
				ScannableCommands.scan(getDatareduction(), 1,1,1, getPixium(), cell.getCalibrant_exposure());
				cell.setCalibrated(true);
			} else {
				logger.info("collect diffraction data from {} without data reduction post processing.", getCalibrantNameScannable().getName());
				ScannableCommands.scan(new DummyScannable("ds"), 1,1,1, getPixium(), cell.getCalibrant_exposure());
			}
		} catch (Exception e) {
			message="Scan failed during calibrant diffraction collection: "+e.getMessage();
			updateMessage(e, message);
			cell.setCalibrated(false);
		} finally {
			InterfaceProvider.getJSFObserver().deleteIObserver(this);
		}
	}
	
	private String getDataDirectory(Cell cell) {
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
		dataDir += Calendar.getInstance().get(Calendar.YEAR)+File.separator+cell.getVisitID();
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

	private void doSampleDataCollection(Sample sample) throws InterruptedException {
		//check if calibration available
		if (!sample.getCell().isCalibrated()) {
			logger.warn("Calibration is not available for sample {} in cell {}", sample.getName(), sample.getCell().getName());
			String message="Calibration is not available for sample "+sample.getName()+" in cell "+ sample.getCell().getName();
			updateMessage(null, message);
		}
		String message="Starting diffraction data collection for sample '"+sample.getName()+"'...";
		updateMessage(null, message);

		String stageID = sample.getCell().getStage().getStageID();
		SampleStage stage = Finder.getInstance().find(stageID);
		checkForPauseAndInterruption();
		currentSampleName = sample.getName();
		currentSample=sample;
		currentSampleNumber++;
		if (getSampleNameScannable()!=null) {
			try {
				getSampleNameScannable().moveTo(currentSampleName);
			} catch (DeviceException e) {
				message="Failed to set sample name to '"+currentSampleName+"' in cell "+sample.getCell().getName() +" on the stage '"+ stageID+"'.";
				updateMessage(e, message);
			}
		}
		if (eventAdmin!=null) {
			((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleProcessingEvent(currentSampleName, currentSampleNumber, currentCalibrationNumber, numActiveSamples, numCalibrations));
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
		if (datareduction != null) {
			if (getDatareduction() instanceof DataReductionScannable) {
				((DataReductionScannable)getDatareduction()).setCalibrant(false);
				((DataReductionScannable)getDatareduction()).setSampleID(sample.getSampleID());
			}
			scanparameters.add(datareduction);
			logger.info("collect diffraction data from sample {} with request for post collection data reduction on cluster.", sample.getName());
		} else {
			logger.info("collect diffraction data from sample {} without post collection data reduction.", sample.getName());
		}
		//set data directory
		LocalProperties.set(LocalProperties.GDA_DATAWRITER_DIR, getDataDirectory(sample.getCell()));
		checkForPauseAndInterruption();
		
		STATUS status = sample.getStatus();
		InterfaceProvider.getJSFObserver().addIObserver(this);
		if (eventAdmin!=null) {
			((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleStatusEvent(sample.getSampleID(), status=STATUS.RUNNING));
		}
		try {
			ScannableCommands.scan(scanparameters);
		} catch (Exception e) {
			message="Scan failed during sample '"+name+"' diffraction collection: "+e.getMessage();
			updateMessage(e, message);
			status=STATUS.ERROR;
		} finally {
			if (status==STATUS.ERROR) {
				//no-op
			} else if (InterfaceProvider.getCurrentScanController().isFinishEarlyRequested()){
				status=STATUS.ABORTED;
			} else {
				status=STATUS.COMPLETED;
			}
		if (eventAdmin!=null) {
			((ScriptControllerBase)eventAdmin).update(eventAdmin, new SampleStatusEvent(sample.getSampleID(), status));
		}
			InterfaceProvider.getJSFObserver().deleteIObserver(this);
		}
	}

	private void updateMessage(Exception e, String message) {
		if (e != null)
			logger.error(message, e);
		else
			logger.info(message);
		print(message);
		if (eventAdmin != null) {
			((ScriptControllerBase) eventAdmin).update(eventAdmin,new ProcessMessage(message));
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

	public List<SampleStage> getStages() {
		return sampleStages;
	}

	public void setStages(List<SampleStage> sampleStages) {
		this.sampleStages = sampleStages;
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
