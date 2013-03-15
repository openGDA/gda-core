package org.opengda.detector.electronanalyser;

import gda.commandqueue.Command;
import gda.commandqueue.CommandBase;
import gda.commandqueue.CommandDetails;
import gda.commandqueue.CommandSummary;
import gda.commandqueue.SimpleCommandDetails;
import gda.commandqueue.SimpleCommandSummary;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;

public class RegionCommand extends CommandBase implements Command {

	private static final long serialVersionUID = 3312489818289239027L;
	CommandDetails  details;
	private Region region;
	private VGScientaAnalyser analyser;
	private boolean repeateUntilStopped=false;
	private boolean confirmAfterEachIteration;
	
	public RegionCommand(Region region) {
		this.region=region;
		try {
			setDetails(region.getName());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setDescription(region.getName());
	}
	@Override
	public CommandDetails getDetails() throws Exception {
		return details;
	}

	@Override
	public void setDetails(String details) throws Exception {
		this.details=new SimpleCommandDetails(details);
	}

	@Override
	public void run() throws Exception {
		beginRun();
		configureCollection(region);
		configureCamera(region);
		addRegionStatusMonitor();
		addProgressMonitor();
		startCollection();
		endRun();
		
	}

	private void addProgressMonitor() {
		// TODO Auto-generated method stub
		
	}
	private void configureCamera(Region region2) throws Exception {
		analyser.setCameraMinX(region.getFirstXChannel());
		analyser.setCameraMinY(region.getFirstYChannel());
		analyser.setCameraSizeX(region.getLastXChannel()-region.getFirstXChannel());
		analyser.setCameraSizeY(region.getLastYChannel()-region.getFirstYChannel());
		analyser.setSlices(region.getSlices());
		analyser.setDetectorMode(region.getDetectorMode().getLiteral());
	}
	private void startCollection() throws Exception {
		analyser.getCollectionStrategy().collectData();
	}
	private void addRegionStatusMonitor() {
		// TODO Auto-generated method stub
		
	}
	private void configureCollection(Region region) throws Exception {
		analyser.setLensMode(region.getLensMode());
		analyser.setAcquisitionMode(region.getAcquisitionMode().getLiteral());
		analyser.setEnergysMode(region.getEnergyMode().getLiteral());
		analyser.setPassEnergy(region.getPassEnergy());
		if (region.getAcquisitionMode()==ACQUISITION_MODE.SWEPT) {
			analyser.setStartEnergy(region.getLowEnergy());
			analyser.setEndEnergy(region.getHighEnergy());
		} else {
			analyser.setCentreEnergy(region.getFixEnergy());
		}
		analyser.setStepTime(region.getStepTime());
		analyser.setEnergyStep(region.getEnergyStep());
		if (!region.getRunMode().isRepeatUntilStopped()) {
			repeateUntilStopped=false;
			analyser.setNumberInterations(region.getRunMode().getNumIterations());
			analyser.setImageMode(ImageMode.SINGLE);
		} else {
			repeateUntilStopped=true;
			analyser.setNumberInterations(1);
			analyser.setImageMode(ImageMode.CONTINUOUS);
		} 
		if (region.getRunMode().isConfirmAfterEachIteration()) {
			confirmAfterEachIteration=true;
		} else {
			confirmAfterEachIteration=false;
		}
	}
	@Override
	public CommandSummary getCommandSummary() throws Exception {
		return new SimpleCommandSummary(getDescription());
	}


}
