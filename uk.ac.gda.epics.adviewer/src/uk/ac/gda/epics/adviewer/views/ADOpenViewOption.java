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
import gda.rcp.views.ViewDefinition;
import uk.ac.gda.epics.adviewer.views.ADUtils.ViewType;

public class ADOpenViewOption implements OpenViewOption {

	private String pvPrefix;
	private String detectorName;
	private String suffixType;

	ViewType viewType;
	private String label;

	public ADOpenViewOption(String label, String detectorName, ViewType viewType, String pvPrefix) {
		this(label,detectorName,viewType, pvPrefix, "");
	}
	public ADOpenViewOption(String label, String detectorName, ViewType viewType, String pvPrefix, String suffixType) {
		this.label = label;
		this.detectorName = detectorName;
		this.viewType = viewType;
		this.pvPrefix = pvPrefix;
		this.suffixType = suffixType;
	}

	@Override
	public ViewDefinition getViewDefinition() {
		return new ViewDefinition(ADUtils.getViewId(viewType), ADUtils.getPVServiceName(detectorName, pvPrefix, suffixType));
	}

	@Override
	public String getLabel() {
		return label;
	}

}
