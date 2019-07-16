package uk.ac.diamond.daq.experiment.api;

import gda.factory.Finder;

public class Services {

	private static ExperimentService experimentService;

	private Services() {
		throw new IllegalAccessError("Static access only");
	}

	public static synchronized ExperimentService getExperimentService() {
		if (experimentService == null) {
			experimentService = Finder.getInstance().findSingleton(ExperimentService.class);
		}
		return experimentService;
	}

}
