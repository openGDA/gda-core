package org.opengda.lde.ui.jobs;

import gda.commandqueue.Command;
import gda.commandqueue.CommandBase;
import gda.commandqueue.CommandDetails;
import gda.commandqueue.CommandSummary;
import gda.commandqueue.SimpleCommandDetails;
import gda.commandqueue.SimpleCommandSummary;
import gda.jython.InterfaceProvider;
import gda.jython.Jython;
import gda.util.Sleep;

import java.io.Serializable;

import org.opengda.lde.model.ldeexperiment.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleCommand extends CommandBase implements Command, Serializable {

	private static final long serialVersionUID = 3312489818289239027L;
	CommandDetails details;
	private Sample sample;
	private static final Logger logger = LoggerFactory.getLogger(SampleCommand.class);

	public SampleCommand(Sample sample) {
		this.sample = sample;
		try {
			setDetails(sample.getName());
		} catch (Exception e) {
			logger.error("cannot set command details", e);
		}
		setDescription(sample.getName());
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
			startCollection();
			while (InterfaceProvider.getScanStatusHolder().getScanStatus()==Jython.RUNNING) {
				Sleep.sleep(500);
			}
		endRun();
	}

	@Override
	public void abort() {
		InterfaceProvider.getCurrentScanController().requestFinishEarly();
		super.abort();
	}

	private void startCollection() throws Exception {
		InterfaceProvider.getCommandRunner().runCommand(sample.getCommand());
	}

	@Override
	public CommandSummary getCommandSummary() throws Exception {
		return new SimpleCommandSummary(getDescription());
	}

	public Sample getSample() {
		return sample;
	}
}
