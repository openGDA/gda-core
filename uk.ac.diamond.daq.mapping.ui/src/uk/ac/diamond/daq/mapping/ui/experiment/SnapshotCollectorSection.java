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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class SnapshotCollectorSection extends AbstractMappingSection {
	/**
	 * Name of the snapshot collector to include as a per-scan monitor in a mapping scan. Can be null.
	 */
	private String snapshotCollectorName;
	private String collectorPrettyName;

	/**
	 * Controls whether the "collect snapshot" option is set by default
	 */
	private boolean collectSnapshotByDefault;

	@Override
	public void createControls(Composite parent) {
		final Composite main = createComposite(parent, 2, true);
		((GridLayout) main.getLayout()).makeColumnsEqualWidth = true;

		GridDataFactory stretch = GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);
		final Button snapshot = new Button(main, SWT.CHECK);
		snapshot.setText("Collect snapshot");
		snapshot.setToolTipText("Collect a "+collectorPrettyName+" snapshot at the start of a scan, which will be saved in the scan nxs file");
		stretch.applyTo(snapshot);

		if (snapshotCollectorName == null || snapshotCollectorName.isEmpty()) {
			snapshot.setEnabled(false);
		} else {
			snapshot.addListener(SWT.Selection, event -> toggle(snapshot.getSelection(), snapshotCollectorName));
			snapshot.setSelection(getInitialSelectionState(snapshotCollectorName, collectSnapshotByDefault));
		}
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
		getBean().setPerScanMonitorNames(monitors);
	}

	private void remove(String collectorName) {
		final Set<String> monitors = getMonitors();
		monitors.remove(collectorName);
		getBean().setPerScanMonitorNames(monitors);
	}

	private Set<String> getMonitors() {
		Set<String> monitors = getBean().getPerScanMonitorNames();
		if (monitors == null) {
			monitors = new HashSet<>();
		}
		return monitors;
	}

	public void setSnapshotCollectorName(String snapshotCollectorName) {
		this.snapshotCollectorName = snapshotCollectorName;
	}

	public void setCollectorPrettyName(String collectorPrettyName) {
		this.collectorPrettyName = collectorPrettyName;
	}

	public void setCollectSnapshotByDefault(boolean collectSnapshotByDefault) {
		this.collectSnapshotByDefault = collectSnapshotByDefault;
	}
}
