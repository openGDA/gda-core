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

package uk.ac.gda.rcp.dashboard.configuration;

import java.util.List;

import gda.factory.FindableBase;

public class DashboardScannables extends FindableBase {

	private List<String> dashboardScannableNames;

	public List<String> getDashboardScannableNames() {
		return dashboardScannableNames;
	}

	public void setDashboardScannableNames(List<String> dashboardScannableNames) {
		this.dashboardScannableNames = dashboardScannableNames;
	}

}
