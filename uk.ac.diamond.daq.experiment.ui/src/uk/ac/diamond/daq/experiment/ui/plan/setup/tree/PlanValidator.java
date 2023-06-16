/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.ui.plan.setup.tree;


import java.util.List;
import java.util.function.Predicate;

import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.remote.SEVListenerRequest;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;

public class PlanValidator {

	private List<Predicate<ExperimentPlanBean>> tests = List.of(
			this::validName,
			this::atLeastOneSegment,
			this::consistentExperimentDriver,
			this::segmentsCorrect,
			this::triggersCorrect);

	public boolean validate(ExperimentPlanBean bean) {
		return tests.stream().allMatch(validator -> validator.test(bean));
	}

	private boolean validName(ExperimentPlanBean bean) {
		return bean.getPlanName() != null && !bean.getPlanName().isEmpty();
	}

	private boolean atLeastOneSegment(ExperimentPlanBean bean) {
		return !bean.getSegmentRequests().isEmpty();
	}

	private boolean consistentExperimentDriver(ExperimentPlanBean bean) {
		var driverBean = bean.getDriverBean();
		if (driverBean == null) return true;
		var driver = driverBean.getDriver();
		var profile = driverBean.getProfile();

		if (driver == null || profile == null) return false;

		return Finder.findSingleton(ExperimentService.class).getDriverProfile(driver, profile, null) != null;
	}

	private boolean segmentsCorrect(ExperimentPlanBean bean) {
		return bean.getSegmentRequests().stream().allMatch(this::sufficientSevInfo);
	}

	private boolean sufficientSevInfo(SEVListenerRequest request) {
		if (request.getSignalSource() == SignalSource.TIME) return true;
		var sev = request.getSampleEnvironmentVariableName();
		return sev != null && !sev.isEmpty();
	}

	private boolean triggersCorrect(ExperimentPlanBean bean) {
		return bean.getSegmentRequests().stream()
				.flatMap(segment -> segment.getTriggerRequests().stream())
				.allMatch(this::triggerCorrect);
	}

	private boolean triggerCorrect(TriggerRequest trigger) {
		return sufficientSevInfo(trigger) && trigger.getScanId() != null;
	}
}
