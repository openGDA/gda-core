package uk.ac.diamond.daq.experiment.plan;

import java.util.concurrent.ExecutorService;

import gda.device.DeviceException;
import gda.factory.FindableBase;
import uk.ac.diamond.daq.concurrent.ExecutorFactory;
import uk.ac.diamond.daq.experiment.api.plan.IPlan;
import uk.ac.diamond.daq.experiment.api.remote.PlanRequest;
import uk.ac.diamond.daq.experiment.api.remote.PlanRequestHandler;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(PlanRequestHandler.class)
public class BasicPlanRequestHandler extends FindableBase implements PlanRequestHandler {
	
	private ExecutorService executor = ExecutorFactory.singleThread();
	
	@Override
	public void submit(PlanRequest planRequest) throws DeviceException {
		PlanRequestParser planRequestParser = new PlanRequestParser();
		IPlan plan = planRequestParser.parsePlanRequest(planRequest);
		executor.submit(plan::start);
	}

}
