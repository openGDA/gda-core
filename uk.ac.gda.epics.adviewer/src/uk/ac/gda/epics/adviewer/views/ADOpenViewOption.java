/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer.views;

import gda.rcp.views.OpenViewOption;

public class ADOpenViewOption extends OpenViewOption {

	private String pvPrefix;
	private String detectorName;

	public ADOpenViewOption(String label, String viewId) {
		super(label, viewId);
	}

	public void setPvPrefix(String pvPrefix) {
		this.pvPrefix = pvPrefix;
	}

	@Override
	public String getSecondaryId() {
		return ADUtils.getPVServiceName(detectorName, pvPrefix);
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

}
