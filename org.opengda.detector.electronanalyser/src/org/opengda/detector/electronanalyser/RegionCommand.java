package org.opengda.detector.electronanalyser;

import gda.commandqueue.Command;
import gda.commandqueue.CommandBase;
import gda.commandqueue.CommandDetails;
import gda.commandqueue.CommandSummary;
import gda.commandqueue.SimpleCommandDetails;
import gda.commandqueue.SimpleCommandSummary;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;

import java.io.Serializable;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionCommand extends CommandBase implements Command, Serializable {

	private static final long serialVersionUID = 3312489818289239027L;
	CommandDetails details;
	private Region region;
	private IVGScientaAnalyser analyser;
	private static final Logger logger = LoggerFactory.getLogger(RegionCommand.class);

	public RegionCommand(Region region) {
		this.region = region;
		try {
			setDetails(region.getName());
		} catch (Exception e) {
			logger.error("cannot set command details", e);
		}
		setDescription(region.getName());
	}

	@Override
	public CommandDetails getDetails() throws Exception {
		return details;
	}

	@Override
	public void setDetails(String details) throws Exception {
		this.details = new SimpleCommandDetails(details);
	}

	@Override
	public void run() throws Exception {
		beginRun();
		configureCollection(region);
		configureCamera(region);
		if (region.getRunMode().isConfirmAfterEachIteration()) {
			throw new NotSupportedException("Confirm after each iteraction is not yet supported");
			// if (region.getRunMode().isRepeatUntilStopped()) {
			// while (region.getRunMode().isRepeatUntilStopped() &&
			// !(getState()==STATE.ABORTED)) {
			// startCollection();
			// analyser.getCollectionStrategy().waitWhileBusy();
			// // TODO ask user for input - continue?
			// // if no, break; yes continue;
			// }
			// } else {
			// int i = 0;
			// while (region.getRunMode().getNumIterations() > i &&
			// !(getState()==STATE.ABORTED)) {
			// startCollection();
			// analyser.getCollectionStrategy().waitWhileBusy();
			// i++;
			// // TODO ask user for input - continue?
			// // if no, break; yes continue;
			// }
			// }
		} else {
			startCollection();
			getAnalyser().waitWhileBusy();
		}
		endRun();
	}

	@Override
	public void abort() {
		try {
			getAnalyser().stop();
		} catch (DeviceException e) {
			logger.error("Exception throw on stopping analyser.", e);
		}
		super.abort();
	}

	private void configureCamera(Region region2) throws Exception {
		getAnalyser().setCameraMinX(region.getFirstXChannel());
		getAnalyser().setCameraMinY(region.getFirstYChannel());
		getAnalyser().setCameraSizeX(region.getLastXChannel() - region.getFirstXChannel());
		getAnalyser().setCameraSizeY(region.getLastYChannel() - region.getFirstYChannel());
		getAnalyser().setSlices(region.getSlices());
		getAnalyser().setDetectorMode(region.getDetectorMode().getLiteral());
	}

	private void startCollection() throws Exception {
		getAnalyser().start();
	}

	private void configureCollection(Region region) throws Exception {
		getAnalyser().setLensMode(region.getLensMode());
		getAnalyser().setEnergysMode(region.getEnergyMode().getLiteral());
		getAnalyser().setPassEnergy(region.getPassEnergy());
		if (region.getAcquisitionMode() == ACQUISITION_MODE.SWEPT) {
			getAnalyser().setStartEnergy(region.getLowEnergy());
			getAnalyser().setEndEnergy(region.getHighEnergy());
		} else {
			getAnalyser().setCentreEnergy(region.getFixEnergy());
		}
		getAnalyser().setStepTime(region.getStepTime());
		getAnalyser().setEnergyStep(region.getEnergyStep() / 1000.0);
		if (!region.getRunMode().isConfirmAfterEachIteration()) {
			if (!region.getRunMode().isRepeatUntilStopped()) {
				getAnalyser().setNumberInterations(region.getRunMode().getNumIterations());
				getAnalyser().setImageMode(ImageMode.SINGLE); // TODO do I need to set this?
			} else {
				getAnalyser().setNumberInterations(100000000);
				getAnalyser().setImageMode(ImageMode.SINGLE);
			}
		} else {
			getAnalyser().setNumberInterations(1);
			getAnalyser().setImageMode(ImageMode.SINGLE);
			throw new NotSupportedException("Confirm after each iteraction is not yet supported");
		}
		getAnalyser().setAcquisitionMode(region.getAcquisitionMode().getLiteral());
	}

	@Override
	public CommandSummary getCommandSummary() throws Exception {
		return new SimpleCommandSummary(getDescription());
	}

	public Region getRegion() {
		return region;
	}

	public IVGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IVGScientaAnalyser analyser) {
		this.analyser = analyser;
	}
}
