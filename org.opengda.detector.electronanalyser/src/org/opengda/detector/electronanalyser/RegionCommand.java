package org.opengda.detector.electronanalyser;

import gda.commandqueue.Command;
import gda.commandqueue.CommandBase;
import gda.commandqueue.CommandDetails;
import gda.commandqueue.CommandSummary;
import gda.commandqueue.SimpleCommandDetails;
import gda.commandqueue.SimpleCommandSummary;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.util.Sleep;

import java.io.Serializable;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.ACQUISITION_MODE;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionCommand extends CommandBase implements Command, Serializable{

	private static final long serialVersionUID = 3312489818289239027L;
	CommandDetails details;
	private Region region;
	private VGScientaAnalyser analyser;
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
			if (region.getRunMode().isRepeatUntilStopped()) {
				while (region.getRunMode().isRepeatUntilStopped() && !(getState() == STATE.ABORTED)) {
					startCollection();
					analyser.getCollectionStrategy().waitWhileBusy();
					while (getState() == STATE.PAUSED) {
						Sleep.sleep(1000);
					}
				}
			} else {
				int i = 0;
				while (region.getRunMode().getNumIterations() > i && !(getState() == STATE.ABORTED)) {
					startCollection();
					analyser.getCollectionStrategy().waitWhileBusy();
					i++;
					while (getState() == STATE.PAUSED) {
						Sleep.sleep(1000);
					}
				}
			}
		}
		endRun();
	}

	@Override
	public void abort() {
		try {
			analyser.stop();
		} catch (DeviceException e) {
			logger.error("Exception throw on stopping analyser.", e);
		}
		super.abort();
	}

	private void configureCamera(Region region2) throws Exception {
		analyser.setCameraMinX(region.getFirstXChannel());
		analyser.setCameraMinY(region.getFirstYChannel());
		analyser.setCameraSizeX(region.getLastXChannel() - region.getFirstXChannel());
		analyser.setCameraSizeY(region.getLastYChannel() - region.getFirstYChannel());
		analyser.setSlices(region.getSlices());
		analyser.setDetectorMode(region.getDetectorMode().getLiteral());
	}

	private void startCollection() throws Exception {
		analyser.getCollectionStrategy().collectData();
	}

	private void configureCollection(Region region) throws Exception {
		analyser.setLensMode(region.getLensMode());
		analyser.setAcquisitionMode(region.getAcquisitionMode().getLiteral());
		analyser.setEnergysMode(region.getEnergyMode().getLiteral());
		analyser.setPassEnergy(region.getPassEnergy());
		if (region.getAcquisitionMode() == ACQUISITION_MODE.SWEPT) {
			analyser.setStartEnergy(region.getLowEnergy());
			analyser.setEndEnergy(region.getHighEnergy());
		} else {
			analyser.setCentreEnergy(region.getFixEnergy());
		}
		analyser.setStepTime(region.getStepTime());
		analyser.setEnergyStep(region.getEnergyStep());
		if (!region.getRunMode().isConfirmAfterEachIteration()) {
			if (!region.getRunMode().isRepeatUntilStopped()) {
				analyser.setNumberInterations(region.getRunMode().getNumIterations());
				analyser.setImageMode(ImageMode.MULTIPLE);
			} else {
				analyser.setNumberInterations(1);
				analyser.setImageMode(ImageMode.CONTINUOUS);
			}
		} else {
			analyser.setNumberInterations(1);
			analyser.setImageMode(ImageMode.SINGLE);
			throw new NotSupportedException("Confirm after each iteraction is not yet supported");
		}
	}

	@Override
	public CommandSummary getCommandSummary() throws Exception {
		return new SimpleCommandSummary(getDescription());
	}

	public Region getRegion() {
		return region;
	}
}
