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
	/**
	 * Name of the dark-field collector to include as a per-scan monitor in a mapping scan. Can be null.
	 */
	private String darkFieldCollectorName;

	/**
	 * Name of the flat-field collector to include as a per-scan monitor in a mapping scan. Can be null.
	 */
	private String flatFieldCollectorName;

	/**
	 * Controls whether the "collect dark field" option is set by default
	 */
	private boolean collectDarkByDefault;

	/**
	 * Controls whether the "collect flat field" option is set by default
	 */
	private boolean collectFlatByDefault;

	@Override
	public void createControls(Composite parent) {
		final Composite main = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).applyTo(main);

		GridDataFactory stretch = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);
		stretch.applyTo(main);

		final Button dark = new Button(main, SWT.CHECK);
		dark.setText("Collect dark field");
		stretch.applyTo(dark);

		if (darkFieldCollectorName == null || darkFieldCollectorName.isEmpty()) {
			dark.setEnabled(false);
		} else {
			dark.addListener(SWT.Selection, event -> toggle(dark.getSelection(), darkFieldCollectorName));
			dark.setSelection(getInitialSelectionState(darkFieldCollectorName, collectDarkByDefault));
		}

		final Button flat = new Button(main, SWT.CHECK);
		flat.setText("Collect flat field");
		stretch.applyTo(flat);

		if (flatFieldCollectorName == null || flatFieldCollectorName.isEmpty()) {
			flat.setEnabled(false);
		} else {
			flat.addListener(SWT.Selection, event -> toggle(flat.getSelection(), flatFieldCollectorName));
			flat.setSelection(getInitialSelectionState(flatFieldCollectorName, collectFlatByDefault));
		}
	}

	@Override
	public boolean createSeparator() {
		return true;
	}

	/**
	 * Get the initial selection state for one of the checkboxes
	 *
	 * @param collectorName
	 *            name of the collector (as set in property)
	 * @param collectByDefault
	 *            the "collect by default" property for this collector
	 * @return <code>true</code> if the the collector was already set, or has been set by this function,
	 *         <code>false</code> otherwise
	 */
	private boolean getInitialSelectionState(String collectorName, boolean collectByDefault) {
		final Set<String> monitors = getMonitors();

		if (monitors.contains(collectorName)) {
			return true;
		}
		if (!collectByDefault) {
			return false;
		}
		add(collectorName, monitors);
		return true;
	}

	private void toggle(boolean acquire, String collectorName) {
		if (acquire) {
			add(collectorName, getMonitors());
		} else {
			remove(collectorName);
		}
	}

	private void add(String collectorName, Set<String> monitors) {
		monitors.add(collectorName);
		getMappingBean().setPerScanMonitorNames(monitors);
	}

	private void remove(String collectorName) {
		final Set<String> monitors = getMonitors();
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

	public void setDarkFieldCollectorName(String darkFieldCollectorName) {
		this.darkFieldCollectorName = darkFieldCollectorName;
	}

	public void setFlatFieldCollectorName(String flatFieldCollectorName) {
		this.flatFieldCollectorName = flatFieldCollectorName;
	}

	public void setCollectDarkByDefault(boolean collectDarkByDefault) {
		this.collectDarkByDefault = collectDarkByDefault;
	}

	public void setCollectFlatByDefault(boolean collectFlatByDefault) {
		this.collectFlatByDefault = collectFlatByDefault;
	}
}
