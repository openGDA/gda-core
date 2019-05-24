package uk.ac.diamond.daq.experiment.api.remote;

import gda.device.DeviceException;
import gda.factory.Findable;

public interface PlanRequestHandler extends Findable {


	void submit(PlanRequest planRequest) throws DeviceException;

}
