package uk.ac.diamond.daq.experiment.plan;

import gda.device.DeviceException;
import gda.factory.FindableBase;
import uk.ac.diamond.daq.experiment.api.plan.IPlan;
import uk.ac.diamond.daq.experiment.api.remote.PlanRequest;
import uk.ac.diamond.daq.experiment.api.remote.PlanRequestHandler;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(PlanRequestHandler.class)
public class BasicPlanRequestHandler extends FindableBase implements PlanRequestHandler {
	
	@Override
	public void submit(PlanRequest planRequest) throws DeviceException {
		PlanRequestParser planRequestParser = new PlanRequestParser();
		IPlan plan = planRequestParser.parsePlanRequest(planRequest);
		plan.start();
	}

}
