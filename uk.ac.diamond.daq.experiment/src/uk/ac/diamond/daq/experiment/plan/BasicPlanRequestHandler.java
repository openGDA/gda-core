/*-
 * Copyright © 2020 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

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
