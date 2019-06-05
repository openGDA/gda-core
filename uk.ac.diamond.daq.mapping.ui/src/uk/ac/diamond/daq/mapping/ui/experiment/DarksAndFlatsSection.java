/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * UI to toggle dark/flat field collection for mapping scan.
 */
public class DarksAndFlatsSection extends AbstractMappingSection {

	private final String darkFieldCollectorName;
	private final String flatFieldCollectorName;

	/**
	 * Takes the names of the dark/flat-field collectors (if any) which can be included
	 * as per-scan monitors in a mapping scan.
	 */
	public DarksAndFlatsSection(String darkFieldCollectorName, String flatFieldCollectorName) {
		this.darkFieldCollectorName = darkFieldCollectorName;
		this.flatFieldCollectorName = flatFieldCollectorName;
	}

	@Override
	public void createControls(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(main);

		GridDataFactory stretch = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);
		stretch.applyTo(main);

		Button dark = new Button(main, SWT.CHECK);
		dark.setText("Collect dark field");
		stretch.applyTo(dark);


		if (darkFieldCollectorName == null || darkFieldCollectorName.isEmpty()) {
			dark.setEnabled(false);
		} else {
			dark.addListener(SWT.Selection, event -> toggle(dark.getSelection(), darkFieldCollectorName));
			dark.setSelection(getMonitors().contains(darkFieldCollectorName));
		}

		Button flat = new Button(main, SWT.CHECK);
		flat.setText("Collect flat field");
		stretch.applyTo(flat);
		if (flatFieldCollectorName == null || flatFieldCollectorName.isEmpty()) {
			flat.setEnabled(false);
		} else {
			flat.addListener(SWT.Selection, event -> toggle(flat.getSelection(), flatFieldCollectorName));
			flat.setSelection(getMonitors().contains(flatFieldCollectorName));
		}
	}

	@Override
	public boolean createSeparator() {
		return true;
	}

	private void toggle(boolean acquire, String collectorName) {
		if (acquire) {
			add(collectorName);
		} else {
			remove(collectorName);
		}
	}

	private void add(String collectorName) {
		Set<String> monitors = getMonitors();
		monitors.add(collectorName);
		getMappingBean().setPerScanMonitorNames(monitors);
	}

	private void remove(String collectorName) {
		Set<String> monitors = getMonitors();
		monitors.remove(collectorName);
		getMappingBean().setPerScanMonitorNames(monitors);
	}

	private Set<String> getMonitors() {
		Set<String> monitors = getMappingBean().getPerScanMonitorNames();
		if (monitors == null) {
			monitors = new HashSet<>();
		}
		return monitors;
	}

}
