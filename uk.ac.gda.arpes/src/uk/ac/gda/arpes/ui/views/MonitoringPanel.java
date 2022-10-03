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

package uk.ac.gda.arpes.ui.views;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.apres.ui.config.HRMonitoringPerspectiveConfiguration;
import uk.ac.gda.client.livecontrol.ControlSet;

public class MonitoringPanel extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(MonitoringPanel.class);

	@Override
	public void createPartControl(Composite parent) {

		HRMonitoringPerspectiveConfiguration config;
		try {
			config = Finder.findLocalSingleton(HRMonitoringPerspectiveConfiguration.class);
		} catch (IllegalArgumentException exception) {
			logger.error("HRMonitoringPerspectiveConfiguration is not a singleton", exception);
			return;
		}

		parent.setLayout(new RowLayout(SWT.HORIZONTAL));

		if (config.getTemperatureControls() != null) {
			addControlGroup(parent, "Temperatures", 3, config.getTemperatureControls());
		} else {
			logger.warn("No temperature controls defined in HRMonitoringPerspectiveConfiguration");
		}

		if (config.getPressureControls() != null) {
			addControlGroup(parent, "Pressures", 1, config.getPressureControls());
		} else {
			logger.warn("No pressure controls defined in HRMonitoringPerspectiveConfiguration");
		}
	}

	private void addControlGroup(Composite parent, String groupName, int numColumns, ControlSet controls) {
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setText(groupName);
		GridLayoutFactory.fillDefaults().numColumns(numColumns).applyTo(group);
		if (controls.getControls() != null) {
			controls.getControls().stream().forEach(control -> control.createControl(group));
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
}
