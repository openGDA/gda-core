package uk.ac.diamond.daq.experiment.plan;

import gda.factory.FindableBase;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.plan.IPlan;
import uk.ac.diamond.daq.experiment.api.remote.PlanRequest;
import uk.ac.diamond.daq.experiment.api.remote.PlanRequestHandler;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(PlanRequestHandler.class)
public class BasicPlanRequestHandler extends FindableBase implements PlanRequestHandler {
	
	private ExperimentService experimentService;

	@Override
	public void submit(PlanRequest planRequest) {
		PlanRequestParser planRequestParser = new PlanRequestParser(experimentService);
		IPlan plan = planRequestParser.parsePlanRequest(planRequest);
		plan.start();
	}
		
	public void setExperimentService(ExperimentService experimentService) {
		this.experimentService = experimentService;
	}

}
