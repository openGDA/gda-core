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

package uk.ac.diamond.daq.experiment.api.remote;

import java.io.Serializable;
import java.util.List;

import uk.ac.diamond.daq.experiment.api.plan.DriverBean;
import uk.ac.gda.common.entity.Document;

public interface PlanRequest extends Document, Serializable {

	/**
	 * @return
	 * @deprecated use {@link Document#getName()}
	 */
	@Deprecated(since="GDA 9.21")
	String getPlanName();

	/**
	 * @return
	 * @deprecated use {@link Document#getDescription()}
	 */
	@Deprecated(since="GDA 9.21")
	String getPlanDescription();


	DriverBean getDriverBean();


	default boolean isDriverUsed() {
		return getDriverBean() != null;
	}


	List<SegmentRequest> getSegmentRequests();
}
